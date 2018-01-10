package com.nikondsl.daikin;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Strings;
import com.nikondsl.daikin.enums.Fan;
import com.nikondsl.daikin.enums.FanDirection;
import com.nikondsl.daikin.enums.Mode;
import com.nikondsl.daikin.wireless.WirelessDaikin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class DaikinController {

    public static final int THREADS_TO_SCAN = 5;
    public static final int DEFAULT_PORT = 80;

    public static void main(String[] args) throws InterruptedException {
        if (args.length == 0) {
            System.err.println("Possible commands:");
            System.err.println("-scan (192.168.1), possible values: (any subnet to scan ip addresses from 1 tp 255) ");
            System.err.println("-protocol (http), possible values: (http|https)");
            System.err.println("-host (192.168.1.165)");
            System.err.println("-port (80)");
            System.err.println("-power (on), possible values: (on|off)");
            System.err.println("-mode (auto), possible values: (auto|dry|cool|heat|fan)");
            System.err.println("-temp (22), possible values: any integer >= 10 and <= 32");
            System.err.println("-humid (3)");
            System.err.println("-fan (1), possible values: (a|1|2|3|4|5)");
            System.err.println("-direction (), possible values: (|h|v|hv|vh)");
            System.err.println("-verbose (), possible values: (|any)");
            System.err.println("-port (80)");
            System.err.println("-file (), possible values: (any path)");
            System.err.println("-check.every (), possible values: (1-3600 seconds)");
            return;
        }

        for (String arg : args) {
            if ("-scan".equals(arg)) {
                ExecutorService lookUpService = Executors.newFixedThreadPool(THREADS_TO_SCAN);
                for (int i = 1; i <= 255; i++) {
                    final int ip = i;
                    lookUpService.submit(() -> {
                        com.nikondsl.daikin.DaikinBase daikin = getDaikin(args[1], ip, DEFAULT_PORT);
                        java.util.List<String> rows = com.nikondsl.daikin.util.RestConnector.submitGet(daikin, "/common/basic_info", false);
                        if (rows == null) {
                            System.err.println("Scanned " + daikin.getHost() + ", not found");
                            return;
                        }
                        String responseFromAirCon = rows.get(0);
                        if (!responseFromAirCon.startsWith("ret=OK,type=aircon,")) {
                            System.err.println("Scanned " + daikin.getHost() + ", found something");
                            return;
                        }
                        String name = java.net.URLDecoder.decode(responseFromAirCon).replaceAll(".*,name=(.*?),", "$1").replaceAll("icon=.*", "");
                        System.err.println("Found Daikin [" + name + "] at " + daikin.getHost());
                    });
                }
                lookUpService.shutdown();
                lookUpService.awaitTermination(2, TimeUnit.MINUTES);
                return;
            }
        }

        CommandParser cParser = new CommandParser();
        new JCommander(cParser, args);
        DaikinBase daikin = DaikinFactory.createWirelessDaikin(String.format("%1s://%2s", cParser.getProtocol(), cParser.getHost()), Integer.parseInt(cParser.getPort()));

        if (cParser.getCheckEvery() != null && cParser.getCheckEvery().length() > 0) {
            String secondsToSleep = cParser.getCheckEvery().replaceAll("\\D", "");
            if (secondsToSleep.length() > 0) {
                while (true) {
                    int seconds = Integer.parseInt(secondsToSleep);
                    if (seconds > TimeUnit.MINUTES.toMillis(5) || seconds == 0) seconds = 60;
                    try {
                        daikin.readDaikinState(cParser.isVerboseOutput(), true);

                        if (cParser.getWriteToFile() != null && cParser.getWriteToFile().length() > 0) {
                            tryWriteToFile(cParser.getWriteToFile(), daikin);
                        }

                        sleep(seconds);
                    } catch (Exception e) {
                        e.printStackTrace(System.err);
                        sleep(seconds);
                    }
                }
            }
        }


        daikin.readDaikinState(cParser.isVerboseOutput(), true);
        System.err.println("State before: " + daikin);

        daikin.setOn("on".equalsIgnoreCase(cParser.getPower()));
        Mode targetMode = WirelessDaikin.parseMode(cParser.getMode());
        if (targetMode != Mode.None) daikin.setMode(targetMode);
        Fan targetFan = WirelessDaikin.parseFan(cParser.getFan());
        if (targetFan != Fan.None) daikin.setFan(targetFan);
        FanDirection targetFanDirection = WirelessDaikin.parseFanDirection(cParser.getFanDirection());
        if (targetFanDirection != FanDirection.None) daikin.setFanDirection(targetFanDirection);
        int targetTemperature = WirelessDaikin.parseInt(cParser.getTargetTemperature());
        if (targetTemperature >= 10 && targetTemperature <= 32) daikin.setTargetTemperature(targetTemperature);
        int targetHumidity = WirelessDaikin.parseInt(cParser.getTargetHumudity());
        if (targetHumidity > 0 && targetHumidity < 100) daikin.setTargetHumidity(targetHumidity);
        daikin.updateDaikinState(cParser.isVerboseOutput());

        daikin.readDaikinState(cParser.isVerboseOutput(), true);

        System.err.println("State after: " + daikin);
    }

    private static DaikinBase getDaikin(String subNet, final int ip, int port) {
        if (Strings.isStringEmpty(subNet)) subNet = "192.168.1.";
        else subNet = subNet.replaceAll("((1?[0-9]{1,2}|2[0-4][0-9]|25[0-5]\\.){3})", "$1");
        if (!subNet.endsWith(".")) subNet += ".";
        return new DaikinBase("http://" + subNet + ip, port) {

            @Override
            public void updateDaikinState(boolean verboseOutput) {
            }

            @Override
            public void readDaikinState(boolean verboseOutput, boolean restAssuranceOnly) {
            }
        };
    }

    private static void sleep(int seconds) {
        try {
            Thread.currentThread().sleep(TimeUnit.SECONDS.toMillis(seconds));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace(System.err);
        }
    }

    private static void tryWriteToFile(String writeToFile, DaikinBase daikin) throws IOException {
        Path filePath = Paths.get(writeToFile);
        if (!Files.exists(filePath)) {
            filePath = Files.createFile(filePath);
            System.err.println(filePath.toAbsolutePath().toString() + " is created");
        }
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        DateTimeFormatter formatter = builder.appendPattern("u").
                appendLiteral("-").
                appendValue(ChronoField.MONTH_OF_YEAR).
                appendLiteral("-").
                appendValue(ChronoField.DAY_OF_MONTH, 2).
                appendLiteral(" ").
                appendValue(ChronoField.HOUR_OF_DAY, 2).
                appendLiteral(":").
                appendValue(ChronoField.MINUTE_OF_HOUR, 2).
                appendLiteral(":").
                appendValue(ChronoField.SECOND_OF_MINUTE, 2).
                toFormatter();
        LocalDateTime dateTime = LocalDateTime.now();

        String s = dateTime.format(formatter) + ", pow=" + (daikin.isOn() ? "1" : "0") + ", htemp=" + daikin.getInsideTemperature() + ", otemp=" + daikin.getOutsideTemperature() + "\r\n";
        System.out.println(s);
        Files.write(filePath, s.getBytes("UTF-8"), StandardOpenOption.APPEND);
    }
}

package com.nikondsl.daikin;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Strings;
import com.nikondsl.daikin.enums.Fan;
import com.nikondsl.daikin.enums.FanDirection;
import com.nikondsl.daikin.enums.Mode;
import com.nikondsl.daikin.util.RestConnector;
import com.nikondsl.daikin.wireless.WirelessDaikin;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class DaikinController {

    public static final int THREADS_TO_SCAN = 5;
    public static final int DEFAULT_PORT = 80;
	public static final String COMMON_BASIC_INFO = "/common/basic_info";
	
	public static void main(String[] args) throws InterruptedException {
        if (args.length == 0) {
            System.err.println("Possible commands:");
            System.err.println("-scan (192.168.1), possible values: (any subnet to scan ip addresses from 1 tp 255) ");
            System.err.println("-protocol (http), possible values: (http|https)");
            System.err.println("-host (), possible value: (any ipv4 address)");
            System.err.println("-port (80)");
            System.err.println("-power (on), possible values: (on|off)");
            System.err.println("-mode (auto), possible values: (auto|dry|cool|heat|fan)");
            System.err.println("-temp (22), possible values: any integer >= 10 and <= 32");
            System.err.println("-humid (3)");
            System.err.println("-fan (auto), possible values: (silent|auto|1|2|3|4|5)");
            System.err.println("-direction (), possible values: (|h|v|hv|vh)");
            System.err.println("-verbose (), possible values: (|any)");
            System.err.println("-port (80)");
            System.err.println("-file (), possible values: (any path)");
            System.err.println("-check.every (), possible values: (1-3600 seconds)");
            return;
        }
		final DaikinController controller = new DaikinController();
        for (String arg : args) {
            if ("-scan".equals(arg)) {
                final Set<String[]> foundUnits = new ConcurrentSkipListSet<>(new Comparator<String[]>() {
                    @Override
                    public int compare(String[] o1, String[] o2) {
                        return (o1[0] + o1[1]).compareTo(o2[0] + o2[1]);
                    }
                });
                ExecutorService lookUpService = Executors.newFixedThreadPool(THREADS_TO_SCAN);
                for (int i = 1; i <= 255; i++) {
                    final int ip = i;
                    lookUpService.submit(() -> {
                        String[] nameAndAddressOfUnit = controller.checkApiExist(getDaikin(args.length>1?args[1]:"", ip, DEFAULT_PORT));
                        if (nameAndAddressOfUnit != null) foundUnits.add(nameAndAddressOfUnit);
                    });
                }
                lookUpService.shutdown();
                lookUpService.awaitTermination(2, TimeUnit.MINUTES);

                System.err.println("Scanning finished, found " + foundUnits.size() + " units.");
                foundUnits.forEach((String[] nameAndAddressOfUnit) -> {
                    System.err.println("[" + nameAndAddressOfUnit[0] + "] on " + nameAndAddressOfUnit[1]);
                });
                return;
            }
        }

        CommandParser cParser = new CommandParser();
        JCommander.newBuilder()
                .addObject(cParser)
                .build()
                .parse(args);
        DaikinBase daikin = DaikinFactory.createWirelessDaikin(String.format("%1s://%2s", cParser.getProtocol(), cParser.getHost()), Integer.parseInt(cParser.getPort()));
		
		final String[] nameAndAddressOfUnit = controller.checkApiExist(daikin);
        if (cParser.getCheckEvery() != null && cParser.getCheckEvery().length() > 0) {
            String secondsToSleep = cParser.getCheckEvery().replaceAll("\\D", "");
            if (secondsToSleep.length() > 0) {
                while (true) {
                    int seconds = Integer.parseInt(secondsToSleep);
                    if (seconds > TimeUnit.MINUTES.toMillis(5) || seconds == 0) seconds = 60;
                    try {
                        daikin.readDaikinState(cParser.isVerboseOutput());

                        if (cParser.getWriteToFile() != null && cParser.getWriteToFile().length() > 0) {
                            tryWriteToFile(cParser.getWriteToFile(), daikin, nameAndAddressOfUnit);
                        }

                        sleep(seconds);
                    } catch (Exception e) {
                        e.printStackTrace(System.err);
                        sleep(seconds);
                    }
                }
            }
        }
		
        daikin.readDaikinState(cParser.isVerboseOutput());
        System.err.println("State before for [" + nameAndAddressOfUnit + "]: " + daikin);

        daikin.setOn("on".equalsIgnoreCase(cParser.getPower()));
        Mode targetMode = cParser.parseModeConsoleCommand();
        daikin.setMode(targetMode);
        Fan targetFan = cParser.parseFanConsoleCommand();
        daikin.setFan(targetFan);
        FanDirection targetFanDirection = cParser.parseFanDirectionConsoleCommand();
        daikin.setFanDirection(targetFanDirection);
        int targetTemperature = WirelessDaikin.parseInt(cParser.getTargetTemperature());
        if (targetTemperature >= 10 && targetTemperature <= 32) daikin.setTargetTemperature(targetTemperature);
        int targetHumidity = WirelessDaikin.parseInt(cParser.getTargetHumudity());
        if (targetHumidity > 0 && targetHumidity < 100) daikin.setTargetHumidity(targetHumidity);
        daikin.updateDaikinState(cParser.isVerboseOutput());

        daikin.readDaikinState(cParser.isVerboseOutput());
        System.err.println("State after for [" + nameAndAddressOfUnit + "]: " + daikin);
    }

	String[] checkApiExist(DaikinBase daikin) {
        List<String> rows = readIdentificationResponse(daikin);
        if (rows == null) {
            System.err.println("Scanned " + daikin.getHost() + ", not found");
            return null;
        }
        String responseFromAirCon = rows.get(0);
        if (!responseFromAirCon.startsWith("ret=OK,type=aircon,")) {
            System.err.println("Scanned " + daikin.getHost() + ", found something");
            return null;
        }
        String nameOfUnit = "";
        String name = null;
        try {
            nameOfUnit = responseFromAirCon.replaceAll(".*,name=(.*?),", "$1").replaceAll("icon=.*", "");
            name = URLDecoder.decode(nameOfUnit, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            System.err.println("Scanned " + daikin.getHost() + ", found something like Daikin AC, but could not decode " + nameOfUnit);
            return null;
        }
        System.err.println("Found Daikin AC [" + name + "] at " + daikin.getHost());
        return new String[]{name, daikin.getHost()};
    }
	
	List<String> readIdentificationResponse(DaikinBase daikin) {
		return RestConnector.submitGet(daikin, COMMON_BASIC_INFO, false);
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
            public void readDaikinState(boolean verboseOutput) {
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

    private static void tryWriteToFile(String writeToFile, DaikinBase daikin, String[] nameAndAddressOfUnit) throws IOException {
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

        String result = nameAndAddressOfUnit[0] + " (" + nameAndAddressOfUnit[1] + "), " + dateTime.format(formatter) + ", pow=" + (daikin.isOn() ? "1" : "0") + ", htemp=" + daikin.getInsideTemperature() + ", otemp=" + daikin.getOutsideTemperature() + "\r\n";
        System.out.println(result);
        Files.write(filePath, result.getBytes("UTF-8"), StandardOpenOption.APPEND);
    }
}

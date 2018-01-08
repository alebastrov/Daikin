package com.nikondsl.daikin;

import com.beust.jcommander.JCommander;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import com.nikondsl.daikin.enums.Fan;
import com.nikondsl.daikin.enums.FanDirection;
import com.nikondsl.daikin.enums.Mode;
import com.nikondsl.daikin.enums.Timer;
import com.nikondsl.daikin.util.RestConnector;
import com.nikondsl.daikin.wireless.WirelessDaikin;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.SystemDefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class DaikinController {

    public static void main(String[] args) throws InterruptedException {
        if (args.length == 0) {
            System.err.println("Possible commands:");
            System.err.println("-scan");
            System.err.println("-protocol (http), possible values: (http|https)");
            System.err.println("-host (192.168.1.165)");
            System.err.println("-port (80)");
            System.err.println("-power (on), possible values: (on|off)");
            System.err.println("-mode (auto), possible values: (auto|dry|cool|heat|fan)");
            System.err.println("-temp (22)");
            System.err.println("-humid (3)");
            System.err.println("-fan (1), possible values: (a|1|2|3|4|5)");
            System.err.println("-direction (), possible values: (|h|v|hv|vh)");
            System.err.println("-verbose (), possible values: (|any)");

            System.err.println("-file (), possible values: (any path)");
            System.err.println("-check.every (), possible values: (1-3600 seconds)");
            return;
        }

        for (String arg : args) {
            if ("-scan".equals(arg)) {
                HttpClientConfig clientConfig = RestAssuredConfig.config().getHttpClientConfig();
                clientConfig = clientConfig.httpClientFactory(new HttpClientConfig.HttpClientFactory() {
                    @Override
                    public HttpClient createHttpClient() {
                        HttpClient rv = new SystemDefaultHttpClient();
                        HttpParams httpParams = rv.getParams();
                        HttpConnectionParams.setConnectionTimeout(httpParams, 1 * 1000); //Wait 5s for a connection
                        HttpConnectionParams.setSoTimeout(httpParams, 2 * 1000); // Default session is 60s
                        return rv;
                    }
                });
                clientConfig = clientConfig.reuseHttpClientInstance();
                RestAssuredConfig.config().httpClient(clientConfig);
                ExecutorService lookUpService = Executors.newFixedThreadPool(5);
                for (int i = 1; i < 255; i++) {
                    final int ip = i;
                    lookUpService.submit(new Runnable() {
                        @Override
                        public void run() {
                            DaikinBase daikin = getDaikin(ip);
                            System.err.println("Scanning " + daikin.getHost());
                            List<String> rows = RestConnector.submitGet(daikin, "/common/basic_info", false);
                            if (rows == null) return;
                            String responseFromAirCon = rows.get(0);
                            if (!responseFromAirCon.startsWith("ret=OK,type=aircon,")) return;
                            String name = URLDecoder.decode(responseFromAirCon).replaceAll(".*,name=(.*?),", "$1").replaceAll("icon=.*", "");
                            System.err.println("Found Daikin [" + name + "] at " + daikin.getHost());
                        }
                    });
                }
                lookUpService.shutdown();
                lookUpService.awaitTermination(2, TimeUnit.MINUTES);
                return;
            }
        }

        CommandParser cParser = new CommandParser();
        new JCommander(cParser, args);
        DaikinBase daikin = DaikinFactory.createWirelessDaikin(String.format("%1s://%2s", cParser.getProtocol(), cParser.getHost()));

        if (cParser.getCheckEvery() != null && cParser.getCheckEvery().length() > 0) {
            String secondsToSleep = cParser.getCheckEvery().replaceAll("\\D", "");
            if (secondsToSleep.length() > 0) {
                while (true) {
                    int seconds = Integer.parseInt(secondsToSleep);
                    if (seconds > 360_000 || seconds == 0) seconds = 60;
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
        double targetTemperature = WirelessDaikin.parseDouble(cParser.getTargetTemperature());
        if (targetTemperature > 0) daikin.setTargetTemperature(targetTemperature);
        int targetHumidity = WirelessDaikin.parseInt(cParser.getTargetHumudity());
        if (targetHumidity > 0) daikin.setTargetHumidity(targetHumidity);
        daikin.updateDaikinState(cParser.isVerboseOutput());

        daikin.readDaikinState(cParser.isVerboseOutput(), true);

        System.err.println("State after: " + daikin);
    }

    private static DaikinBase getDaikin(final int ip) {
        return new DaikinBase("http://192.168.1." + ip) {

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
            Thread.currentThread().sleep(seconds * 1_000L);
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

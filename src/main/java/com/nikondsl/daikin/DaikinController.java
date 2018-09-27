package com.nikondsl.daikin;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Strings;
import com.diogonunes.jcdp.color.ColoredPrinter;
import com.diogonunes.jcdp.color.api.Ansi;
import com.nikondsl.daikin.enums.Fan;
import com.nikondsl.daikin.enums.FanDirection;
import com.nikondsl.daikin.enums.Mode;
import com.nikondsl.daikin.util.RestConnector;
import com.nikondsl.daikin.wireless.WirelessDaikin;
import lombok.Getter;
import lombok.Setter;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
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
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class DaikinController {
	public static final int LAST_ADDRESS_IN_SUB_NET = 255;
	private final Logger LOG = LogManager.getLogger(DaikinController.class);

    public static final int THREADS_TO_SCAN = 5;
    public static final int DEFAULT_PORT = 80;
	public static final String COMMON_BASIC_INFO = "/common/basic_info";
    ColoredPrinter greenColor = new ColoredPrinter.Builder(1, false)
            .foreground(Ansi.FColor.GREEN).background(Ansi.BColor.BLACK)   //setting format
            .build();
	
	@Setter
    @Getter
	private CommandMode commandMode = CommandMode.COMMAND;
	
	public static void main(String[] args) throws InterruptedException {
        final DaikinController controller = new DaikinController();
        if (args.length == 0) {
            controller.LOG.trace("No args[] provided, printing USAGE.");
            printUsage();
            return;
        }
        for (String arg : args) {
            if ("-scan".equals(arg)) {
                controller.setCommandMode(CommandMode.SCANNING);
                controller.scanSubNet(args, controller);
                return;
            }
        }

        CommandParser cParser = new CommandParser();
        JCommander.newBuilder()
                .addObject(cParser)
                .build()
                .parse(args);
        DaikinBase daikin = DaikinFactory.createWirelessDaikin(String.format("%1s://%2s", cParser.getProtocol(), cParser.getHost()), Integer.parseInt(cParser.getPort()));
		try {
            String[] nameAndAddressOfUnit = controller.checkApiExist(daikin);
            if (cParser.getCheckEvery() != null && cParser.getCheckEvery().length() > 0) {
                controller.writeStateToFile(cParser, daikin, nameAndAddressOfUnit);
                return;
            }
            daikin.readDaikinState();
            controller.LOG.debug("Initial state of unit [" + nameAndAddressOfUnit[0] + "]: " + daikin);

            setParametersForUnit(cParser, daikin);
            daikin.updateDaikinState(); //send command to unit

            daikin.readDaikinState();
            controller.LOG.info("State of unit after sending a command [" + nameAndAddressOfUnit[0] + "]: " + daikin);
        } catch (IOException ex) {
            controller.LOG.error("Could not connect to a Daikin unit: " + daikin, ex);
        }
    }

    private static void printUsage() {
        ColoredPrinter yellowColor = new ColoredPrinter.Builder(1, false)
                .foreground(Ansi.FColor.YELLOW).background(Ansi.BColor.BLACK)   //setting format
                .build();
        ColoredPrinter greenColor = new ColoredPrinter.Builder(1, false)
                .foreground(Ansi.FColor.GREEN).background(Ansi.BColor.BLACK)   //setting format
                .build();
        ColoredPrinter whiteColor = new ColoredPrinter.Builder(1, false)
                .foreground(Ansi.FColor.WHITE).background(Ansi.BColor.BLACK)   //setting format
                .build();

        yellowColor.println("Possible commands:");

        printOptionInUsage(greenColor, whiteColor, "-scan", "(192.168.1), possible values: (any subnet to scan ip addresses from 1 to 255) ");
        printOptionInUsage(greenColor, whiteColor, "-protocol ", "(http), possible values: (http|https)");
        printOptionInUsage(greenColor, whiteColor, "-host ", "(), possible value: (any ipv4 address)");
        printOptionInUsage(greenColor, whiteColor, "-port ", "(80), possible values: any port number");
        printOptionInUsage(greenColor, whiteColor, "-power ", "(on), possible values: (on|off)");
        printOptionInUsage(greenColor, whiteColor, "-mode ", "(auto), possible values: (auto|dry|cool|heat|fan)");
        printOptionInUsage(greenColor, whiteColor, "-temp ", "(22), possible values: any integer >= 10 and <= 32");
        printOptionInUsage(greenColor, whiteColor, "-humid ", "(3), possible values: (1-99)");
        printOptionInUsage(greenColor, whiteColor, "-fan ", "(auto), possible values: (silent|auto|1|2|3|4|5)");
        printOptionInUsage(greenColor, whiteColor, "-direction ", "(), possible values: (|h|v|hv|vh)");
        printOptionInUsage(greenColor, whiteColor, "-file ", "(), possible values: (any path)");
        printOptionInUsage(greenColor, whiteColor, "-check.every ", "(), possible values: (1-3600 seconds)");
    }

    private static void printOptionInUsage(ColoredPrinter printerForOption, ColoredPrinter printerForDescription, String option, String description) {
        printerForOption.print(option + " ");
        printerForDescription.println(description);
    }

    private void scanSubNet(String[] args, DaikinController controller) throws InterruptedException {
        final Set<String[]> foundUnits = new ConcurrentSkipListSet<>((o1, o2) -> (o1[0] + o1[1]).compareTo(o2[0] + o2[1]));
        LOG.info("Scanning started, please wait about couple minutes ...");
        ExecutorService lookUpService = Executors.newFixedThreadPool(THREADS_TO_SCAN);
        for (int i = 1; i <= LAST_ADDRESS_IN_SUB_NET; i++) {
            final int ip = i;
            lookUpService.submit(() -> {
                DaikinBase daikin = getDaikin(args.length > 1 ? args[1] : "", ip, DEFAULT_PORT);
                try {
                    String[] nameAndAddressOfUnit = controller.checkApiExist(daikin);
                    if (nameAndAddressOfUnit != null) {
                        foundUnits.add(nameAndAddressOfUnit);
                    }
                } catch (ConnectTimeoutException | SocketTimeoutException ex) {
                    LOG.debug("Scanned " + daikin.getHost() + ". Nothing found, reason (" + ex.getMessage() + ")");
                } catch (IOException e) {
                    LOG.info("Scanned " + daikin.getHost() + ". Nothing found.", e);
                    return;
                }
            });
        }
        lookUpService.shutdown();
        lookUpService.awaitTermination(2, TimeUnit.MINUTES);

        LOG.info("Scanning finished, found " + foundUnits.size() + " units.");
        foundUnits.forEach((String[] nameAndAddressOfUnit) -> {
            LOG.info("[" + nameAndAddressOfUnit[0] + "] on " + nameAndAddressOfUnit[1]);
        });
    }

    private static void setParametersForUnit(CommandParser cParser, DaikinBase daikin) {
        daikin.setOn("on".equalsIgnoreCase(cParser.getPower()));
        Mode targetMode = Mode.getParser().parseCommand(cParser.getMode());
        daikin.setMode(targetMode);
        Fan targetFan = Fan.getParser().parseCommand(cParser.getFan());
        daikin.setFan(targetFan);
        FanDirection targetFanDirection = FanDirection.getParser().parseCommand(cParser.getFanDirection());
        daikin.setFanDirection(targetFanDirection);
        int targetTemperature = WirelessDaikin.parseInt(cParser.getTargetTemperature());
        if (targetTemperature >= 10 && targetTemperature <= 32) daikin.setTargetTemperature(targetTemperature);
        int targetHumidity = WirelessDaikin.parseInt(cParser.getTargetHumudity());
        if (targetHumidity > 0 && targetHumidity < 100) daikin.setTargetHumidity(targetHumidity);
    }

    private void writeStateToFile(CommandParser cParser, DaikinBase daikin, String[] nameAndAddressOfUnit) {
        String secondsToSleep = cParser.getCheckEvery().replaceAll("\\D", "");
        if (secondsToSleep.length() <= 0) {
            return;
        }
        while (true) {
            int seconds = Integer.parseInt(secondsToSleep);
            if (seconds > TimeUnit.MINUTES.toMillis(5) || seconds == 0) seconds = 60;
            try {
                daikin.readDaikinState();
                if (cParser.getWriteToFile() != null && cParser.getWriteToFile().length() > 0) {
                    tryWriteToFile(cParser.getWriteToFile(), daikin, nameAndAddressOfUnit);
                }
                sleep(seconds);
            } catch (Exception e) {
                LOG.error("Could not read Daikin unit state", e);
                sleep(1);
            }
        }
    }

    String[] checkApiExist(DaikinBase daikin) throws IOException {
        List<String> rows = readIdentificationResponse(daikin);
        if (rows.size()==1 && "".equals(rows.get(0))) {
            LOG.debug("Scanned " + daikin.getHost() + ", not found");
            return new String[0];
        }
        String responseFromAirCon = rows.get(0);
        if (!responseFromAirCon.startsWith("ret=OK,type=aircon,")) {
            LOG.info("Scanned " + daikin.getHost() + ", found something");
            return new String[0];
        }
        String nameOfUnit = "";
        String name = null;
        try {
            nameOfUnit = responseFromAirCon.replaceAll(".*,name=(.*?),", "$1").replaceAll("icon=.*", "");
            name = URLDecoder.decode(nameOfUnit, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            LOG.warn("Scanned " + daikin.getHost() + ", found something like Daikin AC, but could not decode " + nameOfUnit);
            return new String[]{"Unknown-" + daikin.getHost(), daikin.getHost()};
        }
        greenColor.println("Found Daikin AC [" + name + "] at " + daikin.getHost());
//        LOG.info("Found Daikin AC [" + name + "] at " + daikin.getHost());
        return new String[]{name, daikin.getHost()};
    }
	
	List<String> readIdentificationResponse(DaikinBase daikin) throws IOException {
		return RestConnector.submitGet(daikin, COMMON_BASIC_INFO, commandMode);
	}
	
	private static DaikinBase getDaikin(String subNet, final int ip, int port) {
        if (Strings.isStringEmpty(subNet)) subNet = "192.168.1.";
        else subNet = subNet.replaceAll("((1?[0-9]{1,2}|2[0-4][0-9]|25[0-5]\\.){3})", "$1");
        if (!subNet.endsWith(".")) subNet += ".";
        return new DaikinBase("http://" + subNet + ip, port) {
	
			@Override
			protected String getTypeOfUnit() {
				return "Virtual ";
			}
	
			@Override
            public void updateDaikinState() {
            }

            @Override
            public void readDaikinState() {
            }
        };
    }

    private void sleep(int seconds) {
        try {
            Thread.currentThread().sleep(TimeUnit.SECONDS.toMillis(seconds));
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            LOG.error("Could not sleep for " + seconds + " seconds", ex);
        }
    }

    private void tryWriteToFile(String writeToFile, DaikinBase daikin, String[] nameAndAddressOfUnit) throws IOException {
        Path filePath = Paths.get(writeToFile);
        if (!Files.exists(filePath)) {
            filePath = Files.createFile(filePath);
            LOG.info(filePath.toAbsolutePath().toString() + " is created");
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
        LOG.info(result);
        Files.write(filePath, result.getBytes("UTF-8"), StandardOpenOption.APPEND);
    }
}

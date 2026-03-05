package com.nikondsl.daikin.discovery;

import com.nikondsl.daikin.DaikinBase;
import com.nikondsl.daikin.DaikinController;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TcpDiscovery implements AutoCloseable {
    private static final Logger LOG = LogManager.getLogger(TcpDiscovery.class);
    public static final int LAST_ADDRESS_IN_SUB_NET = 255;
    public static final int DEFAULT_PORT = 80;

    public void scanSubNet(String[] args, DaikinController controller) throws InterruptedException {
        Set<String[]> foundUnits = new ConcurrentSkipListSet<>((o1, o2) -> (o1[0] + o1[1]).compareTo(o2[0] + o2[1]));

        LOG.info("==========================================================");
        LOG.info("Scanning started, please wait for about couple minutes ...");
        ExecutorService lookUpService = Executors.newVirtualThreadPerTaskExecutor();
        for (int i = 1; i <= LAST_ADDRESS_IN_SUB_NET; i++) {
            int ip = i;
            lookUpService.submit(() -> {
                DaikinBase daikin = getDaikin(args.length > 1 ? args[1] : "", ip, DEFAULT_PORT);
                try {
                    LOG.trace("Scanning " + daikin.getHost() + "...");
                    String[] nameAndAddressOfUnit = controller.checkApiExist(daikin);
                    if (nameAndAddressOfUnit != null) {
                        foundUnits.add(nameAndAddressOfUnit);
                    }
                } catch (SocketTimeoutException ex) {
                    LOG.debug("Scanned " + daikin.getHost() + ". Nothing found, reason (" + ex.getMessage() + ")");
                } catch (IOException e) {
                    LOG.trace("Scanned " + daikin.getHost() + ". Nothing found.");
                }
            });
        }
        lookUpService.shutdown();
        lookUpService.awaitTermination(2, TimeUnit.MINUTES);

        LOG.info("Scanning finished, found " + foundUnits.size() + " units.");
        foundUnits.forEach((String[] nameAndAddressOfUnit) ->
                LOG.info("[" + nameAndAddressOfUnit[0] + "] on " + nameAndAddressOfUnit[1])
        );
    }

    private static DaikinBase getDaikin(String subNet, final int ip, int port) {
        if (subNet == null || subNet.trim().length() == 0) subNet = "192.168.1.";
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

    @Override
    public void close() throws Exception {

    }
}

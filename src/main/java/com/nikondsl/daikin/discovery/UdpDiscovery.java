package com.nikondsl.daikin.discovery;

import com.nikondsl.daikin.DaikinController;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.*;
import java.util.HashSet;
import java.util.Set;

public class UdpDiscovery implements AutoCloseable {
    private static final Logger LOG = LogManager.getLogger(TcpDiscovery.class);

    private String probeData = "DAIKIN_UDP/common/basic_info";
    private String bindAddress = "192.168.0.109";
    private int bindPort = 30050;
    // Discovery message as defined by Daikin protocol
    private static final String DISCOVERY_MSG = "DAIKIN_UDP/1.0\n\n";

    // How long to wait for responses (in milliseconds)
    private static final int TIMEOUT_MS = 15000;

    // Broadcast address (use specific subnet like "192.168.1.255" if needed)
    private static final String BROADCAST_ADDRESS = "192.168.0.255";

    public static void main(String[] args) throws IOException {
        System.err.println(new UdpDiscovery().findDaikinUnits());
    }

    /**
     * Send UDP broadcast and collect responses from Daikin units
     */
    private Set<InetAddress> findDaikinUnits() throws IOException {
        LOG.info("Starting discovery of daikin devices ...");

        Set<InetAddress> devices = new HashSet<>();
        DatagramSocket socket = null;

        try {
            // Create UDP socket
            socket = new DatagramSocket();

            // Enable broadcast
            socket.setBroadcast(true);

            // Set receive timeout
            socket.setSoTimeout(TIMEOUT_MS);

            // Prepare discovery message
            byte[] sendData = DISCOVERY_MSG.getBytes();

            // Resolve broadcast address
            InetAddress broadcastAddress = InetAddress.getByName(BROADCAST_ADDRESS);

            // Create send packet
            DatagramPacket sendPacket = new DatagramPacket(
                    sendData,
                    sendData.length,
                    broadcastAddress,
                    bindPort
            );

            // Send discovery packet
            System.out.println("Sending discovery to " + BROADCAST_ADDRESS + ":" + bindPort);
            socket.send(sendPacket);

            // Buffer for receiving responses
            byte[] receiveBuffer = new byte[4096];

            // Listen for responses
            System.out.println("Listening for responses...\n");

            boolean foundAny = false;

            while (true) {
                try {
                    DatagramPacket receivePacket = new DatagramPacket(
                            receiveBuffer,
                            receiveBuffer.length
                    );

                    socket.receive(receivePacket);

                    // Get sender IP
                    InetAddress senderAddress = receivePacket.getAddress();
                    int senderPort = receivePacket.getPort();

                    // Get message data
                    String message = new String(
                            receivePacket.getData(),
                            0,
                            receivePacket.getLength()
                    );
                    devices.add(InetAddress.getByName(senderAddress.getHostAddress()));
                    System.out.println("========================================");
                    System.out.println("Found Daikin Unit!");
                    System.out.println("IP Address: " + senderAddress.getHostAddress());
                    System.out.println("Port: " + senderPort);
                    System.out.println("Raw Response:");
                    System.out.println(message);
                    System.out.println("========================================\n");

                    foundAny = true;

                } catch (java.net.SocketTimeoutException e) {
                    // Timeout - no more responses expected
                    break;
                }
            }

            if (!foundAny) {
                System.out.println("No Daikin units found on the network.");
                System.out.println("Make sure:");
                System.out.println("  - Units are powered on");
                System.out.println("  - UDP port 30050 is not blocked by firewall");
                System.out.println("  - You are on the same subnet (192.168.0.x) as the units");
            }

        } finally {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        }
        return devices;
    }
//    daikin:
//    api:
//    discover:
//    bind:
//    address: 192.168.1.255
//    port: 30050
//    probe-data: DAIKIN_UDP/
//    buffer-size: 8192

    public Set<InetAddress> discover() {
        LOG.info("Starting discovery of daikin devices ...");

        Set<InetAddress> devices = new HashSet<>();

        try (DatagramSocket sendSocket = new DatagramSocket(bindPort, InetAddress.getLocalHost())) {
            sendSocket.setBroadcast(true);
            sendSocket.setSoTimeout(5000);

            byte[] data = probeData.getBytes();
            DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getByName(bindAddress), bindPort);
            sendSocket.send(packet);
            LOG.info("Discovery package sent - {}:{}", packet.getAddress(), + packet.getPort());

            // Discovery response command
            byte[] response = new byte[8192];
            DatagramPacket responsePacket = new DatagramPacket(response, response.length);
            sendSocket.receive(responsePacket);
            LOG.info("Discovery response received - {}:{}", responsePacket.getAddress(), responsePacket.getPort());

            LOG.info("Got discover response {} ", new String(responsePacket.getData()).trim());
            devices.add(responsePacket.getAddress());
        } catch (Exception e) {
            LOG.error("Failed to discover devices", e);
        }

        return devices;
    }

    public void close() {

    }

    public void scanSubNet(String[] args, DaikinController controller) throws InterruptedException {

    }
}

package com.example.simpleconnect.dial;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class MSearch {


    private static final String MULTICAST_IP = "239.255.255.250";
    private static final int MULTICAST_PORT = 1900;

    private static final String SEARCH_TARGET_HEADER_VALUE = "urn:dial-multiscreen-org:service:dial:1";
    private static final String SEARCH_TARGET_HEADER = "ST";
    private static final String LOCATION_HEADER = "LOCATION";
    private static final String USN_HEADER = "USN";
    private static final String WAKEUP_HEADER = "WAKEUP";
    private static final String SERVER_HEADER = "SERVER";
    private static final String WOL_MAC = "MAC";
    private static final String WOL_TIMEOUT = "TIMEOUT";

    private final String msearchRequest;
    private final int socketTimeoutMs;

    public MSearch(int responseDelay, int socketTimeoutMs) {
        this.msearchRequest = "M-SEARCH * HTTP/1.1\r\n" +
                "HOST: " + MULTICAST_IP + ":" + MULTICAST_PORT + "\r\n" +
                "MAN: \"ssdp:discover\"\r\n" +
                "MX: " + responseDelay + "\r\n" +
                SEARCH_TARGET_HEADER + ": " + SEARCH_TARGET_HEADER_VALUE + "\r\n" +
                "USER-AGENT: OS/version product/version\r\n";
        this.socketTimeoutMs = socketTimeoutMs;
    }

    public List<Device> sendAndReceive() throws IOException {

        InetAddress inetAddress = InetAddress.getByName(MULTICAST_IP);

        byte[] requestBuffer = msearchRequest.getBytes(StandardCharsets.UTF_8);

        DatagramPacket requestPacket = new DatagramPacket(requestBuffer, requestBuffer.length, inetAddress, MULTICAST_PORT);

        MulticastSocket socket = new MulticastSocket(MULTICAST_PORT);

        socket.setReuseAddress(true);
        socket.setSoTimeout(socketTimeoutMs);
        socket.joinGroup(inetAddress);
        socket.send(requestPacket);
        Map<String, Device> discoveredDevicesByNames = new HashMap<>();

        try {
            while (true) {

                byte[] responseBuffer = new byte[1024];
                DatagramPacket responsePacket = new DatagramPacket(responseBuffer, responseBuffer.length);
                socket.receive(responsePacket);

                Device dialServer = toServer(responsePacket);

                if (dialServer != null) {
                    if (!discoveredDevicesByNames.containsKey(dialServer.uniqueServiceName)) {
                        discoveredDevicesByNames.put(dialServer.uniqueServiceName, dialServer);
                    }
                }
            }
        } catch (SocketTimeoutException e) {
        }

        return new ArrayList<>(discoveredDevicesByNames.values());
    }

    private Device toServer(DatagramPacket packet) {

        String data = new String(packet.getData(), StandardCharsets.UTF_8);
        if (!data.contains(SEARCH_TARGET_HEADER_VALUE)) {
            return null;
        }

        String[] dataRows = data.split("\n");
        Device dialServer = new Device();
        for (String row : dataRows) {
            String[] headerParts = row.split(": ");
            if (headerParts.length == 2) {
                String headerName = headerParts[0].toUpperCase();
                switch (headerName) {
                    case LOCATION_HEADER:
                        parseDeviceDescriptorUrl(dialServer, headerParts[1]);
                        break;
                    case USN_HEADER:
                        dialServer.uniqueServiceName = headerParts[1];
                        break;
                    case WAKEUP_HEADER:
                        parseWolHeader(dialServer, headerParts[1]);
                        break;
                    case SERVER_HEADER:
                        dialServer.serverDescription = headerParts[1];
                        break;
                    default:
                }
            }
        }

        if (dialServer.deviceDescriptorUrl != null
                && dialServer.uniqueServiceName != null && dialServer.uniqueServiceName.length() > 0) {
            return dialServer;
        } else {
            return null;
        }
    }

    private void parseDeviceDescriptorUrl(Device dialServer, String headerPart) {
        try {
            dialServer.deviceDescriptorUrl = new URL(headerPart);
        } catch (MalformedURLException e) {
        }
    }

    private void parseWolHeader(Device dialServer, String headerValue) {
        String[] wolParts = headerValue.split(";");
        for (String wolPart : wolParts) {
            String[] wolHeader = wolPart.split("=");
            if (wolHeader.length == 2) {
                switch (wolHeader[0].toUpperCase()) {
                    case WOL_MAC:
                        dialServer.wakeOnLanMAC = wolHeader[1];
                        dialServer.wakeOnLanSupport = true;
                        break;
                    case WOL_TIMEOUT:
                        dialServer.wakeOnLanTimeout = wolHeader[1];
                        break;
                    default:
                }
            }
        }
    }
}
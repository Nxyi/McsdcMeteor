package com.mcsdc.addon.util;

public class TicketIDGenerator {

    private static final int DEFAULT_PORT = 25565;


    // google code cause im lazy
    public static String generateTicketID(String ipAndPort) {
        if (ipAndPort == null || ipAndPort.isEmpty()) {
            throw new IllegalArgumentException("Input cannot be null or empty");
        }

        if (!TicketIDGenerator.isValidIPv4WithPort(ipAndPort)) return "";

        String ip;
        int port = DEFAULT_PORT;

        String[] parts = ipAndPort.split(":");
        if (parts.length == 2) {
            ip = parts[0];
            try {
                port = Integer.parseInt(parts[1]);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid port number: " + parts[1]);
            }
            if (port < 0 || port > 65535) {
                throw new IllegalArgumentException("Port out of range: " + port);
            }
        } else if (parts.length == 1) {
            ip = parts[0];
        } else {
            throw new IllegalArgumentException("Invalid format: must be IP or IP:port");
        }

        // Validate IP
        String[] ipParts = ip.split("\\.");
        if (ipParts.length != 4) {
            throw new IllegalArgumentException("Invalid IPv4 address: " + ip);
        }

        long ipValue = 0;
        for (String segment : ipParts) {
            int byteVal = Integer.parseInt(segment);
            if (byteVal < 0 || byteVal > 255) {
                throw new IllegalArgumentException("Invalid byte in IP: " + segment);
            }
            ipValue = (ipValue << 8) | byteVal;
        }

        // Combine IP and port: shift IP by 16 bits and add port
        long combined = (ipValue << 16) | (port & 0xFFFF);

        return Long.toString(combined, 36).toUpperCase();
    }

    public static String decodeTicketID(String ticketID) {
        long combined = Long.parseLong(ticketID, 36);

        int port = (int) (combined & 0xFFFF);
        long ipValue = combined >> 16;

        StringBuilder ip = new StringBuilder();
        for (int i = 3; i >= 0; i--) {
            long byteVal = (ipValue >> (8 * i)) & 0xFF;
            ip.append(byteVal);
            if (i > 0) {
                ip.append('.');
            }
        }

        return ip + ":" + port;
    }

    public static boolean isValidIPv4WithPort(String ipAndPort) {
        if (ipAndPort == null || ipAndPort.isEmpty()) {
            return false;
        }

        String[] parts = ipAndPort.split(":");
        if (parts.length != 2) {
            return false;
        }

        String ip = parts[0];
        String portStr = parts[1];

        // Validate port
        int port;
        try {
            port = Integer.parseInt(portStr);
        } catch (NumberFormatException e) {
            return false;
        }
        if (port < 0 || port > 65535) {
            return false;
        }

        // Validate IP
        String[] ipParts = ip.split("\\.");
        if (ipParts.length != 4) {
            return false;
        }

        for (String segment : ipParts) {
            try {
                int byteVal = Integer.parseInt(segment);
                if (byteVal < 0 || byteVal > 255) {
                    return false;
                }
            } catch (NumberFormatException e) {
                return false;
            }
        }

        return true;
    }
}


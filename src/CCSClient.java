import java.io.*;
import java.net.*;

public class CCSClient {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: java CCSClient <server-port> <operation-count>");
            return;
        }

        int port;
        int operationCount;
        try {
            port = Integer.parseInt(args[0]);
            operationCount = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.err.println("Invalid port or operation count.");
            return;
        }

        try {
            // 1. Service Discovery via UDP
            InetAddress broadcastAddr = InetAddress.getByName("255.255.255.255");
            DatagramSocket udpSocket = new DatagramSocket();
            udpSocket.setBroadcast(true);

            String discoveryMessage = "CCS DISCOVER";
            byte[] buffer = discoveryMessage.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, broadcastAddr, port);
            udpSocket.send(packet);

            // Wait for a response
            udpSocket.setSoTimeout(5000); // 5 seconds timeout
            byte[] responseBuffer = new byte[1024];
            DatagramPacket responsePacket = new DatagramPacket(responseBuffer, responseBuffer.length);
            udpSocket.receive(responsePacket);

            String response = new String(responsePacket.getData(), 0, responsePacket.getLength());
            if (!"CCS FOUND".equals(response)) {
                System.err.println("Invalid response from server.");
                udpSocket.close();
                return;
            }

            String serverAddress = responsePacket.getAddress().getHostAddress();
            System.out.println("Server found at: " + serverAddress);

            udpSocket.close();

            // 2. Communicate with the Server via TCP
            try (Socket tcpSocket = new Socket(serverAddress, port);
                 BufferedReader in = new BufferedReader(new InputStreamReader(tcpSocket.getInputStream()));
                 PrintWriter out = new PrintWriter(tcpSocket.getOutputStream(), true)) {

                for (int i = 0; i < operationCount; i++) {
                    // Randomly generate operations
                    String[] operations = {"ADD", "SUB", "MUL", "DIV"};
                    String operation = operations[(int) (Math.random() * operations.length)];
                    int arg1 = (int) (Math.random() * 100);
                    int arg2 = (int) (Math.random() * 100);
                    if ("DIV".equals(operation) && arg2 == 0) arg2 = 1; // Avoid division by zero

                    String request = operation + " " + arg1 + " " + arg2;
                    System.out.println("Sending: " + request);
                    out.println(request);

                    // Read the response
                    String result = in.readLine();
                    System.out.println("Received: " + result);
                }
            }

        } catch (SocketTimeoutException e) {
            System.err.println("Discovery timed out. No server response.");
        } catch (IOException e) {
            System.err.println("Client error: " + e.getMessage());
        }
    }
}

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class CCS {
    private static final int STATS_INTERVAL_MS = 10000; // 10 seconds
    private final int port;
    private final ExecutorService threadPool;
    private final Statistics stats;

    public CCS(int port) {
        this.port = port;
        this.threadPool = Executors.newCachedThreadPool();
        this.stats = new Statistics();
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java -jar CCS.jar <port>");
            return;
        }

        int port;
        try {
            port = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            System.err.println("Invalid port number.");
            return;
        }

        CCS server = new CCS(port);
        server.start();
    }

    public void start() {
        startUDPService();
        startTCPService();
        startStatisticsReporter();
    }

    private void startUDPService() {
        threadPool.submit(() -> {
            try (DatagramSocket socket = new DatagramSocket(port)) {
                System.out.println("UDP service started on port " + port);
                byte[] buffer = new byte[1024];
                while (true) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);
                    String message = new String(packet.getData(), 0, packet.getLength());

                    if (message.startsWith("CCS DISCOVER")) {
                        byte[] response = "CCS FOUND".getBytes();
                        socket.send(new DatagramPacket(response, response.length, packet.getAddress(), packet.getPort()));
                        System.out.println("Service discovery request handled.");
                    }
                }
            } catch (IOException e) {
                System.err.println("UDP service error: " + e.getMessage());
            }
        });
    }

    private void startTCPService() {
        threadPool.submit(() -> {
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                System.out.println("TCP service started on port " + port);
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    stats.incrementNewClients();
                    threadPool.submit(new ClientHandler(clientSocket, stats));
                }
            } catch (IOException e) {
                System.err.println("TCP service error: " + e.getMessage());
            }
        });
    }

    private void startStatisticsReporter() {
        threadPool.submit(() -> {
            while (true) {
                try {
                    Thread.sleep(STATS_INTERVAL_MS);
                    stats.printStatistics();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
    }

    static class ClientHandler implements Runnable {
        private final Socket socket;
        private final Statistics stats;

        public ClientHandler(Socket socket, Statistics stats) {
            this.socket = socket;
            this.stats = stats;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

                String line;
                while ((line = in.readLine()) != null) {
                    stats.incrementRequests();
                    System.out.println("Received: " + line);
                    String[] parts = line.split(" ");
                    if (parts.length != 3) {
                        out.println("ERROR");
                        stats.incrementInvalidOperations();
                        continue;
                    }

                    String oper = parts[0];
                    int arg1, arg2;
                    try {
                        arg1 = Integer.parseInt(parts[1]);
                        arg2 = Integer.parseInt(parts[2]);
                    } catch (NumberFormatException e) {
                        out.println("ERROR");
                        stats.incrementInvalidOperations();
                        continue;
                    }

                    int result;
                    try {
                        switch (oper) {
                            case "ADD":
                                result = arg1 + arg2;
                                stats.incrementOperation("ADD");
                                break;
                            case "SUB":
                                result = arg1 - arg2;
                                stats.incrementOperation("SUB");
                                break;
                            case "MUL":
                                result = arg1 * arg2;
                                stats.incrementOperation("MUL");
                                break;
                            case "DIV":
                                if (arg2 == 0) throw new ArithmeticException();
                                result = arg1 / arg2;
                                stats.incrementOperation("DIV");
                                break;
                            default:
                                out.println("ERROR");
                                stats.incrementInvalidOperations();
                                continue;
                        }
                    } catch (ArithmeticException e) {
                        out.println("ERROR");
                        stats.incrementInvalidOperations();
                        continue;
                    }

                    out.println(result);
                    stats.addToSum(result);
                    System.out.println("Result: " + result);
                }
            } catch (IOException e) {
                System.err.println("Client communication error: " + e.getMessage());
            }
        }
    }

    static class Statistics {
        private final AtomicInteger newClients = new AtomicInteger(0);
        private final AtomicInteger totalRequests = new AtomicInteger(0);
        private final Map<String, AtomicInteger> operations = new ConcurrentHashMap<>();
        private final AtomicInteger invalidOperations = new AtomicInteger(0);
        private final AtomicInteger sumOfResults = new AtomicInteger(0);

        public Statistics() {
            operations.put("ADD", new AtomicInteger(0));
            operations.put("SUB", new AtomicInteger(0));
            operations.put("MUL", new AtomicInteger(0));
            operations.put("DIV", new AtomicInteger(0));
        }

        public void incrementNewClients() {
            newClients.incrementAndGet();
        }

        public void incrementRequests() {
            totalRequests.incrementAndGet();
        }

        public void incrementOperation(String oper) {
            operations.getOrDefault(oper, new AtomicInteger(0)).incrementAndGet();
        }

        public void incrementInvalidOperations() {
            invalidOperations.incrementAndGet();
        }

        public void addToSum(int value) {
            sumOfResults.addAndGet(value);
        }

        public void printStatistics() {
            System.out.println("=== *** === *** === *** === *** === *** === *** === *** === *** === *** ===");
            System.out.println("Statistics (total):");
            System.out.println("New clients: " + newClients.get());
            System.out.println("Total requests: " + totalRequests.get());
            operations.forEach((key, value) -> System.out.println(key + " operations: " + value.get()));
            System.out.println("Invalid operations: " + invalidOperations.get());
            System.out.println("Sum of results: " + sumOfResults.get());
            System.out.println("=== *** === *** === *** === *** === *** === *** === *** === *** === *** ===");
        }
    }
}

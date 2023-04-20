package bg.sofia.uni.fmi.mjt.clientserver;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

// NIO, blocking
public class Client {

    private static final int SERVER_PORT = 7777;
    private static final String SERVER_HOST = "localhost";
    private static final int BUFFER_SIZE = 1024;

    private static ByteBuffer buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);

    private static String username;

    private static String sendRequestAndReceiveResponse(SocketChannel socketChannel, String userInput)
        throws IOException {
        buffer.clear();
        buffer.put(userInput.getBytes());
        buffer.flip();
        socketChannel.write(buffer);

        buffer.clear();
        socketChannel.read(buffer);
        buffer.flip();

        byte[] byteArray = new byte[buffer.remaining()];
        buffer.get(byteArray);
        String reply = new String(byteArray, "UTF-8");
        return reply;
    }

    public static void main(String[] args) {

        try (SocketChannel socketChannel = SocketChannel.open();
             Scanner scanner = new Scanner(System.in)) {

            socketChannel.connect(new InetSocketAddress(SERVER_HOST, SERVER_PORT));

            System.out.println("Connected to the server.Please login or register.");

            //this while loop makes sure user has been registered and logged
            // before the user functions from the handler can be used.
            while (true) {
                System.out.print("Enter register or login command: ");
                String userInput = scanner.nextLine();
                if (userInput.contains("register")) {
                    buffer.clear();
                    buffer.put(userInput.getBytes());
                    buffer.flip();
                    socketChannel.write(buffer);

                    buffer.clear();
                    socketChannel.read(buffer);
                    buffer.flip();

                    byte[] byteArray = new byte[buffer.remaining()];
                    buffer.get(byteArray);
                    String reply = new String(byteArray, "UTF-8");
                    System.out.println(reply);
                    if (reply.equals("User successfully registered. Please log into your account before proceeding.")) {
                        System.out.println("Proceed with login command.");
                    }
                } else if (userInput.contains("login")) {
                    buffer.clear();
                    buffer.put(userInput.getBytes());
                    buffer.flip();
                    socketChannel.write(buffer);

                    buffer.clear();
                    socketChannel.read(buffer);
                    buffer.flip();

                    byte[] byteArray = new byte[buffer.remaining()];
                    buffer.get(byteArray);
                    String reply = new String(byteArray, "UTF-8");
                    System.out.println(reply);
                    if (reply.equals("Login successful.")) {
                        username = userInput.split(" ")[1];
                        break;
                    }
                } else {
                    System.out.println(
                        "Please login into your account before proceeding with using the wallet manager.");
                }
            }

            while (true) {
                System.out.print("Enter command: ");
                String input = scanner.nextLine();

                if ("quit".equals(input)) {
                    break;
                }
                String command = username + " " + input;
                System.out.println("Sending command <" + command + "> to the server...");
                buffer.clear();
                buffer.put(command.getBytes());
                buffer.flip();
                socketChannel.write(buffer);

                buffer.clear();
                socketChannel.read(buffer);
                buffer.flip();

                byte[] byteArray = new byte[buffer.remaining()];
                buffer.get(byteArray);
                String reply = new String(byteArray, "UTF-8");
                System.out.println(reply);
            }

        } catch (IOException e) {
            throw new RuntimeException("There is a problem with the network communication", e);
        }
    }
}

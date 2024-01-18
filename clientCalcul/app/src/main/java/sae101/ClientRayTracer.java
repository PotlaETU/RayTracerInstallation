package sae101;

import sae101.parser.Parser;
import sae101.parser.scene.Scene;
import sae101.raytracer.RayTracer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class ClientRayTracer {

    private static final ByteBuffer bbSize = ByteBuffer.allocate(4);
    private static final int SERVER_PORT = 5000;

    public static void main(String[] args) {
        try {
            SocketChannel server = SocketChannel.open();
            InetSocketAddress socketAddr = new InetSocketAddress("localhost", SERVER_PORT);
            server.connect(socketAddr);

            while (true) {
                sendMessage(server, "REQUESTJOB");

                String response = receiveMessage(server);

                if (response.equals("NOSCENE")) {
                    System.out.println("Aucun travail disponible. Attente de 60 secondes...");
                    Thread.sleep(60000);
                } else if ("OK".equals(response)){
                    bbSize.clear();
                    server.read(bbSize);
                    bbSize.flip();
                    int dataSize = bbSize.getInt();
                    bbSize.clear();

                    ByteBuffer dataBuffer = ByteBuffer.allocate(dataSize);
                    server.read(dataBuffer);
                    byte[] sceneData = dataBuffer.array();
                    dataBuffer.clear();


                    Parser parser = new Parser(new String(sceneData, StandardCharsets.UTF_8));
                    Scene scene = parser.build();

                    RayTracer rayTracer = new RayTracer(scene);
                    byte[] imageBinaryData = rayTracer.view();

                    sendMessage(server, "SAVEIMAGE");

                    bbSize.putInt(imageBinaryData.length).flip();
                    server.write(bbSize);
                    bbSize.clear();

                    ByteBuffer buffer=ByteBuffer.allocate(imageBinaryData.length);
                    buffer.put(imageBinaryData)
                            .flip();
                    server.write(buffer);
                    buffer.clear();
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void sendMessage(SocketChannel channel, String msg) throws IOException {
        byte[] msgBytes = msg.getBytes(StandardCharsets.UTF_8);
        ByteBuffer bbSize = ByteBuffer.allocate(4).putInt(msgBytes.length).flip();
        channel.write(bbSize);
        bbSize.clear();
        channel.write(ByteBuffer.wrap(msgBytes));
    }

    private static String receiveMessage(SocketChannel channel) throws IOException {
        channel.read(bbSize);
        bbSize.flip();
        int dataSize = bbSize.getInt();
        bbSize.clear();

        ByteBuffer buffer = ByteBuffer.allocate(dataSize);
        channel.read(buffer);
        return new String(buffer.array(), StandardCharsets.UTF_8);
    }

}

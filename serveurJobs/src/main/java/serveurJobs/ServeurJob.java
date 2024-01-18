package serveurJobs;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ServeurJob {
    private static final ByteBuffer bbSize = ByteBuffer.allocate(4);
    private static final int PORT = 5000;
    private static final List<byte[]> scenesToGenerate = new ArrayList<>();

    public static void main(String[] args) {
        try {
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress(PORT));

            System.out.println("Server listening on port " + PORT);

            while (true) {
                SocketChannel clientChannel = serverSocketChannel.accept();
                new Thread(() -> handleClient(clientChannel)).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String receiveMessage(SocketChannel channel) throws IOException {
        channel.read(bbSize);
        bbSize.flip();
        int typeSize = bbSize.getInt();
        bbSize.clear();

        ByteBuffer buffer = ByteBuffer.allocate(typeSize);
        channel.read(buffer);
        return new String(buffer.array(), StandardCharsets.UTF_8);
    }

    private static void sendMessage(SocketChannel channel, String msg) throws IOException {
        byte[] msgBytes = msg.getBytes(StandardCharsets.UTF_8);

        ByteBuffer bbSize = ByteBuffer.allocate(4)
                .putInt(msgBytes.length)
                .flip();
        channel.write(bbSize);
        channel.write(ByteBuffer.wrap(msgBytes));
    }

    private static void handleClient(SocketChannel clientChannel) {
        try {
            String requestType = receiveMessage(clientChannel);
            switch (requestType) {
                case "ENQUEUEJOB" -> {
                    sendMessage(clientChannel,"ACK");

                    clientChannel.read(bbSize);
                    bbSize.flip();
                    int sceneDataLength = bbSize.getInt();
                    bbSize.clear();

                    ByteBuffer dataBuffer = ByteBuffer.allocate(sceneDataLength);
                    clientChannel.read(dataBuffer);

                    scenesToGenerate.add(dataBuffer.array());

                    System.out.println("ENQUEUEJOB: Scene ajoutée à la liste.");

                    sendMessage(clientChannel,"OK");
                }
                case "REQUESTJOB" -> {
                    if (!scenesToGenerate.isEmpty()) {
                        sendMessage(clientChannel, "OK");

                        byte[] nextSceneData = scenesToGenerate.remove(0);

                        bbSize.putInt(nextSceneData.length);
                        bbSize.flip();
                        clientChannel.write(bbSize);
                        bbSize.clear();

                        clientChannel.write(ByteBuffer.wrap(nextSceneData));

                        System.out.println("SENDJOB: Scène envoyé.");

                        String status = receiveMessage(clientChannel);

                        if("SAVEIMAGE".equals(status)){
                            clientChannel.read(bbSize);
                            bbSize.flip();
                            int imageDataLength = bbSize.getInt();
                            bbSize.clear();

                            ByteBuffer imageDataBuffer = ByteBuffer.allocate(imageDataLength);
                            clientChannel.read(imageDataBuffer);
                            imageDataBuffer.flip();

                            byte[] imageBytes = imageDataBuffer.array();
                            ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes);
                            BufferedImage bi = ImageIO.read(bais);
                            ImageIO.write(bi, "png", new File("test.png"));
                            bais.close();

                            System.out.println("SAVEIMAGE: Image reçue et enregistrée.");
                        }


                    } else {
                        sendMessage(clientChannel, "NOSCENE");

                        System.out.println("SENDJOB: Pas de scène à générer.");
                    }
                }

                default -> System.out.println("Requête inconnue: " + requestType);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

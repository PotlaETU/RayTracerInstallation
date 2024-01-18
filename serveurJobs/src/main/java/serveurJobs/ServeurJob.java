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

            System.out.println("Serveur de job à l'écoute sur le port " + PORT);

            while (true) {
                SocketChannel clientChannel = serverSocketChannel.accept();
                new Thread(() -> {
                    try {
                        handleClient(clientChannel);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }).start();
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

    private static void handleClient(SocketChannel clientChannel) throws IOException {
        try {
            String requestType = receiveMessage(clientChannel);
            switch (requestType) {
                case "ENQUEUEJOB" -> {
                    sendMessage(clientChannel,"ENQUEUEJOB-ACK");

                    clientChannel.read(bbSize);
                    bbSize.flip();
                    int sceneDataLength = bbSize.getInt();
                    bbSize.clear();

                    ByteBuffer dataBuffer = ByteBuffer.allocate(sceneDataLength);
                    clientChannel.read(dataBuffer);

                    scenesToGenerate.add(dataBuffer.array());

                    System.out.println("ENQUEUEJOB: Scene ajoutée à la liste.");

                    sendMessage(clientChannel,"ENQUEUEJOB-OK");
                }
                case "REQUESTJOB" -> {
                    sendMessage(clientChannel,"REQUESTJOB-ACK");
                    if (!scenesToGenerate.isEmpty()) {
                        sendMessage(clientChannel, "REQUESTJOB-OK");

                        byte[] nextSceneData = scenesToGenerate.remove(0);

                        bbSize.putInt(nextSceneData.length);
                        bbSize.flip();
                        clientChannel.write(bbSize);
                        bbSize.clear();

                        clientChannel.write(ByteBuffer.wrap(nextSceneData));

                        System.out.println("SENDJOB: Scène envoyé.");

                        String status = receiveMessage(clientChannel);

                        if("SAVEIMAGE".equals(status)){
                            sendMessage(clientChannel,"SAVEIMAGE-ACK");
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
                            sendMessage(clientChannel, "SAEVIMAGE-OK");

                        }else{
                            System.out.println("SAVEIMAGE: Image non reçue");
                        }

                    } else {
                        sendMessage(clientChannel, "REQUESTJOB-NOSCENE");
                        System.out.println("SENDJOB: Pas de scène à générer.");
                    }
                }

                default -> System.out.println("Requête inconnue: " + requestType);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            clientChannel.close();
        }
    }
}

package sae101;

import sae101.parser.Parser;
import sae101.parser.scene.Scene;
import sae101.raytracer.RayTracer;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class ClientRayTracer {

    private static final ByteBuffer bbSize = ByteBuffer.allocate(4);
    private static final int SERVER_PORT = 5000;

    public static void main(String[] args) {
        try {
            while (true) {
                SocketChannel server = SocketChannel.open();
                InetSocketAddress socketAddr = new InetSocketAddress("localhost", SERVER_PORT);
                try {
                    server.connect(socketAddr);
                }
                catch (ConnectException e){
                    System.err.println("Le serveur ne répond pas");
                    System.out.println("Attente de 60 secondes...");
                    Thread.sleep(60000);
                }

                sendMessage(server, "REQUESTJOB");
                if("REQUESTJOB-ACK".equals(receiveMessage(server))){
                    System.out.println("Demande de job bien reçu par le serveur");
                }

                String response = receiveMessage(server);

                if (response.equals("REQUESTJOB-NOSCENE")) {
                    System.out.println("Aucun travail disponible. Attente de 60 secondes...");
                    server.close();
                    Thread.sleep(60000);
                }
                else if ("REQUESTJOB-OK".equals(response)){
                    System.out.println("Image à générer.");
                    bbSize.clear();
                    server.read(bbSize);
                    bbSize.flip();
                    int dataSize = bbSize.getInt();
                    bbSize.clear();

                    ByteBuffer dataBuffer = ByteBuffer.allocate(dataSize);
                    server.read(dataBuffer);
                    byte[] sceneData = dataBuffer.array();
                    dataBuffer.clear();

                    System.out.println("Scène reçue.");

                    Parser parser = new Parser(new String(sceneData, StandardCharsets.UTF_8));
                    Scene scene = parser.build();

                    RayTracer rayTracer = new RayTracer(scene);
                    byte[] imageBinaryData = rayTracer.view();

                    sendMessage(server, "SAVEIMAGE");

                    if("SAVEIMAGE-ACK".equals(receiveMessage(server))){
                        System.out.println("Demande de sauvegarde d'image bien reçu");
                    }

                    bbSize.putInt(imageBinaryData.length).flip();
                    server.write(bbSize);
                    bbSize.clear();

                    ByteBuffer buffer=ByteBuffer.allocate(imageBinaryData.length);
                    buffer.put(imageBinaryData)
                            .flip();
                    server.write(buffer);
                    buffer.clear();

                    System.out.println("Image envoyé");

                    if("SAVEIMAGE-OK".equals(receiveMessage(server))){
                        System.out.println("Image bien enregistrée par le serveur.");
                    }
                    else{
                        System.out.println("Erreur dans la reception de l'image");
                    }
                    System.out.println("Fin du job.");
                    server.close();
                    Thread.sleep(5000);
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static synchronized void sendMessage(SocketChannel channel, String msg) throws IOException {
        byte[] msgBytes = msg.getBytes(StandardCharsets.UTF_8);
        ByteBuffer bbSize = ByteBuffer.allocate(4).putInt(msgBytes.length).flip();
        channel.write(bbSize);
        bbSize.clear();
        channel.write(ByteBuffer.wrap(msgBytes));
    }

    private static synchronized String receiveMessage(SocketChannel channel) throws IOException {
        channel.read(bbSize);
        bbSize.flip();
        int dataSize = bbSize.getInt();
        bbSize.clear();

        ByteBuffer buffer = ByteBuffer.allocate(dataSize);
        channel.read(buffer);
        return new String(buffer.array(), StandardCharsets.UTF_8);
    }

}

package soumissionJob;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SoumissionJob {

    private static final ByteBuffer bbSize = ByteBuffer.allocate(4);

    private static String receiveMessage(SocketChannel channel) throws IOException {
        channel.read(bbSize);
        bbSize.flip();
        int typeSize = bbSize.getInt();
        bbSize.clear();

        ByteBuffer buffer = ByteBuffer.allocate(typeSize);
        channel.read(buffer);
        return new String(buffer.array(), StandardCharsets.UTF_8);
    }

    public static void main(String[] args) throws IOException {

        String serverAddress = "localhost";
        int serverPort = 5000;
        String sceneFilePath = "scenetd42.txt";

        SocketChannel server = SocketChannel.open();
        SocketAddress socketAddr = new InetSocketAddress(serverAddress, serverPort);
        try {
            server.connect(socketAddr);
        }
        catch (ConnectException e){
            System.err.println("Le serveur ne répond pas.");
            System.err.println("La scène n'a pas était soumise.");
            System.exit(1);
        }

        Path path = Paths.get(sceneFilePath);
        byte[] sceneBytes = Files.readAllBytes(path);

        byte[] msgBytes = "ENQUEUEJOB".getBytes(StandardCharsets.UTF_8);

        bbSize.putInt(msgBytes.length)
                .flip();
        server.write(bbSize);
        bbSize.clear();
        server.write(ByteBuffer.wrap(msgBytes));

        System.out.println("Demande de job transmise.");

        if("ENQUEUEJOB-ACK".equals(receiveMessage(server))){
            System.out.println("Le serveur est prêt à recevoir la scène");
        }

        bbSize.putInt(sceneBytes.length)
                .flip();

        server.write(bbSize);
        bbSize.clear();

        ByteBuffer buffer = ByteBuffer.allocate(1024);

        int position = 0;
        while (position < sceneBytes.length) {
            int remaining = sceneBytes.length - position;
            int bytesToWrite = Math.min(remaining, buffer.capacity());

            buffer.put(sceneBytes, position, bytesToWrite);
            buffer.flip();

            server.write(buffer);

            buffer.clear();
            position += bytesToWrite;
        }

        if("ENQUEUEJOB-OK".equals(receiveMessage(server))){
            System.out.println("Le serveur a enregistré la scène");
        }
        else{
            System.out.println("Le serveur n'a pas enregistré la scène");
        }

        System.out.println("Fin du programme.");

        server.close();
    }
}

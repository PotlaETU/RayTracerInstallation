package soumissionJob;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SoumissionJob {

    private static final ByteBuffer bbSize = ByteBuffer.allocate(4);

    private static void sendMessage(SocketChannel channel, String msg) throws IOException {
        byte[] msgBytes = msg.getBytes(StandardCharsets.UTF_8);

        bbSize.putInt(msgBytes.length)
                .flip();
        channel.write(bbSize);
        bbSize.clear();
        channel.write(ByteBuffer.wrap(msgBytes));
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
            System.out.println("Le serveur ne répond pas");
            e.printStackTrace();
            System.exit(1);
        }

        Path path = Paths.get(sceneFilePath);
        byte[] sceneBytes = Files.readAllBytes(path);

        sendMessage(server,"ENQUEUEJOB");

        System.out.println("Demande de job transmise.");

        bbSize.putInt(sceneBytes.length)
                .flip();

        server.write(bbSize);
        bbSize.clear();

        ByteBuffer dataBuffer = ByteBuffer.wrap(sceneBytes);
        server.write(dataBuffer);

        if("ENQUEUEJOB-ACK".equals(receiveMessage(server))){
            System.out.println("Le serveur a reçu la demande");
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

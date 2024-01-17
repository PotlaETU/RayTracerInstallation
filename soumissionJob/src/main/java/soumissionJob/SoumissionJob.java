package soumissionJob;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SoumissionJob {

    public static void main(String[] args) {
        String serverAddress = "localhost";
        int serverPort = 1410;

        if (args.length != 1) {
            System.out.println("syntax: java SoumissionJob <fichier source>");
            return;
        }

        String sceneFilePath = args[0];

        try (SocketChannel socketChannel = SocketChannel.open()) {
            socketChannel.connect(new InetSocketAddress(serverAddress, serverPort));

            Path path = Paths.get(sceneFilePath);
            byte[] sceneBytes = Files.readAllBytes(path);

            ByteBuffer sizeBuffer = ByteBuffer.allocate(4);
            sizeBuffer.putInt(sceneBytes.length);
            sizeBuffer.flip();
            socketChannel.write(sizeBuffer);

            ByteBuffer dataBuffer = ByteBuffer.wrap(sceneBytes);
            socketChannel.write(dataBuffer);

            ByteBuffer imageSizeBuffer = ByteBuffer.allocate(4);
            socketChannel.read(imageSizeBuffer);
            imageSizeBuffer.flip();
            int imageSize = imageSizeBuffer.getInt();

            ByteBuffer imageBuffer = ByteBuffer.allocate(imageSize);
            socketChannel.read(imageBuffer);
            byte[] imageBytes = imageBuffer.array();
            ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes);
            BufferedImage bi = ImageIO.read(bais);
            ImageIO.write(bi, "png", new File("test.png"));
            bais.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

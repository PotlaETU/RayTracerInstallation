package sae101;

import sae101.parser.Parser;
import sae101.parser.scene.Scene;
import sae101.raytracer.RayTracer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ClientRayTracer {

    public static void main(String[] args) {
        int port = 1410;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server listening on port " + port);

            while (true) {
                try (Socket clientSocket = serverSocket.accept();
                     DataInputStream inputStream = new DataInputStream(clientSocket.getInputStream());
                     DataOutputStream outputStream = new DataOutputStream(clientSocket.getOutputStream())) {

                    int sceneLength = inputStream.readInt();
                    byte[] sceneData = new byte[sceneLength];
                    inputStream.readFully(sceneData);

                    Parser parser = new Parser(new String(sceneData, StandardCharsets.UTF_8));
                    Scene scene = parser.build();

                    RayTracer rayTracer = new RayTracer(scene);
                    byte[] imageBinaryData = rayTracer.view();

                    outputStream.writeInt(imageBinaryData.length);
                    outputStream.write(imageBinaryData);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

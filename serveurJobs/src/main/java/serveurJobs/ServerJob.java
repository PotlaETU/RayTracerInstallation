package main.java.serveurJobs;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ServerJob extends Thread implements Runnable{
    static int NB_THREAD_MAX =20;
    static ServerJob[] threads = new ServerJob[NB_THREAD_MAX];

    static int indexThread = 0;

    static Map<String, String> jobMap= new HashMap<>();
    private static final int port = 7007;


    Socket s;

    SocketChannel socketChannel;

    public ServerJob(){}
    public ServerJob(Socket s) {
        this.s = s;
    };

    public void run() {

        try {
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            int bytesRead = socketChannel.read(buffer);

            if (bytesRead > 0) {
                buffer.flip();
                byte[] bytes = new byte[buffer.remaining()];
                buffer.get(bytes);
                String message = new String(bytes, StandardCharsets.UTF_8);

                //Ici j'ai supposer que le client envoie un message de la forme "DemandeJob" ou "AjoutJob,job,description"
                //mais c'est chiant pour la descrpition de la scene (je sais pas comment faire)
                String[] mots = message.split(",");

                //Il faut changer DemandeJob ici par rapport a ce que le client envoie
                if ("DemandeJob".equals(mots[0])) {
                    StringBuffer reponse = new StringBuffer();
                    Set<String> keys = jobMap.keySet();
                    Iterator<String> keysIterator = keys.iterator();
                    if (keysIterator.hasNext()) {
                        String key = keysIterator.next();
                        reponse.append(key);
                        reponse.append(" ");
                        reponse.append(jobMap.get(key));
                        socketChannel.write(ByteBuffer.wrap(reponse.toString().getBytes()));
                    }else {
                        socketChannel.write(ByteBuffer.wrap("Pas de job disponible".getBytes()));
                    }
                //il faut changer AjoutJob ici par rapport a ce que le client qui donne un job envoie
                } else if ("AjoutJob".equals(mots[0])) {
                    jobMap.put(mots[1], mots[2]);
                }
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

        public static void main(String[] args) {
        for (int i = 0; i < NB_THREAD_MAX; i++) {
            threads[i] = new ServerJob();
        }

        try {
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress(port));
            while (true) {
                SocketChannel socketChannel = serverSocketChannel.accept();
                threads[indexThread].start();
                threads[indexThread].setSocketChannel(socketChannel);
                indexThread++;
                threads[indexThread-1].join();
                indexThread--;
                socketChannel.close();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void setSocketChannel(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }
}

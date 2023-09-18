package com.radicubs;

import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

public class SocketTest {

    public static void main(String[] args) throws IOException {
        ServerSocket server = new ServerSocket(7503);
        System.out.println("server started");

        Queue<Character> q = new LinkedList<>();

        Runnable r = () -> {
            Scanner s = new Scanner(System.in);
            while(true) {
                if(s.hasNext()) {
                    String line = s.nextLine();
                    synchronized (q) {
                        for(char c : line.toCharArray()) {
                            if(c == '\\') q.offer((char) 29);
                            else if(c == '|') q.offer((char) 30);
                            else q.offer(c);
                        }
                        q.offer('\n');
                    }
                }
            }
        };

        AtomicBoolean socketAlive = new AtomicBoolean(true);

        Thread in = new Thread(r);
        in.start();


        while(true) {
            Socket client = server.accept();
            PrintStream out = new PrintStream(client.getOutputStream());
            System.out.println("client connected, starting console read");
            socketAlive.set(true);
            new Thread(getChecker(socketAlive, client)).start();
            while(socketAlive.get()) {
                synchronized (q) {
                    if(!q.isEmpty()) {
                        while(!q.isEmpty()) out.print(q.poll());
                    }
                }

            }
            System.out.println("Client Disconnected");
        }

    }

    public static Runnable getChecker(AtomicBoolean bool, Socket s) {
        return () -> {
            while(bool.get()) {
                try {
                    bool.set(s.getInputStream().read() != -1);
                } catch (IOException e) {
                }
            }
        };
    }

}

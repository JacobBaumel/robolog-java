package com.radicubs;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class Robolog implements Closeable {

    private static Robolog instance;
    public static Robolog getInstance() {
        if(instance == null) instance = new Robolog();
        return instance;
    }

    private static void socketAcceptor(ServerSocket server, List<Socket> socketList, AtomicBoolean stop) {
        while(stop.get()) {
            Socket s = null;
            try {
                s = server.accept();
            } catch(InterruptedIOException e) {
            } catch(IOException e) {
                System.out.println("Error accepting socket! Kept going.");
            }
            if(s != null) socketList.add(s);
        }
    }

    private Robolog() {}

    private ServerSocket server;
    private List<Socket> openSockets;
    private Thread socketAcceptor;
    private AtomicBoolean socketAcceptorContinuer;
    private boolean hasStarted = false;
    private OutputStream stdout;
    private PrintStream oldstdout;
    private OutputStream stderr;
    private PrintStream oldstderr;

    public void start(int port) throws IOException {
        if(hasStarted) {
            throw new IllegalStateException("Already started Robolog on port " + server.getLocalPort() + "!");
        }

        oldstdout = System.out;
        oldstderr = System.err;
        System.setErr(new PrintStream(stderr));
        System.setOut(new PrintStream(stdout));

        hasStarted = true;
        server = new ServerSocket(port);
        server.setSoTimeout(1);
        openSockets = Collections.synchronizedList(new ArrayList<>());
        socketAcceptorContinuer = new AtomicBoolean(true);
        socketAcceptor = new Thread(() -> socketAcceptor(server, openSockets, socketAcceptorContinuer));
    }

    public void logRaw(String category, String message) {
        if(category.isEmpty()) category = "Default";

    }

    private void write(String message) throws IOException {
        Iterator<Socket> it = openSockets.iterator();
        while(it.hasNext()) {
            Socket s = it.next();
            if(s.getInputStream().read() == -1) {
                s.close();
                it.remove();
                continue;
            }

            for(char c : message.toCharArray()) {
                s.getOutputStream().write(c);
            }
        }
    }

    @Override
    public void close() throws IOException {
        System.setErr(oldstderr);
        System.setOut(oldstdout);
        stdout = null;
        stderr = null;
        oldstderr = null;
        oldstdout = null;

        socketAcceptorContinuer.set(false);
        try {
            socketAcceptor.join();
        } catch (InterruptedException e) {
            System.err.println("Failed to stop socket acceptor!");
        }

        for(Socket s : openSockets) s.close();
        server.close();

        server = null;
        openSockets = null;
        socketAcceptor = null;
        socketAcceptorContinuer = null;
    }
}

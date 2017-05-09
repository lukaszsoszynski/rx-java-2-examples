package com.impaqgroup.training.reactive.in02socket;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Test;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SocketTest {

    @Test
    public void singleThreadServer() throws IOException {
        ServerSocket serverSocket = new ServerSocket(12081);
        while (true) {
            try (Socket clientSocket = serverSocket.accept()) {//<-- wait and return client socket when client is connected
                blockingClientHandler(clientSocket);
            }
        }
    }

    @SneakyThrows
    private void blockingClientHandler(Socket clientSocket) {
        log.info("New client connected.");
        try {
            try (PrintWriter writer = new PrintWriter(clientSocket.getOutputStream())) {
                log.info("Sending data to network client.");
                writer.println("What is your name?");
                writer.flush();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
                    log.info("Waiting for data from network client.");
                    String name = reader.readLine();//<-- blocking waiting for data from client
                    writer.println(String.format("%s is very nice name.", name));
                    writer.flush();
                }
            }
        }finally {
            log.info("Client disconnected.");
            clientSocket.close();
        }
    }

    @Test
    public void newThreadPerClientServer() throws IOException {
        ServerSocket serverSocket = new ServerSocket(12081);
        while (true) {
            Socket clientSocket = serverSocket.accept();
            Thread thread = new Thread(() -> blockingClientHandler(clientSocket));
            thread.start();
        }
    }

    @Test
    public void threadPoolServer() throws IOException {
        ExecutorService threadPool = Executors.newFixedThreadPool(3);
        ServerSocket serverSocket = new ServerSocket(12081);
        while (true) {
            Socket clientSocket = serverSocket.accept();
            threadPool.submit(() -> blockingClientHandler(clientSocket));
        }
    }

    @Test
    public void nonBlockingSingleServer() throws IOException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress("localhost", 12081));
        serverSocketChannel.configureBlocking(false);
        Selector selector = Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        while (true) {
            log.info("Check selector");
            if(selector.select() == 0){
                continue;
            }
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            while (iterator.hasNext()){
                SelectionKey selectionKey = iterator.next();
                iterator.remove();
                log.info("Got new selection key");
                if(selectionKey.isAcceptable()){
                    log.info("is acceptable");
                    ServerSocketChannel serverChanel = (ServerSocketChannel) selectionKey.channel();
                    SocketChannel clientChanel = serverChanel.accept();
                    if(clientChanel != null) {
                        log.info("has client channel");
                        clientChanel.configureBlocking(false);
                        clientChanel.register(selector, SelectionKey.OP_READ);
                        clientChanel.write(Charset.defaultCharset().encode(CharBuffer.wrap("What is your name?\n")));
                    }
                }
                if(selectionKey.isReadable()){
                    log.info("Is readable");
                    SocketChannel channel = (SocketChannel) selectionKey.channel();
                    channel.configureBlocking(false);
                    ByteBuffer byteBuffer = ByteBuffer.allocate(512);
                    channel.read(byteBuffer);
                    byteBuffer.flip();
                    String name = new String(byteBuffer.array()).trim();
                    byteBuffer.clear();
                    log.info("String read {}", name);
                    channel.write(Charset.defaultCharset().encode(String.format("%s is very nice name\n", name)));
                    channel.close();
                }
            }
        }
    }
}

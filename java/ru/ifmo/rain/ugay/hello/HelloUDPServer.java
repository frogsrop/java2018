package ru.ifmo.rain.ugay.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.*;

public class HelloUDPServer implements HelloServer {

    private Integer port;
    private Integer threadsAmount;
    private ThreadPoolExecutor executor;
    private DatagramSocket sSocket;

    @Override
    public void start(int port1, int threads1) {
        BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();
        port = port1;
        threadsAmount = threads1;
        executor = new ThreadPoolExecutor(threadsAmount, 3000, 10000, TimeUnit.MILLISECONDS, queue);
        executor.prestartAllCoreThreads();
        try {
            sSocket = new DatagramSocket(port);
        } catch (SocketException ex) {
            System.err.println("Unable to open socket");
            return;
        }
        executor.execute(new Receiver());
    }

    @Override
    public void close() {
        executor.shutdown();
        sSocket.close();
    }
    class Receiver implements Runnable {
        @Override
        public void run() {
            byte[] buf;
            try {
                buf = new byte[sSocket.getReceiveBufferSize()];
            } catch (SocketException ex) {
                System.err.println("Unable to determine buffer size");
                if (!executor.isShutdown()) {
                    executor.execute(new Receiver());
                }
                return;
            }
            DatagramPacket receivePacket = new DatagramPacket(buf, buf.length);
            try {
                sSocket.receive(receivePacket);
                if (!executor.isShutdown()) {
                    executor.execute(new Receiver());
                }
                byte[] sendData;
                //System.out.println(new String(receivePacket.getData()).substring(0, receivePacket.getLength()));
                sendData = ("Hello, " + new String(receivePacket.getData()).substring(0, receivePacket.getLength())).getBytes();
                try {
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, receivePacket.getAddress(), receivePacket.getPort());
                    sSocket.send(sendPacket);
                } catch (UnknownHostException ex) {
                    System.err.println("Unknown host");
                } catch (IOException ex) {
                    System.err.println("Can not send package");
                }
            } catch (IOException ex) {
                if (!executor.isShutdown()) {
                    executor.execute(new Receiver());
                }
                System.err.println("Can not receive packet");
            }

        }
    }
/*
    public static void main(String[] args) {
        if (args.length == 2) {
            try {
                new HelloUDPServer().start(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
            } catch (NumberFormatException numEx) {
                System.err.println("Wrong input. Unable to parse integer.");
            }
        } else {
            System.err.println("Wrong amount of arguments. Enter <port> and <amount of threads>.");
        }
    }*/

}

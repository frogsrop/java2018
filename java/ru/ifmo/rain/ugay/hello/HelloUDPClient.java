package ru.ifmo.rain.ugay.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.*;
import java.net.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class HelloUDPClient implements HelloClient {
    String ip;
    Integer port;
    private String query;
    private Integer threadsAmount;
    private Integer amountOfQueries;
    private DatagramSocket[] sockets;
    private ThreadPoolExecutor executor;
    private ArrayBlockingQueue queue;
    @Override
    public void run(String host, int port1, String prefix, int threads, int requests) {
        ip = host;
        port = port1;
        query = prefix;
        threadsAmount = threads;
        amountOfQueries = requests;
        Thread[] senders = new Thread[threadsAmount];
        sockets = new DatagramSocket[threadsAmount];
        executor = new ThreadPoolExecutor(threadsAmount, 3000, 10000, TimeUnit.MILLISECONDS, queue);
        executor.prestartAllCoreThreads();
        try {
            for (int i = 0; i < threadsAmount; i++) {
                sockets[i] = new DatagramSocket();
            }
        } catch (SocketException ex) {
            System.err.println("Unable to create such socket");
        }
        for (int i = 0; i < threadsAmount; i++) {
            senders[i] = new Sender(query, i);
            senders[i].run();
        }

        try {
            for (int i = 0; i < threadsAmount; i++) {
                senders[i].join();
            }
        } catch (InterruptedException ex) {
            System.err.println("Thread is interrupted");
        }
    }

    class Sender extends Thread {
        private String sendDataS;
        private int number;

        Sender(String text, int number) {
            sendDataS = text + Integer.toString(number) + "_";
            this.number = number;
        }

        private boolean send(int currentNumber) {
            byte[] sendData;
            String sendingMessage = sendDataS + Integer.toString(currentNumber);
            sendData = sendingMessage.getBytes();
            try {
                InetAddress ipAddress = InetAddress.getByName(ip);
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ipAddress, port);
                sockets[number].send(sendPacket);
                try {
                    byte[] buf = new byte[sockets[number].getReceiveBufferSize()];
                    DatagramPacket receivedPacket = new DatagramPacket(buf, buf.length);
                    try {
                        sockets[number].setSoTimeout(100);
                        sockets[number].receive(receivedPacket);
                        String receivedMessage = new String(receivedPacket.getData());
                        if (!receivedMessage.contains(new String(sendData))) {
                            System.out.println("Failure");
                            return false;
                        }
                    } catch (SocketTimeoutException ex) {
                        return false;
                    } catch (IOException ex) {
                        System.err.println("Can not receive anything");
                        return false;
                    }
                } catch (SocketException ex) {
                    System.err.println("Can not determine buffer size");
                    return false;
                }
            } catch (UnknownHostException ex) {
                System.err.println("Unknown host");
                return false;
            } catch (IOException ex) {
                System.err.println("Unable to send package");
                return false;
            }
            return true;
        }

        @Override
        public void run() {
            for (int i = 0; i < amountOfQueries; i++) {
                while (!send(i)) ;
            }
        }
    }
/*
    public static void main(String[] args) {
        if (args.length == 5) {
            try {
                new HelloUDPClient().run(args[0], Integer.parseInt(args[1]), args[2], Integer.parseInt(args[3]), Integer.parseInt(args[4]));
            } catch (NumberFormatException numEx) {
                System.err.println("Wrong input. Unable to parse integer.");
            }
        } else {
            System.err.println("Wrong amount of arguments. Enter <port> and <amount of threads>.");
        }
    }*/
}

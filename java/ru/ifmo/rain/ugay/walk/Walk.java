package ru.ifmo.rain.ugay.walk;

import java.io.*;
import java.nio.file.Path;

public class Walk {

    private static int nextHash(int val, int b) {
        return (val * 0x01000193) ^ (b & 0xff);
    }

    private static void writeString(BufferedWriter writer, int value, String path) throws IOException {
        writer.write(String.format("%08x", value) + " " + path + "\n");
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Error");
        }
        byte[] bytes = new byte[1000];
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(args[0]), "UTF-8"))
        ) {
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(args[1]), "UTF-8"))) {
                String curStr;
                while ((curStr = reader.readLine()) != null) {
                    try (BufferedInputStream hashReader = new BufferedInputStream(new FileInputStream(curStr))) {
                        int h = 0x811c9dc5;
                        int bytesAmount;
                        while ((bytesAmount = hashReader.read(bytes, 0, 1000)) != -1) {
                            for (int i = 0; i < bytesAmount; i++) {
                                h = nextHash(h, bytes[i]);
                            }
                        }
                        writeString(writer, h, curStr);
                    } catch (IOException ex) {
                        System.out.println("Can not open file for hashing");
                        writeString(writer, 0, curStr);
                    }

                }
            } catch (IOException ex) {
                System.out.println("Can not open file for writing");
            }
        } catch (IOException ex) {
            System.out.println("Can not open file with paths");
        }
    }
}

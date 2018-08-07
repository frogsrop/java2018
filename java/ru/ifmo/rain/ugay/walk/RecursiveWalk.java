package ru.ifmo.rain.ugay.walk;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class RecursiveWalk {

    private static int nextHash(final int val, final int b) {
        return (val * 0x01000193) ^ (b & 0xff);
    }

    private static void writeString(final BufferedWriter writer, final int value, final String path) throws IOException {
        writer.write(String.format("%08x", value) + " " + path + System.lineSeparator());
    }

    public static void main(String[] args) {
        if (args == null) {
            System.err.println("No arguments");
            return;
        }
        if (args.length != 2) {
            System.err.println("Two arguments expected, " + args.length + " found. Try RecursiveWalk inputpath outputpath");
        } else {
            byte[] bytes = new byte[1000];
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(args[0]), "UTF-8"))) {
                try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(args[1]), "UTF-8"))) {
                    String curStr;
                    try {
                        while ((curStr = reader.readLine()) != null) {
                            try {
                                Path path = Paths.get(curStr);
                                Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                                    @Override
                                    public FileVisitResult visitFile(Path file, BasicFileAttributes bats) {
                                        try (BufferedInputStream hashReader = new BufferedInputStream(new FileInputStream(file.toString()))) {
                                            int h = 0x811c9dc5;
                                            int bytesAmount;
                                            while ((bytesAmount = hashReader.read(bytes)) != -1) {
                                                for (int i = 0; i < bytesAmount; i++) {
                                                    h = nextHash(h, bytes[i]);
                                                }
                                            }
                                            try {
                                                writeString(writer, h, file.toString());
                                            } catch (IOException ex) {
                                                System.err.println("Can not write hash");
                                            }
                                        } catch (IOException ex) {
                                            System.err.println("Can not read file " + file.toString() + " for hashing");
                                        }
                                        return FileVisitResult.CONTINUE;
                                    }

                                    @Override
                                    public FileVisitResult visitFileFailed(Path file, IOException e) throws IOException {
                                        writeString(writer, 0, file.toString());
                                        System.err.println("Can not open file " + file.toString() + " for hashing");
                                        return FileVisitResult.CONTINUE;
                                    }
                                });
                            } catch (InvalidPathException ex) {
                                writeString(writer, 0, curStr);
                                System.err.println("Invalid Path");
                            }
                        }
                    } catch (IOException ex) {
                        System.err.println("Can't read line with path");
                    }
                } catch (IOException ex) {
                    System.err.println("Can not open file " + args[1] + " for writing");
                } catch (InvalidPathException ex) {
                    System.err.println("Wrong path:" + args[1]);
                }
            } catch (IOException ex) {
                System.err.println("Wrong path or can not open file " + args[0] + " with paths");
            } catch (InvalidPathException ex) {
                System.err.println("Wrong path:" + args[0]);
            }
        }
    }
}

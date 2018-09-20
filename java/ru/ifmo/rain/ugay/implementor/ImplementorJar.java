package ru.ifmo.rain.ugay.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

/**
 * Implementation of {@link JarImpler}.
 * This class generates jar for interfaces and abstract methods of classes.
 *
 * @author Ugay Yanis
 * @see Implementor
 */

public class ImplementorJar extends Implementor implements JarImpler {

    public Manifest manifest = new Manifest();

    @Override
    public void implementJar(Class<?> aClass, Path path) throws ImplerException {
        Path tempDir;
        try {
            tempDir = Files.createTempDirectory(path.toAbsolutePath().getParent(), "jarImplerTemp");
        } catch (IOException e) {
            throw new ImplerException("Unable to create temporary directory");
        }
        implement(aClass, tempDir);
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new ImplerException("Can not get JavaCompiler");
        }

        String[] args = new String[]{"-cp",
                tempDir.toString() + File.pathSeparator + System.getProperty("java.class.path"),
                tempDir + "/" + aClass.getCanonicalName().replaceAll("\\.", "/") + "Impl.java"};
        if (compiler.run(null, null, null, args) != 0) {
            throw new ImplerException("Can not compile " + aClass.getSimpleName() + ".java");
        }
        Attributes attributes = manifest.getMainAttributes();
        attributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        attributes.put(Attributes.Name.IMPLEMENTATION_VENDOR, "Ugay Yanis");
        try (JarOutputStream jarStream = new JarOutputStream(Files.newOutputStream(path), manifest)) {
            try {
                jarStream.putNextEntry(new ZipEntry(aClass.getCanonicalName().replaceAll("\\.", "/") + "Impl.class"));
                Files.copy(Paths.get(tempDir.toAbsolutePath().toString() + "/" + aClass.getPackage().getName().replaceAll("\\.", "/") + "/" + aClass.getSimpleName() + "Impl.class"), jarStream);
            } catch (IOException e) {
                throw new ImplerException("Problem with writing to jar-file");
            }
        } catch (IOException e) {
            throw new ImplerException("Problem with creating or closing jar-file", e);
        } finally {
            try {
                Files.walk(tempDir)
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            } catch (IOException e) {
                System.err.println("Problem with delete temp files.");
            }
        }
    }

    private static void format()
    {
        System.err.println("Expected input:\n" +
                            "\t\t <ClassName> <FullPathToFolderForClass>\n" +
                            "\t\t <ClassName> <FullPathToJarFile.jar>");
    }
    public static void main(String[] args) {
        if (args == null) {
            System.err.println("Arguments expected but found null");
            format();
            return;
        }
        if (args.length != 2 && args.length != 3) {
            System.err.println("Expected 2 or 3 arguments");
            format();
            return;
        }
        for (String arg : args) {
            if (arg == null) {
                System.err.println("Expected non-null arguments.");
                format();
                return;
            }
        }

        ImplementorJar temp = new ImplementorJar();
        try {
            if (args.length == 2) {
                temp.implement(Class.forName(args[0]), Paths.get(args[1]));
            } else if (!args[0].equals("-jar")) {
                System.err.println("Wrong arguments. Expected: -jar <ClassName> <FullPathToJarFile.jar>");
            } else {
                Path path = Paths.get(args[2]);
                temp.implementJar(Class.forName(args[1]), path);
            }
        } catch (InvalidPathException e) {
            System.err.println("Bad second argument. Invalid path. " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.err.println("Bad first argument. Invalid class name. " + e.getMessage());
        } catch (ImplerException e) {
            System.err.println("An error occurred during implementation. " + e.getMessage());
        }
    }

}

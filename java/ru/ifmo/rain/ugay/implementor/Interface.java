package ru.ifmo.rain.ugay.implementor;

import com.sun.org.apache.xpath.internal.operations.Mod;
import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;

import javax.imageio.IIOImage;
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Objects;

public class Interface implements Impler {

    private static HashMap<String, String> objs = new HashMap<>();

    private static StringBuilder outAllDecConstructors(Constructor[] constructors) {
        StringBuilder ans = new StringBuilder();
        for (Constructor constructor : constructors) {
            if (!Modifier.isAbstract(constructor.getModifiers())) {
                continue;
            }
            if (Modifier.isPublic(constructor.getModifiers())) {
                ans.append("\t").append("public ");
            }
            ans.append(constructor.getClass().getSimpleName()).append("Impl(");
            Parameter[] types = constructor.getParameters();
            StringBuilder s = new StringBuilder();
            Integer a = 0;
            for (Parameter t : types) {
                s.append(t.getType().getCanonicalName());
                s.append(" a").append(a.toString()).append(", ");
                a++;
            }
            if (s.length() > 0) {
                s.delete(s.length() - 2, s.length());
            }
            ans.append(s).append(") {}\n");
        }
        return ans;
    }

    private static StringBuilder outAllDecMethods(Method[] methods) {
        StringBuilder ans = new StringBuilder();
        for (Method method : methods) {
            if (Modifier.isStatic(method.getModifiers()) || method.isDefault() || !Modifier.isAbstract(method.getModifiers())) {
                continue;
            }
            System.err.println(method.getName() + " " + method.getModifiers());
            if (Modifier.isPublic(method.getModifiers())) {
                ans.append("\t").append("public ");
            }
            if (Modifier.isPrivate(method.getModifiers())) {
                ans.append("\t").append("private ");
            }
            ans.append(method.getReturnType().getTypeName());
            ans.append(" ").append(method.getName()).append("(");
            Parameter[] types = method.getParameters();
            StringBuilder s = new StringBuilder();
            Integer a = 0;
            for (Parameter t : types) {
                s.append(t.getType().getCanonicalName());
                s.append(" a").append(a.toString()).append(", ");
                a++;
            }
            if (s.length() > 0) {
                s.delete(s.length() - 2, s.length());
            }
            ans.append(s).append(") {\n");
            if (method.getReturnType().isArray()) {
                ans.append("\t\treturn null;\n");
            } else {
                switch (method.getReturnType().getSimpleName()) {
                    case "int":
                        ans.append("\t\treturn 0;\n");
                        break;
                    case "boolean":
                        ans.append("\t\treturn false;\n");
                        break;
                    case "float":
                        ans.append("\t\treturn 0;\n");
                        break;
                    case "byte":
                        ans.append("\t\treturn 0;\n");
                        break;
                    case "short":
                        ans.append("\t\treturn 0;\n");
                        break;
                    case "long":
                        ans.append("\t\treturn 0;\n");
                        break;
                    case "double":
                        ans.append("\t\treturn 0;\n");
                        break;
                    case "char":
                        ans.append("\t\treturn 0;\n");
                        break;
                    case "void":
                        break;
                    default:
                        ans.append("\t\treturn null;\n");
                }
            }
            ans.append("\t}\n\n");
        }
        return ans;
    }

    @Override
    public void implement(Class<?> aClass, Path path) throws ImplerException {
        String curType = aClass.getCanonicalName();
        StringBuilder ans = new StringBuilder();
        Objects.requireNonNull(aClass);
        Objects.requireNonNull(path);

        if (aClass.isPrimitive() || aClass.isArray() || Modifier.isFinal(aClass.getModifiers()) || aClass.equals(Enum.class)) {
            throw new ImplerException("Incorrect type");
        }

        if (aClass.getPackage() != null) {
            ans.append("package ").append(aClass.getPackage().getName()).append(";\n");
        }
        ans.append("public ").append("class ")
                .append(aClass.getSimpleName())
                .append("Impl ");
        if (aClass.isInterface()) {
            ans.append("implements ");
        } else {
            ans.append("extends ");
        }
        ans.append(curType);
        ans.append(" {\n");
        Constructor[] constructors;
        constructors = aClass.getConstructors();
        Method[] meths;
        meths = aClass.getDeclaredMethods();
        StringBuilder constructorsStr = outAllDecConstructors(constructors);
        StringBuilder methodsStr = outAllDecMethods(meths);
        ans.append(constructorsStr);
        ans.append(methodsStr);
        ans.append("}");
        File file = new File(path.toString() + "/" + curType.replaceAll("\\.", "/") + "Impl.java");
        if (!file.getParentFile().exists()) {
            if (!file.getParentFile().mkdirs()) {
                System.err.println("ERROR");
            }
        }
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"))) {
            writer.write(ans.toString());
        } catch (IOException ex) {
            System.out.println(path.toString() + "/" + aClass.getCanonicalName().replaceAll("\\.", "/") + "Impl.java");
            System.out.println("Unable to write into file");
        }
    }

    public abstract static class any {
        public void dec() {
            System.out.println("vasya");
        }

        public abstract void undecpub();

        abstract void undecpacpriv();
    }

    public static void main(String[] args) {
        Interface q = new Interface();
        try {
            q.implement(any.class, Paths.get("D:"));
        } catch (ImplerException e) {
            e.printStackTrace();
        }
    }
}

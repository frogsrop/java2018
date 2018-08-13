package ru.ifmo.rain.ugay.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class Interface implements Impler {

    private static StringBuilder outAllDecConstructors(Class<?> aClass) {
        StringBuilder ans = new StringBuilder();
        if (!aClass.isInterface()) {
            for (Constructor constructor : aClass.getDeclaredConstructors()) {
                if (!Modifier.isPrivate(constructor.getModifiers())) {
                    if (Modifier.isPublic(constructor.getModifiers())) {
                        ans.append("\t").append("public ");
                    }
                    if (Modifier.isPrivate(constructor.getModifiers())) {
                        ans.append("\t").append("private ");
                    }
                    if (Modifier.isProtected(constructor.getModifiers())) {
                        ans.append("\t").append("protected ");
                    }
                    ans.append(aClass.getSimpleName())
                            .append("Impl(");
                    Parameter[] types = constructor.getParameters();
                    StringBuilder s = new StringBuilder();
                    StringBuilder names = new StringBuilder();
                    for (Parameter t : types) {
                        s.append(t.getType().getCanonicalName());
                        s.append(" ").append(t.getName()).append(", ");
                        names.append(t.getName()).append(", ");
                    }

                    if (s.length() > 0) {
                        s.delete(s.length() - 2, s.length());
                        names.delete(names.length() - 2, names.length());
                    }

                    ans.append(s).append(")");
                    boolean b = false;
                    if (constructor.getExceptionTypes().length > 0) {
                        ans.append(" throws ");
                    }
                    for (Class<?> exception : constructor.getExceptionTypes()) {
                        if (b) {
                            ans.append(", ");
                        }
                        ans.append(exception.getCanonicalName());
                        b = true;
                    }
                    ans.append("{\n").append("\t\tsuper(")
                            .append(names).append("); \n\t}\n");
                }
            }
        }
        return ans;
    }

    private static StringBuilder outAllDecMethods(Set<Method> methods) {
        StringBuilder ans = new StringBuilder();
        for (Method method : methods) {
            if (!Modifier.isAbstract(method.getModifiers())) {
                continue;
            }
            if (Modifier.isPublic(method.getModifiers())) {
                ans.append("\t").append("public ");
            }
            if (Modifier.isPrivate(method.getModifiers())) {
                ans.append("\t").append("private ");
            }
            if (Modifier.isProtected(method.getModifiers())) {
                ans.append("\t").append("protected ");
            }
            ans.append(method.getReturnType().getTypeName());
            ans.append(" ").append(method.getName()).append("(");
            Parameter[] types = method.getParameters();
            StringBuilder s = new StringBuilder();
            for (Parameter t : types) {
                s.append(t.getType().getCanonicalName());
                s.append(" ").append(t.getName()).append(", ");
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

    private void methodsFilter(Class<?> aClass, Set<Method> methods) {
        if (aClass == null) {
            return;
        }
        Method[] currentMethods = aClass.getDeclaredMethods();
        for (Method method : currentMethods) {
            if (!Modifier.isAbstract(method.getModifiers())) {
                methods.remove(method);
            } else if (Modifier.isAbstract(method.getModifiers())) {
                methods.add(method);
            }
        }
        methodsFilter(aClass.getSuperclass(), methods);
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

        StringBuilder constructorsStr = outAllDecConstructors(aClass);
        if (constructorsStr.toString().isEmpty() && !aClass.isInterface()) {
            throw new ImplerException();
        }
        ans.append(constructorsStr);

        Set<Method> meths = new TreeSet<>(Comparator.comparing(x ->
                (x.getName() + Arrays.toString(x.getParameterTypes()))));
        meths.addAll(Arrays.stream(aClass.getMethods()).
                filter((Method x) -> Modifier.isAbstract(x.getModifiers())).
                collect(Collectors.toList()));
        methodsFilter(aClass, meths);
        StringBuilder methodsStr = outAllDecMethods(meths);
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
            System.err.println(path.toString() + "/" + aClass.getCanonicalName().replaceAll("\\.", "/") + "Impl.java");
            System.err.println("Unable to write into file");
            throw new ImplerException();
        }
//        System.out.println(ans);
    }

    class Aclass {
        void aclass() {
            System.out.println("A");
        }
    }

    interface Ainterface {
        void ainterface();
    }

    class Aimplement implements Ainterface {
        @Override
        public void ainterface() {
            System.out.println("Aimp");
        }
    }

    class Bclass {
        void bclass() {
            System.out.println("B");
        }
    }

    interface Binterface {
        void bInterface();
    }

    interface Cinterface extends Ainterface, Binterface {
    }

    class Cclass extends Aimplement implements Ainterface, Binterface {

        @Override
        public void bInterface() {
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

package ru.ifmo.rain.ugay.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;

import java.io.*;
import java.lang.reflect.*;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
/**
 * Implementation of {@link Impler}.
 * This class generates code for interfaces and abstract methods of classes.
 * @author Ugay Yanis
 * @see ImplementorJar
 * */
public class Implementor implements Impler {

    /**
     * Creates new instance of object.
     */
    Implementor(){ }

    /**
     * Defines field of view of an executable.
     * @param executable for which {@link Executable} field of view will be defined
     * @return {@link StringBuilder} defined field of view
     */
    private static StringBuilder getPrefixString(Executable executable) {

        StringBuilder result = new StringBuilder();
        if (Modifier.isPublic(executable.getModifiers())) {
            result.append("\t").append("public ");
        }
        if (Modifier.isPrivate(executable.getModifiers())) {
            result.append("\t").append("private ");
        }
        if (Modifier.isProtected(executable.getModifiers())) {
            result.append("\t").append("protected ");
        }
        return result;
    }

    /**
     * Generates string of all generated constructors.
     * @param aClass for which {@link Class} string with constructors string will be generated
     * @return {@link java.lang.StringBuilder} string of constructor definitions*/
    private static StringBuilder getConstructorsString(Class<?> aClass) {
        StringBuilder result = new StringBuilder();
        for (Constructor constructor : aClass.getDeclaredConstructors()) {
            if (Modifier.isPrivate(constructor.getModifiers())) {
                continue;
            }

            //constructor initialising
            result.append(getPrefixString(constructor)).append(aClass.getSimpleName()).append("Impl(");

            //params
            result.append(Arrays.stream(constructor.getParameters())
                    .map((Parameter x) -> x.getType().getCanonicalName() + " " + x.getName())
                    .collect(Collectors.joining(", ")))
                    .append(")");

            //exceptions
            if (constructor.getExceptionTypes().length > 0) {
                result.append(" throws ");
            }
            result.append(Arrays.stream(constructor.getExceptionTypes())
                    .map(Class::getCanonicalName)
                    .collect(Collectors.joining(", ")))
                    .append("{\n");

            //body
            result.append("\t\tsuper(")
                    .append(Arrays.stream(constructor.getParameters())
                            .map(Parameter::getName)
                            .collect(Collectors.joining(", ")))
                    .append("); \n\t}\n");
        }
        return result;
    }
    /**
     * Defines return object of returnType.
     * @param returnType for which {@link Class} return object will be defined
     * @return {@link String} object to be returned*/
    private static String getReturnTypeString(Class<?> returnType) {
        String result = "";
        if (returnType.isArray()) {
            result = "\t\treturn null;\n";
        } else {
            switch (returnType.getSimpleName()) {
                case "boolean":
                    result = "\t\treturn false;\n";
                    break;
                case "int":
                case "float":
                case "byte":
                case "short":
                case "long":
                case "double":
                case "char":
                    result = "\t\treturn 0;\n";
                    break;
                case "void":
                    break;
                default:
                    result = "\t\treturn null;\n";
            }
        }
        return result;
    }

    /**
     * Return string of defined methods.
     * @param methods for which {@link Set} of methods string of definitions will be generated
     * @return {@link StringBuilder} object to be returned*/

    private static StringBuilder getMethodsString(Set<Method> methods) {
        StringBuilder result = new StringBuilder();
        for (Method method : methods) {
            if (!Modifier.isAbstract(method.getModifiers())) {
                continue;
            }
            //method initialising
            result.append(getPrefixString(method))
                    .append(method.getReturnType().getTypeName())
                    .append(" ")
                    .append(method.getName())
                    .append("(");

            //params
            result.append(Arrays.stream(method.getParameters())
                    .map((Parameter x) -> x.getType().getCanonicalName() + " " + x.getName())
                    .collect(Collectors.joining(", "))).append(") ");

            //exceptions
            if (method.getExceptionTypes().length > 0) {
                result.append("throws ");
            }
            result.append(Arrays.stream(method.getExceptionTypes()).map(Class::getCanonicalName)
                    .collect(Collectors.joining(", ")));

            result.append(" {\n");
            //return obj
            result.append(getReturnTypeString(method.getReturnType()));
            result.append("\t}\n\n");
        }
        return result;
    }
    /**
     * Filters non-abstract methods from class aClass.
     * @param aClass class for filtering
     * @param methods {@link Set} of methods that will be filtered
     * */

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
        String implementationClassName = aClass.getCanonicalName();
        StringBuilder result = new StringBuilder();

        if (aClass.isPrimitive() || aClass.isArray() || Modifier.isFinal(aClass.getModifiers()) || aClass.equals(Enum.class)) {
            throw new ImplerException("Unable to implement type");
        }

        if (aClass.getPackage() != null) {
            result.append("package ").append(aClass.getPackage().getName()).append(";\n\n");
        }
        result.append("public class ")
                .append(aClass.getSimpleName())
                .append("Impl ");

        if (aClass.isInterface()) {
            result.append("implements ");
        } else {
            result.append("extends ");
        }

        result.append(implementationClassName).append(" {\n");

        StringBuilder constructorsStr = getConstructorsString(aClass);

        if (constructorsStr.toString().isEmpty() && !aClass.isInterface()) {
            throw new ImplerException();
        }

        result.append(constructorsStr);

        Set<Method> meths = new TreeSet<>(Comparator.comparing(x ->
                (x.getName() + Arrays.toString(x.getParameterTypes()))));
        meths.addAll(Arrays.stream(aClass.getMethods()).
                filter((Method x) -> Modifier.isAbstract(x.getModifiers())).
                collect(Collectors.toList()));
        methodsFilter(aClass, meths);
        StringBuilder methodsStr = getMethodsString(meths);
        result.append(methodsStr).append("}");

        File file = new File(path.toString() + "/" + implementationClassName.replaceAll("\\.", "/") + "Impl.java");
        if (!file.getParentFile().exists()) {
//            System.err.println("No such folder. Attempting to create folder.");
            if (!file.getParentFile().mkdirs()) {
                throw new ImplerException("Unable to create such folder.");
            } else {
//                System.err.println("Successful attempt.");
            }
        }
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"))) {
            writer.write(result.toString());
        } catch (IOException ex) {
            System.err.println(path.toString() + "/" + aClass.getCanonicalName().replaceAll("\\.", "/") + "Impl.java");
            throw new ImplerException("Unable to write into file");
        }
    }
}

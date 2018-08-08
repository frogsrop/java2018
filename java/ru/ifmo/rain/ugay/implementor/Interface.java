package ru.ifmo.rain.ugay.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;
import ru.ifmo.rain.ugay.arrayset.ArraySet;

import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

import java.util.RandomAccess;


public class Interface implements Impler {

    private static HashMap<String, String> objs = new HashMap<>();


    private static StringBuilder outAllDecMethods(Method[] methods) {
        StringBuilder ans = new StringBuilder();
        for (Method method : methods) {
//            if (method.getModifiers() != Modifier.STATIC + Modifier.PUBLIC)
//                ans.append("\t@Override\n");
            ans.append("\t").append("public ");
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
                ans.append("\t\treturn new ").append(method.getReturnType().getTypeName()).delete(ans.length() - 2, ans.length()).append("[0];\n");
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
                        ans.append("\t\t\n");
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
    public void implement(Class<?> aClass, Path path) {

        if (aClass.isInterface()) {
//            String curType = aClass.getCanonicalName();
            String curType = aClass.getCanonicalName();
//            System.err.println(curType);
            StringBuilder ans = new StringBuilder("public class " + aClass.getSimpleName() + "Impl" + " implements ");
            ans.append(curType);
            ans.append(" {\n");
            Method[] meths;
            meths = aClass.getMethods();
            StringBuilder methodsStr = outAllDecMethods(meths);
            ans.append(methodsStr);

            ans.append("}");
            File file = new File(path.toString() + "/" + aClass.getCanonicalName().replaceAll("\\.", "/") + "Impl.java");
            file.getParentFile().mkdirs();
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"))) {
                writer.write(ans.toString());
            } catch (IOException ex) {
                System.out.println(path.toString() + "/" + aClass.getCanonicalName().replaceAll("\\.", "/") + "Impl.java");
                System.out.println("Unable to write into file");
            }
            //System.out.println(ans);
        }
    }
    public interface InterfaceWithoutMethods extends RandomAccess {
    }
    public class InterfaceWithoutMethodsImpl implements ru.ifmo.rain.ugay.implementor.Interface.InterfaceWithoutMethods {
    }

    public static void main(String[] args) {
        Class<?> c = InterfaceWithoutMethods.class;
        Interface q = new Interface();
        q.implement(c, Paths.get("D:"));
    }
}

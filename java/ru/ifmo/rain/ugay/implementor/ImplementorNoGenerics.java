//package ru.ifmo.rain.ugay.implementor;
//
//import info.kgeorgiy.java.advanced.implementor.Impler;
//import info.kgeorgiy.java.advanced.implementor.ImplerException;
//
//import java.io.*;
//import java.lang.reflect.Method;
//import java.lang.reflect.Modifier;
//import java.lang.reflect.Parameter;
//import java.nio.file.Path;
//import java.util.HashMap;
//
//
//public class ImplementorNoGenerics implements Impler {
//
//    private static HashMap<String, String> objs = new HashMap<>();
//
//
//    private static StringBuilder outAllDecMethods(Method[] methods, StringBuilder imps) {
//        StringBuilder ans = new StringBuilder();
//        for (Method m : methods) {
//            if(m.getModifiers() != Modifier.STATIC + Modifier.PUBLIC)
//                ans.append("\t@Override\n");
//            ans.append("\t").append("public ");
////            ans.append(m.getReturnType().getCanonicalName());
//            ans.append(m.getReturnType().getTypeName());
//            ans.append(" ").append(m.getName()).append("(");
//            Parameter[] types = m.getParameters();
//            StringBuilder s = new StringBuilder();
//            Integer a = 0;
//
//            for (Parameter t : types) {
//                s.append(t.getType().getCanonicalName());
//                s.append(" a").append(a.toString()).append(", ");
//                a++;
//            }
//            if (s.length() > 0) {
//                s.delete(s.length() - 2, s.length());
//            }
//            ans.append(s).append(") {\n");
//            if (m.getReturnType().isArray()) {
//                ans.append("\t\treturn new ").append(m.getReturnType().getTypeName()).delete(ans.length() - 2, ans.length()).append("[0];\n");
//            } else {
//                switch (m.getReturnType().getSimpleName()) {
//                    case "int":
//                        ans.append("\t\treturn 0;\n");
//                        break;
//                    case "boolean":
//                        ans.append("\t\treturn false;\n");
//                        break;
//                    case "float":
//                        ans.append("\t\treturn 0;\n");
//                        break;
//                    case "byte":
//                        ans.append("\t\treturn 0;\n");
//                        break;
//                    case "short":
//                        ans.append("\t\treturn 0;\n");
//                        break;
//                    case "long":
//                        ans.append("\t\treturn 0;\n");
//                        break;
//                    case "double":
//                        ans.append("\t\treturn 0;\n");
//                        break;
//                    case "char":
//                        ans.append("\t\treturn 0;\n");
//                        break;
//                    case "void":
//                        ans.append("\t\t\n");
//                        break;
//                    default:
//                        ans.append("\t\treturn null;\n");
//                }
//            }
//            ans.append("\t}\n\n");
//        }
//        return ans;
//    }
//
//    @Override
//    public void implement(Class<?> aClass, Path path) throws ImplerException {
//        Class<?> C = aClass;
//        if (C.isInterface()) {
//            StringBuilder imps = new StringBuilder();
//            String curType = C.getCanonicalName();
//            StringBuilder ans = new StringBuilder("public class " + C.getSimpleName() + "Impl" + " implements ");
//            ans.append(curType);
//            ans.append(" {\n");
//            Method[] meths;
//            meths = C.getMethods();
//            ans.append(outAllDecMethods(meths, imps));
//            ans.append("}");
//            ans = imps.append("\n").append(ans);
//
//            System.out.println(path.toString() + "/" + C.getCanonicalName().replaceAll("\\.", "/") + "Impl.java");
//            File file = new File(path.toString() + "/" + C.getCanonicalName().replaceAll("\\.", "/") + "Impl.java");
//            file.getParentFile().mkdirs();
//            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"))) {
//                writer.write(ans.toString());
//            } catch (IOException ex) {
//                System.out.println("Ups error");
//            }
//            //System.out.println(ans);
//        }
//    }
//}

package ru.ifmo.rain.ugay.implementor;

import jdk.nashorn.internal.runtime.regexp.joni.Regex;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

interface tempInterface extends List<Integer> {
    Integer get(String x);

    void write();
}

class tt implements Set {


    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean contains(Object o) {
        return false;
    }

    @Override
    public Iterator iterator() {
        return null;
    }

    @Override
    public Object[] toArray() {
        return new Object[0];
    }

    @Override
    public boolean add(Object o) {
        return false;
    }

    @Override
    public boolean remove(Object o) {
        return false;
    }

    @Override
    public boolean addAll(Collection c) {
        return false;
    }

    @Override
    public void clear() {

    }

    @Override
    public boolean removeAll(Collection c) {
        return false;
    }

    @Override
    public boolean retainAll(Collection c) {
        return false;
    }

    @Override
    public boolean containsAll(Collection c) {
        return false;
    }

    @Override
    public Object[] toArray(Object[] a) {
        return new Object[0];
    }
}


class tempInterfaceImpl implements ru.ifmo.rain.ugay.implementor.tempInterface {
    @Override
    public java.lang.Integer get(java.lang.String a0) {
        return null;
    }

    @Override
    public void write() {

    }

    @Override

    public int size() {
        return 0;
    }

    public char anyKind() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean contains(Object o) {
        return false;
    }

    @Override
    public Iterator<Integer> iterator() {
        return null;
    }

    @Override
    public Object[] toArray() {
        return new Object[0];
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return null;
    }

    @Override
    public boolean add(Integer integer) {
        return false;
    }

    @Override
    public boolean remove(Object o) {
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean addAll(Collection<? extends Integer> c) {
        return false;
    }

    @Override
    public boolean addAll(int index, Collection<? extends Integer> c) {
        return false;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return false;
    }

    @Override
    public void clear() {

    }

    @Override
    public Integer get(int index) {
        return null;
    }

    @Override
    public Integer set(int index, Integer element) {
        return null;
    }

    @Override
    public void add(int index, Integer element) {

    }

    @Override
    public Integer remove(int index) {
        return null;
    }

    @Override
    public int indexOf(Object o) {
        return 0;
    }

    @Override
    public int lastIndexOf(Object o) {
        return 0;
    }

    @Override
    public ListIterator<Integer> listIterator() {
        return null;
    }

    @Override
    public ListIterator<Integer> listIterator(int index) {
        return null;
    }

    @Override
    public List<Integer> subList(int fromIndex, int toIndex) {
        return null;
    }
}

public class Debug {

    private static HashMap<String, String> objs = new HashMap<>();

    private static void addType(StringBuilder imps, StringBuilder s, String curType) {
        String noArray = curType.replaceAll("\\[]", "");
        if (noArray.contains(".")) {
            if (!objs.containsKey(noArray)) {
                imps.append("import ").append(noArray).append(";\n");
                objs.put(noArray, noArray.replaceAll("[^.]+\\.", ""));
            }
        } else {
            objs.put(noArray, noArray);
        }
        s.append(objs.get(noArray)).append(curType.replaceAll("[^\\]\\[]", ""));
    }

    private static StringBuilder outAllDecMethods(Method[] methods, StringBuilder imps) {
        StringBuilder ans = new StringBuilder();
        for (Method m : methods) {
            if(m.getModifiers() == Modifier.ABSTRACT + Modifier.PUBLIC) {
                ans.append("\t@Override\n");
                ans.append("\t").append("public ");
                addType(imps, ans, m.getReturnType().getCanonicalName());
                ans.append(" ").append(m.getName()).append("(");
                Parameter[] types = m.getParameters();
                StringBuilder s = new StringBuilder();
                Integer a = 0;

                for (Parameter t : types) {
                    addType(imps, s, t.getType().getCanonicalName());
                    s.append(" a").append(a.toString()).append(", ");
                    a++;
                }
                if (s.length() > 0) {
                    s.delete(s.length() - 2, s.length());
                }
                ans.append(s).append(") {\n");
                if (m.getReturnType().isArray()) {
                    ans.append("\t\treturn new ").append(m.getReturnType().getSimpleName()).delete(ans.length() - 2, ans.length()).append("[0];\n");
                } else {
                    switch (m.getReturnType().getSimpleName()) {
                        case "int":
                            ans.append("\t\treturn 0;\n");
                            break;
                        case "boolean":
                            ans.append("\t\treturn false;\n");
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
        }
        return ans;
    }


    static private void getInterfacesMethods(Class C, TreeSet<Method> methods) {
        if (C.isInterface()) {
            Method[] decMethods = C.getDeclaredMethods();
            methods.addAll(Arrays.asList(decMethods));
        } else {
            Class[] inter = C.getInterfaces();
            for (Class cl : inter) {
                getInterfacesMethods(cl, methods);
            }
            getInterfacesMethods(C.getSuperclass(), methods);
        }
    }

    static private void filterMethods(Class C, TreeSet<Method> methods) {
        if (!C.isInterface()) {
            Method[] meths = C.getDeclaredMethods();
            for (Method m : meths) {
                methods.remove(m);
            }
        } else {
            filterMethods(C.getSuperclass(), methods);
        }
    }

    public static void main(String[] args) {
        Class<?> C = Set.class;
        //Class<?> C = tempInterface.class;
        if (C.isInterface()) {
            StringBuilder imps = new StringBuilder();
            String curType = C.getCanonicalName();
            StringBuilder ans = new StringBuilder("public class " + C.getSimpleName() + "Impl" + " implements ");
            addType(imps, ans, curType);
            ans.append(" {\n");
            Method[] meths;
            meths = C.getMethods();
            ans.append(outAllDecMethods(meths, imps));
            ans.append("}");
            ans = imps.append("\n").append(ans);
            System.out.println(ans);
        }
    }
}

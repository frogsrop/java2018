package ru.ifmo.rain.ugay.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import java.nio.file.Paths;

public class ImplementorTester {
    public static void main(String[] args) {
        JarImpler temp = new ImplementorJar();
        try {
            temp.implementJar(Test.class, Paths.get("D:/qq/Mt.jar"));
        } catch (ImplerException ignore) {
        }
    }
}

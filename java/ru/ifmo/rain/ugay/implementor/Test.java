package ru.ifmo.rain.ugay.implementor;

public interface Test {
    int hello();

    default void staticMethod() {
        System.out.println("staticMethod");
    }
}


package ru.ifmo.rain.ugay.implementor;

public interface Test {
    int hello();

    default void defaultMethod() {
        System.out.println("defaultMethod");
    }

}


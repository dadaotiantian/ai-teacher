package com.dadaotiantian.memorize.thread;

public interface Action extends Runnable {
    default int priority() {
        return 0;
    }

    default String comment() {
        return getClass().getSimpleName();
    }
}

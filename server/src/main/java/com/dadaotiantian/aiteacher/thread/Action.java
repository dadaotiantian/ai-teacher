package com.dadaotiantian.aiteacher.thread;

public interface Action extends Runnable {
    default int priority() {
        return 0;
    }

    default String comment() {
        return getClass().getSimpleName();
    }
}

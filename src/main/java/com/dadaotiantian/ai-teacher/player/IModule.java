package com.dadaotiantian.memorize.player;

public interface IModule {
    default void init(Player player) {
    }

    default void start() {
    }

    default void stop() {
    }

    String name();
}

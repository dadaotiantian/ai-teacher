package com.dadaotiantian.memorize.word;

import com.dadaotiantian.memorize.player.IModule;
import com.dadaotiantian.memorize.player.Player;

public class MemoryWordModule implements IModule {
    private Player player;

    @Override
    public void init(Player player) {
        this.player = player;
    }

    @Override
    public String name() {
        return "memory_word";
    }

    public Player getPlayer() {
        return player;
    }
}

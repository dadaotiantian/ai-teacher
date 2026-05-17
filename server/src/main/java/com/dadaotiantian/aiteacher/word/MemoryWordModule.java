package com.dadaotiantian.aiteacher.word;

import com.dadaotiantian.aiteacher.player.IModule;
import com.dadaotiantian.aiteacher.player.Player;

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

package com.dadaotiantian.memorize.agent.modules;

import com.dadaotiantian.memorize.agent.AgentObject;
import com.dadaotiantian.memorize.network.MessagePacket;
import com.dadaotiantian.memorize.word.WordManager;

import java.util.Map;

public class WordTestModule implements IAbility {
    @Override
    public void init(AgentObject agent) {
    }

    @Override
    public String name() {
        return "word_test";
    }

    @Override
    public Map<String, Object> handle(MessagePacket packet) throws Exception {
        return WordManager.test(packet);
    }
}

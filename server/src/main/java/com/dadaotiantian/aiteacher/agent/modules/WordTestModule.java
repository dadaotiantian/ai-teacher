package com.dadaotiantian.aiteacher.agent.modules;

import com.dadaotiantian.aiteacher.agent.AgentObject;
import com.dadaotiantian.aiteacher.network.MessagePacket;
import com.dadaotiantian.aiteacher.word.WordManager;

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

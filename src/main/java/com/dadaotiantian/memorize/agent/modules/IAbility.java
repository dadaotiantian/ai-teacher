package com.dadaotiantian.memorize.agent.modules;

import com.dadaotiantian.memorize.agent.AgentObject;
import com.dadaotiantian.memorize.network.MessagePacket;

import java.util.Map;

public interface IAbility {
    void init(AgentObject agent);

    String name();

    Map<String, Object> handle(MessagePacket packet) throws Exception;
}

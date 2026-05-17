package com.dadaotiantian.aiteacher.agent.modules;

import com.dadaotiantian.aiteacher.agent.AgentObject;
import com.dadaotiantian.aiteacher.network.MessagePacket;

import java.util.Map;

public interface IAbility {
    void init(AgentObject agent);

    String name();

    Map<String, Object> handle(MessagePacket packet) throws Exception;
}

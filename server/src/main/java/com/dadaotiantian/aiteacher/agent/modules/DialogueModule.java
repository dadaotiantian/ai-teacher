package com.dadaotiantian.aiteacher.agent.modules;

import com.dadaotiantian.aiteacher.agent.AgentObject;
import com.dadaotiantian.aiteacher.network.AIServerMsgHandler;
import com.dadaotiantian.aiteacher.network.MessagePacket;
import com.dadaotiantian.aiteacher.utils.JsonUtil;

import java.util.Map;

public class DialogueModule implements IAbility {
    private AgentObject agent;

    @Override
    public void init(AgentObject agent) {
        this.agent = agent;
    }

    @Override
    public String name() {
        return "dialogue";
    }

    @Override
    public Map<String, Object> handle(MessagePacket packet) {
        String text = JsonUtil.string(packet.getBody(), "message");
        Map<String, Object> result = AIServerMsgHandler.ok("ok");
        result.put("agent_id", agent.getAgentId());
        result.put("agent_name", agent.getAgentName());
        result.put("reply", "我会陪你背单词。你刚才说：" + (text == null ? "" : text));
        return result;
    }
}

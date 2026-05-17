package com.dadaotiantian.aiteacher.agent.modules;

import com.dadaotiantian.aiteacher.agent.AgentObject;
import com.dadaotiantian.aiteacher.network.AIServerMsgHandler;
import com.dadaotiantian.aiteacher.network.MessagePacket;

import java.util.Map;

public class StudyPlanModule implements IAbility {
    @Override
    public void init(AgentObject agent) {
    }

    @Override
    public String name() {
        return "study_plan";
    }

    @Override
    public Map<String, Object> handle(MessagePacket packet) {
        Map<String, Object> result = AIServerMsgHandler.ok("ok");
        result.put("plan", "今日建议学习20个新词，并复习到期单词。");
        return result;
    }
}

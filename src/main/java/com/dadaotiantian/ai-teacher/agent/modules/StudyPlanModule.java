package com.dadaotiantian.memorize.agent.modules;

import com.dadaotiantian.memorize.agent.AgentObject;
import com.dadaotiantian.memorize.network.AIServerMsgHandler;
import com.dadaotiantian.memorize.network.MessagePacket;

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

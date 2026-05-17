package com.dadaotiantian.aiteacher.agent;

import com.dadaotiantian.aiteacher.db.MaxIdMgr;
import com.dadaotiantian.aiteacher.network.AIServerMsgHandler;
import com.dadaotiantian.aiteacher.network.MessagePacket;
import com.dadaotiantian.aiteacher.network.ServerFunctionDef;
import com.dadaotiantian.aiteacher.player.Player;
import com.dadaotiantian.aiteacher.player.PlayerManager;
import com.dadaotiantian.aiteacher.thread.Action;
import com.dadaotiantian.aiteacher.utils.JsonUtil;
import io.netty.channel.Channel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class AgentManager {
    private static final Map<Long, AgentObject> AGENTS = new ConcurrentHashMap<>();

    private AgentManager() {
    }

    public static void init() {
    }

    public static void create(Channel channel, MessagePacket packet) throws Exception {
        long uid = packet.getUid();
        Player player = PlayerManager.getOrLoad(uid);
        if (player == null) {
            AIServerMsgHandler.send(channel, ServerFunctionDef.CREATE_AGENT_RSP, uid,
                    AIServerMsgHandler.error("PLAYER_REQUIRED", "请先选择角色"));
            return;
        }
        String name = JsonUtil.string(packet.getBody(), "agent_name");
        if (name == null || name.isBlank()) {
            name = "智能体";
        }
        String abilities = JsonUtil.string(packet.getBody(), "abilities");
        if (abilities == null || abilities.isBlank()) {
            abilities = "dialogue|word_test|study_plan";
        }
        String avatar = JsonUtil.string(packet.getBody(), "avatar");
        long agentId = MaxIdMgr.getMaxId("t_u_agent", "agent_id");
        long now = System.currentTimeMillis() / 1000;
        AgentDB.insert(agentId, uid, name, abilities, avatar, now);
        AgentObject agent = createAgentObject(new AgentDB.AgentRecord(agentId, uid, name, abilities, avatar, now), player);
        Map<String, Object> result = AIServerMsgHandler.ok("创建智能体成功");
        result.put("agent", toMap(agent, abilities, avatar, now));
        AIServerMsgHandler.send(channel, ServerFunctionDef.CREATE_AGENT_RSP, uid, result);
    }

    public static Map<String, Object> list(MessagePacket packet) throws Exception {
        List<Map<String, Object>> agents = AgentDB.list(packet.getUid()).stream()
                .map(record -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("agent_id", record.agentId());
                    map.put("uid", record.uid());
                    map.put("agent_name", record.agentName());
                    map.put("abilities", record.abilities());
                    map.put("avatar", record.avatar());
                    map.put("created_time", record.createdTime());
                    return map;
                }).toList();
        Map<String, Object> result = AIServerMsgHandler.ok("ok");
        result.put("agents", agents);
        return result;
    }

    public static void chat(Channel channel, MessagePacket packet) throws Exception {
        long agentId = JsonUtil.longValue(packet.getBody(), "agent_id", 0);
        AgentObject agent = loadAgentObject(agentId);
        if (agent == null || agent.getUid() != packet.getUid()) {
            AIServerMsgHandler.send(channel, ServerFunctionDef.CHAT_MESSAGE_RSP, packet.getUid(),
                    AIServerMsgHandler.error("AGENT_NOT_FOUND", "智能体不存在"));
            return;
        }
        agent.enqueue(new AgentModuleAction(channel, agent, packet));
    }

    private static AgentObject loadAgentObject(long agentId) throws Exception {
        AgentObject cached = AGENTS.get(agentId);
        if (cached != null) {
            return cached;
        }
        AgentDB.AgentRecord record = AgentDB.find(agentId);
        if (record == null) {
            return null;
        }
        Player player = PlayerManager.getOrLoad(record.uid());
        if (player == null) {
            return null;
        }
        return createAgentObject(record, player);
    }

    private static AgentObject createAgentObject(AgentDB.AgentRecord record, Player player) {
        return AGENTS.computeIfAbsent(record.agentId(), id -> {
            AgentObject agent = new AgentObject();
            agent.setAgentId(record.agentId());
            agent.setAgentName(record.agentName());
            agent.setUid(record.uid());
            agent.bind(player);
            agent.setupAbilities(record.abilities());
            return agent;
        });
    }

    private static Map<String, Object> toMap(AgentObject agent, String abilities, String avatar, long createdTime) {
        Map<String, Object> map = new HashMap<>();
        map.put("agent_id", agent.getAgentId());
        map.put("uid", agent.getUid());
        map.put("agent_name", agent.getAgentName());
        map.put("abilities", abilities);
        map.put("avatar", avatar);
        map.put("created_time", createdTime);
        return map;
    }

    private static class AgentModuleAction implements Action {
        private final Channel channel;
        private final AgentObject agent;
        private final MessagePacket packet;

        private AgentModuleAction(Channel channel, AgentObject agent, MessagePacket packet) {
            this.channel = channel;
            this.agent = agent;
            this.packet = packet;
        }

        @Override
        public void run() {
            try {
                String abilityName = JsonUtil.string(packet.getBody(), "ability");
                if (abilityName == null || abilityName.isBlank()) {
                    abilityName = "dialogue";
                }
                var ability = agent.getAbility(abilityName);
                if (ability == null) {
                    ability = agent.getAbility("dialogue");
                }
                Map<String, Object> result = ability.handle(packet);
                AIServerMsgHandler.send(channel, ServerFunctionDef.CHAT_MESSAGE_RSP, packet.getUid(), result);
            } catch (Exception ex) {
                AIServerMsgHandler.send(channel, ServerFunctionDef.CHAT_MESSAGE_RSP, packet.getUid(),
                        AIServerMsgHandler.error("CHAT_ERROR", ex.getMessage()));
            }
        }
    }
}

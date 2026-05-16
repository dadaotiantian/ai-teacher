package com.dadaotiantian.memorize.agent;

import com.dadaotiantian.memorize.agent.modules.DialogueModule;
import com.dadaotiantian.memorize.agent.modules.IAbility;
import com.dadaotiantian.memorize.agent.modules.StudyPlanModule;
import com.dadaotiantian.memorize.agent.modules.WordTestModule;
import com.dadaotiantian.memorize.player.Player;
import com.dadaotiantian.memorize.thread.AbstractActionQueue;
import com.dadaotiantian.memorize.thread.ThreadManager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class AgentObject extends AbstractActionQueue {
    private long agentId;
    private long uid;
    private String agentName;
    private Player owner;
    private final Map<String, IAbility> abilityMap = new HashMap<>();

    public AgentObject() {
        super(ThreadManager.getPlayerExecutor());
    }

    public void bind(Player player) {
        this.owner = player;
        this.uid = player.getUid();
    }

    public void setupAbilities(String abilities) {
        addAbility(new DialogueModule());
        if (abilities != null && abilities.contains("word_test")) {
            addAbility(new WordTestModule());
        }
        if (abilities != null && abilities.contains("study_plan")) {
            addAbility(new StudyPlanModule());
        }
    }

    public void addAbility(IAbility ability) {
        ability.init(this);
        abilityMap.put(ability.name(), ability);
    }

    public IAbility getAbility(String name) {
        return abilityMap.get(name);
    }

    public Collection<IAbility> getAbilities() {
        return abilityMap.values();
    }

    public long getAgentId() { return agentId; }
    public void setAgentId(long agentId) { this.agentId = agentId; }
    public long getUid() { return uid; }
    public void setUid(long uid) { this.uid = uid; }
    public String getAgentName() { return agentName; }
    public void setAgentName(String agentName) { this.agentName = agentName; }
    public Player getOwner() { return owner; }
}

package com.dadaotiantian.aiteacher.player;

import com.dadaotiantian.aiteacher.thread.AbstractActionQueue;
import com.dadaotiantian.aiteacher.thread.ThreadManager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class BasePlayer extends AbstractActionQueue {
    protected final Map<String, IModule> moduleMap = new HashMap<>();

    protected BasePlayer() {
        super(ThreadManager.getPlayerExecutor());
    }

    protected void addModule(IModule module, Player player) {
        module.init(player);
        moduleMap.put(module.getClass().getName(), module);
    }

    @SuppressWarnings("unchecked")
    public <T extends IModule> T getModule(Class<T> moduleClass) {
        return (T) moduleMap.get(moduleClass.getName());
    }

    public Collection<IModule> getAllModules() {
        return moduleMap.values();
    }

    public void startModules() {
        moduleMap.values().forEach(IModule::start);
    }

    public void stopModules() {
        moduleMap.values().forEach(IModule::stop);
    }
}

package me.mrepiko.discordbotbase.discord.modules;

import com.google.gson.JsonObject;
import lombok.Getter;
import me.mrepiko.discordbotbase.discord.modules.executor.ShowcaseListenerModule;
import me.mrepiko.discordbotbase.discord.modules.executor.ShowcaseTaskModule;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Getter
public class ModuleManager {

    private final List<Module> modules = new ArrayList<>();

    public ModuleManager() {
        registerModules();
    }

    private void registerModules() {
        addModule(new ShowcaseTaskModule());
        addModule(new ShowcaseListenerModule());
    }

    public void setupModules(JsonObject modulesConfig) {
        List<Module> modulesToBeEnabled = new ArrayList<>();
        for (Module m: modules) {
            if (!(modulesConfig.has(m.getName()) && modulesConfig.get(m.getName()).getAsBoolean())) continue;
            modulesToBeEnabled.add(m);
            try {
                m.setupConfig();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            try {
                m.enable();
                System.out.println("[Module] " + m.getName() + " has been enabled.");
            } catch (Exception exception) {
                System.out.println("[Module] Issue while trying to enable " + m.getName() + ":");
                exception.printStackTrace();
            }
        }
        modules.clear();
        modules.addAll(modulesToBeEnabled);
    }

    private void addModule(Module module) {
        if (contains(module)) return;
        modules.add(module);
    }

    private boolean contains(Module module) {
        for (Module m: modules) if (m.getName().toLowerCase(Locale.ROOT).equalsIgnoreCase(module.getName().toLowerCase(Locale.ROOT))) return true;
        return false;
    }

}

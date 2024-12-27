package me.mrepiko.discordbotbase.modules;

import com.google.gson.JsonObject;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Getter
public class ModuleManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModuleManager.class);
    private final List<Module> modules = new ArrayList<>();

    public ModuleManager() {
        registerModules();
    }

    private void registerModules() {

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
                LOGGER.info("[Module] {} has been enabled.", m.getName());
            } catch (Exception exception) {
                LOGGER.error("[Module] Issue while trying to enable {}", m.getName(), exception);
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

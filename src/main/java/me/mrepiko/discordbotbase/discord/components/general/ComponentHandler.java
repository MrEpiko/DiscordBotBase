package me.mrepiko.discordbotbase.discord.components.general;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import me.mrepiko.discordbotbase.common.config.Config;
import me.mrepiko.discordbotbase.discord.mics.Constants;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Getter
public class ComponentHandler  {

    private final String name;
    protected Config componentConfig;
    protected double cooldown;

    protected boolean enabled;
    protected boolean admin;
    protected boolean defer;
    protected boolean ephemeralDefer;

    protected final List<String> requiredRoles = new ArrayList<>();
    protected final List<String> requiredUsers = new ArrayList<>();
    protected final List<String> requiredChannels = new ArrayList<>();

    private final HashMap<String, Long> cooldowns = new HashMap<>();

    public ComponentHandler(String name) {
        this.name = name;
        File folder = new File(Constants.COMPONENT_CONFIGURATION_FOLDER_PATH);
        if (!folder.exists()) return;
        File file = new File(folder.getPath() + "/" + name + ".json");
        if (!file.exists()) return;
        if (file.length() == 0) {
            try {
                FileWriter writer = new FileWriter(file);
                writer.write(Constants.COMPONENT_STRUCTURE);
                writer.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return;
        }
        try {
            this.componentConfig = new Config(file.getPath());
            setupProperties();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void setupProperties() {
        JsonObject properties = getProperties();
        if (properties == null) return;
        enabled = !properties.has("enabled") || properties.get("enabled").getAsBoolean();
        admin = properties.has("admin") && properties.get("admin").getAsBoolean();
        defer = properties.has("ephemeral_defer");
        ephemeralDefer = defer && properties.get("ephemeral_defer").getAsBoolean();
        cooldown = (properties.has("cooldown")) ? properties.get("cooldown").getAsDouble() : 0;
        if (properties.has("required_roles")) for (JsonElement e: properties.get("required_roles").getAsJsonArray()) requiredRoles.add(e.getAsString());
        if (properties.has("required_users")) for (JsonElement e: properties.get("required_users").getAsJsonArray()) requiredUsers.add(e.getAsString());
        if (properties.has("required_channels")) for (JsonElement e: properties.get("required_channels").getAsJsonArray()) requiredChannels.add(e.getAsString());
    }

    @Nullable
    protected JsonObject getProperties() {
        return (componentConfig == null) ? null : componentConfig.get("properties").getAsJsonObject();
    }

}

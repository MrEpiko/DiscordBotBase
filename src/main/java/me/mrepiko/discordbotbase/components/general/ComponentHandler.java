package me.mrepiko.discordbotbase.components.general;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Delegate;
import me.mrepiko.discordbotbase.DiscordBot;
import me.mrepiko.discordbotbase.components.ComponentType;
import me.mrepiko.discordbotbase.config.Config;
import me.mrepiko.discordbotbase.mics.Constants;
import net.dv8tion.jda.api.Permission;
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
    @Delegate protected Config componentConfig;
    protected double cooldown;

    @Setter protected boolean enabled = true;
    protected boolean admin;
    protected boolean talk;
    protected boolean defer;
    protected boolean ephemeralDefer;
    private boolean invokerOnly;

    protected final List<String> requiredRoles = new ArrayList<>();
    protected final List<String> requiredUsers = new ArrayList<>();
    protected final List<String> requiredChannels = new ArrayList<>();
    protected final List<Permission> requiredPermissions = new ArrayList<>();
    protected final List<Permission> requiredChannelPermissions = new ArrayList<>();

    private final HashMap<String, JsonObject> errorHandlers = new HashMap<>();
    private final HashMap<String, Long> cooldowns = new HashMap<>();

    protected ComponentType componentType;

    private final JsonObject defaultResponse = DiscordBot.getInstance().getDefaultResponse();

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
        this.componentConfig = new Config(file.getPath());
        setupProperties();
        setupDefaultConfigurations();
    }

    protected void setupProperties() {
        JsonObject properties = getProperties();
        if (properties == null) return;
        enabled = !properties.has("enabled") || properties.get("enabled").getAsBoolean();
        admin = properties.has("admin") && properties.get("admin").getAsBoolean();
        talk = properties.has("talk") && properties.get("talk").getAsBoolean();
        defer = properties.has("ephemeral_defer");
        ephemeralDefer = defer && properties.get("ephemeral_defer").getAsBoolean();
        cooldown = (properties.has("cooldown")) ? properties.get("cooldown").getAsDouble() : 0;
        invokerOnly = properties.has("invoker_only") && properties.get("invoker_only").getAsBoolean();
        if (properties.has("required_roles")) for (JsonElement e: properties.get("required_roles").getAsJsonArray()) requiredRoles.add(e.getAsString());
        if (properties.has("required_users")) for (JsonElement e: properties.get("required_users").getAsJsonArray()) requiredUsers.add(e.getAsString());
        if (properties.has("required_channels")) for (JsonElement e: properties.get("required_channels").getAsJsonArray()) requiredChannels.add(e.getAsString());
        if (properties.has("required_permissions")) for (JsonElement e: properties.get("required_permissions").getAsJsonArray()) requiredPermissions.add(Permission.valueOf(e.getAsString()));
        if (properties.has("required_channel_permissions")) for (JsonElement e: properties.get("required_channel_permissions").getAsJsonArray()) requiredChannelPermissions.add(Permission.valueOf(e.getAsString()));
        JsonObject errorHandlersObject = DiscordBot.getInstance().getConfig().get("error_handlers").getAsJsonObject().get("components").getAsJsonObject();
        JsonObject localErrorHandlersObject = (properties.has("error_handlers")) ? properties.get("error_handlers").getAsJsonObject() : new JsonObject();
        for (String e: errorHandlersObject.keySet()) {
            if (localErrorHandlersObject.has(e) && !localErrorHandlersObject.get(e).getAsJsonObject().isEmpty()) errorHandlers.put(e, localErrorHandlersObject.get(e).getAsJsonObject());
            else errorHandlers.put(e, errorHandlersObject.get(e).getAsJsonObject());
        }
    }

    public void expectResponseObject(String key) {
        expect(key, defaultResponse);
    }

    public void expectOptionTemplate(String key) {
        expect(key, Constants.getDropdownOptionTemplate());
    }

    public void expectFieldTemplate(String key) {
        expect(key, Constants.getEmbedFieldTemplate());
    }

    public void setupDefaultConfigurations() {}

    @Nullable
    protected JsonObject getProperties() {
        return (componentConfig == null) ? null : componentConfig.get("properties").getAsJsonObject();
    }

}
package me.mrepiko.discordbotbase;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import me.mrepiko.discordbotbase.config.Config;
import me.mrepiko.discordbotbase.database.Common;
import me.mrepiko.discordbotbase.commands.CommandManager;
import me.mrepiko.discordbotbase.components.ComponentManager;
import me.mrepiko.discordbotbase.listeners.general.EventWaiter;
import me.mrepiko.discordbotbase.mics.Constants;
import me.mrepiko.discordbotbase.listeners.general.OnReadyListener;
import me.mrepiko.discordbotbase.modules.Module;
import me.mrepiko.discordbotbase.modules.ModuleManager;
import me.mrepiko.discordbotbase.tasks.TaskManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class DiscordBot {

    @Getter private static DiscordBot instance;
    private JDA jda;
    private Config config;
    private String defaultName, developerGuildId, developerChannelId, timeFormat, dateFormat, defaultIconUrl, truncationIndicator;
    private Color defaultColor;
    @Nullable private Guild developerGuild;
    @Nullable private GuildMessageChannel developerChannel;

    private final HashMap<String, String> endpoints = new HashMap<>();
    private final HashMap<String, String> endpointAuths = new HashMap<>();
    private final HashMap<String, Role> referencedRoles = new HashMap<>();

    private final java.util.List<GatewayIntent> intents = new ArrayList<>();
    private final List<String> admins = new ArrayList<>();

    private CommandManager commandManager;
    private ComponentManager componentManager;
    private ModuleManager moduleManager;
    private TaskManager taskManager;
    private EventWaiter eventWaiter;

    public static void main(String[] args) {
        new DiscordBot();
    }

    public DiscordBot() {
        this.load();
    }

    private void load() {
        instance = this;
        config = new Config(Constants.MAIN_CONFIG_FILE_PATH);

        JsonObject defaultConfigs = config.get("default").getAsJsonObject();
        defaultName = defaultConfigs.get("name").getAsString();
        defaultColor = Color.decode(defaultConfigs.get("color").getAsString());
        defaultIconUrl = defaultConfigs.get("icon_url").getAsString();
        developerGuildId = defaultConfigs.get("developer_guild_id").getAsString();
        developerChannelId = defaultConfigs.get("developer_channel_id").getAsString();
        timeFormat = defaultConfigs.get("time_format").getAsString();
        dateFormat = defaultConfigs.get("date_format").getAsString();
        truncationIndicator = defaultConfigs.get("truncation_indicator").getAsString();

        for (JsonElement e: config.get("admins").getAsJsonArray()) admins.add(e.getAsString());
        for (JsonElement e: config.get("intents").getAsJsonArray()) intents.add(GatewayIntent.valueOf(e.getAsString()));
        for (Map.Entry<String, JsonElement> entry: config.get("endpoints").getAsJsonObject().entrySet()) endpoints.put(entry.getKey(), entry.getValue().getAsString());
        for (Map.Entry<String, JsonElement> entry: config.get("auths").getAsJsonObject().entrySet()) endpointAuths.put(entry.getKey(), entry.getValue().getAsString());

        Common.getDataSource().connect(config.get("databases").getAsJsonArray());
        eventWaiter = new EventWaiter();
        commandManager = new CommandManager();
        componentManager = new ComponentManager();
        moduleManager = new ModuleManager();
        taskManager = new TaskManager();

        JDABuilder builder = JDABuilder.createLight(config.get("token").getAsString())
                .enableIntents(intents)
                .setStatus(OnlineStatus.valueOf(config.get("status").getAsString()))
                .setChunkingFilter(ChunkingFilter.ALL)
                .setMemberCachePolicy((intents.contains(GatewayIntent.GUILD_MEMBERS)) ? MemberCachePolicy.ALL : MemberCachePolicy.DEFAULT)
                .addEventListeners(
                        new OnReadyListener(),
                        eventWaiter,
                        commandManager,
                        componentManager
                )
                .setBulkDeleteSplittingEnabled(false);

        if (!config.getJsonObject("activity").isEmpty()) {
            JsonObject activityObject = config.getJsonObject("activity").getAsJsonObject();
            builder.setActivity(Activity.of(
                    Activity.ActivityType.valueOf(activityObject.get("type").getAsString()),
                    activityObject.get("message").getAsString()
            ));
        }
        jda = builder.build();
    }

    public void setup() {
        if (!developerGuildId.isEmpty()) developerGuild = jda.getGuildById(developerGuildId);
        if (!developerChannelId.isEmpty()) developerChannel = (GuildMessageChannel) jda.getGuildChannelById(developerChannelId);
        for (Map.Entry<String, JsonElement> entry: config.get("referenced_roles").getAsJsonObject().entrySet()) referencedRoles.put(entry.getKey(), jda.getRoleById(entry.getValue().getAsString()));
        moduleManager.setupModules(config.get("modules").getAsJsonObject());
        commandManager.setupCommands();
        componentManager.registerComponentHandlers();
        taskManager.startTasks();
    }

    public void reload() {
        commandManager.reload();
        componentManager.reload();
    }
    public boolean isModuleEnabled(Class<? extends Module> moduleClass) {
        for (Module m: moduleManager.getModules()) {
            if (!m.getClass().equals(moduleClass)) continue;
            return m.isEnabled();
        }
        return false;
    }

}
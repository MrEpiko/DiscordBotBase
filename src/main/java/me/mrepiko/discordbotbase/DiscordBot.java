package me.mrepiko.discordbotbase;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import me.mrepiko.discordbotbase.commands.CommandManager;
import me.mrepiko.discordbotbase.components.ComponentManager;
import me.mrepiko.discordbotbase.config.Config;
import me.mrepiko.discordbotbase.database.Common;
import me.mrepiko.discordbotbase.listeners.EventWaiter;
import me.mrepiko.discordbotbase.listeners.OnReadyListener;
import me.mrepiko.discordbotbase.mics.Constants;
import me.mrepiko.discordbotbase.modules.Module;
import me.mrepiko.discordbotbase.modules.ModuleManager;
import me.mrepiko.discordbotbase.tasks.TaskManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.List;
import java.util.*;

@Getter
public class DiscordBot {

    private static final Logger LOGGER = LoggerFactory.getLogger(DiscordBot.class);

    @Getter
    private static DiscordBot instance;
    private JDA jda;
    private Config config;
    private String defaultName, developerGuildId, developerChannelId, timeFormat, dateFormat, defaultIconUrl, truncationIndicator;
    private Color defaultColor;
    @Nullable
    private Guild developerGuild;
    @Nullable
    private GuildMessageChannel developerChannel;
    private JsonObject constantPlaceholders;
    private JsonObject defaultResponse;

    private boolean development;

    private final HashMap<String, String> endpoints = new HashMap<>();
    private final HashMap<String, String> endpointAuths = new HashMap<>();
    private final HashMap<String, Role> referencedRoles = new HashMap<>();

    private final List<GatewayIntent> intents = new ArrayList<>();
    private final List<String> admins = new ArrayList<>();

    private CommandManager commandManager;
    private ComponentManager componentManager;
    private ModuleManager moduleManager;
    private TaskManager taskManager;

    private EventWaiter eventWaiter;

    private final HashMap<String, User> cachedUsers = new HashMap<>();

    private final Set<String> restrictedIds = new HashSet<>();

    public static void main(String[] args) {
        new DiscordBot();
    }

    public DiscordBot() {
        this.load();
    }

    private void load() {
        instance = this;
        config = new Config(Constants.MAIN_CONFIG_FILE_PATH);

        JsonObject defaultConfigs = config.getJsonObject("default");
        defaultName = defaultConfigs.get("name").getAsString();
        defaultColor = Color.decode(defaultConfigs.get("color").getAsString());
        defaultIconUrl = defaultConfigs.get("icon_url").getAsString();
        developerGuildId = defaultConfigs.get("developer_guild_id").getAsString();
        developerChannelId = defaultConfigs.get("developer_channel_id").getAsString();
        timeFormat = defaultConfigs.get("time_format").getAsString();
        dateFormat = defaultConfigs.get("date_format").getAsString();
        truncationIndicator = defaultConfigs.get("truncation_indicator").getAsString();
        defaultResponse = defaultConfigs.get("response").getAsJsonObject();

        development = config.getBoolean("development");

        constantPlaceholders = config.getJsonObject("constant_placeholders");

        for (JsonElement e : config.getJsonArray("admins")) admins.add(e.getAsString());
        for (JsonElement e : config.getJsonArray("intents")) intents.add(GatewayIntent.valueOf(e.getAsString()));
        for (Map.Entry<String, JsonElement> entry : config.getJsonObject("endpoints").entrySet())
            endpoints.put(entry.getKey(), entry.getValue().getAsString());
        for (Map.Entry<String, JsonElement> entry : config.getJsonObject("auths").entrySet())
            endpointAuths.put(entry.getKey(), entry.getValue().getAsString());

        Common.getDataSource().connect(config.getJsonArray("databases"));

        eventWaiter = new EventWaiter();
        commandManager = new CommandManager();
        componentManager = new ComponentManager();
        moduleManager = new ModuleManager();
        taskManager = new TaskManager();

        JDABuilder builder = JDABuilder.createLight(config.getString("token"))
                .enableIntents(intents)
                .setStatus(OnlineStatus.valueOf(config.getString("status")))
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
        if (!developerChannelId.isEmpty())
            developerChannel = (GuildMessageChannel) jda.getGuildChannelById(developerChannelId);
        for (Map.Entry<String, JsonElement> entry : config.getJsonObject("referenced_roles").entrySet())
            referencedRoles.put(entry.getKey(), jda.getRoleById(entry.getValue().getAsString()));

        new Thread(this::startCommandScanner).start();

        moduleManager.setupModules(config.getJsonObject("modules"));
        commandManager.setupCommands();
        componentManager.registerComponentHandlers();
        taskManager.startTasks();
    }

    private void startCommandScanner() {
        try (Scanner sc = new Scanner(System.in)) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                try {
                    String[] args = line.split(" ");
                    String command = args[0];
                    if (command.equalsIgnoreCase("reboot")) {
                        reboot();
                        return;
                    } else if (command.equalsIgnoreCase("reload")) {
                        reload();
                    }
                } catch (Exception e) {
                    LOGGER.error("Error while scanning command", e);
                }
            }
        }
    }

    public boolean isModuleEnabled(Class<? extends Module> moduleClass) {
        for (Module m : moduleManager.getModules()) {
            if (!m.getClass().equals(moduleClass)) continue;
            return m.isEnabled();
        }
        return false;
    }

    public void reboot() {
        cache();
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                System.exit(0);
            }
        }, 1000L * 8);
    }

    public void reload() {
        commandManager.reload();
        componentManager.reload();
    }

    public void cache() {
        taskManager.stopTasks();
        taskManager.executeCacheableTasks();
    }

}
package me.mrepiko.discordbotbase.commands.types;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Delegate;
import me.mrepiko.discordbotbase.DiscordBot;
import me.mrepiko.discordbotbase.config.Config;
import me.mrepiko.discordbotbase.context.interaction.CommandContext;
import me.mrepiko.discordbotbase.mics.Constants;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Getter
public abstract class Command {

    private final String name;
    @Setter private String discordId;
    private String description;
    private String parentName = "";
    private double cooldown;
    @Delegate private Config commandConfig;

    private final List<String> requiredRoles = new ArrayList<>();
    private final List<String> requiredUsers = new ArrayList<>();
    private final List<String> requiredChannels = new ArrayList<>();
    private final List<Permission> requiredPermissions = new ArrayList<>();
    private final List<Permission> requiredChannelPermissions = new ArrayList<>();
    private final List<Permission> requiredBotPermissions = new ArrayList<>();
    private final List<String> aliases = new ArrayList<>();
    private final List<Option> optionsList = new ArrayList<>();
    private final List<String> guilds = new ArrayList<>();

    private boolean admin;
    private boolean talk;
    @Setter private boolean enabled = true;
    private boolean global;
    private boolean defer;
    private boolean ephemeralDefer;
    private boolean hideOriginalName;

    private final List<Command> children = new ArrayList<>();
    @Setter @Nullable private Command parent;
    private final HashMap<String, List<net.dv8tion.jda.api.interactions.commands.Command.Choice>> autocompleteOptions = new HashMap<>();
    private final HashMap<String, JsonObject> errorHandlers = new HashMap<>();

    private final HashMap<String, Long> cooldowns = new HashMap<>();

    private final JsonObject defaultResponse = DiscordBot.getInstance().getDefaultResponse();

    public Command(String name) {
        this(name, false);
    }

    public Command(String name, boolean responseCommand) {
        this.name = name;
        File folder = new File((responseCommand) ? Constants.RESPONSE_COMMAND_CONFIGURATION_FOLDER_PATH : Constants.COMMAND_CONFIGURATION_FOLDER_PATH);
        if (!folder.exists()) return;
        File file = new File(folder.getPath() + "/" + name + ".json");
        if (!file.exists()) return;
        if (file.length() == 0) {
            try {
                FileWriter writer = new FileWriter(file);
                writer.write(Constants.COMMAND_STRUCTURE);
                writer.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return;
        }
        this.commandConfig = new Config(file.getPath());
        setupProperties();
        setupDefaultConfigurations();
    }

    private void setupProperties() {
        JsonObject properties = get("properties").getAsJsonObject();

        if (properties.has("required_roles")) for (JsonElement e: properties.get("required_roles").getAsJsonArray()) requiredRoles.add(e.getAsString());
        if (properties.has("required_users")) for (JsonElement e: properties.get("required_users").getAsJsonArray()) requiredUsers.add(e.getAsString());
        if (properties.has("required_channels")) for (JsonElement e: properties.get("required_channels").getAsJsonArray()) requiredChannels.add(e.getAsString());
        if (properties.has("required_permissions")) for (JsonElement e: properties.get("required_permissions").getAsJsonArray()) requiredPermissions.add(Permission.valueOf(e.getAsString()));
        if (properties.has("required_channel_permissions")) for (JsonElement e: properties.get("required_channel_permissions").getAsJsonArray()) requiredChannelPermissions.add(Permission.valueOf(e.getAsString()));
        if (properties.has("required_bot_permissions")) for (JsonElement e: properties.get("required_bot_permissions").getAsJsonArray()) requiredBotPermissions.add(Permission.valueOf(e.getAsString()));
        if (properties.has("aliases")) for (JsonElement e: properties.get("aliases").getAsJsonArray()) aliases.add(e.getAsString());
        if (properties.has("guilds")) for (JsonElement e: properties.get("guilds").getAsJsonArray()) guilds.add(e.getAsString());

        JsonObject globalErrorHandlersObject = DiscordBot.getInstance().getConfig().get("error_handlers").getAsJsonObject();
        JsonObject errorHandlersObject = globalErrorHandlersObject.get("commands").getAsJsonObject();
        JsonObject optionErrorHandlersObject = globalErrorHandlersObject.get("command_options").getAsJsonObject();
        JsonObject localErrorHandlersObject = (properties.has("error_handlers")) ? properties.get("error_handlers").getAsJsonObject() : new JsonObject();
        for (String e: errorHandlersObject.keySet()) {
            if (localErrorHandlersObject.has(e) && !localErrorHandlersObject.get(e).getAsJsonObject().isEmpty())
                errorHandlers.put(e, localErrorHandlersObject.get(e).getAsJsonObject());
            else errorHandlers.put(e, errorHandlersObject.get(e).getAsJsonObject());
        }
        global = properties.has("global") && properties.get("global").getAsBoolean();
        description = (properties.has("description")) ? properties.get("description").getAsString() : "N/A";
        cooldown = (properties.has("cooldown")) ? properties.get("cooldown").getAsDouble() : 0;
        admin = properties.has("admin") && properties.get("admin").getAsBoolean();
        talk = properties.has("talk") && properties.get("talk").getAsBoolean();
        enabled = !properties.has("enabled") || properties.get("enabled").getAsBoolean();
        defer = properties.has("ephemeral_defer");
        ephemeralDefer = defer && properties.get("ephemeral_defer").getAsBoolean();
        hideOriginalName = properties.has("hide_original_name") && properties.get("hide_original_name").getAsBoolean();
        parentName = (properties.has("parent")) ? properties.get("parent").getAsString() : "";
        if (properties.has("options")) {
            for (JsonElement e: properties.get("options").getAsJsonArray()) {
                JsonObject o = e.getAsJsonObject();
                boolean autocomplete = o.has("autocomplete") && o.get("autocomplete").getAsBoolean();
                String optionName = o.get("name").getAsString();
                OptionData optionData = new OptionData(
                        OptionType.valueOf(o.get("type").getAsString()),
                        optionName,
                        o.get("description").getAsString(),
                        o.has("required") && o.get("required").getAsBoolean(),
                        autocomplete
                );
                if (o.has("choices")) {
                    if (autocomplete) {
                        autocompleteOptions.put(optionName, new ArrayList<>());
                        for (JsonElement e1: o.get("choices").getAsJsonArray()) {
                            JsonObject o1 = e1.getAsJsonObject();
                            autocompleteOptions.get(optionName).add(new net.dv8tion.jda.api.interactions.commands.Command.Choice(o1.get("name").getAsString(), o1.get("value").getAsString()));
                        }
                    } else for (JsonElement e1: o.get("choices").getAsJsonArray()) {
                        JsonObject o1 = e1.getAsJsonObject();
                        optionData.addChoice(o1.get("name").getAsString(), o1.get("value").getAsString());
                    }
                }
                boolean optionAdmin = o.has("admin") && o.get("admin").getAsBoolean();
                boolean optionEnabled = !o.has("enabled") || o.get("enabled").getAsBoolean();
                List<String> optionRequiredRoles = new ArrayList<>();
                List<String> optionRequiredChannels = new ArrayList<>();
                List<String> optionRequiredUsers = new ArrayList<>();
                List<Permission> optionRequiredPermissions = new ArrayList<>();
                List<Permission> optionRequiredChannelPermissions = new ArrayList<>();
                if (o.has("required_roles")) for (JsonElement e1: o.get("required_roles").getAsJsonArray()) optionRequiredRoles.add(e1.getAsString());
                if (o.has("required_users")) for (JsonElement e1: o.get("required_users").getAsJsonArray()) optionRequiredUsers.add(e1.getAsString());
                if (o.has("required_channels")) for (JsonElement e1: o.get("required_channels").getAsJsonArray()) optionRequiredChannels.add(e1.getAsString());
                if (o.has("required_permissions")) for (JsonElement e1: o.get("required_permissions").getAsJsonArray()) optionRequiredPermissions.add(Permission.valueOf(e1.getAsString()));
                if (o.has("required_channel_permissions")) for (JsonElement e1: o.get("required_channel_permissions").getAsJsonArray()) optionRequiredChannelPermissions.add(Permission.valueOf(e1.getAsString()));
                HashMap<String, JsonObject> optionErrorHandlers = new HashMap<>();
                JsonObject optionLocalErrorHandlersObject = (o.has("error_handlers")) ? o.get("error_handlers").getAsJsonObject() : new JsonObject();
                for (String e1: optionErrorHandlersObject.keySet()) {
                    if (optionLocalErrorHandlersObject.has(e1) && !optionLocalErrorHandlersObject.get(e1).getAsJsonObject().isEmpty())
                        optionErrorHandlers.put(e1, optionLocalErrorHandlersObject.get(e1).getAsJsonObject());
                    else optionErrorHandlers.put(e1, optionErrorHandlersObject.get(e1).getAsJsonObject());
                }
                optionsList.add(new Option(
                        optionData,
                        optionAdmin,
                        optionRequiredRoles,
                        optionRequiredChannels,
                        optionRequiredUsers,
                        optionRequiredPermissions,
                        optionRequiredChannelPermissions,
                        optionEnabled,
                        optionErrorHandlers
                ));
            }
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

    public void addChild(Command command) {
        if (contains(command)) return;
        children.add(command);
    }

    private boolean contains(Command command) {
        for (Command c: children) if (c.getName().equalsIgnoreCase(command.getName())) return true;
        return false;
    }

    public abstract void setupDefaultConfigurations();

    public abstract void handle(CommandContext ctx);

    @AllArgsConstructor
    @Getter
    public static class Option {
        private final OptionData optionData;
        private boolean admin;
        private final List<String> requiredRoles;
        private final List<String> requiredChannels;
        private final List<String> requiredUsers;
        private final List<Permission> requiredPermissions;
        private final List<Permission> requiredChannelPermissions;
        private final boolean enabled;
        private final HashMap<String, JsonObject> errorHandlers;
    }

}
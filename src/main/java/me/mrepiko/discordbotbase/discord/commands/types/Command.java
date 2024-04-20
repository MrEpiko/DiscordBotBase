package me.mrepiko.discordbotbase.discord.commands.types;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import me.mrepiko.discordbotbase.common.config.Config;
import me.mrepiko.discordbotbase.discord.context.interaction.CommandContext;
import me.mrepiko.discordbotbase.discord.mics.Constants;
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
    private String description;
    private String parentName = "";
    private double cooldown;
    private Config commandConfig;

    private final List<String> requiredRoles = new ArrayList<>();
    private final List<String> requiredUsers = new ArrayList<>();
    private final List<String> requiredChannels = new ArrayList<>();
    private final List<String> aliases = new ArrayList<>();
    private final List<OptionData> optionDataList = new ArrayList<>();
    private final List<String> guilds = new ArrayList<>();

    private boolean admin;
    private boolean enabled;
    private boolean global;
    private boolean defer;
    private boolean ephemeralDefer;
    private boolean hideOriginalName;

    private final List<Command> children = new ArrayList<>();
    @Setter @Nullable private Command parent;
    private final HashMap<String, List<net.dv8tion.jda.api.interactions.commands.Command.Choice>> autocompleteOptions = new HashMap<>();

    private final HashMap<String, Long> cooldowns = new HashMap<>();

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
        try {
            this.commandConfig = new Config(file.getPath());
            setupProperties();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void setupProperties() {
        JsonObject properties = commandConfig.get("properties").getAsJsonObject();

        if (properties.has("required_roles")) for (JsonElement e: properties.get("required_roles").getAsJsonArray()) requiredRoles.add(e.getAsString());
        if (properties.has("required_users")) for (JsonElement e: properties.get("required_users").getAsJsonArray()) requiredUsers.add(e.getAsString());
        if (properties.has("required_channels")) for (JsonElement e: properties.get("required_channels").getAsJsonArray()) requiredChannels.add(e.getAsString());
        if (properties.has("aliases")) for (JsonElement e: properties.get("aliases").getAsJsonArray()) aliases.add(e.getAsString());
        if (properties.has("guilds")) for (JsonElement e: properties.get("guilds").getAsJsonArray()) guilds.add(e.getAsString());
        global = properties.has("global") && properties.get("global").getAsBoolean();
        description = (properties.has("description")) ? properties.get("description").getAsString() : "N/A";
        cooldown = (properties.has("cooldown")) ? properties.get("cooldown").getAsDouble() : 0;
        admin = properties.has("admin") && properties.get("admin").getAsBoolean();
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
                optionDataList.add(optionData);
            }
        }
    }

    public void addChild(Command command) {
        if (contains(command)) return;
        children.add(command);
    }

    private boolean contains(Command command) {
        for (Command c: children) if (c.getName().equalsIgnoreCase(command.getName())) return true;
        return false;
    }

    public abstract void handle(CommandContext ctx);

}
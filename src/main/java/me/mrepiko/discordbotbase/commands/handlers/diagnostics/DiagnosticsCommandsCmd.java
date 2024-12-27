package me.mrepiko.discordbotbase.commands.handlers.diagnostics;

import me.mrepiko.discordbotbase.DiscordBot;
import me.mrepiko.discordbotbase.commands.types.Command;
import me.mrepiko.discordbotbase.context.interaction.CommandContext;
import me.mrepiko.discordbotbase.mics.ResponseBuilder;
import me.mrepiko.discordbotbase.mics.placeholders.PlaceholderMap;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class DiagnosticsCommandsCmd extends Command {

    public DiagnosticsCommandsCmd() {
        super("diagnostics_commands");
    }

    @Override
    public void handle(CommandContext ctx) {
        PlaceholderMap map = new PlaceholderMap(ctx);
        DiscordBot instance = DiscordBot.getInstance();

        StringBuilder commands = new StringBuilder();
        List<Command> sortedCommands = new ArrayList<>(instance.getCommandManager().getCommands());
        sortedCommands.sort(Comparator.comparing(Command::getName));
        sortedCommands.sort(Comparator.comparing(Command::isAdmin));
        for (Command command : sortedCommands) {
            if (!command.getChildren().isEmpty()) continue;
            String id = (command.getParent() == null) ? command.getDiscordId() : command.getParent().getDiscordId();
            map.put("command_name", command.getName());
            map.put("command_discord_id", id);
            map.put("command_mention",  "</" + command.getName().replace("_", " ") + ":" + id + ">");
            map.put("command_guild_count", (command.isGlobal() || command.getGuilds().isEmpty()) ? instance.getJda().getGuilds().size() : command.getGuilds().size());
            map.put("command_status", getString((command.isEnabled()) ? "enabled_template" : "disabled_template"));
            commands.append(map.applyPlaceholders(getString("command_template")));
        }
        map.put("commands", commands.toString());

        ResponseBuilder.build(map, getJsonObject("response")).send();
    }

    @Override
    public void setupDefaultConfigurations() {
        expectResponseObject("response");
        expect("enabled_template", "");
        expect("disabled_template", "");
        expect("command_template", "");
    }
}

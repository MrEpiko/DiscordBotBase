package me.mrepiko.discordbotbase.commands.handlers.commands;

import me.mrepiko.discordbotbase.DiscordBot;
import me.mrepiko.discordbotbase.commands.types.Command;
import me.mrepiko.discordbotbase.context.interaction.CommandContext;
import me.mrepiko.discordbotbase.mics.ResponseBuilder;
import me.mrepiko.discordbotbase.mics.placeholders.PlaceholderMap;

public class CommandDisableCmd extends Command {

    public CommandDisableCmd() {
        super("command_disable");
    }

    @Override
    public void handle(CommandContext ctx) {
        PlaceholderMap map = new PlaceholderMap(ctx);
        String commandName = ctx.getOptionAsString("command", "");
        map.put("command_name", commandName);
        boolean enabled = false;
        for (Command command : DiscordBot.getInstance().getCommandManager().getCommands()) {
            if (command.isAdmin()) continue;
            if (commandName.isEmpty()) command.setEnabled(false);
            else if (command.getName().equalsIgnoreCase(commandName)) {
                command.setEnabled(false);
                enabled = true;
                break;
            }
        }
        if (!commandName.isEmpty() && !enabled) {
            ResponseBuilder.build(map, getJsonObject("invalid_command_response")).send();
            return;
        }
        ResponseBuilder.build(map, getJsonObject((commandName.isEmpty() ? "disabled_all_response" : "response"))).send();
    }

    @Override
    public void setupDefaultConfigurations() {
        expectResponseObject("response");
        expectResponseObject("disabled_all_response");
        expectResponseObject("invalid_command_response");
    }
}

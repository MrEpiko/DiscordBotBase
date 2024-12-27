package me.mrepiko.discordbotbase.commands.handlers.commands;

import me.mrepiko.discordbotbase.DiscordBot;
import me.mrepiko.discordbotbase.commands.types.Command;
import me.mrepiko.discordbotbase.context.interaction.CommandContext;
import me.mrepiko.discordbotbase.mics.ResponseBuilder;
import me.mrepiko.discordbotbase.mics.placeholders.PlaceholderMap;

public class CommandEnableCmd extends Command {

    public CommandEnableCmd() {
        super("command_enable");
    }

    @Override
    public void handle(CommandContext ctx) {
        PlaceholderMap map = new PlaceholderMap(ctx);
        String commandName = ctx.getOptionAsString("command", "");
        map.put("command_name", commandName);
        boolean disabled = false;
        for (Command command : DiscordBot.getInstance().getCommandManager().getCommands()) {
            if (commandName.isEmpty()) command.setEnabled(true);
            else if (command.getName().equalsIgnoreCase(commandName)) {
                disabled = true;
                command.setEnabled(true);
                break;
            }
        }
        if (!commandName.isEmpty() && !disabled) {
            ResponseBuilder.build(map, getJsonObject("invalid_command_response")).send();
            return;
        }
        ResponseBuilder.build(map, getJsonObject(commandName.isEmpty() ? "enabled_all_response" : "response")).send();
    }

    @Override
    public void setupDefaultConfigurations() {
        expectResponseObject("response");
        expectResponseObject("enabled_all_response");
        expectResponseObject("invalid_command_response");
    }
}

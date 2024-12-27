package me.mrepiko.discordbotbase.commands.handlers.commands;

import me.mrepiko.discordbotbase.commands.types.Command;
import me.mrepiko.discordbotbase.context.interaction.CommandContext;

public class CommandCmd extends Command {

    public CommandCmd() {
        super("command");
    }

    @Override
    public void handle(CommandContext ctx) {}

    @Override
    public void setupDefaultConfigurations() {}

}

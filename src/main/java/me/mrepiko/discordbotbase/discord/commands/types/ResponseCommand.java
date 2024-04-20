package me.mrepiko.discordbotbase.discord.commands.types;

import me.mrepiko.discordbotbase.discord.context.interaction.CommandContext;
import me.mrepiko.discordbotbase.discord.mics.PlaceholderMap;
import me.mrepiko.discordbotbase.discord.mics.ResponseBuilder;

public class ResponseCommand extends Command {

    public ResponseCommand(String name) {
        super(name, true);
    }

    @Override
    public void handle(CommandContext ctx) {
        PlaceholderMap map = new PlaceholderMap(ctx);
        ResponseBuilder.buildAndSend(map, super.getCommandConfig().get("response").getAsJsonObject());
    }
}

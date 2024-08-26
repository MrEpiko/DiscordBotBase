package me.mrepiko.discordbotbase.commands.types;

import me.mrepiko.discordbotbase.context.interaction.CommandContext;
import me.mrepiko.discordbotbase.mics.PlaceholderMap;
import me.mrepiko.discordbotbase.mics.ResponseBuilder;

public class ResponseCommand extends Command {

    public ResponseCommand(String name) {
        super(name, true);
    }

    @Override
    public void handle(CommandContext ctx) {
        PlaceholderMap map = new PlaceholderMap(ctx);
        ResponseBuilder.build(map, super.getCommandConfig().get("response").getAsJsonObject()).send();
    }
}

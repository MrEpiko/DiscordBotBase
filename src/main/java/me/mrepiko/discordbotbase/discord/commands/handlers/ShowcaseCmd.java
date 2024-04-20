package me.mrepiko.discordbotbase.discord.commands.handlers;

import com.google.gson.JsonObject;
import me.mrepiko.discordbotbase.discord.commands.types.Command;
import me.mrepiko.discordbotbase.discord.context.interaction.CommandContext;
import me.mrepiko.discordbotbase.discord.mics.PlaceholderMap;
import me.mrepiko.discordbotbase.discord.mics.ResponseBuilder;

import java.util.Random;

public class ShowcaseCmd extends Command {

    public ShowcaseCmd() {
        super("showcase");
    }

    @Override
    public void handle(CommandContext ctx) {
        PlaceholderMap map = new PlaceholderMap(ctx);
        String fruit = ctx.getOptionAsString("fruit");
        map.put("fruit", fruit);
        map.put("random_number", new Random().nextInt(10) + 1);
        JsonObject bonus = new JsonObject();
        bonus.addProperty("previous_fruit", fruit);
        ResponseBuilder.buildAndSend(map, super.getCommandConfig().get("response").getAsJsonObject(), bonus);
    }

}

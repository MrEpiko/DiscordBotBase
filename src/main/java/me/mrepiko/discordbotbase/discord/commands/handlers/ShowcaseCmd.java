package me.mrepiko.discordbotbase.discord.commands.handlers;

import com.google.gson.JsonObject;
import me.mrepiko.discordbotbase.discord.commands.types.Command;
import me.mrepiko.discordbotbase.discord.context.interaction.ButtonContext;
import me.mrepiko.discordbotbase.discord.context.interaction.CommandContext;
import me.mrepiko.discordbotbase.discord.mics.PlaceholderMap;
import me.mrepiko.discordbotbase.discord.mics.ResponseBuilder;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;

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
        ResponseBuilder.build(map, super.getCommandConfig().get("response").getAsJsonObject())
                .setComponentBonus(bonus)
                .setConsumer(interactionContext -> {
                    if (!(interactionContext instanceof ButtonContext buttonContext)) return;
                    System.out.println((buttonContext.getButton().getStyle() == ButtonStyle.DANGER) ? "This is a dangerous button!" : "This is not so dangerous button.");
                })
                .send();
    }

}

package me.mrepiko.discordbotbase.components.handlers;

import com.google.gson.JsonObject;
import me.mrepiko.discordbotbase.components.general.types.DropdownHandler;
import me.mrepiko.discordbotbase.context.interaction.DropdownContext;
import me.mrepiko.discordbotbase.mics.PlaceholderMap;
import me.mrepiko.discordbotbase.mics.ResponseBuilder;

public class ShowcaseDropdownHandler extends DropdownHandler {

    public ShowcaseDropdownHandler() {
        super("showcase_dropdown");
    }

    @Override
    public void handle(DropdownContext ctx) {
        PlaceholderMap map = new PlaceholderMap(ctx);
        String fruit = ctx.getSelectedOptions().get(0).getValue();
        map.put("choice", fruit);
        map.put("previous_fruit", (ctx.getRuntimeComponent() != null) ? ctx.getRuntimeComponent().get("previous_fruit").getAsString() : "N/A");
        JsonObject bonus = new JsonObject();
        bonus.addProperty("previous_fruit", fruit);
        ResponseBuilder.build(map, super.getComponentConfig().get("response").getAsJsonObject()).setComponentBonus(bonus).send();
    }
}

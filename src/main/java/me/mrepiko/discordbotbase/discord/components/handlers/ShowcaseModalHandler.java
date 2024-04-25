package me.mrepiko.discordbotbase.discord.components.handlers;

import me.mrepiko.discordbotbase.discord.components.general.types.ModalHandler;
import me.mrepiko.discordbotbase.discord.context.interaction.ModalContext;
import me.mrepiko.discordbotbase.discord.mics.PlaceholderMap;
import me.mrepiko.discordbotbase.discord.mics.ResponseBuilder;

public class ShowcaseModalHandler extends ModalHandler {

    public ShowcaseModalHandler() {
        super("showcase_modal");
    }

    @Override
    public void handle(ModalContext ctx) {
        String answer = ctx.getValue("showcase_modal.answer", "No answer!");
        PlaceholderMap map = new PlaceholderMap(ctx);
        map.put("answer", answer);
        ResponseBuilder.build(map, super.getComponentConfig().get("response").getAsJsonObject()).send();
    }
}

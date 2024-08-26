package me.mrepiko.discordbotbase.components.handlers;

import me.mrepiko.discordbotbase.components.general.types.ModalHandler;
import me.mrepiko.discordbotbase.context.interaction.ModalContext;
import me.mrepiko.discordbotbase.mics.PlaceholderMap;
import me.mrepiko.discordbotbase.mics.ResponseBuilder;

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

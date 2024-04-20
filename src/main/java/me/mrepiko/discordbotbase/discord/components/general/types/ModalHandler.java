package me.mrepiko.discordbotbase.discord.components.general.types;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import me.mrepiko.discordbotbase.discord.components.general.ComponentHandler;
import me.mrepiko.discordbotbase.discord.context.interaction.ModalContext;
import me.mrepiko.discordbotbase.discord.mics.utils.ComponentUtils;
import net.dv8tion.jda.api.interactions.modals.Modal;

@Getter
public abstract class ModalHandler extends ComponentHandler {

    @Setter private Modal modal;

    public ModalHandler(String name) {
        super(name);
        JsonObject properties = getProperties();
        if (properties == null) return;
        this.modal = ComponentUtils.createModal(name, properties);
    }

    public abstract void handle(ModalContext ctx);

}

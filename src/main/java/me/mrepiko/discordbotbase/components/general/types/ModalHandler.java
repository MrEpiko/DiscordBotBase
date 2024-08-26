package me.mrepiko.discordbotbase.components.general.types;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import me.mrepiko.discordbotbase.components.general.ComponentHandler;
import me.mrepiko.discordbotbase.context.interaction.ModalContext;
import me.mrepiko.discordbotbase.mics.utils.ComponentUtils;
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

package me.mrepiko.discordbotbase.discord.components.general.types;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import me.mrepiko.discordbotbase.discord.components.general.BasicComponentHandler;
import me.mrepiko.discordbotbase.discord.context.interaction.ButtonContext;
import me.mrepiko.discordbotbase.discord.mics.utils.ComponentUtils;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

@Getter
public abstract class ButtonHandler extends BasicComponentHandler {

    @Setter private Button button;

    public ButtonHandler(String name) {
        super(name);
        JsonObject properties = getProperties();
        if (properties == null) return;
        button = ComponentUtils.createButton(name, properties);
        setItemComponent(button);
    }

    public abstract void handle(ButtonContext ctx);

}

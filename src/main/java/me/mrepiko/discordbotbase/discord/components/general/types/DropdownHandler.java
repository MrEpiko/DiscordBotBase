package me.mrepiko.discordbotbase.discord.components.general.types;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import me.mrepiko.discordbotbase.discord.components.general.BasicComponentHandler;
import me.mrepiko.discordbotbase.discord.context.interaction.DropdownContext;
import me.mrepiko.discordbotbase.discord.mics.utils.ComponentUtils;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

@Getter
public abstract class DropdownHandler extends BasicComponentHandler {

    @Setter private StringSelectMenu dropdown;

    public DropdownHandler(String name) {
        super(name);
        JsonObject properties = getProperties();
        if (properties == null) return;
        this.dropdown = ComponentUtils.createDropdown(name, properties);
        setItemComponent(dropdown);
    }

    public abstract void handle(DropdownContext ctx);

}
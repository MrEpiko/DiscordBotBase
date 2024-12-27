package me.mrepiko.discordbotbase.components.general.types;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import me.mrepiko.discordbotbase.components.ComponentType;
import me.mrepiko.discordbotbase.components.general.BasicComponentHandler;
import me.mrepiko.discordbotbase.context.interaction.DropdownContext;
import me.mrepiko.discordbotbase.mics.utils.ComponentUtils;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

@Setter
@Getter
public abstract class DropdownHandler extends BasicComponentHandler {

    private StringSelectMenu dropdown;

    public DropdownHandler(String name) {
        super(name);
        JsonObject properties = getProperties();
        if (properties == null) return;
        componentType = ComponentType.DROPDOWN;
        this.dropdown = ComponentUtils.createDropdown(name, properties);
        setItemComponent(dropdown);
    }

    public abstract void handle(DropdownContext ctx);

}
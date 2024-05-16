package me.mrepiko.discordbotbase.discord.mics.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ComponentUtils {

    public static ItemComponent createComponent(String name, JsonObject object) {
        return (object.has("style")) ? createButton(name, object) : createDropdown(name, object);
    }

    public static Button createButton(String name, JsonObject object) {
        Button button = Button.of(
                ButtonStyle.valueOf(object.get("style").getAsString()),
                name + "." + new Random().nextInt(9999),
                object.get("label").getAsString()
        );
        if (object.has("emoji")) button = button.withEmoji(Emoji.fromUnicode(Utils.adaptEmoji(object.get("emoji").getAsString())));
        return button;
    }

    public static StringSelectMenu createDropdown(String name, JsonObject object) {
        StringSelectMenu.Builder menuBuilder = StringSelectMenu.create(name + "." + new Random().nextInt(9999))
                .setPlaceholder(object.get("placeholder").getAsString())
                .setMinValues(object.get("min_options").getAsInt())
                .setMaxValues(object.get("max_options").getAsInt());
        for (JsonElement e: object.get("options").getAsJsonArray()) {
            JsonObject o = e.getAsJsonObject();
            String optionValue = o.get("value").getAsString();
            String optionName = o.get("name").getAsString();
            String optionDescription = (o.has("description")) ? o.get("description").getAsString() : "";
            Emoji optionEmoji = (o.has("emoji")) ? Emoji.fromUnicode(Utils.adaptEmoji(o.get("emoji").getAsString())) : null;
            if (optionEmoji == null) {
                if (optionDescription.isEmpty()) menuBuilder.addOption(optionName, optionValue);
                else menuBuilder.addOption(optionName, optionValue, optionDescription);
            } else {
                if (optionDescription.isEmpty()) menuBuilder.addOption(optionName, optionValue, optionEmoji);
                else menuBuilder.addOption(optionName, optionValue, optionDescription, optionEmoji);
            }
        }
        return menuBuilder.build();
    }

    public static Modal createModal(String name, JsonObject object) {
        Modal.Builder modalBuilder = Modal.create(name + "." + new Random().nextInt(9999), object.get("title").getAsString());
        List<ActionRow> fields = new ArrayList<>();
        for (JsonElement e: object.get("fields").getAsJsonArray()) {
            JsonObject o = e.getAsJsonObject();
            TextInput.Builder textInputBuilder = TextInput.create(
                    o.get("id").getAsString(),
                    o.get("label").getAsString(),
                    TextInputStyle.valueOf(o.get("style").getAsString())
            );
            if (o.has("min_length")) textInputBuilder.setMinLength(o.get("min_length").getAsInt());
            if (o.has("max_length")) textInputBuilder.setMaxLength(o.get("max_length").getAsInt());
            if (o.has("placeholder")) textInputBuilder.setPlaceholder(o.get("placeholder").getAsString());
            if (o.has("value")) textInputBuilder.setValue(o.get("value").getAsString());
            textInputBuilder.setRequired((!o.has("required") || o.get("required").getAsBoolean()));
            fields.add(ActionRow.of(textInputBuilder.build()));
        }
        modalBuilder.addComponents(fields);
        return modalBuilder.build();
    }

}

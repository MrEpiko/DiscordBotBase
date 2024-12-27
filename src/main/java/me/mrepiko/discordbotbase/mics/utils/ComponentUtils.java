package me.mrepiko.discordbotbase.mics.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import me.mrepiko.discordbotbase.DiscordBot;
import me.mrepiko.discordbotbase.components.ComponentHandlerOverrides;
import me.mrepiko.discordbotbase.components.RuntimeComponent;
import me.mrepiko.discordbotbase.components.general.BasicComponentHandler;
import me.mrepiko.discordbotbase.components.general.ComponentHandler;
import me.mrepiko.discordbotbase.components.general.types.DropdownHandler;
import me.mrepiko.discordbotbase.mics.placeholders.PlaceholderMap;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ComponentUtils {

    public static ItemComponent createComponent(String name, JsonObject object, PlaceholderMap map) {
        return (object.has("style")) ? createButton(name, object, map) : createDropdown(name, object, map);
    }

    public static Button createButton(String name, JsonObject object) {
        return createButton(name, object, new PlaceholderMap());
    }

    public static Button createButton(String name, JsonObject object, PlaceholderMap map) {
        Button button = Button.of(
                ButtonStyle.valueOf(object.get("style").getAsString()),
                (Utils.isUrl(name)) ? name : name + "." + new Random().nextInt(9999),
                map.applyPlaceholders(object.get("label").getAsString())
        );
        if (object.has("emoji")) button = button.withEmoji(Emoji.fromFormatted(map.applyPlaceholders(object.get("emoji").getAsString())));
        return button;
    }

    public static StringSelectMenu createDropdown(String name, JsonObject object) {
        return createDropdown(name, object, new PlaceholderMap());
    }

    public static StringSelectMenu createDropdown(String name, JsonObject object, PlaceholderMap map) {
        List<DropdownOption> options = new ArrayList<>();
        for (JsonElement e : object.get("options").getAsJsonArray()) {
            JsonObject o = e.getAsJsonObject();
            options.add(DropdownOption.mapFromJsonObject(o, map));
        }
        return createDropdown(name, object.get("placeholder").getAsString(), object.get("min_options").getAsInt(), object.get("max_options").getAsInt(), options);
    }

    public static StringSelectMenu createDropdown(DropdownHandler dropdownHandler, List<DropdownOption> options) {
        return createDropdown(dropdownHandler.getName(), dropdownHandler.getDropdown().getPlaceholder(), dropdownHandler.getDropdown().getMinValues(), dropdownHandler.getDropdown().getMaxValues(), options);
    }

    public static StringSelectMenu createDropdown(String name, JsonObject object, List<DropdownOption> options) {
        return createDropdown(name, object.get("placeholder").getAsString(), object.get("min_options").getAsInt(), object.get("max_options").getAsInt(), options);
    }

    public static StringSelectMenu createDropdown(String name, String placeholder, int minOptions, int maxOptions, List<DropdownOption> options) {
        StringSelectMenu.Builder menuBuilder = StringSelectMenu.create(name + "." + new Random().nextInt(9999))
                .setPlaceholder(placeholder)
                .setMinValues(minOptions)
                .setMaxValues(maxOptions);
        for (DropdownOption option : options) {
            if (option.getEmoji() == null) {
                if (option.getDescription().isEmpty()) menuBuilder.addOption(option.getLabel(), option.getValue());
                else menuBuilder.addOption(option.getLabel(), option.getValue(), option.getDescription());
            } else {
                if (option.getDescription().isEmpty()) menuBuilder.addOption(option.getLabel(), option.getValue(), option.getEmoji());
                else menuBuilder.addOption(option.getLabel(), option.getValue(), option.getDescription(), option.getEmoji());
            }
        }
        return menuBuilder.build();
    }

    public static Modal createModal(String name, JsonObject object, PlaceholderMap map) {
        Modal.Builder modalBuilder = Modal.create(name + "." + new Random().nextInt(9999), map.applyPlaceholders(object.get("title").getAsString()));
        List<ActionRow> fields = new ArrayList<>();
        for (JsonElement e : object.get("fields").getAsJsonArray()) {
            JsonObject o = e.getAsJsonObject();
            TextInput.Builder textInputBuilder = TextInput.create(
                    o.get("id").getAsString(),
                    map.applyPlaceholders(o.get("label").getAsString()),
                    TextInputStyle.valueOf(o.get("style").getAsString())
            );
            if (o.has("min_length")) textInputBuilder.setMinLength(o.get("min_length").getAsInt());
            if (o.has("max_length")) textInputBuilder.setMaxLength(o.get("max_length").getAsInt());
            if (o.has("placeholder")) textInputBuilder.setPlaceholder(map.applyPlaceholders(o.get("placeholder").getAsString()));
            if (o.has("value")) textInputBuilder.setValue(map.applyPlaceholders(o.get("value").getAsString()));
            textInputBuilder.setRequired((!o.has("required") || o.get("required").getAsBoolean()));
            fields.add(ActionRow.of(textInputBuilder.build()));
        }
        modalBuilder.addComponents(fields);
        return modalBuilder.build();
    }

    public static ComponentHandlerOverrides combineAndGetOverrides(BasicComponentHandler componentHandler, RuntimeComponent runtimeComponent) {
        ComponentHandlerOverrides overrides = new ComponentHandlerOverrides();
        if (runtimeComponent == null) {
            overrides.setDefer(componentHandler.isDefer());
            overrides.setEphemeralDefer(componentHandler.isEphemeralDefer());
            overrides.setInvokerOnly(componentHandler.isInvokerOnly());
            overrides.setDeleteAfterTimeout(componentHandler.getDeleteAfterTimeout());
            overrides.setTimeout(componentHandler.getTimeout());
            overrides.setDisableAllOnceUsed(componentHandler.isDisableAllOnceUsed());
            overrides.setDisableOnceUsed(componentHandler.isDisableOnceUsed());
            return overrides;
        }

        overrides.setDefer(runtimeComponent.isDefer());
        overrides.setEphemeralDefer(runtimeComponent.isEphemeralDefer());
        overrides.setInvokerOnly(runtimeComponent.isInvokerOnly());
        overrides.setDeleteAfterTimeout(runtimeComponent.getDeleteAfterTimeout());
        overrides.setTimeout(runtimeComponent.getTimeout());
        overrides.setDisableAllOnceUsed(runtimeComponent.isDisableAllOnceUsed());
        overrides.setDisableOnceUsed(runtimeComponent.isDisableOnceUsed());
        return overrides;
    }



    @AllArgsConstructor(staticName = "of")
    @Getter
    public static class DropdownOption {

        private final String label;
        private final String value;
        private final String description;
        @Nullable private final Emoji emoji;

        private static DropdownOption mapFromDropdownHandler(String dropdownHandlerName, PlaceholderMap map) {
            ComponentHandler componentHandler = DiscordBot.getInstance().getComponentManager().getComponentHandler(dropdownHandlerName);
            if (!(componentHandler instanceof DropdownHandler dropdownHandler)) throw new IllegalArgumentException("ComponentHandler " + dropdownHandlerName + " is not a DropdownHandler");

            SelectOption option = dropdownHandler.getDropdown().getOptions().get(0);
            return new DropdownOption(
                    map.applyPlaceholders(option.getLabel()),
                    map.applyPlaceholders(option.getValue()),
                    map.applyPlaceholders(option.getDescription()),
                    (option.getEmoji() != null) ? Emoji.fromFormatted(map.applyPlaceholders(option.getEmoji().getFormatted())) : null
            );
        }

        private static DropdownOption mapFromJsonObject(JsonObject jsonObject, PlaceholderMap map) {
            return new DropdownOption(
                    map.applyPlaceholders(jsonObject.get("label").getAsString()),
                    map.applyPlaceholders(jsonObject.get("value").getAsString()),
                    map.applyPlaceholders(jsonObject.get("description").getAsString()),
                    (jsonObject.has("emoji")) ? Emoji.fromFormatted(map.applyPlaceholders(jsonObject.get("emoji").getAsString())) : null
            );
        }

        public static DropdownOption of(String label, String value, String description) {
            return new DropdownOption(label, value, description, null);
        }

        public static DropdownOption decideOf(String dropdownHandlerName, JsonObject jsonObject, PlaceholderMap map) {
            if (jsonObject == null || jsonObject.isEmpty()) return mapFromDropdownHandler(dropdownHandlerName, map);
            else return mapFromJsonObject(jsonObject, map);
        }
    }

}

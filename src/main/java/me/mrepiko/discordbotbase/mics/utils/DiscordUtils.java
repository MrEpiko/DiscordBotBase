package me.mrepiko.discordbotbase.mics.utils;

import com.google.gson.JsonObject;
import me.mrepiko.discordbotbase.DiscordBot;
import me.mrepiko.discordbotbase.mics.placeholders.PlaceholderMap;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.components.ActionComponent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

public class DiscordUtils {

    public static MessageEmbed.Field mapFieldFromJsonObject(JsonObject jsonObject, PlaceholderMap map) {
        String indicator = DiscordBot.getInstance().getTruncationIndicator();
        boolean inline = jsonObject.has("inline") && jsonObject.get("inline").getAsBoolean();
        if (jsonObject.has("blank") && jsonObject.get("blank").getAsBoolean()) return getEmptyField();
        String name = map.applyPlaceholders(jsonObject.get("name").getAsString());
        String value = map.applyPlaceholders(jsonObject.get("value").getAsString());
        if (name.length() > 256) name = name.substring(0, 252) + indicator;
        if (value.length() > 1024) value = value.substring(0, 1020) + indicator;
        return new MessageEmbed.Field(name, value, inline);
    }

    public static MessageEmbed.Field getEmptyField() {
        return new MessageEmbed.Field("\u200e", "\u200e", false);
    }

    public static User getUserById(String userId) {
        DiscordBot instance = DiscordBot.getInstance();
        HashMap<String, User> cachedUsers = instance.getCachedUsers();
        if (cachedUsers.containsKey(userId)) return cachedUsers.get(userId);
        User user = instance.getJda().retrieveUserById(userId).complete();
        if (user != null) cachedUsers.put(userId, user);
        return user;
    }

    public static void getUserById(String userId, Consumer<User> consumer) {
        DiscordBot instance = DiscordBot.getInstance();
        HashMap<String, User> cachedUsers = instance.getCachedUsers();
        if (cachedUsers.containsKey(userId)) {
            consumer.accept(cachedUsers.get(userId));
            return;
        }
        instance.getJda().retrieveUserById(userId).queue(user -> {
            if (user != null) cachedUsers.put(userId, user);
            consumer.accept(user);
        });
    }

    public static void disableMessageComponents(Message message) {
        List<List<ActionComponent>> itemComponents = new ArrayList<>();
        for (LayoutComponent c : message.getComponents()) {
            List<ActionComponent> actionComponents = new ArrayList<>();
            for (ActionComponent a : c.getActionComponents()) {
                actionComponents.add(a.asDisabled());
            }
            itemComponents.add(actionComponents);
        }
        message.editMessageComponents(itemComponents.stream().map(ActionRow::of).toList()).queue();
    }

}

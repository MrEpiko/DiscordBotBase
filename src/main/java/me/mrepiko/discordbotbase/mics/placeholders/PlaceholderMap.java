package me.mrepiko.discordbotbase.mics.placeholders;

import com.google.gson.JsonElement;
import lombok.Getter;
import me.mrepiko.discordbotbase.DiscordBot;
import me.mrepiko.discordbotbase.commands.CommandManager;
import me.mrepiko.discordbotbase.commands.types.Command;
import me.mrepiko.discordbotbase.context.Context;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Getter
public class PlaceholderMap {

    private HashMap<String, String> map = new HashMap<>();
    private final Context ctx;

    public PlaceholderMap(Context ctx) {
        this.ctx = ctx;
        if (ctx.getGuild() != null) put("ctx_guild", ctx.getGuild());
        if (ctx.getUser() != null) put("ctx_user", ctx.getUser());
        if (ctx.getMember() != null) put("ctx_member", ctx.getMember());
        if (ctx.getMessage() != null) put("ctx_message", ctx.getMessage());
        if (ctx.getChannel() != null) put("ctx_channel", ctx.getChannel());
        registerGlobalPlaceholders();
        registerCommandPlaceholders();
    }

    public PlaceholderMap() {
        this(true, false);
    }

    public PlaceholderMap(boolean registerGlobalPlaceholders, boolean registerCommandPlaceholders) {
        this.ctx = null;
        if (registerGlobalPlaceholders) registerGlobalPlaceholders();
        if (registerCommandPlaceholders) registerCommandPlaceholders();
    }

    public PlaceholderMap(Context ctx, PlaceholderMap overrideMap) {
        this.ctx = ctx;
        this.map = overrideMap.getMap();
    }

    private void registerGlobalPlaceholders() {
        DiscordBot instance = DiscordBot.getInstance();
        for (Map.Entry<String, JsonElement> entry : instance.getConstantPlaceholders().entrySet()) {
            map.put(entry.getKey(), entry.getValue().getAsString());
        }
        map.put("bot_name", instance.getJda().getSelfUser().getName());
        map.put("bot_id", instance.getJda().getSelfUser().getId());
        map.put("current_time", LocalTime.now().format(DateTimeFormatter.ofPattern(instance.getTimeFormat())));
        map.put("current_date", LocalDate.now().format(DateTimeFormatter.ofPattern(instance.getDateFormat())));
        put("current_timestamp", System.currentTimeMillis() / 1000);
        put("current_timestamp_millis", System.currentTimeMillis());
        put("default_color", instance.getDefaultColor());
        map.put("default_name", instance.getDefaultName());
        map.put("default_icon_url", instance.getDefaultIconUrl());
        if (instance.getDeveloperChannel() != null) put("developer_channel", instance.getDeveloperChannel());
        if (instance.getDeveloperGuild() != null) put("developer_guild", instance.getDeveloperGuild());
    }

    private void registerCommandPlaceholders() {
        CommandManager commandManager = DiscordBot.getInstance().getCommandManager();
        for (Command command : commandManager.getCommands()) {
            put("command", command, true);
        }
    }

    public void put(String identifier, String input) {
        map.put(identifier, input);
    }

    public void put(String identifier, Number number) {
        map.put(identifier, String.valueOf(number));
        if (number instanceof Integer) map.put(identifier + "_f", String.format("%,d", number));
        else if (number instanceof Long) map.put(identifier + "_f", String.format("%,d", number));
        else if (number instanceof Double) map.put(identifier + "_f", String.format("%,.2f", number));
        else if (number instanceof Float) map.put(identifier + "_f", String.format("%,.2f", number));
    }

    public void put(String identifier, boolean bool) {
        map.put(identifier, String.valueOf(bool));
    }

    public void put(String identifier, Color color) {
        map.put(identifier, String.format("#%06x", color.getRGB() & 0x00FFFFFF));
    }

    public void put(String identifier, int number, boolean formatMedals) {
        if (!formatMedals) map.put(identifier, String.valueOf(number));
        switch (number) {
            default -> map.put(identifier, "#" + number);
            case 1 -> map.put(identifier, "🥇");
            case 2 -> map.put(identifier, "🥈");
            case 3 -> map.put(identifier, "🥉");
        }
    }

    public void put(String identifier, User user) {
        map.put(identifier + "_id", user.getId());
        map.put(identifier + "_name", user.getName());
        map.put(identifier + "_global_name", (user.getGlobalName() != null) ? user.getGlobalName() : "null");
        map.put(identifier + "_avatar_url", user.getAvatarUrl());
        map.put(identifier + "_mention", user.getAsMention());
        map.put(identifier + "_created_timestamp", String.valueOf(user.getTimeCreated().toEpochSecond()));
    }

    public void put(String identifier, Member member) {
        put(identifier, member, "");
    }

    public void put(String identifier, Member member, String removeFromNickname) {
        map.put(identifier + "_effective_name", (member.getNickname() != null) ? ((removeFromNickname.isEmpty()) ? member.getNickname() : member.getNickname().replace(removeFromNickname, "")) : member.getUser().getGlobalName());
        map.put(identifier + "_nickname", member.getNickname());
        map.put(identifier + "_joined_timestamp", String.valueOf(member.getTimeJoined().toEpochSecond()));
        put(identifier, member.getUser());
    }

    public void put(String identifier, MessageChannel guildChannel) {
        map.put(identifier + "_id", guildChannel.getId());
        map.put(identifier + "_name", guildChannel.getName());
        map.put(identifier + "_mention", guildChannel.getAsMention());
    }

    public void put(String identifier, Guild guild) {
        map.put(identifier + "_id", guild.getId());
        map.put(identifier + "_name", guild.getName());
        map.put(identifier + "_icon", guild.getIconUrl());
        map.put(identifier + "_banner", guild.getBannerUrl());
        map.put(identifier + "_created_timestamp", String.valueOf(guild.getTimeCreated()));
        map.put(identifier + "_vanity", (guild.getVanityUrl() != null) ? guild.getVanityUrl() : "null");
        if (guild.getOwner() != null) put(identifier + "_owner", guild.getOwner());
    }

    public void put(String identifier, Role role) {
        map.put(identifier + "_id", role.getId());
        map.put(identifier + "_name", role.getName());
        map.put(identifier + "_mention", role.getAsMention());
        map.put(identifier + "_color", (role.getColor() != null) ? role.getColor().toString() : "#000000");
    }

    public void put(String identifier, Message message) {
        put(identifier, message, "");
    }

    public void put(String identifier, Message message, String emptyContentOverride) {
        map.put(identifier + "_id", message.getId());
        map.put(identifier + "_display_content", (message.getContentDisplay().isEmpty()) ? emptyContentOverride : message.getContentDisplay());
        map.put(identifier + "_raw_content", (message.getContentRaw().isEmpty()) ? emptyContentOverride : message.getContentRaw());
        map.put(identifier + "_stripped_content", (message.getContentStripped().isEmpty()) ? emptyContentOverride : message.getContentStripped());
        map.put(identifier + "_url", message.getJumpUrl());
        map.put(identifier + "_created_timestamp", String.valueOf(message.getTimeCreated().toEpochSecond()));
        map.put(identifier + "_created_time", message.getTimeCreated().format(DateTimeFormatter.ofPattern(DiscordBot.getInstance().getTimeFormat())));
        map.put(identifier + "_created_date", message.getTimeCreated().format(DateTimeFormatter.ofPattern(DiscordBot.getInstance().getDateFormat())));
        if (message.isFromGuild()) put(identifier + "_channel", message.getGuildChannel());
        put(identifier + "_author", message.getAuthor());
    }

    public void put(String identifier, MessageEmbed messageEmbed) {
        map.put(identifier + "_title", messageEmbed.getTitle());
        map.put(identifier + "_description", messageEmbed.getDescription());
        map.put(identifier + "_image_url", (messageEmbed.getImage() == null) ? "" : messageEmbed.getImage().getUrl());
        map.put(identifier + "_thumbnail_url", (messageEmbed.getThumbnail() == null) ? "" : messageEmbed.getThumbnail().getUrl());
        map.put(identifier + "_footer_icon_url", (messageEmbed.getFooter() == null) ? "" : messageEmbed.getFooter().getIconUrl());
        map.put(identifier + "_author_icon_url", (messageEmbed.getAuthor() == null) ? "" : messageEmbed.getAuthor().getIconUrl());
        if (messageEmbed.getColor() != null) put(identifier + "_color", messageEmbed.getColor());
        else map.put(identifier + "_color", "#000000");
        map.put(identifier + "_url", messageEmbed.getUrl());
        if (messageEmbed.getAuthor() != null) {
            map.put(identifier + "_author_text", messageEmbed.getAuthor().getName());
            map.put(identifier + "_author_url", messageEmbed.getAuthor().getUrl());
        }
        map.put(identifier + "_footer_text", messageEmbed.getFooter().getText());
        if (messageEmbed.getTimestamp() != null) put(identifier + "_timestamp", messageEmbed.getTimestamp());
        else {
            map.put(identifier + "_date", "");
            map.put(identifier + "_time", "");
            put(identifier + "_seconds", 0);
            put(identifier + "_milliseconds", 0);
        }
    }

    public void put(String identifier, OffsetDateTime offsetDateTime) {
        map.put(identifier + "_date", offsetDateTime.format(DateTimeFormatter.ofPattern(DiscordBot.getInstance().getDateFormat())));
        map.put(identifier + "_time", offsetDateTime.format(DateTimeFormatter.ofPattern(DiscordBot.getInstance().getTimeFormat())));
        put(identifier + "_seconds", offsetDateTime.toInstant().getEpochSecond());
        put(identifier + "_milliseconds", offsetDateTime.toInstant().getEpochSecond() * 1000);
    }

    public void put(String identifier, Command command, boolean includeCommandName) {
        String id = command.getDiscordId();
        if (id == null && command.getParent() != null) id = command.getParent().getDiscordId();
        if (includeCommandName) {
            map.put(identifier + "_" + command.getName().toLowerCase(Locale.ROOT) + "_id", id);
            map.put(identifier + "_" + command.getName().toLowerCase(Locale.ROOT) + "_mention", "</" + command.getName().replace("_", " ") + ":" + id + ">");
            map.put(identifier + "_" + command.getName().toLowerCase(Locale.ROOT) + "_description", command.getDescription());
            return;
        }
        map.put(identifier + "_id", id);
        map.put(identifier + "_mention", "</" + command.getName().replace("_", " ") + ":" + id + ">");
        map.put(identifier + "_description", command.getDescription());
    }

    public void put(String identifier, PlaceholderMap placeholderMap) {
        for (Map.Entry<String, String> e: placeholderMap.getMap().entrySet()) {
            put(identifier + "_" + e.getKey(), e.getValue());
        }
    }

    public void put(String identifier, @Nullable Placeholderable placeholderable) {
        if (placeholderable == null) return;
        put(identifier, placeholderable.getPlaceholderMap());
    }

    public void put(String identifier, String entryIdentifier, List<? extends Placeholderable> placeholderables, String input) {
        StringBuilder output = new StringBuilder();
        for (Placeholderable o: placeholderables) {
            if (o == null) continue;
            put(entryIdentifier, o.getPlaceholderMap());
            output.append(applyPlaceholders(input));
        }
        put(identifier, output.toString());
    }

    public String applyPlaceholders(String string) {
        if (string == null) return "";
        for (Map.Entry<String, String> e: map.entrySet()) {
            string = string.replace("{" + e.getKey() + "}", (e.getValue() == null) ? "null" : e.getValue());
        }
        return string;
    }

}

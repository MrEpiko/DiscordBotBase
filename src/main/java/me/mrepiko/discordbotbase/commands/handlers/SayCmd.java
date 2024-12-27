package me.mrepiko.discordbotbase.commands.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import me.mrepiko.discordbotbase.commands.types.Command;
import me.mrepiko.discordbotbase.context.channel.GuildMessageChannelContext;
import me.mrepiko.discordbotbase.context.interaction.CommandContext;
import me.mrepiko.discordbotbase.mics.ResponseBuilder;
import me.mrepiko.discordbotbase.mics.placeholders.PlaceholderMap;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

public class SayCmd extends Command {

    public SayCmd() {
        super("say");
    }

    @Override
    public void handle(CommandContext ctx) {
        PlaceholderMap map = new PlaceholderMap(ctx);
        String query = ctx.getOptionAsString("query", "");
        String messageId = ctx.getOptionAsString("message_id");
        JsonObject jsonObject;
        map.put("query", query);
        try {
            jsonObject = new Gson().fromJson(query, JsonObject.class);
        } catch (JsonSyntaxException ex) {
            ResponseBuilder.build(map, getJsonObject("error_response")).send();
            return;
        }

        GuildMessageChannelContext channelCtx = new GuildMessageChannelContext((GuildMessageChannel) ctx.getChannel());
        try {
            PlaceholderMap map1 = new PlaceholderMap(channelCtx, map);
            if (!messageId.isEmpty()) ctx.getChannel().retrieveMessageById(messageId).queue(message -> ResponseBuilder.build(map1, jsonObject).setMessageToBeEdited(message).send(), throwable -> {
                ResponseBuilder.build(map, getJsonObject("invalid_message_response")).send();
            });
            else ResponseBuilder.build(map1, jsonObject).send();
        } catch (Exception e) {
            ResponseBuilder.build(map, getJsonObject("error_response")).send();
            return;
        }
        ResponseBuilder.build(map, getJsonObject("response")).send();
    }

    @Override
    public void setupDefaultConfigurations() {
        expectResponseObject("response");
        expectResponseObject("error_response");
        expectResponseObject("invalid_message_response");
    }

}

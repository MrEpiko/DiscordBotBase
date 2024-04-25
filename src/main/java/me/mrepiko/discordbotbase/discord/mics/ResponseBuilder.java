package me.mrepiko.discordbotbase.discord.mics;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import kotlin.Pair;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.mrepiko.discordbotbase.discord.DiscordBot;
import me.mrepiko.discordbotbase.discord.components.RuntimeComponent;
import me.mrepiko.discordbotbase.discord.components.general.BasicComponentHandler;
import me.mrepiko.discordbotbase.discord.components.general.ComponentHandler;
import me.mrepiko.discordbotbase.discord.components.general.types.ModalHandler;
import me.mrepiko.discordbotbase.discord.context.ChannelContext;
import me.mrepiko.discordbotbase.discord.context.InteractionContext;
import me.mrepiko.discordbotbase.discord.mics.utils.ComponentUtils;
import me.mrepiko.discordbotbase.discord.mics.utils.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.callbacks.IModalCallback;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.requests.restaction.MessageEditAction;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageEditAction;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import net.dv8tion.jda.api.utils.FileUpload;
import org.jetbrains.annotations.Nullable;

import javax.annotation.CheckReturnValue;
import java.awt.*;
import java.io.File;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Getter
@RequiredArgsConstructor
public class ResponseBuilder {

    private final PlaceholderMap map;
    private final JsonObject responseObject;
    private List<File> files = new ArrayList<>();
    private JsonObject componentBonus;
    private HashMap<Predicate<InteractionContext>, Pair<Consumer<InteractionContext>, Consumer<InteractionContext>>> predicates = new HashMap<>();
    private Message messageToBeEdited;

    @CheckReturnValue
    public static ResponseBuilder build(PlaceholderMap map, JsonObject responseObject) {
        return new ResponseBuilder(map, responseObject);
    }

    @CheckReturnValue
    public ResponseBuilder setFiles(List<File> files) {
        this.files = files;
        return this;
    }

    @CheckReturnValue
    public ResponseBuilder setComponentBonus(JsonObject componentBonus) {
        this.componentBonus = componentBonus;
        return this;
    }

    @CheckReturnValue
    public ResponseBuilder setPredicates(HashMap<Predicate<InteractionContext>, Pair<Consumer<InteractionContext>, Consumer<InteractionContext>>> predicates) {
        this.predicates = predicates;
        return this;
    }

    @CheckReturnValue
    public ResponseBuilder setMessageToBeEdited(Message messageToBeEdited) {
        this.messageToBeEdited = messageToBeEdited;
        return this;
    }

    @CheckReturnValue
    public ResponseBuilder addFile(File file) {
        files.add(file);
        return this;
    }

    @CheckReturnValue
    public ResponseBuilder addPredicate(Predicate<InteractionContext> predicate, @Nullable Consumer<InteractionContext> successConsumer, @Nullable Consumer<InteractionContext> failureConsumer) {
        predicates.put(predicate, new Pair<>(successConsumer, failureConsumer));
        return this;
    }

    public void send() {
        send(null);
    }

    public void send(Consumer<Message> postMessageAction) {
        if (map.getCtx() instanceof InteractionContext) interactionContextSend(postMessageAction);
        else if (map.getCtx() instanceof ChannelContext) channelContextSend(postMessageAction);
    }

    private void interactionContextSend(Consumer<Message> postMessageAction) {
        if (messageToBeEdited != null) {
            editMessage(postMessageAction);
            return;
        }

        InteractionContext ctx = (InteractionContext) map.getCtx();
        Modal modal = getModal();
        if (modal != null && ctx.getCallback() != null && (ctx.getCallback() instanceof IModalCallback iModalCallback)) {
            iModalCallback.replyModal(modal).queue();
            DiscordBot.getInstance().getComponentManager().addModal(modal, ctx.getCallback().getId());
            return;
        }

        boolean ephemeral = responseObject.has("ephemeral") && responseObject.get("ephemeral").getAsBoolean();
        boolean pin = responseObject.has("pin") && responseObject.get("pin").getAsBoolean();
        int deleteAfter = (responseObject.has("delete_after")) ? responseObject.get("delete_after").getAsInt() : 0;
        String messageContent = getMessage();
        EmbedBuilder embedBuilder = getEmbed();
        List<Emoji> reactions = getReactions();

        List<RuntimeComponent> components = getRuntimeComponents();
        components.forEach(c -> DiscordBot.getInstance().getComponentManager().addRuntimeComponent(c));
        List<ActionRow> actionRows = sortIntoActionRows(components);

        if (ctx.getCallback().isAcknowledged()) {
            WebhookMessageEditAction<Message> webhookMessageEditAction = ctx.getCallback().getHook().editOriginal(messageContent);
            if (embedBuilder != null) webhookMessageEditAction.setEmbeds(embedBuilder.build());
            if (!actionRows.isEmpty()) webhookMessageEditAction.setComponents(actionRows);
            if (!files.isEmpty()) webhookMessageEditAction.setFiles(files.stream().map(FileUpload::fromData).toList());
            webhookMessageEditAction.queue(message -> {
                if (postMessageAction != null) postMessageAction.accept(message);
                if (pin && !message.isEphemeral()) message.pin().queue();
                if (deleteAfter > 0) {
                    if (message.isEphemeral()) ctx.getCallback().getHook().deleteOriginal().queueAfter(deleteAfter, TimeUnit.SECONDS);
                    else message.delete().queueAfter(deleteAfter, TimeUnit.SECONDS);
                }
                for (RuntimeComponent c: components) {
                    c.setMessageId(message.getId());
                    c.applyTimeout(message);
                }
                if (!message.isEphemeral()) for (Emoji e: reactions) message.addReaction(Emoji.fromUnicode(Utils.adaptEmoji(e.getFormatted()))).queue();
            });
        } else {
            ReplyCallbackAction replyCallbackAction = ctx.getCallback().reply(messageContent).setEphemeral(ephemeral);
            if (embedBuilder != null) replyCallbackAction.setEmbeds(embedBuilder.build());
            if (!actionRows.isEmpty()) replyCallbackAction.setComponents(actionRows);
            if (!files.isEmpty()) replyCallbackAction.addFiles(files.stream().map(FileUpload::fromData).toList());
            replyCallbackAction.queue(interactionHook -> interactionHook.retrieveOriginal().queue(message -> {
                if (postMessageAction != null) postMessageAction.accept(message);
                if (pin && !message.isEphemeral()) message.pin().queue();
                if (deleteAfter > 0) {
                    if (message.isEphemeral()) ctx.getCallback().getHook().deleteOriginal().queueAfter(deleteAfter, TimeUnit.SECONDS);
                    else message.delete().queueAfter(deleteAfter, TimeUnit.SECONDS);
                }
                for (RuntimeComponent c: components) {
                    c.setMessageId(message.getId());
                    c.applyTimeout(message);
                }
                if (!message.isEphemeral()) for (Emoji e: reactions) message.addReaction(Emoji.fromUnicode(Utils.adaptEmoji(e.getFormatted()))).queue();
            }));
        }
    }

    private void channelContextSend(Consumer<Message> postMessageAction) {
        if (messageToBeEdited != null) {
            editMessage(postMessageAction);
            return;
        }

        ChannelContext ctx = (ChannelContext) map.getCtx();
        if (ctx.getChannel() == null) return;
        boolean pin = responseObject.has("pin") && responseObject.get("pin").getAsBoolean();
        int deleteAfter = (responseObject.has("delete_after")) ? responseObject.get("delete_after").getAsInt() : 0;
        String messageContent = getMessage();
        EmbedBuilder embedBuilder = getEmbed();
        List<Emoji> reactions = getReactions();

        List<RuntimeComponent> components = getRuntimeComponents();
        components.forEach(c -> DiscordBot.getInstance().getComponentManager().addRuntimeComponent(c));
        List<ActionRow> actionRows = sortIntoActionRows(components);

        MessageCreateAction messageCreateAction = ctx.getChannel().sendMessage(messageContent);
        if (embedBuilder != null) messageCreateAction.setEmbeds(embedBuilder.build());
        if (!actionRows.isEmpty()) messageCreateAction.setComponents(actionRows);
        if (!files.isEmpty()) messageCreateAction.setFiles(files.stream().map(FileUpload::fromData).toList());
        messageCreateAction.queue(message -> {
            if (postMessageAction != null) postMessageAction.accept(message);
            if (pin) message.pin().queue();
            if (deleteAfter > 0) message.delete().queueAfter(deleteAfter, TimeUnit.SECONDS);
            for (RuntimeComponent c: components) {
                c.setMessageId(message.getId());
                c.applyTimeout(message);
            }
            for (Emoji e: reactions) message.addReaction(Emoji.fromUnicode(Utils.adaptEmoji(e.getFormatted()))).queue();
        });
    }

    private void editMessage(Consumer<Message> postMessageAction) {
        if (messageToBeEdited.isEphemeral()) return;
        boolean pin = responseObject.has("pin") && responseObject.get("pin").getAsBoolean();
        int deleteAfter = (responseObject.has("delete_after")) ? responseObject.get("delete_after").getAsInt() : 0;
        String messageContent = getMessage();
        EmbedBuilder embedBuilder = getEmbed();
        List<Emoji> reactions = getReactions();

        List<RuntimeComponent> components = getRuntimeComponents();
        components.forEach(c -> DiscordBot.getInstance().getComponentManager().addRuntimeComponent(c));
        List<ActionRow> actionRows = sortIntoActionRows(components);

        MessageEditAction messageEditAction = messageToBeEdited.editMessage(messageContent);
        if (embedBuilder != null) messageEditAction.setEmbeds(embedBuilder.build());
        if (!actionRows.isEmpty()) messageEditAction.setComponents(actionRows);
        if (!files.isEmpty()) messageEditAction.setFiles(files.stream().map(FileUpload::fromData).toList());
        messageEditAction.queue(message -> {
            if (postMessageAction != null) postMessageAction.accept(message);
            if (pin) message.pin().queue();
            if (deleteAfter > 0) message.delete().queueAfter(deleteAfter, TimeUnit.SECONDS);
            for (RuntimeComponent c: components) {
                c.setMessageId(message.getId());
                c.applyTimeout(message);
            }
            for (Emoji e: reactions) message.addReaction(Emoji.fromUnicode(Utils.adaptEmoji(e.getFormatted()))).queue();
        });
    }

    private String getMessage() {
        if (!responseObject.has("message")) return "";
        String message = DiscordBot.getInstance().applyPlaceholders(map, responseObject.get("message").getAsString());
        if (message.length() > 2000) message = message.substring(0, 1996) + DiscordBot.getInstance().getTruncationIndicator();
        return message;
    }

    @Nullable
    private EmbedBuilder getEmbed() {
        if (!responseObject.has("description") && !responseObject.has("title")) return null;
        DiscordBot instance = DiscordBot.getInstance();
        String indicator = instance.getTruncationIndicator();
        String title = (responseObject.has("title")) ? instance.applyPlaceholders(map, responseObject.get("title").getAsString()) : "";
        String titleUrl = (responseObject.has("title_url")) ? instance.applyPlaceholders(map, responseObject.get("title_url").getAsString()) : "";
        String description = (responseObject.has("description")) ? instance.applyPlaceholders(map, responseObject.get("description").getAsString()) : "";
        String footerText = (responseObject.has("footer_text")) ? instance.applyPlaceholders(map, responseObject.get("footer_text").getAsString()) : "";
        String authorText = (responseObject.has("author_text")) ? instance.applyPlaceholders(map, responseObject.get("author_text").getAsString()) : "";
        String footerIconUrl = (responseObject.has("footer_icon_url")) ? instance.applyPlaceholders(map, responseObject.get("footer_icon_url").getAsString()) : "";
        String authorIconUrl = (responseObject.has("author_icon_url")) ? instance.applyPlaceholders(map, responseObject.get("author_icon_url").getAsString()) : "";
        String authorUrl = (responseObject.has("author_url")) ? instance.applyPlaceholders(map, responseObject.get("author_url").getAsString()) : "";
        String timestamp = (responseObject.has("timestamp")) ? instance.applyPlaceholders(map, responseObject.get("timestamp").getAsString()) : "";
        String thumbnailUrl = (responseObject.has("thumbnail_url")) ? instance.applyPlaceholders(map, responseObject.get("thumbnail_url").getAsString()) : "";
        String imageUrl = (responseObject.has("image_url")) ? instance.applyPlaceholders(map, responseObject.get("image_url").getAsString()) : "";
        String color = (responseObject.has("color")) ? instance.applyPlaceholders(map, responseObject.get("color").getAsString()) : "";
        JsonArray fields = (responseObject.has("fields")) ? responseObject.get("fields").getAsJsonArray() : new JsonArray();

        EmbedBuilder embedBuilder = new EmbedBuilder();
        if (!title.isEmpty()) {
            if (title.length() > 256) title = title.substring(0, 252) + indicator;
            if (titleUrl.length() > 2000) titleUrl = "";
            if (titleUrl.isEmpty()) embedBuilder.setTitle(title);
            else embedBuilder.setTitle(title, titleUrl);
        }
        if (!description.isEmpty()) {
            if (description.length() > 4096) description = description.substring(0, 4092) + indicator;
            embedBuilder.setDescription(description);
        }
        if (!footerText.isEmpty()) {
            if (footerText.length() > 2048) footerText = footerIconUrl.substring(0, 2044) + indicator;
            if (footerIconUrl.length() > 2000) footerIconUrl = "";
            if (footerIconUrl.isEmpty()) embedBuilder.setFooter(footerText);
            else embedBuilder.setFooter(footerText, footerIconUrl);
        }
        if (!authorText.isEmpty()) {
            if (authorText.length() > 256) authorText = authorUrl.substring(0, 252) + indicator;
            if (authorUrl.length() > 2000) authorUrl = "";
            if (authorIconUrl.length() > 2000) authorIconUrl = "";
            if (authorUrl.isEmpty()) {
                if (authorIconUrl.isEmpty()) embedBuilder.setAuthor(authorText);
                else embedBuilder.setAuthor(authorText, authorIconUrl, authorIconUrl);
            } else {
                if (authorIconUrl.isEmpty()) embedBuilder.setAuthor(authorText, authorUrl);
                else embedBuilder.setAuthor(authorText, authorUrl, authorIconUrl);
            }
        }
        if (!timestamp.isEmpty()) embedBuilder.setTimestamp(LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.decode(timestamp)), ZoneId.systemDefault()));
        if (!thumbnailUrl.isEmpty()) {
            if (thumbnailUrl.length() > 2000) thumbnailUrl = "";
            embedBuilder.setThumbnail(thumbnailUrl);
        }
        if (!imageUrl.isEmpty()) {
            if (imageUrl.length() > 2000) imageUrl = "";
            embedBuilder.setImage(imageUrl);
        }
        if (!color.isEmpty()) embedBuilder.setColor(Color.decode(color));
        for (JsonElement e: fields) {
            JsonObject o = e.getAsJsonObject();
            boolean inline = o.has("inline") && o.get("inline").getAsBoolean();
            if (o.has("blank") && o.get("blank").getAsBoolean()) {
                embedBuilder.addBlankField(inline);
                continue;
            }
            String name = instance.applyPlaceholders(map, o.get("name").getAsString());
            String value = instance.applyPlaceholders(map, o.get("value").getAsString());
            if (name.length() > 256) name = name.substring(0, 252) + indicator;
            if (value.length() > 1024) value = value.substring(0, 1020) + indicator;
            embedBuilder.addField(name, value, inline);
        }

        return embedBuilder;
    }

    private List<Emoji> getReactions() {
        List<Emoji> reactions = new ArrayList<>();
        if (!responseObject.has("reactions")) return reactions;
        for (JsonElement e: responseObject.get("reactions").getAsJsonArray()) reactions.add(Emoji.fromUnicode(e.getAsString()));
        return reactions;
    }

    private List<RuntimeComponent> getRuntimeComponents() {
        List<RuntimeComponent> runtimeComponents = new ArrayList<>();
        if (!responseObject.has("components")) return runtimeComponents;
        for (JsonElement e: responseObject.get("components").getAsJsonArray()) {
            boolean requiresOverriding = false;
            boolean requiresAppearanceOverriding = false;
            boolean requiresRowOverriding = false;
            if (e.isJsonObject()) {
                JsonObject o = e.getAsJsonObject();
                requiresOverriding = true;
                if (o.size() == 2 && o.has("name") && o.has("row_index")) requiresRowOverriding = true;
                else requiresAppearanceOverriding = true;
            }
            String componentName = (requiresOverriding) ? e.getAsJsonObject().get("name").getAsString() : e.getAsString();
            ComponentHandler componentHandler = DiscordBot.getInstance().getComponentManager().getComponentHandler(componentName);
            if (!(componentHandler instanceof BasicComponentHandler basicComponentHandler)) continue;
            RuntimeComponent runtimeComponent = new RuntimeComponent(
                    basicComponentHandler,
                    componentBonus,
                    (requiresAppearanceOverriding) ? ComponentUtils.createComponent(componentName, e.getAsJsonObject()) : null,
                    (requiresRowOverriding) ? e.getAsJsonObject().get("row_index").getAsInt() : basicComponentHandler.getRowIndex(),
                    predicates
            );
            runtimeComponents.add(runtimeComponent);
        }
        return runtimeComponents;
    }

    @Nullable
    private Modal getModal() {
        if (!responseObject.has("modal")) return null;
        boolean requiresOverriding = responseObject.get("modal").isJsonObject();
        String modalName = (requiresOverriding) ? responseObject.get("modal").getAsJsonObject().get("name").getAsString() : responseObject.get("modal").getAsString();
        ComponentHandler componentHandler = DiscordBot.getInstance().getComponentManager().getComponentHandler(modalName);
        if (!(componentHandler instanceof ModalHandler modalHandler)) return null;
        return (requiresOverriding) ? ComponentUtils.createModal(modalName, responseObject.get("modal").getAsJsonObject()) : modalHandler.getModal();
    }

    private List<ActionRow> sortIntoActionRows(List<RuntimeComponent> components) {
        if (components.isEmpty()) return new ArrayList<>();
        HashMap<Integer, List<RuntimeComponent>> sortedComponents = new HashMap<>();
        for (int i = 0; i < 5; i++) sortedComponents.put(i, new ArrayList<>());
        for (RuntimeComponent c: components) {
            List<RuntimeComponent> previous = new ArrayList<>(sortedComponents.getOrDefault(c.getRowIndex(), new ArrayList<>()));
            previous.add(c);
            sortedComponents.put(c.getRowIndex(), previous);
        }
        return sortedComponents.values().stream().filter(x -> !x.isEmpty()).map(x -> ActionRow.of(x.stream().map(RuntimeComponent::getComponent).toList())).toList();
    }

}

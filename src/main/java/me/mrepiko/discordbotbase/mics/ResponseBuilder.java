package me.mrepiko.discordbotbase.mics;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.mrepiko.discordbotbase.DiscordBot;
import me.mrepiko.discordbotbase.components.ComponentHandlerOverrides;
import me.mrepiko.discordbotbase.components.RuntimeComponent;
import me.mrepiko.discordbotbase.components.general.BasicComponentHandler;
import me.mrepiko.discordbotbase.components.general.ComponentHandler;
import me.mrepiko.discordbotbase.components.general.types.DropdownHandler;
import me.mrepiko.discordbotbase.components.general.types.ModalHandler;
import me.mrepiko.discordbotbase.context.InteractionContext;
import me.mrepiko.discordbotbase.context.MessageChannelContext;
import me.mrepiko.discordbotbase.mics.placeholders.PlaceholderMap;
import me.mrepiko.discordbotbase.mics.utils.ComponentUtils;
import me.mrepiko.discordbotbase.mics.utils.DiscordUtils;
import me.mrepiko.discordbotbase.mics.utils.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.interactions.callbacks.IModalCallback;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.requests.restaction.MessageEditAction;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageEditAction;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import net.dv8tion.jda.api.utils.FileUpload;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.CheckReturnValue;
import java.awt.*;
import java.io.File;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Getter
@RequiredArgsConstructor
public class ResponseBuilder {

    private final static Logger LOGGER = LoggerFactory.getLogger(ResponseBuilder.class);

    private final PlaceholderMap map;
    private final JsonObject responseObject;
    private List<File> files = new ArrayList<>();
    private final HashMap<String, List<ComponentUtils.DropdownOption>> injectedDropdownOptions = new HashMap<>();
    private final Set<String> dropdownsContainingInjectedOptions = new HashSet<>();
    private final List<MessageEmbed.Field> injectedFields = new ArrayList<>();
    private JsonObject componentBonus;
    private Consumer<InteractionContext> componentConsumer;
    private Consumer<Message> postMessageAction;
    private Consumer<Throwable> throwableConsumer;
    private Message messageToBeEdited;
    private int componentsLimit;

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
    public ResponseBuilder setComponentsLimit(int componentsLimit) {
        this.componentsLimit = componentsLimit;
        return this;
    }

    @CheckReturnValue
    public ResponseBuilder setComponentBonus(JsonObject componentBonus) {
        this.componentBonus = componentBonus;
        return this;
    }

    @CheckReturnValue
    public ResponseBuilder setComponentConsumer(Consumer<InteractionContext> componentConsumer) {
        this.componentConsumer = componentConsumer;
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
    public ResponseBuilder injectDropdownOptions(String dropdownHandlerName, List<ComponentUtils.DropdownOption> options) {
        dropdownsContainingInjectedOptions.add(dropdownHandlerName);
        if (options.isEmpty()) return this;
        if (options.size() > 25) options = options.subList(0, 25);
        ComponentHandler componentHandler = DiscordBot.getInstance().getComponentManager().getComponentHandler(dropdownHandlerName);
        if (!(componentHandler instanceof DropdownHandler)) {
            LOGGER.error("Component {} is not a dropdown handler", dropdownHandlerName);
            return this;
        }
        injectedDropdownOptions.put(dropdownHandlerName, options);
        return this;
    }

    @CheckReturnValue
    public ResponseBuilder injectEmbedFields(List<MessageEmbed.Field> fields) {
        injectedFields.addAll(fields);
        return this;
    }

    public void send() {
        send(null, null);
    }

    public void send(Consumer<Message> postMessageAction) {
        send(postMessageAction, null);
    }

    public void send(Consumer<Message> postMessageAction, Consumer<Throwable> throwableConsumer) {
        this.postMessageAction = postMessageAction;
        this.throwableConsumer = throwableConsumer;
        if (map.getCtx() instanceof InteractionContext) interactionContextSend();
        else if (map.getCtx() instanceof MessageChannelContext) channelContextSend();
    }

    private void interactionContextSend() {
        if (messageToBeEdited != null) {
            editMessage();
            return;
        }

        InteractionContext ctx = (InteractionContext) map.getCtx();
        Modal modal = getModal();
        if (modal != null && ctx.getCallback() != null && (ctx.getCallback() instanceof IModalCallback iModalCallback)) {
            iModalCallback.replyModal(modal).queue();
            DiscordBot.getInstance().getComponentManager().addModal(modal, (ctx.getMessage() == null) ? "" : ctx.getMessage().getId());
            return;
        }

        boolean ephemeral = responseObject.has("ephemeral") && responseObject.get("ephemeral").getAsBoolean();
        boolean pin = responseObject.has("pin") && responseObject.get("pin").getAsBoolean();
        boolean crosspost = responseObject.has("crosspost") && responseObject.get("crosspost").getAsBoolean();
        int deleteAfter = (responseObject.has("delete_after")) ? responseObject.get("delete_after").getAsInt() : 0;
        String messageContent = getMessage();
        EmbedBuilder embedBuilder = getEmbed();
        if (messageContent.isEmpty() && embedBuilder == null) return;
        List<Emoji> reactions = getReactions();

        List<RuntimeComponent> components = getRuntimeComponents();
        components.forEach(c -> DiscordBot.getInstance().getComponentManager().addRuntimeComponent(c));
        List<ActionRow> actionRows = sortIntoActionRows(components);

        if (ctx.getCallback().isAcknowledged()) {
            WebhookMessageEditAction<Message> webhookMessageEditAction = ctx.getCallback().getHook().editOriginal(messageContent);
            if (embedBuilder != null) webhookMessageEditAction.setEmbeds(embedBuilder.build());
            if (!actionRows.isEmpty()) webhookMessageEditAction.setComponents(actionRows);
            if (!files.isEmpty()) {
                webhookMessageEditAction.setFiles(files.stream().filter(Objects::nonNull).map(x -> FileUpload.fromData(x, x.getName())).toList());
            }

            webhookMessageEditAction.queue(message -> {
                if (postMessageAction != null) postMessageAction.accept(message);
                if (pin && !message.isEphemeral()) message.pin().queue();
                if (deleteAfter > 0) {
                    if (message.isEphemeral()) ctx.getCallback().getHook().deleteOriginal().queueAfter(deleteAfter, TimeUnit.SECONDS);
                    else message.delete().queueAfter(deleteAfter, TimeUnit.SECONDS);
                }
                if (crosspost) message.crosspost().queue();
                for (RuntimeComponent c: components) {
                    c.setMessageId(message.getId());
                    c.applyTimeout(message);
                }
                if (!message.isEphemeral()) for (Emoji e: reactions) message.addReaction(Emoji.fromUnicode(Utils.adaptEmoji(e.getFormatted()))).queue();
            }, throwable -> {
                if (throwableConsumer != null) throwableConsumer.accept(throwable);
                else LOGGER.error("Error while sending response", throwable);
            });
        } else {
            ReplyCallbackAction replyCallbackAction = ctx.getCallback().reply(messageContent).setEphemeral(ephemeral);
            if (embedBuilder != null) replyCallbackAction.setEmbeds(embedBuilder.build());
            if (!actionRows.isEmpty()) replyCallbackAction.setComponents(actionRows);
            if (!files.isEmpty()) replyCallbackAction.setFiles(files.stream().filter(Objects::nonNull).map(x -> FileUpload.fromData(x, x.getName())).toList());

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
            }, throwable -> {
                if (throwableConsumer != null) throwableConsumer.accept(throwable);
                else LOGGER.error("Error while sending response", throwable);
            }));
        }
    }

    private void channelContextSend() {
        if (messageToBeEdited != null) {
            editMessage();
            return;
        }

        MessageChannelContext ctx = (MessageChannelContext) map.getCtx();
        if (ctx.getChannel() == null) return;
        boolean pin = responseObject.has("pin") && responseObject.get("pin").getAsBoolean();
        int deleteAfter = (responseObject.has("delete_after")) ? responseObject.get("delete_after").getAsInt() : 0;
        boolean crosspost = responseObject.has("crosspost") && responseObject.get("crosspost").getAsBoolean();
        String messageContent = getMessage();
        EmbedBuilder embedBuilder = getEmbed();
        if (messageContent.isEmpty() && embedBuilder == null) return;
        List<Emoji> reactions = getReactions();

        List<RuntimeComponent> components = getRuntimeComponents();
        components.forEach(c -> DiscordBot.getInstance().getComponentManager().addRuntimeComponent(c));
        List<ActionRow> actionRows = sortIntoActionRows(components);

        MessageCreateAction messageCreateAction;
        try {
            messageCreateAction = ctx.getChannel().sendMessage(messageContent);
        } catch (InsufficientPermissionException ignored) {
            return;
        }
        if (embedBuilder != null) messageCreateAction.setEmbeds(embedBuilder.build());
        if (!actionRows.isEmpty()) messageCreateAction.setComponents(actionRows);
        if (!files.isEmpty()) messageCreateAction.setFiles(files.stream().filter(Objects::nonNull).map(x -> FileUpload.fromData(x, x.getName())).toList());

        messageCreateAction.queue(message -> {
            if (postMessageAction != null) postMessageAction.accept(message);
            if (pin) message.pin().queue();
            if (deleteAfter > 0) message.delete().queueAfter(deleteAfter, TimeUnit.SECONDS);
            if (crosspost) message.crosspost().queue();
            for (RuntimeComponent c: components) {
                c.setMessageId(message.getId());
                c.applyTimeout(message);
            }
            for (Emoji e: reactions) message.addReaction(Emoji.fromUnicode(Utils.adaptEmoji(e.getFormatted()))).queue();
        }, throwable -> {
            if (throwableConsumer != null) throwableConsumer.accept(throwable);
            else LOGGER.error("Error while sending response", throwable);
        });
    }

    private void editMessage() {
        if (messageToBeEdited.isEphemeral()) return;
        map.put("old_message", messageToBeEdited);
        if (!messageToBeEdited.getEmbeds().isEmpty()) map.put("old_message_embed", messageToBeEdited.getEmbeds().get(0));

        boolean pin = responseObject.has("pin") && responseObject.get("pin").getAsBoolean();
        int deleteAfter = (responseObject.has("delete_after")) ? responseObject.get("delete_after").getAsInt() : 0;
        boolean crosspost = responseObject.has("crosspost") && responseObject.get("crosspost").getAsBoolean();
        boolean clearMessageContent = !responseObject.has("clear_old_message_content") || responseObject.get("clear_old_message_content").getAsBoolean();
        boolean clearEmbed = !responseObject.has("clear_old_embed") || responseObject.get("clear_old_embed").getAsBoolean();
        boolean clearFiles = !responseObject.has("clear_old_files") || responseObject.get("clear_old_files").getAsBoolean();
        boolean clearComponents = !responseObject.has("clear_old_components") || responseObject.get("clear_old_components").getAsBoolean();
        boolean clearReactions = responseObject.has("clear_reactions") && responseObject.get("clear_reactions").getAsBoolean();

        String messageContent = getMessage();
        EmbedBuilder embedBuilder = getEmbed();
        if (messageContent.isEmpty() && embedBuilder == null) return;
        List<Emoji> reactions = getReactions();

        List<RuntimeComponent> components = getRuntimeComponents();
        components.forEach(c -> DiscordBot.getInstance().getComponentManager().addRuntimeComponent(c));
        List<ActionRow> actionRows = sortIntoActionRows(components);

        MessageEditAction messageEditAction = messageToBeEdited.editMessage(messageContent);
        if (messageContent.isEmpty() && !clearMessageContent) messageEditAction.setContent(messageToBeEdited.getContentRaw());
        else if (messageContent.isEmpty()) messageEditAction.setContent("");
        if (embedBuilder != null) messageEditAction.setEmbeds(embedBuilder.build());
        else if (!clearEmbed) messageEditAction.setEmbeds(messageToBeEdited.getEmbeds());
        else messageEditAction.setEmbeds(new ArrayList<>());
        if (!actionRows.isEmpty()) messageEditAction.setComponents(actionRows);
        else if (!clearComponents) messageEditAction.setComponents(messageToBeEdited.getComponents());
        else messageEditAction.setComponents(new ArrayList<>());
        if (!files.isEmpty()) messageEditAction.setFiles(files.stream().filter(Objects::nonNull).map(x -> FileUpload.fromData(x, x.getName())).toList());
        else if (!clearFiles) messageEditAction.setContent(messageToBeEdited.getContentRaw());
        else messageEditAction.setFiles(new ArrayList<>());

        messageEditAction.queue(message -> {
            if (postMessageAction != null) postMessageAction.accept(message);
            if (pin) message.pin().queue();
            if (deleteAfter > 0) message.delete().queueAfter(deleteAfter, TimeUnit.SECONDS);
            if (crosspost) message.crosspost().queue();
            for (RuntimeComponent c: components) {
                c.setMessageId(message.getId());
                c.applyTimeout(message);
            }
            if (clearReactions) message.clearReactions().queue(x -> {
                for (Emoji e: reactions) message.addReaction(Emoji.fromUnicode(Utils.adaptEmoji(e.getFormatted()))).queue();
            });
        }, throwable -> {
            if (throwableConsumer != null) throwableConsumer.accept(throwable);
            else LOGGER.error("Error while sending response", throwable);
        });
    }

    private String getMessage() {
        if (!responseObject.has("message")) return "";
        String message = map.applyPlaceholders(responseObject.get("message").getAsString());
        if (message.length() > 2000) message = message.substring(0, 1996) + "...";
        return message;
    }

    @Nullable
    private EmbedBuilder getEmbed() {
        if (!responseObject.has("description") && !responseObject.has("title")) return null;
        String indicator = "...";
        String title = (responseObject.has("title")) ? map.applyPlaceholders(responseObject.get("title").getAsString()) : "";
        String titleUrl = (responseObject.has("title_url")) ? map.applyPlaceholders(responseObject.get("title_url").getAsString()) : "";
        String description = (responseObject.has("description")) ? map.applyPlaceholders(responseObject.get("description").getAsString()) : "";
        String footerText = (responseObject.has("footer_text")) ? map.applyPlaceholders(responseObject.get("footer_text").getAsString()) : "";
        String authorText = (responseObject.has("author_text")) ? map.applyPlaceholders(responseObject.get("author_text").getAsString()) : "";
        String footerIconUrl = (responseObject.has("footer_icon_url")) ? map.applyPlaceholders(responseObject.get("footer_icon_url").getAsString()) : "";
        String authorIconUrl = (responseObject.has("author_icon_url")) ? map.applyPlaceholders(responseObject.get("author_icon_url").getAsString()) : "";
        String authorUrl = (responseObject.has("author_url")) ? map.applyPlaceholders(responseObject.get("author_url").getAsString()) : "";
        String timestamp = (responseObject.has("timestamp")) ? map.applyPlaceholders(responseObject.get("timestamp").getAsString()) : "";
        String thumbnailUrl = (responseObject.has("thumbnail_url")) ? map.applyPlaceholders(responseObject.get("thumbnail_url").getAsString()) : "";
        String imageUrl = (responseObject.has("image_url")) ? map.applyPlaceholders(responseObject.get("image_url").getAsString()) : "";
        String color = (responseObject.has("color")) ? map.applyPlaceholders(responseObject.get("color").getAsString()) : "";
        JsonArray fields = (responseObject.has("fields")) ? responseObject.get("fields").getAsJsonArray() : new JsonArray();
        boolean includeInjectedFields = !responseObject.has("include_injected_fields") || responseObject.get("include_injected_fields").getAsBoolean();

        EmbedBuilder embedBuilder = new EmbedBuilder();
        if (!title.isEmpty()) {
            if (title.length() > 256) title = title.substring(0, 252) + indicator;
            if (titleUrl.length() > 2000) titleUrl = "";
            if (titleUrl.isEmpty()) embedBuilder.setTitle(title);
            else if (Utils.isUrl(titleUrl)) embedBuilder.setTitle(title, titleUrl);
        }
        if (!description.isEmpty()) {
            if (description.length() > 4096) description = description.substring(0, 4092) + indicator;
            embedBuilder.setDescription(description);
        }
        if (!footerText.isEmpty()) {
            if (footerText.length() > 2048) footerText = footerIconUrl.substring(0, 2044) + indicator;
            if (footerIconUrl.length() > 2000) footerIconUrl = "";
            if (footerIconUrl.isEmpty()) embedBuilder.setFooter(footerText);
            else if (Utils.isUrl(footerIconUrl) || Utils.isAttachmentUri(footerIconUrl)) embedBuilder.setFooter(footerText, footerIconUrl);
        }
        if (!authorText.isEmpty()) {
            if (authorText.length() > 256) authorText = authorUrl.substring(0, 252) + indicator;
            if (authorUrl.length() > 2000) authorUrl = "";
            if (authorIconUrl.length() > 2000) authorIconUrl = "";
            if (authorUrl.isEmpty()) {
                if (authorIconUrl.isEmpty()) embedBuilder.setAuthor(authorText);
                else if (Utils.isUrl(authorIconUrl) || Utils.isAttachmentUri(authorIconUrl)) embedBuilder.setAuthor(authorText, authorIconUrl, authorIconUrl);
            } else {
                if (authorIconUrl.isEmpty()) embedBuilder.setAuthor(authorText, authorUrl);
                else if (Utils.isUrl(authorIconUrl) || Utils.isAttachmentUri(authorIconUrl)) embedBuilder.setAuthor(authorText, authorUrl, authorIconUrl);
            }
        }
        if (!timestamp.isEmpty()) embedBuilder.setTimestamp(LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.decode(timestamp)), ZoneId.systemDefault()));
        if (!thumbnailUrl.isEmpty()) {
            if (thumbnailUrl.length() > 2000) thumbnailUrl = "";
            if (Utils.isUrl(thumbnailUrl) || Utils.isAttachmentUri(thumbnailUrl)) embedBuilder.setThumbnail(thumbnailUrl);
        }
        if (!imageUrl.isEmpty()) {
            if (imageUrl.length() > 2000) imageUrl = "";
            if (Utils.isUrl(imageUrl) || Utils.isAttachmentUri(imageUrl)) embedBuilder.setImage(imageUrl);
        }
        if (!color.isEmpty()) embedBuilder.setColor(Color.decode(color));

        if (includeInjectedFields) {
            if (responseObject.has("inject_blank_fields")) {
                for (JsonElement blankField : responseObject.get("inject_blank_fields").getAsJsonArray()) {
                    JsonObject fieldObject = blankField.getAsJsonObject();
                    injectedFields.add(fieldObject.get("index").getAsInt(), new MessageEmbed.Field("\u200B", "\u200B", fieldObject.get("inline").getAsBoolean()));
                }
            }
            for (MessageEmbed.Field injectedField : injectedFields) {
                embedBuilder.addField(injectedField);
            }
        }
        if (injectedFields.isEmpty()) {
            for (JsonElement e : fields) embedBuilder.addField(DiscordUtils.mapFieldFromJsonObject(e.getAsJsonObject(), map));
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
            String buttonUrl = "";

            ComponentHandlerOverrides overrides = new ComponentHandlerOverrides();
            if (e.isJsonObject()) {
                JsonObject o = e.getAsJsonObject();
                requiresOverriding = true;
                int totalSize = o.size();
                if (o.has("url")) {
                    buttonUrl = map.applyPlaceholders(o.get("url").getAsString());
                    totalSize--;
                }
                if (o.has("row_index")) {
                    overrides.setRowIndex(o.get("row_index").getAsInt());
                    totalSize--;
                }
                if (o.has("ephemeral_defer")) {
                    overrides.setEphemeralDefer(o.get("ephemeral_defer").getAsBoolean());
                    overrides.setDefer(true);
                    totalSize--;
                }
                if (o.has("timeout")) {
                    overrides.setTimeout(o.get("timeout").getAsInt());
                    totalSize--;
                }
                if (o.has("delete_after_timeout")) {
                    overrides.setDeleteAfterTimeout(o.get("delete_after_timeout").getAsInt());
                    totalSize--;
                }
                if (o.has("disable_once_used")) {
                    overrides.setDisableOnceUsed(o.get("disable_once_used").getAsBoolean());
                    totalSize--;
                }
                if (o.has("disable_all_once_used")) {
                    overrides.setDisableAllOnceUsed(o.get("disable_all_once_used").getAsBoolean());
                    totalSize--;
                }
                if (o.has("invoker_only")) {
                    overrides.setInvokerOnly(o.get("invoker_only").getAsBoolean());
                    totalSize--;
                }
                if (totalSize > 0) requiresAppearanceOverriding = true;
            }

            if (!buttonUrl.isEmpty()) {
                overrides.setComponent(ComponentUtils.createButton(buttonUrl, e.getAsJsonObject()));
                runtimeComponents.add(new RuntimeComponent(
                        map.getCtx().getUser().getId(),
                        null,
                        overrides,
                        new JsonObject(),
                        null
                ));
                continue;
            }

            String componentName = (requiresOverriding) ? e.getAsJsonObject().get("name").getAsString() : e.getAsString();
            ComponentHandler componentHandler = DiscordBot.getInstance().getComponentManager().getComponentHandler(componentName);

            if (!(componentHandler instanceof BasicComponentHandler basicComponentHandler)) continue;

            ItemComponent itemComponent;
            if (basicComponentHandler instanceof DropdownHandler dropdownHandler) {
                List<ComponentUtils.DropdownOption> options = new ArrayList<>(injectedDropdownOptions.getOrDefault(componentName, new ArrayList<>()));
                if (requiresAppearanceOverriding) {
                    if (dropdownsContainingInjectedOptions.contains(componentName)) {
                        if (options.isEmpty()) continue;
                        itemComponent = ComponentUtils.createDropdown(componentName, e.getAsJsonObject(), options);
                    }
                    else itemComponent = ComponentUtils.createDropdown(componentName, e.getAsJsonObject());
                } else {
                    if (dropdownsContainingInjectedOptions.contains(componentName)) {
                        if (options.isEmpty()) continue;
                        itemComponent = ComponentUtils.createDropdown(dropdownHandler, options);
                    }
                    else itemComponent = dropdownHandler.getDropdown();
                }
            } else itemComponent = (requiresAppearanceOverriding) ? ComponentUtils.createComponent(componentName, e.getAsJsonObject(), map) : basicComponentHandler.getItemComponent();
            overrides.setComponent(itemComponent);

            RuntimeComponent runtimeComponent = new RuntimeComponent(
                    map.getCtx().getUser().getId(),
                    basicComponentHandler,
                    overrides,
                    componentBonus,
                    componentConsumer
            );

            runtimeComponents.add(runtimeComponent);
        }
        if (componentsLimit > 0) runtimeComponents = runtimeComponents.subList(0, Math.min(runtimeComponents.size(), componentsLimit));
        return runtimeComponents;
    }

    @Nullable
    private Modal getModal() {
        if (!responseObject.has("modal")) return null;

        boolean requiresOverriding = responseObject.get("modal").isJsonObject();
        String modalName = (requiresOverriding) ? responseObject.get("modal").getAsJsonObject().get("name").getAsString() : responseObject.get("modal").getAsString();
        ComponentHandler componentHandler = DiscordBot.getInstance().getComponentManager().getComponentHandler(modalName);

        if (!(componentHandler instanceof ModalHandler modalHandler)) return null;
        return (requiresOverriding) ? ComponentUtils.createModal(modalName, responseObject.get("modal").getAsJsonObject(), map) : modalHandler.getModal();
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

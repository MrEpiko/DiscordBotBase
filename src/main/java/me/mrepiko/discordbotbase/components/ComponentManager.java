package me.mrepiko.discordbotbase.components;

import lombok.Getter;
import me.mrepiko.discordbotbase.DiscordBot;
import me.mrepiko.discordbotbase.components.general.BasicComponentHandler;
import me.mrepiko.discordbotbase.components.general.ComponentHandler;
import me.mrepiko.discordbotbase.components.general.types.ButtonHandler;
import me.mrepiko.discordbotbase.components.general.types.DropdownHandler;
import me.mrepiko.discordbotbase.components.general.types.ModalHandler;
import me.mrepiko.discordbotbase.components.handlers.ShowcaseButtonHandler;
import me.mrepiko.discordbotbase.components.handlers.ShowcaseDropdownHandler;
import me.mrepiko.discordbotbase.components.handlers.ShowcaseModalHandler;
import me.mrepiko.discordbotbase.context.interaction.ButtonContext;
import me.mrepiko.discordbotbase.context.interaction.DropdownContext;
import me.mrepiko.discordbotbase.context.interaction.ModalContext;
import me.mrepiko.discordbotbase.mics.PlaceholderMap;
import me.mrepiko.discordbotbase.mics.ResponseBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionComponent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.interactions.modals.Modal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@Getter
public class ComponentManager extends ListenerAdapter {

    private final List<ComponentHandler> componentHandlers = new ArrayList<>();
    private final List<RuntimeComponent> runtimeComponents = new ArrayList<>();
    private final HashMap<String, HashMap<String, Modal>> modals = new HashMap<>();

    public void registerComponentHandlers() {
        addComponentHandler(new ShowcaseButtonHandler());
        addComponentHandler(new ShowcaseDropdownHandler());
        addComponentHandler(new ShowcaseModalHandler());
    }

    public void reload() {
        componentHandlers.clear();
        runtimeComponents.clear();
        modals.clear();
        registerComponentHandlers();
    }

    @Nullable
    public ComponentHandler getComponentHandler(String name) {
        for (ComponentHandler c: componentHandlers) if (c.getName().equalsIgnoreCase(name)) return c;
        return null;
    }

    private void addComponentHandler(ComponentHandler componentHandler) {
        if (contains(componentHandler)) return;
        componentHandlers.add(componentHandler);
    }

    private boolean contains(ComponentHandler componentHandler) {
        for (ComponentHandler c: componentHandlers) if (c.getName().equalsIgnoreCase(componentHandler.getName())) return true;
        return false;
    }

    public void addRuntimeComponent(RuntimeComponent runtimeComponent) {
        runtimeComponents.add(runtimeComponent);
        BasicComponentHandler basicComponentHandler = runtimeComponent.getBasicComponentHandler();
        if (basicComponentHandler.getTimeout() > 0) {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    runtimeComponents.remove(runtimeComponent);
                }
            }, basicComponentHandler.getTimeout() * 1000L + 2000L);
        }
    }

    @Nullable
    private RuntimeComponent getRuntimeComponent(String name, String messageId) {
        for (RuntimeComponent r: runtimeComponents) {
            if (r == null || r.getMessageId() == null) continue;
            if (r.getBasicComponentHandler().getName().equalsIgnoreCase(name) && r.getMessageId().equalsIgnoreCase(messageId)) return r;
        }
        return null;
    }

    @Nullable
    private Modal getModal(String modalId, String messageId) {
        Modal modal = modals.getOrDefault(modalId, new HashMap<>()).getOrDefault(messageId, null);
        if (modal != null) modals.get(modalId).remove(messageId);
        return modal;
    }

    public void addModal(Modal modal, String messageId) {
        HashMap<String, Modal> previousModals = new HashMap<>(modals.getOrDefault(modal.getId(), new HashMap<>()));
        previousModals.put(messageId, modal);
        modals.put(modal.getId(), previousModals);
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        String componentName = event.getComponentId().split("\\.")[0];
        RuntimeComponent runtimeComponent = getRuntimeComponent(componentName, event.getMessageId());
        ComponentHandler componentHandler = (runtimeComponent == null) ? getComponentHandler(componentName) : runtimeComponent.getBasicComponentHandler();
        if (!(componentHandler instanceof ButtonHandler buttonHandler)) return;
        ButtonContext ctx = new ButtonContext(event, runtimeComponent);
        PlaceholderMap map = new PlaceholderMap(ctx);
        if (runtimeComponent != null) runtimeComponent.acceptConsumer(ctx);
        if (!checkForRequirements(map, componentHandler)) return;
        if (buttonHandler.getComponentConfig().has("default_response")) ResponseBuilder.build(map, componentHandler.getComponentConfig().get("default_response").getAsJsonObject()).send();
        else {
            if (buttonHandler.isDefer()) ctx.getCallback().deferReply(buttonHandler.isEphemeralDefer()).queue();
            buttonHandler.handle(ctx);
        }
        if (!buttonHandler.isDisableOnceUsed() && !buttonHandler.isDisableAllOnceUsed()) return;
        List<List<ActionComponent>> itemComponents = new ArrayList<>();
        for (LayoutComponent c: event.getMessage().getComponents()) {
            List<ActionComponent> actionComponents = new ArrayList<>();
            for (ActionComponent a: c.getActionComponents()) {
                if (buttonHandler.isDisableAllOnceUsed()) actionComponents.add(a.asDisabled());
                else actionComponents.add((a.getId() == null || !a.getId().equalsIgnoreCase(buttonHandler.getName())) ? a : a.asDisabled());
            }
            itemComponents.add(actionComponents);
        }
        event.getMessage().editMessageComponents(itemComponents.stream().map(ActionRow::of).toList()).queue();
    }

    @Override
    public void onStringSelectInteraction(@NotNull StringSelectInteractionEvent event) {
        String componentName = event.getComponentId().split("\\.")[0];
        RuntimeComponent runtimeComponent = getRuntimeComponent(componentName, event.getMessageId());
        ComponentHandler componentHandler = (runtimeComponent == null) ? getComponentHandler(componentName) : runtimeComponent.getBasicComponentHandler();
        if (!(componentHandler instanceof DropdownHandler dropdownHandler)) return;
        DropdownContext ctx = new DropdownContext(event, runtimeComponent);
        PlaceholderMap map = new PlaceholderMap(ctx);
        if (runtimeComponent != null) runtimeComponent.acceptConsumer(ctx);
        if (!checkForRequirements(map, componentHandler)) return;
        if (dropdownHandler.getComponentConfig().has("default_response")) ResponseBuilder.build(map, componentHandler.getComponentConfig().get("default_response").getAsJsonObject()).send();
        else {
            if (dropdownHandler.isDefer()) ctx.getCallback().deferReply(dropdownHandler.isEphemeralDefer()).queue();
            dropdownHandler.handle(ctx);
        }
        if (!dropdownHandler.isDisableOnceUsed() && !dropdownHandler.isDisableAllOnceUsed()) return;
        List<List<ActionComponent>> itemComponents = new ArrayList<>();
        for (LayoutComponent c: event.getMessage().getComponents()) {
            List<ActionComponent> actionComponents = new ArrayList<>();
            for (ActionComponent a: c.getActionComponents()) {
                if (dropdownHandler.isDisableAllOnceUsed()) actionComponents.add(a.asDisabled());
                else actionComponents.add((a.getId() == null || !a.getId().equalsIgnoreCase(dropdownHandler.getName())) ? a : a.asDisabled());
            }
            itemComponents.add(actionComponents);
        }
        event.getMessage().editMessageComponents(itemComponents.stream().map(ActionRow::of).toList()).queue();
    }

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        Modal modal = getModal(event.getModalId(), (event.getMessage() == null) ? "" : event.getMessage().getId());
        ModalHandler modalHandler = (ModalHandler) getComponentHandler(event.getModalId().split("\\.")[0]);
        if (modalHandler == null) return;
        ModalContext ctx = new ModalContext(event, modal);
        PlaceholderMap map = new PlaceholderMap(ctx);
        if (!checkForRequirements(map, modalHandler)) return;
        if (modalHandler.getComponentConfig().has("default_response")) {
            ResponseBuilder.build(map, modalHandler.getComponentConfig().get("default_response").getAsJsonObject()).send();
            return;
        }
        if (modalHandler.isDefer()) ctx.getCallback().deferReply(modalHandler.isEphemeralDefer()).queue();
        modalHandler.handle(ctx);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean checkForRequirements(PlaceholderMap map, ComponentHandler componentHandler) {
        DiscordBot instance = DiscordBot.getInstance();
        User user = map.getCtx().getUser();
        if (user == null) return false;

        if (!componentHandler.isEnabled()) {
            ResponseBuilder.build(map, componentHandler.getErrorHandlers().get("disabled")).send();
            return false;
        }
        if (componentHandler.isAdmin() && !instance.getAdmins().contains(user.getId())) {
            ResponseBuilder.build(map, componentHandler.getErrorHandlers().get("reserved_for_admin")).send();
            return false;
        }
        Long currentCooldown = componentHandler.getCooldowns().getOrDefault(user.getId(), 0L);
        if (currentCooldown > 0) {
            if (System.currentTimeMillis() - currentCooldown < componentHandler.getCooldown() * 1000) {
                map.put("cooldown_time_left", String.valueOf(Math.round((double) currentCooldown / 1000 + componentHandler.getCooldown() - (double) System.currentTimeMillis() / 1000)));
                ResponseBuilder.build(map, componentHandler.getErrorHandlers().get("cooldown")).send();
                return false;
            } else componentHandler.getCooldowns().remove(user.getId());
        }
        if (map.getCtx().getMember() != null && !componentHandler.getRequiredRoles().isEmpty()) {
            boolean hasRole = false;
            for (Role r: map.getCtx().getMember().getRoles()) {
                if (componentHandler.getRequiredRoles().contains(r.getId())) {
                    hasRole = true;
                    break;
                }
            }
            if (!hasRole) {
                ResponseBuilder.build(map, componentHandler.getErrorHandlers().get("reserved_for_role")).send();
                return false;
            }
        }
        if (!componentHandler.getRequiredUsers().isEmpty() && !componentHandler.getRequiredUsers().contains(user.getId())) {
            ResponseBuilder.build(map, componentHandler.getErrorHandlers().get("reserved_for_user")).send();
            return false;
        }
        if (map.getCtx().getGuild() != null && map.getCtx().getChannel() != null && !componentHandler.getRequiredChannels().isEmpty() && !componentHandler.getRequiredChannels().contains(map.getCtx().getChannel().getId())) {
            ResponseBuilder.build(map, componentHandler.getErrorHandlers().get("reserved_for_channel")).send();
            return false;
        }
        Member member = map.getCtx().getMember();
        if (member != null && !componentHandler.getRequiredPermissions().isEmpty() && !member.hasPermission(componentHandler.getRequiredPermissions())) {
            ResponseBuilder.build(map, componentHandler.getErrorHandlers().get("missing_permissions")).send();
            return false;
        }
        if (member != null && map.getCtx().getChannel() != null && !componentHandler.getRequiredChannelPermissions().isEmpty() && !member.hasPermission((GuildMessageChannel) map.getCtx().getChannel(), componentHandler.getRequiredChannelPermissions())) {
            ResponseBuilder.build(map, componentHandler.getErrorHandlers().get("missing_channel_permissions")).send();
            return false;
        }

        if (componentHandler.getCooldown() > 0) componentHandler.getCooldowns().put(user.getId(), System.currentTimeMillis());
        return true;
    }

}
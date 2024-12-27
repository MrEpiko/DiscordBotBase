package me.mrepiko.discordbotbase.components;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Delegate;
import me.mrepiko.discordbotbase.components.general.BasicComponentHandler;
import me.mrepiko.discordbotbase.context.InteractionContext;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Getter
public class RuntimeComponent {

    private final BasicComponentHandler basicComponentHandler;
    @Delegate private final JsonObject bonus;
    @Nullable private final Consumer<InteractionContext> consumer;

    private final ComponentHandlerOverrides overrides;

    @Setter private String messageId;
    private final String invokerId;

    public RuntimeComponent(String invokerId, BasicComponentHandler basicComponentHandler, ComponentHandlerOverrides overrides, JsonObject bonus, @Nullable Consumer<InteractionContext> consumer) {
        this.invokerId = invokerId;
        this.basicComponentHandler = basicComponentHandler;
        this.overrides = overrides;
        this.bonus = bonus;
        this.consumer = consumer;
    }

    public void acceptConsumer(InteractionContext interactionContext) {
        if (consumer == null) return;
        consumer.accept(interactionContext);
    }

    public ItemComponent getComponent() {
        return (overrides.isComponentOverrode()) ? overrides.getComponent() : basicComponentHandler.getItemComponent();
    }

    public int getRowIndex() {
        if (!overrides.isRowIndexOverrode() && basicComponentHandler == null) return 0;
        return (overrides.isRowIndexOverrode()) ? overrides.getRowIndex() : basicComponentHandler.getRowIndex();
    }

    public boolean isDefer() {
        return (overrides.isDeferOverrode()) ? overrides.isDefer() : basicComponentHandler.isDefer();
    }

    public boolean isEphemeralDefer() {
        return (overrides.isEphemeralDeferOverrode()) ? overrides.isEphemeralDefer() : basicComponentHandler.isEphemeralDefer();
    }

    public int getTimeout() {
        if (!overrides.isTimeoutOverrode() && basicComponentHandler == null) return 0;
        return (overrides.isTimeoutOverrode()) ? overrides.getTimeout() : basicComponentHandler.getTimeout();
    }

    public int getDeleteAfterTimeout() {
        return (overrides.isDeleteAfterTimeoutOverrode()) ? overrides.getDeleteAfterTimeout() : basicComponentHandler.getDeleteAfterTimeout();
    }

    public boolean isDisableOnceUsed() {
        return (overrides.isDisableOnceUsedOverrode()) ? overrides.isDisableOnceUsed() : basicComponentHandler.isDisableOnceUsed();
    }

    public boolean isDisableAllOnceUsed() {
        return (overrides.isDisableAllOnceUsedOverrode()) ? overrides.isDisableAllOnceUsed() : basicComponentHandler.isDisableAllOnceUsed();
    }

    public boolean isInvokerOnly() {
        return (overrides.isInvokerOnlyOverrode()) ? overrides.isInvokerOnly() : basicComponentHandler.isInvokerOnly();
    }

    public void applyTimeout(Message message) {
        setMessageId(message.getId());
        if (getTimeout() <= 0) return;
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                List<LayoutComponent> components = new ArrayList<>();
                for (LayoutComponent x : message.getComponents()) components.add(x.asDisabled());
                message.editMessageComponents(components).queue(x -> {}, x -> {});
                if (getDeleteAfterTimeout() > 0) message.delete().queueAfter(getDeleteAfterTimeout(), TimeUnit.SECONDS, s -> {}, s -> {});
            }
        }, getTimeout() * 1000L);
    }



}
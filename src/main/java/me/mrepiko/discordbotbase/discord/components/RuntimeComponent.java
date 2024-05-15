package me.mrepiko.discordbotbase.discord.components;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Delegate;
import me.mrepiko.discordbotbase.discord.components.general.BasicComponentHandler;
import me.mrepiko.discordbotbase.discord.context.InteractionContext;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import org.jetbrains.annotations.Nullable;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Getter
public class RuntimeComponent {

    private final BasicComponentHandler basicComponentHandler;
    @Delegate private final JsonObject bonus;
    @Nullable private final Consumer<InteractionContext> consumer;
    private final ItemComponent overrideComponent;
    private final int overrideRowIndex;

    @Setter private String messageId;

    public RuntimeComponent(BasicComponentHandler basicComponentHandler, JsonObject bonus, ItemComponent overrideComponent, int overrideRowIndex, @Nullable Consumer<InteractionContext> consumer) {
        this.basicComponentHandler = basicComponentHandler;
        this.bonus = bonus;
        this.overrideComponent = overrideComponent;
        this.overrideRowIndex = overrideRowIndex;
        this.consumer = consumer;
    }

    public void acceptConsumer(InteractionContext interactionContext) {
        if (consumer == null) return;
        consumer.accept(interactionContext);
    }

    public ItemComponent getComponent() {
        return (overrideComponent == null) ? basicComponentHandler.getItemComponent() : overrideComponent;
    }

    public int getRowIndex() {
        return (overrideRowIndex == 9) ? basicComponentHandler.getRowIndex() : overrideRowIndex;
    }

    public void applyTimeout(Message message) {
        setMessageId(message.getId());
        if (basicComponentHandler.getTimeout() <= 0) return;
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                for (LayoutComponent x: message.getComponents()) message.editMessageComponents(x.asDisabled()).queue(y -> {}, y -> {});
                if (basicComponentHandler.getDeleteAfterTimeout() > 0) message.delete().queueAfter(basicComponentHandler.getDeleteAfterTimeout(), TimeUnit.SECONDS, s -> {}, s -> {});
            }}, basicComponentHandler.getTimeout() * 1000L);
    }

}
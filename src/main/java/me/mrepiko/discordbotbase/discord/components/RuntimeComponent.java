package me.mrepiko.discordbotbase.discord.components;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Delegate;
import me.mrepiko.discordbotbase.discord.components.general.BasicComponentHandler;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import org.jetbrains.annotations.Nullable;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Getter
public class RuntimeComponent {

    private final BasicComponentHandler basicComponentHandler;
    @Delegate private final JsonObject bonus;
    private ItemComponent overrideComponent;
    private int overrideRowIndex;
    @Setter private String messageId;

    public RuntimeComponent(BasicComponentHandler basicComponentHandler, JsonObject object, @Nullable ItemComponent overrideComponent, int overrideRowIndex) {
        this(basicComponentHandler, object, overrideComponent);
        this.overrideRowIndex = overrideRowIndex;
    }

    public RuntimeComponent(BasicComponentHandler basicComponentHandler, JsonObject bonus, int overrideRowIndex) {
        this(basicComponentHandler, bonus);
        this.overrideRowIndex = overrideRowIndex;
    }

    public RuntimeComponent(BasicComponentHandler basicComponentHandler, JsonObject bonus, @Nullable ItemComponent overrideComponent) {
        this(basicComponentHandler, bonus);
        this.overrideComponent = overrideComponent;
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
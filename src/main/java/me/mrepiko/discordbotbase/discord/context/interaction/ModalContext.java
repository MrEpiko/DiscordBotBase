package me.mrepiko.discordbotbase.discord.context.interaction;

import lombok.Getter;
import lombok.experimental.Delegate;
import me.mrepiko.discordbotbase.discord.context.InteractionContext;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.interactions.modals.ModalInteraction;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@Getter
public class ModalContext extends InteractionContext {


    private final ModalInteractionEvent event;
    @Delegate private final ModalInteraction interaction;
    @Nullable private final Modal modal;

    public ModalContext(ModalInteractionEvent event, @Nullable Modal modal) {
        super(event, event.getInteraction());
        this.event = event;
        this.interaction = event.getInteraction();
        this.modal = modal;
    }

    public String getValue(String option, String defaultValue) {
        return (interaction.getValue(option) != null) ? Objects.requireNonNull(interaction.getValue(option)).getAsString() : defaultValue;
    }

}

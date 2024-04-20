package me.mrepiko.discordbotbase.discord.context.interaction;

import lombok.Getter;
import lombok.experimental.Delegate;
import me.mrepiko.discordbotbase.discord.components.RuntimeComponent;
import me.mrepiko.discordbotbase.discord.context.InteractionContext;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;
import org.jetbrains.annotations.Nullable;

@Getter
public class ButtonContext extends InteractionContext {

    private final ButtonInteractionEvent event;
    @Delegate private final ButtonInteraction interaction;
    @Nullable private final RuntimeComponent runtimeComponent;

    public ButtonContext(ButtonInteractionEvent event, @Nullable RuntimeComponent runtimeComponent) {
        super(event, event.getInteraction());
        this.event = event;
        this.interaction = event.getInteraction();
        this.runtimeComponent = runtimeComponent;
    }

}

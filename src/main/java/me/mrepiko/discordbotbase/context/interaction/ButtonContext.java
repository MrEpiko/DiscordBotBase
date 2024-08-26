package me.mrepiko.discordbotbase.context.interaction;

import lombok.Getter;
import lombok.experimental.Delegate;
import me.mrepiko.discordbotbase.components.RuntimeComponent;
import me.mrepiko.discordbotbase.context.InteractionContext;
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

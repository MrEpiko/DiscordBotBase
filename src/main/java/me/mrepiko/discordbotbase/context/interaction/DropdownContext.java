package me.mrepiko.discordbotbase.context.interaction;

import lombok.Getter;
import lombok.experimental.Delegate;
import me.mrepiko.discordbotbase.components.RuntimeComponent;
import me.mrepiko.discordbotbase.context.InteractionContext;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectInteraction;
import org.jetbrains.annotations.Nullable;

@Getter
public class DropdownContext extends InteractionContext {

    private final StringSelectInteractionEvent event;
    @Delegate private final StringSelectInteraction interaction;
    @Nullable private final RuntimeComponent runtimeComponent;

    public DropdownContext(StringSelectInteractionEvent event, @Nullable RuntimeComponent runtimeComponent) {
        super(event, event.getInteraction());
        this.event = event;
        this.interaction = event.getInteraction();
        this.runtimeComponent = runtimeComponent;
    }

}

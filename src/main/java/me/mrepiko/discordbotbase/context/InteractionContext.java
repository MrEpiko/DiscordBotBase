package me.mrepiko.discordbotbase.context;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import org.jetbrains.annotations.Nullable;

@AllArgsConstructor
@Getter
public class InteractionContext implements IContext {

    private final Event event;
    private final IReplyCallback callback;

    @Nullable
    @Override
    public Guild getGuild() {
        return null;
    }

    @Nullable
    @Override
    public MessageChannel getChannel() {
        return null;
    }

    @Nullable
    @Override
    public Message getMessage() {
        return null;
    }

    @Nullable
    @Override
    public User getUser() {
        return null;
    }

    @Nullable
    @Override
    public Member getMember() {
        return null;
    }
}

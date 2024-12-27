package me.mrepiko.discordbotbase.context;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import org.jetbrains.annotations.Nullable;

@AllArgsConstructor
@Getter
public class MessageChannelContext implements Context {

    private final MessageChannel channel;
    private final User user;

    public MessageChannelContext(MessageChannel channel) {
        this.channel = channel;
        this.user = null;
    }

    @Nullable
    @Override
    public Guild getGuild() {
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
        return user;
    }

    @Nullable
    @Override
    public Member getMember() {
        return null;
    }

}

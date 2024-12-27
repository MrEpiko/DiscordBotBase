package me.mrepiko.discordbotbase.context.channel;

import me.mrepiko.discordbotbase.context.MessageChannelContext;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import org.jetbrains.annotations.Nullable;

public class PrivateChannelContext extends MessageChannelContext {

    private final PrivateChannel privateChannel;
    private final User user;

    public PrivateChannelContext(PrivateChannel privateChannel) {
        super(privateChannel, privateChannel.getUser());
        this.privateChannel = privateChannel;
        this.user = privateChannel.getUser();
    }

    public PrivateChannelContext(PrivateChannel privateChannel, User user) {
        super(privateChannel, user);
        this.privateChannel = privateChannel;
        this.user = user;
    }

    @Override
    public MessageChannel getChannel() {
        return privateChannel;
    }

    @Nullable
    @Override
    public User getUser() {
        return user;
    }

}

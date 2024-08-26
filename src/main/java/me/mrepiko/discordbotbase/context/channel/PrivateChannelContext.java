package me.mrepiko.discordbotbase.context.channel;

import me.mrepiko.discordbotbase.context.ChannelContext;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import org.jetbrains.annotations.Nullable;

public class PrivateChannelContext extends ChannelContext {

    private final PrivateChannel privateChannel;


    public PrivateChannelContext(PrivateChannel privateChannel) {
        super(privateChannel);
        this.privateChannel = privateChannel;
    }

    @Override
    public MessageChannel getChannel() {
        return privateChannel;
    }

    @Nullable
    @Override
    public User getUser() {
        return privateChannel.getUser();
    }

}

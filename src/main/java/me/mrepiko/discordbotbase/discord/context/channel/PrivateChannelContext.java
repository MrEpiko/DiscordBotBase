package me.mrepiko.discordbotbase.discord.context.channel;

import lombok.Getter;
import me.mrepiko.discordbotbase.discord.context.ChannelContext;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import org.jetbrains.annotations.Nullable;

public class PrivateChannelContext extends ChannelContext {

    private final PrivateChannel privateChannel;
    @Nullable @Getter private final Runnable successfulAction;
    @Nullable @Getter private final Runnable failedAction;

    public PrivateChannelContext(PrivateChannel privateChannel) {
        this(privateChannel, null, null);
    }

    public PrivateChannelContext(PrivateChannel privateChannel, @Nullable Runnable sucessfulAction, @Nullable Runnable failedAction) {
        super(privateChannel);
        this.privateChannel = privateChannel;
        this.successfulAction = sucessfulAction;
        this.failedAction = failedAction;
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

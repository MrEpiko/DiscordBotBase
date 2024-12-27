package me.mrepiko.discordbotbase.context.channel;

import lombok.experimental.Delegate;
import me.mrepiko.discordbotbase.context.MessageChannelContext;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

import javax.annotation.Nullable;

public class GuildMessageChannelContext extends MessageChannelContext {

    @Delegate private final GuildMessageChannel guildMessageChannel;
    private final User user;

    public GuildMessageChannelContext(GuildMessageChannel guildMessageChannel, User user) {
        super(guildMessageChannel, user);
        this.guildMessageChannel = guildMessageChannel;
        this.user = user;
    }

    public GuildMessageChannelContext(GuildMessageChannel guildMessageChannel) {
        super(guildMessageChannel, null);
        this.guildMessageChannel = guildMessageChannel;
        this.user = null;
    }

    @Override
    public MessageChannel getChannel() {
        return guildMessageChannel;
    }

    @Override
    @Nullable
    public User getUser() {
        return user;
    }

    public String getGuildId() {
        return guildMessageChannel.getGuild().getId();
    }

}

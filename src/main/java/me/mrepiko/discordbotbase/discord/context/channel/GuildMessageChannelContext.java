package me.mrepiko.discordbotbase.discord.context.channel;

import lombok.experimental.Delegate;
import me.mrepiko.discordbotbase.discord.context.ChannelContext;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

public class GuildMessageChannelContext extends ChannelContext {

    @Delegate private final GuildMessageChannel guildMessageChannel;

    public GuildMessageChannelContext(GuildMessageChannel guildMessageChannel) {
        super(guildMessageChannel);
        this.guildMessageChannel = guildMessageChannel;
    }

    @Override
    public MessageChannel getChannel() {
        return guildMessageChannel;
    }

}

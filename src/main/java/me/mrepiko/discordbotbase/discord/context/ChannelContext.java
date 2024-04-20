package me.mrepiko.discordbotbase.discord.context;

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
public class ChannelContext implements IContext {

    private final MessageChannel channel;

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
        return null;
    }

    @Nullable
    @Override
    public Member getMember() {
        return null;
    }

}

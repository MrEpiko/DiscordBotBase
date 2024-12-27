package me.mrepiko.discordbotbase.context;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

import javax.annotation.Nullable;

public interface Context {

    @Nullable
    Guild getGuild();
    @Nullable
    Message getMessage();
    @Nullable
    Member getMember();
    MessageChannel getChannel();
    User getUser();

}

package me.mrepiko.discordbotbase.discord.context.interaction;

import lombok.Getter;
import lombok.experimental.Delegate;
import me.mrepiko.discordbotbase.discord.commands.types.Command;
import me.mrepiko.discordbotbase.discord.context.InteractionContext;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@Getter
public class CommandContext extends InteractionContext {

    private final SlashCommandInteractionEvent event;
    @Delegate private final SlashCommandInteraction interaction;
    private final Command command;

    public CommandContext(SlashCommandInteractionEvent event, Command command) {
        super(event, event.getInteraction());
        this.event = event;
        this.command = command;
        this.interaction = event.getInteraction();
    }

    public String getOptionAsString(String option) {
        return getOptionAsString(option, "");
    }

    public int getOptionAsInteger(String option) {
        return Objects.requireNonNull(interaction.getOption(option)).getAsInt();
    }

    public double getOptionAsDouble(String option) {
        return Objects.requireNonNull(interaction.getOption(option)).getAsDouble();
    }

    public long getOptionAsLong(String option) {
        return Objects.requireNonNull(interaction.getOption(option)).getAsLong();
    }

    public boolean getOptionAsBoolean(String option) {
        return Objects.requireNonNull(interaction.getOption(option)).getAsBoolean();
    }

    @Nullable
    public User getOptionAsUser(String option) {
        return Objects.requireNonNull(interaction.getOption(option)).getAsUser();
    }

    @Nullable
    public Member getOptionAsMember(String option) {
        return Objects.requireNonNull(interaction.getOption(option)).getAsMember();
    }

    @Nullable
    public Channel getOptionAsChannel(String option) {
        return Objects.requireNonNull(interaction.getOption(option)).getAsChannel();
    }

    @Nullable
    public Role getOptionAsRole(String option) {
        return Objects.requireNonNull(interaction.getOption(option)).getAsRole();
    }

    @Nullable
    public IMentionable getOptionAsMentionable(String option) {
        return Objects.requireNonNull(interaction.getOption(option)).getAsMentionable();
    }

    @Nullable
    public Message.Attachment getOptionAsAttachment(String option) {
        return Objects.requireNonNull(interaction.getOption(option)).getAsAttachment();
    }

    public String getOptionAsString(String option, String defaultValue) {
        return (interaction.getOption(option) == null) ? defaultValue : Objects.requireNonNull(interaction.getOption(option)).getAsString();
    }

    public int getOptionAsInteger(String option, int defaultValue) {
        return (interaction.getOption(option) == null) ? defaultValue : getOptionAsInteger(option);
    }

    public double getOptionAsDouble(String option, double defaultValue) {
        return (interaction.getOption(option) == null) ? defaultValue : getOptionAsDouble(option);
    }

    public long getOptionAsLong(String option, long defaultValue) {
        return (interaction.getOption(option) == null) ? defaultValue : getOptionAsLong(option);
    }

    public boolean getOptionAsBoolean(String option, boolean defaultValue) {
        return (interaction.getOption(option) == null) ? defaultValue : getOptionAsBoolean(option);
    }

    public User getOptionAsUser(String option, User defaultValue) {
        return (interaction.getOption(option) == null) ? defaultValue : getOptionAsUser(option);
    }

    public Member getOptionAsMember(String option, Member defaultValue) {
        return (interaction.getOption(option) == null) ? defaultValue : getOptionAsMember(option);
    }

    public Channel getOptionAsChannel(String option, Channel defaultValue) {
        return (interaction.getOption(option) == null) ? defaultValue : getOptionAsChannel(option);
    }

    public Role getOptionAsRole(String option, Role defaultValue) {
        return (interaction.getOption(option) == null) ? defaultValue : getOptionAsRole(option);
    }

    public IMentionable getOptionAsMentionable(String option, IMentionable defaultValue) {
        return (interaction.getOption(option) == null) ? defaultValue : getOptionAsMentionable(option);
    }

    public Message.Attachment getOptionAsAttachment(String option, Message.Attachment defaultValue) {
        return (interaction.getOption(option) == null) ? defaultValue : getOptionAsAttachment(option);
    }

    public int getPage() {
        return getOptionAsInteger("page", 1);
    }

}

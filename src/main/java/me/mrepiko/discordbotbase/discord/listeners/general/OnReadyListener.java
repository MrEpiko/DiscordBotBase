package me.mrepiko.discordbotbase.discord.listeners.general;

import me.mrepiko.discordbotbase.discord.DiscordBot;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class OnReadyListener extends ListenerAdapter {

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        DiscordBot instance = DiscordBot.getInstance();
        instance.setup();
    }
}

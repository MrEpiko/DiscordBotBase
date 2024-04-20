package me.mrepiko.discordbotbase.discord.modules.executor;

import me.mrepiko.discordbotbase.discord.listeners.ShowcaseListener;
import me.mrepiko.discordbotbase.discord.modules.Module;

public class ShowcaseListenerModule extends Module {

    public ShowcaseListenerModule() {
        super("showcase_listener", false);
    }

    @Override
    public void onEnable() {
        registerListener(new ShowcaseListener());
    }
}

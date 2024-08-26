package me.mrepiko.discordbotbase.modules.executor;

import me.mrepiko.discordbotbase.listeners.ShowcaseListener;
import me.mrepiko.discordbotbase.modules.Module;

public class ShowcaseListenerModule extends Module {

    public ShowcaseListenerModule() {
        super("showcase_listener", false);
    }

    @Override
    public void onEnable() {
        registerListener(new ShowcaseListener());
    }
}

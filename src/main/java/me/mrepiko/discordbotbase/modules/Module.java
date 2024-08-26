package me.mrepiko.discordbotbase.modules;

import lombok.Getter;
import me.mrepiko.discordbotbase.DiscordBot;
import me.mrepiko.discordbotbase.config.Config;
import me.mrepiko.discordbotbase.mics.Constants;
import me.mrepiko.discordbotbase.tasks.Task;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Getter
public abstract class Module {

    private final String name;
    private final boolean configEnabled;
    private boolean enabled;
    private final List<ListenerAdapter> listeners = new ArrayList<>();
    private final List<Task> tasks = new ArrayList<>();
    private Config config;

    public Module(String name, boolean configEnabled) {
        this.name = name;
        this.configEnabled = configEnabled;
    }

    public void setupConfig() throws IOException {
        if (!configEnabled) return;
        this.config = new Config(Constants.MODULE_CONFIGURATION_FOLDER_PATH + "/" + name + ".json");
    }

    public void enable() {
        enabled = true;
        onEnable();
    }

    protected void registerListener(ListenerAdapter listener) {
        listeners.add(listener);
        DiscordBot.getInstance().getJda().addEventListener(listener);
    }

    protected void registerTask(Task task) {
        tasks.add(task);
        DiscordBot.getInstance().getTaskManager().addTask(task);
    }

    public abstract void onEnable();

}

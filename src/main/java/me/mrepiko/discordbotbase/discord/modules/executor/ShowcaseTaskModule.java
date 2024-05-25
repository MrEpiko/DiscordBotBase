package me.mrepiko.discordbotbase.discord.modules.executor;

import me.mrepiko.discordbotbase.discord.modules.Module;
import me.mrepiko.discordbotbase.discord.tasks.Task;

import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class ShowcaseTaskModule extends Module {

    public ShowcaseTaskModule() {
        super("showcase_task", false);
    }

    @Override
    public void onEnable() {
        registerTask(new Task("showcase_task", 5, 10, TimeUnit.SECONDS, () -> System.out.println("Hey this is a showcase of task module!")));
    }

}

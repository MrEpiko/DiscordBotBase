package me.mrepiko.discordbotbase.discord.modules.executor;

import me.mrepiko.discordbotbase.discord.modules.Module;
import me.mrepiko.discordbotbase.discord.tasks.Task;

import java.util.TimerTask;

public class ShowcaseTaskModule extends Module {

    public ShowcaseTaskModule() {
        super("showcase_task", false);
    }

    @Override
    public void onEnable() {
        registerTask(new Task("showcase_task", 5000L, 1000L * 10, new TimerTask() {
            @Override
            public void run() {
                System.out.println("Hey this is a showcase of task module!");
            }
        }));
    }

}

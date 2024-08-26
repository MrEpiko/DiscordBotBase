package me.mrepiko.discordbotbase.modules.executor;


import me.mrepiko.discordbotbase.modules.Module;
import me.mrepiko.discordbotbase.tasks.Task;

public class ShowcaseTaskModule extends Module {

    public ShowcaseTaskModule() {
        super("showcase_task", false);
    }

    @Override
    public void onEnable() {
        registerTask(new Task("showcase_task", 5, 10, () -> System.out.println("Hey this is a showcase of task module!")));
    }

}

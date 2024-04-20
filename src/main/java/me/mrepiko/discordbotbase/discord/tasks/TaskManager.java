package me.mrepiko.discordbotbase.discord.tasks;

import lombok.Getter;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

@Getter
public class TaskManager {

    private final HashMap<String, Task> tasks = new HashMap<>();

    public void addTask(Task task) {
        if (tasks.containsKey(task.getId()) || task.getDelay() < 5000) return;
        tasks.put(task.getId(), task);
    }

    public void startTask(String id) {
        Task task = tasks.getOrDefault(id, null);
        if (task == null) return;
        if (task.getTaskType() == Task.Type.SCHEDULED) {
            task.getTimer().schedule(task.getTimerTask(), task.getDelay());
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    tasks.remove(task.getId());
                }
            }, task.getDelay() + 2000);
        } else task.getTimer().scheduleAtFixedRate(task.getTimerTask(), task.getDelay(), task.getPeriod());
        task.setStartedAtTimestamp(System.currentTimeMillis() / 1000);
    }

    public void startTasks() {
        for (Task t: tasks.values()) startTask(t.getId());
    }

}

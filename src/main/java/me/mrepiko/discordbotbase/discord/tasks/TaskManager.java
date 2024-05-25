package me.mrepiko.discordbotbase.discord.tasks;

import lombok.Getter;

import java.sql.Time;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Getter
public class TaskManager {

    private final HashMap<String, Task> tasks = new HashMap<>();
    private final ScheduledExecutorService service = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors() * 2);

    public void addTask(Task task) {
        if (tasks.containsKey(task.getId()) || task.getDelay() < 5000) return;
        tasks.put(task.getId(), task);
    }

    public void startTask(String id) {
        Task task = tasks.getOrDefault(id, null);
        if (task == null) return;
        if (task.getTaskType() == Task.Type.SCHEDULED) {
            long delay = service.schedule(task.getRunnable(), task.getDelay(), task.getTimeUnit()).getDelay(TimeUnit.MILLISECONDS);
            service.schedule(() -> {
                tasks.remove(task.getId());
            }, delay + 2000, TimeUnit.MILLISECONDS);
        } else service.scheduleAtFixedRate(task.getRunnable(), task.getDelay(), task.getPeriod(), task.getTimeUnit());
        task.setStartedAtTimestamp(System.currentTimeMillis() / 1000);
    }

    public void startTasks() {
        for (Task t: tasks.values()) startTask(t.getId());
    }

}

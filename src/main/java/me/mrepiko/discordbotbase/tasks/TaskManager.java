package me.mrepiko.discordbotbase.tasks;

import lombok.Getter;

import java.util.HashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Getter
public class TaskManager {

    private final HashMap<String, Task> tasks = new HashMap<>();
    private final ScheduledExecutorService service = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors() * 2);

    public void addTask(Task task) {
        if (tasks.containsKey(task.getId()) || task.getDelay() < 5) return;
        tasks.put(task.getId(), task);
    }

    public void startTask(String id) {
        Task task = tasks.getOrDefault(id, null);
        if (task == null) return;
        if (task.getTaskType() == Task.Type.SCHEDULED) {
            service.schedule(task.getRunnable(), task.getDelay(), TimeUnit.SECONDS);
            service.schedule(() -> {
                tasks.remove(task.getId());
            }, task.getDelay() + 2, TimeUnit.SECONDS);
        } else service.scheduleAtFixedRate(task.getRunnable(), task.getDelay(), task.getPeriod(), TimeUnit.SECONDS);
        task.setStartedAtTimestamp(System.currentTimeMillis() / 1000);
    }

    public void startTasks() {
        for (Task t: tasks.values()) startTask(t.getId());
    }

}

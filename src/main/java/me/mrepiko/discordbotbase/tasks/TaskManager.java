package me.mrepiko.discordbotbase.tasks;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Getter
public class TaskManager {

    private final static Logger LOGGER = LoggerFactory.getLogger(TaskManager.class);

    private final HashMap<String, Task> tasks = new HashMap<>();
    private final ScheduledExecutorService service = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors() * 2);

    public void addTask(Task task) {
        if (tasks.containsKey(task.getId()) || task.getDelay() < 5) return;
        tasks.put(task.getId(), task);
    }

    public void startTask(String id) {
        Task task = tasks.getOrDefault(id, null);
        if (task == null) throw new IllegalArgumentException("Task not found");
        Runnable wrappedRunnable = () -> {
            try {
                task.getRunnable().run();
            } catch (Exception e) {
                LOGGER.error("Error while running task {}", task.getId(), e);
            }
        };
        ScheduledFuture<?> scheduledFuture;
        if (task.getTaskType() == Task.Type.SCHEDULED) {
            scheduledFuture = service.schedule(wrappedRunnable, task.getDelay(), TimeUnit.SECONDS);
            service.schedule(() -> {
                tasks.remove(task.getId());
            }, task.getDelay() + 2, TimeUnit.SECONDS);
        } else {
            scheduledFuture = service.scheduleAtFixedRate(wrappedRunnable, task.getDelay(), task.getPeriod(), TimeUnit.SECONDS);
        }
        task.setScheduledFuture(scheduledFuture);
        task.setStartedAtTimestamp(System.currentTimeMillis() / 1000 + task.getDelay());
    }

    public void startTasks() {
        for (Task t: tasks.values()) startTask(t.getId());
    }

    public void stopTask(String id) {
        Task task = tasks.getOrDefault(id, null);
        if (task == null) throw new IllegalArgumentException("Task not found");
        task.getScheduledFuture().cancel(true);
    }

    public void stopTasks() {
        for (Task t: tasks.values()) {
            stopTask(t.getId());
        }
    }

    public void executeTask(Task task, boolean includeDelay) {
        Runnable wrappedRunnable = () -> {
            try {
                task.getRunnable().run();
            } catch (Exception e) {
                LOGGER.error("Error while running task {}", task.getId(), e);
            }
        };
        if (includeDelay) service.schedule(wrappedRunnable, task.getDelay(), TimeUnit.SECONDS);
        else service.execute(wrappedRunnable);
    }

    public void executeTask(String id) {
        Task task = tasks.getOrDefault(id, null);
        if (task == null) throw new IllegalArgumentException("Task not found");
        Runnable wrappedRunnable = () -> {
            try {
                task.getRunnable().run();
            } catch (Exception e) {
                LOGGER.error("Error while running task {}", task.getId(), e);
            }
        };
        service.execute(wrappedRunnable);
    }

    public void executeCacheableTasks() {
        for (Task t: tasks.values()) {
            if (t instanceof Cacheable) executeTask(t.getId());
        }
    }

}

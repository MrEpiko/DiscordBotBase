package me.mrepiko.discordbotbase.discord.tasks;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Getter
public class Task {

    private final String id;
    private final Type taskType;
    private final long delay;
    private final long period;
    private final TimeUnit timeUnit;

    private final Runnable runnable;

    @Setter private long startedAtTimestamp;

    public Task(String id, long delay, long period, TimeUnit timeUnit, Runnable runnable) {
        this(id, Type.SCHEDULED_AT_FIXED_RATE, delay, period, timeUnit, runnable);
    }

    public enum Type {
        SCHEDULED,
        SCHEDULED_AT_FIXED_RATE
    }

}

package me.mrepiko.discordbotbase.listeners;

import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.session.ShutdownEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.SubscribeEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EventWaiter implements EventListener {

    private final HashMap<Class<?>, Set<WaitingEvent>> waitingEvents;
    private final ScheduledExecutorService threadPool;
    private final boolean shutdownAutomatically;

    public EventWaiter() {
        this(Executors.newSingleThreadScheduledExecutor(), true);
    }

    public EventWaiter(ScheduledExecutorService threadPool, boolean shutdownAutomatically) {
        this.waitingEvents = new HashMap<>();
        this.threadPool = threadPool;
        this.shutdownAutomatically = shutdownAutomatically;
    }

    public <T extends Event> void waitForEvent(Class<T> classType, Predicate<T> condition, Consumer<T> action) {
        waitForEvent(classType, condition, action, -1, null);
    }

    public <T extends Event> void waitForEvent(Class<T> classType, Predicate<T> condition, Consumer<T> action, long timeout, Runnable timeoutAction) {
        WaitingEvent<T> waitingEvent = new WaitingEvent<>(condition, action);
        Set<WaitingEvent> waitingEventSet = waitingEvents.computeIfAbsent(classType, c -> new HashSet<>());
        waitingEventSet.add(waitingEvent);

        if (timeout > 0) {
            threadPool.schedule(() -> {
                try {
                    if (waitingEventSet.remove(waitingEvent) && timeoutAction != null)
                        timeoutAction.run();
                } catch (Exception ignored) {}
            }, timeout, TimeUnit.SECONDS);
        }
    }

    @Override
    @SubscribeEvent
    public final void onEvent(GenericEvent event) {
        for (Class<?> c = event.getClass(); c != null; c = c.getSuperclass()) {
            if (this.waitingEvents.containsKey(c)) {
                Set<WaitingEvent> set = this.waitingEvents.get(c);
                WaitingEvent[] toRemove = set.toArray(new WaitingEvent[0]);
                set.removeAll(Stream.of(toRemove).filter((i) -> {
                    return i.attempt(event);
                }).collect(Collectors.toSet()));
            }

            if (event instanceof ShutdownEvent && this.shutdownAutomatically) this.threadPool.shutdown();
        }
    }

    private static class WaitingEvent<T extends GenericEvent> {
        final Predicate<T> condition;
        final Consumer<T> action;

        WaitingEvent(Predicate<T> condition, Consumer<T> action) {
            this.condition = condition;
            this.action = action;
        }

        boolean attempt(T event) {
            if (condition.test(event)) {
                action.accept(event);
                return true;
            }
            return false;
        }
    }

}

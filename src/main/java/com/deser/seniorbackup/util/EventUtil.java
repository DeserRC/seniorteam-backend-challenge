package com.deser.seniorbackup.util;

import com.deser.seniorbackup.SeniorBackup;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredListener;

import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.bukkit.event.EventPriority.NORMAL;
import static org.bukkit.event.HandlerList.getHandlerLists;


public final class EventUtil<E extends Event> {
    private final SeniorBackup main;
    private final Class<E> clazz;
    private final ScheduledExecutorService executor;

    private long time;
    private RegisteredListener listener;
    private Runnable runnable;
    private Predicate<E> predicate;
    private Consumer<E> consumer;

    private EventUtil(final SeniorBackup main, final Class<E> clazz) {
        this.main = main;
        this.clazz = clazz;
        this.executor = newSingleThreadScheduledExecutor();
    }

    public void schedule() {
        register(event -> {
            if (!clazz.isInstance(event)) return;
            executor.execute(() -> consumer.accept((E) event));
            unregister();
            executor.shutdownNow();
        });

        executor.schedule(() -> {
            unregister();
            runnable.run();
        }, time, SECONDS);
    }

    private void register(final Consumer<Event> consumer) {
        listener = new RegisteredListener(new Listener() {},
                (listener, event) -> consumer.accept(event),
                NORMAL, main, false);

        for (HandlerList handler : getHandlerLists()) {
            handler.register(listener);
        }
    }

    private void unregister() {
        for (HandlerList handler : getHandlerLists()) {
            handler.unregister(listener);
        }
    }

    public static class EventBuilder<E extends Event> {
        private final EventUtil<E> event;

        public EventBuilder(final SeniorBackup main, final Class<E> clazz) {
            this.event = new EventUtil<>(main, clazz);
        }

        public EventBuilder<E> filter(final Predicate<E> predicate) {
            event.predicate = predicate;
            return this;
        }

        public EventBuilder<E> execute(final Consumer<E> consumer) {
            event.consumer = consumer;
            return this;
        }

        public EventBuilder<E> limit(final long time, final Runnable runnable) {
            event.time = time;
            event.runnable = runnable;
            return this;
        }

        public void build() {
            event.schedule();
        }
    }
}

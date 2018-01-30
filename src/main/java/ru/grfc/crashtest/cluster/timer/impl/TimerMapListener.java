package ru.grfc.crashtest.cluster.timer.impl;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.MapEvent;
import com.hazelcast.map.listener.*;
import org.apache.log4j.Logger;
import ru.grfc.crashtest.cluster.timer.TimerDescriptor;

/**
 * Created by mvj on 10.10.2017.
 */
public class TimerMapListener implements EntryAddedListener<String, TimerDescriptor>,
        EntryUpdatedListener<String, TimerDescriptor>,
        EntryRemovedListener<String, TimerDescriptor>,
        EntryEvictedListener<String, TimerDescriptor>,
        MapEvictedListener,
        MapClearedListener {
    private static final Logger logger = Logger.getLogger(TimerMapListener.class);

    @Override
    public void entryAdded(EntryEvent<String, TimerDescriptor> entryEvent) {
        logger.info("Entry Added:" + entryEvent);
    }

    @Override
    public void entryRemoved(EntryEvent<String, TimerDescriptor> entryEvent) {
        logger.info("Entry Removed:" + entryEvent);
    }

    @Override
    public void entryUpdated(EntryEvent<String, TimerDescriptor> entryEvent) {
        logger.info("Entry Updated:" + entryEvent);
    }

    @Override
    public void entryEvicted(EntryEvent<String, TimerDescriptor> event) {
        System.out.println("Entry Evicted:" + event);
    }

    @Override
    public void mapEvicted(MapEvent event) {
        System.out.println("Map Evicted:" + event);
    }

    @Override
    public void mapCleared(MapEvent event) {
        System.out.println("Map Cleared:" + event);
    }
}

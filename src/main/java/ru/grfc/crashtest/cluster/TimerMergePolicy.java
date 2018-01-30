package ru.grfc.crashtest.cluster;

import com.hazelcast.core.EntryView;
import com.hazelcast.map.merge.MapMergePolicy;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import org.apache.log4j.Logger;
import ru.grfc.crashtest.cluster.timer.TimerDescriptor;
import ru.grfc.crashtest.cluster.timer.impl.ClusterTimerDestroyer;

import java.io.IOException;

/**
 * Created by mvj on 11.10.2017.
 */
public class TimerMergePolicy implements MapMergePolicy {
    private static final Logger logger = Logger.getLogger(TimerMergePolicy.class);

    @Override
    public Object merge(String mapName, EntryView mergingEntry, EntryView existingEntry) {
        logger.debug("Merging...");
        if (mergingEntry == null) {
            return existingEntry.getValue();
        }
        if (existingEntry == null) {
            return mergingEntry.getValue();
        }

        //mergingEntry win!
        if (existingEntry.getValue() != null && existingEntry.getValue() instanceof TimerDescriptor) {
            TimerDescriptor timerDescriptor = (TimerDescriptor) existingEntry.getValue();
            logger.info("Stop timer: " + timerDescriptor.getClusteredTimer().getTimerInfo().getTimerName());
            boolean result = LocalTimerManagerHolder.getInstance().unwrap(ClusterTimerDestroyer.class).justStopLocalTimer(timerDescriptor.getClusteredTimer());
            logger.info(result ? "Timer stopped." : "Timer doesn't stopped!");
        }
        return mergingEntry.getValue();
    }

    @Override
    public void writeData(ObjectDataOutput objectDataOutput) throws IOException {

    }

    @Override
    public void readData(ObjectDataInput objectDataInput) throws IOException {

    }
}

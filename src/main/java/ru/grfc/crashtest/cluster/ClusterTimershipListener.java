package ru.grfc.crashtest.cluster;

import com.hazelcast.core.MemberAttributeEvent;
import com.hazelcast.core.MembershipEvent;
import com.hazelcast.core.MembershipListener;
import org.apache.log4j.Logger;

/**
 * Created by mvj on 17.11.2016.
 */
public class ClusterTimershipListener implements MembershipListener {
    private static final Logger logger = Logger.getLogger(ClusterTimershipListener.class);

    public void memberAdded(MembershipEvent membershipEvent) {
        logger.info("Added: " + membershipEvent);
    }

    public void memberRemoved(MembershipEvent membershipEvent) {
        logger.info("Removed: " + membershipEvent);
    }

    public void memberAttributeChanged(MemberAttributeEvent memberAttributeEvent) {
        logger.info("Member attribute changed: " + memberAttributeEvent);
    }

}

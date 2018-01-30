package ru.grfc.crashtest.cluster;

import java.io.Serializable;
import java.util.List;

/**
 * Created by mvj on 30.08.2017.
 */
public interface ICluster extends Serializable {
    String getGroupName();

    String getGroupPassword();

    Boolean getManagementEnabled();

    String getManagementUrl();

    List<IClusterNode> getClusterNodes();

    void setClusterNodes(List<IClusterNode> clusterNodes);

    List<IStartupTimer> getStartupTimers();

    void setStartupTimers(List<IStartupTimer> startupTimers);

}

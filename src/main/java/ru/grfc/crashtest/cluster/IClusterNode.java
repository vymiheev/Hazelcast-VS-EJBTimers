package ru.grfc.crashtest.cluster;

import java.io.Serializable;
import java.util.List;

/**
 * Created by mvj on 07.03.2017.
 */
public interface IClusterNode extends Serializable {
    String getNode();

    Integer getHzPort();

    String getClusterName();

    boolean getNodeEnabled();

    Integer getPort();

    String getNodeName();

    List<IStartupTimer> getStartupTimers();

    void setStartupTimers(List<IStartupTimer> startupTimers);

    ICluster getClusterWrapper();

    void setClusterWrapper(ICluster clusterWrapper);

}

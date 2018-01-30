package ru.hz.ejb.cluster;

import java.util.List;

/**
 * Created by mvj on 29.08.2017.
 */
public interface IStartupTimer {
    String getTimerName();

    Boolean getIsEnabled();

    String getScope();

    String getDescription();

    Boolean getIsActual();

    ICluster getCluster();

    void setCluster(ICluster cluster);

    List<IClusterNode> getClusterNodes();

    void setClusterNodes(List<IClusterNode> clusterNodes);

}

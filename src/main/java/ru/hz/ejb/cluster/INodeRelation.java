package ru.hz.ejb.cluster;

import java.util.List;

/**
 * Created by mvj on 07.03.2017.
 */
public interface INodeRelation {
    IClusterNode getThisNode();

    void setThisNode(IClusterNode thisNode);

    List<IClusterNode> getRelatedNodes();

    void setRelatedNodes(List<IClusterNode> relatedNodes);

    boolean isClustered();
}

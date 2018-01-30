package ru.hz.ejb.cluster;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mvj on 03.03.2017.
 */
public class NodesRelation implements INodeRelation {
    private static final long serialVersionUID = 5589759091528649243L;
    private IClusterNode thisNode;
    private List<IClusterNode> relatedNodes = new ArrayList<>();

    public IClusterNode getThisNode() {
        return thisNode;
    }

    public void setThisNode(IClusterNode thisNode) {
        this.thisNode = thisNode;
    }

    public List<IClusterNode> getRelatedNodes() {
        return relatedNodes;
    }

    public void setRelatedNodes(List<IClusterNode> relatedNodes) {
        this.relatedNodes = relatedNodes;
    }

    public boolean isClustered() {
        return !relatedNodes.isEmpty();
    }
}

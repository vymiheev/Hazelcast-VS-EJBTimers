package ru.grfc.crashtest.cluster;

/**
 * Created by mvj on 07.03.2017.
 */
public class NetworkConfiguration {
    private INodeRelation nodeRelation;
    private String groupName;
    private String groupPassword;
    private String manCenterURL;
    private boolean manCenterEnabled;

    public String getManCenterURL() {
        return manCenterURL;
    }

    public void setManCenterURL(String manCenterURL) {
        this.manCenterURL = manCenterURL;
    }

    public boolean isManCenterEnabled() {
        return manCenterEnabled;
    }

    public void setManCenterEnabled(boolean manCenterEnabled) {
        this.manCenterEnabled = manCenterEnabled;
    }

    public String getGroupPassword() {
        return groupPassword;
    }

    public void setGroupPassword(String groupPassword) {
        this.groupPassword = groupPassword;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public INodeRelation getNodeRelation() {
        return nodeRelation;
    }

    public void setNodeRelation(INodeRelation nodeRelation) {
        this.nodeRelation = nodeRelation;
    }

    public boolean isClustered() {
        return nodeRelation != null && nodeRelation.isClustered();
    }
}

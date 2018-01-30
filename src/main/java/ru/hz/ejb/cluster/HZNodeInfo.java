package ru.hz.ejb.cluster;

import java.io.Serializable;
import java.net.InetSocketAddress;

/**
 * Created by mvj on 10.02.2017.
 */
public class HZNodeInfo implements Serializable {
    private static final long serialVersionUID = 5542145902201028613L;
    private boolean localMember;
    private InetSocketAddress address;
    private String uuid;

    public boolean isLocalMember() {
        return localMember;
    }

    public void setLocalMember(boolean localMember) {
        this.localMember = localMember;
    }

    public InetSocketAddress getAddress() {
        return address;
    }

    public void setAddress(InetSocketAddress address) {
        this.address = address;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}

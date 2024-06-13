package ru.afkgroupnn.FreeIP.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Cluster {
    private int clusterId;
    private String clusterName;
    private List<Pool> clientIPPools;
    private List<Pool> switchesIPPools;
    private List<Range> privateIPsRange;

    public List<IPClass> getArrayFreeIp(){
        List<IPClass> freeIps = new ArrayList<>();
        for(Pool pool : clientIPPools){
            freeIps.addAll(pool.getArrayFreeIp());
        }
        return freeIps;
    }
    @Override
    public String toString() {
        StringBuilder clusterStr = new StringBuilder();
        clusterStr.append("\nCluster: " + clusterName + "\n" +
                "\tFree Public IPs: " + getFreeIPSize() + "\n");
        clusterStr.append("\tSwitches Pools: \n");
        for (Pool pool : switchesIPPools){
            clusterStr.append(pool);
        }
        clusterStr.append("\tPublic IPs Pools: \n");
        for (Pool pool : clientIPPools) {
           clusterStr.append(pool);
        }

        return clusterStr.toString();
    }
    public int getFreeIPSize(){
        int summ = 0;
        for (Pool pool : clientIPPools){
            summ = summ + pool.getFreeSizePool();
        }
        return summ;
    }
}

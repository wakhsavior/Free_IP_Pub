package ru.afkgroupnn.FreeIP.model;

import lombok.Data;

@Data
public class FindIPResult {
    private Cluster cluster;
    private Pool pool;
    public FindIPResult(Cluster cluster,Pool pool){
        this.cluster = cluster;
        this.pool = pool;
    }
}

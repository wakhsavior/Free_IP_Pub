package ru.afkgroupnn.FreeIP.repositories;

import ru.afkgroupnn.FreeIP.Exceptions.WrongExecuteException;
import ru.afkgroupnn.FreeIP.model.Cluster;
import ru.afkgroupnn.FreeIP.model.IPClass;
import ru.afkgroupnn.FreeIP.model.Pool;

import java.util.List;
import java.util.function.Function;

public interface ClustersImplement {
    Cluster getClusterbyName(String name);
    Cluster getClusterbyId(int id);
    List<Cluster> getAllClusters();
    void removeFreeIP(IPClass IP) throws WrongExecuteException;
    IPClass getFreeIP(Cluster cluster) throws WrongExecuteException;
    void recoverFreeIP(IPClass IP) throws WrongExecuteException;
    Cluster getClusterByIP(IPClass IP,Function<Cluster, List<Pool>> func);
    Cluster getClusterByIP(IPClass IP);
    void createClusters();

}

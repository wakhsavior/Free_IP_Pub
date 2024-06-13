package ru.afkgroupnn.FreeIP.services;


import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Service;
import ru.afkgroupnn.FreeIP.model.Cluster;
import ru.afkgroupnn.FreeIP.repositories.ClustersFilesRepo;
import ru.afkgroupnn.FreeIP.repositories.ClustersImplement;


import java.util.List;

@Service
@Log4j
public class ClustersService {

    private final ClustersImplement clustersRepo;

    public ClustersService(ClustersFilesRepo clustersRepo) {
        this.clustersRepo = clustersRepo;
        LOGGER.info("BEAN " + this.getClass() + " created.");
    }


    public List<Cluster> getClusters() {
        return clustersRepo.getAllClusters();
    }

    public Cluster getClusterByName(String name) {
        return clustersRepo.getClusterbyName(name);
    }

    public Cluster getClusterById(int id) {
        return clustersRepo.getClusterbyId(id);
    }

    public void createClusters() {
        clustersRepo.createClusters();
    }

}

package ru.afkgroupnn.FreeIP.repositories;


import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Component;
import ru.afkgroupnn.FreeIP.Exceptions.UserException;
import ru.afkgroupnn.FreeIP.Exceptions.WrongExecuteException;
import ru.afkgroupnn.FreeIP.Exceptions.WrongInputDataConfigException;
import ru.afkgroupnn.FreeIP.Exceptions.WrongInputDataException;
import ru.afkgroupnn.FreeIP.FuncInterfaces.ConsumerWithException;
import ru.afkgroupnn.FreeIP.config.ApplicationProperties;
import ru.afkgroupnn.FreeIP.model.*;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import java.util.regex.Pattern;

@Component
@Log4j
public class ClustersFilesRepo implements ClustersImplement {
    private List<Cluster> clusters;
    private final ApplicationProperties appProperties;
    private final String clustersFiles;
    private final String poolIPAddressFile;
    private final String poolSwitchesFile;
    private final String ipRangeFile;
    private final List<String[]> clustersData;
    private final List<String[]> switchesData;
    private final List<String[]> rangeData;
    private final List<String[]> IPAddressData;

    public ClustersFilesRepo(ApplicationProperties appProperties) {
        this.appProperties = appProperties;
        this.clustersFiles = appProperties.getClustersDataPath() + appProperties.getClustersFile();
        this.poolIPAddressFile = appProperties.getClustersDataPath() + appProperties.getPoolIpAddressFile();
        this.poolSwitchesFile = appProperties.getClustersDataPath() + appProperties.getPoolSwitchesFile();
        this.ipRangeFile = appProperties.getClustersDataPath() + appProperties.getIpRangeFile();
        this.clustersData = readDataFromFile(clustersFiles);
        this.switchesData = readDataFromFile(poolSwitchesFile);
        this.rangeData = readDataFromFile(ipRangeFile);
        this.IPAddressData = readDataFromFile(poolIPAddressFile);
        LOGGER.info("BEAN " + this.getClass() + " created. " + this);
    }

    @Override
    public Cluster getClusterbyName(String name) {
        for (Cluster cluster : clusters) {
            if (cluster.getClusterName().equals(name)) {
                return cluster;
            }
        }
        return null;
    }

    @Override
    public Cluster getClusterbyId(int id) {
        for (Cluster cluster : clusters) {
            if (cluster.getClusterId() == id) {
                return cluster;
            }
        }
        return null;
    }

    @Override
    public void createClusters() {
        clusters = null;
        clusters = readClusterList();

        for (Cluster cluster : clusters) {
            cluster.setPrivateIPsRange(getRangeByClusterName(cluster.getClusterName()));
            cluster.setSwitchesIPPools(createPools(cluster.getClusterName(), switchesData));
            cluster.setClientIPPools(createPools(cluster.getClusterName(), IPAddressData));
        }
    }


    @Override
    public List<Cluster> getAllClusters() {
        return clusters;
    }

    private List<Pool> createPools(String clusterName, List<String[]> dataLines) {
        List<Pool> pools = new ArrayList<>();
        try {
            for (String[] line : dataLines) {
                if (line[0].equals(clusterName)) {
                    pools.add(Pool.createPool(line[1]));
                }
            }
        } catch (WrongInputDataException e) {
            LOGGER.error("Ошибка в создании пула для кластера " + clusterName);
            LOGGER.error(e.getMessage());
            throw new RuntimeException(e);
        }
        return pools;
    }

    private List<Range> getRangeByClusterName(String clusterName) {
        List<Range> ranges = new ArrayList<>();
        for (String[] line : rangeData) {
            if (line[0].equals(clusterName)) {
                try {
                    String[] res = line[1].split(" ");
                    IPClass startIP = IPClass.createIP(res[1]);
                    IPClass endIP = IPClass.createIP(res[2]);
                    Range range = new Range(startIP, endIP);
                    ranges.add(range);
                } catch (Exception ex) {
                    LOGGER.error("Ошибка ввода {range startIP  endIP}:" + line[1]);
                    LOGGER.error(ex.getMessage());
                    throw new RuntimeException(ex);
                }
            }
        }
        return ranges;
    }

    private List<Cluster> readClusterList() {
        List<Cluster> clusters = new ArrayList<>();
        List<Integer> ids = new ArrayList<>();
        try {
            for (String[] line : clustersData) {
                int id = Integer.parseInt(line[1]);
                if (ids.contains(id)) {
                    throw new WrongInputDataException("Cluster id должен быть уникальным");
                }
                Cluster cluster = new Cluster();
                cluster.setClusterName(line[0]);
                cluster.setClusterId(id);
                clusters.add(cluster);
                ids.add(cluster.getClusterId());
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            throw new RuntimeException(e);
        }
        return clusters;
    }

    private List<String[]> readDataFromFile(String csvFile) {

        String line;
        String cvsSplitBy = ";";
        List<String[]> output = new ArrayList<>();
        String re = "^(\\w|-)+" + cvsSplitBy + "[^;]+$";
        Pattern pattern = Pattern.compile(re);
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {

            while ((line = br.readLine()) != null) {

                if (pattern.matcher(line).matches()) {

                    // use comma as separator
                    String[] dataFromFile = line.split(cvsSplitBy);
                    output.add(dataFromFile);
                } else {
                    if (!line.isEmpty()) {
                        throw new WrongInputDataConfigException("Неправильный формат данных, проверьте данные: " + line);
                    }
                }
            }
        } catch (IOException | WrongInputDataConfigException e) {
            LOGGER.error(e.getMessage());
            throw new RuntimeException(e);
        }
        return output;
    }

    /**
     * Поиск IP адреса в Cluster
     *
     * @param IP   Искомый IP адрес
     * @param func lambda указывает на метод поиска в Коммутаторах или клиентах
     *             cluster_tmp -> cluster_tmp.getClientIPPools() - Для поиска в клиентских пулах
     *             cluster_tmp -> cluster_tmp.getSwitchesIPPools() - Для поиска в пулах коммутаторов
     * @return Пару значений Cluster и Pool
     */
    private Optional<FindIPResult> findIP(IPClass IP, Function<Cluster, List<Pool>> func) {
        for (Cluster cluster : clusters) {
            for (Pool pool : func.apply(cluster)) {
                if (IP.checkIPMatching(pool.getPool())) {
                    return Optional.of(new FindIPResult(cluster, pool));
                }
            }
        }
        return Optional.empty();
    }

    private Optional<FindIPResult> findIP(IPClass IP) {
        for (Cluster cluster : clusters) {
            List<Pool> pools = new ArrayList<>();
            pools.addAll(cluster.getClientIPPools());
            pools.addAll(cluster.getSwitchesIPPools());
            for (Pool pool : pools) {
                if (IP.checkIPMatching(pool.getPool())) {
                    return Optional.of(new FindIPResult(cluster, pool));
                }
            }
        }
        return Optional.empty();
    }

    private void doFreeIP(IPClass IP, ConsumerWithException<Pool> poolConsumer) throws UserException {
        Optional<FindIPResult> res = findIP(IP, Cluster::getClientIPPools);
        if (res.isPresent()) {
            poolConsumer.accept(res.get().getPool());
        } else {
            LOGGER.warn(IP + " problem");
            throw new WrongExecuteException(IP + " not found in any Cluster.");
        }
    }

    /**
     * Метод используется для выдачи любого свободного IP из кластера.
     *
     * @param cluster Из которго нужно выдать адрес
     * @return IP адрес
     * @throws WrongExecuteException Если свободного адреса нет, возвращается Exception
     */
    @Override
    public IPClass getFreeIP(Cluster cluster) throws WrongExecuteException {
        LOGGER.debug(cluster.getArrayFreeIp());
        IPClass IP = cluster.getArrayFreeIp().stream().findAny().orElse(null);
        if (IP != null) {
            removeFreeIP(IP);
            return IP;
        } else {
            throw new WrongExecuteException("Ошибка в выдаче IP адреса, свободные адреса в пуле закончились");
        }
    }

    /**
     * Удаляет IP адрес из свобоных
     *
     * @param IP удаляемый адрес
     */
    @Override
    public void removeFreeIP(IPClass IP) throws WrongExecuteException {
        try {
            doFreeIP(IP, pool -> pool.useFreeIPFromArray(IP));
        } catch (UserException ex){
            LOGGER.warn(ex.getMessage());
            throw new WrongExecuteException("Извлечение адреса " + IP + " завершилось неудачей.");
        }
    }


    /**
     * Возвращает IP адрес в свободные
     *
     * @param IP Возвращаемый адрес
     */
    @Override
    public void recoverFreeIP(IPClass IP) throws WrongExecuteException {

        try {
            doFreeIP(IP, pool -> pool.addFreeIPToArray(IP));
        }
        catch (UserException ex){
            LOGGER.warn(ex.getMessage());
            throw new WrongExecuteException("Возврат адреса " + IP + " завершилось неудачей.");
        }
    }

    /**
     * Поиск IP адреса в Cluster, указывается где его нужно искать, в коммутаторах или пользовательском пуле
     *
     * @param IP   Исходный адрес для поиска
     * @param func lambda указывает на метод поиска в Коммутаторах или клиентах
     *             *             cluster_tmp -> cluster_tmp.getClientIPPools() - Для поиска в клиентских пулах
     *             *             cluster_tmp -> cluster_tmp.getSwitchesIPPools() - Для поиска в пулах коммутаторов
     * @return Cluster в котором находится IP адрес
     */
    @Override
    public Cluster getClusterByIP(IPClass IP, Function<Cluster, List<Pool>> func) {
        return findIP(IP, func).get().getCluster();
    }

    @Override
    public Cluster getClusterByIP(IPClass IP) {
        return findIP(IP).get().getCluster();
    }


    @Override
    public String toString() {
        return "ClustersFilesRepo{" +
                "appProperties=" + appProperties +
                ", clustersFiles='" + clustersFiles + '\'' +
                ", poolIPAddressFile='" + poolIPAddressFile + '\'' +
                ", poolSwitchesFile='" + poolSwitchesFile + '\'' +
                ", ipRangeFile='" + ipRangeFile + '\'' +
                ", clustersData=" + clustersData +
                ", switchesData=" + switchesData +
                ", rangeData=" + rangeData +
                ", IPAddressData=" + IPAddressData +
                '}';
    }
}

package ru.afkgroupnn.FreeIP.repositories;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import ru.afkgroupnn.FreeIP.Exceptions.UserException;
import ru.afkgroupnn.FreeIP.Exceptions.WrongExecuteException;
import ru.afkgroupnn.FreeIP.Exceptions.WrongInputDataException;
import ru.afkgroupnn.FreeIP.config.ApplicationProperties;
import ru.afkgroupnn.FreeIP.model.IPClass;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@RequiredArgsConstructor
public class ClusterFileRepoTest {

    @Test
    public void createCluster() throws Exception {
        ApplicationProperties appProp = new ApplicationProperties();
        appProp.setClustersFile("testCluster.dat");
        appProp.setPoolIpAddressFile("testPoolIP.dat");
        appProp.setIpRangeFile("testRange.dat");
        appProp.setPoolSwitchesFile("testSwitches.dat");
        appProp.setClustersDataPath("./");

        try (FileWriter writer = new FileWriter(appProp.getClustersFile(), false)) {
            writer.write("TestCluster1;1\n");
            writer.write("TestCluster2;2\n");
            writer.write("TestCluster3;3\n");
            writer.write("TestCluster100;100\n");
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
        try (FileWriter writer = new FileWriter(appProp.getPoolIpAddressFile(), false)) {
            writer.write("TestCluster1;86.27.224.30-255\n");
            writer.write("TestCluster2;86.27.226.2-254\n");
            writer.write("TestCluster3;86.27.227.2-254\n");
            writer.write("TestCluster100;108.32.68.2-255\n");
            writer.write("TestCluster100;108.32.69.0-254\n");

        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
        try (FileWriter writer = new FileWriter(appProp.getIpRangeFile(), false)) {
            writer.write("TestCluster1;range 10.30.128.2 10.30.128.254\n");
            writer.write("TestCluster2;range 10.30.129.2 10.30.129.254\n");
            writer.write("TestCluster3;range 10.30.130.2 10.30.130.254\n");
            writer.write("TestCluster100;range 10.30.131.2 10.30.130.254\n");
            writer.write("TestCluster100;range 10.30.132.2 10.30.130.254\n");
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
        try (FileWriter writer = new FileWriter(appProp.getPoolSwitchesFile(), false)) {
            writer.write("TestCluster1;172.16.0.0-254\n");
            writer.write("TestCluster2;172.16.1.0-63\n");
            writer.write("TestCluster3;172.16.1.64-254\n");
            writer.write("TestCluster100;172.16.4.0-78\n");
            writer.write("TestCluster100;172.16.2.128-254\n");
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }

        ClustersImplement clusters = new ClustersFilesRepo(appProp);
        clusters.createClusters();

        System.out.println(clusters.getAllClusters());

        new File(appProp.getClustersDataPath() + appProp.getClustersFile()).delete();
        new File(appProp.getClustersDataPath() + appProp.getPoolIpAddressFile()).delete();
        new File(appProp.getClustersDataPath() + appProp.getPoolSwitchesFile()).delete();
        new File(appProp.getClustersDataPath() + appProp.getIpRangeFile()).delete();

        assertEquals("TestCluster1", clusters.getClusterbyId(1).getClusterName());
        assertEquals("TestCluster2", clusters.getClusterbyId(2).getClusterName());
        assertEquals("TestCluster3", clusters.getClusterbyId(3).getClusterName());
        assertEquals("TestCluster100", clusters.getClusterbyId(100).getClusterName());
        assertNull(clusters.getClusterbyId(5));

        assertEquals(1, clusters.getClusterbyName("TestCluster1").getClusterId());
        assertEquals(2, clusters.getClusterbyName("TestCluster2").getClusterId());
        assertEquals(3, clusters.getClusterbyName("TestCluster3").getClusterId());
        assertEquals(100, clusters.getClusterbyName("TestCluster100").getClusterId());
        assertNull(clusters.getClusterbyName("WrongName"));


        assertEquals(254, clusters.getClusterbyId(100).getClientIPPools().get(0).getSizePool());
        assertEquals(0, clusters.getClusterbyId(100).getClientIPPools().get(1).getStartOctet());
        assertEquals(254, clusters.getClusterbyId(100).getClientIPPools().get(1).getStopOctet());
        assertEquals("range 10.30.128.2 10.30.128.254", clusters.getClusterbyId(1).getPrivateIPsRange().get(0).toString());
        assertEquals(64, clusters.getClusterbyId(2).getSwitchesIPPools().get(0).getSizePool());

        IPClass testIPfromCluster;
        IPClass testIP;

        testIPfromCluster = clusters.getFreeIP(clusters.getClusterbyId(1));
        testIP = new IPClass("86.27.224.30");
        assertEquals(testIP, testIPfromCluster);
        assertThrows(UserException.class, () -> clusters.removeFreeIP(new IPClass("77.77.77.77")));

        clusters.recoverFreeIP(testIPfromCluster);

    }
}

package ru.afkgroupnn.FreeIP.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Component;
import ru.afkgroupnn.FreeIP.config.ApplicationProperties;
import ru.afkgroupnn.FreeIP.model.Abonent;
import ru.afkgroupnn.FreeIP.model.Cluster;
import ru.afkgroupnn.FreeIP.model.Range;
import ru.afkgroupnn.FreeIP.repositories.ClustersImplement;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
@Log4j
public class DhcpConfigService {
    private final ClustersImplement clusterRepo;
    private final ApplicationProperties appProp;

    private BufferedWriter getWriter(String path, String fileName) throws IOException {
        BufferedWriter writer;
        File pathUrl = new File(path);
        if (!pathUrl.exists()) {
            pathUrl.mkdir();
        }
        String fileUrl = path + fileName;
        writer = new BufferedWriter(new FileWriter(fileUrl));
        return writer;
    }


    public void writeToConfigFileClasses(List<Abonent> abonents) {
        LOGGER.info("Общее количество абонентов = " + abonents.size() + " для записи в файл " + appProp.getFileClasses());
        int i = 0;
        try (BufferedWriter writer = getWriter(appProp.getExportDataPath(), appProp.getFileClasses());) {

            for (Abonent abonent : abonents) {
                writer.write("class \"" + abonent.getEQ_IP() + ":" + abonent.getLAN_PORT() + "\" { ");
                writer.newLine();
                writer.write("match if concat(substring ( option agent.remote-id, 2, extract-int " +
                        "(substring ( option agent.remote-id, 1, 1), 8)),\":\",binary-to-ascii " +
                        "( 10, 8, \"\", suffix ( option agent.circuit-id, 1)))=\"" +
                        abonent.getEQ_IP() + ":" + abonent.getLAN_PORT() + "\"; ");
                writer.newLine();
                writer.write("}");
                writer.newLine();
                i++;
            }
            LOGGER.info("Успешно записано " + i + " абонентов в файл " + appProp.getFileClasses());
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
    }

    public void writeToConfigFileClusterStatic(List<Abonent> abonents) {
        LOGGER.info("Общее количество абонентов = " + abonents.size() + " для записи в файлы кластеров");
//        BufferedWriter writerStatic;
//        BufferedWriter writerDeny;
        String fileDir = appProp.getExportDataPath();


        for (Cluster cluster : clusterRepo.getAllClusters()) {
            int i = 0;
            int j = 0;
            String clusterName = String.valueOf(cluster.getClusterName());
            try (BufferedWriter writerStatic = getWriter(fileDir, clusterName + appProp.getFileStaticEnd());
                 BufferedWriter writerDeny = getWriter(fileDir, clusterName + appProp.getFileDenyEnd())) {
                writerDeny.write("pool {");
                writerDeny.newLine();
                for (Abonent abonent : abonents) {

                    if (abonent.getCLUSTER() == null ||
                            abonent.getCLUSTER().isEmpty() ||
                            abonent.getLAN_IP() == null) {
                        continue;
                    }

                    if (cluster.getClusterName().compareToIgnoreCase(abonent.getCLUSTER()) == 0) {
                        writerStatic.write("pool { range " + abonent.getLAN_IP() +
                                "; allow members of \"" +
                                abonent.getEQ_IP() + ":" + abonent.getLAN_PORT() + "\"; }");
                        writerStatic.newLine();
                        i++;
                    }
                    if (cluster.getClusterName().compareToIgnoreCase(abonent.getCLUSTER()) == 0) {
                        writerDeny.write("deny members of \"" +
                                abonent.getEQ_IP() + ":" + abonent.getLAN_PORT() + "\";");
                        writerDeny.newLine();
                        j++;
                    }

                }
                if (cluster.getPrivateIPsRange() != null && !cluster.getPrivateIPsRange().isEmpty()) {
                    for (Range range : cluster.getPrivateIPsRange()) {
                        writerDeny.write(range.toString() + ";");
                        writerDeny.newLine();
                    }
                }

                writerDeny.write("}");

                LOGGER.info("Успешно записано " + i + " абонентов в файл " + clusterName + appProp.getFileStaticEnd());
                LOGGER.info("Успешно записано " + j + " абонентов в файл " + clusterName + appProp.getFileDenyEnd());
            } catch (IOException e) {
                LOGGER.info(e.getMessage());
            }

        }
    }
}

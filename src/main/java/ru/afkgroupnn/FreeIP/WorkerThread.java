package ru.afkgroupnn.FreeIP;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Component;
import ru.afkgroupnn.FreeIP.config.ApplicationProperties;
import ru.afkgroupnn.FreeIP.model.Abonent;
import ru.afkgroupnn.FreeIP.model.Cluster;
import ru.afkgroupnn.FreeIP.model.IPClass;
import ru.afkgroupnn.FreeIP.services.AbonentsService;
import ru.afkgroupnn.FreeIP.services.ClustersService;
import ru.afkgroupnn.FreeIP.services.DhcpConfigService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
@Log4j
public class WorkerThread extends Thread {
    private final ApplicationProperties appProperties;
    private final ClustersService clustersService;
    private final AbonentsService abonentsService;
    private final DhcpConfigService dhcpConfigs;

    public void run() {
        LOGGER.info(" " + (appProperties.getDaemonStatus() ? "Приложение в режиме демона, повторение каждые " + appProperties.getTimeBetweenStart()
                + " минут" : "Пользовательский однократный запуск"));
        if (appProperties.getDaemonStatus()) {
            while (true) {
                Date startDate = new Date();
                startApp();
                Date endDate = new Date();
                long startNextTime = (startDate.getTime() + appProperties.getTimeBetweenStart() * 60000);
                try {
                    if (startNextTime > (endDate.getTime())) {
                        LOGGER.debug("Пауза до следующего выполнения " + (startNextTime - endDate.getTime())/1000 + " секунд. " );
                        Thread.sleep(startNextTime - endDate.getTime());
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        } else {
            startApp();
        }
    }

    private void startApp() {
        Date startDate = new Date();
        LOGGER.info("Время начала выполнения приложения: " + startDate);
        try {
            runFuncDaemon();
            LOGGER.info("Приложение выполнено успешно.");
        } catch (RuntimeException e) {
            LOGGER.error("Приложение выполнено с ОШИБКАМИ!");
            LOGGER.error(e.getMessage());
        } finally {
            Date endDate = new Date();
            LOGGER.info("Время окончания выполнения приложения: " + endDate);
            LOGGER.info("Время выполнения приложения: " + (endDate.getTime() - startDate.getTime())/1000 + " секунд." );
        }
    }


    void runFuncDaemon() throws RuntimeException {

        clustersService.createClusters();

        List<Abonent> disabledAbonentsToChangeIP = abonentsService.getAbonentsToChangePublicIPs(appProperties.getDaysToDeleteIP());
        List<Abonent> disabledAbonentsToDeleteLan = abonentsService.getAbonentsToClearUnusedIPs(appProperties.getDaysToDeleteIP());
        List<Abonent> abonentsToUpdate = new ArrayList<>(disabledAbonentsToChangeIP);


        //
        // Ниже 2 метода которые меняют базу данных, удаляют аттрибуты для определенных абонентов,
        // и меняют IP адрес на 1.1.1.1 у других
        // может сказаться на производительности, но так как таким абонентов не должно быть много, то не критично

        if (!disabledAbonentsToDeleteLan.isEmpty()) {
            abonentsService.clearUnusedIPs(disabledAbonentsToDeleteLan);
        }
        abonentsService.updateAbonentsData(abonentsToUpdate);

        List<Abonent> enabledAbonentsWithLan = abonentsService.getAbonentsWithLan();
        List<Abonent> enabledAbonentsWithoutLan = abonentsService.getAbonentsWithoutLan();
        List<Abonent> enabledAbonentsWithWrongLan = abonentsService.getAbonentsWithWrongLan();
        List<Abonent> abonentsWithWrongIP;

        LOGGER.info("Списки абонентов с IP и без успешно сформированы.");

        List<IPClass> badIPs = abonentsService.updateFreeIPsInClusters();

        abonentsWithWrongIP = abonentsService.getAbonentsWithWrongIPs(badIPs, enabledAbonentsWithLan);
        // Нужно сначал назначить новые адреса, сформировать пул для обновления, объединить его с пулами
        // для изменения адресов, и в конце его применить в БД

        abonentsService.updateAbonentsIPAddressWithoutIP(enabledAbonentsWithWrongLan);
        abonentsService.updateAbonentsIPAddressWithoutIP(abonentsWithWrongIP);
        abonentsToUpdate.addAll(abonentsWithWrongIP);
        abonentsToUpdate.addAll(enabledAbonentsWithWrongLan);

        for (Cluster cluster : clustersService.getClusters()) {
            LOGGER.info(cluster);

        }
        // При вызове метода происходит обновление базы данных для всех переданных абонентов.
        abonentsService.updateAbonentsData(abonentsToUpdate);

        enabledAbonentsWithLan = abonentsService.getAbonentsWithLan();

        if (enabledAbonentsWithLan != null && !enabledAbonentsWithLan.isEmpty()) {
            //выгрузим файлы для статических адресов
            dhcpConfigs.writeToConfigFileClasses(enabledAbonentsWithLan);
            dhcpConfigs.writeToConfigFileClusterStatic(enabledAbonentsWithLan);
        }

    }
}
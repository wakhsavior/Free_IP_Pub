package ru.afkgroupnn.FreeIP;

import lombok.extern.log4j.Log4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import ru.afkgroupnn.FreeIP.config.ApplicationProperties;
import ru.afkgroupnn.FreeIP.repositories.ClustersImplement;
import ru.afkgroupnn.FreeIP.services.ClustersService;

@SpringBootApplication
@Log4j
public class FreeIpApplication {

    public static void main(String[] args) {
        var context = SpringApplication.run(FreeIpApplication.class,args);

//       var context = new AnnotationConfigApplicationContext(FreeIpApplication.class);
//        ApplicationProperties appProperties = context.getBean(ApplicationProperties.class);
//        ClustersImplement poolsImplement = context.getBean(ClustersImplement.class);
//        ClustersService poolsService = context.getBean(ClustersService.class);
        LOGGER.info("context created successfully");
        WorkerThread workerThread = context.getBean(WorkerThread.class);
        context.close();
        LOGGER.info("context closed successfully");
//        LOGGER.info(appProperties);
//        LOGGER.info(poolsImplement);
//        LOGGER.info(poolsService);

        runThread(workerThread);

    }

    private static void runThread(WorkerThread workerThread) {

        workerThread.start();
        LOGGER.info("Основной поток закончил выполнение, управление передано рабочему потоку.");
    }

}

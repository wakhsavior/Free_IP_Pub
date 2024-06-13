package ru.afkgroupnn.FreeIP.repositories;

import org.apache.log4j.Logger;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import ru.afkgroupnn.FreeIP.model.Abonent;
import ru.afkgroupnn.FreeIP.services.AbonentsService;


import java.util.List;



@SpringBootTest
public class AbonentRepoTest {
    @Autowired
    private AbonentsService abonServ;
   final static Logger LOGGER = Logger.getLogger(AbonentRepoTest.class);
   @Test
   public void getAbonentsWithoutLan(){

        Object obj = abonServ.getAbonentsWithWrongLan();
        assert List.class.isInstance(obj);
        LOGGER.debug("Without LAN method: " + ((List<Abonent>) obj).stream().findAny());
    }

    @Test
    public void getAbonentsWithLan(){

        Object obj = abonServ.getAbonentsWithLan();
        assert List.class.isInstance(obj);
        LOGGER.debug("With LAN method: " + ((List<Abonent>) obj).stream().findAny());
    }
    @Test
    public void getAbonentsToChangePublicIPs(){

        Object obj = abonServ.getAbonentsToChangePublicIPs(30);
        assert List.class.isInstance(obj);
        LOGGER.debug("Change Public IP method: " + ((List<Abonent>) obj).stream().findAny());
    }

    @Test
    public void getAbonentsToClearUnusedIPs(){

        Object obj = abonServ.getAbonentsToClearUnusedIPs(30);
        assert List.class.isInstance(obj);
        LOGGER.debug("Clear IP method: " +((List<Abonent>) obj).stream().findAny());
    }

}

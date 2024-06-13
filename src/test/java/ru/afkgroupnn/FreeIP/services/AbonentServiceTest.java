package ru.afkgroupnn.FreeIP.services;


import org.apache.log4j.Logger;

import org.junit.jupiter.api.Test;

import ru.afkgroupnn.FreeIP.Exceptions.WrongInputDataException;
import ru.afkgroupnn.FreeIP.model.Abonent;
import ru.afkgroupnn.FreeIP.model.IPClass;
import ru.afkgroupnn.FreeIP.repositories.AbonentRepo;
import ru.afkgroupnn.FreeIP.repositories.ClustersImplement;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;


public class AbonentServiceTest {
    final static Logger LOGGER = Logger.getLogger(AbonentServiceTest.class);

    public static List<Abonent> getTestAbonents() {

        List<Abonent> abonents = new ArrayList<>();
        try {
            for (int i = 1; i <= 5; i++) {

                Abonent abonent = new Abonent();
                abonent.setCUSTOMER_ID(String.valueOf(i));
                abonent.setDOGOVOR_NO(String.valueOf(i));
                abonent.setFIRSTNAME("Test" + i);
                abonent.setMIDLENAME("Test" + i);
                abonent.setSURNAME("Test" + i);
                abonent.setLAN_IP(new IPClass("192.168.100." + String.valueOf(100 + i)));
                abonent.setLAN_PORT(10 + i);
                abonent.setEQ_IP(new IPClass("172.16.0." + String.valueOf(10 + i)));
                abonent.setEQ_NAME("DES-3200-26/C1");
                abonent.setCLUSTER("Test");
                abonents.add(abonent);
            }
        } catch (WrongInputDataException ex) {
            LOGGER.error(ex.getMessage());
        }

        return abonents;
    }

    @Test
    public void getAbonentsToChangePublicIPsTest() {
        AbonentRepo abonRepo = mock(AbonentRepo.class);
        ClustersImplement clusterRepo = mock(ClustersImplement.class);
        AbonentsService abonServ = new AbonentsService(abonRepo, clusterRepo);
        List<Abonent> abonents = getTestAbonents();

        given(abonRepo.getAbonentsToDisableIP(30)).willReturn(abonents);
        given(abonRepo.getAbonentsWithLan()).willReturn(abonents);
        given(abonRepo.getAbonentsWithWrongLan()).willReturn(abonents);
        given(abonRepo.getAbonentsToDeleteAttributes(30)).willReturn(abonents);

        assertEquals(abonServ.getAbonentsToChangePublicIPs(30), getTestAbonents());
        assertEquals(abonServ.getAbonentsWithLan(), getTestAbonents());
        assertEquals(abonServ.getAbonentsWithWrongLan(), getTestAbonents());
        assertEquals(abonServ.getAbonentsToClearUnusedIPs(30), getTestAbonents());
    }

    /**
     * Переделать, нужно эмулировать AbonentRepo, проверить правильность работы методов сервиса
     @Test public void getAbonentsWithLan(){
     AbonentRepo abonentRepo = new AbonentRepo(jdbc);
     AbonentsService abonServ = new AbonentsService(abonentRepo);
     var result = abonServ.getAbonentsWithLan();
     LOGGER.debug(result);
     assertNotNull(result);
     }
     @Test public void getAbonentsWithoutLan(){
     AbonentRepo abonentRepo = new AbonentRepo(jdbc);
     AbonentsService abonServ = new AbonentsService(abonentRepo);
     var result = abonServ.getAbonentsWithoutLan();
     LOGGER.debug(result);
     assertNotNull(result);
     }
     */
}

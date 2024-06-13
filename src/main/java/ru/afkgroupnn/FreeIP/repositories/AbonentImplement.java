package ru.afkgroupnn.FreeIP.repositories;

import ru.afkgroupnn.FreeIP.model.Abonent;

import java.util.List;

public interface AbonentImplement {
    List<Abonent> getEnabledAbonentsWithoutLan();
    List<Abonent> getAbonentsWithLan();
    List<Abonent> getAbonentsWithWrongLan();
    List<Abonent> getAbonentsToDisableIP(int daysFromDisableToDelete);
    List<Abonent> getAbonentsToDeleteAttributes(int daysFromDisableToDelete);
    List<Abonent> getAbonentsWithStaticIP();
    void deleteAttributesToAbonent(Abonent abon);
    void initIpIfAbonentIsNotLeases(String id);
    int checkIPExistance(String IP);
    List<String> getAllUsedIP();
    void updateAbonentIP(Abonent abonent);
}

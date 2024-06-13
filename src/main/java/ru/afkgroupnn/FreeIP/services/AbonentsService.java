package ru.afkgroupnn.FreeIP.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Service;
import ru.afkgroupnn.FreeIP.Exceptions.WrongExecuteException;
import ru.afkgroupnn.FreeIP.Exceptions.WrongInputDataException;
import ru.afkgroupnn.FreeIP.model.Abonent;
import ru.afkgroupnn.FreeIP.model.Cluster;
import ru.afkgroupnn.FreeIP.model.IPClass;
import ru.afkgroupnn.FreeIP.repositories.AbonentImplement;
import ru.afkgroupnn.FreeIP.repositories.ClustersImplement;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j
public class AbonentsService {
    private final AbonentImplement abonentRepo;
    private final ClustersImplement clusterRepo;

    /**
     * Метод обращается к репозиторию БД для высвобождения белого  IP адресов у выключенных абонентов,
     * очищение происходит при достижении определенного количества дней после отключения
     *
     * @param days Количество дней после отключения абоненто в течении которого IP адрес сохраняется
     */
    public List<Abonent> getAbonentsToChangePublicIPs(int days) {
        List<Abonent> abonents = abonentRepo.getAbonentsToDisableIP(days);
        for (Abonent abonent : abonents) {
            try {
                abonent.setLAN_IP(new IPClass("1.1.1.1"));
                abonent.setCLUSTER("None");
            } catch (WrongInputDataException e) {
                LOGGER.error(e.getMessage());
            }
        }
        return abonents;
    }

    /**
     * Метод получает от репозитория список абонетов для удаления IP адресов
     *
     * @param days Количество дней после отключения абоненто через которое происходит удаление IP адреса
     * @return Список абонентов для очистки IP адреса
     */
    public List<Abonent> getAbonentsToClearUnusedIPs(int days) {
        return abonentRepo.getAbonentsToDeleteAttributes(days);
    }

    /**
     * Метод с использование репозитория производит удаление записи об IP адресе у абонентов из списка
     *
     * @param abons Список абонентов для удаления IP адресов
     */

    public void clearUnusedIPs(List<Abonent> abons) {
        int i = 0;
        for (Abonent abonent : abons) {
            abonentRepo.deleteAttributesToAbonent(abonent);
            LOGGER.info("Удаление адреса для abonent (" + Integer.valueOf(i + 1).toString() + "/" + abons.size() + "): " + abonent);
            i++;
        }
    }

    public List<Abonent> getAbonentsWithLan() {
        return abonentRepo.getAbonentsWithLan();
    }

    public List<Abonent> getAbonentsWithWrongLan() {
        return abonentRepo.getAbonentsWithWrongLan();
    }

    public List<Abonent> getAbonentsWithoutLan() {
        return abonentRepo.getEnabledAbonentsWithoutLan();
    }


    /**
     * Метод удаляет все занятые адреса из пулов свободных IP адресов во всех крастерах.
     * Так-же проверяет корректность назначенных адресов.
     * Исключаются абоненты у которых IP статических для сохранения адреса
     *
     * @return Массив неправильно назначенных адреов
     */
    public List<IPClass> updateFreeIPsInClusters() {
        int badIPsCount = 0;
        List<IPClass> incorrectIPs = new ArrayList<>();
        List<IPClass> usedIPs = null;
        usedIPs = IPClass.createIP(abonentRepo.getAllUsedIP());

        if (usedIPs!=null){
            LOGGER.debug("Пул выданных адресов сформирован {" + usedIPs.size() + "}: " + usedIPs);
            for (IPClass IP : usedIPs) {
                try {
                    clusterRepo.removeFreeIP(IP);
                } catch (WrongExecuteException ex) {
                    LOGGER.info(IP.toString() + " не был удален из пула, не найден ни в одном пуле.");
                    incorrectIPs.add(IP);
                    badIPsCount++;
                }
//                LOGGER.debug(IP.toString() + " был удален из пула.");
            }
            LOGGER.info("Общее количество клиентски IP адресов не попавших ни в один пул: " + badIPsCount);
            return incorrectIPs;
        }
        return null;
    }

    /**
     * Метод назначает Абоненту без IP новый IP адрес, и принадлежность к Cluster
     *
     * @param abonents Список абонентов без корректных IP адресов
     */
    public void updateAbonentsIPAddressWithoutIP(List<Abonent> abonents) {
        if (abonents == null || abonents.isEmpty()) return;

        for (Abonent abonent : abonents) {
            LOGGER.debug(abonent);

            Cluster cluster = clusterRepo.getClusterByIP(abonent.getEQ_IP());
//           LOGGER.debug(cluster);

            if (cluster != null) {
                abonent.setCLUSTER(cluster.getClusterName());
            } else {
                LOGGER.warn("abonent ID: " + abonent.getCUSTOMER_ID() +
                        " FIO: " + abonent.getSURNAME() + " " + abonent.getFIRSTNAME() + " " + abonent.getMIDLENAME()
                        + " не имеет кластера, коммутатор не попадает ни в один пул.");
                continue;
            }
            IPClass freeIp;
            try {
                freeIp = clusterRepo.getFreeIP(cluster);
                LOGGER.debug("Abonent ID: " + abonent.getCUSTOMER_ID() + " IP адрес для выдачи: " + freeIp);
                abonent.setLAN_IP(freeIp);
            } catch (WrongExecuteException e) {
                LOGGER.warn("abonent ID: " + abonent.getCUSTOMER_ID() + " Dogovor Num: " + abonent.getDOGOVOR_NO() + " Cluster: " + abonent.getCLUSTER() +
                        " FIO: " + abonent.getSURNAME() + " " + abonent.getFIRSTNAME() + " " + abonent.getMIDLENAME()
                        + " не может получить IP");
                LOGGER.warn(e.getMessage());
            }
        }
    }

    /**
     * Метод возвращает список всех IP адресов абонентов из репозитория,
     * возможны дубликаты адресов в случае неправильного назначения
     *
     * @return Список IP адресов
     */
    public List<IPClass> getAllIPs() {
        List<IPClass> ips = new ArrayList<>();
        try {
            for (String ipStr : abonentRepo.getAllUsedIP()) {
                ips.add(new IPClass(ipStr));
            }
        } catch (WrongInputDataException ex) {
            LOGGER.error(ex.getMessage());
        }
        return ips;
    }

    /**
     * Метод обновляет данные у всех абонентов списка
     *
     * @param abonents список обновляемых абонентов
     */
    public void updateAbonentsData(List<Abonent> abonents) {
        int i = 0;
        for (Abonent abonent : abonents) {
            abonentRepo.updateAbonentIP(abonent);
            LOGGER.info("Изменение данных для abonent (" + Integer.valueOf(i + 1).toString() + "/" + abonents.size() + "): " + abonent);
            i++;
        }
    }

    /**
     * Метод проверяет наличие IP адреса в базе, если адреса нет, возвращает true,
     * если есть или неправильное значение, то false
     *
     * @param IP искомый IP адрес абонента
     * @return количество найденных адресов
     */
    private boolean checkIpFromDbForFree(String IP) {
        if (IP == null || IP.isEmpty()) return false;

        int rez = abonentRepo.checkIPExistance(IP);
        return rez == 0;
    }

    /**
     * Метод выбирает из списка обонентов с IP адресами переданными в другом списке.
     * В методе так-же проводится обновление необходимых параметов
     *
     * @param badIPs   Список IP адресов для поиска
     * @param abonents Список абонентов в котором проводится поиск
     * @return Список подходящих абонентов
     */
    public List<Abonent> getAbonentsWithWrongIPs(List<IPClass> badIPs, List<Abonent> abonents) {
        List<Abonent> abons = new ArrayList<>();
        List<Abonent> abonsWithStaticIP = abonentRepo.getAbonentsWithStaticIP();
        for (Abonent abonent : abonents) {
            try {
                if (badIPs.contains(abonent.getLAN_IP()) && !abonsWithStaticIP.contains(abonent) ||
                        !(clusterRepo.getClusterByIP(abonent.getLAN_IP()).getClusterName().equals(abonent.getCLUSTER()))) {
                    abonent.setLAN_IP(new IPClass("1.1.1.1"));
                    abonent.setCLUSTER("None");
                    abons.add(abonent);
                }

            } catch (Exception e) {
                LOGGER.error("Неудачная попытка обработки абонента " + abonent);
                LOGGER.error(e.getMessage());
            }
        }
        return abons;
    }
}

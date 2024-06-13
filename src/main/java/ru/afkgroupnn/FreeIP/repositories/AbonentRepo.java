package ru.afkgroupnn.FreeIP.repositories;

import lombok.RequiredArgsConstructor;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.afkgroupnn.FreeIP.model.Abonent;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class AbonentRepo implements AbonentImplement {
    private final JdbcTemplate jdbc;

    /**
     * Метод возвращает список активных абонентов с установленным белым IP адресом
     *
     * @return Список Абонентов
     */
    @Override
    public List<Abonent> getEnabledAbonentsWithoutLan() {
        return jdbc.query("""
                SELECT DISTINCT cust.CUSTOMER_ID, cust.DOGOVOR_NO, cust.SURNAME, cust.FIRSTNAME,\s
                cust.MIDLENAME, lan.IP as LAN_IP, lan.PORT as LAN_PORT, lan.NOTICE as CLUSTER,\s
                eq.NAME as EQ_NAME, eq.IP as EQ_IP, eq.MAC as EQ_MAC  FROM CUSTOMER cust\s
                LEFT JOIN SUBSCR_SERV ss ON CUST.CUSTOMER_ID =ss.CUSTOMER_ID\s
                LEFT JOIN SERVICES serv ON ss.SERV_ID =SERV.SERVICE_ID\s
                LEFT JOIN TV_LAN lan ON  cust.CUSTOMER_ID = lan.CUSTOMER_ID\s
                LEFT JOIN EQUIPMENT eq ON lan.EQ_ID = eq.EID\s
                WHERE serv.SERVICE_ID >17 AND ss.STATE_SGN=1 AND lan.ip is NULL""", new AbonentMapper());
    }

    @Override
    public List<Abonent> getAbonentsWithLan() {

        return jdbc.query(
                """
                        SELECT DISTINCT cust.CUSTOMER_ID, cust.DOGOVOR_NO, cust.SURNAME, cust.FIRSTNAME,\s
                        cust.MIDLENAME, lan.IP as LAN_IP, lan.PORT as LAN_PORT, lan.NOTICE as CLUSTER,\s
                        eq.NAME as EQ_NAME, eq.IP as EQ_IP, eq.MAC as EQ_MAC  FROM CUSTOMER cust\s
                        LEFT JOIN SUBSCR_SERV ss ON CUST.CUSTOMER_ID =ss.CUSTOMER_ID\s
                        LEFT JOIN SERVICES serv ON ss.SERV_ID =SERV.SERVICE_ID\s
                        LEFT JOIN TV_LAN lan ON  cust.CUSTOMER_ID = lan.CUSTOMER_ID\s
                        LEFT JOIN EQUIPMENT eq ON lan.EQ_ID = eq.EID\s
                        WHERE serv.SERVICE_ID >17 AND ss.STATE_SGN=1 and eq.IP is not NULL\s
                        and lan.ip is not NULL\s
                        and lan.ip not like '%1.1.1.1%'""", new AbonentMapper());
    }

    /**
     * Метод возвращает всех абонентов у которых добавлены аттрибуты с указанием коммутатора и порта,
     * но IP адрес не присвоен
     *
     * @return Список абонентов
     */
    @Override
    public List<Abonent> getAbonentsWithWrongLan() {

        return jdbc.query(
                """
                        SELECT DISTINCT cust.CUSTOMER_ID, cust.DOGOVOR_NO, cust.SURNAME, cust.FIRSTNAME,\s
                        cust.MIDLENAME, lan.IP as LAN_IP, lan.PORT as LAN_PORT, lan.NOTICE as CLUSTER,\s
                        eq.NAME as EQ_NAME, eq.IP as EQ_IP, eq.MAC as EQ_MAC  FROM CUSTOMER cust\s
                        LEFT JOIN SUBSCR_SERV ss ON CUST.CUSTOMER_ID =ss.CUSTOMER_ID\s
                        LEFT JOIN SERVICES serv ON ss.SERV_ID =SERV.SERVICE_ID\s
                        left join TV_LAN lan ON  cust.CUSTOMER_ID = lan.CUSTOMER_ID\s
                        left join EQUIPMENT eq ON lan.EQ_ID = eq.EID\s
                        WHERE serv.SERVICE_ID >17 AND ss.STATE_SGN=1 AND eq.IP is not NULL\s
                        and lan.ip is not NULL\s
                        and lan.ip like '%1.1.1.1%'""", new AbonentMapper());

    }

    /**
     * Метод возвращает список абонентов с текущим белым IP адресом в случае если абонент получает статус неактивный.
     * Список формируется для абонентов со сроком неактивности менее определенного количества дней
     *
     * @param daysFromDisableToDelete количество дней до удаления адресов
     * @return Список абонентов для смены IP адреса на 1.1.1.1
     */
    @Override
    public List<Abonent> getAbonentsToDisableIP(int daysFromDisableToDelete) {

// SQL Request for Firebird
//        return jdbc.query(
//                """
//                        SELECT cust.CUSTOMER_ID, cust.DOGOVOR_NO, cust.SURNAME, cust.FIRSTNAME,\s
//                        cust.MIDLENAME, tl.IP as LAN_IP, tl.PORT as LAN_PORT, tl.NOTICE as CLUSTER, eq.NAME as EQ_NAME,\s
//                        eq.IP as EQ_IP, eq.MAC as EQ_MAC FROM CUSTOMER cust\s
//                        LEFT JOIN SUBSCR_SERV ss ON cust.CUSTOMER_ID = ss.CUSTOMER_ID\s
//                        LEFT JOIN services ON ss.SERV_ID = services.SERVICE_ID\s
//                        JOIN TV_LAN tl ON CUST.CUSTOMER_ID =TL.CUSTOMER_ID\s
//                        LEFT JOIN EQUIPMENT eq ON tl.EQ_ID  = EQ.EID\s
//                        WHERE services.SERVICE_ID >17\s
//                        AND eq.IP is not NULL\s
//                        AND tl.ip is not NULL\s
//                        AND tl.IP not like '%1.1.1.1%'\s
//                        AND NOT cust.CUSTOMER_ID IN (\s
//                                SELECT cust.CUSTOMER_ID FROM CUSTOMER cust\s
//                                LEFT JOIN SUBSCR_SERV ss ON CUST.CUSTOMER_ID = ss.CUSTOMER_ID\s
//                                LEFT JOIN services ON ss.SERV_ID = services.SERVICE_ID\s
//                                WHERE services.SERVICE_ID >17 AND ss.STATE_SGN=1)\s
//                        GROUP BY Cust.CUSTOMER_ID, cust.DOGOVOR_NO, cust.SURNAME, cust.FIRSTNAME, cust.MIDLENAME, tl.IP, tl.PORT,\s
//                        tl.NOTICE, eq.NAME, eq.IP, eq.MAC HAVING CURRENT_DATE - MAX(ss.STATE_DATE) <= ?""", new AbonentMapper(), daysFromDisableToDelete);

        // SQL Request for SQLite
        return jdbc.query(
                """
                        SELECT cust.CUSTOMER_ID, cust.DOGOVOR_NO, cust.SURNAME, cust.FIRSTNAME,\s
                        cust.MIDLENAME, tl.IP as LAN_IP, tl.PORT as LAN_PORT, tl.NOTICE as CLUSTER, eq.NAME as EQ_NAME,\s
                        eq.IP as EQ_IP, eq.MAC as EQ_MAC FROM CUSTOMER cust\s
                        LEFT JOIN SUBSCR_SERV ss ON cust.CUSTOMER_ID = ss.CUSTOMER_ID\s
                        LEFT JOIN services ON ss.SERV_ID = services.SERVICE_ID\s
                        JOIN TV_LAN tl ON CUST.CUSTOMER_ID =TL.CUSTOMER_ID\s
                        LEFT JOIN EQUIPMENT eq ON tl.EQ_ID  = EQ.EID\s
                        WHERE services.SERVICE_ID >17\s
                        AND eq.IP is not NULL\s
                        AND tl.ip is not NULL\s
                        AND tl.IP not like '%1.1.1.1%'\s
                        AND NOT cust.CUSTOMER_ID IN (\s
                                SELECT cust.CUSTOMER_ID FROM CUSTOMER cust\s
                                LEFT JOIN SUBSCR_SERV ss ON CUST.CUSTOMER_ID = ss.CUSTOMER_ID\s
                                LEFT JOIN services ON ss.SERV_ID = services.SERVICE_ID\s
                                WHERE services.SERVICE_ID >17 AND ss.STATE_SGN=1)\s
                        GROUP BY Cust.CUSTOMER_ID, cust.DOGOVOR_NO, cust.SURNAME, cust.FIRSTNAME, cust.MIDLENAME, tl.IP, tl.PORT,\s
                        tl.NOTICE, eq.NAME, eq.IP, eq.MAC HAVING (unixepoch(CURRENT_DATE) - unixepoch(MAX(ss.STATE_DATE)))/86400 <= ?""", new AbonentMapper(), daysFromDisableToDelete);
    }

    /**
     * Метод возвращает список абонентов, которым нужно удалить адреса по прошествию определенного количества дней
     *
     * @param daysFromDisableToDelete количество дней до удаления адресов
     * @return Список абонентов для удаления адресов
     */
    @Override
    public List<Abonent> getAbonentsToDeleteAttributes(int daysFromDisableToDelete) {
        // SQL Request for Firebird
//        return jdbc.query(
//                """
//                        SELECT cust.CUSTOMER_ID, cust.DOGOVOR_NO, cust.SURNAME, cust.FIRSTNAME,\s
//                        cust.MIDLENAME, tl.IP as LAN_IP, tl.PORT as LAN_PORT, tl.NOTICE as CLUSTER, eq.NAME as EQ_NAME,\s
//                        eq.IP as EQ_IP, eq.MAC as EQ_MAC FROM CUSTOMER cust\s
//                        LEFT JOIN SUBSCR_SERV ss ON cust.CUSTOMER_ID = ss.CUSTOMER_ID\s
//                        LEFT JOIN services ON ss.SERV_ID = services.SERVICE_ID\s
//                        JOIN TV_LAN tl ON CUST.CUSTOMER_ID =TL.CUSTOMER_ID\s
//                        LEFT JOIN EQUIPMENT eq ON tl.EQ_ID  = EQ.EID\s
//                        WHERE services.SERVICE_ID >17 AND NOT cust.CUSTOMER_ID IN (\s
//                        SELECT cust.CUSTOMER_ID FROM CUSTOMER cust\s
//                        LEFT JOIN SUBSCR_SERV ss ON CUST.CUSTOMER_ID = ss.CUSTOMER_ID\s
//                        LEFT JOIN services ON ss.SERV_ID = services.SERVICE_ID\s
//                        WHERE services.SERVICE_ID >17 AND ss.STATE_SGN=1)\s
//                        GROUP BY Cust.CUSTOMER_ID, cust.DOGOVOR_NO, cust.SURNAME, cust.FIRSTNAME, cust.MIDLENAME,\s
//                        tl.IP, tl.PORT, tl.NOTICE, eq.NAME, eq.IP, eq.MAC\s
//                        HAVING CURRENT_DATE - MAX(ss.STATE_DATE) > ?""",new AbonentMapper(), daysFromDisableToDelete);
        // SQL Request for SQLite
        return jdbc.query(
                """
                        SELECT cust.CUSTOMER_ID, cust.DOGOVOR_NO, cust.SURNAME, cust.FIRSTNAME,\s
                        cust.MIDLENAME, tl.IP as LAN_IP, tl.PORT as LAN_PORT, tl.NOTICE as CLUSTER, eq.NAME as EQ_NAME,\s
                        eq.IP as EQ_IP, eq.MAC as EQ_MAC FROM CUSTOMER cust\s
                        LEFT JOIN SUBSCR_SERV ss ON cust.CUSTOMER_ID = ss.CUSTOMER_ID\s
                        LEFT JOIN services ON ss.SERV_ID = services.SERVICE_ID\s
                        JOIN TV_LAN tl ON CUST.CUSTOMER_ID =TL.CUSTOMER_ID\s
                        LEFT JOIN EQUIPMENT eq ON tl.EQ_ID  = EQ.EID\s
                        WHERE services.SERVICE_ID >17 AND NOT cust.CUSTOMER_ID IN (\s
                        SELECT cust.CUSTOMER_ID FROM CUSTOMER cust\s
                        LEFT JOIN SUBSCR_SERV ss ON CUST.CUSTOMER_ID = ss.CUSTOMER_ID\s
                        LEFT JOIN services ON ss.SERV_ID = services.SERVICE_ID\s
                        WHERE services.SERVICE_ID >17 AND ss.STATE_SGN=1)\s
                        GROUP BY Cust.CUSTOMER_ID, cust.DOGOVOR_NO, cust.SURNAME, cust.FIRSTNAME, cust.MIDLENAME,\s
                        tl.IP, tl.PORT, tl.NOTICE, eq.NAME, eq.IP, eq.MAC\s
                        HAVING (unixepoch(CURRENT_DATE) - unixepoch(MAX(ss.STATE_DATE)))/86400 > ?""", new AbonentMapper(), daysFromDisableToDelete);
    }

    @Override
    public List<Abonent> getAbonentsWithStaticIP() {

        return jdbc.query(
                """
                        SELECT DISTINCT cust.CUSTOMER_ID, cust.DOGOVOR_NO, cust.SURNAME, cust.FIRSTNAME,\s
                        cust.MIDLENAME, lan.IP as LAN_IP, lan.PORT as LAN_PORT, lan.NOTICE as CLUSTER,\s
                        eq.NAME as EQ_NAME, eq.IP as EQ_IP, eq.MAC as EQ_MAC  FROM CUSTOMER cust\s
                        LEFT JOIN SUBSCR_SERV ss ON CUST.CUSTOMER_ID =ss.CUSTOMER_ID\s
                        LEFT JOIN SERVICES serv ON ss.SERV_ID =SERV.SERVICE_ID\s
                        LEFT JOIN TV_LAN lan ON  cust.CUSTOMER_ID = lan.CUSTOMER_ID\s
                        LEFT JOIN EQUIPMENT eq ON lan.EQ_ID = eq.EID\s
                        WHERE serv.SERVICE_ID = 13293926 AND ss.STATE_SGN=1 and eq.IP is not NULL\s
                        and lan.ip is not NULL\s
                        and lan.ip not like '%1.1.1.1%'""", new AbonentMapper());
    }

    /**
     * Метод очищает неиспользуемые IP адреса у выключенных абонента
     *
     * @param abon Изменяемый абонент
     */
    @Override
    public void deleteAttributesToAbonent(Abonent abon) {

        jdbc.update("DELETE FROM TV_LAN WHERE TV_LAN.Customer_id = ?", abon.getCUSTOMER_ID());
    }

    @Override
    public void initIpIfAbonentIsNotLeases(String id) {
        jdbc.update("UPDATE TV_LAN SET  IP = '1.1.1.1', MAC = '', NOTICE = '' " +
                "WHERE  CUSTOMER_ID  = ?", id);
    }

    @Override
    public int checkIPExistance(String IP) {
        var variable = jdbc.queryForObject("SELECT count(r.IP) FROM TV_LAN r where r.IP = ?", String.class, IP);
        if (variable != null) {
            return Integer.parseInt(variable);
        } else {
            return 0;
        }

    }

    /**
     * Метод получает все IP адреса активных абонентов, service.SERVICE_ID != 13293926 - исключает услугу "Статический IP"
     *
     * @return Список всех полученных от базы данных адресов
     */
    @Override
    public List<String> getAllUsedIP() {
        return jdbc.queryForList(
                """
                        SELECT tl.IP FROM CUSTOMER cust
                        LEFT JOIN SUBSCR_SERV ss ON CUST.CUSTOMER_ID = ss.CUSTOMER_ID
                        LEFT JOIN services ON ss.SERV_ID = services.SERVICE_ID
                        LEFT JOIN TV_LAN tl ON  cust.CUSTOMER_ID = tl.CUSTOMER_ID
                        LEFT JOIN EQUIPMENT eq ON tl.EQ_ID = eq.EID
                        WHERE services.SERVICE_ID >17 AND services.SERVICE_ID != 13293926 AND ss.STATE_SGN=1
                        AND eq.IP is not NULL
                        AND tl.ip is not NULL
                        AND tl.ip not like '%1.1.1.1%'""", String.class);

    }

    /**
     * Метод обновляет в базе данных IP адрес, имя Cluster для конкретного абонента
     *
     * @param abonent обновляемый абонент
     */
    @Override
    public void updateAbonentIP(Abonent abonent) {
        jdbc.update("UPDATE TV_LAN  SET IP = ?, NOTICE = ? WHERE CUSTOMER_ID = ?",
                abonent.getLAN_IP().toString(), abonent.getCLUSTER(), abonent.getCUSTOMER_ID());
    }

}

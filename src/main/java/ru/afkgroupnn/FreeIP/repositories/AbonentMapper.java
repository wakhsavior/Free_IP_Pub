package ru.afkgroupnn.FreeIP.repositories;

import lombok.extern.log4j.Log4j;
import org.springframework.jdbc.core.RowMapper;
import ru.afkgroupnn.FreeIP.model.Abonent;
import ru.afkgroupnn.FreeIP.model.IPClass;

import java.sql.ResultSet;
import java.sql.SQLException;

@Log4j
public class AbonentMapper implements RowMapper<Abonent> {
    @Override
    public Abonent mapRow(ResultSet rs, int rowNum) throws SQLException {
        Abonent abonent = new Abonent();
        try {
            abonent.setCUSTOMER_ID(rs.getString("CUSTOMER_ID"));
            abonent.setDOGOVOR_NO(rs.getString("DOGOVOR_NO"));
            abonent.setSURNAME(rs.getString("SURNAME"));
            abonent.setFIRSTNAME(rs.getString("FIRSTNAME"));
            abonent.setMIDLENAME(rs.getString("MIDLENAME"));
            if (!(rs.getString("LAN_IP") == null)){
                abonent.setLAN_IP(new IPClass(rs.getString("LAN_IP")));
            }else {abonent.setLAN_IP(null);}
            if (!(rs.getString("LAN_PORT") == null)){
                abonent.setLAN_PORT(Integer.valueOf(rs.getString("LAN_PORT")));
            }else {abonent.setLAN_PORT(0);}
            abonent.setEQ_NAME(rs.getString("EQ_NAME"));
            if (!(rs.getString("EQ_IP") == null)){
                abonent.setEQ_IP(new IPClass(rs.getString("EQ_IP")));
            }else {abonent.setEQ_IP(null);}
            abonent.setEQ_MAC(rs.getString("EQ_MAC"));
            abonent.setCLUSTER(rs.getString("CLUSTER"));
        } catch (Exception ex){
            LOGGER.error("Ошибка в преобразовании в экземпляра для Абонента: ");
            LOGGER.error("Dogovor num: " + abonent.getDOGOVOR_NO() + "; Cust_num: " + abonent.getCUSTOMER_ID() +
                    "; FIO: " + abonent.getSURNAME() + " " + abonent.getFIRSTNAME() + " " + abonent.getMIDLENAME());
            LOGGER.error(ex.getMessage());
        }
        return abonent;
    }
}

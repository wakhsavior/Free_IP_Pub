package ru.afkgroupnn.FreeIP.model;


import lombok.Data;

import java.util.Date;
import java.util.Objects;

@Data
public class Abonent {

    private String CUSTOMER_ID;
    private String DOGOVOR_NO;
    private String SURNAME;
    private String FIRSTNAME;
    private String MIDLENAME;
    private IPClass LAN_IP;
    private int LAN_PORT;
    private String EQ_NAME;
    private IPClass EQ_IP;
    private String EQ_MAC;
    private IPClass LEASES_IP;
    private String LEASES_MAC;
    private IPClass LEASES_EQUIPMENT_IP;
    private int LEASES_EQUIPMENT_PORT;
    private Date DATE_START_RENT;
    private Date DATE_END_RENT;
    private String CLUSTER;

    @Override
    public String toString() {
        return "Abonent{" +
                "CUSTOMER_ID='" + CUSTOMER_ID + '\'' +
                ", DOGOVOR_NO='" + DOGOVOR_NO + '\'' +
                ", FIRSTNAME='" + FIRSTNAME + '\'' +
                ", CLUSTER='" + CLUSTER + '\'' +
                ", LAN_IP='" + LAN_IP + '\'' +
                ", LAN_PORT='" + LAN_PORT + '\'' +
                ", EQ_IP='" + EQ_IP + '\'' +
                ", LEASE_IP='" + LEASES_IP + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Abonent abonent = (Abonent) o;
        return Objects.equals(CUSTOMER_ID, abonent.CUSTOMER_ID) && Objects.equals(DOGOVOR_NO, abonent.DOGOVOR_NO);
    }

}

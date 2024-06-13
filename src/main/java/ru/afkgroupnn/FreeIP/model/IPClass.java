package ru.afkgroupnn.FreeIP.model;

import lombok.Data;
import lombok.extern.log4j.Log4j;
import ru.afkgroupnn.FreeIP.Exceptions.WrongInputDataException;

import java.util.ArrayList;
import java.util.List;


@Data
@Log4j
public class IPClass {

    private int intIP;

    public static IPClass createIP(String s) throws WrongInputDataException {
        IPClass ip = new IPClass();
        ip.setIP(s);
        return ip;
    }

    public static IPClass createIP(int intIP) {
        IPClass ip = new IPClass();
        ip.setIP(intIP);
        return ip;
    }

    public static List<IPClass> createIP(List<String> strIPs) {
        List<IPClass> IPs = new ArrayList<>();
        for (String strIP : strIPs) {
            try {
                IPs.add(createIP(strIP));
            } catch (WrongInputDataException ex){
                LOGGER.error("Некорректный IP адрес");
                LOGGER.error(ex.getMessage());
            }
        }
        return IPs;
    }

    public IPClass(String ip) throws WrongInputDataException {
        setIP(ip);
    }

    public IPClass() {
    }


    public void setIP(int intIP) {
        this.intIP = intIP;
    }

    public void setIP(String string) throws WrongInputDataException {
        if (string == null) {
            intIP = 0;
            return;
        }
        string = string.strip();
        String[] octetsString = string.split("\\.");
        if (octetsString.length != Integer.BYTES) {
            throw new WrongInputDataException("Недопустимая длина массива для: " + string + " - " + String.valueOf(octetsString.length));
        }
        for (int i = 0; i < octetsString.length; i++) {
            int octet = Integer.parseInt(octetsString[i]);
            if (octet < 0 || octet > 255) {
                throw new WrongInputDataException("Недопустимое значение в массиве: " + string + " - " + octetsString[i]);
            }
            intIP = intIP + (octet << (Byte.SIZE * (Integer.BYTES - i - 1)));

        }
    }

    /**
     * Метод проверки IP адреса на соответствие шаблону
     *
     * @param pattern *.*.*.* , 192.168.1.0-255 , *
     *                <code>address = 10.2.88.12  pattern = *.*.*.*   result: true
     *                address = 10.2.88.12  pattern = *   result: true
     *                address = 10.2.88.12  pattern = 10.2.88.12-13   result: true
     *                address = 10.2.88.12  pattern = 10.2.88.13-125   result: false
     * @return true если адрес соответствует шаблону
     */
    public boolean checkIPMatching(String pattern) {
        if (pattern.equals("*.*.*.*") || pattern.equals("*"))
            return true;

        String[] mask = pattern.split("\\.");
        int[]  ip_address = this.getIPOctets();
        for (int i = 0; i < mask.length; i++) {
            if (mask[i].equals("*") || mask[i].equals(String.valueOf(ip_address[i])))
                continue;
            else if (mask[i].contains("-")) {
                int min = Integer.parseInt(mask[i].split("-")[0]);
                int max = Integer.parseInt(mask[i].split("-")[1]);
                int ip = ip_address[i];
                if (ip < min || ip > max)
                    return false;
            } else
                return false;
        }
        return true;
    }
    public int[] getIPOctets(){
        int[] octets = new int[4];
        for (int i = 0; i < Integer.BYTES; i++) {
            octets[i] = intIP << (Byte.SIZE * i) >>> (Byte.SIZE * (Integer.BYTES - 1));
        }
        return octets;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IPClass ipClass = (IPClass) o;
        return intIP == ipClass.intIP;
    }

    @Override
    public String toString() {
        StringBuilder stringIP = new StringBuilder();

        for (int i = 0; i < Integer.BYTES; i++) {
            stringIP.append(Integer.toString(intIP << (Byte.SIZE * i) >>> (Byte.SIZE * (Integer.BYTES - 1))));
            stringIP.append(".");
        }
        stringIP.deleteCharAt(stringIP.length() - 1);
        return stringIP.toString();
    }
}
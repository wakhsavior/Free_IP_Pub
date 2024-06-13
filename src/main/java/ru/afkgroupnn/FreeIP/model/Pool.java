package ru.afkgroupnn.FreeIP.model;

import lombok.Data;

import ru.afkgroupnn.FreeIP.Exceptions.UserException;
import ru.afkgroupnn.FreeIP.Exceptions.WrongInputDataException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
public class Pool {

    private String pool;
    private String mask;
    private int startOctet;
    private int stopOctet;
    private int sizePool;
    private int freeSizePool;
    private List<IPClass> arrayFreeIp;


    private Pool(
            String pool,
            String mask,
            int startOctet,
            int stopOctet,
            int sizePool,
            int freeSizePool,
            List<IPClass> arrayFreeIp) throws WrongInputDataException {
        this.pool = pool;
        this.mask = mask;
        this.startOctet = startOctet;
        this.stopOctet = stopOctet;
        this.sizePool = sizePool;
        this.freeSizePool = freeSizePool;
        this.arrayFreeIp = arrayFreeIp;

    }

    public static Pool createPool(String poolData) throws WrongInputDataException {
        Pool pool;
        String regexp = "(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})-(\\d{1,3})";
        try {
            Pattern pattern = Pattern.compile(regexp);
            Matcher matcher = pattern.matcher(poolData);
            List<Integer> ip_octets = new ArrayList<>();
            if (matcher.find()) {
                for (int i = 1; i <= matcher.groupCount(); i++) {
                    Integer octet = Integer.parseInt(matcher.group(i));
                    if (octet < 0 || octet > 255) {
                        throw new WrongInputDataException("Октет должен быть между 0..255 -> " + poolData);
                    }
                    ip_octets.add(octet);
                }
            } else {
                throw new WrongInputDataException("Входные данные не соответствуют 172.16.0.0-254 выражению -> " + poolData);
            }
            String mask = ip_octets.get(0) + "." + ip_octets.get(1) + "." + ip_octets.get(2) + ".";
            List<IPClass> arrayFreeIp = new ArrayList<>();
            int startOctet = ip_octets.get(3);
            int stopOctet = ip_octets.get(4);
            String FreeIpString = mask;
            int poolSize = stopOctet - startOctet + 1;
            for (int i = startOctet; i <= stopOctet; i++) {
                IPClass FreeIp = new IPClass(FreeIpString + String.valueOf(i));
                arrayFreeIp.add(FreeIp);
            }
            pool = new Pool(poolData, mask,startOctet,stopOctet,
                    poolSize, poolSize, arrayFreeIp);
            return pool;

        } catch (Exception ex) {
            throw new WrongInputDataException("Ошибка в создании пула: " + ex.getMessage());
        }
    }

    public void setArrayFreeIp(List<IPClass> IPs) throws WrongInputDataException {
        int count = IPs.size();
        if (count > sizePool) {
            throw new WrongInputDataException("Лист свободных IP {" + count + "} превышает размер пула: " +
                    pool + ": " + sizePool + "\t" + IPs);
        }
        freeSizePool = count;
        arrayFreeIp = IPs;
    }

    public IPClass useFreeIPFromArray(String IP) throws WrongInputDataException {
        IPClass ip = IPClass.createIP(IP);
        return useFreeIPFromArray(ip);
    }

    public IPClass useFreeIPFromArray(IPClass IP) throws WrongInputDataException {
        if (arrayFreeIp.contains(IP)) {
            arrayFreeIp.remove(IP);
            freeSizePool--;
        } else {
            throw new WrongInputDataException(IP.toString() + " IP уже используется.");
        }
        return IP;
    }

    public IPClass useFreeIPFromArray() throws UserException {
        if (!arrayFreeIp.isEmpty()) {
            IPClass IP = arrayFreeIp.stream().findFirst().get();
            return useFreeIPFromArray(IP);
        } else throw new WrongInputDataException("В массиве нет свободных адресов.");
    }

    public void addFreeIPToArray(IPClass IP) throws UserException {
        if (!IP.checkIPMatching(getPool())) {
            throw new WrongInputDataException(IP.toString() + " адрес не из пула " + getPool());
        }
        if (!arrayFreeIp.contains(IP)) {
            arrayFreeIp.add(IP);
            freeSizePool++;
        } else {
            throw new WrongInputDataException(IP.toString() + " IP Уже свободен и присутствует в пуле.");
        }
    }


    @Override
    public String toString() {
        return "\tpool IP = " + pool + "\n" +
                "\t\tmask = " + mask + "\n" +
                "\t\tstartOctet = " + startOctet + "\n" +
                "\t\tstopOctet = " + stopOctet + "\n" +
                "\t\tsizePool = " + sizePool + "\n" +
                "\t\tfreeSizePool = " + freeSizePool + "\n";
    }
}

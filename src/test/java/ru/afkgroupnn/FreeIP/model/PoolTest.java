package ru.afkgroupnn.FreeIP.model;


import org.junit.Test;
import ru.afkgroupnn.FreeIP.Exceptions.WrongInputDataException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class PoolTest {
    @Test
    public void createPool() {
        Pool pool;

        try {
            pool = Pool.createPool("172.16.0.0-254");
        } catch (WrongInputDataException e) {
            throw new RuntimeException(e);
        }
        assertEquals(255, pool.getSizePool());

        var result = assertThrows(WrongInputDataException.class, () -> {
            Pool pool1 = Pool.createPool("172.16.0");
        });
        System.out.println(result);
        result = assertThrows(WrongInputDataException.class, () -> {
            Pool pool1 = Pool.createPool("172.16.2456.0-254");
        });
        System.out.println(result);
        result = assertThrows(WrongInputDataException.class, () -> {
            Pool pool1 = Pool.createPool("172.16.0.1");
        });
        System.out.println(result);
        result = assertThrows(WrongInputDataException.class, () -> {
            Pool pool1 = Pool.createPool("172.16.288.1-234");
        });
        System.out.println(result);
        try {
            pool = Pool.createPool("86.27.227.2-254");
        } catch (WrongInputDataException e) {
            throw new RuntimeException(e);
        }
        assertEquals(253, pool.getFreeSizePool());
        assertEquals(253, pool.getSizePool());
    }
}

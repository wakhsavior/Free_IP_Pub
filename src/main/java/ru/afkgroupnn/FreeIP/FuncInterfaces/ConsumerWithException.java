package ru.afkgroupnn.FreeIP.FuncInterfaces;

import ru.afkgroupnn.FreeIP.Exceptions.UserException;

@FunctionalInterface
public interface ConsumerWithException<T>{
    void accept(T t) throws UserException;
}

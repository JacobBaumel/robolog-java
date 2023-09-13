package com.radicubs;

import com.radicubs.logger.Log;
import com.radicubs.logger.Loggable;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.function.Supplier;

public class Main {

    public static void main(String[] args) {
        SupplierClass e = new SupplierClass();
        grabandlog(e);
    }

    public static void grabandlog(Object o) {
        Arrays.stream(o.getClass().getMethods())
                .filter(m -> m.isAnnotationPresent(Log.class))
                .filter(m -> m.getReturnType() == Supplier.class)
                .map(m -> {
                    try {
                        return (Supplier<?>) m.invoke(o);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                })
                .filter(s -> s.get() instanceof Loggable)
                .map(s -> (Supplier<Loggable>) s)
                .map(Supplier::get)
                .forEach(l -> System.out.println(l.level() + ": " + l.log()));
    }

}

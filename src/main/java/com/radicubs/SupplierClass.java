package com.radicubs;

import com.radicubs.logger.Log;
import com.radicubs.logger.LogGroup;
import com.radicubs.logger.LogLevel;
import com.radicubs.logger.Loggable;

import java.util.function.Supplier;

@LogGroup(groupName="")
public class SupplierClass {

    private int i;

    @Log
    @LogGroup(groupName="eee")
    public Supplier<Loggable> logggg() {
        return () -> new Loggable() {
            @Override
            public String log() {
                return "hi";
            }

            @Override
            public LogLevel level() {
                return LogLevel.INFO;
            }
        };
    }
}

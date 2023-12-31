package com.google.common.util.concurrent;

import java.lang.Thread;
import java.util.logging.Level;
import java.util.logging.Logger;

final class UncaughtExceptionHandlers$Exiter implements Thread.UncaughtExceptionHandler {
    private static final Logger logger = Logger.getLogger(UncaughtExceptionHandlers$Exiter.class.getName());

    /* renamed from: wO */
    private final Runtime f2569wO;

    public void uncaughtException(Thread thread, Throwable th) {
        try {
            logger.log(Level.SEVERE, String.format("Caught an exception in %s.  Shutting down.", new Object[]{thread}), th);
        } catch (Throwable th2) {
            this.f2569wO.exit(1);
            throw th2;
        }
        this.f2569wO.exit(1);
    }
}

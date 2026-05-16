package com.dadaotiantian.memorize.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LogUtil {
    private LogUtil() {
    }

    public static Logger getLogger(Class<?> type) {
        return LoggerFactory.getLogger(type);
    }
}

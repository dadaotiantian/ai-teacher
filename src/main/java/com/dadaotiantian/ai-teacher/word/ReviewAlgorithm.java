package com.dadaotiantian.memorize.word;

import java.util.HashMap;
import java.util.Map;

public final class ReviewAlgorithm {
    private ReviewAlgorithm() {
    }

    public static Map<String, Object> next(Map<String, Object> data, boolean correct) {
        double ef = number(data.get("easiness_factor"), 2.5);
        int repetition = (int) number(data.get("repetition"), 0);
        int interval = (int) number(data.get("interval"), 0);
        if (correct) {
            repetition++;
            ef = Math.min(2.5, ef + 0.1);
        } else {
            repetition = 0;
            ef = Math.max(1.3, ef - 0.2);
        }
        if (repetition <= 1) {
            interval = 1;
        } else if (repetition == 2) {
            interval = 6;
        } else {
            interval = Math.max(1, (int) Math.round(interval * ef));
        }
        Map<String, Object> result = new HashMap<>(data);
        result.put("easiness_factor", ef);
        result.put("repetition", repetition);
        result.put("interval", interval);
        return result;
    }

    private static double number(Object value, double defaultValue) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        if (value == null) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }
}

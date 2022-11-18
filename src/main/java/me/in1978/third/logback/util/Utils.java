package me.in1978.third.logback.util;

import java.lang.reflect.Field;
import java.util.List;
import java.util.function.Predicate;

public class Utils {

    public static <T> T ognlGet(Object obj, String expr) {
        if (obj == null) {
            return null;
        }

        String[] arr = expr.split("\\.");
        if (arr.length > 1) {
            Object o = obj;
            for (String expr1 : arr) {
                o = ognlGet(o, expr1);
            }

            return (T) o;
        }

        Field field = getField(obj.getClass(), expr);
        if (field == null) {
            throw new RuntimeException("bad expr:" + expr);
        }
        field.setAccessible(true);
        try {
            return (T) field.get(obj);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

    }


    private static Field getField(Class<?> c, String name) {
        for (Class<?> c2 = c; c2 != Object.class; c2 = c2.getSuperclass()) {
            try {
                return c2.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
            }
        }

        return null;
    }


    public static <T> int indexOf(List<T> list, Predicate<T> predicate) {
        for (int i = 0; i < list.size(); i++) {
            if (predicate.test(list.get(i))) {
                return i;
            }
        }

        return -1;
    }

}

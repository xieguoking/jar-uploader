package xyz.xieguoking;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author xieguoking
 * @author (2021 / 4 / 8 add by xieguoking
 * @version 1.0
 * @since 1.0
 */
public class Utils {

    public static final String getStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    public static final void setFieldValue(Object bean, String name, Object value) {
        try {
            final Field fileFiled = getField(bean.getClass(), name);
            if (!fileFiled.isAccessible()) {
                fileFiled.setAccessible(true);
            }
            fileFiled.set(bean, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static final Field getField(Class clazz, String name) {
        try {
            return clazz.getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            if (clazz.getSuperclass() != Object.class) {
                return getField(clazz.getSuperclass(), name);
            }
            throw new RuntimeException(e);
        }
    }

    public static final <K, V> Map<K, V> mapOf(K k, V v) {
        Map<K, V> result = new LinkedHashMap<>(1);
        result.put(k, v);
        return result;
    }
}

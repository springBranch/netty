package com.qbao.netty.conf;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Properties;

/**
 * @author song.j
 * @create 2017-10-20 17:17:21
 **/
public abstract class AbstractConfig {

    public abstract Map<String, String> getProperties();

    public AbstractConfig() {
    }

    public String get(String key, String defaultValue) {
        String value = getProperties().get(key);
        if (value == null) {
            return defaultValue;
        }
        return value.trim();
    }

    public int getInt(String key, int defaultValue) {
        String value = get(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public long getLong(String key, long defaultValue) {
        String value = get(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Long.parseLong(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public float getFloat(String key, float defaultValue) {
        String value = get(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Float.parseFloat(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        String value = get(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Boolean.parseBoolean(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public String get(String key) {
        String value = getProperties().get(key);
        if (value == null) {
            return null;
        } else {

            try {
                return new String(value.getBytes("ISO-8859-1"), "utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public Properties copy() {
        Properties p = new Properties();
        for (Map.Entry<String, String> entry : getProperties().entrySet()) {
            p.put(entry.getKey(), entry.getValue());
        }
        return p;
    }

    public void set(String key, String value) {
        getProperties().put(key, value);
    }

}

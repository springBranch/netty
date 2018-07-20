package com.qbao.netty.conf;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 配置文件加载至内存
 * <p>
 * <p>
 * baseConfig 不会定时间加载
 * <p>
 * config 定时加载
 */
public class Config extends AbstractConfig {

    final Map<String, String> kvPairs = new ConcurrentHashMap();

    final File confFile;

    Config(String confPath) {
        confFile = new File(confPath);
    }

    void load() throws IOException {
        InputStream is = null;
        try {
            is = new FileInputStream(confFile);
            Properties properties = new Properties();
            properties.load(is);

            for (Entry<Object, Object> entry : properties.entrySet()) {
                kvPairs.put((String) entry.getKey(), (String) entry.getValue());
            }
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    @Override
    public Map<String, String> getProperties() {
        return kvPairs;
    }

    public void releaseSource() throws IOException {
        // sub class should do something
    }

    private static Config BASE_CONFIG;

    public static final Config getBase() {
        try {
            if (BASE_CONFIG == null) {
                synchronized (Config.class) {

                    String basePath = System.getProperty(LoadValues.BASE_CONFIG_PATH);

                    if (basePath == null){
                        throw new NullPointerException("base.config.path is null");
                    }

                    BASE_CONFIG = new Config(System.getProperty(LoadValues.BASE_CONFIG_PATH));

                    System.out.println("base.config.path = " + System.getProperty(LoadValues.BASE_CONFIG_PATH, ""));

                    if (!BASE_CONFIG.confFile.exists()) {
                        System.out.println("base.config.path not found");
                    } else {
                        BASE_CONFIG.load();
                    }
                }

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BASE_CONFIG;
    }

    private static Config CONFIG;

    public static final Config get() {
        if (CONFIG == null) {
            synchronized (DefaultConfig.class) {
                if (CONFIG == null) {
                    CONFIG = new DefaultConfig();
                }
            }
        }
        return CONFIG;
    }

    private static class DefaultConfig extends Config {

        long lastModified = -1;

        Timer timer = new Timer();

        DefaultConfig() {
            super(System.getProperty(LoadValues.LOAD_CONFIG_PATH, ""));

            System.out.println("load.config.path = " + System.getProperty(LoadValues.LOAD_CONFIG_PATH, ""));

            if (!confFile.exists()) {
                System.out.println("load.config.path not found");
                return;
            }
            load();

            timer.schedule(new TimerTask() {

                @Override
                public void run() {
                    load();
                }

            }, 1000, 10 * 1000);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> timer.cancel()));
        }

        @Override
        void load() {
            try {
                long newLastModified = confFile.lastModified();
                if (newLastModified > 0 && newLastModified != lastModified) {
                    super.load();
                    lastModified = newLastModified;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void releaseSource() throws IOException {
            timer.cancel();
        }

    }

}

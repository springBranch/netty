package com.qbao.netty.conf;

import java.io.*;
import java.sql.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * 数据库加载至内存
 */

public class DbConfig extends AbstractConfig {

    private static DbConfig SINGLETON;

    private Config configBase;

    private String dbConnect;
    private String dbUserName;
    private String dbPassword;

    private String oldSource;

    private ScheduledExecutorService service;
    private Connection con = null;

    /**
     * 所有properties，放入内存，定时刷新
     */
    public static Map<String, String> properties = new HashMap();

    /***************************************************************
     * @method�?get<br>
     * <br>
     * @return
     * @throws FileNotFoundException
     * <br>
     */
    public static DbConfig get() throws Exception {
        synchronized (DbConfig.class) {
            if (SINGLETON == null) {
                if (!Config.getBase().getBoolean("deploy.server.config", false))
                    SINGLETON = new EmptyLoadConfig();
                else
                    SINGLETON = new DbConfig();
            }
        }
        return SINGLETON;

    }

    @Override
    public Map<String, String> getProperties() {
        return properties;
    }


    /***************************************************************
     * @Constructor�?LoadConfig.java - LoadConfig<br>
     * <br>
     * @throws FileNotFoundException
     * <br>
     */
    private DbConfig() throws Exception {
        configBase = Config.getBase();
        this.dbConnect = configBase.get(LoadValues.CONFIG_CONNECTION);
        this.dbUserName = configBase.get(LoadValues.SEARCHDB_USERNAME).trim();
        this.dbPassword = configBase.get(LoadValues.SEARCHDB_PASSWORD).trim();

        this.oldSource = "";
        this.service = Executors.newScheduledThreadPool(2);
        int intervalTime = configBase.getInt(LoadValues.LOAD_CONFIG_TIME, 300);


        if (intervalTime > 0)
            service.scheduleWithFixedDelay(new LoadTask(), 0, intervalTime, TimeUnit.SECONDS);
        else
            service.execute(new LoadTask());

    }

    private void loadPropertiesTable() {
        StringBuffer source = new StringBuffer("\n");
        synchronized (this) {
            Statement stmt = null;
            ResultSet rs = null;
            try {
                if (con == null) {
                    con = DriverManager.getConnection(dbConnect, dbUserName, dbPassword);
                }
                stmt = con.createStatement();

                if (LoadValues.QUERY_SQL == null)
                    return;

                rs = stmt.executeQuery(configBase.get(LoadValues.QUERY_SQL));

                while (rs.next()) {
                    String key = rs.getString("property_name");
                    String value = rs.getString("property_value");
                    properties.put(key, value);
                    String comment = rs.getString("remark");
                    if (comment != null && !comment.equalsIgnoreCase(""))
                        source.append("#").append(comment).append("\n");
                    source.append(key).append(" = ").append(value).append("\n\r");
                }
                rs.close();
                stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
                try {
                    con.close();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
                con = null;
            } finally {
                rs = null;
                stmt = null;
            }
        }
        String newSource = source.toString();
        System.out.println(new Date().toString() + "生成配置文件！");
        if (!newSource.equals("\n") && !oldSource.equalsIgnoreCase(newSource)) {
            try {
                doFile(LoadValues.FILE_PATH + LoadValues.TABLE_FILE, newSource);
                oldSource = newSource;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * <br>
     * Copyright 2012 Ctrip.com, Inc. All rights reserved.<br>
     * Ctrip.com PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
     * <br>
     * Projet Name:	Arch.Search.Common<br>
     * File Name:	LoadConfig.java<br>
     * <p>
     * Author:      MENG Wenyuan (wymeng@ctrip.com)<br>
     * Create Date: 2011-12-20<br>
     * Version:		1.0<br>
     * Modification:<br><br>
     */
    class LoadTask implements Runnable {
        /**
         * 自动更新数据库配置到内存
         */
        public void run() {
            try {
                loadPropertiesTable();
            } catch (Exception e) {
                //suppress it so that subsequent task won't be suppressed
                e.printStackTrace();
            }
        }
    }

    /***************************************************************
     * @method�?doFile<br>
     * <br>
     * @param source
     * <br>
     */
    private void doFile(String fileName, String source) throws IOException {
        FileOutputStream out = null;
        BufferedOutputStream Buff = null;
        source = "#" + (new Date()) + "\n\r" + source;
        out = new FileOutputStream(new File(fileName));

        Buff = new BufferedOutputStream(out);
        long begin0 = System.currentTimeMillis();
        Buff.write(source.getBytes());

        Buff.flush();
        Buff.close();
        long end0 = System.currentTimeMillis();
        System.out.println(fileName + " 执行耗时:" + (end0 - begin0) + " 豪秒");
    }


    /***************************************************************
     * @method： releaseSource<br>
     * <br><br>
     * <br>
     */
    public void releaseSource() {
        service.shutdownNow();
    }

    static class EmptyLoadConfig extends DbConfig {
        public EmptyLoadConfig() throws Exception {
            super(false);
        }
    }

    private DbConfig(boolean isDeploy) throws Exception {
        con = null;
    }
}


		 

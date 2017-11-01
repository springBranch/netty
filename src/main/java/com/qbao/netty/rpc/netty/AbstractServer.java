package com.qbao.netty.rpc.netty;

import com.qbao.log.QbLogger;
import com.qbao.log.QbLoggerManager;
import com.qbao.netty.conf.Config;
import com.qbao.netty.rpc.Server;
import com.qbao.netty.util.NetworkUtils;
import com.qbao.netty.util.StringEntry;
import com.qbao.netty.util.xml.XMLHelper;
import org.dom4j.Document;
import org.dom4j.Element;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.ChannelGroupFuture;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Description
 * @Copyright Copyright (c)2011
 * @Company ctrip.com
 * @Author li_yao
 * @Version 1.0
 * @Create-at 2011-8-5 09:59:12
 * @Modification-history <br>Date					Author		Version		Description
 * <br>----------------------------------------------------------
 * <br>2011-8-5 09:59:12  	li_yao		1.0			Newly created
 */
public abstract class AbstractServer implements Server {
    private static QbLogger logger = QbLoggerManager.getLogger(AbstractServer.class);

    protected String serverName;

    protected String serverAddress;

    protected final ChannelGroup allChannels =
            new DefaultChannelGroup(getClass().getName());

    /**
     * -1 represent that this server hasn't been started
     */
    protected int port = -1;

    protected final ServerBootstrap bootstrap;

    public AbstractServer(String serverName) {
        this.serverName = serverName;
        bootstrap = new ServerBootstrap();
        bootstrap.setFactory(new NioServerSocketChannelFactory(
                new ThreadPoolExecutor(
                        Config.get().getInt("server.boss.executor.core.pool.size",
                                Runtime.getRuntime().availableProcessors() * 2),
                        Config.get().getInt("server.boss.executor.max.pool.size",
                                10000),
                        60L,
                        TimeUnit.SECONDS,
                        new SynchronousQueue<Runnable>()
                ),

                new ThreadPoolExecutor(
                        Config.get().getInt("server.ioworker.executor.core.pool.size",
                                Runtime.getRuntime().availableProcessors() * 2),
                        Config.get().getInt("server.ioworker.executor.max.pool.size",
                                10000),
                        60L,
                        TimeUnit.SECONDS,
                        new SynchronousQueue<Runnable>()
                )
        ));

        ChannelCollectablePipelineFactory pipelineFactory =
                createPipelineFactory();
        pipelineFactory.setAllChannels(allChannels);
        bootstrap.setPipelineFactory(pipelineFactory);

        bootstrap.setOption("child.tcpNoDelay", true);
        bootstrap.setOption("child.keepAlive", true);
    }

    @Override
    public void init() throws Exception {

    }

    public abstract ChannelCollectablePipelineFactory createPipelineFactory();

    public static class StartedException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public StartedException(Object Server) {
            super(Server + " has been started!");
        }

    }

    public static class UnStartedException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public UnStartedException(Object Server) {
            super(Server + " hasn't been started!");
        }

    }

    protected boolean isStarted() {
        return port != -1;
    }

    protected void ensureStarted() {
        if (!isStarted()) {
            throw new UnStartedException(this);
        }
    }

    protected void ensureUnStarted() {
        if (isStarted()) {
            throw new StartedException(this);
        }
    }

    @Override
    public int getPort() {
        ensureStarted();
        return port;
    }

    @Override
    public void start(int port) throws IOException {
        start(port, false);
    }

    @Override
    public void start(int port, boolean tryMode) throws IOException {

        ensureUnStarted();

        Channel ch;
        do {
            try {
                ch = bootstrap.bind(new InetSocketAddress(port));
                break;
            } catch (Exception e) {

                if (tryMode && e.getCause() instanceof BindException) {
                    logger.warn(e);
                    port++;
                } else {
                    logger.error(e);
                    throw new RuntimeException(e);
                }
            }
        } while (true);
        this.port = port;
        allChannels.add(ch);
        this.serverAddress = NetworkUtils.getLocalIP() + ":" + port;
        logger.info("{} started at:{}", serverName, port);

    }

    @Override
    public void stop() throws Exception {
        ChannelGroupFuture future = allChannels.close();
        future.awaitUninterruptibly();
        bootstrap.getFactory().releaseExternalResources();
        logger.info("{} stopped at:{}", serverName, port);
    }

    @Override
    public void getMonitorInfos(String prefix, Map<String, Object> map) {
        prefix += AbstractServer.class.getSimpleName() + ".";
//		for(Map.Entry<String, String> entry:SigarService.stats().entrySet()) {
//			map.put(prefix + entry.getKey(), entry.getValue());
//		}
        map.put(prefix + "serverAddress", serverAddress);
        map.put(prefix + "serverName", serverName);
        map.put(prefix + "port", port);
        map.put(prefix + "isStarted()", isStarted());
        map.put(prefix + "last5MinutesWarnLogNum",
                5);
        map.put(prefix + "last5MinutesErrorLogNum",
                5);
    }

    public List<StringEntry> getMonitorInfos(boolean forAlerting,
                                             String keyPattern) {
        Map<String, Object> map = new TreeMap<String, Object>();
        getMonitorInfos("main.server#", map);
        if (forAlerting) {
            if (keyPattern == null || keyPattern.isEmpty()) {
                keyPattern = Config.get().get("alerting.key.pattern",
                        ".*last5MinutesWarnLogNum$|.*last5MinutesErrorLogNum$|.*lastRebuildTime$|.*lastRebuildFailedEngines$|.*lastRebuildDocsNum$|.*lastScrapeStartTime$|.*lastScrapeEndTime$|.*lastScrapeFailedEngines$|.*lastScrapeDocsNum$|.*constantDocsNum$|.*realtimeDocsNum$|.*indexReadersNum$|.*searchCount$|.*lastMinuteSearchAvgTime$|.*lastMinuteSearchNum$|.*last5MinutesFailedSearchNum$");
            }
        }
        List<StringEntry> entrys = new ArrayList<StringEntry>(map.size());
        for (Entry<String, Object> entry : map.entrySet()) {
            if (keyPattern == null || entry.getKey().matches(keyPattern)) {
                String name = entry.getKey();
                if (forAlerting) {//rewrite to brief key
                    name = "framework.searchengine" +
                            name.substring(name.lastIndexOf('.'));
                }
                entrys.add(new StringEntry(name, String.valueOf(entry.getValue())));
            }
        }
        return entrys;
    }

    public String getMonitorInfos(boolean forAlerting, String keyPattern,
                                  String respFormat, String sep) throws IOException {
        List<StringEntry> entrys = getMonitorInfos(forAlerting, keyPattern);
        if (respFormat == null) {
            respFormat = "";
        }
        if (respFormat.equalsIgnoreCase("json")) {
            StringBuilder sb = new StringBuilder(entrys.size() * 100);
            sb.append("{metrics:[");
            for (StringEntry entry : entrys) {
                sb.append("{key:'");
                sb.append(entry.name);
                sb.append("',");
                sb.append("value:'");
                sb.append(entry.value);
                sb.append("'},");
            }
            if (entrys.size() > 0) {
                sb.setLength(sb.length() - 1);
            }
            sb.append("]}");
            return sb.toString();
        } else if (respFormat.equalsIgnoreCase("xml")) {
            Document d = XMLHelper.create("Monitor");
            Element monitor = d.getRootElement();
            for (StringEntry entry : entrys) {
                monitor.addElement(entry.name).addText(entry.value);
            }
            return XMLHelper.toString(d);
        } else {
            if (sep == null || sep.isEmpty()) {
                sep = "<br />";
            }
            StringBuilder sb = new StringBuilder(10 * 1024);
            for (StringEntry entry : entrys) {
                sb.append(entry.name);
                sb.append(':');
                sb.append(entry.value);
                sb.append(sep);
            }
            return sb.toString();
        }
    }
}

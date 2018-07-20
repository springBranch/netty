package com.qbao.netty.rpc.netty;

import com.qbao.log.QbLogger;
import com.qbao.log.QbLoggerManager;
import com.qbao.netty.rpc.Server;
import com.qbao.netty.util.NetworkUtils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;


public abstract class AbstractServer implements Server {
    private static QbLogger logger = QbLoggerManager.getLogger(AbstractServer.class);

    protected String serverName;

    protected String serverAddress;

    //    protected final ChannelGroup allChannels ;
    EventLoopGroup bossGroup = new NioEventLoopGroup();

    EventLoopGroup workerGroup = new NioEventLoopGroup(4);

    /**
     * -1 represent that this server hasn't been started
     */
    protected int port = -1;

    protected final ServerBootstrap bootstrap;

    public AbstractServer(String serverName) {
        this.serverName = serverName;
        bootstrap = new ServerBootstrap();
//        allChannels =   new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
//                .handler(new LoggingHandler(LogLevel.WARN))
                .childHandler(createPipelineFactory());



//        bootstrap.option(ChannelOption.TCP_NODELAY, true);
//        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
    }

    @Override
    public void init() throws Exception {

    }

    public abstract ChannelHandler createPipelineFactory();

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

        ChannelFuture ch;
        do {
            try {

                ch = bootstrap.bind(new InetSocketAddress(port)).sync();

                ch.channel().closeFuture().sync();

                break;
            } catch (Exception e) {

                if (tryMode && e.getCause() instanceof BindException) {
                    e.printStackTrace();
                    port++;
                } else {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            } finally {

                bossGroup.shutdownGracefully();

                workerGroup.shutdownGracefully();
            }
        } while (true);


        this.port = port;
//        allChannels.add(ch);
        this.serverAddress = NetworkUtils.getLocalIP() + ":" + port;
        logger.info("{} started at:{}", serverName, port);

//        allChannels.close().awaitUninterruptibly();
    }

    @Override
    public void stop() throws Exception {
//        ChannelGroupFuture future = allChannels.close();
//        future.awaitUninterruptibly();
        // TODO: 17/11/3  处理
//        bootstrap.getFactory().releaseExternalResources();
        logger.info("{} stopped at:{}", serverName, port);
    }
}

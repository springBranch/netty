package com.qbao.netty.rpc.netty;

import com.qbao.log.QbLogger;
import com.qbao.log.QbLoggerManager;
import com.qbao.netty.conf.Config;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Description
 * @Copyright Copyright (c)2011
 * @Company ctrip.com
 * @Author li_yao
 * @Version 1.0
 * @Create-at 2011-8-5 09:59:51
 * @Modification-history <br>Date					Author		Version		Description
 * <br>----------------------------------------------------------
 * <br>2011-8-5 09:59:51  	li_yao		1.0			Newly created
 */
public abstract class HttpServer extends AbstractServer {

    private static QbLogger logger = QbLoggerManager.getLogger(HttpServer.class);

    public static String CLIENT_IP_HEADER = "Netty-Client-IP";

    private ExecutorService executor;
    private Object executorLock = new Object();

    protected boolean sendException;

    protected final String deployVersion;


    static class HttpHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
        HttpServer server;
        long accessTime;

        HttpHandler(HttpServer server, long accessTime) {
            this.server = server;
            this.accessTime = accessTime;
        }

        @Override
        public void channelRead0(ChannelHandlerContext ctx, FullHttpRequest httpRequest) throws Exception {

            //计数器 +1 内存不回收。
            httpRequest.content().retain();
            HttpRequestParser parser = server.getHttpRequestParser();
            HttpRequestHandler handler = parser.parse(httpRequest);
            handler.setServer(server);
            handler.setTimeStamp(accessTime);
            handler.setRequest(httpRequest);
            handler.setChannel(ctx.channel());
            handler.setSendException(server.sendException);

            ExecutorService executor = parser.getExecutor(handler.getClass());
            if (executor == null) {
                executor = server.getExecutor();
            }
            executor.submit(handler);

        }

        /**
         * 异常处理
         *
         * @param ctx
         * @param e
         * @throws Exception
         */
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx,
                                    Throwable e) throws Exception {

            logger.error("exceptionCaught,send error - {}, close channel - {}", e.getCause(), ctx.channel());

            HttpUtil.sendHttpResponse(ctx.channel(), null, e, server.sendException, accessTime);

            e.printStackTrace();

            ctx.channel().close();//async
        }


    }


    public HttpServer(String serverName, boolean sendException) {
        super(serverName);
        this.sendException = sendException;
        BufferedReader br = null;
        String version = null;
        try {
            version = "lexis1.0";
        } finally {
            deployVersion = version;
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    logger.error(e);
                }
            }
        }
    }


    protected abstract HttpRequestParser getHttpRequestParser();


    protected ExecutorService getExecutor() {
        if (executor == null) {
            synchronized (executorLock) {
                if (executor == null) {
                    executor = new ThreadPoolExecutor(
                            Config.get().getInt("server.executor.pool.core.size",
                                    Runtime.getRuntime().availableProcessors()),
                            Config.get().getInt("server.executor.pool.max.size",
                                    Runtime.getRuntime().availableProcessors() * 20),
                            60L,
                            TimeUnit.SECONDS,
                            new LinkedBlockingQueue<Runnable>(Config.get().getInt(
                                    "server.executor.queue.size", 5000))
                    );
                }
            }
        }
        return executor;
    }


    /**
     * add childHandler
     *
     * @return
     */
    @Override
    public ChannelHandler createPipelineFactory() {
        return new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                long accessTime = System.currentTimeMillis();
                ChannelPipeline p = ch.pipeline();
//                p.addLast(new HttpRequestDecoder());
//                p.addLast(new HttpObjectAggregator(65536));
//                p.addLast(new HttpResponseEncoder());
//                p.addLast(new HttpContentCompressor());
//                p.addLast(new ChunkedWriteHandler());
//                p.addLast(new HttpHandler(HttpServer.this, accessTime));


                p.addLast(new HttpServerCodec());
                p.addLast(new HttpObjectAggregator(65536));
                p.addLast(new HttpHandler(HttpServer.this, accessTime));
            }
        };
    }
}

package com.qbao.netty.rpc.netty;

import com.qbao.log.QbLogger;
import com.qbao.log.QbLoggerManager;
import com.qbao.netty.util.CommonUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;


public class HttpUtil {
    private static QbLogger logger = QbLoggerManager.getLogger(HttpUtil.class);

    private static QbLogger accessLogger = QbLoggerManager.getLogger(HttpUtil.class);

    public static HttpResponse getHttpResponse(Object response, boolean sendException, long accessTime) {
        HttpResponse httpResponse = null;
        if (response instanceof FullHttpResponse) {
            httpResponse = (FullHttpResponse) response;
        } else {
            int i = 0;
            final int maxLoop = 5;
            for (; i < maxLoop; i++) {
                try {
                    HttpResponseStatus status;
                    if (response instanceof Throwable) {
                        status = response instanceof IllegalAccessException ?
                                HttpResponseStatus.NOT_FOUND :
                                HttpResponseStatus.EXPECTATION_FAILED;
                        response = sendException ? CommonUtil.toString(
                                (Throwable) response) : status.toString();
                    } else {
                        status = HttpResponseStatus.OK;
                    }

                    ByteBuf buffer;
                    if (response != null) {
                        if (response instanceof ByteBuf) {
                            buffer = (ByteBuf) response;
                        } else if (response instanceof byte[]) {
                            buffer =
                                    Unpooled.wrappedBuffer((byte[]) response);
                        } else {
                            buffer = Unpooled.copiedBuffer(
                                    response.toString(), CharsetUtil.UTF_8);
                        }
                    } else {
                        buffer = Unpooled.EMPTY_BUFFER;
                    }

                    httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, buffer);
                    httpResponse.headers().set("Content-Length",
                            buffer.writerIndex());
                    break;
                } catch (Throwable t) {
                    logger.error(t);
                    response = t;
                }
            }
            if (i == maxLoop) {
                httpResponse = new DefaultHttpResponse(HttpVersion.HTTP_1_1,
                        HttpResponseStatus.EXPECTATION_FAILED);
            }
        }
        httpResponse.headers().add("Time-Used", (System.currentTimeMillis() - accessTime) + "ms");
        return httpResponse;
    }

    /**
     * nevel throw exception
     *
     * @param channel
     * @param response
     * @param sendException
     */
    public static void sendHttpResponse(Channel channel, HttpRequest httpRequest,
                                        HttpResponse response, boolean sendException, long accessTime) {
        String responseStatus = "No-Response";
        long responseSize = -1;
        try {
            if (channel == null || !channel.isOpen() || !channel.isActive()) {
                logger.error("channel is not connent");
                return;
            }
            channel.writeAndFlush(response);
//                    .addListener(ChannelFutureListener.CLOSE);

        } catch (Throwable t) {
            t.printStackTrace();
            try {
                channel.close();
                logger.error(t);
            } catch (Throwable t2) {
                //do nothing
            }
        } finally {//logging
//            channel.close();
//            try {
//                logAccess(channel, httpRequest, responseStatus, responseSize,
//                        accessTime);
//            } catch (Exception e) {
//
//            }
        }
    }

    /**
     * error response handl
     *
     * @param channel
     * @param httpRequest
     * @param error
     * @param sendException
     * @param accessTime
     */
    public static void sendHttpResponse(Channel channel, HttpRequest httpRequest,
                                        Throwable error, boolean sendException, long accessTime) {


        HttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_GATEWAY,
                Unpooled.copiedBuffer(error.getMessage(), CharsetUtil.UTF_8));

        sendHttpResponse(channel, httpRequest, response, sendException, accessTime);

    }


    public static String getUri(String url) {
        int pos = url.indexOf('?');
        if (pos >= 0) {
            url = url.substring(0, pos);
        }
        if (url.charAt(url.length() - 1) == '/') {
            url = url.substring(0, url.length() - 1);
        }
        return url;
    }

    static void logAccess(Channel channel, HttpRequest request,
                          String responseStatus, long responseSize, long accessTime) {

        FullHttpRequest httpRequest = (FullHttpRequest) request;

        if (httpRequest == null || httpRequest.headers().get("access-logged") == null) {
            return;
        }
        httpRequest.headers().remove("access-logged");

        int logRequestContentLen = 300;
        String requestStr = null;
        String rwIndex = httpRequest.headers().get("request-content-index-for-log");
        if (rwIndex != null) {
            int[] fromTo = CommonUtil.genFromTo(rwIndex);
            httpRequest.content().readerIndex(fromTo[0]);
            httpRequest.content().writerIndex(fromTo[1]);
            logRequestContentLen += httpRequest.content().readableBytes();
            requestStr = httpRequest.content().toString(CharsetUtil.UTF_8);
            httpRequest.headers().remove("request-content-index-for-log");
        }

        String requestContentLen =
                httpRequest.headers().get("request-content-length");
        httpRequest.headers().remove("request-content-length");

        String logDateFormat =
                "yyyy-MM-dd HH:mm:ss.SSS";
        String logRecordSep =
                "#";
        String logItemSep =
                "\t";
        String logHttpHeadSep = ";";
//                Config.get().get("access.log.item.http.header.separator", ";");

        StringBuilder sb = new StringBuilder(logRequestContentLen);
        sb.append(logRecordSep);

        sb.append(new SimpleDateFormat(logDateFormat).format(
                new Date(accessTime)));//access time

        sb.append(logItemSep);
        sb.append(channel.localAddress());//local address

        sb.append(logItemSep);
        sb.append(channel.remoteAddress());//remote address

        sb.append(logItemSep);
        sb.append(CommonUtil.getRemoteIP(httpRequest));//user IP

        sb.append(logItemSep);
        sb.append(responseStatus);//HttpResonse Status

        sb.append(logItemSep);
        sb.append(System.currentTimeMillis() - accessTime);//usedTime

        sb.append(logItemSep);
        sb.append(responseSize);//response size

        sb.append(logItemSep);
        sb.append(requestContentLen);//request size

        sb.append(logItemSep);
        sb.append(httpRequest.method());//method

        sb.append(logItemSep);
        sb.append(httpRequest.release());//chunked

        sb.append(logItemSep);
        sb.append(httpRequest.uri());//URI

        sb.append(logItemSep);//protocol version
        sb.append(httpRequest.protocolVersion().text());

        sb.append(logItemSep);
        for (Map.Entry<String, String> e : httpRequest.headers()) {
            if (!e.getKey().startsWith(CommonUtil.PARAM_HEADER)) {
                sb.append(e.getKey());
                sb.append(":");
                sb.append(e.getValue());
                sb.append(logHttpHeadSep);
            }
        }
        if (requestStr != null) {
            sb.append(logItemSep);
            sb.append(requestStr);//request content
        }

        accessLogger.info(sb.toString());
    }

}

package com.qbao.netty.rpc.netty;

import io.netty.handler.codec.http.HttpResponse;

public abstract class SimpleHttpRequestHandler<T> extends HttpRequestHandler {

    protected abstract T doRun() throws Exception;


    @Override
    final public void run() {
        Object resp;
        try {
            resp = doRun();

            closeByteBuf();
        } catch (Throwable t) {
            t.printStackTrace();
            resp = t;
        }

        HttpResponse httpResonse = HttpUtil.getHttpResponse(resp, sendException,
                getTimeStamp());

        editResponse(httpResonse);

        HttpUtil.sendHttpResponse(channel, httpRequest, httpResonse, sendException,
                getTimeStamp());

    }

    /**
     * 下游业务自定义response
     *
     * @param response
     */
    public abstract void editResponse(HttpResponse response);

    /**
     * 手动回收netty 数据0复制穿透
     */
    private void closeByteBuf() {
        httpRequest.content().release();
    }

}

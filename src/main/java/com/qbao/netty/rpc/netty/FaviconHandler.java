package com.qbao.netty.rpc.netty;

import com.qbao.netty.rpc.Server;
import io.netty.handler.codec.http.HttpResponse;

/**
 * @author song.j
 * @create 2018-07-20 11:11:12
 **/
public class FaviconHandler extends SimpleHttpRequestHandler {
    @Override
    protected Object doRun() throws Exception {
        return null;
    }

    /**
     * 下游业务自定义response
     *
     * @param response
     */
    @Override
    public void editResponse(HttpResponse response) {

    }

    @Override
    public void setServer(Server server) {

    }
}

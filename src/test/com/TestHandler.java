package com;

import com.qbao.netty.rpc.Server;
import com.qbao.netty.rpc.netty.SimpleHttpRequestHandler;

/**
 * @author song.j
 * @create 2017-10-20 15:15:27
 **/
public class TestHandler extends SimpleHttpRequestHandler<String> {
    @Override
    protected String doRun() throws Exception {
        return "ok";
    }

    @Override
    public void setServer(Server server) {
    }
}

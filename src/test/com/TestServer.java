package com;

import com.qbao.netty.rpc.netty.HttpRequestParser;
import com.qbao.netty.rpc.netty.HttpServer;

/**
 * @author song.j
 * @create 2017-10-20 15:15:21
 **/
public class TestServer extends HttpServer {


    TestParser testParser ;

    public TestServer(String serverName, boolean sendException) {
        super(serverName, sendException);

        testParser = new TestParser();
    }

    @Override
    protected HttpRequestParser getHttpRequestParser() {
        return testParser;
    }


    public static void main(String[] args) throws Exception{
        new TestServer("testserver",false).start(29010);
    }
}

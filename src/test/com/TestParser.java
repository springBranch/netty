package com;

import com.qbao.netty.rpc.netty.RestParser;
import org.jboss.netty.handler.codec.http.HttpMethod;


/**
 * @author song.j
 * @create 2017-10-20 15:15:23
 **/
public class TestParser extends RestParser {


    public TestParser() {
        addHandler(HttpMethod.GET, "/test", TestHandler.class);
    }
}

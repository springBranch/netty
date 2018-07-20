package com.qbao.netty.rpc;

import java.io.IOException;


public interface Server {

    void init() throws Exception;

    void start(int port) throws IOException;

    void start(int port, boolean tryMode) throws IOException;

    int getPort();

    void stop() throws Exception;
}

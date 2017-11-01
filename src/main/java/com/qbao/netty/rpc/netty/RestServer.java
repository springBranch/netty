/**
 *
 */
package com.qbao.netty.rpc.netty;

import com.qbao.log.QbLogger;
import com.qbao.log.QbLoggerManager;
import org.jboss.netty.handler.codec.http.HttpMethod;

/**
 * @author li_yao
 */
public class RestServer extends HttpServer {
    final static QbLogger logger = QbLoggerManager.getLogger(RestServer.class);

    final protected RestParser restParser = new RestParser();

    /**
     * @param serverName
     * @param sendException
     * @param downloadRootPath
     */
    public RestServer(String serverName, boolean sendException) {
        super(serverName, sendException);
    }

    public void addHandler(HttpMethod method, String uri,
                           Class<? extends HttpRequestHandler> handlerClass) {
        restParser.addHandler(method, uri, handlerClass);
    }

    @Override
    final protected HttpRequestParser getHttpRequestParser() {
        return restParser;
    }

}

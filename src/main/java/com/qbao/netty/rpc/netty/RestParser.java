/**
 *
 */
package com.qbao.netty.rpc.netty;

import com.qbao.log.QbLogger;
import com.qbao.log.QbLoggerManager;
import com.qbao.netty.conf.Config;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

public class RestParser implements HttpRequestParser {
    private static QbLogger logger = QbLoggerManager.getLogger(RestParser.class);

    final protected Map<HttpMethod, Map<String, Class<? extends HttpRequestHandler>>> handlerClasses = new HashMap<HttpMethod,
            Map<String, Class<? extends HttpRequestHandler>>>();

    @Override
    public HttpRequestHandler parse(HttpRequest httpRequest)
            throws Exception {
        Map<String, Class<? extends HttpRequestHandler>> subHandlerClasses =
                handlerClasses.get(httpRequest.getMethod());
        if (subHandlerClasses == null) {
            throw new IllegalAccessException("Illegal http method: " +
                    httpRequest.getMethod());
        }
        String uri = HttpUtil.getUri(httpRequest.getUri().toLowerCase());

        accessCheck(uri, httpRequest.headers().get(HttpServer.CLIENT_IP_HEADER));

        Class<? extends HttpRequestHandler> handlerClass =
                subHandlerClasses.get(uri);
        if (handlerClass == null) {
            throw new IllegalAccessException("Illegal http request:[method]"
                    + httpRequest.getMethod() + ",[uri]" + httpRequest.getUri());
        }
        return handlerClass.newInstance();
    }

    void accessCheck(String uri, String clientIP) throws IllegalAccessException {
        String aclRules = Config.get().get("acl.rules");
        if (aclRules == null || aclRules.isEmpty()) {
            return;
        }
        for (String aclRule : aclRules.split("#@@@#")) {
            String[] rule = aclRule.split("#~~~#");
            if (uri.matches(rule[0])) {
                if (!clientIP.matches(rule[1])) {
                    throw new IllegalAccessException("The IP:" + clientIP
                            + " can't access the URI:" + uri);
                }
            }
        }
    }

    public synchronized void addHandler(HttpMethod method, String uri,
                                        Class<? extends HttpRequestHandler> handlerClass) {
        uri = uri.toLowerCase();
        Map<String, Class<? extends HttpRequestHandler>> subHandlerClasses =
                handlerClasses.get(method);
        if (subHandlerClasses == null) {
            subHandlerClasses =
                    new HashMap<String, Class<? extends HttpRequestHandler>>();
            handlerClasses.put(method, subHandlerClasses);
        }
        if (uri.charAt(uri.length() - 1) == '/') {
            uri = uri.substring(0, uri.length() - 1);
        }
        Class<? extends HttpRequestHandler> cls = subHandlerClasses.get(uri);
        if (cls != null) {
            throw new IllegalArgumentException("handler:[" + method + " " + uri + " " + cls + "] has already existed");
        }

        subHandlerClasses.put(uri, handlerClass);
        logger.info("add handler:[{} {} {}]", method, uri, handlerClass);
    }

    @Override
    public ExecutorService getExecutor(Class<? extends HttpRequestHandler>
                                               handlerClass) {
        // TODO Auto-generated method stub
        return null;
    }

}

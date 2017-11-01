package com.qbao.netty.rpc.netty;

import com.qbao.log.QbLogger;
import com.qbao.log.QbLoggerManager;
import com.qbao.netty.util.LifeCycleUtil;

public abstract class SimpleHttpRequestHandler<T> extends HttpRequestHandler {

    private static QbLogger logger = QbLoggerManager.getLogger(
            SimpleHttpRequestHandler.class);

    protected abstract T doRun() throws Exception;

    /**
     * check if it is really a error that should be focus on(will send email)
     *
     * @param t
     * @return
     */
    protected boolean isError(Throwable t) {
        return true;
    }

    @Override
    final public void run() {
        Object resp;
        try {
            LifeCycleUtil.putShutdownRecordPropertie("last.request",
                    httpRequest.toString());
            resp = doRun();
        } catch (Throwable t) {
            try {
                if (isError(t)) {
                    logger.error("channel:{}, httpRequest:{}", t, channel,
                            httpRequest);
                } else {
                    logger.warn("channel:{}, httpRequest:{}", t, channel,
                            httpRequest);
                }
            } catch (Throwable t2) {

            }

            resp = t;
        }
        HttpUtil.sendHttpResponse(channel, httpRequest, resp, sendException,
                getTimeStamp());

    }

}

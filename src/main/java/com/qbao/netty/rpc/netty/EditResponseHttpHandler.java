package com.qbao.netty.rpc.netty;

import com.qbao.log.QbLogger;
import com.qbao.log.QbLoggerManager;
import com.qbao.netty.util.LifeCycleUtil;
import org.jboss.netty.handler.codec.http.HttpResponse;

/**
 * @author song.j
 * @create 2017-08-16 15:15:53
 **/
public abstract class EditResponseHttpHandler<T> extends HttpRequestHandler {
    private static QbLogger logger = QbLoggerManager.getLogger(
            EditResponseHttpHandler.class);

    protected abstract T doRun() throws Exception;

    protected abstract void editResponse(HttpResponse response);

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
            logger.error("channel:{}, httpRequest:{}", t, channel,
                    httpRequest);
            resp = t;
        }
        HttpResponse response = EditResponseHttpUtil.getHttpResponse(resp, sendException, getTimeStamp());
        editResponse(response);

        //这里方法重载了。
        EditResponseHttpUtil.sendHttpResponse(channel, httpRequest, response, sendException,
                getTimeStamp());
    }
}

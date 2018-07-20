package com.qbao.netty.util;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.util.CharsetUtil;

import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author song.j
 * @create 2017-12-01 17:17:56
 **/
public class ParamUtil {

    public static Map getParamGet(FullHttpRequest httpRequest) {

        Map requestParams = new HashMap();
        // 处理get请求
        try {
            Map<String, List<String>> parame = new QueryStringDecoder(
                    URLDecoder.decode(httpRequest.uri(), "UTF-8"),
                    Charset.forName("UTF-8")).parameters();
            Iterator<Map.Entry<String, List<String>>> iterator = parame.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, List<String>> next = iterator.next();
                String value = next.getValue().get(0);
                if ("".equals(value) || value == null) {
                    continue;
                }
                value = value.trim();
                if (value != null && !"".equals(value.trim())) {
                    requestParams.put(next.getKey(), value.trim());
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return requestParams;
    }

    /**
     * 方法处理
     *
     * @param httpRequest
     * @return
     */
    public static String getParamPost(FullHttpRequest httpRequest) {

        return httpRequest.content().toString(CharsetUtil.UTF_8);
    }
}

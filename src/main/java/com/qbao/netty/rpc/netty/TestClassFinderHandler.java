package com.qbao.netty.rpc.netty;

import com.qbao.netty.rpc.Server;
import com.qbao.netty.util.ClassFinder;

/**
 * @Description
 * 
 * @Copyright Copyright (c)2011
 * 
 * @Company ctrip.com
 * 
 * @Author li_yao
 * 
 * @Version 1.0
 * 
 * @Create-at 2011-11-30 10:26:30
 * 
 * @Modification-history
 * <br>Date					Author		Version		Description
 * <br>----------------------------------------------------------
 * <br>2011-11-30 10:26:30  	li_yao		1.0			Newly created
 */
public class TestClassFinderHandler extends SimpleHttpRequestHandler<Integer> {

	HttpServer server;
	
	@Override
	public void setServer(Server server) {
		this.server = (HttpServer) server;
	}


	@Override
	protected Integer doRun() throws Exception {
		return ClassFinder.searchClassPath("").size();
	}


}

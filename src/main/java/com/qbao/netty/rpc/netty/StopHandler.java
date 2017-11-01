package com.qbao.netty.rpc.netty;

import com.qbao.log.QbLogger;
import com.qbao.log.QbLoggerManager;
import com.qbao.netty.conf.Config;
import com.qbao.netty.conf.DbConfig;
import com.qbao.netty.rpc.Server;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;

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
public class StopHandler extends SimpleHttpRequestHandler<Object> {
	private static QbLogger logger = QbLoggerManager.getLogger(StopHandler.class);

	Server server;
	
	@Override
	public void setServer(Server server) {
		this.server = server;
	}

	private void stopServer() throws Exception{
		try{
			logger.info("received a stop reqeust!");
			server.stop();
		} finally {
			try{
				DbConfig.get().releaseSource();
				logger.info("released LoadConfig.get() source");
			} finally{
				try{
					Config.getBase().releaseSource();
					logger.info("released Config.get() source");
				} finally {
					try {
						HttpClient.get().close();
					} finally {
						System.exit(0);
					}

				}
			}
		}
	}

	@Override
	protected Object doRun() throws Exception {
		
		StopHandler.this.channel.getCloseFuture().addListener(
			new ChannelFutureListener(){
	
				@Override
				public void operationComplete(ChannelFuture future)
						throws Exception {
					new Thread(){
						public void run(){
							try {
								stopServer();
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}.start();
				}
				
			}
		);
		
		return server + " stop!";
	}


}

package com.qbao.netty.rpc.netty;


import com.qbao.netty.rpc.AbstractRequestHandler;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpRequest;

public abstract class HttpRequestHandler extends AbstractRequestHandler {
	
	
	protected FullHttpRequest httpRequest;
	
	protected Channel channel;
	
	protected boolean sendException = false;

	@Override
	final public void setRequest(FullHttpRequest request) throws Exception {
		httpRequest = request;
	}
	
	
	public void setChannel(Channel channel){
		this.channel = channel;
	}

	
	public void setSendException(boolean sendException){
		this.sendException = sendException;
	}
	


}

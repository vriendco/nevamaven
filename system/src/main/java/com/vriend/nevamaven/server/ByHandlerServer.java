package com.vriend.nevamaven.server;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

public class ByHandlerServer {

	private Server server; 

	public void start(AbstractHandler handler, Integer port) throws Exception {
		this.server = new Server(port);
		this.server.setHandler(handler); 

		this.server.start();
	}

	public void stop() throws Exception {
		this.server.stop();
	} 
}
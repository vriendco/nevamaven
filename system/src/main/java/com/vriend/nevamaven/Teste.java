package com.vriend.nevamaven;

import com.vriend.nevamaven.server.MavenServer;

public class Teste {

	public static void main(String argsv[]) throws Exception {
		MavenServer server = new MavenServer(7777, "H:\\repository\\", "http://central.maven.org/maven2");
		server.start();

		Thread.sleep(30 * 60 * 1000);

	}
}

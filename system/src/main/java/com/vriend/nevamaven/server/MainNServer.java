package com.vriend.nevamaven.server;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.math.NumberUtils;

import com.vriend.nevamaven.NevamavenUtils;

public class MainNServer {

	private final static String PARAM_PORT = "p";
	private final static String PARAM_LOCAL = "l";
	private final static String PARAM_REMOTE = "r";

	private MavenServer server = null;

	private Integer port = null;

	private String local = null;

	private String remote = null;

	public static void main(String argsv[]) throws Exception {

		Options options = buildOptions();

		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = parser.parse(options, argsv);

		HelpFormatter formatter = new HelpFormatter();

		if (!cmd.hasOption(PARAM_LOCAL)) {
			formatter.printHelp("MainNServer", options);
			return;
		}

		if (!cmd.hasOption(PARAM_REMOTE)) {
			formatter.printHelp("MainNServer", options);
			return;
		}

		String portvalue = cmd.getOptionValue(PARAM_PORT, "80");
		Integer port = 80;

		if (!NumberUtils.isDigits(portvalue)) {
			System.out.println("Port not a number!");
			formatter.printHelp("MainNServer", options);
			return;
		} else {
			port = Integer.parseInt(portvalue);
		}

		String local = cmd.getOptionValue(PARAM_LOCAL);

		String remote = cmd.getOptionValue(PARAM_REMOTE);

		MainNServer mns = new MainNServer(port, local, remote);

		mns.start();

		Thread.currentThread().join();

		mns.stop();

	}

	public MainNServer(Integer port, String local, String remote) throws Exception {
		this.port = port;
		this.local = local;
		this.remote = remote;
	}

	public void start() throws Exception {
		System.out.println("Starting Nevamaven Server :");
		System.out.println("Port		" + this.port);
		System.out.println("Local		" + this.local);
		System.out.println("Remote		" + this.remote);

		server = new MavenServer(port, local, remote);

		server.start();

	}

	public void stop() throws Exception {
		System.out.println("Stoping Nevamaven Server...");
		server.stop();
		System.out.println("Nevamaven Server Stoped");
	}

	private static Options buildOptions() {
		Options options = new Options();

		options.addOption(PARAM_PORT, true, "maven server port, default 80");
		options.addOption(PARAM_LOCAL, true, "required local repository path");
		options.addOption(PARAM_REMOTE, true, "required remote proxy maven server");

		return options;
	}

}

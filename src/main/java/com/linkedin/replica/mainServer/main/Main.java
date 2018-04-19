package com.linkedin.replica.mainServer.main;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.TimeoutException;

import com.linkedin.replica.mainServer.config.Configuration;
import com.linkedin.replica.mainServer.server.Server;

public class Main {
	
	
	public static void start(String... args) throws IOException, InterruptedException {
		if(args.length != 3)
			throw new IllegalArgumentException("Expected three arguments. 1- Web services config file path \n "
					+ "2- Commands config file path \n  3- App config file path");
		
		// create singleton instance of Configuration class that will hold configuration files paths
		Configuration.init(args[0], args[1], args[2]);
		
		// start the server
		String ip = Configuration.getInstance().getAppConfigProp("server.ip");
		int port = Integer.parseInt(Configuration.getInstance().getAppConfigProp("server.port"));
		Server.start(ip, port);

	}
	
	public static void shutdown() throws FileNotFoundException, ClassNotFoundException, IOException, SQLException{
		
	}
	
	public static void main(String[] args) throws FileNotFoundException, ClassNotFoundException, IOException, SQLException, InterruptedException, TimeoutException {
		String[] arr = {"src/main/resources/webserv.microserv.config", "src/main/resources/webserv.command.config", "src/main/resources/app.config"};
		start(arr);
	}
}
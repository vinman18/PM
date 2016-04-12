package it.vin.dev.menzione.logica;

public class Configuration {

	public Configuration() {
		// TODO Auto-generated constructor stub
	}

	public static String ip;
	public static String user;
	public static String password;
	public static String logfile;
	public static final int DBVERSION = 2;
	public static final String PROG_VERSION = "1.0b5";
	
	public static String getLogfile() {
		return logfile;
	}
	public static void setLogfile(String logfile) {
		Configuration.logfile = logfile;
	}
	public static String getIp() {
		return ip;
	}
	public static void setIp(String ip) {
		Configuration.ip = ip;
	}
	public static String getUser() {
		return user;
	}
	public static void setUser(String user) {
		Configuration.user = user;
	}
	public static String getPassword() {
		return password;
	}
	public static void setPassword(String password) {
		Configuration.password = password;
	}
	
	
}

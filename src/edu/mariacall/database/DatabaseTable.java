package edu.mariacall.database;

public class DatabaseTable {
	static public class Ibeacon {
		// table name define
		public static String name = "Ibeacon";
		
		public static final String colDeviceID = "id";
		public static final String colDeviceMAC = "device";
		public static final String colRSSI = "rssi";
		public static final String colDistance = "distance";

		// sql syntax : create
		public static String create() {
			return "CREATE TABLE IF NOT EXISTS " + name + "("
					+ colDeviceID	+ " nvarchar(3)	NOT NULL,"
					+ colDeviceMAC	+ " nvarchar(6)	NOT NULL,"
					+ colRSSI		+ " nvarchar(6)	NOT NULL,"
					+ colDistance 	+ " nvarchar(64) NULL  )";
		}
	}
}

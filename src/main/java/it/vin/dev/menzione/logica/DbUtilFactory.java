package it.vin.dev.menzione.logica;

import java.sql.Connection;
import java.sql.SQLException;

public class DbUtilFactory {


	private DbUtilFactory() {
	}


	public static DbUtil createDbUtil() throws SQLException{
		DbUtil dbu = null;
		Connection conn = DbUtil.createConnection();
		dbu = new DbUtil(conn);

		if(dbu.getDbVersion() != Configuration.DBVERSION)
			throw new SQLException("Versione del database non compatibile con questa versione del programma");

		return dbu;
	}

}

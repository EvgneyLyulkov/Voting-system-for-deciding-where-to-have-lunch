package org.largecode.rmvoting;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@SuppressWarnings("serial")
public class RMV_Exception extends Exception {
	
	private static final String CONSTRAINT_QUERY = 
			"SELECT OBJECT_NAME(OBJECT_ID), OBJECT_NAME(parent_object_id) FROM sys.objects WHERE type_desc LIKE '%CONSTRAINT'";

	public RMV_Exception(String msg) {
		super(msg);
	}

	public RMV_Exception(String msg, Exception cause) {
		super(msg, cause);
	}

	/**
	 * If exception e is instance of RMV_Exception, propagate it as-is.
	 * If exception e is RMV-specific, create and throw new RMV_Exception caused by e
	 * Otherwise propagate e as-is.
	 * 
	 * @param connection
	 * @param e
	 * @throws SQLException
	 * @throws RMV_Exception
	 */
	public static void propagateException(Connection connection, Exception e) throws Exception, RMV_Exception {
		if ( e instanceof RMV_Exception )
			throw e; // Propagate it as-is 

		if ( e instanceof SQLException && connection != null ) { // check if e is RMV-specific
			String msg = e.getMessage();
			
			// check whether exception was thrown by RVM-specific triggers:
			if (msg.contains("Menu update declined") || msg.contains("Vote update is declined")) // this is an RMV-specific exception. 
				throw new RMV_Exception(msg, e); // Throw new RMV_Exception caused by e

			// query all RMV-specific constraints (ConstraintName, TableName):
			Statement stmtSelectConstraints = connection.createStatement();
			try {
				ResultSet rs = stmtSelectConstraints.executeQuery( CONSTRAINT_QUERY );
				try {
					while (rs.next())
						if ( msg.contains(rs.getString(1)) ) // this is an RMV-specific exception.  
							throw new RMV_Exception(msg, e); // Throw new RMV_Exception caused by e

				} finally {
					rs.close();
				}
			} finally {
				stmtSelectConstraints.close();
			}
		}

		// e is not RMV-specific one. Propagate it as-is:
		throw e; 
	}

}

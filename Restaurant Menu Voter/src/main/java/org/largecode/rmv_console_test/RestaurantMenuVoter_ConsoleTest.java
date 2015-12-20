package org.largecode.rmv_console_test;

import java.sql.SQLException;

import org.largecode.rmvoting.RMV_Database;
import org.largecode.rmvoting.RMV_Exception;

/**
 * Voting system for deciding where to have lunch
 *
 */
public class RestaurantMenuVoter_ConsoleTest extends RMV_Database
{
    public static void main( String[] args )
    {
		if (args.length < 1) {
			System.out.println("Usage: java " + RestaurantMenuVoter_ConsoleTest.class.getSimpleName() + " <DB connection string>");
			System.exit(1);
		}
		
		try {
			RestaurantMenuVoter_ConsoleTest rmv = new RestaurantMenuVoter_ConsoleTest(args[0]);
			
			// temp. test
			//System.out.println(rmv.getGrades());
			//System.out.println(rmv.findOrInsertRestaurant("Maxim"));
			//System.out.println(rmv.addMenu(
				//"{\"restaurant\": \"Irish beer\", \"menu\":[{\"dish\": \"Light beeer\", \"price\": 12}, {\"dish\": \"Dark Beer\", \"price\": 25}]}"));
			rmv.addVote(
					"{\"visitor\": {\"name\": \"Bob\", \"surname\": \"Tail\"} , \"restaurant\": \"Russian style\", \"grade\": 3}");
			//System.out.println(rmv.executeQuery("SELECT * FROM Restaurant"));
			//System.out.println(rmv.executeReport("Detailed voting information"));
		} catch (Exception e) {
			System.out.println(handleException(e));
		}		
    }
    
	public static String handleException(Exception e) {
		if (! (e instanceof RMV_Exception))
			e.printStackTrace();
		return e.getMessage();
	}

    /**
     * @param connectionString - connection string to establish a DB connection
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public RestaurantMenuVoter_ConsoleTest(String connectionString) throws ClassNotFoundException, SQLException {
    	super(connectionString);
    }
    
    /**
     * Full list of applicable vote grades.
     * @return JSON array of JSON <Grade.Value: Grade.Name> objects
     */
    /* (non-Javadoc)
     * @see org.largecode.rmvoting.RMV_Database#getGrades()
     */
    @Override
	public String getGrades() {
		try {
			return super.getGrades();
		} catch (SQLException e) {
			return handleException(e);
		}
	}
	
    /* (non-Javadoc)
     * @see org.largecode.rmvoting.RMV_Database#addMenu(java.lang.String)
     */
    @Override
	public int addMenu(String menu) {
		try {
			return super.addMenu(menu);
		} catch (Exception e) {
			System.out.println(handleException(e));
			return -1;
		}
	}
    
}

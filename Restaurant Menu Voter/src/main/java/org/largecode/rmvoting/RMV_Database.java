package org.largecode.rmvoting;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import utils.JSON_Utils;

public class RMV_Database {

	private final static String
	SELECT_GRADES = "SELECT Value, Name FROM Grade ORDER BY Value desc",
	SELECT_RESTAURANT = "SELECT Id FROM Restaurant WHERE Name = ?",
	INSERT_RESTAURANT = "INSERT INTO Restaurant(Name) VALUES(?)",
	SELECT_VISITOR = "SELECT Id FROM Visitor WHERE Name = ? AND Surname = ?",
	INSERT_VISITOR = "INSERT INTO Visitor(Name, Surname) VALUES(?, ?)",
	INSERT_MENU = "INSERT INTO Menu(Restaurant) VALUES(?)",
	SELECT_DISH = "SELECT Id FROM Dish WHERE Name = ?",
	INSERT_DISH = "INSERT INTO Dish(Name) VALUES(?)",
	INSERT_MENU_ITEM = "INSERT INTO MenuItem(Menu,Dish,Price) VALUES (?, ?, ?)",
	SELECT_ACTUAL_MENU = "SELECT Id FROM Menu m WHERE Restaurant = ? AND Since = (SELECT MAX(Since) FROM Menu mm WHERE mm.Restaurant = m.Restaurant)",
	SELECT_GRADE_VALUE_BY_NAME = "SELECT Value FROM Grade WHERE Name = ?",
	INSERT_VOTE = "INSERT INTO Vote(Visitor, Menu, Grade) VALUES(?, ?, ?)",
	UPDATE_VOTE = "UPDATE Vote SET Grade = ? WHERE Visitor = ? AND Menu= ?",
	SELECT_REPORT_BY_ID = "SELECT Query FROM Report WHERE Id = ?",
	SELECT_REPORT_BY_NAME = "SELECT Query FROM Report WHERE Name = ?";

	private Connection connection;

	PreparedStatement
	psSelectGrades,
	psSelectRestaurant,	psInsertRestaurant,
	psSelectVisitor, psInsertVisitor,
	psInsertMenu,
	psSelectDish, psInsertDish,
	psInsertMenuItem, psSelectActualMenu,
	psSelectGradeValueByName, 
	psInsertVote, psUpdateVote,
	psSelectReportById, psSelectReportByName;


	public RMV_Database(String connectionString) throws SQLException, ClassNotFoundException {
		connectToDatabase(connectionString);

		psSelectGrades = connection.prepareStatement(SELECT_GRADES);
		psSelectRestaurant = connection.prepareStatement(SELECT_RESTAURANT);
		psInsertRestaurant = connection.prepareStatement(INSERT_RESTAURANT, Statement.RETURN_GENERATED_KEYS);
		psSelectVisitor = connection.prepareStatement(SELECT_VISITOR);
		psInsertVisitor = connection.prepareStatement(INSERT_VISITOR, Statement.RETURN_GENERATED_KEYS);
		psInsertMenu = connection.prepareStatement(INSERT_MENU, Statement.RETURN_GENERATED_KEYS);
		psSelectDish = connection.prepareStatement(SELECT_DISH);
		psInsertDish = connection.prepareStatement(INSERT_DISH, Statement.RETURN_GENERATED_KEYS);
		psInsertMenuItem = connection.prepareStatement(INSERT_MENU_ITEM);
		psSelectActualMenu = connection.prepareStatement(SELECT_ACTUAL_MENU);
		psSelectGradeValueByName = connection.prepareStatement(SELECT_GRADE_VALUE_BY_NAME);
		psInsertVote = connection.prepareStatement(INSERT_VOTE);
		psUpdateVote = connection.prepareStatement(UPDATE_VOTE);
		psSelectReportById = connection.prepareStatement(SELECT_REPORT_BY_ID);
		psSelectReportByName = connection.prepareStatement(SELECT_REPORT_BY_NAME);
	}

	private void connectToDatabase(String connectionString) throws ClassNotFoundException, SQLException {
		if (connectionString.contains("jtds")) // jTDS connection:
			Class.forName("net.sourceforge.jtds.jdbc.Driver");
		else if (connectionString.contains("mysql")) // JDBC MySQL connection:
			Class.forName("com.mysql.jdbc.Driver");
		else if (connectionString.contains("sqlserver")) // JDBC MS SQL Server connection
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
		// JDBC Type 2: Program files\IBM\SQLLIB\java\db2jcc  must be in class path
		// JDBC Type 4: Program files\IBM\SQLLIB\java\db2jcc4 must be in class path
		else if (connectionString.contains("db2")) // JCC DB2 connection:
			Class.forName("com.ibm.db2.jcc.DB2Driver");

		// establish a connection:
		connection = DriverManager.getConnection(connectionString);
	}

	/**
	 * Full list of applicable vote grades.
	 * @return JSON array of JSON <Grade.Value: Grade.Name> objects
	 * @throws  desc If SQL query fails.
	 */
	public String getGrades() throws SQLException {
		ResultSet rs = psSelectGrades.executeQuery();
		try {
			return JSON_Utils.resultSetToJSON(rs);
		} finally {
			rs.close();
		}
	}

	/**
	 * Find a user by given name and surname, then returns user's Id. If not found, inserts new one.
	 * @param user: JSON object as follows:
	 * 		{"name": <userName>, "surname: <surname>"}
	 * @return User.Id
	 * @throws SQLException 
	 * @throws RMV_Exception 
	 */
	int findOrInsertVisitor(JSONObject visitor) throws SQLException, RMV_Exception {
		String visitorName = visitor.get("name").toString();
		String surname = visitor.get("surname").toString();

		// first try to find visitor by name:
		psSelectVisitor.setString(1, visitorName);
		psSelectVisitor.setString(2, surname);
		ResultSet rs = psSelectVisitor.executeQuery();
		try {
			if (rs.next())
				return rs.getInt(1);
		} finally {
			rs.close();
		}

		// visitor is not found. Let's add new one:
		psInsertVisitor.setString(1, visitorName);
		psInsertVisitor.setString(2, surname);
		psInsertVisitor.executeUpdate();
		ResultSet generatedKeys = psInsertVisitor.getGeneratedKeys();
		if ( generatedKeys.next() )
			return generatedKeys.getInt(1);

		throw new RMV_Exception("Cound not find either insert a visitor " + visitorName + " " + surname);
	}

	/**
	 * Find a restaurant by given name and returns its Id. If not found, insert new one.
	 * @param restaurantName
	 * @return Id of a restaurant being found or inserted 
	 * @throws SQLException
	 * @throws RMV_Exception
	 */
	int findOrInsertRestaurant(String restaurantName) throws SQLException, RMV_Exception {
		// first try to find restaurant by name:
		psSelectRestaurant.setString(1, restaurantName);
		ResultSet rs = psSelectRestaurant.executeQuery();
		try {
			if (rs.next())
				return rs.getInt(1);
		} finally {
			rs.close();
		}

		// restaurant is not found. Let's add new one:
		psInsertRestaurant.setString(1, restaurantName);
		psInsertRestaurant.executeUpdate();
		ResultSet generatedKeys = psInsertRestaurant.getGeneratedKeys();
		if ( generatedKeys.next() )
			return generatedKeys.getInt(1);

		throw new RMV_Exception("Cound not find either insert a restaurant " + restaurantName);
	}

	/**
	 * Finds a dish by given name and returns its Id. If not found, inserts new one.
	 * @param dishName
	 * @return Id of a dish being found or inserted 
	 * @throws SQLException
	 * @throws RMV_Exception
	 */
	int findOrInsertDish(String dishName) throws SQLException, RMV_Exception {
		// first try to find dish by name:
		psSelectDish.setString(1, dishName);
		ResultSet rs = psSelectDish.executeQuery();
		try {
			if (rs.next())
				return rs.getInt(1);
		} finally {
			rs.close();
		}

		// dish is not found. Let's add new one:
		psInsertDish.setString(1, dishName);
		psInsertDish.executeUpdate();
		ResultSet generatedKeys = psInsertDish.getGeneratedKeys();
		if ( generatedKeys.next() )
			return generatedKeys.getInt(1);

		throw new RMV_Exception("Cound not find and insert dish " + dishName);
	}

	/**
	 * Add new restaurant menu 
	 * @param menu: JSON object as follows:
	 * 		{"restaurant": <restaurantName>, "menu":[{"dish": <dishName>, "price": <price>}, {"dish": <dishName>, "price": <price>}, ...]} 
	 *  Example: 
	 *  	{"restaurant": "Exotic fruits", "menu":[{"dish": "Lobster", "price": 125}, {"dish": "Some new dish", "price": 25}]}
	 * @return Id of menu
	 * @throws RMV_Exception 
	 * @throws SQLException 
	 * @throws JSONException 
	 * @throws Exception 
	 */
	public int addMenu(String menu) throws Exception {
		int menuId = 0;
		JSONObject json = new JSONObject(menu);

		boolean autoCommit = connection.getAutoCommit();
		connection.setAutoCommit(false);
		try {
			// determine restaurant:
			String restaurantName = json.get("restaurant").toString();
			int restaurantId = findOrInsertRestaurant(restaurantName);

			// add new menu:
			psInsertMenu.setInt(1, restaurantId);
			psInsertMenu.executeUpdate();
			ResultSet generatedKeys = psInsertMenu.getGeneratedKeys();
			if ( generatedKeys.next() )
				menuId = generatedKeys.getInt(1);
			else {
				connection.rollback();
				throw new RMV_Exception("Could not add new menu for restaurant " + restaurantName);
			}

			// populate menu:
			psInsertMenuItem.setInt(1, menuId);
			JSONArray dishes = json.getJSONArray("menu");
			JSONObject dish;
			String dishName;
			int dishId;
			double price;
			for (int i = 0; i < dishes.length(); i++) {
				dish = dishes.getJSONObject(i);
				dishName = dish.getString("dish");
				dishId = findOrInsertDish(dishName);
				price = dish.getDouble("price");
				psInsertMenuItem.setInt(2, dishId);
				psInsertMenuItem.setDouble(3, price);
				psInsertMenuItem.executeUpdate();
			}
			connection.commit();
		} catch (SQLException e) {
			connection.rollback();
			RMV_Exception.propagateException(connection, e);
		} finally {
			connection.setAutoCommit(autoCommit);
		}
		return menuId;
	}

	/**
	 * @param vote: JSON object as follows:
	 * 		{"visitor": {"name": <userName>, "surname: <surname>} , "restaurant": <restaurantName>, "grade": <grade>}
	 * 		The <grade> can be either an integer corresponding to Grade.Id or a string corresponding to a Grade.Name 
	 *  Example: 
	 *  	{"visitor": {"name": "John", "surname": "Boil"} , "restaurant": "Exotic fruits", "grade": 4}
	 *  or
	 *  	{"visitor": {"name": "John", "surname": "Boil"} , "restaurant": "Exotic fruits", "grade": "Good"}
	 * @throws Exception 
	 */
	public void addVote(String vote) throws Exception {
		final String GRADE = "grade";

		JSONObject json = new JSONObject(vote);

		boolean autoCommit = connection.getAutoCommit();
		connection.setAutoCommit(false);
		try {
			String restaurantName = json.get("restaurant").toString();
			int menuId = getActualMenu(restaurantName);

			// determine visitor:
			int visitorId = findOrInsertVisitor(json.getJSONObject("visitor"));

			int grade;
			try {
				grade = json.getInt(GRADE);
			} catch (JSONException e) {
				String gradeStr = json.getString(GRADE);
				psSelectGradeValueByName.setString(1, gradeStr);
				ResultSet rs = psSelectGradeValueByName.executeQuery();
				try {
					if ( rs.next() )
						grade = rs.getInt(1);
					else
						throw new RMV_Exception("Voting failed: grade '" + gradeStr + "' is incorrect.\nCorrect values are:\n" + getGrades());
				} finally {
					rs.close();
				}
			}

			psInsertVote.setInt(1, visitorId);
			psInsertVote.setInt(2, menuId);
			psInsertVote.setInt(3, grade);
			try {
				psInsertVote.executeUpdate();
			} catch (SQLException e) {
				// It can be Violation of PRIMARY KEY constraint 'Vote_PK'. Cannot insert duplicate key in object 'dbo.Vote'. The duplicate key value is (1, 2).
				// It means that visitor already voted this menu. 
				// Try to update that vote (there is not real need to pre-check whether this vote is really already exist:
				psUpdateVote.setInt(1, grade);
				psUpdateVote.setInt(2, visitorId);
				psUpdateVote.setInt(3, menuId);
				psUpdateVote.executeUpdate();
			}
			connection.commit();
		} catch (SQLException e) {
			connection.rollback();
			RMV_Exception.propagateException(connection, e);
		} finally {
			connection.setAutoCommit(autoCommit);
		}
	}

	/**
	 * Actual menu is the most recent menu published by a restaurant
	 * @param restaurantId
	 * @return Menu.Id
	 * @throws SQLException
	 * @throws RMV_Exception
	 */
	int getActualMenu(final int restaurantId) throws SQLException, RMV_Exception {
		// select the latest menu:
		psSelectActualMenu.setInt(1, restaurantId);
		ResultSet rs = psSelectActualMenu.executeQuery();
		try {
			if (rs.next())
				return rs.getInt(1);
		} finally {
			rs.close();
		}

		throw new RMV_Exception("Voting is not allowed: restaurant did not public any menu yet");
	}

	/**
	 * Actual menu is the most recent menu published by a restaurant
	 * @param restaurantName
	 * @return Menu.Id
	 * @throws SQLException
	 * @throws RMV_Exception
	 */
	int getActualMenu(String restaurantName) throws SQLException, RMV_Exception {
		return getActualMenu(findOrInsertRestaurant(restaurantName));
	}

	/**
	 * @param query - SQL SELECT query to execute
	 * @return Query result in JSON format
	 * @throws SQLException
	 */
	public String executeQuery(String query) throws SQLException {
		Statement st = connection.createStatement();
		try {
			ResultSet rs = st.executeQuery(query);
			try {
				return JSON_Utils.resultSetToJSON(rs);
			} finally {
				rs.close();
			}
		} finally {
			st.close();
		}
	}

	/**
	 * @param reportId - Report.Id to execute
	 * @return Results of report query in JSON format
	 * @throws SQLException
	 * @throws RMV_Exception
	 */
	public String executeReport(final int reportId) throws SQLException, RMV_Exception {
		psSelectReportById.setInt(1, reportId);
		ResultSet rs = psSelectReportById.executeQuery();
		try {
			if ( rs.next() )
				return executeQuery(rs.getString(1));
			else
				throw new RMV_Exception("Report # " + String.valueOf(reportId) + " is not found");
		} finally {
			rs.close();
		}
	}

	/**
	 * @param reportName - Report.Name to execute
	 * @return Results of report query in JSON format
	 * @throws SQLException
	 * @throws RMV_Exception
	 */
	public String executeReport(String reportName) throws SQLException, RMV_Exception {
		psSelectReportByName.setString(1, reportName);
		ResultSet rs = psSelectReportByName.executeQuery();
		try {
			if ( rs.next() )
				return executeQuery(rs.getString(1));
			else
				throw new RMV_Exception("Report '" + reportName + "' is not found");
		} finally {
			rs.close();
		}
	}

}

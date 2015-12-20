package com.largecode.rv_test;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
//import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;
import org.largecode.rmvoting.RMV_Database;
import org.largecode.rmvoting.RMV_Exception;

import utils.JSON_Utils;

/**
 * Servlet implementation class RMV_test_servlet
 */
//@ WebServlet("/RMV_test_servlet")
public class RMV_test_servlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static final String
	PROPERTIES_PATH = "/WEB-INF/RestaurantMenuVoter_JSP_test.properties",
	CONNECTION_STRING =	"SQL_DB_CONNECTION_STRING";

	private static RMV_Database rmv;
	private String generalFailure; 

	private Map<Integer, String> reports = new TreeMap<Integer, String>();

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public RMV_test_servlet() {
		super();		
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		initRMV();
	}

	private void initRMV() {
		if (rmv == null)
			try {
				InputStream is = getServletContext().getResourceAsStream(PROPERTIES_PATH );
				if (is == null) 
					throw new Exception("Cound not open " + PROPERTIES_PATH);

				Properties properties = new Properties();
				properties.load(is);
				String connectionString = properties.getProperty(CONNECTION_STRING);
				rmv = new RMV_Database(connectionString);

				loadReports();
			} catch (Exception e) {
				generalFailure = e.getMessage();
				e.printStackTrace();
			}
	}

	private void loadReports() throws SQLException {
		String reportList = rmv.executeQuery("SELECT Id, Name FROM Report ORDER BY Id");
		JSONArray json = new JSONArray(reportList);
		JSONObject o;
		for (int i = 0; i < json.length(); i++) {
			o = json.getJSONObject(i);
			reports.put(o.getInt("Id"), o.getString("Name"));
		}
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = response.getWriter();

		Map<String, String[]> parameters = request.getParameterMap();

		out.append(getResponceHeader());
		try {
			out.append("<br><b>Testing result:</b><br>");
			if (generalFailure != null)
				out.append("<font color=\"red\"><b>General Failure:<br>").append(generalFailure).append("</b></font>");
			else {
				if (parameters.containsKey("vote"))
					out.append(vote(request));
				else if (parameters.containsKey("addMenu"))
					out.append(addMenu(request));
				else //if (parameters.containsKey("report"))
					out.append(report(parameters.get("report")));
				out.append("<br><font color=\"blue\"><b>Served successfully.</font></b>");
			}
		} catch (Exception e) {
			out.append("<font color=\"red\"><b>Error:<br>").append(e.getMessage()).append("</b></font>");
		} finally {
			out.append(getResponceFooter());
		}
	}

	private CharSequence getResponceHeader() {
		return "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=ISO-8859-1\">" +
				"<title>Restaurant Menu Voter</title></head><body>" + 
				"<h1>Restaurant Menu Voter</h1><h2>Testing stuff</h2><hr/>";
	}

	private CharSequence getResponceFooter() {
		return "<br/><h2>Reports</h2>" +
				getReportingForms() + 
				"</body></html>";	
	}

	private String getReportingForms() {
		StringBuffer result = new StringBuffer("<form action=\"RV_test\">");
		try {
			for (Map.Entry<Integer, String> me: reports.entrySet())
				result.append("<input type=\"submit\" name=\"report\" value=\"" + me.getValue() + "\"/><br/>");
		} finally {
			result.append("</form>");
		}
		return result.toString(); 
	}

	private CharSequence addMenu(HttpServletRequest request) throws Exception {
		String[] dishes = request.getParameterValues("dish");
		String[] prices = request.getParameterValues("price");
		int len = Math.min(dishes.length, prices.length);
		JSONArray menu = new JSONArray();
		for (int i = 0; i < len; i++) 
			if ( !dishes[i].isEmpty() && !prices[i].isEmpty() ){
				JSONObject dish = new JSONObject();
				dish.put("dish", dishes[i]);
				dish.put("price", prices[i]);
				menu.put(dish);
			}

		Map<String, String[]> parameters = request.getParameterMap();

		JSONObject json = new JSONObject();
		json.put("menu", menu);
		put(json, parameters, "restaurant");
		String result = json.toString(); 
		rmv.addMenu(result);
		return result;
	}

	private CharSequence vote(HttpServletRequest request) throws Exception {
		Map<String, String[]> parameters = request.getParameterMap();

		JSONObject visitor = new JSONObject();
		put(visitor, parameters, "name");
		put(visitor, parameters, "surname");
		JSONObject json = new JSONObject();
		json.put("visitor", visitor);
		put(json, parameters, "restaurant");
		put(json, parameters, "grade");
		String result = json.toString(); 
		rmv.addVote(result);
		return result;
	}

	private void put(JSONObject json, Map<String, String[]> parameters, String key) {
		String[] values = parameters.get(key);
		if ( values != null )
			for (String s: values)
				json.put(key, s);
	}

	private CharSequence report(String[] reportId) throws SQLException, RMV_Exception {
		if (reportId != null && reportId.length > 0) {
			String res = rmv.executeReport(reportId[0]);
			return "<h2>" + reportId[0] + "</h2><br/>" + JSON_Utils.jsonArrToHTML(res);
		}
		return "Report not found";
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}

package utils;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONStringer;

public class JSON_Utils {
	/*
	 * Convert ResultSet to a common JSON Object array
	 * Result is like: [{"Value":"4","Name":"Good"},{"Value":"3","Name":"Ambiguous"},{"Value":"2","Name":"Bad"},{"Value":"1","Name":"Disgusting"}]
	 */
	public static String resultSetToJSON(ResultSet rs) throws SQLException {
		// get column names
		ResultSetMetaData rsMeta = rs.getMetaData();
		int columnCnt = rsMeta.getColumnCount();
		String[] columnNames = new String[columnCnt];
		int[] columnTypes = new int[columnCnt];
		for(int i = 0; i < columnCnt; i++) {
			columnNames[i] = rsMeta.getColumnName(i + 1);
			columnTypes[i] = rs.getMetaData().getColumnType(i + 1);
		}

		JSONStringer js = new JSONStringer();
		js.array();
		try {
			while(rs.next()) { // convert each value to JSON key-value pair
				js.object();
				try {
					for(int i = 0; i < columnCnt; i++) {
						js.key(columnNames[i]);
						if (rs.getObject(i + 1) == null )
							js.value(null);
						else
							switch ( columnTypes[i] ) {
							case Types.DATE :     
								js.value(rs.getDate(i + 1)); break;
							case Types.TIMESTAMP :
								js.value(rs.getTimestamp(i + 1)); break;
							case Types.TIME :
								js.value(rs.getTime(i + 1)); break;
							case Types.BIGINT :
							case Types.INTEGER :
								js.value(rs.getInt(i + 1)); break;
							case Types.NUMERIC :
							case Types.REAL :
							case Types.DECIMAL :
							case Types.FLOAT :
							case Types.DOUBLE :
								js.value(rs.getDouble(i + 1)); break;
							case Types.BOOLEAN :
							case Types.BIT :
								js.value(rs.getBoolean(i + 1)); break;
							default :
								js.value(rs.getString(i + 1));
							}
					}
				} finally {
					js.endObject();
				}
			}
		} finally {
			js.endArray();
		}
		return js.toString();
	}

	public static String jsonArrToHTML(String json) {
		JSONArray ja = new JSONArray(json);
		if (ja.length() < 1)
			return json;
		
		Set<String> keys;
		StringBuffer result = new StringBuffer("<table cellspacing=\"2\" border=\"1\" cellpadding=\"5\" width=\"600\">");
		try {
			JSONObject jrow = ja.getJSONObject(0);
			keys = jrow.keySet();
			result.append("<tr>");
			try {
				for (String key: keys)
					result.append("<td><b>" + key + "</b></td>" );
			} finally {
				result.append("</tr>");
			}
			
			String value;
			for (int i = 0; i < ja.length(); i++) {
				result.append("<tr>");
				try {
					jrow = ja.getJSONObject(i);
					for (String key: keys) {
						value = jrow.get(key).toString();
						result.append("<td>" + (value.equals("null") ? "" : value) + "</td>" );
					}
				} finally {
					result.append("</tr>");
				}
			}
		} finally {
			result.append("</table>");
		}

		return result.toString();
	}

}
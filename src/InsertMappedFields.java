import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.mysql.jdbc.PreparedStatement;

@MultipartConfig
public class InsertMappedFields extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static String serverURL = "jdbc:mysql://localhost:3306/capstonedb?useSSL=false";
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json");
		JSONObject returnable = new JSONObject();
		returnable.put("MESSAGE", "No GET support on this endpoint");
		String json = returnable.toString();
		response.getWriter().write(json);
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.setContentType("application/json");
		String responseMessage = "NONE";
		JSONObject returnable = new JSONObject();
		boolean success = true;
		Connection dbConn;
		int docID = -1;
		
		String fieldArray = request.getParameter("FieldIDs");
		String mapArray = request.getParameter("MappingIDs");
		fieldArray = fieldArray.replace("\"", "");
		mapArray = mapArray.replace("\"", "");
		String[] fArray = (fieldArray.substring(1, fieldArray.length()-1)).split(",");;
		String[] mArray = (mapArray.substring(1, mapArray.length()-1)).split(",");
		
		try{		
			Class.forName("com.mysql.jdbc.Driver");
			dbConn = DriverManager.getConnection(serverURL, "root","Trojans17");
			for (int i = 0; i < fArray.length; i++) {
				String sqlInsertString = "Insert into mapped_fields (field_unique_id,mapping_fields_id) values (?,?)";
				PreparedStatement query = (PreparedStatement) dbConn.prepareStatement(sqlInsertString);
				query.setInt(1, Integer.parseInt(fArray[i]));
				query.setInt(2, Integer.parseInt(mArray[i]));
				query.executeUpdate();
			}
		}
		catch (ClassNotFoundException e) {
			responseMessage = e.getMessage();
			success = false;
		} catch (SQLException e) {
			responseMessage = e.getMessage();
			success = false;
		}
		
		returnable.put("FieldArray", new JSONArray(Arrays.asList(fArray)));
		returnable.put("MapArray", new JSONArray(Arrays.asList(mArray)));
		returnable.put("Message", responseMessage);
		returnable.put("Success", success);
		String json = returnable.toString();
		response.getWriter().write(json);
	}
}

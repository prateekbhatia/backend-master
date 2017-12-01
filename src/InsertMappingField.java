import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;
import com.mysql.jdbc.PreparedStatement;

@MultipartConfig
public class InsertMappingField extends HttpServlet {
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
		
		String fieldName = request.getParameter("FIELDNAME");
		String category = request.getParameter("CATEGORY");
		
		try{		
			Class.forName("com.mysql.jdbc.Driver");
			dbConn = DriverManager.getConnection(serverURL, "root","Trojans17");
			String sqlInsertString = "Insert into mapping_fields (full_field_name,category) values (?,?)";
			PreparedStatement query = (PreparedStatement) dbConn.prepareStatement(sqlInsertString);
			query.setString(1, fieldName);
			query.setString(2, category);
			query.executeUpdate();
		}
		catch (ClassNotFoundException e) {
			responseMessage = e.getMessage();
			success = false;
		} catch (SQLException e) {
			responseMessage = e.getMessage();
			success = false;
		}
		
		returnable.put("Message", responseMessage);
		returnable.put("Success", success);
		String json = returnable.toString();
		response.getWriter().write(json);
	}
}

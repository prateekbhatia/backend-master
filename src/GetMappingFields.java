import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.simple.JSONObject;


import com.mysql.jdbc.PreparedStatement;

@MultipartConfig
public class GetMappingFields extends HttpServlet {
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
		
		String pdfCategory = request.getParameter("CATEGORY");
		
	    //GET MAPPING FIELDS POSSIBILITIES
		try {
			Class.forName("com.mysql.jdbc.Driver");
			java.sql.Connection dbConnFin2 = DriverManager.getConnection(serverURL, "root", "Trojans17");
			
			PreparedStatement findMappingFields = (PreparedStatement) dbConnFin2.prepareStatement("SELECT * FROM mapping_fields WHERE category=?");
			findMappingFields.setString(1, pdfCategory);
			ResultSet mappingFields = findMappingFields.executeQuery();
			
            JSONArray data = new JSONArray();
            while (mappingFields.next()) 
            {
                JSONArray row = new JSONArray();
                row.put(mappingFields.getString("id"));
                row.put(mappingFields.getString("full_field_name"));
                data.put(row);
            }
            returnable.put("MappingArray", data);
			
			dbConnFin2.close();
			
		} catch (ClassNotFoundException e) {
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
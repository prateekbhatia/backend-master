import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;

import com.mysql.jdbc.PreparedStatement;

public class UpdateSecondary extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static String serverURL = "jdbc:mysql://localhost:3306/capstonedb?useSSL=false";
	private final String sqlInsertSecondary = "INSERT secondary_profile (user_id, mapping_id, value) values (?,?,?)";
	private final String sqlUpdateSecondary = "UPDATE secondary_profile SET value = ? WHERE user_id = ? AND mapping_id = ?";
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json");
		JSONObject returnable = new JSONObject();
		returnable.put("MESSAGE", "No GET support on this endpoint");
		PrintWriter output = response.getWriter();
		output.print(returnable);
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json");
		String responseMessage = "NONE";
		JSONObject returnable = new JSONObject();
		boolean success = true;
		Connection dbConn;
		
		int secondaryID = Integer.parseInt(request.getParameter("SecondaryID"));
		
		try {
			Class.forName("com.mysql.jdbc.Driver");
			dbConn = DriverManager.getConnection(serverURL,"root","Trojans17");
			if (secondaryID == -1) { // insert
				PreparedStatement query = (PreparedStatement) dbConn.prepareStatement(sqlInsertSecondary);
				query.setInt(1, Integer.parseInt(request.getParameter("UserID")));
				query.setInt(2, Integer.parseInt(request.getParameter("MappingID")));
				query.setString(3, request.getParameter("Value"));
				query.executeUpdate();
			} else { // update
				PreparedStatement query = (PreparedStatement) dbConn.prepareStatement(sqlUpdateSecondary);
				query.setString(1, request.getParameter("Value"));
				query.setInt(2, Integer.parseInt(request.getParameter("UserID")));
				query.setInt(3, Integer.parseInt(request.getParameter("MappingID")));
				query.executeUpdate();
			}
		} catch (ClassNotFoundException e) {
			success = false;
			returnable.put("Message", e.getMessage());
			returnable.put("Success", success);
			response.getWriter().print(returnable);
			return;
//			e.printStackTrace();
		} catch (SQLException e) {
			success = false;
			returnable.put("Message", responseMessage + " " + e.getMessage());
			returnable.put("Success", success);
			response.getWriter().print(returnable);
			return;
//			e.printStackTrace();
		}
		
		returnable.put("Message", responseMessage);
		returnable.put("Success", success);
		response.getWriter().print(returnable);
	}
}

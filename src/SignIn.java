import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;

import com.mysql.jdbc.PreparedStatement;


public class SignIn extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static String serverURL = "jdbc:mysql://localhost:3306/capstonedb?useSSL=false";


	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Connection dbConn;
		PreparedStatement findUser = null;
		
		response.setContentType("application/json");
		PrintWriter output = response.getWriter();
		String message = "None";
		JSONObject obj = new JSONObject();
		
		try {
			Class.forName("com.mysql.jdbc.Driver");
			dbConn = DriverManager.getConnection(serverURL,"root","Trojans17");
			
			findUser = (PreparedStatement) dbConn.prepareStatement("SELECT * FROM user WHERE Email=? AND Password=?");
			findUser.setString(1, request.getParameter("Username"));
			findUser.setString(2, request.getParameter("Password"));
			ResultSet userInQuestion = findUser.executeQuery();
			if(userInQuestion.first()){
				message = "User found in database.";
				obj.put("Message", message);
				obj.put("UserId", userInQuestion.getString("Id"));
				obj.put("Password",userInQuestion.getString("Password"));
				obj.put("EmailAddress",userInQuestion.getString("Email"));
				obj.put("LicenseNumber",userInQuestion.getString("DLNumber"));
				obj.put("State",userInQuestion.getString("State"));
				obj.put("PhoneNumber",userInQuestion.getString("PhoneNumber"));
				obj.put("PostalCode",userInQuestion.getInt("ZipCode"));
				obj.put("City",userInQuestion.getString("City"));
				obj.put("Address2",userInQuestion.getString("Address2"));
				obj.put("Address1",userInQuestion.getString("Address1"));
				obj.put("DOB",userInQuestion.getDate("DOB").toString());
				obj.put("MiddleName",userInQuestion.getString("MiddleName"));
				obj.put("LastName",userInQuestion.getString("LastName"));
				obj.put("FirstName",userInQuestion.getString("FirstName"));
				obj.put("Success", true);
			}
			else{
				message = "User profile unable to be located.";
				obj.put("Message", message);
				obj.put("Success", false);
			}
			dbConn.close();
			
		} catch (ClassNotFoundException e) {
			obj.put("Message", e.getMessage());
			obj.put("Success", false);
			e.printStackTrace();
		} catch (SQLException e) {
			obj.put("Message", e.getMessage());
			obj.put("Success", false);
			e.printStackTrace();
		}
		output.print(obj);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}

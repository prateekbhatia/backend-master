import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;

import com.mysql.jdbc.PreparedStatement;

/**
 * Servlet implementation class Main
 */
//@WebServlet("/De")
public class Main extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static String serverURL = "jdbc:mysql://localhost:3306/capstonedb?useSSL=false";
	
	//private PreparedStatement findUser;
	//private PreparedStatement getAllPDF;
	//private PreparedStatement findPDF;
	
	public void init() throws ServletException{
	}
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//TODO Differences between the DB fields initially given and mentioned as key/val pairs.
		/*
		 * Address is two parts.
		 * City is not included
		 * No password 
		 */
		Connection dbConn;
		PreparedStatement createUser = null;
		PreparedStatement checkForUser = null;
		int rowsAffected = 0;
		response.setContentType("application/json");
		PrintWriter output = response.getWriter();
		String errMessage = "None";
		try {
			Class.forName("com.mysql.jdbc.Driver");

			dbConn = DriverManager.getConnection(serverURL, "root", "Trojans17");
			
			
			String emailGiven =  request.getParameter("EmailAddress");
			if(emailGiven != null){
				checkForUser = (PreparedStatement) dbConn.prepareStatement("Select Email from user where Email = ?");
				checkForUser.setString(1, emailGiven);
				ResultSet userExists = checkForUser.executeQuery();
				if(userExists.first()){
					errMessage = "Email already in use.";
				}
				else{
					createUser = (PreparedStatement) dbConn.prepareStatement("INSERT INTO user (FirstName,LastName,MiddleName,DOB,Address1,Address2,City,ZipCode,PhoneNumber,State,DLNumber, Email, Password) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)");
					createUser.setString(1, request.getParameter("FirstName"));
					createUser.setString(2, request.getParameter("LastName"));
					createUser.setString(3, request.getParameter("MiddleName"));
					
					String nullCheckString;
					if((nullCheckString = request.getParameter("DOB")) != null)
					createUser.setDate(4, Date.valueOf(nullCheckString));
					
					createUser.setString(5, request.getParameter("Address1"));
					createUser.setString(6, request.getParameter("Address2"));
					createUser.setString(7, request.getParameter("City"));
					
					if((nullCheckString = request.getParameter("PostalCode")) != null)
					createUser.setInt(8, Integer.parseInt(nullCheckString));
					
					createUser.setString(9, request.getParameter("PhoneNumber"));
					createUser.setString(10, request.getParameter("State"));
					createUser.setString(11, request.getParameter("LicenseNumber"));
					createUser.setString(12, emailGiven);
					createUser.setString(13, request.getParameter("Password"));

					rowsAffected = createUser.executeUpdate();
				}
			}
			dbConn.close();
			}
		catch (ClassNotFoundException e){
			output.println("CNF" + e.getMessage());
			errMessage = e.getMessage();
			e.printStackTrace();
		}
		catch (SQLException e) {
				//output.println("SQL" + e.getMessage());
				//e.printStackTrace();
				errMessage = e.getMessage();
			}
		
		JSONObject obj = new JSONObject();
		if(rowsAffected != 0){
			obj.put("Message", "Successfully inserted new user.");
			obj.put("Success",true);
		}
		else{
			obj.put("Message", errMessage);
			obj.put("Success", false);
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
	
	public void destroy(){
		
	}
}
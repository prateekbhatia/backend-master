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

public class GetMappedField extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static String serverURL = "jdbc:mysql://localhost:3306/capstonedb?useSSL=false";
	private final String sqlSelectFieldIDString = "SELECT id from pdf_structure where field_name = ? and pdf_id = ?";
	private final String sqlMappingIDString = "SELECT mapping_fields_id from mapped_fields where field_unique_id = ?";
	private final String sqlSelectMappingString = "SELECT category, full_field_name from mapping_fields where id = ?";
	private final String sqlSelectSecondaryString = "SELECT id, value from secondary_profile where user_id = ? and mapping_id = ?";

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
		
		int fieldID = -1;
		int mappingID = -1;
		boolean isMapped = true;
		boolean isSecondary = false;
		String fullFieldName = "";
		String category = "";
		
		try {
			Class.forName("com.mysql.jdbc.Driver");
			dbConn = DriverManager.getConnection(serverURL,"root","Trojans17");
			
			int pdfID = Integer.parseInt(request.getParameter("PdfID"));
			int userID = Integer.parseInt(request.getParameter("UserID"));
			String fieldName = request.getParameter("FieldName");
			
			PreparedStatement selectFieldIDStatement = (PreparedStatement) dbConn.prepareStatement(sqlSelectFieldIDString);
			selectFieldIDStatement.setString(1, fieldName);
			selectFieldIDStatement.setInt(2, pdfID);
			ResultSet fieldIDResult = selectFieldIDStatement.executeQuery();
			if(fieldIDResult.first()){
				fieldID = fieldIDResult.getInt("id");						
			} else {
				success = false;
				responseMessage = "Field ID not found in pdf_structure";
				returnable.put("Message", responseMessage);
				returnable.put("Success", success);
				response.getWriter().print(returnable);
				return;
			}
			
			PreparedStatement selectMappingIDStatement = (PreparedStatement) dbConn.prepareStatement(sqlMappingIDString);
			selectMappingIDStatement.setInt(1, fieldID);
//			responseMessage += "before query ";
			ResultSet mappingIDResult = selectMappingIDStatement.executeQuery();
//			responseMessage += "after query ";
			if (mappingIDResult.first()) {
//				responseMessage += "mapping id has result ";
				mappingID = mappingIDResult.getInt("mapping_fields_id");
			} else {
				isMapped = false;
//				responseMessage += "isSecondary: " + isSecondary;
				returnable.put("isSecondary", isSecondary);
			}
//			responseMessage += "isMapped: " + isMapped;
			returnable.put("isMapped", isMapped);
			
			if (isMapped) {
				// "SELECT category, full_field_name from mapping_fields where id = ?"
				PreparedStatement selectMappingStatement =  (PreparedStatement) dbConn.prepareStatement(sqlSelectMappingString);
				selectMappingStatement.setInt(1,  mappingID);
				ResultSet mappingResult = selectMappingStatement.executeQuery();
				if (mappingResult.first()) {
					fullFieldName = mappingResult.getString("full_field_name");
					category = mappingResult.getString("category");
				} else {
					success = false;
					responseMessage = "Mapping not found in mapping_fields";
					returnable.put("Message", responseMessage);
					returnable.put("Success", success);
					response.getWriter().print(returnable);
					return;
				}
			}
			returnable.put("Category", category);
			String autofill = "";
			int secondaryID = -1;
			if (category.equals("General")) {
				returnable.put("isSecondary", isSecondary);
				autofill = getGeneralProfileMapping(fullFieldName, userID, dbConn);
			} else {
				isSecondary = true;
				returnable.put("isSecondary", isSecondary);
				ResultSet rs = getSecondaryProfileMapping(fullFieldName, userID, mappingID, dbConn);
				if (rs != null && rs.first()) {
					autofill = rs.getString("value");
					secondaryID = rs.getInt("id");
				}
			}
			returnable.put("mappingID", mappingID);
			returnable.put("autofill", autofill);
			returnable.put("secondaryID", secondaryID);
//			responseMessage += " autofill: " + autofill;
			
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
	
	protected ResultSet getSecondaryProfileMapping(String fullFieldName, int userID, int mappingID, Connection dbConn) {
		String autofill = "";
				
		try {
			PreparedStatement selectSecondaryStatement = (PreparedStatement) dbConn.prepareStatement(sqlSelectSecondaryString);

			selectSecondaryStatement.setInt(1, userID);
			selectSecondaryStatement.setInt(2, mappingID);
			ResultSet secondaryResult = selectSecondaryStatement.executeQuery();
			return secondaryResult;
//			if(secondaryResult.first()){
//				autofill = secondaryResult.getString("value");						
//			} 
		} catch (SQLException e) {
			//tMessage = e.getMessage();
			e.printStackTrace();
			return null;
		}
	}
	
	protected String getGeneralProfileMapping(String fullFieldName, int userID, Connection dbConn) {
		String autofill = "";
		String profileColumn = "";
		if (fullFieldName.equals("First Name")) {
			profileColumn = "FirstName";
		} else if (fullFieldName.equals("Last Name")) {
			profileColumn = "LastName";
		} else if (fullFieldName.equals("Middle Name")) {
			profileColumn = "MiddleName";
		} else if (fullFieldName.equals("Date of Birth")) {
			profileColumn = "DOB";
		} else if (fullFieldName.equals("Address")) {
			profileColumn = "Address1";
		} else if (fullFieldName.equals("City")) {
			profileColumn = "City";
		} else if (fullFieldName.equals("Zip Code")) {
			profileColumn = "ZipCode";
		} else if (fullFieldName.equals("Phone Number")) {
			profileColumn = "PhoneNumber";
		} else if (fullFieldName.equals("State")) {
			profileColumn = "State";
		} else if (fullFieldName.equals("Driver's License Number")) {
			profileColumn = "DLNumber";
		} else if (fullFieldName.equals("Email")) {
			profileColumn = "Email";
		}
		
		String selectGeneralProfile = "SELECT " + profileColumn + " FROM user WHERE id = ?";
		
		try {
			PreparedStatement selectGeneralStatement = (PreparedStatement) dbConn.prepareStatement(selectGeneralProfile);

			selectGeneralStatement.setInt(1, userID);
			ResultSet generalResult = selectGeneralStatement.executeQuery();
			if(generalResult.first()){
				autofill = generalResult.getString(profileColumn);						
			} 
		} catch (SQLException e) {
			//tMessage = e.getMessage();
//			e.printStackTrace();
			return "error why";
		}
		return autofill;
	}
}
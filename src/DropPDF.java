import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.simple.JSONObject;

/**
 * Servlet implementation class DropPDF
 */
@WebServlet("/DropPDF")
public class DropPDF extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private String serverURL = "jdbc:mysql://localhost:3306/capstonedb?useSSL=false";
	private String sqlDelCommand = "DELETE FROM filled_pdfs WHERE UniqueID = ?";

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		JSONObject outputObject = new JSONObject();
		String returnMessage = "No Get";
		boolean successfulDelete = false;
		outputObject.put("Success", successfulDelete);
		outputObject.put("Message", returnMessage);
		response.getWriter().print(outputObject);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		JSONObject outputObject = new JSONObject();
		String returnMessage = "None Available";
		boolean successfulDelete = false;
		String pdfIDString = request.getParameter("PDFID");
		if(pdfIDString != null){
			Integer pdfToDeleteID = Integer.parseInt(pdfIDString);
			
			Connection dbConn;
	
			try {
				Class.forName("com.mysql.jdbc.Driver");
				dbConn = DriverManager.getConnection(serverURL, "root", "Trojans17");
			
				PreparedStatement deletePDFCommand = dbConn.prepareStatement(sqlDelCommand);
				deletePDFCommand.setInt(1, pdfToDeleteID);
				int deleteCount;
				if((deleteCount = deletePDFCommand.executeUpdate()) != 1){
					returnMessage = (deleteCount > 1)?"Many Deleted":"None Deleted";
				}
				else{
					successfulDelete = true;
					returnMessage = "One Deleted";
				}
			} catch (SQLException e) {
				returnMessage = e.getMessage();
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				returnMessage = e.getMessage();
				e.printStackTrace();
			}
		}		
		outputObject.put("Success", successfulDelete);
		outputObject.put("Message", returnMessage);
		response.getWriter().print(outputObject);
	}
}

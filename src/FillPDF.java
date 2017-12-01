import java.io.File;
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

import org.apache.pdfbox.pdmodel.PDDocument;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.mysql.jdbc.PreparedStatement;

public class FillPDF extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static String serverURL = "jdbc:mysql://localhost:3306/capstonedb?useSSL=false";
    //public static final String pdfNameKey = "PDFKEY";
    private final String userIDKey = "USERID";
    public static final String pdfIDKey = "PDFID";
    private String documentPathQ = "Select FileUrl from documents where Id = ?";
    private String sqlQuery = "Insert into filled_pdfs (PDFTitle,UniqueIDOfUser,FilePath) values (?,?,?)";
    private String baseDocLoc = "/var/www/html/pdfs/";
   
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter outputWriter  = response.getWriter();
		Connection dbConn;
		String errMessage = "None";
		//String tMessage = "Nil";
		boolean successful = true;
		JSONObject obj = new JSONObject();

			//TODO the request is going to change to send a single JSON object under KEY
			JSONParser parser =  new JSONParser();
			String jsonString = request.getParameter("pdfJsonResults");
			JSONObject pdfFieldData = null;
			try {
				pdfFieldData = (JSONObject) parser.parse(jsonString);
			} catch (ParseException e2) {
				errMessage = "Malformed JSON String";
				e2.printStackTrace();
			}	
			
			StringBuilder fileLocation = new StringBuilder();

			if(request.getParameter(pdfIDKey) == null){
				errMessage = "No User info given.";

			}
			else{
				try {
					Class.forName("com.mysql.jdbc.Driver");
					dbConn = DriverManager.getConnection(serverURL,"root","Trojans17");
					PreparedStatement getPathStatement = (PreparedStatement) dbConn.prepareStatement(documentPathQ);
					getPathStatement.setInt(1, Integer.parseInt(request.getParameter(pdfIDKey)) );
					ResultSet fileUrlResult = getPathStatement.executeQuery();
					if(fileUrlResult.first()){
						if(request.getParameter(userIDKey) != null){
							fileLocation.append(baseDocLoc);
							fileLocation.append(fileUrlResult.getString("FileUrl"));							
						}
						else{
							fileLocation.append("/noexist/");
							//vtMessage = "No pdf title given.";
						}
					}
				} catch (ClassNotFoundException e) {
					//tMessage = e.getMessage();
					e.printStackTrace();
				} catch (SQLException e) {
					//tMessage = e.getMessage();
					e.printStackTrace();
				}
				
			}
			
			File pdfDoc = new File(fileLocation.toString());
			if(!pdfDoc.exists()){
				errMessage = "FILE DOES NOT EXIST";
			}
			
			PDDocument truePDF;
			try {
				Class.forName("org.apache.pdfbox.pdmodel.PDDocument");
				truePDF = PDDocument.load(pdfDoc);
				String pdfTitle = fileLocation.substring(fileLocation.lastIndexOf("/")+1);
				// error line
				String pdfLoc = HelperFunctions.listFields(truePDF,pdfFieldData,pdfTitle,request.getParameter(pdfIDKey));

				
				Class.forName("com.mysql.jdbc.Driver");
				dbConn = DriverManager.getConnection(serverURL, "root", "Trojans17");
				PreparedStatement newFilledPDFStatement = (PreparedStatement) dbConn.prepareStatement(sqlQuery);

				newFilledPDFStatement.setString(1, pdfTitle);
				int uid = -1;
				if(request.getParameter(userIDKey) != null ){
					uid = Integer.parseInt(request.getParameter(userIDKey));
				}
				newFilledPDFStatement.setInt(2,uid);
				newFilledPDFStatement.setString(3,pdfLoc);
				newFilledPDFStatement.executeUpdate();
				obj.put("FileURL", pdfLoc);

			} catch (ClassNotFoundException e1) {
				errMessage = "CNF"+e1.getMessage();
				successful = false;
				e1.printStackTrace();
			}catch (Exception e) {
				errMessage = "EEEEE"+ e + "    " + e.getMessage();
				successful = false;
				e.printStackTrace();
			}
		//obj.put("TMESS",tMessage);
		//obj.put("TEST", fileLocation.toString());	
		obj.put("Message",errMessage);
		obj.put("Success", successful);
		outputWriter.print(obj);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}
}
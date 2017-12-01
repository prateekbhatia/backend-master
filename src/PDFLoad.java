import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDChoice;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.json.JSONArray;
import org.json.simple.JSONObject;


import com.mysql.jdbc.PreparedStatement;

@MultipartConfig
public class PDFLoad extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static String serverURL = "jdbc:mysql://localhost:3306/capstonedb?useSSL=false";
	private final String sqlInsertString = "Insert into documents (FileName,FileURL,Category) values (?,?,?)";
	private final String sqlSelectString = "Select Id from documents where FileName = ? and FileURL = ? and Category = ?";
	private final String pdfBaseFolder = "/var/www/html/pdfs/";

	
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
		
		Part pdfPart = request.getPart("PDF");
		String defaultFileName = pdfPart.getHeader("content-disposition");
		String fileName = defaultFileName.substring(defaultFileName.indexOf("filename=\"") + 10, defaultFileName.indexOf(".pdf"));
		String pdfFileName = !request.getParameter("FILENAME").equals("")?request.getParameter("FILENAME"):defaultFileName;
		String pdfCategory = request.getParameter("CATEGORY");
		returnable.put("Category", pdfCategory);
		
		StringBuilder pdfSaveLocation = new StringBuilder();
		pdfSaveLocation.append(pdfBaseFolder);
		pdfSaveLocation.append(pdfCategory+"/");
		pdfSaveLocation.append(fileName + ".pdf");
		returnable.put("pdfSaveLocation", pdfSaveLocation.toString());
		
		String[] fieldArray = null;
		
		try{
			pdfPart.write(pdfSaveLocation.toString());
			//Set File Permissions
			Set<PosixFilePermission> otherReadablePermissionSet = new HashSet<>();
			otherReadablePermissionSet.add(PosixFilePermission.OWNER_READ);
			otherReadablePermissionSet.add(PosixFilePermission.OWNER_WRITE);
			otherReadablePermissionSet.add(PosixFilePermission.OWNER_EXECUTE);
			otherReadablePermissionSet.add(PosixFilePermission.GROUP_EXECUTE);
			otherReadablePermissionSet.add(PosixFilePermission.GROUP_WRITE);
			otherReadablePermissionSet.add(PosixFilePermission.GROUP_READ);
			otherReadablePermissionSet.add(PosixFilePermission.OTHERS_READ);
			otherReadablePermissionSet.add(PosixFilePermission.OTHERS_WRITE);
			otherReadablePermissionSet.add(PosixFilePermission.OTHERS_EXECUTE);
			Files.setPosixFilePermissions(Paths.get(pdfSaveLocation.toString()), otherReadablePermissionSet );
			
			Class.forName("com.mysql.jdbc.Driver");
			dbConn = DriverManager.getConnection(serverURL, "root","Trojans17");
			PreparedStatement query = (PreparedStatement) dbConn.prepareStatement(sqlInsertString);
			query.setString(1, pdfFileName);
			String fileUrl = pdfSaveLocation.toString();
			int cutOffBase = fileUrl.indexOf(pdfCategory);
			query.setString(2, fileUrl.substring(cutOffBase));
			query.setString(3, pdfCategory);
			query.executeUpdate();
			PreparedStatement insertedElementID = (PreparedStatement) dbConn.prepareStatement(sqlSelectString);
			insertedElementID.setString(1, pdfFileName);
			insertedElementID.setString(2, fileUrl.substring(cutOffBase));
			insertedElementID.setString(3, pdfCategory);
			ResultSet elementRow = insertedElementID.executeQuery();
			if(elementRow.first()){
				docID = elementRow.getInt("Id");
				responseMessage = String.valueOf(docID);
			}
		}
		catch(IOException e){
			responseMessage = e.getMessage();
			success = false;
			
		}catch (ClassNotFoundException e) {
			responseMessage = e.getMessage();
			success = false;
		} catch (SQLException e) {
			responseMessage = e.getMessage();
			success = false;
		}
		
		File file;
	    String path;
	    path = pdfSaveLocation.toString();
	    file = new File(path);
	    //making sure it exists and works
	    if(!file.exists() || file.isDirectory()) { 
		       responseMessage = "File Not Found. Existential Error";
		       success = false;
	    }
	    
	    try {
			Class.forName("com.mysql.jdbc.Driver");
			java.sql.Connection dbConnFin = DriverManager.getConnection(serverURL, "root", "Trojans17");
			PreparedStatement PDFinfo = null;
			String insertTableSQL = "INSERT INTO pdf_structure"
					+ "(type, field_name, field_option, pdf_id) VALUES"
					+ "(?,?,?,?)";
			PDFinfo = (PreparedStatement) dbConnFin.prepareStatement(insertTableSQL);
			// Load the pdfTemplate
		    PDDocument pdfTemplate;
		    try {
				pdfTemplate = PDDocument.load(file);
				//listFields(pdfTemplate);

		    	PDDocumentCatalog docCatalog = pdfTemplate.getDocumentCatalog();
		    	PDAcroForm acroForm = docCatalog.getAcroForm();

		    	// Get field names
		    	java.util.List<PDField> fieldList = new ArrayList<PDField>();
		    	fieldList = acroForm.getFields();

		    	// String the object array
		    	fieldArray = new String[fieldList.size()];
		    	
		    	int i = 0;
		    	for (PDField sField : fieldList) {
		        	fieldArray[i] = sField.getFullyQualifiedName();
		        	i++;
		    	}
		    	returnable.put("FieldArray", new JSONArray(Arrays.asList(fieldArray)));
		    	//
		    	for (String f : fieldArray) {
		    		PDField field = acroForm.getField(f);

		    		String fieldNameTyope = field.getFieldType(); 
 
            		String valueAsString = field.getValueAsString();

					String checkBoxOptions = "";
					String fixedQuestion = "";

					//SQL NORMAL CASE
					PDFinfo.setString(2, f);
					PDFinfo.setString(3, valueAsString);


					if (fieldNameTyope == "Btn"){
	            		if (f.contains("[") && f.contains("]")){	
	            			//check boxes
	            			checkBoxOptions = f.substring(f.indexOf("[") + 1, f.indexOf("]"));

							//fixing question format now
							//fixedQuestion = StringUtils.substringBefore(f, "[");
							fixedQuestion = f.substring(0, f.indexOf("["));

							//SQL FOR THIS CASE
							PDFinfo.setString(2, fixedQuestion);
							PDFinfo.setString(3, checkBoxOptions); //valueAsString

	            		}
	            	}

	            	if (fieldNameTyope == "Ch"){
	            		List<String> test = ((PDChoice)(field)).getOptions(); 
	            		for (String t:test){

	            			PDFinfo.setString(1, fieldNameTyope);
	            			PDFinfo.setString(2, f);
	            			PDFinfo.setString(3, t);
	            			PDFinfo.setInt(4, docID);
	            			int rowsAffected = PDFinfo.executeUpdate();
	            			System.out.println(t);
	            		}
	            	}

		    		PDFinfo.setString(1, fieldNameTyope);
					PDFinfo.setInt(4, docID);
					int rowsAffected = PDFinfo.executeUpdate();
		    	}
			}catch (IOException e) {
				e.printStackTrace();
				success = false;
			}
		}catch(Exception x){
		 responseMessage = x.getMessage();  
		 success = false;
		}
	    
	    //GET MAPPING FIELDS POSSIBILITIES
		try {
			Class.forName("com.mysql.jdbc.Driver");
			java.sql.Connection dbConnFin2 = DriverManager.getConnection(serverURL, "root", "Trojans17");
			
			PreparedStatement findMappingFields = (PreparedStatement) dbConnFin2.prepareStatement("SELECT * FROM mapping_fields WHERE category=? OR category=?");
			findMappingFields.setString(1, "General");
			findMappingFields.setString(2, pdfCategory);
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
		
		//GET FIELD IDS
		try {
			Class.forName("com.mysql.jdbc.Driver");
			java.sql.Connection dbConnFin3 = DriverManager.getConnection(serverURL, "root", "Trojans17");
			
			PreparedStatement findFieldIDS = (PreparedStatement) dbConnFin3.prepareStatement("SELECT * FROM pdf_structure WHERE pdf_id=?");
			findFieldIDS.setInt(1, docID);
			ResultSet fieldIds = findFieldIDS.executeQuery();
			
            JSONArray data = new JSONArray();
            while (fieldIds.next()) 
            {
                data.put(fieldIds.getString("id"));
            }
            returnable.put("FieldIDArray", data);
			
			dbConnFin3.close();
			
		} catch (ClassNotFoundException e) {
			responseMessage = e.getMessage();  
			success = false;
		} catch (SQLException e) {
			responseMessage = e.getMessage();  
			success = false;
		}
		
		returnable.put("PDFName", pdfFileName);
		returnable.put("Category", pdfCategory);
		returnable.put("Message", responseMessage);
		returnable.put("Success", success);
		String json = returnable.toString();
		response.getWriter().write(json);
	}
}

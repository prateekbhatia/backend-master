import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDButton;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class HelperFunctions {

	private static final String endPDFLoc = "/var/www/html/pdfs/";
	public static int getStorageLocation(){
		return endPDFLoc.length();
	}


	public static String listFields( PDDocument doc, JSONObject fieldsData, String pdfTitle, String pdfID) throws Exception {
		PDDocumentCatalog catalog = doc.getDocumentCatalog();
		PDAcroForm form = catalog.getAcroForm();
		java.util.List<PDField> fields = form.getFields();

		//try {

		//debug
		PrintWriter writer = new PrintWriter("/opt/tomcat/webapps/debug-output.txt", "UTF-8");

		Map<String, mPDFField> preparedField = new HashMap<>();
		StringBuilder test = new StringBuilder();

		//Buttons in the PDF will belong to a group and have names like "btnGrp[value]"
		for (PDField field : fields) {
			String fieldType = field.getFieldType();
			String fieldName = field.getFullyQualifiedName();
			//test.append("{T: "+fieldType+" N: "+fieldName+"}");

			if (fieldType.equals("Btn")) {
				String btnGrp = fieldName.substring(0, fieldName.indexOf("["));
				if (preparedField.containsKey(btnGrp)) {
					preparedField.get(btnGrp).addField(field);
				} else {
					mPDFField newfie = new mPDFField(btnGrp);
					newfie.setType(fieldType);
					newfie.addField(field);
					preparedField.put(btnGrp, newfie);
				}
			} else if (fieldType.equals("Tx")) {
				mPDFField newfie = new mPDFField(fieldName);
				newfie.setType(fieldType);
				newfie.addField(field);
				preparedField.put(fieldName, newfie);
			} else if (fieldType.equals("Ch")) {
				mPDFField newfie = new mPDFField(fieldName);
				newfie.setType(fieldType);
				newfie.addField(field);
				preparedField.put(fieldName, newfie);
			}
		}


		Set<String> fieldObjects = fieldsData.keySet();
		for (String s : fieldObjects) {
			JSONObject currObject = (JSONObject) fieldsData.get(s);
			//test.append("<BUFF>"+currObject.get("value")+"</BUFF>");
			if (preparedField.containsKey(s)) {
				mPDFField fieldToComplete = preparedField.get(s);
				if (fieldToComplete.isGroup()) {
					JSONArray groupValues = ((JSONArray) currObject.get("value"));
					for (int i = 0; i < groupValues.size(); ++i) {
						writer.println("line 92");
						fieldToComplete.tickField((String) groupValues.get(i));
					}
				} else {
					if (fieldToComplete.isCh()) {
						String value = (String) ((JSONArray) currObject.get("value")).get(0);
						writer.println("line 99");
						fieldToComplete.setValue(value);
					} else {
						String value = currObject.get("value").toString(); //JENNY'S CHANGE
						writer.println("line 104");
						fieldToComplete.setValue(value);
					}
				}
			}

		}


		StringBuilder tempPath = new StringBuilder();
		tempPath.append(endPDFLoc);
		String pdfFileNameSansExt = pdfTitle;
		int dotIndex = pdfFileNameSansExt.lastIndexOf(".");
		tempPath.append(pdfFileNameSansExt.substring(0, dotIndex) + "_");
		tempPath.append(pdfID + "_");
		tempPath.append(new SimpleDateFormat("yyyyMMddhhmm").format(new Date()));
		tempPath.append(".pdf");
		writer.println("line 122");

		form.flatten();
		writer.println("line 125");

		doc.save(tempPath.toString());
		writer.println("line 128");

		doc.close();
		writer.println("line 130");

		Set<PosixFilePermission> otherReadablePermissionSet = new HashSet<>();
		otherReadablePermissionSet.add(PosixFilePermission.OWNER_READ);
		otherReadablePermissionSet.add(PosixFilePermission.OWNER_WRITE);
		otherReadablePermissionSet.add(PosixFilePermission.GROUP_READ);
		otherReadablePermissionSet.add(PosixFilePermission.OTHERS_READ);
		writer.println("line 137");
		Files.setPosixFilePermissions(Paths.get(tempPath.toString()), otherReadablePermissionSet);
		test.append(tempPath.toString());
		writer.close();

		return test.toString();

		//} catch (IOException e){}

	}
	/*
	 * <--Storage File Naming Convention-->
	 * [Base pdf name]_[PDFID]_[TIMESTAMP].pdf
	 * 
	 * <--Storage Location-->
	 * /var/www/html/ so Apache can handle access to the PDF's online.
	 */
	private static class mPDFField {
		private String fieldName;
		private String fieldType;
		private ArrayList<PDField> fieldElements;

		public boolean isGroup(){
			return fieldType.equals("Btn");
		}
		public boolean isCh(){
			return fieldType.equals("Ch");
		}
		public mPDFField(String title){
			fieldElements = new ArrayList<>();
			fieldName = title;
		}
		public void setType(String type )
		{
			fieldType = type;
		}
		public void addField(PDField newField){
			fieldElements.add(newField);
		}
		public void tickField(String grpElementTitle) throws IOException{
			for(PDField pdF : fieldElements){
				String fieldTitle = pdF.getFullyQualifiedName();
				if( fieldTitle.substring(fieldTitle.indexOf("[")+1, fieldTitle.lastIndexOf("]")).equals(grpElementTitle) ){
					((PDButton)pdF).setValue("Yes");
				}
			}
		}
		public void setValue(String val) throws IOException{
			fieldElements.get(0).setValue(val);
		}
	}
}
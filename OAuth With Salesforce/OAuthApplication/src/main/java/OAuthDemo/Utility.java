package OAuthDemo;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;

/**
 * 
 * Description		-	Utility class to contain the reusable methods
 *
 * Created By		-	Rajeev Jain
 * 
 * Created Date		-	7/13/2018
 */
public class Utility {

	/**
	 * @Description		-	Method to parse the body present in the HTTP Request
	 * 
	 * @return			-	String
	 * 
	 * @args			-	HttpServletRequest
	 * 
	 */
	public static String parseRequestBody(HttpServletRequest request) throws IOException {
		
		//Variable to contain the parsed body
		String data = "";
		
		//Try - catch block
		try {
			
			//Getting the requested body
	    	StringBuilder buffer = new StringBuilder();
		    String line;
		    while ((line = request.getReader().readLine()) != null) {
		        buffer.append(line);
		    }
		    
		    //Converting to string
		    data = buffer.toString();
		    
		    return data;
		    
			}catch(Exception e) {
				data = null;
			}
		
		return data;
	}
	
	
	
	/**
	 * @description 	-	Method to generate the json to send as a response
	 * 
	 * @return			-	JSONObject
	 * 
	 * @param			-	String, String
	 */
	public static JSONObject generateJSON(String message, String success) {
		
		//Creating the json to send as response
		JSONObject obj = new JSONObject();
		obj.put("Success",success);
		obj.put("Message",message);
		return obj;
	}
}

package OAuthDemo;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;

import com.google.gson.Gson;
import com.sforce.async.SObject;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.partner.QueryResult;
import com.sforce.ws.ConnectionException;

/**
 * Description		-		Class to make the connection with salesforce
 * 
 * Created By		-		Rajeev Jain
 * 
 * Created Date		-		
 */
public class SalesforceLoginHelper {

	//Variable to hold the message to display
	public String clientMessage = "";
	
	//Constructor
	public SalesforceLoginHelper(HttpServletRequest request) throws Exception  {
		
		//Calling the method to login the user and sending specific response as a String
		clientMessage = loginUser(request);
	}
	
	/**
	 * @description		-	Method to login the user
	 * 
	 * @return			-	String
	 * 
	 * @param			-	HttpServletRequest
	 * 
	 */
	public String loginUser(HttpServletRequest request) throws Exception {
		
		//Try- catch block
		try {
			
			//Variables need to fetch from request body
			JSONObject json = new JSONObject();
			
			String refreshToken = "";
			String orgType = "";
			
			//String to contain the request body
			String requestBody = Utility.parseRequestBody(request);
					    
		    //Checking if the request body is neither null nor blank
		    if(requestBody != null && requestBody != "") {
		    	
		    	JSONParser jsonParse = new Gson().fromJson(requestBody, JSONParser.class);
		    	refreshToken = jsonParse.refreshToken;
		    	orgType = jsonParse.orgType;
		    	
		    	System.out.println("refreshToken---"+refreshToken);
		    	System.out.println("orgType---"+orgType);
						
					//Calling the method to generate the access token
					OrgConnectionConfig connection = new OrgConnectionCreator().getConnectionInfo(refreshToken, orgType, Constant.KEYWORD_REFRESH);
					
					System.out.println("orgType---"+connection);
					
					//Calling the SFUtitlity class to create the partner connection by sending the third param as true
					SFLoginUtility sfLogin = new SFLoginUtility(connection, false, true, false, false);
			    	
					//Checking if partner connection is established
			    	if(sfLogin.partnerConnnection != null ){

			    		//Calling the method to validate the user
			    		com.sforce.soap.partner.sobject.SObject[] contacts = validateContact(sfLogin.partnerConnnection);
			    		
			    		
			    		//Checking if any user returned by above method
			    		if(contacts != null && contacts.length > 0) {
			    			
			    		for(int i = 0 ; i < contacts.length ; i++) {
			    			com.sforce.soap.partner.sobject.SObject contact = contacts[i];
			    			System.out.println("account:::::"+contact);
			    			Object Name = contact.getField("Name");
			    			System.out.println("Name:::::"+contact);
			    			
			    			json.put("Name"+(i+1), contact.getField("Name").toString());
			    		}
			    			
			    			return json.toString();
			    		}
			    		else {
			    			
			    			return Utility.generateJSON(Constant.NOT_AUTHORIZED, "false").toString();
			    		}
			    	}

	    
	    return Utility.generateJSON(Constant.INVALID, "false").toString();
		    }
		}catch(Exception e) {
			
			e.printStackTrace();
			return Utility.generateJSON("Exception occured while processing the request", "false").toString();
		}
		
		return null;
	}
	
	/**
	 * @description	-	Method to validate if the user exist in the database or not.
	 * @param 		-	PartnerConnection, String, String
	 * @return 		-	com.sforce.soap.partner.sobject.SObject[]
	 * @throws 		-	ConnectionException
	 */
	public com.sforce.soap.partner.sobject.SObject[] validateContact(PartnerConnection pConnection) throws ConnectionException {
		
		//Variable to contain the query to salesforce
	    String soql = "";
	    
	    	soql = "SELECT Id, Name FROM Contact ";
    	
	    //Checking if query is builded
	    if(soql != "") {
	    	
	    	//Debug
	    	System.out.println("soql:::+++++::"+soql);
	    	
	    	//Getting the query results
	    	QueryResult query = pConnection.query(soql);
	    	
	    	//Assinging the query results into an array of sobject
	    	com.sforce.soap.partner.sobject.SObject[] contacts = query.getRecords();
	    	
	    	//Size check of sobject array
	    	if(contacts.length > 0) 
	    		return contacts;
	    }
		return null;
	}
	
	
	public static void main(String [] args) throws SQLException {
		
		
	}
}

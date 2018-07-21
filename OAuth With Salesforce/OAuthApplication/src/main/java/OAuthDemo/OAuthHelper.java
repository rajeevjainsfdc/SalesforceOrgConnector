package ICherubPackage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.sql.Connection; 
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;   
import java.sql.ResultSetMetaData;

import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.partner.SaveResult;
import com.sforce.ws.ConnectionException;

/**
 * Description		:	Class to store the org specific tokens
 * 
 * Created By		:	Rajeev Jain
 * 
 * Created Date		:	7/11/2018
 */
public class OAuthHelper {
	
	//Variable to contain the redirection url
	String url = "";
	
	//Constructor
	public OAuthHelper(HttpServletRequest request, HttpServletResponse response) throws ConnectionException, SQLException {
		
		//Initializing helper class and calling helper class meathod
		OrgConnectionCreator obj = new OrgConnectionCreator();
		OrgConnectionConfig orgConnection = obj.createConnectionRecord(request, response);
			
		//Calling the SFUtitlity class to create the partner connection by sending the third param as true
		SFLoginUtility sfLogin = new SFLoginUtility(orgConnection, false, true, false, false);
		
		if(sfLogin.partnerConnnection != null ) 
    		createCustomSetting(orgConnection, sfLogin.partnerConnnection);
		
		//Build url for redirection
		url = orgConnection.instanceUrl + Constant.REDIRECTURL_1 + orgConnection.accessToken + Constant.REDIRECTURL_2;
	
	}
	
	/**
	 * 
	 * @param partnerConnection
	 * @param connection
	 * @throws ConnectionException
	 */
	
    public void createCustomSetting(OrgConnectionConfig connection, PartnerConnection partnerConnection) throws ConnectionException {
    	
    	//Creating the instace of custom setting object
    	com.sforce.soap.partner.sobject.SObject Oauth_Settings__c = new com.sforce.soap.partner.sobject.SObject();
    	Oauth_Settings__c.setType("Oauth_Settings__c");
    	
    	//Adding the field values
    	Oauth_Settings__c.setField("IsOAuth__c", true);
    	Oauth_Settings__c.setField("Name", "OAuth");
    	Oauth_Settings__c.setField("Organization_Id__c", connection.orgId);
    	Oauth_Settings__c.setField("Refresh_Token__c", connection.refreshToken);
    	Oauth_Settings__c.setField("Access_Token__c", connection.accessToken);
    	Oauth_Settings__c.setField("Instance_URL__c", connection.instanceUrl);
    	//Oauth_Settings__c.setField("Unique_Id__c", String.valueOf(uniqueId));
        	
        // Add this sObject to an array 
    	com.sforce.soap.partner.sobject.SObject[] oauthSettings = new com.sforce.soap.partner.sobject.SObject[1];
    	oauthSettings[0] = Oauth_Settings__c;
        
        // Make a create call and pass it the array of sObjects
        SaveResult[] saveResults = partnerConnection.create(oauthSettings);
    
        // check the returned results for any errors
        for (int i=0; i< saveResults.length; i++) {
          if (saveResults[i].isSuccess()) {
        	 
            System.out.println(i+". Successfully updated record - Id: " + saveResults[i].getId());
          } else {
            com.sforce.soap.partner.Error[] errors = saveResults[i].getErrors();
            for (int j=0; j< errors.length; j++) {
              System.out.println("ERROR updating record: " + errors[j].getMessage());
            }
          }    
        }
    }
}

package OAuthDemo;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.sforce.async.SObject;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.partner.QueryResult;
import com.sforce.soap.partner.SaveResult;

/**
 * Description		-		Class to retrive the org properties like access token,referesh token, instance url
 * 
 * Created By		-		Rajeev Jain
 *
 * Created Date		-		6/8/2018
 */
public class OrgConnectionCreator {

	// New connection parameters
	public static String orgType = null;
	
	// Connection details
	public static OrgConnectionConfig connection = null;

	/*
	 * @description : Method to parse url and obtain connection details and store in
	 * salesforce object
	 *
	 * @args : String authCode, String orgType
	 *
	 * @return : void
	 *
	 **/
	public OrgConnectionConfig createConnectionRecord(HttpServletRequest request, HttpServletResponse response) {

		// Get OAuth code from url
		String authCode = request.getParameter("code");

		String state = request.getParameter("state");
		
		System.out.println("state----"+state);
		System.out.println("authCode----"+authCode);
		
		if(state != null && state != "")
			orgType = state;

		// Initializing Org connection class

		try {
			// Get new connection details using OAuth code
			connection = getConnectionInfo(authCode, orgType, "OAuth");

			return connection;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/*
	 * @description : Method gets connection detaials of an org - org connection via
	 * connected app
	 *
	 * @args : String , String ,String
	 *
	 * @return : void
	 *
	 **/

	public OrgConnectionConfig getConnectionInfo(String token, String orgType, String tokenType) throws Exception {

		OrgConnectionConfig connection = null;

		String url = "";
		// Check org type and build base url
		String urlPrefix = (orgType.contains("Production")? "login" : "test");
		
		System.out.println("urlPrefix----"+urlPrefix);

		// Base url
		url = "https://" + urlPrefix + Constant.OAUTHTOKENURL;
		
		// Build url of string
		URL obj = new URL(url);
		
		// Open connection
		HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
		
		// add request header
		con.setRequestMethod("POST");
		
		System.out.println("Constant.CLIENTID-----"+Constant.CLIENTID);
		System.out.println("Constant.CLIENTSECRET-----"+Constant.CLIENTSECRET);

		// Input connected app details for connection
		String clientId = new String(Constant.CLIENTID.getBytes("UTF-8"));
		String clientSecret = new String(Constant.CLIENTSECRET.getBytes("UTF-8"));
		String redirectUri = Constant.REDIRECTURL;
		String urlParameters = "";

		// Get connection details
		if (tokenType == "OAuth") {

			urlParameters = "grant_type=authorization_code&" + "code=" + token + "&" + "client_id=" + clientId + "&"
					+ "client_secret=" + clientSecret + "&" + "redirect_uri=" + redirectUri;

		}
		// Get local org access token
		if (tokenType == "Refresh") {
			urlParameters = "grant_type=refresh_token&" + "refresh_token=" + token + "&" + "client_id=" + clientId + "&"
					+ "client_secret=" + clientSecret;
		}
		
		System.out.println("urlParameters----"+urlParameters);

		// Send post request
		con.setDoOutput(true);
		// Initialize data stream
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		// add request
		wr.writeBytes(urlParameters);
		// Send request
		wr.flush();
		wr.close();

		// Chekck response code
		int responseCode = con.getResponseCode();
		
		System.out.println("responseCode----"+responseCode);
		if (responseCode == 400) {

			throw new Exception(Constant.INVALID);
		}
		if (responseCode == 401) {

			throw new Exception(Constant.FAILED_AUTHENTICATION);
		}

		// Get Response data
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();
		
		System.out.println("in----"+in);

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
		
		System.out.println("response.toString()----"+response.toString());
		
		// Parse response using method
		connection = parseConectionResponse(response.toString(), orgType);

		return connection;
	}

	/*
	 * @description : Method gets connection detaials of an org - org connection via
	 * connected app
	 *
	 * @args : String authCode, String orgType
	 *
	 * @return : void
	 *
	 **/
	public OrgConnectionConfig parseConectionResponse(String response, String orgType) throws Exception {

		// Initialize jason factory for parsing connection response
		JsonFactory jfactory = new JsonFactory();
		
		// Read json response from string
		JsonParser jParser = jfactory.createJsonParser(response);
		
		// Initialize variables
		String orgId = null, refreshToken = null, accessToken = null, instanceUrl = null, userId = null;
		
		// loop until token equal to "}"
		while (jParser.nextToken() != JsonToken.END_OBJECT) {

			String fieldname = jParser.getCurrentName();

			if ("refresh_token".equalsIgnoreCase(fieldname)) {
				
				// current token is "refresh_token", move to next, which is "refresh_token"'s value
				jParser.nextToken();
				refreshToken = jParser.getText();
			}

			if ("access_token".equalsIgnoreCase(fieldname)) {
				
				// current token is "access_token", move to next, which is "access_token"'s value
				jParser.nextToken();
				accessToken = jParser.getText();
			}

			if ("id".equalsIgnoreCase(fieldname)) {
				
				// current token is "id", move to next, which is "id"'s value
				jParser.nextToken();
				String id = jParser.getText();
				int startIndex = id.indexOf("id/") + 3;
				int endIndex = startIndex + 18;
				orgId = id.substring(startIndex, endIndex);
				userId = id.substring(endIndex + 1, id.length());
			}

			if ("instance_url".equalsIgnoreCase(fieldname)) {
				
				// current token is "instance_url", move to next, which is "instance_url"'s value
				jParser.nextToken();
				instanceUrl = jParser.getText();
			}	

		}

		jParser.close();
		
		//Debug
		System.out.println("orgId:"+orgId);
		System.out.println("orgType:"+orgType);
		System.out.println("refreshToken:"+refreshToken);
		System.out.println("accessToken::"+accessToken);
		System.out.println("instanceUrl::"+instanceUrl);
		

		OrgConnectionConfig connection = new OrgConnectionConfig(orgId, orgType, refreshToken, accessToken, instanceUrl, userId);
		return connection;
	}
}

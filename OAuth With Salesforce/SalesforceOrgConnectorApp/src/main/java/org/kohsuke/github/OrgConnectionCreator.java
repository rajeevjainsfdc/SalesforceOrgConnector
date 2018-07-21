package org.kohsuke.github;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.net.ssl.HttpsURLConnection;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

import com.sforce.soap.partner.GetUserInfoResult;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.partner.SaveResult;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * Description : Class to act as helper class for org connection creater
 *
 * Created By : Rajeev Jain
 *
 * Created Date : 
 *
 * Version : V1.0 Created
 * 
 **/
public class OrgConnectionCreator {

	// Source org connection parameters
	public static String sourceOrgType = null;
	public static String sourceRefreshToken = null;
	public static String sourceAccessToken = null;
	public static String sourceInstanceURL = null;
	// New connection parameters
	public static String orgName = null;
	public static String orgType = null;
	// Flag for local connection
	public boolean isSelfConnection = false;
	// Connection details
	public static OrgConnection connection = null;

	/*
	 * @description : Method to parse url and obtain connection details and store in
	 * salesforce object
	 *
	 * @args : HttpServletRequest , HttpServletResponse
	 *
	 * @return : void
	 *
	 **/
	public void createConnectionRecord(HttpServletRequest request, HttpServletResponse response) throws IOException {

		// Get OAuth code from url
		String authCode = request.getParameter("code");
		final PrintWriter writerA = response.getWriter();
		writerA.println("OAUth CODE---------"+authCode);
		
		// Parsing parameters in state parameter
		parseStateParameter(request);

		try {

			// If connection is to an external org
			if (isSelfConnection == false) {

				// Get local connection access token
				connection = getConnectionInfo(sourceRefreshToken, sourceOrgType, "Refresh");
				// Initialize local org access token to static variable
				sourceAccessToken = connection.accessToken;
			}
			// Get new connection details using OAuth code
			connection = getConnectionInfo(authCode, orgType, "OAuth");
			writerA.println("accessToken---------"+connection.accessToken);
			writerA.println("refreshToken---------"+connection.refreshToken);
			

			// Populate user info into wrapper
			doGetUserInfo();

			//if (orgType.equals(Constant.PRODUCTION) == false)
				//populateSandboxRefreshDate(connection);
			// Method to save connection record
			saveConnectionRecord(response);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * @description : Method gets connection details of an org via connected app
	 * using OAuth token
	 *
	 * @args : String authCode, String orgType ,String tokenType
	 *
	 * @return : void
	 *
	 **/

	public OrgConnection getConnectionInfo(String token, String orgType, String tokenType) throws Exception {

		// Initialize wrapper class instance
		OrgConnection connection = null;

		// Check org type and build base url
		String urlPrefix = (orgType.equals(Constant.PRODUCTION) ? "login" : "test");

		// Base url
		String url = "https://" + urlPrefix + Constant.OAUTHTOKENURL;

		// Build url of string
		URL obj = new URL(url);

		// Open connection
		HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

		// add request header
		con.setRequestMethod("POST");

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

		// Send post request
		con.setDoOutput(true);
		// Initialize data stream
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		// add request
		wr.writeBytes(urlParameters);
		// Send request
		wr.flush();
		wr.close();

		// Check response code
		int responseCode = con.getResponseCode();

		if (responseCode == 400) {

			throw new Exception("Connection invalid");
		}
		if (responseCode == 401) {

			throw new Exception("Auhtnetication Failed");
		}

		// Get Response data
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
		// Parse response using method
		connection = parseConectionResponse(response.toString(), orgType);

		return connection;
	}

	/*
	 * @description : Method gets connection details
	 *
	 * @args : String response, String orgType
	 *
	 * @return : void
	 *
	 **/
	public OrgConnection parseConectionResponse(String response, String orgType) throws Exception {

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
				// current token is "refresh_token",
				// move to next, which is "refresh_token"'s value
				jParser.nextToken();
				refreshToken = jParser.getText();
			}

			if ("access_token".equalsIgnoreCase(fieldname)) {
				// current token is "access_token",
				// move to next, which is "access_token"'s value
				jParser.nextToken();
				accessToken = jParser.getText();
			}

			if ("id".equalsIgnoreCase(fieldname)) {
				// current token is "id",
				// move to next, which is "id"'s value
				jParser.nextToken();
				String id = jParser.getText();
				int startIndex = id.indexOf("id/") + 3;
				int endIndex = startIndex + 18;
				orgId = id.substring(startIndex, endIndex);
				userId = id.substring(endIndex + 1, id.length());
			}

			if ("instance_url".equalsIgnoreCase(fieldname)) {
				// current token is "instance_url",
				// move to next, which is "instance_url"'s value
				jParser.nextToken();
				instanceUrl = jParser.getText();
			}

		}

		jParser.close();

		OrgConnection connection = new OrgConnection(orgId, orgType, refreshToken, accessToken, instanceUrl, userId);
		return connection;
	}

	/*
	 * @description :Method to update sandbox refresh date if connection type is
	 * sandbox
	 *
	 * @args : OrgConnection connection
	 *
	 * @return : void
	 *
	 **/
	public void populateSandboxRefreshDate(OrgConnection connection) throws ConnectionException, ParseException {

		// Initialize partner config
		ConnectorConfig configPartner = new ConnectorConfig();
		String instanceUrlPartner = connection.instanceUrl + Constant.SOAPURL;
		configPartner.setServiceEndpoint(instanceUrlPartner);
		configPartner.setAuthEndpoint(Constant.LOGINURL);
		configPartner.setSessionId(connection.accessToken);

		// Initialize partner connection
		PartnerConnection partnerConnnection = new PartnerConnection(configPartner);

		com.sforce.soap.partner.QueryResult queryResult = null;
		queryResult = partnerConnnection
				.query("Select CreatedDate from SetupAuditTrail order by CreatedDate Asc limit 1");

		// Initialize instance of SObject
		com.sforce.soap.partner.sobject.SObject[] sObject;

		// Add data to array of SObject
		sObject = queryResult.getRecords();

		// Loop to map apex class to test class
		for (int i = 0; i < sObject.length; i++) {

			if (sObject[i].getField("CreatedDate") != null && sObject[i].getField("CreatedDate") != "") {

				// Setu[p date format
				DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
				Date refreshDate = dateFormatter.parse((String) sObject[i].getField("CreatedDate"));
				// Add date value to wrapper
				connection.sandboRefreshDate = refreshDate;
			}
		}
	}

	/*
	 * @description :Method to get user details
	 *
	 * @args : OrgConnection connection
	 *
	 * @return : void
	 *
	 **/
	public void doGetUserInfo() {

		try {

			ConnectorConfig configPartner = new ConnectorConfig();
			String instanceUrlPartner = connection.instanceUrl + Constant.SOAPURL;
			configPartner.setServiceEndpoint(instanceUrlPartner);
			configPartner.setAuthEndpoint(Constant.LOGINURL);
			configPartner.setSessionId(connection.accessToken);

			// Initialize partner connection
			PartnerConnection partnerConnnection = new PartnerConnection(configPartner);

			System.out.println("partnerConnnection.getUserInfo(); - "+partnerConnnection.getUserInfo());
			System.out.println("partnerConnnection.getUserInfo(); - "+partnerConnnection.getUserInfo().getUserEmail());
			// Use partner connection to get user details
			com.sforce.soap.partner.GetUserInfoResult result = partnerConnnection.getUserInfo();
			System.out.println("\nUser Information");
			System.out.println("\tFull name: " + result.getUserFullName());
			System.out.println("\tEmail: " + result.getUserEmail());
			System.out.println("\tLocale: " + result.getUserLocale());
			System.out.println("\tTimezone: " + result.getUserTimeZone());
			System.out.println("\tCurrency symbol: " + result.getCurrencySymbol());
			System.out.println("\tCurrency symbol: " + result.getUserName());
			System.out.println("\tOrganization is multi-currency: " + result.isOrganizationMultiCurrency());

			// Add user details to wrapper
			connection.userFullName = result.getUserFullName();
			connection.userEmail = result.getUserEmail();
			connection.userId = result.getUserId();
	
		} catch (ConnectionException ce) {
			
			ce.printStackTrace();
		}
	}

	/*
	 * @description :Method to get stored connection id
	 *
	 * @args : String response
	 *
	 * @return : void
	 *
	 **/
	private String parseRecordInsertResponse(String response) throws Exception {
		JsonFactory jfactory = new JsonFactory();

		/** read from file **/
		JsonParser jParser = jfactory.createJsonParser(response);

		String recordId = null;

		// loop until token equal to "}"
		while (jParser.nextToken() != JsonToken.END_OBJECT) {

			String fieldname = jParser.getCurrentName();

			if ("id".equalsIgnoreCase(fieldname)) {

				// current token is "refresh_token",
				// move to next, which is "refresh_token"'s value
				jParser.nextToken();
				recordId = jParser.getText();

			}
		}

		return recordId;
	}

	/*
	 * @description : Method id used to parse state parameters
	 *
	 * @args : HttpServletRequest
	 *
	 * @return : void
	 *
	 **/
	public void parseStateParameter(HttpServletRequest request) {

		// Get parameters form force org
		String state = request.getParameter(Constant.STATE);

		// If connnection is local
		if (state.contains(Constant.SELFCONNECTION) == true) {

			String[] items = state.split("\\|");
			orgName = items[0];
			orgType = items[1];
			// set flag as true
			isSelfConnection = true;

		} else {
			// If connection is external
			String authCode = request.getParameter(Constant.CODE);
			String[] items = state.split("\\|");
			// new connection name
			orgName = items[0];
			// New connection org type
			orgType = items[1];
			// Local refresh token
			sourceRefreshToken = items[2];
			// Local instance url
			sourceInstanceURL = items[3];
			// local org type
			sourceOrgType = items[4];

		}
	}

	/*
	 * @description : Method id used to save connection record
	 *
	 * @args : OrgConnection connection, HttpServletResponse response
	 *
	 * @return : void
	 *
	 **/
	public void saveConnectionRecord(HttpServletResponse response) {

		try {

			// Initialize json factory instance
			JsonFactory jfactory = new JsonFactory();
			// Initialize string writer
			StringWriter outputWriter = new StringWriter();
			// Initialize Json Generator
			JsonGenerator jGenerator = jfactory.createJsonGenerator(outputWriter);
			jGenerator.writeStartObject();

			// Adding parameters of connection to add to database
			jGenerator.writeStringField(Constant.COFNAME, orgName);
			jGenerator.writeStringField(Constant.COFACCESSTOKEN, connection.accessToken);
			jGenerator.writeStringField(Constant.COFINSTANCEURL, connection.instanceUrl);
			jGenerator.writeStringField(Constant.COFREFRESHTOKEN, connection.refreshToken);
			jGenerator.writeStringField(Constant.COFORGID, connection.orgId);
			jGenerator.writeStringField(Constant.COFORGTYPE, orgType);
			jGenerator.writeStringField(Constant.COFUSERFULLNAME, connection.userFullName);
			jGenerator.writeStringField(Constant.COFUSEREMAIL, connection.userEmail);
			jGenerator.writeStringField(Constant.COFUSERID, connection.userId);
			System.out.println("connection.userFullName -" + connection.userFullName);
			System.out.println("connection.userEmail -" + connection.userEmail);
			System.out.println("connection.userId -" +connection.userId);


			// End data storage
			jGenerator.writeEndObject();
			jGenerator.close();

			String connectionStr = outputWriter.toString();

			String url = "";

			// Url path to save record in org
			if (isSelfConnection == true) {

				url = connection.instanceUrl + Constant.CONNECTIONOBJECTPATH;

			} else {

				url = sourceInstanceURL + Constant.CONNECTIONOBJECTPATH;
				response.getWriter().println(url);
			}

			URL obj = new URL(url);
			// Initialize url connection for data insertion
			HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

			String accessToken = null;

			if (isSelfConnection == true) {
				accessToken = connection.accessToken;

			} else {

				accessToken = sourceAccessToken;
				response.getWriter().println(accessToken);
			}

			// Define connection type - POST
			con.setRequestMethod("POST");
			// Setting connection header
			con.setRequestProperty("Authorization", "OAuth " + accessToken);
			// Setting request type
			con.setRequestProperty("Content-Type", "application/json");
			con.setDoOutput(true);

			// Stream to
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.write(connectionStr.getBytes("UTF-8"));
			wr.flush();
			wr.close();

			// Get response code
			int responseCode = con.getResponseCode();

			// Initialize buffer reader
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));

			String inputLine;
			StringBuffer stringBufferResponse = new StringBuffer();

			// Passing string through buffer reader
			while ((inputLine = in.readLine()) != null) {

				stringBufferResponse.append(inputLine);
			}

			// Close buffer reader
			in.close();

			//if (orgType.equals(Constant.PRODUCTION) == false)
				//updateSandboxRefreshDate(connection, parseRecordInsertResponse(stringBufferResponse.toString()));

			// Calling method to redirect to record detail page
			redirectToRecord(response, parseRecordInsertResponse(stringBufferResponse.toString()));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * @description :Method to get stored connection id
	 *
	 * @args : String response
	 *
	 * @return : void
	 *
	 **/
	public void updateSandboxRefreshDate(OrgConnection connection, String recordID)
			throws ConnectionException, ParseException {

		// Initialize partner config

		ConnectorConfig configPartner = new ConnectorConfig();
		// In case of self connection
		if (isSelfConnection == true) {
			String instanceUrlPartner = connection.instanceUrl + Constant.SOAPURL;
			configPartner.setServiceEndpoint(instanceUrlPartner);
			configPartner.setAuthEndpoint(Constant.LOGINURL);
			configPartner.setSessionId(connection.accessToken);
		} else {
			String instanceUrlPartner = sourceInstanceURL + Constant.SOAPURL;
			configPartner.setServiceEndpoint(instanceUrlPartner);
			configPartner.setAuthEndpoint(Constant.LOGINURL);
			configPartner.setSessionId(sourceAccessToken);

		}

		// Initialize partner connection
		PartnerConnection partnerConnnection = new PartnerConnection(configPartner);

		com.sforce.soap.partner.sobject.SObject[] testSummaryRecords = new com.sforce.soap.partner.sobject.SObject[1];
		com.sforce.soap.partner.sobject.SObject summary = new com.sforce.soap.partner.sobject.SObject();
		summary.setType("Connection__c");

		// Set id of testsummary lookp record
		summary.setId(recordID);
		summary.setField(Constant.COFSANDBOXREFRESHDATE, connection.sandboRefreshDate);

		// Add record to save
		testSummaryRecords[0] = summary;

		SaveResult[] results;
		// Save record
		results = partnerConnnection.update(testSummaryRecords);
		// get if of saved records
		for (int j = 0; j < results.length; j++) {

			// If record is saved successfully
			if (results[j].isSuccess()) {

			} else {
				// There were errors during the create call,
				// go through the errors array and write
				// them to the console
				for (int i = 0; i < results[j].getErrors().length; i++) {
					com.sforce.soap.partner.Error err = results[j].getErrors()[i];
					System.out.println("Errors were found on item " + j);
					System.out.println("Error code: " + err.getStatusCode().toString());
					System.out.println("Error message: " + err.getMessage());
				}
			}

		}
	}
	/*
	 * @description : Meathod is used to redirect to new connection record detail
	 * page
	 *
	 * @args : HttpServletResponse response, String recordId
	 *
	 * @return : void
	 *
	 **/

	public void redirectToRecord(HttpServletResponse response, String recordId) {

		try {
			// Set response to text /html
			response.setContentType("text/html");
			// Initialize print writer stream
			PrintWriter pw;
			// Get respose in writer stream
			pw = response.getWriter();

			// Redirect to record page
			if (isSelfConnection == true) {

				// if local connection
				response.sendRedirect(connection.instanceUrl + "/" + recordId);

			} else {
				// if external connection
				response.sendRedirect(sourceInstanceURL + "/" + recordId);
			}
			// Close stream
			pw.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*
	 * @description :Class to hold connection detail values
	 *
	 * @args : String response
	 *
	 * @return : void
	 *
	 **/
	public class OrgConnection {
		public String orgId;
		public String orgType;
		public String refreshToken;
		public String accessToken;
		public String instanceUrl;
		public String orgName;
		public String userId;
		public String userName;
		public String userFullName;
		public String userEmail;
		public Boolean isSourceConnection;
		public Date sandboRefreshDate;

		public OrgConnection(String orgId, String orgType, String refreshToken, String accessToken, String instanceUrl,
				String userId) {
			this.orgId = orgId;
			this.orgType = orgType;
			this.refreshToken = refreshToken;
			this.accessToken = accessToken;
			this.instanceUrl = instanceUrl;
			this.userId = userId;
		}
	}
}

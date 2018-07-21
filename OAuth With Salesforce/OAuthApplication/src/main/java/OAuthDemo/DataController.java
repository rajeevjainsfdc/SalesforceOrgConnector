package OAuthDemo;


import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;
import com.sforce.async.SObject;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.partner.QueryResult;
import com.sforce.soap.partner.SaveResult;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;

import java.beans.Statement;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;

/**
 * Description		-		Class to handle ICherub Web Service's requests
 * 
 * Created By		-		Rajeev Jain
 * 
 * Created Date		-		
 */
@Controller
public class DataController {
	
	/**
	 * @description		-		Method to do authorize any salesforce org
	 * 
	 * @return			-		void
	 * 
	 * @param			-		HttpServletRequest , HttpServletResponse
	 *  
	 */
	
	@RequestMapping(value = "/createConnection", method = RequestMethod.GET )
	public void createConnection(HttpServletRequest request, HttpServletResponse response) throws Exception {	
		
		//Calling the helper class for making connection 
		OAuthHelper oAuth = new OAuthHelper(request, response);
		
		//Navigating to specific url
		if(oAuth.url != "")
			response.sendRedirect(oAuth.url);
	}
	
	/**
	 * @description		-	Method to Login and Identify user
	 * 
	 * @return			-	String 
	 * 
	 * @args			-   HttpServletRequest, HttpServletResponse
	 * 
	 */
	@RequestMapping(value = "/LoginAndIdentifyUser", method = RequestMethod.POST)
	@ResponseBody
	public String LoginAndIdentifyUser(HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		//Instatiating the SalesforceLoginHelper class to login to SFDC
		SalesforceLoginHelper login = new SalesforceLoginHelper(request);
		
		System.out.println("-------"+login.clientMessage);
		return login.clientMessage;
	}
	
}

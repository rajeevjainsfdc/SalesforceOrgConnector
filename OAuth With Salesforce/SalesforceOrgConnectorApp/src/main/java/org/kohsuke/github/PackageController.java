package org.kohsuke.github;
/**
* Description   :   Class to act as controller for spring framework 
*
* Created By    :   Ishan Arora
*
* Created Date  :   21/02/2018
*
* Version       :   V1.0 Created
*             
**/ 
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class PackageController {
	
	/*
	 * @description : Method to call Org connection creater helper class for org connection creation
	 *
	 * @args : HttpServletRequest , HttpServletResponse
	 *
	 * @return : void
	 *
	 **/
	@RequestMapping(value = "/createConnection", method = RequestMethod.GET)
	public void createConnection(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		//Initializing helper class and calling helper class meathod
		OrgConnectionCreator obj = new OrgConnectionCreator();
		obj.createConnectionRecord(request, response);

		
	}
	
	

}

package OAuthDemo;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

import com.sforce.soap.metadata.MetadataConnection;
import com.sforce.soap.enterprise.Connector;
import com.sforce.soap.enterprise.EnterpriseConnection;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;
//import com.sforce.ws.transport.SoapConnection;
import com.sforce.soap.tooling.SoapConnection;

/**
 * @Description : Class used to initialize connection of various type with
 *                various org.
 *
 * @Created By  : Rajeev Jain
 *
 * @Created Date : 6 Jun 2018
 *
 * @Revision Logs : V_1.0 Created
 * 
 **/
public class SFLoginUtility {

	//SOAP Connection
	public SoapConnection soapConnection = null;

	// Hold Metadata Connection Details with it
	public MetadataConnection metadataConnection = null;

	// public static Partner connection toolingConection;
	public PartnerConnection partnerConnnection = null;

	// public static ToolingConnection toolingConection;
	public EnterpriseConnection enterpriseConnection = null;


	// Constructor is used to create the different connection based on the user requirnment
	public SFLoginUtility(OrgConnectionConfig config, Boolean isEnterprise, Boolean isPartner, Boolean isMetadata,Boolean isTooling) throws ConnectionException {

		// For creating enterprise connection
		if (isEnterprise)
			createEnterpriseConnection(config);
		
		// for creating metadata connection
		if (isMetadata)
			createMetadataConnection(config);
		
		// for creating tooling connection
		if (isTooling)
			createToolingConnection(config);
		
		// for creating Partner connection
		if (isPartner)
			createPartnerConnection(config);
	}

	/*
	 * @Description : Method to create Partner connection
	 *
	 * @args : OrgConnectionConfig
	 *
	 * @return : void
	 *
	 **/
	public void createPartnerConnection(OrgConnectionConfig config) throws ConnectionException {

		//Connector config
		ConnectorConfig configPartner = new ConnectorConfig();
		configPartner.setServiceEndpoint(config.instanceUrl + Constant.PARTNER_SERVICE_ENDPOINT);
		configPartner.setAuthEndpoint(Constant.PARTNER_AUTH_ENDPOINT);
		configPartner.setSessionId(config.accessToken);

		partnerConnnection = new PartnerConnection(configPartner);
		
		//Debug
		System.out.println("Auth---"+configPartner.getAuthEndpoint());
		System.out.println("Service EndPoint: "+configPartner.getServiceEndpoint());
        System.out.println("Username: "+configPartner.getUsername());
        System.out.println("SessionId: "+configPartner.getSessionId());

	}
	/*
	 * @Description : Method to create Enterprise connection
	 *
	 * @args : OrgConnectionConfig
	 *
	 * @return : void
	 *
	 **/
	public void createEnterpriseConnection(OrgConnectionConfig config) throws ConnectionException {

		// Set Login Config first to set Login End Point URL
		ConnectorConfig loginConfig = new ConnectorConfig();
		loginConfig.setServiceEndpoint(config.instanceUrl + Constant.ENTERPRISE_SERVICE_ENDPOINT);
		loginConfig.setAuthEndpoint(Constant.ENTERPRISE_AUTH_ENDPOINT);
		loginConfig.setManualLogin(true);
		loginConfig.setSessionId(config.accessToken);

		// Create the enterprise connection
		enterpriseConnection = Connector.newConnection(loginConfig);
	}

	/*
	 * @Description : Method to create Metadata connection
	 *
	 * @args : OrgConnectionConfig
	 *
	 * @return : void
	 *
	 **/
	public void createMetadataConnection(OrgConnectionConfig config) throws ConnectionException {

		// Prepare Metadata Config to get Metadata API connection in Salesforce
		final ConnectorConfig metadataConfig = new ConnectorConfig();
		metadataConfig.setServiceEndpoint(config.instanceUrl + Constant.METADATA_SERVICE_ENDPOINT);
		metadataConfig.setSessionId(config.accessToken);

		// Set Metadata Conncection.
		metadataConnection = new MetadataConnection(metadataConfig);
	}
	
	/*
	 * @Description : Method to create Tooling connection
	 *
	 * @args : OrgConnectionConfig
	 *
	 * @return : void
	 *
	 **/
	public void createToolingConnection(OrgConnectionConfig config) throws ConnectionException {

		// Tooling Connection setup
		ConnectorConfig configTooling = new ConnectorConfig();
		String instanceUrlTooling = config.instanceUrl + Constant.TOOLING_SERVICE_ENDPOINT;
		configTooling.setServiceEndpoint(instanceUrlTooling);
		configTooling.setAuthEndpoint(Constant.TOOLING_AUTH_ENDPOINT);
		configTooling.setSessionId(config.accessToken);

		//soapConnection = new SoapConnection(instanceUrlTooling, instanceUrlTooling, null, configTooling);
		soapConnection = new SoapConnection(configTooling);

	}
}

package OAuthDemo;


/**
 * 
 * @description  -  Class to keep the constant values xxxx
 * 
 * @author 		 -  Rajeev Jain
 *
 */
public class Constant {

	//------------------------Connected App-----------------------------
	public final static String CLIENTID = "3MVG9Y6d_Btp4xp54O9J0scFPlz7H7YjdeRifG0T4g.ZzFZRcfmlu_pbue6D3AHaDWF9fVA8dwZlT7RxgrrG7";
	public final static String CLIENTSECRET = "1024940821443839431";
	public final static String REDIRECTURL = "https://oauthapplication`.herokuapp.com/createConnection";
	public final static String CONNECTIONOBJECTPATH = "/services/data/" + "v40.0" + "/sobjects/Connection__c/";
	public final static String LOCALCONNECTIONNAME = "Local Connection";
	public final static String OAUTHTOKENURL = ".salesforce.com/services/oauth2/token";
	
	public final static String ORG_REFRESH_TOKEN = "orgRefreshToken";
	public final static String ORGTYPE = "OrgType";
	public final static String KEYWORD_REFRESH = "Refresh";
	public final static String USERNAME = "username";
	public final static String PASSWORD = "password";
	public final static String VALIDATE = "validate";
	
	
	//------------------------Messages------------------------------
	public final static String NOT_AUTHORIZED = "Please Check your username and password";
	public final static String AUTHORIZED = "Authorized Successfully";
	public final static String INVALID = "Connection invalid";
	public final static String FAILED_AUTHENTICATION = "Authentication Failed";
	public final static String INVALID_DEVICE = "Invalid Device Token";
	public final static String LOGOUT_MESSAGE = "Logged out Successfully";
	public final static String INVALID_CONNECTION = "Invalid Connection";
	public final static String INVALID_CREDENTIAL = "Invalid username or password";
	public final static String FIRST_REGISTRATION_COMPLETE = "First Time Registration Completed Successfully";
	
	
	//-----------------------Different type of connections related variables----
	public final static String VERSION = "41.0";
	public final static String PARTNER_AUTH_ENDPOINT = "https://login.salesforce.com/services/Soap/u/"+VERSION;
	public final static String TOOLING_AUTH_ENDPOINT = "https://login.salesforce.com/services/Soap/c/"+VERSION;
	public final static String ENTERPRISE_AUTH_ENDPOINT = "https://login.salesforce.com/services/Soap/c/"+VERSION;
	
	public final static String PARTNER_SERVICE_ENDPOINT = "/services/Soap/u/"+VERSION;
	public final static String ENTERPRISE_SERVICE_ENDPOINT = "/services/Soap/c/"+VERSION;
	public final static String METADATA_SERVICE_ENDPOINT = "/services/Soap/m/"+VERSION;
	public final static String TOOLING_SERVICE_ENDPOINT = "/services/Soap/T/"+VERSION;
	
	public final static String REDIRECTURL_1 = "/secur/frontdoor.jsp?sid=";
	public final static String REDIRECTURL_2 = "&retURL=/apex/CreateOrgConnectionPage";
		
	
	//-----------------------PostGres related variables---------------------------------------------------
	public static final String DRIVER = "org.postgresql.Driver";   
	public static final String URL = "jdbc:postgresql://ec2-23-21-238-28.compute-1.amazonaws.com:5432/ddgri5088829mi?ssl=true&sslfactory=org.postgresql.ssl.NonValidatingFactory";   
	public static final String POSTGRES_USERNAME = "sxgwlqlfvqadmr";   
	public static final String POSTGRES_PASSWORD = "f69973121a9fe885b2d0beb7c7a91e865b42dded0d6addfbdb39082187741a51";
	public static final String POSTGRES_TABLENAME = "ConnectionInformation";
	public static final String POSTRGRES_TABLE_USERLOGINTRACK = "userlogintrack";
	
	public static final String FIELD_ACCESSTOKEN = "Access_Token";
	public static final String FIELD_REFRESHTOKEN = "Refresh_Token";
	public static final String FIELD_ORGTYPE = "Org_Type";
	public static final String FIELD_INSTANCEURL = "Instance_Url";
	public static final String FIELD_ORGID = "Org_Id";
	public static final String FIELD_USERID = "userid";
	
	public static final String FIELD_DEVICEID = "deviceId";
	
	//----------------------Sobject related variables
	public static final String SOBJECT_CONTACT = "Contact";
	
	//---------------Firebase variables
	public final static String FIREBASE_ENDPOINT = "https://fcm.googleapis.com/fcm/send";
	public final static String FIREBASE_SERVERKEY = "AAAA4vgy_XQ:APA91bHfR220ToeHV_8tLghGiNo9kKUJywRcmlqGueHvDElIA_95DiLLK4c5jXVzi_1_yrHTO_i_hklucZ6mP9BgSAa4zPnJyWH5eQ5wKFIpcI4iEer9wtKnGmLJrXLUm4A861c67eRoy-n5xyljkkdwm_fLU3HeUQ";
	
}

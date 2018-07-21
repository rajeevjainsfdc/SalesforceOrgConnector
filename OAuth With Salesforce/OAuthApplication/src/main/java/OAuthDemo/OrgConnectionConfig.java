package OAuthDemo;

/**
 * @Description : Class is used to hold details of connection
 *
 * @Created By  : Rajeev Jain
 *
 * @Created Date :  
 
 * @Revision Logs : V_1.0 - Created
 *
 **/
public class OrgConnectionConfig {
	
	//Class variables
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
	
	//Constructor
	public OrgConnectionConfig(String orgId, String orgType, String refreshToken, String accessToken, String instanceUrl,String userId) {
		
		//Assigning values to the variables
		this.orgId = orgId;
		this.orgType = orgType;
		this.refreshToken = refreshToken;
		this.accessToken = accessToken;
		this.instanceUrl = instanceUrl;
		this.userId = userId;
	}
}

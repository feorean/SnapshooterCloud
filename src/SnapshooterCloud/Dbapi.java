package SnapshooterCloud;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.auth.profile.ProfilesConfigFile;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.*;
import com.amazonaws.services.dynamodbv2.util.TableUtils;

import java.util.Map;

import org.apache.http.NameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;


/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Khalid
 */
public class Dbapi {
    
	//Global for Dbapi instances
	private static AmazonDynamoDBClient dynamoDB = null;
    private static Common common;
    public  static Map<String, String> userTableBuckets = null; 
    private static String aws_credentials_location = "default";
    
    
    //Automatically create a session
    
    private void init() {
    	
    	
    	if (common != null) {
    		
    		aws_credentials_location = common.getAWSCredentials();
    	}
    	
    	System.out.println("AWS cred. localation"+aws_credentials_location);
    	
        /*
         * The ProfileCredentialsProvider will return your [default]
         * credential profile by reading from the credentials file located at
         * (/Users/Khalid/.aws/credentials).
         */
        AWSCredentials credentials = null;
        
        if (dynamoDB == null) {
        
	        try {
	        	
	        	ProfilesConfigFile cfg = new ProfilesConfigFile(aws_credentials_location);
	        	 
	        	credentials = new ProfileCredentialsProvider(cfg, "default").getCredentials();
	        	
	            //credentials = new ProfileCredentialsProvider(aws_credentials_location).getCredentials();
	        } catch (Exception e) {
	            throw new AmazonClientException(
	                    "Cannot load the credentials from the credential profiles file. " +
	                    "Please make sure that your credentials file is at the correct " +
	                    "location (/Users/Khalid/.aws/credentials), and is in valid format.",
	                    e);
	        }
	        dynamoDB = new AmazonDynamoDBClient(credentials);
	        Region usWest2 = Region.getRegion(Regions.US_WEST_2);
	        dynamoDB.setRegion(usWest2);
	        
        }    	
        
        loadUserBuckets();
    }
    
    //Basic
    public Dbapi() {
        
    	init();
        
    }    
    
    //If you want to use URL execution etc
    public Dbapi(Common cmn) {
    
        common = cmn;
        init();        
    }
    
    public Map<String, String> getUsersWithBuckets() {
    	
    	Map<String, String> usersBuckets = new HashMap<String, String>();
        List <NameValuePair> params = new ArrayList <>();

        String jsonData = common.executeURLWithParams("getuserswithbuckets", params);

        //DEBUG:Print 
        System.out.println("data:"+jsonData);
        
        JSONObject obj = new JSONObject(jsonData);

        
        Iterator<String> itr = obj.keys();
        while(itr.hasNext()) {
        	
        	JSONObject row = obj.getJSONObject(itr.next());
        	usersBuckets.put(row.getString("uid"), row.getString("ub"));
        	
        }
        
        return usersBuckets;
    }
    
    public Map<String, String> loadUserBuckets() {    	
    	
    	if (userTableBuckets == null) {
    		    	
    		userTableBuckets = getUsersWithBuckets();
    		
    	}
    	
    	return userTableBuckets;
    }
    
    public String getUserTable(String userId) {
    	    	
    	return "snaps.snapshots";    	
    }    
   
/*    public boolean createTable(String tableBucket) {
    	
    	boolean res = false;
    	
    	try {
	    	
    		 String tableName = "snaps.snapshots_"+tableBucket;
	    	
	    	 if (Tables.doesTableExist(dynamoDB, tableName)) {
	             System.out.println("Table " + tableName + " is already ACTIVE");
	         } else {
	         	
	         	KeySchemaElement partitionKey = new KeySchemaElement().withAttributeName("ub").withKeyType(KeyType.HASH);
	         	KeySchemaElement sortKey = new KeySchemaElement().withAttributeName("id").withKeyType(KeyType.RANGE);
	
	         	List<KeySchemaElement> keyCollection = new ArrayList<KeySchemaElement>();
	         	keyCollection.add(partitionKey);            	
	         	keyCollection.add(sortKey);  	         	
	         	
	         	
	             // Create a table with a primary hash key named 'name', which holds a string
	             CreateTableRequest createTableRequest = new CreateTableRequest().withTableName(tableName)
	                 .withKeySchema(keyCollection)
	                 .withAttributeDefinitions(new AttributeDefinition().withAttributeName("ub").withAttributeType(ScalarAttributeType.S),
	                 		new AttributeDefinition().withAttributeName("id").withAttributeType(ScalarAttributeType.N))
	                 .withProvisionedThroughput(new ProvisionedThroughput().withReadCapacityUnits(1L).withWriteCapacityUnits(1L));
	                 TableDescription createdTableDescription = dynamoDB.createTable(createTableRequest).getTableDescription();
	             System.out.println("Created Table: " + createdTableDescription);
	
	             // Wait for it to become active
	             System.out.println("Waiting for " + tableName + " to become ACTIVE...");
	             Tables.awaitTableToBecomeActive(dynamoDB, tableName);
	             
	             res = true;
	         }
    	 }
    	 catch (Exception e) {
    		 
    		 System.out.println(e.getMessage());
    		 res = false;
    	 }
    	
    	return res;
    }*/
    
    public boolean checkIfTableExists(String tablename) {
        
    	boolean res;
        
    	try {
        	
        	TableUtils.waitUntilExists(dynamoDB, tablename, 2, 1);         	
        	res = true;
        	
        } catch (AmazonClientException | InterruptedException ie) {
        	
        	res = false;
        }
        
        return res;
        
    }  
    

    
    public boolean addSnapRecordToDB(String  createDay,
								            String  createDatetime,
								            String  fileName,
								            String  fileSize,
								            String  screenName,
								            String  userId,
								            String  deviceId,
								            String  localId) {


			Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
			item.put("ub", new AttributeValue(userTableBuckets.get(userId)));
			item.put("id", new AttributeValue().withN(userId+createDay+"."+localId));        
			item.put("cr_dt", new AttributeValue(createDatetime));
			item.put("fn", new AttributeValue(fileName));
			item.put("fs", new AttributeValue(fileSize));
			item.put("sn", new AttributeValue(screenName));
			item.put("did", new AttributeValue(deviceId));
			
			
			
			PutItemRequest putItemRequest = new PutItemRequest(getUserTable(userId), item);
			dynamoDB.putItem(putItemRequest);  
			
			return true; 
			
	}
           
    
     public QueryResult getFileList(  String    dateFrom,
	                                  String    dateTo,
	                                  String    screenName,
	                                  String    userId,
	                                  String    deviceid,
	                                  String    fromId,
	                                  String    limit) {
    	 
    	 Map<String, AttributeValue> attributeValues = new HashMap<String, AttributeValue>();
    	 System.out.println("Test1_0");
    	 attributeValues.put(":v_bucket_id", new AttributeValue(userTableBuckets.get(userId)));
         attributeValues.put(":v_from_id", new AttributeValue().withN(userId+dateFrom+"."+fromId));//"520150101.1"
         attributeValues.put(":v_to_id", new AttributeValue().withN(userId+dateTo+".99999999999999999999"));//"520150102.1"
         attributeValues.put(":v_device_id", new AttributeValue(deviceid));
         attributeValues.put(":v_scree_name", new AttributeValue(screenName));
    	 
         System.out.println("v_bucket_id:"+userTableBuckets.get(userId));
         System.out.println("v_from_id:"+userId+dateFrom+"."+fromId);
         System.out.println("v_to_id:"+userId+dateTo+".99999999999999999999");         
         
         
         //Get list
         QueryRequest qReq = new QueryRequest(getUserTable(userId))
         	    .withKeyConditionExpression("ub = :v_bucket_id and id between :v_from_id and :v_to_id")
         	    .withFilterExpression("did = :v_device_id and sn = :v_scree_name")
         	    .withExpressionAttributeValues(attributeValues)
        	    .withLimit(Integer.valueOf(limit));

         QueryResult qResult = dynamoDB.query(qReq);
    	 
        
        return qResult;
        
    }  
    
    public boolean deleteSnap(String userId,
                              String fileKey) {

        HashMap<String, AttributeValue> deleteKey = new HashMap<>();
        deleteKey.put("ub", new AttributeValue().withS(userTableBuckets.get(userId)));
        deleteKey.put("id", new AttributeValue().withN(fileKey));

        
        dynamoDB.deleteItem(getUserTable(userId), deleteKey);    	
    	
    	
    	return true;
            
        }    
    
    
}

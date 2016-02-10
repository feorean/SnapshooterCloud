package SnapshooterCloud;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.QueryResult;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import javax.servlet.ServletContext;
import javax.naming.AuthenticationException;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;

import org.json.JSONObject;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Khalid
 */

 
public class Common {
    
    //Declarations
    
    private HttpSession session;
    private HttpServletRequest  request;
    private HttpServletResponse response;
    private static String drupalAddress = null;
    private static String awsCredentials = null;
        
    public enum MessageType {        
        Information, Error        
    }
    
    public static void setDrupalAddress(String url) {
    	
        drupalAddress = url;      	
    }
  
    public  String getDrupalAddress() {    	
    	
        return drupalAddress;      	
    }    
    
    public static void setAWSCredentials(String credentials) {
    	
        awsCredentials = credentials;      	
    }
  
    public  String getAWSCredentials() {    	
    	
        return awsCredentials;      	
    }     
    
    private void loadContextData(ServletContext sc) {
    	
        setDrupalAddress( sc.getInitParameter("drupalAddress"));  
        setAWSCredentials( sc.getInitParameter("aws_credentials_location"));    	
    }
    
    //If you want to Call a URL then you need to call this one
    public Common (HttpServletRequest inRequest, HttpServletResponse inResponse, ServletContext sc) {
        
    	try {
	        this.setRequest(inRequest);    	
	        this.setResponse(inResponse);
	        loadContextData(sc);

    	}
    	catch (Exception e) {
    		
    		System.out.println(e.getMessage());
    	}
    }
    
    public Common (HttpServletRequest inRequest, HttpServletResponse inResponse) {

    	try {
	        this.setRequest(inRequest);    	
	        this.setResponse(inResponse);
    	}
    	catch (Exception e) {
    		
    		System.out.print(e.getMessage());
    	}
      
    }    
    
    public Common (ServletContext sc) {  
    	
    	loadContextData(sc);
    	
    }     
    
    public void setRequest(HttpServletRequest inRequest) throws Exception {
    	
    	if (inRequest != null) {
    		
    		request = inRequest;
    		
    	} else {
    		
    		throw new Exception("Request is null!");
    	}
	
    }
    
    public void setResponse(HttpServletResponse inResponse) throws Exception {
    	
    	if (inResponse != null) {
    	
	        response = inResponse; 
	        response.setContentType("text/html");
	        
    	} else {
    		
    		throw new Exception("Response is null!");
    	}
    }
    
    public HttpServletRequest getRequest() {
    	
    	return request;
    }
    
    public HttpServletResponse getResponse() throws NullPointerException {
    	
    	if (response == null) {
    		
    		throw new NullPointerException("Response is not set");
    	}
    	
    	return response;
    }
        
    
    public void setContentType(String contentType) {
        
        response.setContentType(contentType);
    }
    
    
    public HttpSession getCurrentSession() {
        
        if ( session == null ) {
            
            session=getRequest().getSession(false);
            
        }
        
        return session;
        
    }
    
    public HttpSession createNewSession() {
        
        session=getRequest().getSession();  
        
        return session;
    }
    
    public static String nvl(String value, String alternateValue) {
    if (value == null)
        return alternateValue;

    return value;
    }
    
    private String wrapMessage(MessageType messageType, String message) {
        

        String tmp = null;
        String result = null;

        try {        
        //System.out.println("Message type:"+messageType+" "+message);
        switch (messageType) {
            case Information: 
                tmp = "INFO";
                break;
            case Error:
                tmp = "ERR";
                break;
            default: {
                System.out.println("Wrong message type:"+messageType+" "+message);
                break; 
            }
        }

        if (tmp != null) {
            
            result = "{\""+tmp+"\":\""+nvl(message, " ")+"\"}";
            
        } else {
                       
            System.out.println("result is null"); 
            
        }

       }
        
        catch (NullPointerException e) {
            System.out.println("Got some nulllllllllllllllllllllllll222222222");
        }        
        
        return result;
    }
    
    public void respondMessage(MessageType messageType, String message) {
      
        
        try {
            if ((messageType != null) && (message != null )) {

            	printMessage(wrapMessage(messageType, message));

            }
        }
        catch (NullPointerException e) {
            System.out.println("Got some nulllllllllllllllllllllllll");
        }
    }

    public void respondMessageInfo(String message) {
        
        respondMessage(MessageType.Information, message);
        
    }
    
    public void respondMessageErr(String message) {
        
        respondMessage(MessageType.Error, message);
        
    }
    

    public void printMessage(String message) {

        PrintWriter out = null;
        
        try {
            
                out = getResponse().getWriter();            
        
        }
        catch (IOException e) {
                
            System.err.println("IO Exception: " + e.getMessage());    
        }        
        
        try {
            if ((message != null ) && (out != null)) {

                out.println( message);

            }
        }
        catch (NullPointerException e) {
            System.out.println("Got some nulllllllllllllllllllllllll");
        }
    }    
    
    //Authentication
    
    public void proceedIfAuthenticated() 
            throws AuthenticationException {
            
        if ( getCurrentSession() == null ) { 

            respondMessageErr("NOT_AUTHENTICATED");

            throw new AuthenticationException("NOT_AUTHENTICATED");

        }   
    
    }

    
    
    
    public String wrapArrayToJSON (String arrayName, QueryResult records) {

        if (arrayName == null) {
            
            System.out.println("Array name is empty!");
            return "";
            
        }
        
        if (records == null || records.getCount()==0) {
            
            System.out.println("Array is either null or empty!");
            return "";
            
        }
        
        JSONArray jsonArray = new JSONArray();
        
        List<Map<String, AttributeValue>> items = records.getItems();
        
        Iterator<Map<String, AttributeValue>> iterator = items.iterator();
        while (iterator.hasNext()) {
        	

        	
        	JSONObject obj=new JSONObject();
        	
        	Map<String, AttributeValue> row = iterator.next();        	
        	Iterator<String> columns = row.keySet().iterator();
                	           
            while(columns.hasNext()) {
                
                String columnName = columns.next();
                
                obj.put(columnName, row.get(columnName));                   

            }

            jsonArray.put(obj);        	
        }
        
        JSONObject res=new JSONObject();
            res.put(arrayName, jsonArray);
    
        return res.toString();
    }
    
    public JSONArray strToJSONArray(String arrayName, String jsonData) {
        
        JSONObject obj = new JSONObject(jsonData);
        
        JSONArray array = obj.getJSONArray(arrayName);
        
        return array;
    }
    
    public String executeURLWithParams (String url, List <NameValuePair> params) {
    	
    	String res = null;
    	
    	try {

	        CloseableHttpClient httpclient = HttpClients.createDefault();
	        HttpPost httppost = new HttpPost(getDrupalAddress() +"/"+ url);  
	        
	        if (!params.isEmpty()) { 
	        	httppost.setEntity(new UrlEncodedFormEntity(params));
	        }
	        
	        System.out.println("Executing request: " + httppost.getRequestLine());
	        CloseableHttpResponse httpResponse = httpclient.execute(httppost);
	        
	        res = EntityUtils.toString(httpResponse.getEntity());  
    	}     	
        catch (Error | IOException e) {
                        
            System.err.println(e.toString());
            res = "Url execution failed:"+url;
        }
    	
    	return res;
    	
    }
    
    
    public String validateUser(User user, boolean getHash) {

		String validateUrl = (getHash)?"validateandgethash":"validateuser";
		
        List <NameValuePair> params = new ArrayList <>();
        params.add(new BasicNameValuePair("username", user.getUsername()));
        params.add(new BasicNameValuePair("password", user.getPassword()));

        return executeURLWithParams(validateUrl, params);
    	
    }
    
}

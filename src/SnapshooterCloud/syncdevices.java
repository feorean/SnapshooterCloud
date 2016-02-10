package SnapshooterCloud;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;



/**
 *
 * @author Khalid
 */
@MultipartConfig
public class syncdevices extends HttpServlet {

  
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

    
    @Override
    public void init( ){


    }
    
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

    	 
    	 
    	 //String contextPath= common.getDrupalAddress();
    	    
    	 //response.sendRedirect(response.encodeRedirectURL(contextPath + "/snaps/syncdevice"));
    	  
        
    	System.out.println("Syncyng devices..");
    	
    	Common common = new Common(request, response, getServletContext());    	 
    	 
    	 
    	 
    	 
    	 
         try {
             
             //First check if authenticated  
             common.proceedIfAuthenticated();
             
             String jsonlist = request.getParameter("devicelist");
             String userid = common.getCurrentSession().getAttribute("USERID").toString();
             
             jsonlist = jsonlist.replace("\\", "");
             
             JSONArray devices = common.strToJSONArray("devices", jsonlist);
             
             //System.out.println(devices.toString());
             
             
             final int n = devices.length();
             for (int i = 0; i < n; ++i) {
               final JSONObject device = devices.getJSONObject(i);
               

               
               //Add only if all values are set
               if (device.has("id") && device.getString("id") != null 
                   && device.has("type") && device.getString("type") != null  
                   && device.has("screen") && device.getString("screen") != null) {
               
                     List <NameValuePair> params = new ArrayList <>();
                     params.add(new BasicNameValuePair("uid", common.getCurrentSession().getAttribute("USERID").toString()));
                     params.add(new BasicNameValuePair("did", device.getString("id")));
                     params.add(new BasicNameValuePair("type", device.getString("type")));
                     params.add(new BasicNameValuePair("screen", device.getString("screen")));         
                     
                     /*System.out.println("Uid:"+params. ("uid");
                     System.out.println("Did:"+request.getParameter("did"));
                     System.out.println("Type:"+request.getParameter("type"));
                     System.out.println("Screen:"+request.getParameter("screen"));*/
                     
                     String jsonResponseData = common.executeURLWithParams("syncdevice", params);    	 
                	                      
                     System.out.println("Call sync device URL result:"+jsonResponseData);
                     
               } else {
                   
                   common.respondMessageErr("One of device values is null or not set! ( deviceid, type or screen )!"); 
               }
               
             }
             
            
             
             common.respondMessageInfo("SYNC COMPLETE");

         }
         catch ( Exception e ){
             
             System.err.println("Exception in device sync: " + e.getMessage() ); 
             common.respondMessageErr("ERROR_IN_SYNC_DEVICES");
         }    	 
    	 
    }


    
}

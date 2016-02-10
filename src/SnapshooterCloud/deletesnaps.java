package SnapshooterCloud;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.auth.profile.ProfilesConfigFile;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;

/**
 *
 * @author Khalid
 */
@MultipartConfig
public class deletesnaps extends HttpServlet {

	static Dbapi  dbapi;
	static final long serialVersionUID = 1L;
    static AmazonS3 s3;
    static String bucketName;
    private static String aws_credentials_location = "default";
    
    
    @Override
    public void init( ){

    	Common common = new Common(getServletContext());        
        dbapi = new Dbapi(common);
    	
    	if (common != null) { 
    		
    		aws_credentials_location = common.getAWSCredentials();
    	}    	

    	
        /*
         * The ProfileCredentialsProvider will return your [default]
         * credential profile by reading from the credentials file located at
         * (/Users/Khalid/.aws/credentials).
         */
        AWSCredentials credentials = null;
        try {
        	ProfilesConfigFile cfg = new ProfilesConfigFile(aws_credentials_location);
       	 
        	credentials = new ProfileCredentialsProvider(cfg, "default").getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException(
                    "Cannot load the credentials from the credential profiles file. " +
                    "Please make sure that your credentials file is at the correct " +
                    "location (/Users/Khalid/.aws/credentials), and is in valid format.",
                    e);
        }

        s3 = new AmazonS3Client(credentials);
        Region usWest2 = Region.getRegion(Regions.US_WEST_2);
        s3.setRegion(usWest2);

        bucketName = "snapshooterbucket";

    	
        
    }
    
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Common common = new Common(request, response);
        
//        Enumeration<String> headerNames = request.getHeaderNames();
//
//        while (headerNames.hasMoreElements()) {
//            String headerName = headerNames.nextElement();
//            System.out.print(headerName);
//            //System.out.print("n");
//
//            Enumeration<String> headers = request.getHeaders(headerName);
//            while (headers.hasMoreElements()) {
//                String headerValue = headers.nextElement();
//                System.out.print( headerValue);
//                //System.out.print("n");
//            }
//        }
        
        JSONArray arr = new JSONArray();
        
        try {
            
            //First check if authenticated  
            common.proceedIfAuthenticated();
            
            String jsonlist = request.getParameter("ids");
            String userid = common.getCurrentSession().getAttribute("USERID").toString();
            
            jsonlist = jsonlist.replace("\\", "");
            
            JSONObject deleteList = new JSONObject(jsonlist);
        
            //String deviceId = deleteList.getString("deviceid");
            
            JSONArray files = deleteList.getJSONArray("deletelist");
                       
            
            final int n = files.length();
            for (int i = 0; i < n; ++i) {
              final JSONObject file = files.getJSONObject(i);
              
                String fileKey = userid+file.getString("dt")+"."+String.valueOf(file.getLong("id"));
              
                dbapi.deleteSnap(userid, fileKey);                

                s3.deleteObject(bucketName, "snaps/"+userid+"/"+fileKey);
                
                System.out.println("Deleted Id:"+file.getLong("id"));
                
                arr.put(file.getLong("id"));
                 
            }
            JSONObject obj = new JSONObject().put("DELETED", arr);
            common.printMessage(obj.toString());
            
        }
        catch ( Exception e ){
            
            System.err.println("Exception in deleteds sync: " + e.getMessage() ); 
            common.respondMessageErr("ERROR_IN_SYNC_DELETEDS");
        }
   }


    
}

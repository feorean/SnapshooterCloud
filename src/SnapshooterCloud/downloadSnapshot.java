package SnapshooterCloud;

import java.io.IOException;
import java.io.*;
  
import javax.servlet.ServletException;  
import javax.servlet.http.HttpServlet;  
import javax.servlet.http.HttpServletRequest;  
import javax.servlet.http.HttpServletResponse;  

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.auth.profile.ProfilesConfigFile;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import javax.naming.AuthenticationException;

public class downloadSnapshot extends HttpServlet {  
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	//Global variables

    private String filePath;
    private String fileName;
    private Common common;
    static AmazonS3 s3;
    static String bucketName;    
    private static String aws_credentials_location = "default";
    
    
    @Override
    public void init( ){

    	Common cmn = new Common(getServletContext()); 
    	
    	if (cmn != null) {
    		
    		aws_credentials_location = cmn.getAWSCredentials();
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
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response)  
                    throws ServletException, IOException {  
        
        common = new Common(request, response, getServletContext());

//        Enumeration<String> headerNames = request.getHeaderNames();
//
//        while (headerNames.hasMoreElements()) {
//            String headerName = headerNames.nextElement();
//            
//            //System.out.print("n");
//            
//            String headerValue = "";
//            
//            Enumeration<String> headers = request.getHeaders(headerName);
//            while (headers.hasMoreElements()) {
//                 headerValue = headerValue +" " + headers.nextElement();
//                //System.out.print( headerValue);
//                //System.out.print("n");
//            }
//            
//            System.out.print(headerName + ":" + headerValue);
//        }
                
        
        
        try {
            
            common.proceedIfAuthenticated();

            String userId = common.getCurrentSession().getAttribute("USERID").toString();

            fileName = request.getParameter("filename");
            filePath = "snaps/"+userId+"/";
     
            if ( (fileName == "") || (fileName == null) ) {
                
                common.respondMessageErr("FILE_NAME_INCORRECT");
                return;
            }
           
            common.setContentType("image/png");
            
            OutputStream targetFileStream = response.getOutputStream();
           
            
            
            S3ObjectInputStream sourceFileStream = s3.getObject(bucketName, filePath+fileName)
            									     .getObjectContent(); 
            
            byte[] buffer = new byte[4096];
            int length;
            while ((length = sourceFileStream.read(buffer)) > 0){
                targetFileStream.write(buffer, 0, length);
            }
            sourceFileStream.close();
            targetFileStream.flush();
            
          
        }
        catch (FileNotFoundException fne) {
            
            common.respondMessageErr("FILE_NOT_FOUND");
                      
        } 
        catch ( AuthenticationException | IOException e ){
            
            System.err.println("Exception in download snapshot: " + e.getMessage()); 
            
        }
             
    }  
}  

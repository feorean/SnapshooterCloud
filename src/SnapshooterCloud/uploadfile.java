package SnapshooterCloud;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.*;
/*import javax.imageio.*;
import java.awt.image.*;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import org.apache.commons.io.FilenameUtils;
*/
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import javax.servlet.annotation.*;

import org.json.JSONObject;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.auth.profile.ProfilesConfigFile;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;

import com.amazonaws.services.s3.model.PutObjectResult;



/**
 *
 * @author Khalid
 */
@MultipartConfig
public class uploadfile extends HttpServlet {

   
    /**
	 * 
	 */
	
	static Dbapi  dbapi;
	static final long serialVersionUID = 1L;
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

        Common common = new Common(getServletContext());        
        dbapi = new Dbapi(common);

    }
    
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

    	 Common common = new Common(request, response, getServletContext());
//         common.setRequest(request);  	
//         common.setResponse(response);
        
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
        
        try {
            
            //First check if authenticated  
            common.proceedIfAuthenticated();
            
            String deviceid = request.getParameter("deviceid");
            String screenName= request.getParameter("screenname"); 
            String userId = common.getCurrentSession().getAttribute("USERID").toString();
            String createDate = request.getParameter("createdate"); 
            String localId = request.getParameter("localid");
         
            
            if (deviceid == null || screenName == null || createDate == null || localId == null) {

              common.respondMessageErr("deviceid, screenName or createDate incorrect!"); 

              return;

            }  
            
       
            Part filePart = request.getPart("file"); // Retrieves <input type="file" name="file">
            String fileNameFull = filePart.getSubmittedFileName();            
            //String fileExtention = FilenameUtils.getExtension(fileNameFull);
            //String fileName = FilenameUtils.getBaseName(fileNameFull);
            
            if (fileNameFull == null) {
                
                System.out.println("FileName is null");
                
                return;
            }
            
            if ( filePart.getSize() <= 0 ) {
                
                common.respondMessageErr("EMPTY_FILE");
                
                return;
            }
                     
            PutObjectResult uploadResult = null;
            try {
            	
                InputStream fileContent = filePart.getInputStream();                
                uploadResult = s3.putObject(bucketName, "snaps/"+userId+"/"+fileNameFull, fileContent, null);
            
            } catch (AmazonServiceException ase) {
                System.out.println("Caught an AmazonServiceException, which means your request made it "
                        + "to Amazon S3, but was rejected with an error response for some reason.");
                System.out.println("Error Message:    " + ase.getMessage());
                System.out.println("HTTP Status Code: " + ase.getStatusCode());
                System.out.println("AWS Error Code:   " + ase.getErrorCode());
                System.out.println("Error Type:       " + ase.getErrorType());
                System.out.println("Request ID:       " + ase.getRequestId());
            } catch (AmazonClientException ace) {
                System.out.println("Caught an AmazonClientException, which means the client encountered "
                        + "a serious internal problem while trying to communicate with S3, "
                        + "such as not being able to access the network.");
                System.out.println("Error Message: " + ace.getMessage());
            }            
            
            
            if (uploadResult.getContentMd5().length() > 0) {
            	
            	System.out.println("Successfully uploaded");
            	
            }else {
            	
            	System.out.println("NOT Uploaded");
            	return;            	
            }
            
            /* //Resize the image
            BufferedImage originalImage = ImageIO.read(new File(filePath + File.separator + fileNameFull));
            int type = originalImage.getType() == 0? BufferedImage.TYPE_INT_ARGB : originalImage.getType();
			
            BufferedImage resizedImage = resizeImage(originalImage, originalImage.getWidth()/4, 
                                                                         originalImage.getHeight()/4, type);
            
            ImageIO.write(resizedImage, "jpg", new File(filePath + File.separator + fileName+"_XS." + fileExtention)); 
            */    
            

            boolean insertResult = dbapi.addSnapRecordToDB(createDate.substring(0,8),
                                    createDate,
                                    fileNameFull, 
                                    String.valueOf(filePart.getSize()),
                                    screenName,
                                    userId,
                                    deviceid,
                                    localId
                                   );
            
            
            if (insertResult) {                
                //System.err.println("OK2");
                //common.respondMessageInfo("SUCCESSFULLY_UPLOADED:"+fileNameFull);          
                JSONObject obj = new JSONObject().put("SUCCESSFULLY_UPLOADED", fileNameFull);
                common.printMessage(obj.toString());
                //System.err.println("OK3");  
            } else {
                
                System.out.println("Not inserted!");
            }
        }
        catch ( Exception e ){
            
            System.err.println("Exception in upload snapshot: " + e.getMessage() ); 
            common.respondMessageErr("ERROR_IN_UPLOAD");
        }
   }

/*
    private static BufferedImage resizeImage(BufferedImage originalImage, int imgWidth, int imgHeight, int type){
        
	BufferedImage resizedImage = new BufferedImage(imgWidth, imgHeight, type);
	Graphics2D g = resizedImage.createGraphics();
	g.drawImage(originalImage, 0, 0, imgWidth, imgHeight, null);
	g.dispose();
		
	return resizedImage;
    }
*/    
    
}

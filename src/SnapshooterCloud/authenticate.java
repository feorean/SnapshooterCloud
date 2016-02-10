package SnapshooterCloud;

import java.io.IOException;  
  
import javax.servlet.ServletException;  
import javax.servlet.http.HttpServlet;  
import javax.servlet.http.HttpServletRequest;  
import javax.servlet.http.HttpServletResponse;  
import javax.servlet.http.HttpSession;  
import javax.servlet.annotation.MultipartConfig;
import org.json.JSONObject;

@MultipartConfig
public class authenticate extends HttpServlet {  
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	//Global variables
    Common common;

    void printSuccessAuth() {
    	
        JSONObject obj = new JSONObject().put("AUTHENTICATED", "YES");
        common.printMessage(obj.toString());    	
    }
     
    protected void doPost(HttpServletRequest request, HttpServletResponse response)  
                    throws ServletException, IOException {  

    	String validationStatus = null;
        common = new Common(request, response, getServletContext());
        
        try {
        
		        if( common.getCurrentSession() != null ){  
		 
		        	printSuccessAuth(); 
		            
		            //common.getCurrentSession().invalidate();
		            return;
		        } 
		        
		        User user = new User();
		        
		        user.setUsername(request.getParameter("username"));            
		        user.setPassword(request.getParameter("password"));  
		        boolean getHashKey = (request.getParameterMap().containsKey("gethash"));
		    	
		        System.out.println("Authenticating user:"+user.getUsername());
		        System.out.println("With password:"+user.getPassword());		        
		        
		        if (user.getUsername() == null || user.getPassword() == null) {
		            
			           //common.respondMessageErr("USERNAME_PASSWORD_INCORRECT"); 
			        	throw new Exception("USERNAME_PASSWORD_INCORRECT");
			            
			        }		        
		        
		        validationStatus = common.validateUser(user, getHashKey);
		
		        System.out.println("Validation response status:"+validationStatus);
		    	
		        if (validationStatus == null)  {
		        	
		        	throw new Exception("AUTHENTICATION_FAILED");
		        }
		        

		        JSONObject validationResponse = new JSONObject(validationStatus);

		        System.out.println("Auth validation response:"+validationResponse.toString());

		        if (validationResponse.has("ERR")) {
		        
		        	throw new Exception(validationResponse.get("ERR").toString());
		        }
		        
		        if (validationResponse.has("HASH")) {
		        	
		        	user.setPassword(validationResponse.getString("HASH"));
		        	
		        } 	        
		       
		        if (validationResponse.has("ID")) {
		        	
		        	user.setUserid(validationResponse.getString("ID"));
		        	
		        } /*else {
		        	
		        	throw new Exception("INVALID_RESPONSE="+validationResponse.toString());
		        }*/		        
	        
		        //System.out.println("Auth validation response:"+validationResponse.toString());
		        
		        
		        if ( user.getUserid() != null ) {  
		                              
		            HttpSession session = common.createNewSession();
		            
		            if (session != null) {
		
		                session.setAttribute("USERID", user.getUserid());
		                //common.respondMessageInfo("AUTHENTICATED");
		                printSuccessAuth();
		                
		                System.out.println("New Session has been created!");
		            }
		                    
		        }  
		        else{  
		            
		            common.respondMessageErr("PASSWORD_INCORRECT");  
		            
		        } 
        
        }
        
        catch (Exception e) {
        	
        	 common.respondMessageErr(e.getMessage());
        }
        
        //finalizeRequest();
        
    }  
}  

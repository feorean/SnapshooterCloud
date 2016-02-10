package SnapshooterCloud;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Khalid
 */

import java.io.IOException;  

import javax.servlet.ServletException;  
import javax.servlet.http.HttpServlet;  
import javax.servlet.http.HttpServletRequest;  
import javax.servlet.http.HttpServletResponse;  

//import org.json.JSONObject;

import javax.persistence.EntityNotFoundException;
import javax.servlet.annotation.MultipartConfig;




@MultipartConfig
public class validateuser extends HttpServlet {  
    
    //Global variables

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Common common;
 
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)  
                    throws ServletException, IOException {  
        
        //System.out.println("Username:"+request.getParameter("username"));
        //System.out.println("Password:"+request.getParameter("password"));        
        
        common = new Common(request, response, getServletContext());

        try {
            
            User user = new User();
            boolean getHashKey = (request.getParameterMap().containsKey("gethash"));
            
            user.setUsername(request.getParameter("username"));            
            user.setPassword(request.getParameter("password")); 
            System.out.println("Validating user:"+user.getUsername());
            System.out.println("with password:"+user.getPassword());            
            

            if (user.getUsername() == null || user.getPassword() == null ) {
                
                System.out.println("NOT_ALL_VALUES_SUPPLIED:");
                System.out.println("username:" + user.getUsername());
                System.out.println("password:" + user.getPassword());
                
                throw new NullPointerException();                
            }
            
            String str = common.validateUser(user, getHashKey);
            
            //JSONObject obj = new JSONObject().put("HASH", getHashKey);
            //obj.put("ID", )
            
            
            
            common.printMessage(str);

        System.out.println(str);
            
          
        }

        catch ( EntityNotFoundException e ) {
            
            common.respondMessageErr("VALIDATION_FAILED");
            
        } 
        
        catch ( NullPointerException e ) {
            
            common.respondMessageErr("VALIDATION_FAILED");
        } 
        
        catch (Error e) {
            
            common.respondMessageErr("VALIDATION_FAILED");
            System.err.println(e.toString());
        }
             
    }  
}  


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

import com.amazonaws.services.dynamodbv2.model.QueryResult;
import javax.naming.AuthenticationException;


public class listfiles extends HttpServlet {  
    
    //Global variables

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Common common;
    private Dbapi dbapi;    

    @Override
    public void init( ){
      
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)  
                    throws ServletException, IOException {  
        
        common = new Common(request, response, getServletContext());

        dbapi = new Dbapi(common);
        
        try {
            
            common.proceedIfAuthenticated();

            String userId = common.getCurrentSession().getAttribute("USERID").toString();
            
            String dateFrom = request.getParameter("datefrom");
            String dateTo = request.getParameter("dateto");
            String screenName = request.getParameter("screenname");            
            String deviceId = request.getParameter("deviceid");
            String fromId =  Common.nvl(request.getParameter("fromid"), "0");
            String limit = Common.nvl(request.getParameter("limit"), "20");
            
            System.out.println("fromId="+fromId);
            System.out.println("limit="+limit); 
            
            if ((dateFrom==null) || (dateTo==null) || (screenName==null) || (userId==null) || (deviceId==null) ) {
                
                System.out.println("One of the values is null");
                System.out.println("dateFrom="+dateFrom);
                System.out.println("dateTo="+dateTo);
                System.out.println("screenName="+screenName);
                System.out.println("userId="+userId);
                System.out.println("deviceId="+deviceId);
                System.out.println("fromId="+fromId); 
                System.out.println("limit="+limit);                 

                return;
            }
            
            //Test
            System.out.println("dateFrom="+dateFrom);
            System.out.println("dateTo="+dateTo);
            System.out.println("screenName="+screenName);
            System.out.println("userId="+userId);
            System.out.println("deviceId="+deviceId);
            System.out.println("fromId="+fromId); 
            System.out.println("limit="+limit);    
            //end
            
            QueryResult results = dbapi.getFileList(  dateFrom, 
	                                                  dateTo, 
	                                                  screenName, 
	                                                  userId, 
	                                                  deviceId,
	                                                  fromId,
	                                                  limit);
            if (results.getCount() > 0) {
            	
            	common.printMessage( common.wrapArrayToJSON("Records", results) );
            	
            } else {
            	
            	common.respondMessageErr("NO_DATA");
            }
            
        }

        catch ( AuthenticationException e ){
            
            System.err.println("Exception in getting file list: " + e.getMessage()); 
            
        } 
             
    }  
}  


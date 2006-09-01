package edu.northwestern.bioinformatics.studycalendar.web.accesscontrol;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;
import gov.nih.nci.security.*;


public class SecureOperation extends TagSupport
{
	private String operation;
 private String element;

 public void setOperation(String val)
 {
   this.operation=val;
 }
 public String getOperation()
 {
   return this.operation;
 }
 public void setElement(String val)
 {
   this.element= val;
 }
 public String getElement()
 {
   return this.element;
 }
 
 	public int doStartTag() throws JspTagException {
 		
               
        AuthorizationManager authorizationManager = null;
        //ThreadLocal tlData = new ThreadLocal();
        
        String userName = (String) LocalUser.getInstance();
        System.out.println("username   ---------          " + userName);
        try{
        	authorizationManager = SecurityServiceProvider.getAuthorizationManager("study_calendar");
        }catch(Exception e){
        	System.out.println("message : " + e.getMessage());
        	e.printStackTrace();
        	throw new JspTagException(e.getMessage());
        }
               
        try{
        	
        	return isAllowed(authorizationManager, userName, getElement(), getOperation());
        	
        }catch(Exception ex){
      	
        	ex.printStackTrace();
        	throw new JspTagException(ex.getMessage());
        }

    }

    public int doEndTag() throws JspTagException {
        
                  return this.EVAL_PAGE;
    }

    private int isAllowed(AuthorizationManager i_authorizationManager, String i_userName, String i_element, String i_operation)throws Exception{
        boolean allowed = i_authorizationManager.checkPermission(i_userName, i_element, i_operation);
        int k =0;
        if(!allowed){
       	 	k = this.SKIP_BODY;       
        }else{
        	k=this.EVAL_BODY_INCLUDE;
        }
        return k;
    }
        
}
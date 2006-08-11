package edu.northwestern.bioinformatics.studycalendar.web;


/**
 * @author Padmaja Vedula
 * Login Credentials class. 
 */

public class LoginCommand {

    private String userId;
    private String password;
    
    public LoginCommand(){
    }

    /**
     * @return Returns the password.
     */
    public String getPassword() {
        return password;
    }
    
    /**
     * @param password 
     */
    public void setPassword(String password) {
        this.password = password;
    }
    
    /**
     * @return Returns the userId.
     */
    public String getUserId() {
        return userId;
    }

    /**
     * 
     * @param userId
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }
}

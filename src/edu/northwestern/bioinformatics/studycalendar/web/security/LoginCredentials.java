
package edu.northwestern.bioinformatics.studycalendar.web.security;


/**
 * @author Padmaja Vedula
 * Login Credentials class. 
 */

public class LoginCredentials {

	public LoginCredentials(){
		
	}
	
	private String userId;

	private String password;
	
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
	 * @param userId
	 */
	public void setUserId(String userId) {
		this.userId = userId;
	}
}

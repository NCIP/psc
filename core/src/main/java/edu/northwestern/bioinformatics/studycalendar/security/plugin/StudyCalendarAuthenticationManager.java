package edu.northwestern.bioinformatics.studycalendar.security.plugin;

import org.acegisecurity.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import gov.nih.nci.security.util.StringEncrypter;
import gov.nih.nci.security.util.StringUtilities;


/**
 * @author Jalpa Patel
 */
public class StudyCalendarAuthenticationManager{
    private DataSource dataSource;
    private static final Logger log = LoggerFactory.getLogger(StudyCalendarAuthenticationManager.class);

    public boolean authenticate(Authentication authentication) {
        Boolean value = validate(authentication.getPrincipal().toString(),authentication.getCredentials().toString());
        if(value == false)
            throw new BadCredentialsException("Invalid username or password");
        return true;
    }

    private boolean validate(String userName, String password) throws StudyCalendarValidationException
        {
          if (null == userName || userName.trim().length() == 0)
          {
              throw new StudyCalendarValidationException("User Name cannot be blank");
          }
          if (null == password || password.trim().length() == 0)
          {
              throw new StudyCalendarValidationException("Password cannot be blank");
          }
		  try {
             Connection connection = dataSource.getConnection();
             if (connection == null)
		       {
			      return false;
		       }
             String encryptedPassword;
             StringEncrypter se;
             try {
                  se = new StringEncrypter();
			      encryptedPassword = se.encrypt(new String(password));
             } catch (StringEncrypter.EncryptionException ep) {
                  throw new StudyCalendarSystemException("Error while Encrypting password",ep);
			 }

             encryptedPassword = StringUtilities.initTrimmedString(encryptedPassword);
             String query="SELECT * FROM CSM_USER WHERE LOGIN_NAME = (SELECT NAME FROM USERS WHERE NAME = ? and ACTIVE_FLAG = ?) and PASSWORD =?";
             return executeQuery(connection, query, userName,encryptedPassword);
              
          } catch (SQLException le) {
               throw new StudyCalendarSystemException("Error while introspecting the database", le);
          }
    }

    private static boolean executeQuery(Connection connection, String query, String userID, String password) throws StudyCalendarValidationException
	{
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		boolean validLogin = false;
        boolean active_flag = true;
		try
		{
		    statement = connection.prepareStatement(query);
			statement.setString(1, userID);
            statement.setBoolean(2,active_flag);
			statement.setString(3,password);
			resultSet = statement.executeQuery();
			if (resultSet != null)
			{
                while(resultSet.next())
				{
					validLogin = true;
					break;
				}
			}
		} catch (SQLException e) {
			throw new StudyCalendarSystemException("Error while executing the query to validate user credentials",e);
		} finally {
			try
			{
				if (resultSet != null) resultSet.close();
				if (statement != null) statement.close();
				if (connection != null) connection.close();
			} catch (SQLException sqe) {
					log.error("Closing the connection {} failed", connection, sqe);
			}
		}
		return validLogin;
	}

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }
}

package edu.northwestern.bioinformatics.studycalendar.security.plugin.websso;

import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.userdetails.User;
import org.acegisecurity.userdetails.UserDetails;

/**
 * @author Saurabh Agrawal
 */
public class WebSSOUser extends User {

    private String gridId;
    private String delegatedEPR;
    private String firstName;
    private String lastName;


    public WebSSOUser(String string, String string1, boolean b, boolean b1, boolean b2, boolean b3, GrantedAuthority[] grantedAuthorities) throws IllegalArgumentException {
        super(string, string1, b, b1, b2, b3, grantedAuthorities);
    }

    public WebSSOUser(UserDetails user) {
        this(user.getUsername(), user.getPassword(), true, true, true, true,
                user.getAuthorities());
    }

    public String getGridId() {
        return gridId;
    }

    public void setGridId(String gridId) {
        this.gridId = gridId;
    }


    public String getDelegatedEPR() {
        return delegatedEPR;
    }

    public void setDelegatedEPR(String delegatedEPR) {
        this.delegatedEPR = delegatedEPR;
    }


    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }


}

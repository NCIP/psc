package edu.northwestern.bioinformatics.studycalendar.web.setup;

import org.springframework.webflow.core.collection.MutableAttributeMap;

import java.io.Serializable;

/**
 * @author Jalpa Patel
 */
public class SelectAuthenticationSystemCommand implements Serializable {
    private String authenticationSystem;

    public void apply(MutableAttributeMap scope) {
      scope.put("authenticationSystemValue", getAuthenticationSystem());
    }
   
    public void setAuthenticationSystem(String authenticationSystem) {
        this.authenticationSystem = authenticationSystem;
    }

    public String getAuthenticationSystem() {
        return authenticationSystem;
    }
}

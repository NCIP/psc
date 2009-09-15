package edu.northwestern.bioinformatics.studycalendar.web.setup;

import org.springframework.webflow.execution.RequestContext;

/**
 * @author Jalpa Patel
 */
public class SelectAuthenticationSystemCommand {
    private RequestContext context;
    private String authenticationSystem;

    public SelectAuthenticationSystemCommand(RequestContext context) {
      this.context = context;
    }

    public void apply() {
      context.getFlowScope().put("authenticationSystemValue", getAuthenticationSystem());
    }
   
    public void setAuthenticationSystem(String authenticationSystem) {
        this.authenticationSystem = authenticationSystem;
    }

    public String getAuthenticationSystem() {
        return authenticationSystem;
    }
}

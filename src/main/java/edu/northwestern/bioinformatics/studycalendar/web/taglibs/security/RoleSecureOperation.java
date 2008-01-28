package edu.northwestern.bioinformatics.studycalendar.web.taglibs.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.Authentication;
import org.acegisecurity.GrantedAuthority;

import javax.servlet.jsp.tagext.TagSupport;
import javax.servlet.jsp.JspTagException;

import edu.northwestern.bioinformatics.studycalendar.domain.Role;

import java.util.List;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: nshurupova
 * Date: Oct 16, 2007
 * Time: 2:52:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class RoleSecureOperation extends TagSupport {
    private static Logger log = LoggerFactory.getLogger(RoleSecureOperation.class);

    private String element;

    private final String SUBJECT_COORDINATOR_STRING = "Subject Coordinator";
    private final String STUDY_COORDINATOR_STRING = "Study Coordinator";
    private final String SITE_COORDINATOR_STRING = "Site Coordinator";
    private final String STUDY_ADMIN_STRING = "Study Admin";
    private final String SYSTEM_ADMIN_STRING = "System Admin";

    public void setElement(String val) {
        this.element = val;
    }

    public String getElement() {
        return this.element;
    }

    public int doStartTag() throws JspTagException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        GrantedAuthority[] authorities = authentication.getAuthorities();

        Role elementRole = getElementRole(getElement());

        try {
            return isAllowed(authorities, elementRole);
        } catch (Exception e) {
            log.error("Exception evaluating SecureOperation startTag", e);
            throw new JspTagException(e.getMessage() + " (Rethrown exception; see log for details)");
        }
    }                                                                                         

    public int doEndTag() throws JspTagException {
        return EVAL_PAGE;
    }

    protected int isAllowed(GrantedAuthority[] authorities, Role elementRole) throws Exception {
        boolean allowed = true;
        List<String> authorityList = new ArrayList<String>();
        for (int i = 0; i < authorities.length; i++) {
            authorityList.add(authorities[i].toString());
        }
        if (!authorityList.contains(elementRole.toString())){
            allowed = false;
        }
        int k = 0;
        if (!allowed) {
            k = SKIP_BODY;
        } else {
            k = EVAL_BODY_INCLUDE;
        }
        return k;
    }

    protected Role getElementRole(String role) {
        log.info("Method getElementRoles: Looking up role for " + getElement());
        if (SUBJECT_COORDINATOR_STRING.equals(role)) {
            return Role.SUBJECT_COORDINATOR;
        } else if (SITE_COORDINATOR_STRING.equals(role)) {
            return Role.SITE_COORDINATOR;
        } else if (STUDY_COORDINATOR_STRING.equals(role)) {
            return Role.STUDY_COORDINATOR;
        } else if (STUDY_ADMIN_STRING.equals(role)) {
            return Role.STUDY_ADMIN;
        } else if (SYSTEM_ADMIN_STRING.equals(role)) {
            return Role.SYSTEM_ADMINISTRATOR;
        }
        return null;
    }



    public void release() {
        super.release();
    }
}
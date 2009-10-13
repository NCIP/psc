package edu.northwestern.bioinformatics.studycalendar.web;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.northwestern.bioinformatics.studycalendar.web.osgi.InstalledAuthenticationSystem;

/**
 * @author Nataliya Shurupova
 */
public class LoginController extends AbstractController {
    private InstalledAuthenticationSystem installedAuthenticationSystem;

    protected ModelAndView handleRequestInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
        if (installedAuthenticationSystem.getAuthenticationSystem().usesLocalPasswords()) {
            return new ModelAndView("login");
        } else {
            return new ModelAndView("redirectToRoot");
        }
    }

    public void setInstalledAuthenticationSystem(InstalledAuthenticationSystem installedAuthenticationSystem) {
        this.installedAuthenticationSystem = installedAuthenticationSystem;
    }
}


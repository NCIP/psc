package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.web.osgi.InstalledAuthenticationSystem;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Nataliya Shurupova
 */
public class LoginController extends AbstractController {
    private InstalledAuthenticationSystem installedAuthenticationSystem;

    protected ModelAndView handleRequestInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
        if (installedAuthenticationSystem.getAuthenticationSystem().usesLocalLoginScreen()) {
            return new ModelAndView("login");
        } else {
            return new ModelAndView("redirectToRoot");
        }
    }

    public void setInstalledAuthenticationSystem(InstalledAuthenticationSystem installedAuthenticationSystem) {
        this.installedAuthenticationSystem = installedAuthenticationSystem;
    }
}


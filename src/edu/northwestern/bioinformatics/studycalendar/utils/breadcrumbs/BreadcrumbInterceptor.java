package edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

import edu.northwestern.bioinformatics.studycalendar.domain.DomainObject;
import edu.northwestern.bioinformatics.studycalendar.utils.DomainObjectTools;

/**
 * @author Rhett Sutphin
 */
public class BreadcrumbInterceptor extends HandlerInterceptorAdapter {
    private BreadcrumbCreator breadcrumbCreator;

    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView mv) throws Exception {
        if (!(handler instanceof CrumbSource)) return;
        if (isRedirect(mv)) return;
        CrumbSource src = (CrumbSource) handler;
        mv.getModel().put(
            "breadcrumbs",
            breadcrumbCreator.createAnchors(src, createContext(mv.getModel()))
        );
    }

    BreadcrumbContext createContext(Map<String, Object> model) {
        DomainObject basis = null;
        // look through the context for the most specific model object
        for (Object o : model.values()) {
            if (o instanceof DomainObject) {
                DomainObject domainObject = (DomainObject) o;
                if (basis == null || DomainObjectTools.isMoreSpecific(domainObject.getClass(), basis.getClass())) {
                    basis = domainObject;
                }
            }
        }
        return BreadcrumbContext.create(basis);
    }

    private boolean isRedirect(ModelAndView mv) {
        boolean namedRedirect = mv.getViewName() != null && mv.getViewName().startsWith("redirect");
        return mv.getView() instanceof RedirectView || namedRedirect;
    }

    ////// CONFIGURATION

    public void setBreadcrumbCreator(BreadcrumbCreator breadcrumbCreator) {
        this.breadcrumbCreator = breadcrumbCreator;
    }
}

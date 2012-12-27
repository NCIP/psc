/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs;

import edu.northwestern.bioinformatics.studycalendar.domain.DomainObjectTools;
import edu.northwestern.bioinformatics.studycalendar.domain.Population;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Revision;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.service.DomainContext;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import gov.nih.nci.cabig.ctms.domain.DomainObject;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @author Rhett Sutphin
 */
public class BreadcrumbInterceptor extends HandlerInterceptorAdapter {
    private TemplateService templateService;
    private BreadcrumbCreator breadcrumbCreator;

    @Override
    @SuppressWarnings({ "unchecked" })
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView mv) throws Exception {
        if (!(handler instanceof CrumbSource)) return;
        if (mv == null) return;
        if (isRedirect(mv)) return;
        CrumbSource src = (CrumbSource) handler;
        DomainContext context = createContext(mv.getModel());
        mv.getModel().put(
            "breadcrumbs",
            breadcrumbCreator.createAnchors(src, context)
        );
        mv.getModel().put(
            "domainContext", context
        );
    }

    DomainContext createContext(Map<String, Object> model) {
        DomainObject basis = null;
        // look through the context for the most specific model object
        for (Object o : model.values()) {
            if (isBreadcrumbCompatible(o)) {
                DomainObject domainObject = (DomainObject) o;
                if (basis == null || DomainObjectTools.isMoreSpecific(domainObject.getClass(), basis.getClass())) {
                    basis = domainObject;
                }
            }
        }
        DomainContext context = DomainContext.create(basis, templateService);
        // look for other model fields which can be set into the context
        for (Object o : model.values()) {
            if (o instanceof Amendment) {
                context.setAmendment((Amendment) o);
            }
            if (o instanceof StudySite) {
                context.setStudySite((StudySite) o);
            }
            if (o instanceof Site) {
                context.setSite((Site) o);
            }
            if (o instanceof Population) {
                context.setPopulation((Population) o);
            }
            if (o instanceof PscUser) {
                context.setUser((PscUser) o);
            }
        }
        return context;
    }

    // XXX: quick hack
    private boolean isBreadcrumbCompatible(Object o) {
        return o instanceof DomainObject && !(o instanceof Revision);
    }

    private boolean isRedirect(ModelAndView mv) {
        boolean namedRedirect = mv.getViewName() != null && mv.getViewName().startsWith("redirect");
        return mv.getView() instanceof RedirectView || namedRedirect;
    }

    ////// CONFIGURATION

    @Required
    public void setBreadcrumbCreator(BreadcrumbCreator breadcrumbCreator) {
        this.breadcrumbCreator = breadcrumbCreator;
    }

    @Required
    public void setTemplateService(TemplateService templateService) {
        this.templateService = templateService;
    }
}

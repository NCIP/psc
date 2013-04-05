/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs;

import gov.nih.nci.cabig.ctms.tools.spring.ControllerUrlResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.Ordered;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import edu.northwestern.bioinformatics.studycalendar.service.DomainContext;

/**
 * @author Rhett Sutphin
 */
public class BreadcrumbCreator implements Ordered, BeanFactoryPostProcessor {
    private final Logger log = LoggerFactory.getLogger(BreadcrumbCreator.class);

    private ControllerUrlResolver urlResolver;
    private Map<Crumb, String> names;

    public int getOrder() { return 3; }

    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        names = new HashMap<Crumb, String>();
        Map<String, CrumbSource> beans = beanFactory.getBeansOfType(CrumbSource.class, false, true);
        for (Map.Entry<String, CrumbSource> entry : beans.entrySet()) {
            names.put(entry.getValue().getCrumb(), entry.getKey());
        }
    }

    public List<Anchor> createAnchors(CrumbSource current, DomainContext domainContext) {
        log.debug("Creating anchors for source of type " + current.getClass().getName());
        List<Crumb> ancestry = new LinkedList<Crumb>();
        buildAncestry(current, ancestry);

        List<Anchor> anchors = new LinkedList<Anchor>();
        for (Crumb crumb : ancestry) {
            Map<String, String> params = crumb.getParameters(domainContext);

            anchors.add(new Anchor(
                createUrl(names.get(crumb), params),
                crumb.getName(domainContext)
            ));
        }

        return anchors;
    }

    private String createUrl(String controllerName, Map<String, String> params) {
        StringBuilder baseUrl = new StringBuilder(urlResolver.resolve(controllerName).getUrl(true));
        if (params != null && params.size() > 0) {
            baseUrl.append('?');
            for (Iterator<Map.Entry<String, String>> it = params.entrySet().iterator(); it.hasNext();) {
                Map.Entry<String, String> entry = it.next();
                baseUrl.append(entry.getKey()).append('=').append(entry.getValue());
                if (it.hasNext()) {
                    baseUrl.append('&');
                }
            }
        }
        return baseUrl.toString();
    }

    private void buildAncestry(CrumbSource child, List<Crumb> family) {
        if (child.getCrumb() == null) {
            log.debug("{} ({}) did not provide a crumb", child, child.getClass().getName());
            return;
        }
        CrumbSource parent = child.getCrumb().getParent();
        if (parent != null) buildAncestry(parent, family);
        family.add(child.getCrumb());
    }

    ////// CONFIGURATION

    @Required
    public void setUrlResolver(ControllerUrlResolver urlResolver) {
        this.urlResolver = urlResolver;
    }
}

/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.setup;

import edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.Membrane;
import edu.northwestern.bioinformatics.studycalendar.web.admin.AuthenticationSystemDirectory;
import org.osgi.framework.BundleContext;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.webflow.action.FormAction;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.ScopeType;

/**
 * @author Jalpa Patel
 */
public class SelectAuthenticationSystemFormAction extends FormAction {
    private BundleContext bundleContext;
    private Membrane membrane;

    public SelectAuthenticationSystemFormAction() {
        super(SelectAuthenticationSystemCommand.class);
        setFormObjectName("selectAuthenticationSystemCommand");
        setFormObjectScope(ScopeType.FLOW);
    }

    public Event setupReferenceData(RequestContext context) throws Exception {
        MutableAttributeMap requestScope = context.getRequestScope();
        requestScope.put("directory", new AuthenticationSystemDirectory(bundleContext, membrane));
        return success();
    }

    @Override
    protected Object createFormObject(RequestContext context){
        return new SelectAuthenticationSystemCommand();
    }

   ////// CONFIGURATION
    @Required
    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }
    @Required
    public void setMembrane(Membrane membrane) {
        this.membrane = membrane;
    }
 }


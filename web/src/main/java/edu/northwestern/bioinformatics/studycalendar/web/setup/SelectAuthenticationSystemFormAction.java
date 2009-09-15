package edu.northwestern.bioinformatics.studycalendar.web.setup;

import org.springframework.webflow.action.FormAction;
import org.springframework.webflow.execution.ScopeType;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.beans.factory.annotation.Required;
import org.osgi.framework.BundleContext;
import edu.northwestern.bioinformatics.studycalendar.web.admin.AuthenticationSystemDirectory;
import edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.Membrane;

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

    protected Object createFormObject(RequestContext context){
        SelectAuthenticationSystemCommand command = new SelectAuthenticationSystemCommand(context);
        return command;
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


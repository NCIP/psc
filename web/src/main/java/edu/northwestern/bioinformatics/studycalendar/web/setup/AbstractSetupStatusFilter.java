/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.setup;

import edu.northwestern.bioinformatics.studycalendar.core.setup.SetupStatus;
import gov.nih.nci.cabig.ctms.web.filters.ContextRetainingFilterAdapter;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;

/**
 * @author Rhett Sutphin
 */
public class AbstractSetupStatusFilter extends ContextRetainingFilterAdapter {
    private final String alreadyCheckedAttributeName = getClass().getName() + ".SETUP_ALREADY_CHECKED";

    private SetupStatus status;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        super.init(filterConfig);
        status = (SetupStatus) getApplicationContext().getBean("setupStatus");
    }

    protected SetupStatus getSetupStatus() {
        return status;
    }

    protected boolean setupAlreadyChecked() {
        Boolean attribute = (Boolean) getServletContext().getAttribute(alreadyCheckedAttributeName);
        return attribute != null && attribute;
    }

    protected void noteSetupAlreadyChecked() {
        getServletContext().setAttribute(alreadyCheckedAttributeName, true);
    }
}

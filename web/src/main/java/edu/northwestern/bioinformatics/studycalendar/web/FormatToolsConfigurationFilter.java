/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.configuration.Configuration;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.tools.FormatTools;

import javax.servlet.*;
import java.io.IOException;

/**
 * @author Nataliya Shurupova
 */
public class FormatToolsConfigurationFilter implements Filter {
    public void init(FilterConfig filterConfig) throws ServletException { }

    public void doFilter(
            ServletRequest request, ServletResponse response, FilterChain chain
    ) throws IOException, ServletException {
        gov.nih.nci.cabig.ctms.tools.configuration.Configuration configuration =
                (gov.nih.nci.cabig.ctms.tools.configuration.Configuration) request.getAttribute("configuration");
        if (configuration == null) {
            throw new StudyCalendarSystemException("configuration not present in request attributes");
        }
        String displayDateFormat = convertedDateFormat(configuration.get(Configuration.DISPLAY_DATE_FORMAT));
        FormatTools.setLocal(new FormatTools(displayDateFormat));

        chain.doFilter(request, response);
        FormatTools.clearLocalInstance();
    }

    private String convertedDateFormat(String configuredDateFormat) {
        return configuredDateFormat.replace("YYYY", "yyyy").replace("DD", "dd");
    }

    public void destroy() { }
}

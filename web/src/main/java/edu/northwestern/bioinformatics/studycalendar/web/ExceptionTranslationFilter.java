/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web;

import gov.nih.nci.cabig.ctms.web.filters.ContextRetainingFilterAdapter;
import org.springframework.web.bind.MissingServletRequestParameterException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * This filter catches some known exceptions and translates them into appropriate HTTP error codes.
 *
 * @author Rhett Sutphin
 */
public class ExceptionTranslationFilter extends ContextRetainingFilterAdapter {

    @Override
    public void doFilter(
        ServletRequest request, ServletResponse response, FilterChain filterChain
    ) throws IOException, ServletException {
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        try {
            filterChain.doFilter(request, response);
        } catch (MissingServletRequestParameterException missing) {
            treatAsBadRequest(httpResponse, missing);
        } catch (MissingRequiredBoundProperty missing) {
            treatAsBadRequest(httpResponse, missing);
        }
    }

    private void treatAsBadRequest(HttpServletResponse httpResponse, Throwable throwable) throws IOException {
        log.debug("Translating exception into HTTP 400 (BAD REQUEST)", throwable);
        httpResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, throwable.getMessage());
    }
}

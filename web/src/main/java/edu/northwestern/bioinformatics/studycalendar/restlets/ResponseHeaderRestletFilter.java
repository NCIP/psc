/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.restlets;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Parameter;
import org.restlet.engine.http.HttpRequest;
import org.restlet.engine.http.HttpResponse;
import org.restlet.routing.Filter;
import org.restlet.util.Series;

import java.util.Set;

/**
 * @author Jalpa Patel
 */
public class ResponseHeaderRestletFilter extends Filter {

    @Override
    protected void afterHandle(Request request, Response response) {
        String ua = ((HttpRequest)request).getHttpCall().getRequestHeaders().getValues("User-Agent");
        boolean isMSIE = (ua != null && ua.indexOf("MSIE") != -1);
        if (isMSIE) {
            Set<String> headerNames = ((HttpResponse) response).getHttpCall().getResponseHeaders().getNames();
            headerNames.remove("Cache-Control");
            headerNames.remove("Pragma");
        } else {
            // default to no caching
            Series<Parameter> headers = ((HttpResponse) response).getHttpCall().getResponseHeaders();
            headers.add("Cache-Control", "no-store, no-cache, must-revalidate, post-check=0, pre-check=0");
            headers.add("Pragma", "no-cache");
        }
        super.afterHandle(request,response);
    }
}


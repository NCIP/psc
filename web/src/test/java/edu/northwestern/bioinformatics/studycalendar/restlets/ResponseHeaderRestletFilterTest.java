/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.restlets;

import org.restlet.data.Parameter;
import org.restlet.util.Series;

import java.util.Set;

/**
 * @author Jalpa Patel
 */
public class ResponseHeaderRestletFilterTest extends RestletTestCase {
    private ResponseHeaderRestletFilter filter;
    private MockRestlet next;
    private Series<Parameter> requestHeaders;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        useHttpRequest();

        next = new MockRestlet();

        filter = new ResponseHeaderRestletFilter();
        filter.setNext(next);
        requestHeaders = request.getHttpCall().getRequestHeaders();
    }

    public void testResponseHeaderForIE() throws Exception {
        requestHeaders.add("User-Agent","Mozilla/4.0(PC) (compatible; MSIE 7.0; Windows NT 5.1; SV1; .NET CLR 1.1.4322)");
        replayMocks();
        filter.afterHandle(request, response);
        verifyMocks();
        Set<String> headerNames = response.getHttpCall().getResponseHeaders().getNames();
        assertNotContains("Response should not contain cache header", headerNames, "Cache-Control");
        assertNotContains("Response should not contain Pragma", headerNames, "Pragma");
    }

    public void testResponseHeaderForFireFox() throws Exception {
        requestHeaders.add("User-Agent","Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.5; en-US; rv:1.9.2.12) Gecko/20101026 Firefox/3.6.12");
        replayMocks();
        filter.afterHandle(request, response);
        verifyMocks();
        Set<String> headerNames = response.getHttpCall().getResponseHeaders().getNames();
        assertContains("Response should contain cache header", headerNames, "Cache-Control");
        assertContains("Response should contain Pragma", headerNames, "Pragma");
    }
}

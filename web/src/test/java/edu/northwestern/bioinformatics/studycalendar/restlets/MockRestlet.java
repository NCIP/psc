/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.ApplicationSecurityManager;
import org.restlet.Restlet;
import org.restlet.Request;
import org.restlet.Response;

/**
 * @author Jalpa Patel
*/
final class MockRestlet extends Restlet {
    private Request lastRequest;
    private Response lastResponse;
    private RuntimeException toThrow;
    private String securityContextUser;

    private ApplicationSecurityManager applicationSecurityManager =
        new ApplicationSecurityManager();

    @Override
    public void handle(Request request, Response response) {
        this.lastRequest = request;
        this.lastResponse = response;
        if (toThrow != null) {
            throw toThrow;
        }
        securityContextUser = applicationSecurityManager.getUserName();
    }

    public boolean handleCalled() {
        return lastRequest != null;
    }

    public void setException(RuntimeException toThrow) {
        this.toThrow = toThrow;
    }

    public String getSecurityContextUser() {
        return securityContextUser;
    }
}

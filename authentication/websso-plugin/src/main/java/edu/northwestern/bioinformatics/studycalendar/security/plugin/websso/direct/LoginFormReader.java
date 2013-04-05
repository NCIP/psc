/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.security.plugin.websso.direct;

import edu.northwestern.bioinformatics.studycalendar.security.plugin.cas.direct.CasDirectException;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Rhett Sutphin
 */
public class LoginFormReader extends edu.northwestern.bioinformatics.studycalendar.security.plugin.cas.direct.LoginFormReader {
    public LoginFormReader(String body) {
        super(body);
    }

    /**
     * Extracts a mapping from visible names to authentication service URLs.  Derived from
     * the SELECT which is labeled "organization" in the WebSSO UI.
     *
     * @return
     */
    public Map<String, String> getAuthenticationServices() {
        NodeList selects = getDocument().getElementsByTagName("select");
        for (int i = 0; i < selects.getLength(); i++) {
            Element select = (Element) selects.item(i);
            if ("authenticationServiceURL".equals(select.getAttribute("id"))) {
                return extractAuthenticationServiceMap(select);
            }
        }
        failWithNoAuthenticationServices();
        return null; // unreachable
    }

    private Map<String, String> extractAuthenticationServiceMap(Element authenticationServiceUrlSelect) {
        Map<String, String> orgs = new LinkedHashMap<String, String>();
        NodeList options = authenticationServiceUrlSelect.getElementsByTagName("option");
        for (int i = 0; i < options.getLength(); i++) {
            Element option = (Element) options.item(i);
            String key = option.getTextContent();
            String value = option.getAttribute("value");
            if (!"-".equals(value)) {
                orgs.put(key, value);
            }
        }
        if (orgs.size() == 0) failWithNoAuthenticationServices();
        return orgs;
    }

    public boolean hasUsernameAndPasswordFields() {
        boolean hasUsername = false, hasPassword = false;

        NodeList inputs = getDocument().getElementsByTagName("input");
        for (int i = 0; i < inputs.getLength(); i++) {
            Element input = (Element) inputs.item(i);
            hasUsername = hasUsername || "username".equals(input.getAttribute("name"));
            hasPassword = hasPassword || "password".equals(input.getAttribute("name"));
        }

        return hasUsername && hasPassword;
    }

    private void failWithNoAuthenticationServices() {
        throw new CasDirectException("The WebSSO login page does not include a list of authenticationServiceURLs.  Cannot use direct login.");
    }
}

/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.restlets;

public class UserActionsResourceTest extends AuthorizedResourceTestCase<UserActionsResource> {
    @Override
    @SuppressWarnings({ "unchecked" })
    protected UserActionsResource createAuthorizedResource() {
        return new UserActionsResource();
    }

    public void testPostAllowed() throws Exception {
        assertAllowedMethods("POST");
    }
}

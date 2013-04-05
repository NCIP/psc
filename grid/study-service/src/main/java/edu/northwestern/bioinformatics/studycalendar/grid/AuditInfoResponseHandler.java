/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.grid;

import gov.nih.nci.cabig.ctms.audit.domain.DataAuditInfo;
import org.apache.axis.AxisFault;
import org.apache.axis.MessageContext;
import org.apache.axis.handlers.BasicHandler;

/**
 * @author Saurabh Agrawal
 */
public class AuditInfoResponseHandler extends BasicHandler {

    /* (non-Javadoc)
     * @see org.apache.axis.Handler#invoke(org.apache.axis.MessageContext)
     */
    public void invoke(MessageContext context) throws AxisFault {
        DataAuditInfo.setLocal(null);
    }
}
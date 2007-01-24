/**
 * 
 */
package edu.northwestern.bioinformatics.studycalendar.grid;

import org.apache.axis.AxisFault;
import org.apache.axis.MessageContext;
import org.apache.axis.handlers.BasicHandler;

import edu.northwestern.bioinformatics.studycalendar.domain.auditing.DataAuditInfo;

/**
 * @author <a href="mailto:joshua.phillips@semanticbits.com>Joshua Phillips</a>
 *
 */
//TODO: refactor to use common code among grid component impls
public class AuditInfoResponseHandler extends BasicHandler {

    /* (non-Javadoc)
     * @see org.apache.axis.Handler#invoke(org.apache.axis.MessageContext)
     */
    public void invoke(MessageContext context) throws AxisFault {
        DataAuditInfo.setLocal(null);
    }

}

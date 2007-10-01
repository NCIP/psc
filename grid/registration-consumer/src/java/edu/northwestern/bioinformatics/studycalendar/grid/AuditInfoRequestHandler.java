/**
 * 
 */
package edu.northwestern.bioinformatics.studycalendar.grid;

import gov.nih.nci.cabig.ctms.audit.domain.DataAuditInfo;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.apache.axis.AxisFault;
import org.apache.axis.MessageContext;
import org.apache.axis.handlers.BasicHandler;
import org.apache.axis.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.globus.wsrf.security.SecurityManager;

/**
 * @author <a href="mailto:joshua.phillips@semanticbits.com>Joshua Phillips</a>
 * 
 */
public class AuditInfoRequestHandler extends BasicHandler {

	private static final Log logger = LogFactory.getLog(AuditInfoRequestHandler.class);

	/*
	 * (non-Javadoc)
	 * @see org.apache.axis.Handler#invoke(org.apache.axis.MessageContext)
	 */
	public void invoke(final MessageContext context) throws AxisFault {

		HttpServletRequest request = (HttpServletRequest) context.getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
		String identity = SecurityManager.getManager().getCaller();
		if (identity == null) {
			identity = "ANONYMOUS";
		}
		logger.debug("Auditing request from " + identity);
		String info = null;
		// try {
		// info = context.getCurrentMessage().getSOAPEnvelope().getAsString();
		// } catch (Exception ex) {
		// logger.error("Error serializing message to string: " + ex.getMessage(), ex);
		// }
		info = request.getRequestURI();
		gov.nih.nci.cabig.ctms.audit.domain.DataAuditInfo.setLocal(new DataAuditInfo(identity, request.getRemoteAddr(),
				new Date(), info));

	}

}

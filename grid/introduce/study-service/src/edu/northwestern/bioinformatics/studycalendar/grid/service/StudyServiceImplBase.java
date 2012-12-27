/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.grid.service;

import edu.northwestern.bioinformatics.studycalendar.grid.service.globus.resource.BaseResource;
import org.apache.axis.MessageContext;
import org.globus.wsrf.*;

import javax.naming.InitialContext;
import javax.xml.namespace.QName;
import java.rmi.RemoteException;
/** 
 * Provides some simple accessors for the Impl.
 * 
 * @created by Introduce Toolkit version 1.0
 * 
 */
public abstract class StudyServiceImplBase {
	
	public StudyServiceImplBase() throws RemoteException {
	
	}
	
	public ServiceConfiguration getConfiguration() throws Exception {
		return ServiceConfiguration.getConfiguration();
	}
	
	
	public edu.northwestern.bioinformatics.studycalendar.grid.service.globus.resource.BaseResourceHome getResourceHome() throws Exception {
		ResourceHome resource = getResourceHome("home");
		return (edu.northwestern.bioinformatics.studycalendar.grid.service.globus.resource.BaseResourceHome)resource;
	}

	
	
	
	
	protected ResourceHome getResourceHome(String resourceKey) throws Exception {
		MessageContext ctx = MessageContext.getCurrentContext();

		ResourceHome resourceHome = null;
		
		String servicePath = ctx.getTargetService();

		String jndiName = Constants.JNDI_SERVICES_BASE_NAME + servicePath + "/" + resourceKey;
		try {
			javax.naming.Context initialContext = new InitialContext();
			resourceHome = (ResourceHome) initialContext.lookup(jndiName);
		} catch (Exception e) {
			throw new Exception("Unable to instantiate resource home. : " + resourceKey, e);
		}

		return resourceHome;
	}
	
	
	
	
	protected gov.nih.nci.cagrid.metadata.ServiceMetadata getServiceMetadataMD(){
		BaseResource serviceBaseResource;
		try {
			serviceBaseResource = (BaseResource)ResourceContext.getResourceContext().getResource();
		} catch (ResourceContextException e) {
			return null;
		} catch (ResourceException e) {
			return null;
		}
		return serviceBaseResource.getServiceMetadataMD();
	}

		
	
	
	protected Object getMetadata(QName metadataQName) {
		BaseResource serviceBaseResource = null;
		try {
			serviceBaseResource = (BaseResource) ResourceContext.getResourceContext().getResource();
		} catch (ResourceContextException e) {
			return null;
		} catch (ResourceException e) {
			return null;
		}
		ResourcePropertySet resourcePropertySet = serviceBaseResource.getResourcePropertySet();
		if (resourcePropertySet != null) {
			ResourceProperty property = resourcePropertySet.get(metadataQName);
			if (property != null) {
				return property.get(0);
			}

		}
		return null;
	}
	


}


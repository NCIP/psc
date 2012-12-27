/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.grid.service.globus.resource;

public class ResourceConfiguration {
	private String registrationTemplateFile;
	private boolean performRegistration;
	private String serviceMetadataFile;



	public boolean shouldPerformRegistration() {
		return performRegistration;
	}


	public void setPerformRegistration(boolean performRegistration) {
		this.performRegistration = performRegistration;
	}


	public String getRegistrationTemplateFile() {
		return registrationTemplateFile;
	}


	public void setRegistrationTemplateFile(String registrationTemplateFile) {
		this.registrationTemplateFile = registrationTemplateFile;
	}
	
	
	
	public String getServiceMetadataFile() {
		return serviceMetadataFile;
	}
	
	
	public void setServiceMetadataFile(String serviceMetadataFile) {
		this.serviceMetadataFile = serviceMetadataFile;
	}
		
}

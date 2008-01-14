/**
 * StudyServicePortType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2RC2 Apr 28, 2006 (12:42:00 EDT) WSDL2Java emitter.
 */

package edu.northwestern.bioinformatics.studycalendar.grid.stubs;

public interface StudyServicePortType extends java.rmi.Remote {
    public edu.northwestern.bioinformatics.studycalendar.grid.stubs.RetrieveStudyByAssignedIdentifierResponse retrieveStudyByAssignedIdentifier(edu.northwestern.bioinformatics.studycalendar.grid.stubs.RetrieveStudyByAssignedIdentifierRequest parameters) throws java.rmi.RemoteException, edu.northwestern.bioinformatics.studycalendar.grid.stubs.types.StudyDoesNotExists;
    public edu.northwestern.bioinformatics.studycalendar.grid.stubs.CreateStudyResponse createStudy(edu.northwestern.bioinformatics.studycalendar.grid.stubs.CreateStudyRequest parameters) throws java.rmi.RemoteException, edu.northwestern.bioinformatics.studycalendar.grid.stubs.types.StudyAlreadyException;
    public org.oasis.wsrf.properties.GetResourcePropertyResponse getResourceProperty(javax.xml.namespace.QName getResourcePropertyRequest) throws java.rmi.RemoteException, org.oasis.wsrf.properties.InvalidResourcePropertyQNameFaultType, org.oasis.wsrf.properties.ResourceUnknownFaultType;
    public org.oasis.wsrf.properties.GetMultipleResourcePropertiesResponse getMultipleResourceProperties(org.oasis.wsrf.properties.GetMultipleResourceProperties_Element getMultipleResourcePropertiesRequest) throws java.rmi.RemoteException, org.oasis.wsrf.properties.InvalidResourcePropertyQNameFaultType, org.oasis.wsrf.properties.ResourceUnknownFaultType;
    public org.oasis.wsrf.properties.QueryResourcePropertiesResponse queryResourceProperties(org.oasis.wsrf.properties.QueryResourceProperties_Element queryResourcePropertiesRequest) throws java.rmi.RemoteException, org.oasis.wsrf.properties.InvalidResourcePropertyQNameFaultType, org.oasis.wsrf.properties.InvalidQueryExpressionFaultType, org.oasis.wsrf.properties.QueryEvaluationErrorFaultType, org.oasis.wsrf.properties.ResourceUnknownFaultType, org.oasis.wsrf.properties.UnknownQueryExpressionDialectFaultType;
    public gov.nih.nci.cagrid.introduce.security.stubs.GetServiceSecurityMetadataResponse getServiceSecurityMetadata(gov.nih.nci.cagrid.introduce.security.stubs.GetServiceSecurityMetadataRequest parameters) throws java.rmi.RemoteException;
}

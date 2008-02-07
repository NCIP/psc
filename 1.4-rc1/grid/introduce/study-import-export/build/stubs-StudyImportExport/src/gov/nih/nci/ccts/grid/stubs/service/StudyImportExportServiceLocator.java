/**
 * StudyImportExportServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2RC2 Apr 28, 2006 (12:42:00 EDT) WSDL2Java emitter.
 */

package gov.nih.nci.ccts.grid.stubs.service;

public class StudyImportExportServiceLocator extends org.apache.axis.client.Service implements gov.nih.nci.ccts.grid.stubs.service.StudyImportExportService {

    public StudyImportExportServiceLocator() {
    }


    public StudyImportExportServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public StudyImportExportServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for StudyImportExportPortTypePort
    private java.lang.String StudyImportExportPortTypePort_address = "http://localhost:8080/wsrf/services/";

    public java.lang.String getStudyImportExportPortTypePortAddress() {
        return StudyImportExportPortTypePort_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String StudyImportExportPortTypePortWSDDServiceName = "StudyImportExportPortTypePort";

    public java.lang.String getStudyImportExportPortTypePortWSDDServiceName() {
        return StudyImportExportPortTypePortWSDDServiceName;
    }

    public void setStudyImportExportPortTypePortWSDDServiceName(java.lang.String name) {
        StudyImportExportPortTypePortWSDDServiceName = name;
    }

    public gov.nih.nci.ccts.grid.stubs.StudyImportExportPortType getStudyImportExportPortTypePort() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(StudyImportExportPortTypePort_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getStudyImportExportPortTypePort(endpoint);
    }

    public gov.nih.nci.ccts.grid.stubs.StudyImportExportPortType getStudyImportExportPortTypePort(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            gov.nih.nci.ccts.grid.stubs.bindings.StudyImportExportPortTypeSOAPBindingStub _stub = new gov.nih.nci.ccts.grid.stubs.bindings.StudyImportExportPortTypeSOAPBindingStub(portAddress, this);
            _stub.setPortName(getStudyImportExportPortTypePortWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setStudyImportExportPortTypePortEndpointAddress(java.lang.String address) {
        StudyImportExportPortTypePort_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (gov.nih.nci.ccts.grid.stubs.StudyImportExportPortType.class.isAssignableFrom(serviceEndpointInterface)) {
                gov.nih.nci.ccts.grid.stubs.bindings.StudyImportExportPortTypeSOAPBindingStub _stub = new gov.nih.nci.ccts.grid.stubs.bindings.StudyImportExportPortTypeSOAPBindingStub(new java.net.URL(StudyImportExportPortTypePort_address), this);
                _stub.setPortName(getStudyImportExportPortTypePortWSDDServiceName());
                return _stub;
            }
        }
        catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        java.lang.String inputPortName = portName.getLocalPart();
        if ("StudyImportExportPortTypePort".equals(inputPortName)) {
            return getStudyImportExportPortTypePort();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://grid.ccts.nci.nih.gov/StudyImportExport/service", "StudyImportExportService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://grid.ccts.nci.nih.gov/StudyImportExport/service", "StudyImportExportPortTypePort"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        if ("StudyImportExportPortTypePort".equals(portName)) {
            setStudyImportExportPortTypePortEndpointAddress(address);
        }
        else { // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(javax.xml.namespace.QName portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}

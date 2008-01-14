/**
 * StudyServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2RC2 Apr 28, 2006 (12:42:00 EDT) WSDL2Java emitter.
 */

package edu.northwestern.bioinformatics.studycalendar.grid.stubs.service;

public class StudyServiceLocator extends org.apache.axis.client.Service implements edu.northwestern.bioinformatics.studycalendar.grid.stubs.service.StudyService {

    public StudyServiceLocator() {
    }


    public StudyServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public StudyServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for StudyServicePortTypePort
    private java.lang.String StudyServicePortTypePort_address = "http://localhost:8080/wsrf/services/";

    public java.lang.String getStudyServicePortTypePortAddress() {
        return StudyServicePortTypePort_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String StudyServicePortTypePortWSDDServiceName = "StudyServicePortTypePort";

    public java.lang.String getStudyServicePortTypePortWSDDServiceName() {
        return StudyServicePortTypePortWSDDServiceName;
    }

    public void setStudyServicePortTypePortWSDDServiceName(java.lang.String name) {
        StudyServicePortTypePortWSDDServiceName = name;
    }

    public edu.northwestern.bioinformatics.studycalendar.grid.stubs.StudyServicePortType getStudyServicePortTypePort() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(StudyServicePortTypePort_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getStudyServicePortTypePort(endpoint);
    }

    public edu.northwestern.bioinformatics.studycalendar.grid.stubs.StudyServicePortType getStudyServicePortTypePort(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            edu.northwestern.bioinformatics.studycalendar.grid.stubs.bindings.StudyServicePortTypeSOAPBindingStub _stub = new edu.northwestern.bioinformatics.studycalendar.grid.stubs.bindings.StudyServicePortTypeSOAPBindingStub(portAddress, this);
            _stub.setPortName(getStudyServicePortTypePortWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setStudyServicePortTypePortEndpointAddress(java.lang.String address) {
        StudyServicePortTypePort_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (edu.northwestern.bioinformatics.studycalendar.grid.stubs.StudyServicePortType.class.isAssignableFrom(serviceEndpointInterface)) {
                edu.northwestern.bioinformatics.studycalendar.grid.stubs.bindings.StudyServicePortTypeSOAPBindingStub _stub = new edu.northwestern.bioinformatics.studycalendar.grid.stubs.bindings.StudyServicePortTypeSOAPBindingStub(new java.net.URL(StudyServicePortTypePort_address), this);
                _stub.setPortName(getStudyServicePortTypePortWSDDServiceName());
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
        if ("StudyServicePortTypePort".equals(inputPortName)) {
            return getStudyServicePortTypePort();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://grid.studycalendar.bioinformatics.northwestern.edu/StudyService/service", "StudyService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://grid.studycalendar.bioinformatics.northwestern.edu/StudyService/service", "StudyServicePortTypePort"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        if ("StudyServicePortTypePort".equals(portName)) {
            setStudyServicePortTypePortEndpointAddress(address);
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

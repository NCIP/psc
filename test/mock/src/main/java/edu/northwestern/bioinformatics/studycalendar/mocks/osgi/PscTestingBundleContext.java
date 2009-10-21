package edu.northwestern.bioinformatics.studycalendar.mocks.osgi;

import org.springframework.osgi.mock.MockBundleContext;
import org.springframework.osgi.mock.MockServiceReference;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.InvalidSyntaxException;

import java.util.Map;
import java.util.HashMap;

/**
 * @author Rhett Sutphin
 */
public class PscTestingBundleContext extends MockBundleContext {
    protected Map<String, TestingBundleDetails> testingDetails;

    public PscTestingBundleContext() {
        testingDetails = new HashMap<String, TestingBundleDetails>();
    }

    /**
     * "Registers" the object as the sole implementation of the given interface for
     * this context.
     * 
     * @param serviceInterface
     * @param instance
     */
    public void addService(Class<?> serviceInterface, Object instance) {
        testingDetails.put(
            serviceInterface.getName(), new TestingBundleDetails(serviceInterface, instance));
    }

    @Override
    public ServiceReference getServiceReference(String s) {
        return testingDetails.get(s).getServiceReference();
    }

    @Override
    public Object getService(ServiceReference serviceReference) {
        for (TestingBundleDetails details : testingDetails.values()) {
            if (details.getServiceReference() == serviceReference) return details.instance;
        }
        return null;
    }

    @Override
    public ServiceReference[] getServiceReferences(String clazz, String filter) throws InvalidSyntaxException {
        TestingBundleDetails details = testingDetails.get(clazz);
        if (details == null) {
            return null;
        } else {
            return new ServiceReference[] { details.getServiceReference() };
        }
    }

    protected static class TestingBundleDetails {
        private String serviceName;
        private Object instance;
        private ServiceReference serviceReference;

        private TestingBundleDetails(Class<?> serviceInterface, Object instance) {
            this.serviceName = serviceInterface.getName();
            this.instance = instance;
            this.serviceReference = new MockServiceReference(
                new String[] { serviceName });
        }

        public String getServiceName() {
            return serviceName;
        }

        public Object getInstance() {
            return instance;
        }

        public ServiceReference getServiceReference() {
            return serviceReference;
        }
    }
}

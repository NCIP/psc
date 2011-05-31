package edu.northwestern.bioinformatics.studycalendar.mocks.osgi;

import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.osgi.mock.MockBundleContext;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Rhett Sutphin
 */
public class PscTestingBundleContext extends MockBundleContext {
    private final Logger log = LoggerFactory.getLogger(getClass());

    protected Map<String, List<TestingBundleDetails>> testingDetails;

    public PscTestingBundleContext() {
        testingDetails = new HashMap<String, List<TestingBundleDetails>>();
    }

    /**
     * "Registers" the object as the sole implementation of the given interface for
     * this context.
     * 
     * @param serviceInterface
     * @param instance
     */
    public void addService(Class<?> serviceInterface, Object instance) {
        registerService(serviceInterface.getName(), instance, null);
    }

    @Override
    @SuppressWarnings( { "RawUseOfParameterizedType" })
    public ServiceRegistration registerService(
        String[] clazzes, Object service, Dictionary properties
    ) {
        ServiceRegistration reg = super.registerService(clazzes, service, properties);

        for (String interfaze : clazzes) {
            if (!testingDetails.containsKey(interfaze)) {
                testingDetails.put(interfaze, new ArrayList<TestingBundleDetails>());
            }
            testingDetails.get(interfaze).add(new TestingBundleDetails(reg, service));
        }
        return reg;
    }

    @Override
    public ServiceReference getServiceReference(String s) {
        List<TestingBundleDetails> allBundles = testingDetails.get(s);
        if (allBundles != null) {
            if (allBundles.size() > 1) {
                log.info("There are multiple {} services registered. Returning the first one.", s);
            }
            return allBundles.get(0).getServiceReference();
        } else {
            return null;
        }
    }

    @Override
    public Object getService(ServiceReference serviceReference) {
        for (List<TestingBundleDetails> services : testingDetails.values()) {
            for (TestingBundleDetails service : services) {
                if (service.getServiceReference() == serviceReference) return service.instance;
            }
        }
        return null;
    }

    @Override
    public ServiceReference[] getServiceReferences(String clazz, String filter) throws InvalidSyntaxException {
        List<TestingBundleDetails> details = testingDetails.get(clazz);
        if (details == null) {
            return null;
        } else {
            ServiceReference[] refs = new ServiceReference[details.size()];
            for (int i = 0; i < refs.length; i++) {
                refs[i] = details.get(i).getServiceReference();
            }
            return refs;
        }
    }

    protected static class TestingBundleDetails {
        private Object instance;
        private ServiceRegistration serviceRegistration;

        private TestingBundleDetails(ServiceRegistration registration, Object instance) {
            this.instance = instance;
            this.serviceRegistration = registration;
        }

        public Object getInstance() {
            return instance;
        }

        public ServiceReference getServiceReference() {
            return serviceRegistration.getReference();
        }
    }
}

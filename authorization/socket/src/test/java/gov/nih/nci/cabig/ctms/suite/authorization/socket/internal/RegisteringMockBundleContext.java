/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package gov.nih.nci.cabig.ctms.suite.authorization.socket.internal;

import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.springframework.osgi.mock.MockBundleContext;
import org.springframework.osgi.mock.MockServiceRegistration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Rhett Sutphin
 */
public class RegisteringMockBundleContext extends MockBundleContext {
    private List<UnregisterableMockServiceRegistration> registrations;
    private Map<ServiceReference, Object> registeredServices;

    public RegisteringMockBundleContext() {
        this.registrations = new LinkedList<UnregisterableMockServiceRegistration>();
        this.registeredServices = new HashMap<ServiceReference, Object>();
    }

    public List<UnregisterableMockServiceRegistration> getRegistrations() {
        return registrations;
    }

    @Override
    public ServiceReference[] getAllServiceReferences(String clazz, String filter) throws InvalidSyntaxException {
        List<ServiceReference> found = new ArrayList<ServiceReference>(registrations.size());

        for (UnregisterableMockServiceRegistration registration : registrations) {
            if (registration.getInterfaces().contains(clazz)) {
                found.add(registration.getReference());
            }
        }

        if (found.size() == 0) {
            return null;
        } else {
            return found.toArray(new ServiceReference[found.size()]);
        }
    }

    @Override
    public Object getService(ServiceReference reference) {
        return registeredServices.get(reference);
    }

    @Override
    @SuppressWarnings( { "RawUseOfParameterizedType" })
    public ServiceRegistration registerService(String[] clazzes, Object service, Dictionary properties) {
        UnregisterableMockServiceRegistration reg = new UnregisterableMockServiceRegistration(clazzes, service, properties);
        registrations.add(reg);
        registeredServices.put(reg.getReference(), service);
        return reg;
    }

    public class UnregisterableMockServiceRegistration extends MockServiceRegistration {
        private String[] interfaces;
        private Object service;

        @SuppressWarnings( { "RawUseOfParameterizedType" })
        private UnregisterableMockServiceRegistration(
            String[] clazz, Object service, Dictionary props
        ) {
            super(clazz, props);
            this.interfaces = clazz;
            this.service = service;
        }

        public List<String> getInterfaces() {
            return Arrays.asList(interfaces);
        }

        public Object getService() {
            return this.service;
        }

        @Override
        public void unregister() {
            registrations.remove(this);
        }
    }
}

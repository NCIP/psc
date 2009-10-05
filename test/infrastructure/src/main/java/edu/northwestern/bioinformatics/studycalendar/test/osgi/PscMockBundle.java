package edu.northwestern.bioinformatics.studycalendar.test.osgi;

import org.osgi.framework.ServiceReference;
import org.springframework.osgi.mock.MockBundle;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;

import edu.northwestern.bioinformatics.studycalendar.tools.MapBuilder;

/**
 * @author Rhett Sutphin
 */
@SuppressWarnings({ "RawUseOfParameterizedType" })
public class PscMockBundle extends MockBundle {
    private int state;
    private String symbolicName;
    private List<ServiceReference> registeredServices;

    public PscMockBundle(Dictionary headers) {
        super(headers);
        registeredServices = new ArrayList<ServiceReference>();
    }

    @Override
    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    @Override
    public String getSymbolicName() {
        return symbolicName;
    }

    public void setSymbolicName(String symbolicName) {
        this.symbolicName = symbolicName;
    }

    public void addRegisteredService(ServiceReference ref) {
        registeredServices.add(ref);
    }

    public void clearRegisteredServices() {
        registeredServices.clear();
    }

    @Override
    public ServiceReference[] getRegisteredServices() {
        if (registeredServices.isEmpty()) {
            return null;
        } else {
            return registeredServices.toArray(new ServiceReference[registeredServices.size()]);
        }
    }

    public static PscMockBundle create(
        int id, final String symbolicName, final int mockState, String version,
        String name, String description
    ) {
        PscMockBundle bundle = new PscMockBundle(
            new MapBuilder<String, Object>().
                put("Bundle-Version", version).
                put("Bundle-Name", name).
                put("Bundle-Description", description).
                toDictionary()
        );
        bundle.setState(mockState);
        bundle.setSymbolicName(symbolicName);
        bundle.setBundleId(id);
        return bundle;
    }
}

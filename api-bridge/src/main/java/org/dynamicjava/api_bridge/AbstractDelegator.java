package org.dynamicjava.api_bridge;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.ArrayList;

/**
 * @author Rhett Sutphin
 */
public abstract class AbstractDelegator {
    protected final Logger log = LoggerFactory.getLogger(getClass());
    private final Object delegate;
    private final ApiBridge apiBridge;
    private final ApiBridge delegateApiBridge;

    public AbstractDelegator(Object delegate, ApiBridge apiBridge) {
        this.delegate = delegate;
        this.apiBridge = apiBridge;
        delegateApiBridge = apiBridge.getReverseApiBridge(delegate.getClass().getClassLoader());
    }

    protected Object[] bridgeObjects(Object[] args, Class<?>[] targetTypes) {
        if (args == null) {
            return null;
        }

        List<Object> result = new ArrayList<Object>();
        for (int i = 0; i < args.length; i++) {
            result.add(reverseBridge(args[i], targetTypes[i].getClassLoader()));
        }
        return result.toArray();
    }

    protected Object reverseBridge(Object arg) {
        return reverseBridge(arg, null);
    }

    protected Object reverseBridge(Object arg, ClassLoader loader) {
        ApiBridge reverseBridge;
        if (loader == delegate.getClass().getClassLoader()) {
            reverseBridge = getDelegateApiBridge();
        } else {
            reverseBridge = apiBridge.getReverseApiBridge(loader);
        }

        if (reverseBridge == null) {
            return arg;
        } else {
            log.debug("Bridging object {} using bridge {} with loader {}", new Object[] { arg, Integer.toHexString(System.identityHashCode(reverseBridge)), reverseBridge.getTargetApiClassLoader() });
            return reverseBridge.bridge(arg, true);
        }
    }

    protected Object getDelegate() {
        return delegate;
    }

    protected ApiBridge getApiBridge() {
        return apiBridge;
    }

    protected ApiBridge getDelegateApiBridge() {
        return delegateApiBridge;
    }
}

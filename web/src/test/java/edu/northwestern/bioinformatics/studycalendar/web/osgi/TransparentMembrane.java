package edu.northwestern.bioinformatics.studycalendar.web.osgi;

import edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.Membrane;

/**
 * @author Rhett Sutphin
*/
public class TransparentMembrane extends Membrane {
    @Override
    public Object farToNear(Object farObject) {
        return farObject;
    }

    @Override
    public Object traverse(Object object, ClassLoader newCounterpartClassLoader, ClassLoader newCounterpartReverseClassLoader) {
        return object;
    }
}

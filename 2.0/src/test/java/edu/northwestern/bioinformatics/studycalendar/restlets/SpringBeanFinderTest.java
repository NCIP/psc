package edu.northwestern.bioinformatics.studycalendar.restlets;

import junit.framework.TestCase;
import org.springframework.beans.factory.BeanFactory;
import static org.easymock.EasyMock.*;
import org.restlet.resource.Resource;

/**
 * @author Rhett Sutphin
 */
public class SpringBeanFinderTest extends TestCase {
    private static final String BEAN_NAME = "fish";

    private SpringBeanFinder finder;
    private BeanFactory beanFactory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        beanFactory = createMock(BeanFactory.class);
        finder = new SpringBeanFinder(beanFactory, BEAN_NAME);
    }

    public void testReturnsCorrectBeanWhenExists() throws Exception {
        Resource expectedResource = new Resource();
        expect(beanFactory.getBean(BEAN_NAME)).andReturn(expectedResource);

        replay(beanFactory);
        Resource actual = finder.createResource();
        verify(beanFactory);

        assertSame("Wrong resource returned", expectedResource, actual);
    }

    public void testExceptionWhenBeanNotPresent() throws Exception {
        expect(beanFactory.getBean(BEAN_NAME)).andReturn(null);

        replay(beanFactory);
        try {
            finder.createResource();
            fail("Exception not thrown");
        } catch (NullPointerException npe) {
            assertEquals("No bean named fish", npe.getMessage());
        }
    }
    
    public void testExceptionWhenBeanIsWrongType() throws Exception {
        expect(beanFactory.getBean(BEAN_NAME)).andReturn("Wrong thing");

        replay(beanFactory);
        try {
            finder.createResource();
            fail("Exception not thrown");
        } catch (ClassCastException cce) {
            assertEquals("fish does not resolve to an instance of org.restlet.resource.Resource", cce.getMessage());
        }
    }
}

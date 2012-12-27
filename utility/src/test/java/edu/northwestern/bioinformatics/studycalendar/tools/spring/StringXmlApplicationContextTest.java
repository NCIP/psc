/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.tools.spring;

import edu.northwestern.bioinformatics.studycalendar.tools.spring.StringXmlApplicationContext;
import junit.framework.TestCase;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.StaticApplicationContext;

import java.util.List;

/**
 * @author Rhett Sutphin
 */
public class StringXmlApplicationContextTest extends TestCase {
    public void testBeansAreRecoverableFromCreatedContext() throws Exception {
        String xml = createBeansXml(new StringBuilder().
            append("  <bean id='str' class='java.lang.String'>\n").
            append("    <constructor-arg index='0' value='wonko'/>\n").
            append("  </bean>\n").
            toString()
        );
        ApplicationContext actual = new StringXmlApplicationContext(null, xml);
        assertEquals("wonko", actual.getBean("str"));
    }

    private String createBeansXml(String beanXml) {
        return new StringBuilder().
            append("<beans xmlns='http://www.springframework.org/schema/beans'\n").
            append("       xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n").
            append("       xsi:schemaLocation=\"\n").
            append("http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-2.0.xsd\n").
            append("       \">\n").
            append(beanXml).
            append("</beans>").
            toString();
    }

    @SuppressWarnings({ "RawUseOfParameterizedType" })
    public void testUsesParentApplicationContextIfProvided() throws Exception {
        StaticApplicationContext parent = new StaticApplicationContext();
        parent.registerSingleton("someBean", Bean.class);
        parent.refresh();

        ApplicationContext actual = new StringXmlApplicationContext(parent, createBeansXml(new StringBuilder().
            append("  <bean id='listOfBeans' class='java.util.LinkedList'>\n").
            append("    <constructor-arg index='0'><list><ref bean='someBean'/></list></constructor-arg>\n").
            append("  </bean>\n").
            toString()));

        assertTrue("Bean not of expected type", actual.getBean("listOfBeans") instanceof List);
        List actualList = (List) actual.getBean("listOfBeans");
        assertEquals(1, actualList.size());
        assertTrue(actualList.get(0) instanceof Bean);
    }

    public static class Bean { }
}

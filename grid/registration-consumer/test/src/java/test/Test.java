/**
 * 
 */
package test;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author <a href="mailto:joshua.phillips@semanticbits.com>Joshua Phillips</a>
 *
 */
public class Test {

    /**
     * @param args
     */
    public static void main(String[] args) {
        ApplicationContext ctx = new ClassPathXmlApplicationContext(
                        new String[] { 
                                        "classpath:applicationContext.xml",
                                        "classpath:applicationContext-api.xml",
                                        "classpath:applicationContext-command.xml",
                                        "classpath:applicationContext-dao.xml",
                                        "classpath:applicationContext-security.xml",
                                        "classpath:applicationContext-service.xml",
                                        "classpath:applicationContext-spring.xml"
                                        }
                    );
        
//        ApplicationContext ctx = new ClassPathXmlApplicationContext(
//                        new String[] { 
//                                        "classpath:applicationContext.xml",
//                                        "classpath*:applicationContext-*.xml"
//                                        }
//                    );
      
        ctx.getBean("scheduledCalendarService");
        String[] names = ctx.getBeanDefinitionNames();
        for(int i = 0; i < names.length; i++){
            System.out.println(names[i]);
        }
    }

}

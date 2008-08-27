package edu.northwestern.bioinformatics.studycalendar.security.plugin;

/**
 * @author Saurabh Agrawal
 */
public class WebSSOAuthenticationSystemTest extends CasAuthenticationSystemTest {

    private WebSSOAuthenticationSystem webSSOAuthenticationSystem;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        webSSOAuthenticationSystem = new WebSSOAuthenticationSystem();
    }



    @Override
    public CasAuthenticationSystem getSystem() {
        return webSSOAuthenticationSystem;
    }
}
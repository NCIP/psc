package edu.northwestern.bioinformatics.studycalendar.security.csm.internal;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.database.PscDataSourceSelfDiscoveringPropertiesFactoryBean;
import gov.nih.nci.cabig.ctms.suite.authorization.csmext.FasterAuthorizationDao;
import gov.nih.nci.logging.api.logger.util.ApplicationProperties;
import gov.nih.nci.security.AuthorizationManager;
import gov.nih.nci.security.dao.AuthorizationDAO;
import gov.nih.nci.security.exceptions.CSConfigurationException;
import gov.nih.nci.security.provisioning.AuthorizationManagerImpl;
import gov.nih.nci.security.system.ApplicationSessionFactory;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.orm.hibernate3.LocalSessionFactoryBean;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * Creates a CSM AuthorizationManager.
 * Embodies some truly terrible hacks that are required by the true terror that is CSM.
 * If you are some future lost soul who is having trouble with this class, I recommend
 * factoring out the dependency on CSM. If that's still not an option, the comments on
 * some individual methods outline the nature and method of the hacks.
 */
public class DefaultCsmAuthorizationManagerFactory {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private static final String APPLICATION_NAME_KEY = "csm.application.context";

    private String csmApplicationName;
    private DataSource csmDataSource;

    public DefaultCsmAuthorizationManagerFactory(DataSource csmDataSource) {
        initClm();
        this.csmDataSource = csmDataSource;
        this.csmApplicationName = determineCsmApplicationName();
    }

    /**
     * The "common" logging module that is used by CSM attempts to configure itself
     * from the thread context classloader on first reference, if it isn't already
     * configured. (It is configured one time using a JVM-static singleton, as befits
     * professional software intended for deployment in complex environments.)
     * Since there's no way to specify a different classloader, this method temporarily
     * overrides the thread context classloader.
     * <p>
     * This method also relies on the presence of 'ObjectStateLoggerConfig.xml' in this
     * bundle. The copy that is in this bundle was copied from CSM.
     */
    private void initClm() {
        ClassLoader originalContextCL = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
            // init is a side effect of this method
            ApplicationProperties.getInstance(
                "edu/northwestern/bioinformatics/studycalendar/security/csm/internal/ObjectStateLoggerConfig.xml");
        } finally {
            Thread.currentThread().setContextClassLoader(originalContextCL);
        }
    }

    private String determineCsmApplicationName() {
        Properties properties =
            new PscDataSourceSelfDiscoveringPropertiesFactoryBean().getProperties();
        String name = properties.getProperty(APPLICATION_NAME_KEY);
        if (name == null) {
            throw new StudyCalendarSystemException(
                "Missing required datasource property {}", APPLICATION_NAME_KEY);
        } else {
            return name;
        }
    }

    public AuthorizationManager create() {
        log.info("Creating a new CSM AuthorizationManager for application {} with datasource {}",
            csmApplicationName, csmDataSource);
        try {
            SessionFactory sf = ensureCsmSessionFactoryExists();
            AuthorizationManagerImpl authorizationManager = new AuthorizationManagerImpl(csmApplicationName);
            authorizationManager.setAuthorizationDAO(createAuthorizationDao(sf));
            return authorizationManager;
        } catch (Exception e) {
            throw new StudyCalendarSystemException(
                "Creating the CSM authorization manager failed", e);
        }
    }

    private AuthorizationDAO createAuthorizationDao(
        SessionFactory sf
    ) throws CSConfigurationException {
        return new FasterAuthorizationDao(sf, csmApplicationName);
    }

    /**
     * This method is a gross hack. CSM's internals rely on a statically-configured
     * set of hibernate session factories &mdash; {@link ApplicationSessionFactory}. (It looks at
     * this even if you provide it with session factories in all the objects that depend on one.)
     * You can only configure an instance into this monstrosity from a file or URL. (There's also a
     * method that takes a HashMap [no, not a Map], but that only lets you set a very limited set
     * of properties.) If you want to provide your own {@link DataSource} using the public API,
     * you are SOL.
     * <p>
     * Fortunately, this bit of half-assed design is mirrored in poor implementation: the static
     * set of of session factories is stored in a public Hashtable (no, not Dictionary) field. The
     * following method uses this oversight to insert a session factory there directly.
     * <p>
     * This will likely break when a new version of CSM is released. It doesn't seem that that
     * will ever happen, though.
     */
    @SuppressWarnings( { "unchecked" })
    private SessionFactory ensureCsmSessionFactoryExists() throws Exception {
        if (!ApplicationSessionFactory.appSessionFactories.containsKey(csmApplicationName)) {
            ApplicationSessionFactory.appSessionFactories.put(
                csmApplicationName, createCsmSessionFactory());
        }
        return (SessionFactory) ApplicationSessionFactory.appSessionFactories.get(csmApplicationName);
    }

    private SessionFactory createCsmSessionFactory() throws Exception {
        // Use LocalSessionFactoryBean because hibernate alone doesn't seem to have an option
        // to use a provided DataSource instance.
        LocalSessionFactoryBean bean = new LocalSessionFactoryBean();
        
        // these paths are copied straight from CSM's ApplicationSessionFactory
        String[] mappingPaths = {
            "gov/nih/nci/security/authorization/domainobjects/InstanceLevelMappingElement.hbm.xml",
            "gov/nih/nci/security/authorization/domainobjects/Privilege.hbm.xml",
            "gov/nih/nci/security/authorization/domainobjects/Application.hbm.xml",
            "gov/nih/nci/security/authorization/domainobjects/FilterClause.hbm.xml",
            "gov/nih/nci/security/authorization/domainobjects/Role.hbm.xml",
            "gov/nih/nci/security/dao/hibernate/RolePrivilege.hbm.xml",
            "gov/nih/nci/security/dao/hibernate/UserGroup.hbm.xml",
            "gov/nih/nci/security/dao/hibernate/ProtectionGroupProtectionElement.hbm.xml",
            "gov/nih/nci/security/authorization/domainobjects/Group.hbm.xml",
            "gov/nih/nci/security/authorization/domainobjects/User.hbm.xml",
            "gov/nih/nci/security/authorization/domainobjects/ProtectionGroup.hbm.xml",
            "gov/nih/nci/security/authorization/domainobjects/ProtectionElement.hbm.xml",
            "gov/nih/nci/security/authorization/domainobjects/UserGroupRoleProtectionGroup.hbm.xml",
            "gov/nih/nci/security/authorization/domainobjects/UserProtectionElement.hbm.xml"
        };
        Resource[] mappingLocations = new Resource[mappingPaths.length];
        for (int i = 0; i < mappingPaths.length; i++) {
            String path = mappingPaths[i];
            mappingLocations[i] = new ClassPathResource(
                path, AuthorizationManager.class.getClassLoader());
        }
        bean.setMappingLocations(mappingLocations);

        bean.setDataSource(csmDataSource);

        bean.afterPropertiesSet();
        return (SessionFactory) bean.getObject();
    }
}

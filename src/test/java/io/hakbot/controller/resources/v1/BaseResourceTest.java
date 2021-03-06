/*
 * This file is part of Hakbot Origin Controller.
 *
 * Hakbot Origin Controller is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * Hakbot Origin Controller is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Hakbot Origin Controller. If not, see http://www.gnu.org/licenses/.
 */
package io.hakbot.controller.resources.v1;

import alpine.Config;
import alpine.auth.JsonWebToken;
import alpine.auth.KeyManager;
import alpine.filters.AuthenticationFeature;
import alpine.filters.AuthenticationFilter;
import alpine.model.ApiKey;
import alpine.model.LdapUser;
import alpine.persistence.PersistenceManagerFactory;
import alpine.resources.VersionResource;
import io.hakbot.controller.model.Job;
import io.hakbot.controller.model.Team;
import io.hakbot.controller.workers.State;
import io.hakbot.providers.shell.ShellProvider;
import org.apache.commons.lang3.ArrayUtils;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.After;
import org.junit.Before;
import javax.jdo.PersistenceManager;
import javax.jdo.datastore.JDOConnection;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;

public class BaseResourceTest extends JerseyTest {

    private static final String API_VERSION = "v1";
    static final String HEADER_API_KEY_NAME = "X-Api-Key";
    static final String HEADER_API_KEY_VALUE = "01234567890123456789012345678912";
    static final String HEADER_AUTH = "Authorization";
    static String HEADER_AUTH_TOKEN_ADMIN;
    static String HEADER_AUTH_TOKEN_USER;

    protected enum Target {
        CONSOLE(API_VERSION + "/console"),
        JOB(API_VERSION + "/job"),
        PROVIDERS(API_VERSION + "/providers"),
        PUBLISHERS(API_VERSION + "/publishers"),
        USER(API_VERSION + "/user"),
        USER_HAKMASTER(API_VERSION + "/user/hakmaster"),
        VERSION("version");

        public final String target;
        Target(String target) {
            this.target = target;
        }
    }

    public BaseResourceTest() {
        Config.enableUnitTests();
    }

    @Override
    protected URI getBaseUri() {
        return UriBuilder.fromUri("http://127.0.0.1/").port(getPort()).build();
    }

    private String createJsonWebToken(LdapUser ldapUser) {
        KeyManager keyManager = KeyManager.getInstance();
        JsonWebToken jwt = new JsonWebToken(keyManager.getSecretKey());
        return jwt.createToken(ldapUser);
    }

    /** By default, configure the resource to use all available classes */
    @Override
    protected Application configure() {
        return new ResourceConfig(
                authenticatedClasses()
        );
    }

    /** Listing of all classes (including ones necessary to check for authentication) */
    protected Class[] authenticatedClasses() {
        Class[] classes = {
                AuthenticationFilter.class,
                AuthenticationFeature.class
        };
        // Combine and return both the authenticated and configure classes
        return ArrayUtils.addAll(classes, configureClasses());
    }

    /** Listing of classes (excluding authentication checks) */
    protected Class[] configureClasses() {
        return new Class[] {
                ConsoleResource.class,
                JobResource.class,
                ProvidersResource.class,
                PublishersResource.class,
                UserResource.class,
                VersionResource.class
        };
    }

    @Before
    @SuppressWarnings("unchecked")
    public void before() {
        PersistenceManager pm = PersistenceManagerFactory.createPersistenceManager();

        pm.currentTransaction().begin();

        ApiKey apiKey = new ApiKey();
        apiKey.setKey(HEADER_API_KEY_VALUE);

        LdapUser adminUser = new LdapUser();
        adminUser.setDN("cn=admin,o=example,c=us");
        adminUser.setUsername("admin");

        LdapUser user = new LdapUser();
        user.setDN("cn=user,o=example,c=us");
        user.setUsername("user");

        pm.makePersistentAll(apiKey, adminUser, user);
        pm.currentTransaction().commit();
        pm.currentTransaction().begin();

        Team admins = new Team();
        admins.setName("Administrators");
        admins.setUuid("00000000-0000-0000-0000-000000000000");
        admins.setHakmaster(true);
        admins.setLdapUsers(new ArrayList<LdapUser>(){{add(adminUser);}});

        Team users = new Team();
        users.setName("Users");
        users.setUuid("00000000-0000-0000-0000-000000000001");
        users.setHakmaster(false);
        users.setLdapUsers(new ArrayList<LdapUser>(){{add(user);}});

        pm.makePersistentAll(admins, users);
        pm.currentTransaction().commit();
        pm.currentTransaction().begin();

        for (int i=0; i<10; i++) {
            Job job = new Job();
            job.setName("Job " + i);
            job.setUuid("00000000-0000-0000-0000-00000000000" + i);
            job.setCreated(new Date());
            job.setState(State.CREATED);
            job.setProvider(ShellProvider.class.getCanonicalName());
            job.setStartedByApiKeyId(apiKey.getId());
            pm.makePersistent(job);
        }
        pm.currentTransaction().commit();

        HEADER_AUTH_TOKEN_ADMIN = "Bearer " + createJsonWebToken(adminUser);
        HEADER_AUTH_TOKEN_USER = "Bearer " + createJsonWebToken(user);

        pm.close();
    }

    @After
    @SuppressWarnings("unchecked")
    public void after() throws Exception {
        PersistenceManager pm = PersistenceManagerFactory.createPersistenceManager();
        JDOConnection jdoConnection = pm.getDataStoreConnection();
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = (Connection)jdoConnection.getNativeConnection();
            stmt = conn.createStatement();
            stmt.executeUpdate("DROP ALL OBJECTS DELETE FILES");
        } finally {
            if (conn != null) {
                conn.close();
            }
            if (stmt != null) {
                stmt.close();
            }
        }
        pm.close();
    }

}

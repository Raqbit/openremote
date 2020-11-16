package org.openremote.setup;

import org.openremote.container.Container;
import org.openremote.manager.setup.Setup;
import org.openremote.manager.setup.SetupTasks;
import org.openremote.manager.setup.builtin.*;

import java.util.List;
import java.util.logging.Logger;

import static org.openremote.container.util.MapAccess.getBoolean;

public class LoadTestSetupTasks extends BuiltinSetupTasks {

    private static final Logger LOG = Logger.getLogger(LoadTestSetupTasks.class.getName());

    public static final String SETUP_IEMS_PROD_MODE = "SETUP_IEMS_PROD_MODE";
    public static final boolean SETUP_IEMS_PROD_MODE_DEFAULT = false;

    @Override
    public List<Setup> createTasks(Container container) {
        // Production or staging deployment
        boolean prodMode = getBoolean(container.getConfig(), SETUP_IEMS_PROD_MODE, SETUP_IEMS_PROD_MODE_DEFAULT);

        LOG.info("Setting up load testing: " + prodMode);

        // Standard Keycloak tasks for cleaning the system and configuring admin user with roles
        addTask(new KeycloakCleanSetup(container));
        addTask(new KeycloakInitSetup(container));
        addTask(new LoadTestKeycloakSetup(container));
        addTask(new LoadTestManagerSetup(container));

        return getTasks();
    }
}

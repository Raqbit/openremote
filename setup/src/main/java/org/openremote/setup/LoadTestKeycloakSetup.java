package org.openremote.setup;

import org.openremote.container.Container;
import org.openremote.manager.setup.AbstractKeycloakSetup;
import org.openremote.model.security.Tenant;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static org.openremote.model.Constants.MASTER_REALM;

public class LoadTestKeycloakSetup extends AbstractKeycloakSetup {

    private static final Logger LOG = Logger.getLogger(LoadTestKeycloakSetup.class.getName());

    public LoadTestKeycloakSetup(Container container) {
        super(container);
    }

    @Override
    public void onStart() throws Exception {
        super.onStart();

        // TODO: Create any realms for load testing
        Tenant loadTenantA = createTenant("tenantA", "Tenant A", true);
    }
}

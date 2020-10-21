/*
 * Copyright 2020, OpenRemote Inc.
 *
 * See the CONTRIBUTORS.txt file in the distribution for a
 * full listing of individual contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.openremote.model.asset.agent;

import org.openremote.model.asset.Asset;
import org.openremote.model.asset.AssetDescriptor;
import org.openremote.model.attribute.Attribute;
import org.openremote.model.attribute.MetaItem;
import org.openremote.model.v2.*;

/**
 * An agent is a special sub type of {@link Asset} that is associated with a {@link Protocol} and is responsible
 * for providing an instance of the associated {@link Protocol} when requested via {@link #getProtocolInstance}.
 */
public abstract class Agent extends Asset {

    public static AttributeDescriptor<Boolean> DISABLED = new AttributeDescriptor<>("agentDisabled", true, ValueTypes.BOOLEAN, null);

    public static AttributeDescriptor<ConnectionStatus> STATUS = new AttributeDescriptor<>("agentStatus", true, ValueTypes.CONNECTION_STATUS, null,
        new MetaItem<>(MetaTypes.READ_ONLY, true)
    );

    protected <T extends Agent> Agent(String name, AssetDescriptor<T> descriptor) {
        super(name, descriptor);
    }

    /**
     * Get the protocol instance for this Agent.
     */
    public abstract Protocol<?> getProtocolInstance();

    public boolean isDisabled() {
        return getAttributes().get(DISABLED).flatMap(Attribute::getValue).orElse(false);
    }

    public Agent setDisabled(boolean disabled) {
        getAttributes().set(new Attribute<>(DISABLED, disabled));
        return this;
    }
}

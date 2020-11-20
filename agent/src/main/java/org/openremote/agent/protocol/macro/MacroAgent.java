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
package org.openremote.agent.protocol.macro;

import org.openremote.model.asset.agent.Agent;
import org.openremote.model.asset.agent.AgentDescriptor;
import org.openremote.model.asset.agent.Protocol;
import org.openremote.model.attribute.AttributeExecuteStatus;
import org.openremote.model.v2.AttributeDescriptor;
import org.openremote.model.v2.MetaItemDescriptor;
import org.openremote.model.v2.ValueDescriptor;
import org.openremote.model.v2.ValueType;

import java.util.Optional;

public class MacroAgent extends Agent {

    public static final ValueDescriptor<MacroAction> MACRO_ACTION_VALUE = new ValueDescriptor<>("Macro action", MacroAction.class);

    public static final AttributeDescriptor<MacroAction[]> MACRO_ACTIONS = new AttributeDescriptor<>("macroActions", false, MACRO_ACTION_VALUE.asArray());

    public static final AttributeDescriptor<Boolean> MACRO_DISABLED = new AttributeDescriptor<>("macroDisabled", false, ValueType.BOOLEAN);

    public static final AttributeDescriptor<AttributeExecuteStatus> MACRO_STATUS = new AttributeDescriptor<>("macroStatus", false, ValueType.EXECUTION_STATUS);

    public static final MetaItemDescriptor<Integer> MACRO_ACTION_INDEX = new MetaItemDescriptor<>("macroActionIndex", ValueType.POSITIVE_INTEGER, null);

    public MacroAgent(String name) {
        this(name, DESCRIPTOR);
    }

    protected <T extends MacroAgent, S extends Protocol<T>> MacroAgent(String name, AgentDescriptor<T, S> descriptor) {
        super(name, descriptor);
    }

    @Override
    public MacroProtocol getProtocolInstance() {
        return new MacroProtocol(this);
    }

    public Optional<MacroAction[]> getMacroActions() {
        return getAttributes().getValue(MACRO_ACTIONS);
    }

    public Optional<Boolean> isMacroDisabled() {
        return getAttributes().getValue(MACRO_DISABLED);
    }

    public Optional<AttributeExecuteStatus> getMacroStatus() {
        return getAttributes().getValue(MACRO_STATUS);
    }
}

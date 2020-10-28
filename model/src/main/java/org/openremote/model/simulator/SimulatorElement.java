/*
 * Copyright 2016, OpenRemote Inc.
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
package org.openremote.model.simulator;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.openremote.model.attribute.AttributeRef;
import org.openremote.model.simulator.element.ColorSimulatorElement;
import org.openremote.model.simulator.element.NumberSimulatorElement;
import org.openremote.model.simulator.element.ReplaySimulatorElement;
import org.openremote.model.simulator.element.SwitchSimulatorElement;
import org.openremote.model.v2.ValueDescriptor;

import java.util.Optional;

@JsonSubTypes({
    // Events used on client and server (serializable)
    @JsonSubTypes.Type(value = NumberSimulatorElement.class, name = NumberSimulatorElement.ELEMENT_NAME),
    @JsonSubTypes.Type(value = SwitchSimulatorElement.class, name = SwitchSimulatorElement.ELEMENT_NAME),
    @JsonSubTypes.Type(value = ColorSimulatorElement.class, name = ColorSimulatorElement.ELEMENT_NAME),
    @JsonSubTypes.Type(value = ReplaySimulatorElement.class, name = ReplaySimulatorElement.ELEMENT_NAME),
})
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    property = "elementType"
)
public abstract class SimulatorElement {

    public AttributeRef attributeRef;
    public ValueDescriptor<?> expectedType;
    public Object elementValue = null;

    protected SimulatorElement() {
    }

    public SimulatorElement(AttributeRef attributeRef, ValueDescriptor<?> expectedType) {
        this.attributeRef = attributeRef;
        this.expectedType = expectedType;
    }

    public AttributeRef getAttributeRef() {
        return attributeRef;
    }

    public ValueDescriptor<?> getExpectedType() {
        return expectedType;
    }

    public Optional<Object> getValue() {
        return Optional.ofNullable(elementValue);
    }

    public void setValue(Object value) {
        this.elementValue = value;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
            "value=" + elementValue +
            "}";
    }
}

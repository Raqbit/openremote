/*
 * Copyright 2017, OpenRemote Inc.
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
package org.openremote.model.simulator.element;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openremote.model.attribute.AttributeRef;
import org.openremote.model.simulator.SimulatorElement;
import org.openremote.model.v2.ValueTypes;
import org.openremote.model.value.Values;

import java.util.Arrays;

public class ReplaySimulatorElement extends SimulatorElement {

    public static final String ELEMENT_NAME = "replay";

    protected ReplaySimulatorElement() {
    }

    public ReplaySimulatorElement(AttributeRef attributeRef) {
        super(attributeRef, ValueTypes.ARRAY);
    }

    public ReplaySimulatorDatapoint getNextDatapoint(long seconds) throws JsonProcessingException {
        ReplaySimulatorDatapoint[] datapoints = getDatapoints();
        if (datapoints == null) {
             return null;
        }
        return Arrays.stream(datapoints)
            .filter(replaySimulatorDatapoint -> replaySimulatorDatapoint.timestamp > seconds)
            .findFirst()
            .orElse(datapoints[0]);
    }

    public ReplaySimulatorDatapoint[] getDatapoints() throws JsonProcessingException {
        return Values.convert(ReplaySimulatorDatapoint[].class, elementValue);
    }

    public static class ReplaySimulatorDatapoint {

        /**
         * In seconds
         */
        public long timestamp;
        public Object value;
    }
}

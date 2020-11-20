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
package org.openremote.agent.protocol.timer;

import org.openremote.model.asset.agent.Agent;
import org.openremote.model.asset.agent.AgentDescriptor;
import org.openremote.model.asset.agent.Protocol;
import org.openremote.model.attribute.AttributeState;
import org.openremote.model.v2.AttributeDescriptor;
import org.openremote.model.v2.MetaItemDescriptor;
import org.openremote.model.v2.ValueDescriptor;
import org.openremote.model.v2.ValueType;

import java.util.Optional;

public class TimerAgent extends Agent {

    public static final ValueDescriptor<TimerValue> TIMER_VALUE_DESCRIPTOR = new ValueDescriptor<>("Timer value", TimerValue.class);

    public static final ValueDescriptor<CronExpressionParser> TIMER_CRON_EXPRESSION_DESCRIPTOR = new ValueDescriptor<>("Timer cron expression", CronExpressionParser.class);

    public static final AttributeDescriptor<AttributeState> TIMER_ACTION = new AttributeDescriptor<>("timerAction", false, ValueType.ATTRIBUTE_STATE);

    public static final AttributeDescriptor<CronExpressionParser> TIMER_CRON_EXPRESSION = new AttributeDescriptor<>("timerCronExpression", false, TIMER_CRON_EXPRESSION_DESCRIPTOR);

    public static final AttributeDescriptor<Boolean> TIMER_ACTIVE = new AttributeDescriptor<>("timerActive", false, ValueType.BOOLEAN);

    public static final MetaItemDescriptor<TimerValue> META_TIMER_VALUE = new MetaItemDescriptor<>("timerValue", TIMER_VALUE_DESCRIPTOR, null);

    public TimerAgent(String name) {
        this(name, DESCRIPTOR);
    }

    protected <T extends TimerAgent, S extends Protocol<T>> TimerAgent(String name, AgentDescriptor<T, S> descriptor) {
        super(name, descriptor);
    }

    @Override
    public TimerProtocol getProtocolInstance() {
        return new TimerProtocol(this);
    }

    public Optional<AttributeState> getTimerAction() {
        return getAttributes().getValue(TIMER_ACTION);
    }

    public Optional<CronExpressionParser> getTimerCronExpression() {
        return getAttributes().getValue(TIMER_CRON_EXPRESSION);
    }

    public Optional<Boolean> isTimerActive() {
        return getAttributes().getValue(TIMER_ACTIVE);
    }
}

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
package org.openremote.agent.protocol.timer;

import org.openremote.model.AbstractValueHolder;
import org.openremote.model.attribute.AttributeValidationFailure;
import org.openremote.model.ValueHolder;
import org.openremote.model.attribute.Attribute;
import org.openremote.model.attribute.AttributeState;
import org.openremote.model.attribute.AttributeValidationResult;
import org.openremote.model.attribute.MetaItem;
import org.openremote.model.value.Values;

import java.util.Optional;

import static org.openremote.agent.protocol.timer.TimerProtocol.*;
import static org.openremote.model.asset.agent.ProtocolConfiguration.getProtocolName;
import static org.openremote.model.asset.agent.ProtocolConfiguration.initProtocolConfiguration;
import static org.openremote.model.attribute.MetaItem.isMetaNameEqualTo;
import static org.openremote.model.attribute.MetaItem.replaceMetaByName;
import static org.openremote.model.util.TextUtil.isNullOrEmpty;

/**
 * Utility functions for working with timer configuration attributes for the {@link TimerProtocol}
 */
final public class TimerConfiguration {

    private TimerConfiguration() {
    }

    public static Attribute initTimerConfiguration(Attribute attribute, String cronExpression, AttributeState action) {
        initProtocolConfiguration(attribute, TimerProtocol.PROTOCOL_NAME);
        setCronExpression(attribute, cronExpression);
        setAction(attribute, action);
        return attribute;
    }

    public static boolean isTimerConfiguration(Attribute attribute) {
        return getProtocolName(attribute)
            .map(TimerProtocol.PROTOCOL_NAME::equals)
            .orElse(false);
    }

    public static boolean validateTimerConfiguration(Attribute attribute, AttributeValidationResult result) {
        boolean failure = false;

        if (!isTimerConfiguration(attribute)) {
            failure = true;
            if (result != null) {
                result.addAttributeFailure(new AttributeValidationFailure(ValueHolder.ValueFailureReason.VALUE_MISMATCH, PROTOCOL_NAME));
            }
        }

        boolean actionFound = false;
        boolean cronFound = false;

        if (attribute.getMeta() != null && !attribute.getMeta().isEmpty()) {
            for (int i = 0; i < attribute.getMeta().size(); i++) {
                MetaItem metaItem = attribute.getMeta().get(i);
                if (isMetaNameEqualTo(metaItem, META_TIMER_ACTION)) {
                    actionFound = true;
                    if (!getAction(metaItem).isPresent()) {
                        failure = true;
                        if (result == null) {
                            break;
                        }
                        result.addMetaFailure(
                            i, new AttributeValidationFailure(MetaItem.MetaItemFailureReason.META_ITEM_VALUE_MISMATCH, "Timer Action")
                        );
                    }
                } else if (isMetaNameEqualTo(metaItem, META_TIMER_CRON_EXPRESSION)) {
                    cronFound = true;
                    if (!metaItem.getValueAsString().map(TimerProtocol::createCronExpression).isPresent()) {
                        failure = true;
                        if (result == null) {
                            break;
                        }
                        result.addMetaFailure(
                            i, new AttributeValidationFailure(MetaItem.MetaItemFailureReason.META_ITEM_VALUE_MISMATCH, "Timer Cron Expression")
                        );
                    }
                }
            }
        }

        if (!cronFound) {
            failure = true;
            if (result != null) {
                result.addMetaFailure(
                    new AttributeValidationFailure(MetaItem.MetaItemFailureReason.META_ITEM_MISSING, META_TIMER_CRON_EXPRESSION)
                );
            }
        }

        if (!actionFound) {
            failure = true;
            if (result != null) {
                result.addMetaFailure(
                    new AttributeValidationFailure(MetaItem.MetaItemFailureReason.META_ITEM_MISSING, META_TIMER_ACTION)
                );
            }
        }

        return !failure;
    }

    public static boolean isValidTimerConfiguration(Attribute attribute) {
        return isTimerConfiguration(attribute)
            && isActionValid(attribute)
            && isCronExpressionValid(attribute);
    }

    public static boolean hasCronExpression(Attribute attribute) {
        return attribute != null && attribute.hasMetaItem(META_TIMER_CRON_EXPRESSION);
    }

    public static boolean isCronExpressionValid(Attribute attribute) {
        return attribute != null && attribute
            .getMetaItem(META_TIMER_CRON_EXPRESSION)
            .flatMap(AbstractValueHolder::getValueAsString)
            .map(TimerConfiguration::isCronExpressionValid)
            .orElse(false);
    }

    public static boolean isCronExpressionValid(String cronExpression) {
        return !isNullOrEmpty(cronExpression) && createCronExpression(cronExpression) != null;
    }

    public static Optional<String> getCronExpression(Attribute attribute) {
        return attribute == null ? Optional.empty() : attribute
            .getMetaItem(META_TIMER_CRON_EXPRESSION)
            .flatMap(AbstractValueHolder::getValueAsString);
    }

    public static void setCronExpression(Attribute attribute, String cronExpression) {
        if (attribute == null)
            return;

        replaceMetaByName(attribute.getMeta(), META_TIMER_CRON_EXPRESSION, Values.create(cronExpression));
    }

    public static boolean hasAction(Attribute attribute) {
        return attribute != null && attribute.hasMetaItem(META_TIMER_ACTION);
    }

    public static boolean isActionValid(Attribute attribute) {
        return attribute != null && attribute.getMetaItem(META_TIMER_ACTION)
            .flatMap(AbstractValueHolder::getValue)
            .map(AttributeState::isAttributeState)
            .orElse(false);
    }

    public static Optional<AttributeState> getAction(Attribute attribute) {
        return attribute == null ? Optional.empty() : attribute
            .getMetaItem(META_TIMER_ACTION)
            .flatMap(TimerConfiguration::getAction);
    }

    public static Optional<AttributeState> getAction(MetaItem metaItem) {
        return metaItem.getValueAsObject()
            .flatMap(AttributeState::fromValue);
    }

    public static void setAction(Attribute attribute, AttributeState action) {
        if (attribute == null)
            return;
        replaceMetaByName(attribute.getMeta(), META_TIMER_ACTION, action.toObjectValue());
    }

    public static void removeTimer(Attribute attribute) {
        if (attribute == null)
            return;

        attribute
            .getMeta()
            .removeIf(
                isMetaNameEqualTo(META_TIMER_ACTION)
                    .or(isMetaNameEqualTo(META_TIMER_CRON_EXPRESSION))
            );
    }

    public static Optional<TimerValue> getValue(Attribute attribute) {
        return attribute == null ? Optional.empty() : attribute
            .getMetaItem(META_TIMER_VALUE_LINK)
            .flatMap(AbstractValueHolder::getValueAsString)
            .map(TimerValue::fromString);
    }
}

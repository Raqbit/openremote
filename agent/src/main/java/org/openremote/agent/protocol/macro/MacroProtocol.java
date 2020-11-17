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
package org.openremote.agent.protocol.macro;

import org.openremote.agent.protocol.AbstractProtocol;
import org.openremote.model.Container;
import org.openremote.model.attribute.Attribute;
import org.openremote.model.asset.agent.ConnectionStatus;
import org.openremote.model.attribute.*;
import org.openremote.model.syslog.SyslogCategory;
import org.openremote.model.value.Value;
import org.openremote.model.value.ValueType;
import org.openremote.model.value.Values;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

import static org.openremote.agent.protocol.macro.MacroConfiguration.getMacroActionIndex;
import static org.openremote.agent.protocol.macro.MacroConfiguration.isValidMacroConfiguration;
import static org.openremote.model.Constants.PROTOCOL_NAMESPACE;
import static org.openremote.model.syslog.SyslogCategory.PROTOCOL;
import static org.openremote.model.util.TextUtil.REGEXP_PATTERN_INTEGER_POSITIVE;

// TODO: Remove this protocol once attribute linking and flow integration is done
/**
 * This protocol is responsible for executing macros.
 * <p>
 * It expects a {@link AttributeExecuteStatus} as the attribute event value on the {@link #doLinkedAttributeWrite}.
 * The protocol will then try to perform the request on the linked macro protocol instance.
 * <p>
 * {@link Attribute}s can also read/write the macro configuration's {@link MacroAction} values by using the
 * {@link MacroAgent#MACRO_ACTION_INDEX} Meta Item with the index of the {@link MacroAction} to link to.
 */
public class MacroProtocol extends AbstractProtocol<MacroAgent> {

    private static final Logger LOG = SyslogCategory.getLogger(PROTOCOL, MacroProtocol.class);

    public static final String PROTOCOL_DISPLAY_NAME = "Macro";

    class MacroExecutionTask {

        AttributeRef attributeRef;
        List<MacroAction> actions;
        boolean repeat;
        boolean cancelled;
        ScheduledFuture<?> scheduledFuture;
        int iteration = -1;

        public MacroExecutionTask(AttributeRef attributeRef, List<MacroAction> actions, boolean repeat) {
            this.attributeRef = attributeRef;
            this.actions = actions;
            this.repeat = repeat;
        }

        void start() {
            executions.put(attributeRef, this);
            updateLinkedAttribute(new AttributeState(attributeRef, AttributeExecuteStatus.RUNNING.asValue()));
            run();
        }

        void cancel() {
            LOG.fine("Macro Execution cancel");
            scheduledFuture.cancel(false);
            cancelled = true;
            executions.remove(attributeRef);
            updateLinkedAttribute(new AttributeState(attributeRef, AttributeExecuteStatus.CANCELLED.asValue()));
        }

        private void run() {
            if (cancelled) {
                return;
            }

            if (iteration >= 0) {
                // Process the execution of the next action
                MacroAction action = actions.get(iteration);
                AttributeState actionState = action.getAttributeState();

                // send attribute event
                sendAttributeEvent(actionState);
            }

            boolean isLast = iteration == actions.size() - 1;
            boolean restart = isLast && repeat;

            if (restart) {
                iteration = 0;
            } else {
                iteration++;
            }

            if ((isLast && !restart)) {
                executions.remove(attributeRef);
                // Update the command Status of this attribute
                updateLinkedAttribute(new AttributeState(attributeRef, AttributeExecuteStatus.COMPLETED.asValue()));
                return;
            }

            // Get next execution delay
            int delayMillis = actions.get(iteration).getDelayMilliseconds();

            // Schedule the next iteration
            scheduledFuture = executorService.schedule(this::run, Math.max(delayMillis, 0));
        }
    }

    protected List<MacroAction> actions = new ArrayList<>();
    protected final Map<AttributeRef, MacroExecutionTask> executions = new ConcurrentHashMap<>();

    @Override
    public String getProtocolName() {
        return PROTOCOL_DISPLAY_NAME;
    }

    @Override
    public String getProtocolInstanceUri() {
        return "macro://" + agent.getId();
    }

    @Override
    protected void doStart(Container container) throws Exception {

        actions = Arrays.asList(agent.getMacroActions().orElseThrow(() -> {
            String msg = "Macro actions attribute missing or invalid: " + this;
            LOG.warning(msg);
            throw new IllegalArgumentException(msg);
        }));
        setConnectionStatus(ConnectionStatus.CONNECTED);
    }

    @Override
    protected void doStop(Container container) throws Exception {
    }

    @Override
    protected void doLinkAttribute(String assetId, Attribute<?> attribute) {
        AttributeRef attributeRef = new AttributeRef(assetId, attribute.getName());

        // Check for executable meta item
        if (attribute.getValueType().getType() == AttributeExecuteStatus.class) {
            LOG.fine("Macro linked attribute is marked as executable so it will be linked to the firing of the macro");
            // Update the command Status of this attribute
            updateLinkedAttribute(
                new AttributeState(
                    attributeRef,
                    agent.isMacroActive().orElse(true)
                        ? AttributeExecuteStatus.READY
                        : AttributeExecuteStatus.DISABLED
                )
            );
            return;
        }

        // Check for action index or default to index 0
        int actionIndex = attribute.getMetaValue(MacroAgent.MACRO_ACTION_INDEX).orElse(0);

        // Pull the macro action value out with the same type as the linked attribute
        // otherwise push a null value through to the attribute
        Object actionValue = null;

        if (actions.isEmpty()) {
            LOG.fine("No actions are available for the linked macro, maybe it is disabled?: " + this);
        } else {
            actionIndex = Math.min(actions.size(), Math.max(0, actionIndex));
            actionValue = actions.get(actionIndex).getAttributeState().getValue().orElse(null);
            LOG.fine("Attribute is linked to the value of macro action index: actionIndex");
        }

        // Push the value of this macro action into the attribute
        updateLinkedAttribute(new AttributeState(attributeRef, actionValue));
    }

    @Override
    protected void doUnlinkAttribute(String assetId, Attribute<?> attribute) {
    }

    @Override
    protected void doLinkedAttributeWrite(Attribute<?> attribute, AttributeEvent event, Object processedValue) {

        if (attribute.getValueType().getType() == AttributeExecuteStatus.class) {
            // This is a macro execution related write operation
            AttributeExecuteStatus status = event.getValue()
                .flatMap(Values::getString)
                .flatMap(AttributeExecuteStatus::fromString)
                .orElse(null);
            AttributeRef attributeRef = event.getAttributeRef();

            // Check if it's a cancellation request
            if (status == AttributeExecuteStatus.REQUEST_CANCEL) {
                LOG.fine("Request received to cancel macro execution: " + event);
                executions.computeIfPresent(attributeRef,
                    (attributeRef1, macroExecutionTask) -> {
                        macroExecutionTask.cancel();
                        return macroExecutionTask;
                    }
                );
                return;
            }

            if (actions.isEmpty()) {
                LOG.fine("No actions to execute");
                return;
            }

            executeMacro(attributeRef, actions, status == AttributeExecuteStatus.REQUEST_REPEATING);
            return;
        }

        // Assume this is a write to a macro action value (default to index 0)
        int actionIndex = attribute.getMetaValue(MacroAgent.MACRO_ACTION_INDEX).orElse(0);

        if (actions.isEmpty()) {
            LOG.fine("No actions are available for the linked macro, maybe it is disabled?: " + this);
        } else {
            actionIndex = Math.min(actions.size(), Math.max(0, actionIndex));
            MacroAction action = actions.get(actionIndex);

            if (action == null) {
                return;
            }

            action.setAttributeState(new AttributeState(action.getAttributeState().getAttributeRef(), event.getValue().orElse(null)));
            updateAgentAttribute(new AttributeState(agent.getId(), MacroAgent.MACRO_ACTIONS.getName(), actions));
        }
    }

    protected void executeMacro(AttributeRef attributeRef, List<MacroAction> actions, boolean repeat) {
        MacroExecutionTask task = new MacroExecutionTask(attributeRef, actions, repeat);
        task.start();
    }
}

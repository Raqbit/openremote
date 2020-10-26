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
package org.openremote.model.attribute;

import org.openremote.model.v2.AbstractNameValueHolderImpl;
import org.openremote.model.v2.AttributeDescriptor;
import org.openremote.model.v2.ValueDescriptor;

import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

public class Attribute<T> extends AbstractNameValueHolderImpl<T> {

    protected MetaList meta;
    protected long timestamp;

    protected Attribute() {
    }

    public Attribute(AttributeDescriptor<T> attributeDescriptor, T value) {
        this(attributeDescriptor.getName(), attributeDescriptor.getValueDescriptor(), value);

        // Auto merge meta from attribute descriptor
        if (attributeDescriptor.getMeta() != null) {
            getMeta().addAll(attributeDescriptor.getMeta());
        }
    }

    public Attribute(String name, ValueDescriptor<T> valueDescriptor, T value) {
        super(name, valueDescriptor, value);

        // Auto add meta from value descriptor
        if (valueDescriptor.getMeta() != null) {
            getMeta().addAll(valueDescriptor.getMeta());
        }
    }

    public MetaList getMeta() {
        if (meta == null) {
            meta = new MetaList();
        }

        return meta;
    }

    public Attribute<T> addOrReplaceMetaItems(@NotNull MetaList meta) {
        getMeta().addAll(meta);
        return this;
    }

    public Attribute<T> addOrReplaceMetaItems(@NotNull MetaItem<?>...meta) {
        return addOrReplaceMetaItems(Arrays.asList(meta));
    }

    public Attribute<T> addOrReplaceMetaItems(@NotNull Collection<MetaItem<?>> meta) {
        getMeta().addAll(meta);
        return this;
    }

    @Override
    public void setValue(T value) {
        super.setValue(value);
        // Store system time as negative timestamp to allow simple & fast equality check using timestamp; assuming value
        // is immutable as requested in ValueProvider then when the value is set we change the timestamp which
        // indirectly indicates that the value has changed, we use negative value to indicate that the backend needs
        // to set the time at the point of saving.
        setTimestamp(-1*System.currentTimeMillis());
    }

    public void setValue(T value, long timestamp) {
        super.setValue(value);
        setTimestamp(timestamp);
    }

    public long getTimestamp() {
        return Math.abs(timestamp);
    }

    public boolean hasExplicitTimestamp() {
        return timestamp > 0;
    }

    public Attribute<T> setTimestamp(long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    //    public boolean hasLabel() {
//        return getMetaStream().anyMatch(isMetaNameEqualTo(LABEL));
//    }
//
//    public Optional<String> getLabel() {
//        return Optional.ofNullable(getMetaStream()
//            .filter(isMetaNameEqualTo(LABEL))
//            .findFirst()
//            .flatMap(AbstractValueHolder::getValueAsString)
//            .orElseGet(() -> getName().orElse(null)));
//    }
//
//    public Optional<String> getLabelOrName() {
//        return getLabel().map(Optional::of).orElseGet(this::getName);
//    }
//
//    public void setLabel(String label) {
//        if (!isNullOrEmpty(label)) {
//            replaceMetaByName(getMeta(), LABEL, Values.create(label));
//        } else {
//            getMeta().removeIf(isMetaNameEqualTo(LABEL));
//        }
//    }
//
//    public boolean isExecutable() {
//        return getMetaStream()
//            .filter(isMetaNameEqualTo(EXECUTABLE))
//            .findFirst()
//            .map(metaItem -> metaItem.getValueAsBoolean().orElse(false))
//            .orElse(false);
//    }
//
//    public void setExecutable(boolean executable) {
//        if (executable) {
//            replaceMetaByName(getMeta(), EXECUTABLE, Values.create(true));
//        } else {
//            getMeta().removeIf(isMetaNameEqualTo(EXECUTABLE));
//        }
//    }
//
//    public boolean hasAgentLink() {
//        return getMetaStream().anyMatch(isMetaNameEqualTo(AGENT_LINK));
//    }
//
//    public boolean isProtocolConfiguration() {
//        return getMetaStream()
//            .filter(isMetaNameEqualTo(PROTOCOL_CONFIGURATION))
//            .findFirst()
//            .map(metaItem -> metaItem.getValueAsBoolean().orElse(false))
//            .orElse(false);
//    }
//
//    public boolean isShowOnDashboard() {
//        return getMetaStream()
//            .filter(isMetaNameEqualTo(SHOW_ON_DASHBOARD))
//            .findFirst()
//            .map(metaItem -> metaItem.getValueAsBoolean().orElse(false))
//            .orElse(false);
//    }
//
//    public void setShowOnDashboard(boolean show) {
//        if (show) {
//            replaceMetaByName(getMeta(), SHOW_ON_DASHBOARD, Values.create(true));
//        } else {
//            getMeta().removeIf(isMetaNameEqualTo(SHOW_ON_DASHBOARD));
//        }
//    }
//
//    public boolean hasFormat() {
//        return getMetaStream().anyMatch(isMetaNameEqualTo(FORMAT));
//    }
//
//    public Optional<String> getFormat() {
//        return getMetaStream()
//            .filter(isMetaNameEqualTo(FORMAT))
//            .findFirst()
//            .flatMap(AbstractValueHolder::getValueAsString);
//    }
//
//    public void setFormat(String format) {
//        if (!isNullOrEmpty(format)) {
//            replaceMetaByName(getMeta(), FORMAT, Values.create(format));
//        } else {
//            getMeta().removeIf(isMetaNameEqualTo(FORMAT));
//        }
//    }
//
//    public boolean hasDescription() {
//        return getMetaStream().anyMatch(isMetaNameEqualTo(DESCRIPTION));
//    }
//
//    public Optional<String> getDescription() {
//        return getMetaStream()
//            .filter(isMetaNameEqualTo(DESCRIPTION))
//            .findFirst()
//            .flatMap(AbstractValueHolder::getValueAsString);
//    }
//
//    public void setDescription(String description) {
//        if (!isNullOrEmpty(description)) {
//            replaceMetaByName(getMeta(), DESCRIPTION, Values.create(description));
//        } else {
//            getMeta().removeIf(isMetaNameEqualTo(DESCRIPTION));
//        }
//    }
//
//    public boolean isAccessRestrictedRead() {
//        return getMetaStream()
//            .filter(isMetaNameEqualTo(MetaItemType.ACCESS_RESTRICTED_READ))
//            .findFirst()
//            .map(metaItem -> metaItem.getValueAsBoolean().orElse(false))
//            .orElse(false);
//    }
//
//    public boolean isAccessRestrictedWrite() {
//        return getMetaStream()
//            .filter(isMetaNameEqualTo(MetaItemType.ACCESS_RESTRICTED_WRITE))
//            .findFirst()
//            .map(metaItem -> metaItem.getValueAsBoolean().orElse(false))
//            .orElse(false);
//    }
//
//    public boolean isAccessPublicRead() {
//        return getMetaStream()
//            .filter(isMetaNameEqualTo(MetaItemType.ACCESS_PUBLIC_READ))
//            .findFirst()
//            .map(metaItem -> metaItem.getValueAsBoolean().orElse(false))
//            .orElse(false);
//    }
//
//    public boolean isReadOnly() {
//        return getMetaStream()
//            .filter(isMetaNameEqualTo(READ_ONLY))
//            .findFirst()
//            .map(metaItem -> metaItem.getValueAsBoolean().orElse(false))
//            .orElse(false);
//    }
//
//    public void setReadOnly(boolean readOnly) {
//        if (readOnly) {
//            replaceMetaByName(getMeta(), READ_ONLY, Values.create(true));
//        } else {
//            getMeta().removeIf(isMetaNameEqualTo(READ_ONLY));
//        }
//    }
//
//    public boolean isStoreDatapoints() {
//        return getMetaStream()
//            .filter(isMetaNameEqualTo(STORE_DATA_POINTS))
//            .findFirst()
//            .map(metaItem -> metaItem.getValueAsBoolean().orElse(false))
//            .orElse(false);
//    }
//
//    public void setStoreDatapoints(boolean storeDatapoints) {
//        if (storeDatapoints) {
//            replaceMetaByName(getMeta(), STORE_DATA_POINTS, Values.create(true));
//        } else {
//            getMeta().removeIf(isMetaNameEqualTo(STORE_DATA_POINTS));
//        }
//    }
//
//    public boolean isRuleState() {
//        return getMetaStream()
//            .filter(isMetaNameEqualTo(RULE_STATE))
//            .findFirst()
//            .map(metaItem -> metaItem.getValueAsBoolean().orElse(false))
//            .orElse(false);
//    }
//
//    public void setRuleState(boolean ruleState) {
//        if (ruleState) {
//            replaceMetaByName(getMeta(), RULE_STATE, Values.create(true));
//        } else {
//            getMeta().removeIf(isMetaNameEqualTo(RULE_STATE));
//        }
//    }
//
//    public boolean isRuleEvent() {
//        return getMetaStream()
//            .filter(isMetaNameEqualTo(RULE_EVENT))
//            .findFirst()
//            .map(metaItem -> metaItem.getValueAsBoolean().orElse(false))
//            .orElse(false);
//    }
//
//    public void setRuleEvent(boolean ruleEvent) {
//        if (ruleEvent) {
//            replaceMetaByName(getMeta(), RULE_EVENT, Values.create(true));
//        } else {
//            getMeta().removeIf(isMetaNameEqualTo(RULE_EVENT));
//        }
//    }
//
//    public Optional<String> getRuleEventExpires() {
//        return getMetaStream()
//            .filter(isMetaNameEqualTo(RULE_EVENT_EXPIRES))
//            .findFirst()
//            .flatMap(AbstractValueHolder::getValueAsString);
//    }
//
//    public void setRuleEventExpires(String expiry) {
//        if (!isNullOrEmpty(expiry)) {
//            replaceMetaByName(getMeta(), RULE_EVENT_EXPIRES, Values.create(expiry));
//        } else {
//            getMeta().removeIf(isMetaNameEqualTo(RULE_EVENT_EXPIRES));
//        }
//    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
            "name='" + name + '\'' +
            ", value='" + value + '\'' +
            ", timestamp='" + getTimestamp() + '\'' +
            ", meta='" + getMeta().stream().map(MetaItem::toString).collect(Collectors.joining(",")) + '\'' +
            "} ";
    }

    @Override
    public int hashCode() {
        return super.hashCode() + Objects.hash(timestamp);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (!(obj instanceof Attribute))
            return false;
        Attribute that = (Attribute) obj;

        return Objects.equals(timestamp, that.timestamp)
            && super.equals(obj);
    }

//    /**
//     * @return All attributes that exist only in the new list or are different than any attribute in the old list.
//     */
//    public static Stream<Attribute> getAddedOrModifiedAttributes(List<Attribute> oldAttributes,
//                                                                 List<Attribute> newAttributes) {
//        return Attribute.getAddedOrModifiedAttributes(oldAttributes, newAttributes, key -> false);
//    }
//
//    /**
//     * @return All attributes that exist only in the new list or are different than any attribute in the old list.
//     */
//    public static Stream<Attribute> getAddedOrModifiedAttributes(List<Attribute> oldAttributes,
//                                                                 List<Attribute> newAttributes,
//                                                                 Predicate<String> ignoredAttributeKeys) {
//        return Attribute.getAddedOrModifiedAttributes(oldAttributes, newAttributes, name -> false, name -> false, ignoredAttributeKeys);
//    }
//
//    /**
//     * @return All attributes that exist only in the new list or are different than any attribute in the old list.
//     */
//    public static Stream<Attribute> getAddedOrModifiedAttributes(List<Attribute> oldAttributes,
//                                                                 List<Attribute> newAttributes,
//                                                                 Predicate<String> ignoredAttributeNames,
//                                                                 Predicate<String> ignoredAttributeKeys) {
//        return Attribute.getAddedOrModifiedAttributes(
//            oldAttributes,
//            newAttributes,
//            null,
//            ignoredAttributeNames,
//            ignoredAttributeKeys);
//    }
//
//    /**
//     * @return All attributes that exist only in the new list or are different than any attribute in the old list.
//     */
//    public static Stream<Attribute> getAddedOrModifiedAttributes(List<Attribute> oldAttributes,
//                                                                 List<Attribute> newAttributes,
//                                                                 Predicate<String> limitToAttributeNames,
//                                                                 Predicate<String> ignoredAttributeNames,
//                                                                 Predicate<String> ignoredAttributeKeys) {
//        return newAttributes.stream().filter(newAttribute -> oldAttributes.stream().noneMatch(
//            oldAttribute -> newAttribute.getObjectValue().equalsIgnoreKeys(oldAttribute.getObjectValue(), ignoredAttributeKeys))
//        ).filter(addedOrModifiedAttribute ->
//            !addedOrModifiedAttribute.getName().isPresent() ||
//                (limitToAttributeNames == null && ignoredAttributeNames == null) ||
//                (limitToAttributeNames != null && limitToAttributeNames.test(addedOrModifiedAttribute.getName().get())) ||
//                (ignoredAttributeNames != null && !ignoredAttributeNames.test(addedOrModifiedAttribute.getName().get()))
//        );
//    }
//
//    /**
//     * @return All attributes that exist only in the new list (based on name).
//     */
//    public static Stream<Attribute> getAddedAttributes(List<Attribute> oldAttributes,
//                                                       List<Attribute> newAttributes) {
//        return newAttributes.stream().filter(newAttribute -> oldAttributes.stream().noneMatch(
//            oldAttribute -> newAttribute.getNameOrThrow().equals(newAttribute.getNameOrThrow())
//        ));
//    }
}

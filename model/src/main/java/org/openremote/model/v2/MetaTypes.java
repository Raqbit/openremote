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
package org.openremote.model.v2;

import org.openremote.model.asset.UserAsset;
import org.openremote.model.attribute.AttributeEvent;
import org.openremote.model.attribute.AttributeLink;
import org.openremote.model.rules.AssetState;
import org.openremote.model.rules.TemporaryFact;

import javax.validation.constraints.Pattern;

import static org.openremote.model.v2.ValueTypes.*;

public final class MetaTypes {

    /* PROTOCOL / SERVICE META */

    /**
     * Links the attribute to an agent, connecting it to a sensor and/or actuator.
     */
    public static final MetaDescriptor<String> AGENT_LINK = new MetaDescriptor<>("agentLink", ASSET_ID, null);

    /**
     * Links the attribute to another attribute, so an attribute event on the attribute triggers the same attribute
     * event on the linked attribute.
     */
    public static final MetaDescriptor<AttributeLink> ATTRIBUTE_LINK = new MetaDescriptor<>("attributeLink", ValueTypes.ATTRIBUTE_LINK, null);



    /* ACCESS PERMISSION META */

    /**
     * Marks the attribute as readable by unauthenticated public clients.
     */
    public static final MetaDescriptor<Boolean> ACCESS_PUBLIC_READ = new MetaDescriptor<>("accessPublicRead", BOOLEAN, true);

    /**
     * Marks the attribute as writable by unauthenticated public clients.
     */
    public static final MetaDescriptor<Boolean> ACCESS_PUBLIC_WRITE = new MetaDescriptor<>("accessPublicWrite", BOOLEAN, true);

    /**
     * Marks the attribute as readable by restricted clients and therefore users who are linked to the asset, see {@link
     * UserAsset}.
     */
    public static final MetaDescriptor<Boolean> ACCESS_RESTRICTED_READ = new MetaDescriptor<>("accessRestrictedRead", BOOLEAN, true);

    /**
     * Marks the attribute as writable by restricted clients and therefore users who are linked to the asset, see {@link
     * UserAsset}.
     */
    public static final MetaDescriptor<Boolean> ACCESS_RESTRICTED_WRITE = new MetaDescriptor<>("accessRestrictedWrite", BOOLEAN, true);

    /**
     * Marks the attribute as read-only for non-superuser clients. South-bound {@link AttributeEvent}s by regular or
     * restricted users are ignored. North-bound {@link AttributeEvent}s made by protocols and rules engine are
     * possible.
     */
    public static final MetaDescriptor<Boolean> READ_ONLY = new MetaDescriptor<>("readOnly", BOOLEAN, true);


    /* DATA POINT META */

    /**
     * Should attribute values be stored in time series database
     */
    public static final MetaDescriptor<Boolean> STORE_DATA_POINTS = new MetaDescriptor<>("storeDataPoints", BOOLEAN, true);

    /**
     * How long to store attribute values in time series database (data older than this will be automatically purged)
     */
    public static final MetaDescriptor<Integer> DATA_POINTS_MAX_AGE_DAYS = new MetaDescriptor<>("dataPointsMaxAgeDays", POSITIVE_INTEGER, null);

    /**
     * Could possibly have predicted data points
     */
    // TODO: Re-evaluate this can this info be retrieved automatically using prediction service
    public static final MetaDescriptor<Boolean> HAS_PREDICTED_DATA_POINTS = new MetaDescriptor<>("hasPredictedDataPoints", BOOLEAN, true);



    /* RULE META */

    /**
     * Set maximum lifetime of {@link AssetState} temporary facts in rules, for example "1h30m5s". The rules engine will
     * remove temporary {@link AssetState} facts if they are older than this value (using event source/value timestamp,
     * not event processing time).
     * <p>
     * The default expiration for asset events can be configured with environment variable
     * <code>RULE_EVENT_EXPIRES</code>.
     * <p>
     * Also see {@link TemporaryFact#GUARANTEED_MIN_EXPIRATION_MILLIS}.
     */
    @Pattern(regexp = "^([+-])?((\\d+)[Dd])?\\s*((\\d+)[Hh])?\\s*((\\d+)[Mm])?\\s*((\\d+)[Ss])?\\s*((\\d+)([Mm][Ss])?)?$")
    public static final MetaDescriptor<String> RULE_EVENT_EXPIRES = new MetaDescriptor<>("ruleEventExpires", STRING, null);

    /**
     * Should attribute writes be processed by the rules engines as temporary facts. When an attribute is updated, the
     * change will be inserted as a new {@link AssetState} temporary fact in rules engines. These facts expire
     * automatically after a defined time, see {@link #RULE_EVENT_EXPIRES}. If you want to match (multiple) {@link
     * AssetState}s for the same attribute over time, to evaluate the change history of an attribute, add this meta
     * item.
     */
    public static final MetaDescriptor<Boolean> RULE_EVENT = new MetaDescriptor<>("ruleEvent", BOOLEAN, true);

    /**
     * Should attribute writes be processed by the rules engines as {@link AssetState} facts, with a lifecycle that
     * reflects the state of the asset attribute. Each attribute will have one fact at all times in rules memory. These
     * state facts are kept in sync with asset changes: When the attribute is updated, the fact will be updated
     * (replaced). If you want evaluate the change history of an attribute, you typically need to combine this with
     * {@link #RULE_EVENT}.
     */
    public static final MetaDescriptor<Boolean> RULE_STATE = new MetaDescriptor<>("ruleState", BOOLEAN, true);



    /* FORMATTING / DISPLAY META */

    /**
     * A human-friendly string that can be displayed in UI instead of the raw attribute name.
     */
    public static final MetaDescriptor<String> LABEL = new MetaDescriptor<>("label", STRING, null);

    /**
     * Format string that can be used to render the attribute value, see https://github.com/alexei/sprintf.js.
     */
    public static final MetaDescriptor<String> FORMAT = new MetaDescriptor<>("format", STRING, null);

    /**
     * Indicates the unit type that this value represents.
     * For e.g. if the value represents currency and it's in euro's then the unit type would be EUR.
     * For e.g. if the value represents distance and it's in kilometers then the unit type would be KM.
     */
    public static final MetaDescriptor<String> UNIT_TYPE = new MetaDescriptor<>("unitType", STRING, null);

    /**
     * Marks the value as secret and indicates that clients should display this in a concealed manner (e.g. password
     * input with optional show)
     */
    public static final MetaDescriptor<Boolean> SECRET = new MetaDescriptor<>("secret", BOOLEAN, true);

    /**
     * Indicates that any input should support multiline text entry
     */
    public static final MetaDescriptor<Boolean> MULTILINE = new MetaDescriptor<>("multiline", BOOLEAN, true);

    /**
     * If there is a dashboard, some kind of attribute overview, should this attribute be shown.
     */
    public static final MetaDescriptor<Boolean> SHOW_ON_DASHBOARD = new MetaDescriptor<>("showOnDashboard", BOOLEAN, true);

    protected MetaTypes() {
    }
}

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
package org.openremote.model.asset;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.Formula;
import org.openremote.model.Constants;
import org.openremote.model.IdentifiableEntity;
import org.openremote.model.attribute.Attribute;
import org.openremote.model.attribute.AttributeList;
import org.openremote.model.geo.GeoJSONPoint;
import org.openremote.model.value.AttributeDescriptor;
import org.openremote.model.value.ValueType;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Optional;

import static org.openremote.model.Constants.PERSISTENCE_JSON_VALUE_TYPE;
import static org.openremote.model.Constants.PERSISTENCE_UNIQUE_ID_GENERATOR;

// @formatter:off

/**
 * The main model class of this software.
 * <p>
 * An asset is an identifiable item in a composite relationship with other assets. This tree
 * of assets can be managed through a <code>null</code> {@link #parentId} property for root
 * assets, and a valid parent identifier for sub-assets.
 * <p>
 * The properties {@link #parentName} and {@link #parentType} are transient, not required
 * for storing assets, and only resolved and usable when the asset is loaded from storage.
 * <p>
 * An asset is stored in and therefore access-controlled through a {@link #realm}.
 * <p>
 * The {@link #createdOn} value is milliseconds since the Unix epoch.
 * <p>
 * The {@link #type} of the asset is an arbitrary string which should correspond with an {@link AssetDescriptor}
 * registered within the running instance. If the corresponding {@link AssetDescriptor} cannot be found then
 * the fallback generic {@link Asset#DESCRIPTOR} will be assumed.
 * <p>
 * The {@link #path} is a list of parent asset identifiers, starting with the identifier of
 * this asset, followed by parent asset identifiers, and ending with the identifier of the
 * root asset in the tree. This is a transient property and only resolved and usable when
 * the asset is loaded from storage and as calculating it is costly, might be empty when
 * certain optimized loading operations are used.
 * An asset may have 0-N {@link #attributes}; the {@link AssetDescriptor} associated with an
 * asset type describes the standard {@link Attribute}s that can be found and what the value type of these
 * {@link Attribute}s should be but additional {@link Attribute}s can also be added but obviously no validation
 * can be performed on such dynamic {@link Attribute}s. Use the {@link Attribute} etc. class to work with this API.
 * This property can be empty when certain optimized loading operations are used.
 * <p>
 * For more details on restricted access of user-assigned assets, see {@link UserAsset}.
 * </p>
 * <p>
 * Example JSON representation of an asset tree:
 * <blockquote><pre>{@code
 * {
 * "id": "0oI7Gf_kTh6WyRJFUTr8Lg",
 * "version": 0,
 * "createdOn": 1489042784142,
 * "name": "Smart Building",
 * "type": "urn:openremote:asset:building",
 * "accessPublicRead": false,
 * "realm": "building",
 * "realmDisplayName": "Building",
 * "path": [
 * "0oI7Gf_kTh6WyRJFUTr8Lg"
 * ],
 * "coordinates": [
 * 5.469751699216005,
 * 51.44760787406028
 * ]
 * }
 * }</pre></blockquote>
 * <blockquote><pre>{@code
 * {
 * "id": "B0x8ZOqZQHGjq_l0RxAJBA",
 * "version": 0,
 * "createdOn": 1489042784148,
 * "name": "Apartment 1",
 * "type": "urn:openremote:asset:residence",
 * "accessPublicRead": false,
 * "parentId": "0oI7Gf_kTh6WyRJFUTr8Lg",
 * "parentName": "Smart Building",
 * "parentType": "urn:openremote:asset:building",
 * "realm": "c38a3fdf-9d74-4dac-940c-50d3dce1d248",
 * "tenantRealm": "building",
 * "tenantDisplayName": "Building",
 * "path": [
 * "B0x8ZOqZQHGjq_l0RxAJBA",
 * "0oI7Gf_kTh6WyRJFUTr8Lg"
 * ],
 * "coordinates": [
 * 5.469751699216005,
 * 51.44760787406028
 * ]
 * }
 * }</pre></blockquote>
 * <blockquote><pre>{@code
 * {
 * "id": "bzlRiJmSSMCl8HIUt9-lMg",
 * "version": 0,
 * "createdOn": 1489042784157,
 * "name": "Living Room",
 * "type": "urn:openremote:asset:room",
 * "accessPublicRead": false,
 * "parentId": "B0x8ZOqZQHGjq_l0RxAJBA",
 * "parentName": "Apartment 1",
 * "parentType": "urn:openremote:asset:residence",
 * "realm": "c38a3fdf-9d74-4dac-940c-50d3dce1d248",
 * "tenantRealm": "building",
 * "tenantDisplayName": "Building",
 * "path": [
 * "bzlRiJmSSMCl8HIUt9-lMg",
 * "B0x8ZOqZQHGjq_l0RxAJBA",
 * "0oI7Gf_kTh6WyRJFUTr8Lg"
 * ],
 * "coordinates": [
 * 5.469751699216005,
 * 51.44760787406028
 * ]
 * }
 * }</pre></blockquote>
 * <blockquote><pre>{@code
 * {
 * "id": "W7GV_lFeQVyHLlgHgE3dEQ",
 * "version": 0,
 * "createdOn": 1489042784164,
 * "name": "Living Room Thermostat",
 * "type": "urn:openremote:asset:thing",
 * "accessPublicRead": false,
 * "parentId": "bzlRiJmSSMCl8HIUt9-lMg",
 * "parentName": "Living Room",
 * "parentType": "urn:openremote:asset:room",
 * "realm": "c38a3fdf-9d74-4dac-940c-50d3dce1d248",
 * "tenantRealm": "building",
 * "tenantDisplayName": "Building",
 * "path": [
 * "W7GV_lFeQVyHLlgHgE3dEQ",
 * "bzlRiJmSSMCl8HIUt9-lMg",
 * "B0x8ZOqZQHGjq_l0RxAJBA",
 * "0oI7Gf_kTh6WyRJFUTr8Lg"
 * ],
 * "coordinates": [
 * 5.460315214821094,
 * 51.44541688237109
 * ],
 * "attributes": {
 * "currentTemperature": {
 * "meta": [
 * {
 * "name": "urn:openremote:asset:meta:label",
 * "value": "Current Temp"
 * },
 * {
 * "name": "urn:openremote:asset:meta:accessRestrictedRead",
 * "value": true
 * },
 * {
 * "name": "urn:openremote:asset:meta:readOnly",
 * "value": true
 * },
 * {
 * "name": "urn:openremote:foo:bar",
 * "value": "FOO"
 * },
 * {
 * "name": "urn:thirdparty:bar",
 * "value": "BAR"
 * }
 * ],
 * "type": "Decimal",
 * "value": 19.2,
 * "valueTimestamp": 1.489670166115E12
 * },
 * "somethingPrivate": {
 * "type": "String",
 * "value": "Foobar",
 * "valueTimestamp": 1.489670156115E12
 * }
 * }
 * }
 * }</pre></blockquote>
 */
// @formatter:on
@Entity
@Table(name = "ASSET")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "TYPE")
@DiscriminatorValue("non null")
@Check(constraints = "ID != PARENT_ID")
@JsonTypeInfo(include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type", visible = true, use = JsonTypeInfo.Id.CUSTOM, defaultImpl = Asset.class)
public class Asset implements IdentifiableEntity {

    /**
     * Generic asset descriptor called "Thing"
     */
    public static final AssetDescriptor<Asset> DESCRIPTOR = new AssetDescriptor<>("cube-outline", null, Asset.class);

    /*
     * ATTRIBUTE DESCRIPTORS DESCRIBING FIXED NAME ATTRIBUTES AND THEIR VALUE TYPE - ALL SUB TYPES OF THIS ASSET TYPE
     * WILL INHERIT THESE DESCRIPTORS ALSO; IT IS REQUIRED THAT EACH DESCRIPTOR HAS CORRESPONDING GETTER WITH OPTIONAL
     * SETTER, THIS ENSURES BASIC COMPILE TIME CHECKING OF CONFLICTS BUT JUST MAKES GOOD SENSE FOR CONSUMERS
    */
    public static final AttributeDescriptor<GeoJSONPoint> LOCATION = new AttributeDescriptor<>("location", ValueType.GEO_JSON_POINT);

    public static final AttributeDescriptor<String> EMAIL = new AttributeDescriptor<>("email", ValueType.EMAIL);

    public static final AttributeDescriptor<String[]> TAGS = new AttributeDescriptor<>("tags", ValueType.STRING.asArray());

    public static final AttributeDescriptor<String> NOTES = new AttributeDescriptor<>("notes", ValueType.STRING);

    @Id
    @Column(name = "ID", length = 22, columnDefinition = "char(22)")
    @GeneratedValue(generator = PERSISTENCE_UNIQUE_ID_GENERATOR)
    protected String id;

    @Version
    @Column(name = "VERSION", nullable = false)
    protected long version;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CREATED_ON", updatable = false, nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    @org.hibernate.annotations.CreationTimestamp
    protected Date createdOn;

    @NotNull(message = "{Asset.name.NotNull}")
    @Size(min = 1, max = 1023, message = "{Asset.name.Size}")
    @Column(name = "NAME", nullable = false, length = 1023)
    protected String name;

    @NotNull(message = "{Asset.type.NotNull}")
    @Size(min = 3, max = 255, message = "{Asset.type.Size}")
    @Column(name = "TYPE", nullable = false, updatable = false)
    protected String type;

    @Column(name = "ACCESS_PUBLIC_READ", nullable = false)
    protected boolean accessPublicRead;

    @Column(name = "PARENT_ID", length = 36)
    protected String parentId;

    @Column(name = "REALM", nullable = false)
    protected String realm;

    @Transient
    protected String parentName;

    @Transient
    protected String parentType;

    // The following are expensive to query, so if they are null, they might not have been loaded

    @Formula("get_asset_tree_path(ID)")
    @org.hibernate.annotations.Type(type = Constants.PERSISTENCE_STRING_ARRAY_TYPE)
    protected String[] path;

    @Column(name = "ATTRIBUTES", columnDefinition = "jsonb")
    @org.hibernate.annotations.Type(type = PERSISTENCE_JSON_VALUE_TYPE)
    protected AttributeList attributes;

    /**
     * For use by hydrators (i.e. JPA/Jackson)
     */
    Asset() {
    }

    public Asset(String name) {
        this(name, DESCRIPTOR);
    }

    public <T extends Asset> Asset(String name, AssetDescriptor<T> descriptor) {
        setName(name);
        this.type = descriptor.getName();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) throws IllegalArgumentException {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public boolean isAccessPublicRead() {
        return accessPublicRead;
    }

    public void setAccessPublicRead(boolean accessPublicRead) {
        this.accessPublicRead = accessPublicRead;
    }

    public void setParent(Asset parent) {
        if (parent == null) {
            parentId = null;
            parentName = null;
            parentType = null;
        } else {
            parentId = parent.id;
            parentName = parent.name;
            parentType = parent.type;
        }
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    /**
     * NOTE: This is a transient and optional property, set only in database query results.
     */
    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    /**
     * NOTE: This is a transient and optional property, set only in database query results.
     */
    public String getParentType() {
        return parentType;
    }

    public String getRealm() {
        return realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

    /**
     * NOTE: This is a transient and optional property, set only in database query results.
     * <p>
     * The identifiers of all parents representing the path in the tree. The first element
     * is the identifier of this instance, the last is the root asset without a parent.
     */
    public String[] getPath() {
        return path;
    }

    public void setPath(String[] path) {
        this.path = path;
    }

    /**
     * NOTE: This is a transient and optional property, set only in database query results.
     * <p>
     * The identifiers of all parents representing the path in the tree. The first element
     * is the root asset without a parent, the last is the identifier of this instance.
     */
    public String[] getReversePath() {
        if (path == null)
            return null;

        String[] newArray = new String[path.length];
        int j = 0;
        for (int i = path.length; i > 0; i--) {
            newArray[j] = path[i - 1];
            j++;
        }
        return newArray;
    }

    public boolean pathContains(String assetId) {
        return path != null && Arrays.asList(getPath()).contains(assetId);
    }

    public AttributeList getAttributes() {
        if (attributes == null) {
            attributes = new AttributeList();
        }
        return attributes;
    }

    public Asset setAttributes(AttributeList attributes) {
        if (attributes == null) {
            attributes = new AttributeList();
        }
        this.attributes = attributes;
        return this;
    }

    public Asset setAttributes(Attribute<?>...attributes) {
        return setAttributes(Arrays.asList(attributes));
    }

    public Asset setAttributes(Collection<Attribute<?>> attributes) {
        this.attributes = new AttributeList(attributes);
        return this;
    }

    public <T> Optional<Attribute<T>> getAttribute(AttributeDescriptor<T> descriptor) {
        return getAttributes().get(descriptor);
    }

    public Optional<Attribute<?>> getAttribute(String attributeName) {
        return getAttributes().get(attributeName);
    }

    @SuppressWarnings("unchecked")
    public <U> Optional<Attribute<U>> getAttribute(String attributeName, Class<U> valueType) {
        return getAttributes().get(attributeName).map(attribute -> {
            if (attribute.getValueType().getType() == valueType) {
                return (Attribute<U>)attribute;
            } else {
                return null;
            }
        });
    }

    public Optional<GeoJSONPoint> getLocation() {
        return getAttributes().getValue(LOCATION);
    }

    public Asset setLocation(GeoJSONPoint location) {
        getAttributes().addOrReplace(new Attribute<>(LOCATION, location));
        return this;
    }

    public Optional<String[]> getTags() {
        return getAttributes().getValue(TAGS);
    }

    public Asset setTags(String[] tags) {
        getAttributes().getOrCreate(TAGS).setValue(tags);
        return this;
    }

    public Optional<String> getEmail() {
        return getAttributes().getValue(EMAIL);
    }

    public Asset setEmail(String email) {
        getAttributes().getOrCreate(EMAIL).setValue(email);
        return this;
    }

    public Optional<String> getNotes() {
        return getAttributes().getValue(NOTES);
    }

    public Asset setNotes(String notes) {
        getAttributes().getOrCreate(NOTES).setValue(notes);
        return this;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
            "id='" + id + '\'' +
            ", name='" + name + '\'' +
            ", type ='" + type + '\'' +
            ", parentId='" + parentId + '\'' +
            '}';
    }

    public String toStringAll() {
        return "Asset{" +
            "id='" + id + '\'' +
            ", version=" + version +
            ", createdOn=" + createdOn +
            ", name='" + name + '\'' +
            ", type='" + type + '\'' +
            ", accessPublicRead='" + accessPublicRead + '\'' +
            ", parentId='" + parentId + '\'' +
            ", parentName='" + parentName + '\'' +
            ", parentType='" + parentType + '\'' +
            ", realm='" + realm + '\'' +
            ", path=" + Arrays.toString(path) +
            ", attributes=" + attributes +
            '}';
    }

//    ---------------------------------------------------
//    FUNCTIONAL METHODS BELOW
//    ---------------------------------------------------
//
//    public static boolean isAssetTypeEqualTo(Asset asset, AssetType assetType) {
//        return asset != null && asset.getWellKnownType() == assetType;
//    }
//
//    public static Asset map(Asset assetToMap, Asset asset,
//                            String overrideName,
//                            String overrideRealm,
//                            String overrideParentId,
//                            String overrideType,
//                            Boolean overrideAccessPublicRead,
//                            ObjectValue overrideAttributes) {
//        asset.setVersion(assetToMap.getVersion());
//        asset.setName(overrideName != null ? overrideName : assetToMap.getName());
//        if (overrideType != null) {
//            asset.setType(overrideType);
//        } else {
//            asset.setType(assetToMap.getType());
//        }
//
//        asset.setAccessPublicRead(overrideAccessPublicRead != null ? overrideAccessPublicRead : assetToMap.isAccessPublicRead());
//
//        asset.setParentId(overrideParentId != null ? overrideParentId : assetToMap.getParentId());
//        asset.setParentName(null);
//        asset.setParentType(null);
//
//        asset.setRealm(overrideRealm != null ? overrideRealm : assetToMap.getRealm());
//
//        asset.setAttributes(overrideAttributes != null ? overrideAttributes : assetToMap.getAttributes());
//
//        return asset;
//    }
}

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
package org.openremote.model.geo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import static org.openremote.model.geo.GeoJSONPoint.TYPE;

@JsonTypeName(TYPE)
public class GeoJSONPoint extends GeoJSONGeometry {

    public static final String TYPE = "Point";
    @JsonProperty
    protected Position coordinates;

    @JsonCreator
    public GeoJSONPoint(@JsonProperty("coordinates") Position coordinates) {
        super(TYPE);
        this.coordinates = coordinates;
    }

    public GeoJSONPoint(double x, double y) {
        this(new Position(x, y));
    }

    public GeoJSONPoint(double x, double y, double z) {
        this(new Position(x, y, z));
    }

    public Position getCoordinates() {
        return coordinates;
    }

    @JsonIgnore
    public double getX() {
        return coordinates.getX();
    }

    @JsonIgnore
    public double getY() {
        return coordinates.getY();
    }

    @JsonIgnore
    public Double getZ() {
        return coordinates.getZ();
    }

    @JsonIgnore
    public boolean hasZ() {
        return coordinates.hasZ();
    }
}

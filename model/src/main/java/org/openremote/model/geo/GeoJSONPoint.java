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

import com.fasterxml.jackson.annotation.*;
import com.vividsolutions.jts.geom.Coordinate;

import static org.openremote.model.geo.GeoJSONPoint.TYPE;

@JsonTypeName(TYPE)
public class GeoJSONPoint extends GeoJSONGeometry {

    public static final String TYPE = "Point";
    @JsonProperty
    @JsonFormat(shape = JsonFormat.Shape.ARRAY)
    protected Coordinate coordinates;

    @JsonCreator
    public GeoJSONPoint(@JsonProperty("coordinates") Coordinate coordinates) {
        super(TYPE);
        this.coordinates = coordinates;
    }

    public GeoJSONPoint(double x, double y) {
        this(new Coordinate(x, y));
    }

    public GeoJSONPoint(double x, double y, double z) {
        this(new Coordinate(x, y, z));
    }

    public Coordinate getCoordinates() {
        return coordinates;
    }

    @JsonIgnore
    public double getX() {
        return coordinates.x;
    }

    @JsonIgnore
    public double getY() {
        return coordinates.y;
    }

    @JsonIgnore
    public Double getZ() {
        return coordinates.z;
    }

    @JsonIgnore
    public boolean hasZ() {
        return coordinates.z != Coordinate.NULL_ORDINATE;
    }
}

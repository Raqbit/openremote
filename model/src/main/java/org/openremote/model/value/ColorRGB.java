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
package org.openremote.model.value;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.Objects;

@JsonFormat(shape = JsonFormat.Shape.ARRAY)
@JsonPropertyOrder({"red", "green", "blue", "amberWhite"})
public class ColorRGB {

    protected int red;
    protected int green;
    protected int blue;
    protected Integer amberWhite = null;

    public ColorRGB(int red, int green, int blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    public ColorRGB(int red, int green, int blue, int amberWhite) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.amberWhite = amberWhite;
    }

    public int getRed() {
        return red;
    }

    public int getGreen() {
        return green;
    }

    public int getBlue() {
        return blue;
    }

    public Integer getAmberWhite() {
        return amberWhite;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ColorRGB other = (ColorRGB) o;

        if (red != other.red) return false;
        if (green != other.green) return false;
        if (blue != other.blue) return false;
        return Objects.equals(amberWhite, other.amberWhite);
    }

    @Override
    public int hashCode() {
        return Objects.hash(red, green, blue, amberWhite);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
            "red=" + red +
            ", green=" + green +
            ", blue=" + blue +
            '}';
    }
}

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
package org.openremote.model.query.filter;

import org.openremote.model.value.Values;

import java.util.Arrays;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class StringArrayPredicate implements ValuePredicate {

    public static final String name = "string-array";
    public StringPredicate[] predicates = new StringPredicate[0];

    public StringArrayPredicate() {
    }

    public StringArrayPredicate(StringPredicate...predicates) {
        this.predicates = predicates;
    }

    public StringArrayPredicate predicates(StringPredicate...predicates) {
        this.predicates = predicates;
        return this;
    }

    @Override
    public Predicate<Object> asPredicate(Supplier<Long> currentMillisSupplier) {
        return obj -> {

            if (predicates == null || predicates.length == 0) {
                return true;
            }
            
            if (obj == null) {
                return false;
            }

            // Treat arrays as a special case
            if (Values.isArray(obj.getClass())) {
                @SuppressWarnings("OptionalGetWithoutIsPresent")
                Object[] strings = Values.getValue(obj, Object[].class).get();

                if (strings.length != predicates.length) {
                    return false;
                }

                for (int i = 0; i < predicates.length; i++) {
                    StringPredicate p = predicates[i];
                    if (!p.asPredicate(currentMillisSupplier).test(strings[i])) {
                        return false;
                    }
                }

                return true;
            }


            return Arrays.stream(predicates).map(stringPredicate -> stringPredicate.asPredicate(currentMillisSupplier)).allMatch(p -> p.test(obj));
        };
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
            "predicates=" + Arrays.toString(predicates) +
            '}';
    }
}

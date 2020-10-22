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
package org.openremote.model.asset.impl;

import org.openremote.model.asset.Asset;
import org.openremote.model.asset.AssetDescriptor;
import org.openremote.model.console.ConsoleProviders;
import org.openremote.model.v2.AttributeDescriptor;
import org.openremote.model.v2.ValueTypes;

public class Console extends Asset {

    public static final AttributeDescriptor<String> CONSOLE_NAME = new AttributeDescriptor<>("consoleName", false, ValueTypes.STRING, null);
    public static final AttributeDescriptor<String> CONSOLE_VERSION = new AttributeDescriptor<>("consoleVersion", false, ValueTypes.STRING, null);
    public static final AttributeDescriptor<String> CONSOLE_PLATFORM = new AttributeDescriptor<>("consolePlatform", false, ValueTypes.STRING, null);
    public static final AttributeDescriptor<ConsoleProviders> CONSOLE_PROVIDERS = new AttributeDescriptor<>("consoleProviders", false, ValueTypes.CONSOLE_PROVIDERS, null);
    public static final AssetDescriptor<Console> DESCRIPTOR = new AssetDescriptor<>("Console", "monitor-cellphone", null, Console.class);

    protected <T extends Console> Console(String name) {
        super(name, DESCRIPTOR);
    }
}

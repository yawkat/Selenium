/*******************************************************************************
 * Selenium, Minecraft texture pack viewer and editor
 * 
 * Copyright (C) 2013  Jonas Konrad
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package at.yawk.selenium.resourcepack;

import java.util.Arrays;

public class ResourceTypes {
    private ResourceTypes() {}
    
    private static ResourceType[] types = new ResourceType[0];
    
    public static synchronized void registerResourceType(ResourceType resourceType) {
        types = Arrays.copyOf(types, types.length + 1);
        types[types.length - 1] = resourceType;
    }
    
    public static synchronized ResourceType getResourceType(Resource resource) {
        for (ResourceType t : types) {
            if (t.matches(resource.getFile().getName())) {
                return t;
            }
        }
        return null;
    }
}

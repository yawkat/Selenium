/*******************************************************************************
 * Selenium, Minecraft resource pack viewer and editor
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

import java.awt.image.BufferedImage;
import java.io.IOException;

import at.yawk.selenium.ui.ResourceEditor;

public interface ResourceType {
    boolean matches(String filename);
    
    BufferedImage getPreview(Resource resource) throws IOException;
    
    ResourceEditor getEditor(Resource resource);
    
    boolean equals(Resource r1, Resource r2);
}

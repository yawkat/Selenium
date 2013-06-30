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
package at.yawk.selenium.ui;

import java.awt.image.BufferedImage;
import java.io.IOError;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.imageio.ImageIO;

public class Icons {
    private Icons() {}
    
    private static final Map<String, BufferedImage> icons = new ConcurrentHashMap<>();
    
    public static BufferedImage getIcon(String name) {
        if (!icons.containsKey(name)) {
            try {
                icons.put(name, ImageIO.read(Icons.class.getResourceAsStream("images/" + name)));
            } catch (IOException e) {
                throw new IOError(e);
            }
        }
        return icons.get(name);
    }
}

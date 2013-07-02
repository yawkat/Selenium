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
package at.yawk.selenium;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;

public class Util {
    private Util() {}
    
    public static BufferedImage resizeImage(BufferedImage image, int width, int height) {
        if (image.getWidth() == width && image.getHeight() == height) {
            return image;
        }
        BufferedImage resized = new BufferedImage(width, height, image.getType());
        Graphics g = resized.createGraphics();
        g.drawImage(image, 0, 0, width, height, null);
        g.dispose();
        return resized;
    }
    
    public static BufferedImage resizeImageIfBigger(BufferedImage image, int width, int height) {
        return resizeImage(image, Math.min(width, image.getWidth()), Math.min(height, image.getHeight()));
    }
    
    public static File getMinecraftDirectory() {
        String osName = System.getProperty("os.name").toLowerCase();
        String userHome = System.getProperty("user.home", ".");
        if (osName.contains("win")) {
            String appdata = System.getenv("APPDATA");
            return new File(appdata != null ? appdata : userHome, ".minecraft/");
        } else if (osName.contains("mac")) {
            return new File(userHome, "Library/Application Support/minecraft");
        } else if (osName.contains("linux") || osName.contains("unix")) {
            return new File(userHome, ".minecraft/");
        } else {
            return new File(userHome, "minecraft/");
        }
    }
}

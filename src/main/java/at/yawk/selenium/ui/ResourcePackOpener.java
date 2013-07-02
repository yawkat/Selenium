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
package at.yawk.selenium.ui;

import java.io.File;

import javax.swing.JFileChooser;

import at.yawk.selenium.Selenium;

public class ResourcePackOpener extends JFileChooser {
    private static final long serialVersionUID = 1L;
    
    public ResourcePackOpener(File initDirectory) {
        super(initDirectory);
        setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
    }
    
    public ResourcePackOpener() {
        this(Selenium.currentFileChooserDirectory);
    }
    
    public File selectResourcePack0() {
        File f = showOpenDialog(null) == JFileChooser.APPROVE_OPTION ? getSelectedFile() : null;
        Selenium.currentFileChooserDirectory = getCurrentDirectory();
        return f;
    }
    
    public static File selectResourcePack(File initDirectory) {
        return new ResourcePackOpener(initDirectory).selectResourcePack0();
    }
    
    public static File selectResourcePack() {
        return new ResourcePackOpener().selectResourcePack0();
    }
}

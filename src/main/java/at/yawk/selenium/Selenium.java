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

import static at.yawk.selenium.Strings.t;

import java.awt.Frame;
import java.awt.Window;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import at.yawk.selenium.fs.Zip;
import at.yawk.selenium.resourcepack.ResourceTree;
import at.yawk.selenium.resourcepack.types.IcnsType;
import at.yawk.selenium.resourcepack.types.ImageType;
import at.yawk.selenium.resourcepack.types.PropertyType;
import at.yawk.selenium.resourcepack.types.Sound3dType;
import at.yawk.selenium.resourcepack.types.json.McMetaType;
import at.yawk.selenium.ui.SeleniumSuite;

public class Selenium {
    public static Window mainWindow;
    public static File currentFileChooserDirectory;
    
    static {
        File f = Util.getMinecraftDirectory();
        currentFileChooserDirectory = f.exists() && f.isDirectory() ? f : new File(".");
    }
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
        
        try {
            // load resource types
            Class.forName(ImageType.class.getName());
            Class.forName(Sound3dType.class.getName());
            Class.forName(PropertyType.class.getName());
            Class.forName(IcnsType.class.getName());
            Class.forName(McMetaType.class.getName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        
        File[] rootFiles = new File[args.length];
        for (int i = 0; i < rootFiles.length; i++) {
            rootFiles[i] = new File(args[i]);
        }
        
        mainWindow = new JFrame(t("Selenium"));
        ResourceTree[] trees = new ResourceTree[rootFiles.length];
        for (int i = 0; i < trees.length; i++) {
            trees[i] = new ResourceTree(Zip.toFileSystem(rootFiles[i]));
        }
        mainWindow.add(new SeleniumSuite(trees));
        mainWindow.pack();
        ((Frame) mainWindow).setExtendedState(((Frame) mainWindow).getExtendedState() | JFrame.MAXIMIZED_BOTH);
        ((JFrame) mainWindow).setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainWindow.setVisible(true);
    }
}

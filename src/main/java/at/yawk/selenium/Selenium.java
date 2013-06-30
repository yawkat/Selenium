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

import java.io.File;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import at.yawk.selenium.fs.NioFileSystem;
import at.yawk.selenium.fs.Zip;
import at.yawk.selenium.resourcepack.ResourceTree;
import at.yawk.selenium.resourcepack.types.IcnsType;
import at.yawk.selenium.resourcepack.types.ImageType;
import at.yawk.selenium.resourcepack.types.PropertyType;
import at.yawk.selenium.resourcepack.types.Sound3dType;
import at.yawk.selenium.ui.ResourcePackOpener;
import at.yawk.selenium.ui.SeleniumSuite;

public class Selenium {
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
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        
        File rootFile;
        if (args.length == 1) {
            rootFile = new File(args[0]);
        } else {
            rootFile = ResourcePackOpener.selectResourcePack();
            if (rootFile == null) {
                System.exit(-1);
                return;
            }
        }
        
        JFrame test = new JFrame(t("Selenium"));
        test.add(new SeleniumSuite(new ResourceTree(new NioFileSystem(Zip.toPath(rootFile)))));
        test.pack();
        test.setExtendedState(test.getExtendedState() | JFrame.MAXIMIZED_BOTH);
        test.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        test.setVisible(true);
    }
}

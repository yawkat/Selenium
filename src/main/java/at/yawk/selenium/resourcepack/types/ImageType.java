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
package at.yawk.selenium.resourcepack.types;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import at.yawk.selenium.resourcepack.McMeta.McMetaException;
import at.yawk.selenium.resourcepack.Resource;
import at.yawk.selenium.resourcepack.ResourceType;
import at.yawk.selenium.resourcepack.ResourceTypes;
import at.yawk.selenium.resourcepack.types.json.McMetaDefaults;
import at.yawk.selenium.resourcepack.types.json.McMetaType;
import at.yawk.selenium.ui.ResourceEditor;

public class ImageType implements ResourceType {
    static {
        ResourceTypes.registerResourceType(new ImageType());
    }
    
    @Override
    public boolean matches(String filename) {
        filename = filename.toLowerCase();
        return filename.endsWith(".png") || filename.endsWith(".bmp") || filename.endsWith(".gif") || filename.endsWith(".jpg") || filename.endsWith(".jpeg");
    }
    
    @Override
    public BufferedImage getPreview(Resource file) throws IOException {
        try (InputStream i = file.getFile().getInput()) {
            return ImageIO.read(i);
        }
    }
    
    @Override
    public ResourceEditor getEditor(final Resource file) {
        return new ResourceEditor() {
            @Override
            public JComponent getEditor() throws IOException {
                return new JPanel() {
                    private static final long serialVersionUID = 1L;
                    
                    private JPanel main;
                    private JSplitPane leftPane;
                    
                    {
                        BufferedImage image = getPreview(file);
                        
                        main = new JPanel();
                        leftPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);
                        
                        ResizedImageView original = new ResizedImageView(image);
                        
                        Dimension size = new Dimension(Math.max(image.getWidth(), 350), image.getHeight());
                        original.setPreferredSize(size);
                        leftPane.setTopComponent(original);
                        try {
                            leftPane.setBottomComponent(McMetaType.instance.getEditor(file.getMeta(), McMetaDefaults.DEFAULTS_TEXTURE).getEditor());
                        } catch (McMetaException e) {}
                        leftPane.setPreferredSize(new Dimension(size.width, Integer.MAX_VALUE));
                        
                        main.setLayout(new LayoutManager() {
                            @Override
                            public void removeLayoutComponent(Component c) {}
                            
                            @Override
                            public Dimension preferredLayoutSize(Container c) {
                                return c.getComponent(0).getPreferredSize();
                            }
                            
                            @Override
                            public Dimension minimumLayoutSize(Container c) {
                                return preferredLayoutSize(c);
                            }
                            
                            @Override
                            public void layoutContainer(Container c) {
                                Component[] components = c.getComponents();
                                Dimension d1 = components[0].getPreferredSize();
                                components[0].setLocation(new Point());
                                components[0].setSize(Math.min(d1.width, c.getSize().width), c.getSize().height);
                                components[1].setLocation(components[0].getWidth(), 0);
                                components[1].setSize(c.getSize().width - d1.width, c.getSize().height);
                            }
                            
                            @Override
                            public void addLayoutComponent(String s, Component c) {}
                        });
                        
                        main.add(leftPane, BorderLayout.CENTER);
                        main.add(new ResizedImageView(image), BorderLayout.LINE_END);
                        
                        setLayout(new BorderLayout());
                        add(main, BorderLayout.CENTER);
                    }
                };
            }
        };
    }
    
    @Override
    public boolean equals(Resource r1, Resource r2) {
        try {
            BufferedImage b1 = getPreview(r1);
            BufferedImage b2 = getPreview(r2);
            
            if (b1.getWidth() != b2.getWidth() || b1.getHeight() != b2.getHeight()) {
                return false;
            }
            
            int[] rgb1 = new int[b1.getWidth() * b1.getHeight()];
            b1.getRGB(0, 0, b1.getWidth(), b1.getHeight(), rgb1, 0, 1);
            int[] rgb2 = new int[rgb1.length];
            b2.getRGB(0, 0, b2.getWidth(), b2.getHeight(), rgb2, 0, 1);
            
            return Arrays.equals(rgb1, rgb2);
        } catch (IOException e) {
            return false;
        }
    }
}

class ResizedImageView extends JComponent {
    private static final long serialVersionUID = 1L;
    
    private BufferedImage image;
    
    public ResizedImageView(BufferedImage image) {
        setImage(image);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (image == null) {
            return;
        }
        
        int sw = image.getWidth();
        int sh = image.getHeight();
        int dw = getWidth();
        int dh = getHeight();
        
        float sx = (float) dw / sw;
        float sy = (float) dh / sh;
        
        float s = Math.min(sx, sy);
        
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, dw, dh);
        g.drawImage(image, 0, 0, (int) (sw * s), (int) (sh * s), 0, 0, sw, sh, null);
    }
    
    public BufferedImage getImage() {
        return image;
    }
    
    public void setImage(BufferedImage image) {
        this.image = image;
    }
}

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

import static at.yawk.selenium.Strings.t;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.border.Border;

import at.yawk.selenium.fs.Zip;
import at.yawk.selenium.resourcepack.PreviewCache;
import at.yawk.selenium.resourcepack.Resource;
import at.yawk.selenium.resourcepack.ResourceTree;
import at.yawk.selenium.resourcepack.ResourceType;
import at.yawk.selenium.resourcepack.ResourceTypes;
import at.yawk.selenium.ui.ResourceTreeViewer.ResourceOpenListener;

public class SeleniumSuite extends JPanel {
    private static final long serialVersionUID = 1L;
    
    private final ResourceTreeViewer tree;
    private JTabbedPane resourceEditor = new JTabbedPane();
    private JMenuBar menu;
    private JSplitPane center = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true);
    
    public SeleniumSuite(ResourceTree... trees) {
        this.tree = new ResourceTreeViewer(trees);
        setLayout(new BorderLayout());
        center.setLeftComponent(this.tree);
        center.setRightComponent(resourceEditor);
        add(center, BorderLayout.CENTER);
        
        this.tree.setOpenListener(new ResourceOpenListener() {
            @Override
            public void onResourceOpened(Resource resource) {
                if (!resource.getFile().isDirectory()) {
                    addEditor(resource);
                }
            }
        });
        
        menu = new JMenuBar();
        JMenu file = new JMenu(t("File"));
        {
            JMenuItem open = new JMenuItem(new AbstractAction(t("Open Resource Pack...")) {
                private static final long serialVersionUID = 1L;
                
                @Override
                public void actionPerformed(ActionEvent e) {
                    File f = ResourcePackOpener.selectResourcePack();
                    if (f != null) {
                        ResourceTree[] trees = Arrays.copyOf(tree.getTrees(), tree.getTrees().length + 1);
                        trees[trees.length - 1] = new ResourceTree(Zip.toFileSystem(f));
                        setResourceTrees(trees);
                    }
                }
            });
            file.add(open);
        }
        menu.add(file);
        add(menu, BorderLayout.PAGE_START);
    }
    
    public void setResourceTrees(ResourceTree... trees) {
        this.tree.setTrees(trees);
    }
    
    private void addEditor(Resource resource) {
        ResourceType type = ResourceTypes.getResourceType(resource);
        JComponent component;
        Icon icon = null;
        if (type == null) {
            component = new JLabel(t("Unknown file format"));
            ((JLabel) component).setHorizontalAlignment(JLabel.CENTER);
            component.setFont(component.getFont().deriveFont(30F));
            icon = null;
        } else {
            try {
                icon = new ImageIcon(PreviewCache.getPreview(resource, type, 16, 16));
                component = type.getEditor(resource).getEditor();
            } catch (IOException e) {
                component = new JLabel(e.getMessage());
            }
        }
        final int index = resourceEditor.getTabCount();
        resourceEditor.insertTab("", null, component, resource.getPath(), index);
        {
            final JComponent closing = component;
            // close button
            final JComponent derived = new JPanel();
            derived.setBackground(new Color(0, 0, 0, 0));
            derived.setLayout(new BorderLayout());
            
            JLabel l1 = new JLabel(icon);
            JLabel l2 = new JLabel(resource.getPath());
            JButton l3 = new JButton(new AbstractAction("", new ImageIcon(Icons.getIcon("close.png"))) {
                private static final long serialVersionUID = 1L;
                
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    resourceEditor.remove(closing);
                }
            });
            l3.setHideActionText(true);
            l3.setContentAreaFilled(false);
            
            Border border = BorderFactory.createEmptyBorder(2, 2, 2, 2);
            l1.setBorder(border);
            l2.setBorder(border);
            l3.setBorder(border);
            
            derived.add(l1, BorderLayout.LINE_START);
            derived.add(l2, BorderLayout.CENTER);
            derived.add(l3, BorderLayout.LINE_END);
            
            final JPopupMenu tabPopup = new JPopupMenu();
            derived.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    maybeShowPopup(e);
                }
                
                @Override
                public void mouseReleased(MouseEvent e) {
                    maybeShowPopup(e);
                }
                
                public void maybeShowPopup(MouseEvent event) {
                    if (event.isPopupTrigger()) {
                        tabPopup.show(event.getComponent(), event.getX(), event.getY());
                    }
                }
            });
            tabPopup.add(new JMenuItem(new AbstractAction(t("Close")) {
                private static final long serialVersionUID = 1L;
                
                @Override
                public void actionPerformed(ActionEvent event) {
                    resourceEditor.remove(closing);
                }
            }));
            tabPopup.add(new JMenuItem(new AbstractAction(t("Close all")) {
                private static final long serialVersionUID = 1L;
                
                @Override
                public void actionPerformed(ActionEvent event) {
                    resourceEditor.removeAll();
                }
            }));
            tabPopup.add(new JMenuItem(new AbstractAction(t("Close all but this")) {
                private static final long serialVersionUID = 1L;
                
                @Override
                public void actionPerformed(ActionEvent event) {
                    for (int i = resourceEditor.getTabCount() - 1; i >= 0; i--) {
                        if (resourceEditor.getComponentAt(i) != closing) {
                            resourceEditor.removeTabAt(i);
                        }
                    }
                }
            }));
            
            resourceEditor.setTabComponentAt(index, derived);
        }
        resourceEditor.setSelectedIndex(index);
    }
}

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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;

import at.yawk.selenium.fs.FileSystem;
import at.yawk.selenium.resourcepack.PreviewCache;
import at.yawk.selenium.resourcepack.Resource;
import at.yawk.selenium.resourcepack.ResourceTree;
import at.yawk.selenium.resourcepack.ResourceType;
import at.yawk.selenium.resourcepack.ResourceTypes;

public class ResourceTreeViewer extends JPanel {
    private static final long serialVersionUID = 1L;
    private ResourceTree tree = null;
    private ResourceOpenListener openListener;
    
    public ResourceTreeViewer(ResourceTree tree) {
        this.setTree(tree);
        init();
        setPreferredSize(new Dimension(350, -1));
    }
    
    public ResourceTreeViewer() {
        this(null);
    }
    
    private void init() {
        removeAll();
        setLayout(new BorderLayout());
        if (getTree() == null) {
            // TODO message
        } else {
            FileSystem rootFile = tree.getRoot();
            final DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(rootFile.getName());
            addRecursive(tree, rootFile, rootNode);
            final JTree tree = new JTree(rootNode);
            add(new JScrollPane(tree), BorderLayout.CENTER);
            
            for (int row = 0; row < tree.getRowCount(); row++) {
                tree.expandRow(row);
            }
            
            tree.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent event) {
                    if (event.getClickCount() > 1) {
                        if (openListener != null) {
                            int selRow = tree.getRowForLocation(event.getX(), event.getY());
                            Resource r = rowToResource(tree, selRow);
                            if (r != null) {
                                openListener.onResourceOpened(r);
                            }
                        }
                        event.consume();
                    }
                }
            });
            
            tree.setCellRenderer(new DefaultTreeCellRenderer() {
                private static final long serialVersionUID = 1L;
                
                @Override
                public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                    Component c = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
                    if (leaf) {
                        Resource r = rowToResource(tree, row);
                        if (r != null) {
                            ResourceType type = ResourceTypes.getResourceType(r);
                            if (type != null) {
                                try {
                                    setIcon(new ImageIcon(PreviewCache.getPreview(r, type, 16, 16)));
                                } catch (IOException e) {}
                            }
                        }
                    }
                    return c;
                }
            });
        }
    }
    
    private static void addRecursive(ResourceTree tree, FileSystem s, MutableTreeNode node) {
        if (s.isDirectory()) {
            for (FileSystem child : s.listChildren()) {
                DefaultMutableTreeNode n = new DefaultMutableTreeNode(child.getName());
                addRecursive(tree, child, n);
                node.insert(n, node.getChildCount());
            }
        }
    }
    
    public ResourceTree getTree() {
        return tree;
    }
    
    public void setTree(ResourceTree tree) {
        this.tree = tree;
        init();
    }
    
    public ResourceOpenListener getOpenListener() {
        return openListener;
    }
    
    public void setOpenListener(ResourceOpenListener openListener) {
        this.openListener = openListener;
    }
    
    public static interface ResourceOpenListener {
        void onResourceOpened(Resource resource);
    }
    
    private Resource rowToResource(JTree tree, int row) {
        if (row < 0) {
            return null;
        }
        TreePath selPath = tree.getPathForRow(row);
        StringBuilder path = new StringBuilder();
        Object[] oa = selPath.getPath();
        for (int i = 1; i < oa.length; i++) {
            path.append(((DefaultMutableTreeNode) oa[i]).getUserObject());
            path.append('/');
        }
        String pathS = path.toString();
        Resource resource = ResourceTreeViewer.this.tree.getResource(pathS);
        FileSystem fs = resource.getFile();
        return fs.isDirectory() ? null : resource;
    }
}

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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
    private ResourceTree[] trees = new ResourceTree[0];
    private ResourceOpenListener openListener;
    
    public ResourceTreeViewer(ResourceTree... trees) {
        this.setTrees(trees);
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(350, -1));
        init();
    }
    
    private void init() {
        removeAll();
        if (getTrees().length == 0) {
            // TODO message
        } else {
            DefaultMutableTreeNode root = new DefaultMutableTreeNode();
            for (ResourceTree resourceTree : getTrees()) {
                FileSystem rootFile = resourceTree.getRoot();
                final DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(resourceTree);
                addRecursive(resourceTree, rootFile, rootNode);
                root.add(rootNode);
            }
            final JTree tree = new JTree(root);
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
                    
                    value = ((DefaultMutableTreeNode) value).getUserObject();
                    
                    if (value instanceof Resource) {
                        setText(((Resource) value).getFile().getName());
                        if (((Resource) value).getFile().isDirectory()) {
                            setIcon(new ImageIcon(Icons.getIcon("folder.png")));
                        } else {
                            ResourceType type = ResourceTypes.getResourceType((Resource) value);
                            if (type != null) {
                                try {
                                    setIcon(new ImageIcon(PreviewCache.getPreview((Resource) value, type, 16, 16)));
                                } catch (IOException e) {}
                            } else {
                                setIcon(new ImageIcon(Icons.getIcon("page.png")));
                            }
                        }
                    } else if (value instanceof ResourceTree) {
                        setText(((ResourceTree) value).getRoot().getName());
                        setIcon(new ImageIcon(((ResourceTree) value).getRoot().getFileTypePreview()));
                    } else {
                        setIcon(new ImageIcon(Icons.getIcon("page.png")));
                    }
                    return c;
                }
            });
            tree.setShowsRootHandles(true);
            tree.setRootVisible(false);
        }
        validate();
    }
    
    private static void addRecursive(ResourceTree tree, FileSystem s, MutableTreeNode node) {
        if (s.isDirectory()) {
            for (FileSystem child : s.listChildren()) {
                DefaultMutableTreeNode n = new DefaultMutableTreeNode(tree.getResource(child.getRelativePath(tree.getRoot())));
                addRecursive(tree, child, n);
                node.insert(n, node.getChildCount());
            }
        }
    }
    
    public ResourceTree[] getTrees() {
        return trees;
    }
    
    public void setTrees(ResourceTree... trees) {
        // filter duplicates while keeping order
        List<ResourceTree> treeList = new ArrayList<>();
        for (ResourceTree tree : trees) {
            if (!treeList.contains(tree)) {
                treeList.add(tree);
            }
        }
        trees = treeList.toArray(new ResourceTree[treeList.size()]);
        
        if (Arrays.equals(this.trees, trees)) {
            return;
        }
        
        this.trees = trees;
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
        Object o = ((DefaultMutableTreeNode) selPath.getLastPathComponent()).getUserObject();
        return o instanceof Resource && !((Resource) o).getFile().isDirectory() ? (Resource) o : null;
    }
}

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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
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
    private JTree treeView;
    
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
            treeView = new JTree(root);
            add(new JScrollPane(treeView), BorderLayout.CENTER);
            
            for (int row = 0; row < treeView.getRowCount(); row++) {
                treeView.expandRow(row);
            }
            
            treeView.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent event) {
                    TreePath selRow = treeView.getPathForLocation(event.getX(), event.getY());
                    if (selRow == null) {
                        return;
                    }
                    final Object uobj = ((DefaultMutableTreeNode) selRow.getLastPathComponent()).getUserObject();
                    if (event.getClickCount() > 1 && event.getButton() == MouseEvent.BUTTON1 && openListener != null && uobj instanceof Resource) {
                        openListener.onResourceOpened((Resource) uobj);
                    } else if (event.getButton() == MouseEvent.BUTTON3 && uobj instanceof ResourceTree) {
                        final JPopupMenu popup = new JPopupMenu();
                        popup.add(new JMenuItem(new AbstractAction(t("Compress")) {
                            private static final long serialVersionUID = 1L;
                            
                            @Override
                            public void actionPerformed(ActionEvent arg0) {
                                List<ResourceTree> other = new ArrayList<>(Arrays.asList(trees));
                                other.remove(uobj);
                                new WizardCompress((ResourceTree) uobj, other.toArray(new ResourceTree[other.size()])).showWizard();
                            }
                        }));
                        popup.add(new JMenuItem(new AbstractAction(t("Rewrite images")) {
                            private static final long serialVersionUID = 1L;
                            
                            @Override
                            public void actionPerformed(ActionEvent arg0) {
                                if (JOptionPane.showConfirmDialog(ResourceTreeViewer.this.getParent(), t("Are you sure you want to rewrite all images? This is unlikely but possible to result in data loss."), t("Rewrite images"), JOptionPane.OK_CANCEL_OPTION) == JOptionPane.YES_OPTION) {
                                    runCompression(((ResourceTree) uobj).getRoot());
                                    ((ResourceTree) uobj).getRoot().flushManagingSystem();
                                    PreviewCache.clearCache();
                                    ResourceTreeViewer.this.validate();
                                }
                            }
                            
                            private void runCompression(FileSystem system) {
                                try {
                                    String l = system.getName().toLowerCase();
                                    if (l.endsWith(".png")) {
                                        try (InputStream in = system.getInputStream(); OutputStream out = system.getOutputStream()) {
                                            ImageIO.write(ImageIO.read(in), "PNG", out);
                                        }
                                    }
                                } catch (Exception e) {}
                                
                                for (FileSystem child : system.listChildren()) {
                                    runCompression(child);
                                }
                            }
                        }));
                        popup.show(treeView, event.getX(), event.getY());
                        event.consume();
                    }
                    event.consume();
                }
            });
            treeView.setCellRenderer(new DefaultTreeCellRenderer() {
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
                        final ResourceTree root = (ResourceTree) value;
                        setText(root.getRoot().getName());
                        setIcon(new ImageIcon(root.getRoot().getFileTypePreview()));
                    } else {
                        setIcon(new ImageIcon(Icons.getIcon("page.png")));
                    }
                    return c;
                }
            });
            treeView.setShowsRootHandles(true);
            treeView.setRootVisible(false);
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
}

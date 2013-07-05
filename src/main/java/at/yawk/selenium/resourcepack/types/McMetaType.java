package at.yawk.selenium.resourcepack.types;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.image.BufferedImage;
import java.io.IOError;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.google.common.collect.Maps;

import at.yawk.selenium.resourcepack.McMeta;
import at.yawk.selenium.resourcepack.McMeta.McMetaException;
import at.yawk.selenium.resourcepack.Resource;
import at.yawk.selenium.resourcepack.ResourceType;
import at.yawk.selenium.resourcepack.ResourceTypes;
import at.yawk.selenium.ui.Icons;
import at.yawk.selenium.ui.ResourceEditor;

public class McMetaType implements ResourceType {
    public static final McMetaType instance = new McMetaType();
    
    static {
        ResourceTypes.registerResourceType(instance);
    }
    
    private McMetaType() {}
    
    @Override
    public boolean matches(String filename) {
        return filename.toLowerCase().endsWith(".mcmeta");
    }
    
    @Override
    public BufferedImage getPreview(Resource resource) throws IOException {
        return Icons.getIcon("gear.png");
    }
    
    @Override
    public ResourceEditor getEditor(final Resource resource) {
        try {
            return getEditor(new McMeta(resource.getFile()));
        } catch (McMetaException e) {
            throw new IOError(e);
        }
    }
    
    public ResourceEditor getEditor(final McMeta meta) {
        return new ResourceEditor() {
            @Override
            public JComponent getEditor() throws IOException {
                try {
                    return new JPanel() {
                        private static final long serialVersionUID = 1L;
                        
                        JTree tree;
                        
                        {
                            JSONObject rootj = meta.getRoot();
                            DefaultMutableTreeNode rootn = new DefaultMutableTreeNode();
                            add(Maps.immutableEntry((String) null, rootj), new String[0], rootn);
                            
                            tree = new JTree(rootn);
                            tree.setRootVisible(false);
                            tree.setCellRenderer(new DefaultTreeCellRenderer() {
                                private static final long serialVersionUID = 1L;
                                
                                @Override
                                public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                                    Component c = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
                                    JsonEntry e = (JsonEntry) ((DefaultMutableTreeNode) value).getUserObject();
                                    setText(e.path[e.path.length - 1]);
                                    return c;
                                }
                            });
                            
                            setLayout(new BorderLayout());
                            add(tree);
                        }
                        
                        void add(Entry<String, ? extends Object> toAdd, String[] parentPath, DefaultMutableTreeNode node) {
                            String[] path = Arrays.copyOf(parentPath, parentPath.length + 1);
                            path[path.length - 1] = toAdd.getKey();
                            
                            node.setUserObject(new JsonEntry(path, toAdd.getValue()));
                            Object value = toAdd.getValue();
                            if (value instanceof JSONObject) {
                                @SuppressWarnings("unchecked")
                                List<Entry<String, ? extends Object>> children = new ArrayList<>(((JSONObject) value).entrySet());
                                Collections.sort(children, new Comparator<Entry<String, ?>>() {
                                    @Override
                                    public int compare(Entry<String, ?> arg0, Entry<String, ?> arg1) {
                                        return String.CASE_INSENSITIVE_ORDER.compare(arg0.getKey(), arg1.getKey());
                                    }
                                });
                                for (Entry<String, ? extends Object> child : children) {
                                    DefaultMutableTreeNode childNode = new DefaultMutableTreeNode();
                                    node.add(childNode);
                                    add(child, path, childNode);
                                }
                            } else if (value instanceof JSONArray) {
                                int index = 0;
                                for (Object object : (JSONArray) value) {
                                    index++;
                                    DefaultMutableTreeNode childNode = new DefaultMutableTreeNode();
                                    node.add(childNode);
                                    add(Maps.immutableEntry(String.valueOf(index), object), path, childNode);
                                }
                            }
                        }
                        
                        class JsonEntry {
                            private final String[] path;
                            private final Object value;
                            
                            public JsonEntry(String[] path, Object value) {
                                this.path = path;
                                this.value = value;
                            }
                        }
                    };
                } catch (McMetaException e) {
                    throw new IOException(e);
                }
            }
        };
    }
    
    @Override
    public boolean equals(Resource r1, Resource r2) {
        // TODO Auto-generated method stub
        return false;
    }
}

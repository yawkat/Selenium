package at.yawk.selenium.resourcepack.types.json;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOError;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import at.yawk.selenium.fs.Zip;
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
        String l = filename.toLowerCase();
        return l.endsWith(".mcmeta") || l.endsWith(".json");
    }
    
    @Override
    public BufferedImage getPreview(Resource resource) throws IOException {
        return Icons.getIcon("gear.png");
    }
    
    @Override
    public ResourceEditor getEditor(final Resource resource) {
        try {
            return getEditor(new McMeta(resource.getFile()), McMetaDefaults.DEFAULTS_NONE);
        } catch (McMetaException e) {
            throw new IOError(e);
        }
    }
    
    public ResourceEditor getEditor(McMeta meta, McMetaDefaults defaults) {
        try {
            return new MetaResourceEditor(meta, defaults);
        } catch (McMetaException e) {
            throw new IOError(e);
        }
        
        /*
        return new ResourceEditor() {
            @SuppressWarnings("unchecked")
            @Override
            public JComponent getEditor() throws IOException {
                try {
                    return new JPanel() {
                        private static final long serialVersionUID = 1L;
                        
                        JTree tree;
                        
                        {
                            JSONObject rootj = meta.getRoot();
                            JSONObject defaultsObject = new JSONObject();
                            defaultsObject.putAll(deepClone(rootj));
                            defaults.addDefaults(defaultsObject);
                            DefaultMutableTreeNode rootn = new DefaultMutableTreeNode();
                            add(Maps.immutableEntry(t("McMeta"), defaultsObject), new String[0], rootn, rootj);
                            
                            tree = new JTree(rootn);
                            for (int row = 0; row < tree.getRowCount(); row++) {
                                tree.expandRow(row);
                            }
                            tree.setRootVisible(true);
                            tree.setCellRenderer(new DefaultTreeCellRenderer() {
                                private static final long serialVersionUID = 1L;
                                
                                @Override
                                public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                                    Component c = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
                                    JsonEntry e = (JsonEntry) ((DefaultMutableTreeNode) value).getUserObject();
                                    setTextNonSelectionColor(e.fromDefaults ? new Color(0x999999) : Color.BLACK);
                                    if (e.value instanceof JSONArray || e.value instanceof JSONObject) {
                                        setIcon(new ImageIcon(Icons.getIcon("folder.png")));
                                        setText(e.path[e.path.length - 1]);
                                    } else {
                                        setIcon(new ImageIcon(Icons.getIcon("page.png")));
                                        if (e.fromDefaults) {
                                            setText(e.path[e.path.length - 1] + ": " + e.value);
                                        } else {
                                            setText("<html><body>" + StringEscapeUtils.escapeHtml4(e.path[e.path.length - 1]) + ": <span" + (sel ? "" : " color='#999999'") + ">" + StringEscapeUtils.escapeHtml4(String.valueOf(e.value)) + "</span></body></html>");
                                        }
                                    }
                                    return c;
                                }
                            });
                            
                            setLayout(new BorderLayout());
                            add(tree);
                        }
                        
                        void add(Entry<String, ? extends Object> toAdd, String[] parentPath, DefaultMutableTreeNode node, JSONObject rootj) {
                            String[] path = Arrays.copyOf(parentPath, parentPath.length + 1);
                            path[path.length - 1] = toAdd.getKey();
                            
                            boolean fromDefaults = false;
                            {
                                JSONObject o = rootj;
                                for (int i = 1; i < path.length; i++) {
                                    Object p = o.get(path[i]);
                                    if (p instanceof JSONObject) {
                                        o = (JSONObject) p;
                                    } else {
                                        fromDefaults = toAdd.getValue().equals(p);
                                        break;
                                    }
                                }
                            }
                            node.setUserObject(new JsonEntry(path, toAdd.getValue(), fromDefaults));
                            Object value = toAdd.getValue();
                            if (value instanceof Map<?, ?>) {
                                @SuppressWarnings("rawtypes")
                                List<Entry<String, ? extends Object>> children = new ArrayList<>(((Map) value).entrySet());
                                Collections.sort(children, new Comparator<Entry<String, ?>>() {
                                    @Override
                                    public int compare(Entry<String, ?> arg0, Entry<String, ?> arg1) {
                                        return String.CASE_INSENSITIVE_ORDER.compare(arg0.getKey(), arg1.getKey());
                                    }
                                });
                                for (Entry<String, ? extends Object> child : children) {
                                    DefaultMutableTreeNode childNode = new DefaultMutableTreeNode();
                                    add(child, path, childNode, rootj);
                                    node.add(childNode);
                                }
                            } else if (value instanceof List<?>) {
                                int index = 0;
                                for (Object object : (List<?>) value) {
                                    index++;
                                    DefaultMutableTreeNode childNode = new DefaultMutableTreeNode();
                                    node.add(childNode);
                                    add(Maps.immutableEntry(String.valueOf(index), object), path, childNode, rootj);
                                }
                            }
                        }
                        
                        class JsonEntry {
                            private final String[] path;
                            private final Object value;
                            private final boolean fromDefaults;
                            
                            private JsonEntry(String[] path, Object value, boolean fromDefaults) {
                                this.path = path;
                                this.value = value;
                                this.fromDefaults = fromDefaults;
                            }
                        }
                    };
                } catch (McMetaException e) {
                    throw new IOException(e);
                }
            }
        };
        */
    }
    
    public static void main(String[] args) throws IOException, McMetaException {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
        
        JFrame jFrame = new JFrame();
        jFrame.add(new McMetaType().getEditor(new McMeta(Zip.toFileSystem(new File(args[0]))), McMetaDefaults.DEFAULTS_TEXTURE).getEditor());
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame.pack();
        jFrame.setVisible(true);
    }
    
    @Override
    public boolean equals(Resource r1, Resource r2) {
        // TODO Auto-generated method stub
        return false;
    }
}

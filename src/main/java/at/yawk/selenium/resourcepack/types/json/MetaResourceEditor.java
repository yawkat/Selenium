package at.yawk.selenium.resourcepack.types.json;

import static at.yawk.selenium.resourcepack.types.json.Util.e;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.io.IOException;
import java.util.Map.Entry;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import at.yawk.selenium.resourcepack.McMeta;
import at.yawk.selenium.resourcepack.McMeta.McMetaException;
import at.yawk.selenium.ui.Icons;
import at.yawk.selenium.ui.ResourceEditor;

class MetaResourceEditor implements ResourceEditor {
    final JSONObject data;
    final JSONObject withDefaults;
    
    public MetaResourceEditor(McMeta meta, McMetaDefaults defaults) throws McMetaException {
        data = meta.getRoot();
        withDefaults = Util.deepClone(data);
        defaults.addDefaults(withDefaults);
    }
    
    @Override
    public JComponent getEditor() throws IOException {
        return new MetaResourceEditorComponent(this);
    }
}

class MetaResourceEditorComponent extends JPanel {
    private static final long serialVersionUID = 1L;
    
    private final MetaResourceEditor editor;
    
    private JTree tree;
    
    public MetaResourceEditorComponent(MetaResourceEditor editor) {
        this.editor = editor;
        setLayout(new BorderLayout());
        init();
    }
    
    private void init() {
        this.tree = new JTree(new Node(null, editor.data, editor.withDefaults));
        tree.setCellRenderer(new NodeCellRenderer());
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        for (int row = 0; row < tree.getRowCount(); row++) {
            tree.expandRow(row);
        }
        
        removeAll();
        add(tree);
    }
    
    class Node extends DefaultMutableTreeNode {
        private static final long serialVersionUID = 1L;
        
        final Node parent;
        /**
         * Either a String (if the parent element is a {@link JSONObject}) or an
         * Integer (if the parent element is a {@link JSONArray})
         */
        final Object key;
        /**
         * Either a {@link String}, {@link Long}, {@link Double} or
         * {@link Boolean}
         */
        final Object valueRegular;
        /**
         * Either a {@link String}, {@link Long}, {@link Double} or
         * {@link Boolean}
         */
        final Object valueDefaults;
        Node[] children;
        
        public Node(Node parent, Object key, Object valueRegular, Object valueDefaults) {
            this.parent = parent;
            this.key = key;
            this.valueRegular = valueRegular;
            this.valueDefaults = valueDefaults;
            this.children = createChildren();
            setAllowsChildren(canHaveChildren());
            
            if (parent != null) {
                parent.add(this);
            }
        }
        
        public Node(Object key, Object valueRegular, Object valueDefaults) {
            this(null, key, valueRegular, valueDefaults);
        }
        
        private Node[] createChildren() {
            if (valueDefaults instanceof JSONArray) {
                JSONArray v = (JSONArray) valueDefaults;
                Node[] children = new Node[v.size()];
                int inx = 0;
                for (Object object : v) {
                    children[inx] = new Node(this, inx, ((JSONArray) valueRegular).get(inx), object);
                    inx++;
                }
                return children;
            }
            if (valueDefaults instanceof JSONObject) {
                JSONObject v = (JSONObject) valueDefaults;
                Node[] children = new Node[v.size()];
                int inx = 0;
                for (Object object : v.entrySet()) {
                    @SuppressWarnings("unchecked")
                    Entry<String, Object> entry = (Entry<String, Object>) object;
                    children[inx] = new Node(this, entry.getKey(), valueRegular == null ? null : ((JSONObject) valueRegular).get(entry.getKey()), entry.getValue());
                    inx++;
                }
                return children;
            }
            return new Node[0];
        }
        
        boolean canHaveChildren() {
            return valueDefaults instanceof JSONArray || valueDefaults instanceof JSONObject;
        }
    }
    
    class NodeCellRenderer extends DefaultTreeCellRenderer {
        private static final long serialVersionUID = 1L;
        
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            Component c = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            
            Node node = (Node) value;
            
            String k = node.key instanceof String ? (String) node.key : "#" + node.key;
            String v;
            if (node.canHaveChildren()) {
                v = null;
                setIcon(new ImageIcon(Icons.getIcon("folder.png")));
            } else {
                v = String.valueOf(node.valueDefaults);
                setIcon(new ImageIcon(Icons.getIcon("page.png")));
            }
            
            if (selected) {
                setText(v == null ? k : k + ": " + v);
            } else {
                if (node.valueRegular != null) {
                    setText(v == null ? k : "<html><body>" + e(k) + ": <span color='#999999'>" + e(v));
                } else {
                    setText(v == null ? k : k + ": " + v);
                    setForeground(new Color(0x999999));
                }
            }
            
            return c;
        }
    }
}
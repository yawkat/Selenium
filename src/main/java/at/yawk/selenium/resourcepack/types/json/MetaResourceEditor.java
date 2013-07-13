package at.yawk.selenium.resourcepack.types.json;

import java.awt.BorderLayout;
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
        this.tree = new JTree(new Node(null, editor.data));
        tree.setCellRenderer(new NodeCellRenderer());
        tree.setRootVisible(false);
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
         * Either a {@link String}, {@link Long}, {@link Double},
         * {@link Boolean}
         */
        final Object value;
        Node[] children;
        
        public Node(Node parent, Object key, Object value) {
            this.parent = parent;
            this.key = key;
            this.value = value;
            this.children = createChildren();
            setAllowsChildren(canHaveChildren());
            
            if (parent != null) {
                parent.add(this);
            }
        }
        
        public Node(Object key, Object value) {
            this(null, key, value);
        }
        
        private Node[] createChildren() {
            if (value instanceof JSONArray) {
                JSONArray v = (JSONArray) value;
                Node[] children = new Node[v.size()];
                int inx = 0;
                for (Object object : v) {
                    children[inx] = new Node(this, inx, object);
                    inx++;
                }
                return children;
            }
            if (value instanceof JSONObject) {
                JSONObject v = (JSONObject) value;
                Node[] children = new Node[v.size()];
                int inx = 0;
                for (Object object : v.entrySet()) {
                    @SuppressWarnings("unchecked")
                    Entry<String, Object> entry = (Entry<String, Object>) object;
                    children[inx] = new Node(this, entry.getKey(), entry.getValue());
                    inx++;
                }
                return children;
            }
            return new Node[0];
        }
        
        boolean canHaveChildren() {
            return value instanceof JSONArray || value instanceof JSONObject;
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
                v = String.valueOf(node.value);
                setIcon(new ImageIcon(Icons.getIcon("page.png")));
            }
            
            setText(v == null ? k : k + ": " + v);
            
            return c;
        }
    }
}
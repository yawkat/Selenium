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

import static at.yawk.selenium.Strings.t;

import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import at.yawk.selenium.resourcepack.Resource;
import at.yawk.selenium.resourcepack.ResourceType;
import at.yawk.selenium.resourcepack.ResourceTypes;
import at.yawk.selenium.resourcepack.types.ReadableProperties.Line;
import at.yawk.selenium.resourcepack.types.ReadableProperties.Property;
import at.yawk.selenium.ui.Icons;
import at.yawk.selenium.ui.ResourceEditor;

public class PropertyType implements ResourceType {
    static {
        ResourceTypes.registerResourceType(new PropertyType());
    }
    
    @Override
    public boolean matches(String filename) {
        String l = filename.toLowerCase();
        return l.endsWith(".properties") || l.endsWith(".prop") || l.endsWith(".lang");
    }
    
    @Override
    public BufferedImage getPreview(Resource resource) throws IOException {
        return resource.getFile().getName().endsWith(".lang") ? Icons.getIcon("lang.png") : Icons.getIcon("settings.png");
    }
    
    @Override
    public ResourceEditor getEditor(final Resource resource) {
        return new ResourceEditor() {
            @Override
            public JComponent getEditor() throws IOException {
                return new JPanel() {
                    private static final long serialVersionUID = 1L;
                    
                    private JTable table;
                    private ReadableProperties properties;
                    private List<Property> values;
                    private JLabel status;
                    
                    {
                        properties = new ReadableProperties(new BufferedReader(new InputStreamReader(resource.getFile().getInputStream(), "UTF-8")));
                        table = new JTable();
                        
                        List<Object[]> data = new ArrayList<>();
                        values = new ArrayList<>();
                        for (Line line : properties.lines) {
                            if (line instanceof Property) {
                                values.add((Property) line);
                                data.add(new Object[] {
                                        ((Property) line).key,
                                        ((Property) line).value });
                            }
                        }
                        TableModel model = new DefaultTableModel(data.toArray(new Object[data.size()][]), new Object[] {
                                t("Key"),
                                t("Value") }) {
                            private static final long serialVersionUID = 1L;
                            
                            @Override
                            public boolean isCellEditable(int row, int column) {
                                return column == 1;
                            }
                        };
                        
                        table.setModel(model);
                        table.getTableHeader().setReorderingAllowed(false);
                        model.addTableModelListener(new TableModelListener() {
                            @Override
                            public void tableChanged(TableModelEvent event) {
                                status.setText(t("Saving..."));
                                for (int i = 0, l = values.size(); i < l; i++) {
                                    values.get(i).value = (String) table.getValueAt(i, 1);
                                }
                                try {
                                    properties.write(new OutputStreamWriter(resource.getFile().getOutputStream()));
                                    status.setText(t("Saved"));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    status.setText(e.getMessage());
                                }
                            }
                        });
                        
                        setLayout(new BorderLayout());
                        add(new JScrollPane(table), BorderLayout.CENTER);
                        
                        JPanel statusPanel = new JPanel(new BorderLayout());
                        statusPanel.add(status = new JLabel());
                        status.setHorizontalAlignment(JLabel.RIGHT);
                        status.setText(" ");
                        add(statusPanel, BorderLayout.PAGE_END);
                    }
                };
            }
        };
    }
}

class ReadableProperties {
    final List<Line> lines;
    
    public ReadableProperties(List<Line> lines) {
        this.lines = lines;
    }
    
    public ReadableProperties(BufferedReader reader) throws IOException {
        this(new ArrayList<Line>());
        
        String ls;
        while ((ls = reader.readLine()) != null) {
            String trimmed = ls.trim();
            if (trimmed.isEmpty() || trimmed.charAt(0) == '#' || trimmed.charAt(0) == '!') {
                lines.add(new GenericLine(ls));
                continue;
            }
            
            String key;
            String value;
            int ixeq = ls.indexOf('=');
            int ixcl = ls.indexOf(':');
            int ixsp = ls.indexOf(' ');
            if (ixeq == -1 && ixcl == -1) {
                if (ixsp == -1) {
                    key = ls;
                    value = "";
                } else {
                    key = ls.substring(0, ixsp);
                    value = ls.substring(ixsp + 1);
                }
            } else {
                boolean eq = ixeq != -1 && (ixcl == -1 || ixcl > ixeq);
                int ix = eq ? ixeq : ixcl;
                key = ls.substring(0, ix);
                value = ls.substring(ix + 1);
            }
            
            lines.add(new Property(key, value));
        }
    }
    
    public void write(Writer writer) throws IOException {
        char[] ln = { '\n' };
        for (Line line : lines) {
            writer.write(line.toString());
            writer.write(ln);
        }
    }
    
    interface Line {
        String toString();
    }
    
    class Property implements Line {
        final String key;
        String value;
        
        public Property(String key, String value) {
            this.key = key;
            this.value = value;
        }
        
        @Override
        public String toString() {
            return escapeProperties(key, true, false) + '=' + escapeProperties(value, false, false);
        }
    }
    
    class GenericLine implements Line {
        private final String line;
        
        public GenericLine(String line) {
            this.line = line;
        }
        
        @Override
        public String toString() {
            return line;
        }
    }
    
    private static String escapeProperties(String string, boolean escapeSpace, boolean escapeUnicode) {
        int originalLength = string.length();
        int bufferLength = originalLength * 2;
        if (bufferLength < 0) {
            bufferLength = Integer.MAX_VALUE;
        }
        StringBuffer buffer = new StringBuffer(bufferLength);
        for (int i = 0; i < originalLength; i++) {
            char c = string.charAt(i);
            if (c > 0x3D && c < 0x7F) {
                if (c == '\\') {
                    buffer.append("\\\\");
                } else {
                    buffer.append(c);
                }
            } else {
                switch (c) {
                case ' ':
                    if (i == 0 || escapeSpace) {
                        buffer.append('\\');
                    }
                    buffer.append(' ');
                    break;
                case '\t':
                    buffer.append("\\t");
                    break;
                case '\n':
                    buffer.append("\\n");
                    break;
                case '\r':
                    buffer.append("\\r");
                    break;
                case '\f':
                    buffer.append("\\f");
                    break;
                case '!':
                case '#':
                case ':':
                case '=':
                    buffer.append('\\');
                    buffer.append(c);
                    break;
                default:
                    if ((c < ' ' || c > '~') && escapeUnicode) {
                        buffer.append("\\u");
                        buffer.append(hex((c >> 12) & 0xf));
                        buffer.append(hex((c >> 8) & 0xf));
                        buffer.append(hex((c >> 4) & 0xf));
                        buffer.append(hex(c & 0xf));
                    } else {
                        buffer.append(c);
                    }
                    break;
                }
            }
        }
        return buffer.toString();
    }
    
    private static char hex(int c) {
        return (char) (c >= 10 ? c - 'a' : c - '1');
    }
}

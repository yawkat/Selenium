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
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import at.yawk.selenium.fs.FileSystem;
import at.yawk.selenium.resourcepack.Resource;
import at.yawk.selenium.resourcepack.ResourceTree;
import at.yawk.selenium.resourcepack.ResourceType;
import at.yawk.selenium.resourcepack.ResourceTypes;

public class WizardCompress extends Wizard {
    private static final long serialVersionUID = 1L;
    
    private final ResourceTree thisPack;
    private final JPanel panel = new JPanel();
    private JComboBox<ResourceTree> defaultPacks;
    private JProgressBar progress;
    private JTextArea progressLog;
    private ExecutorService worker = Executors.newFixedThreadPool(4);
    
    public WizardCompress(ResourceTree thisPack, ResourceTree[] otherPacks) {
        this.thisPack = thisPack;
        setMode(OPTION_NEXT | OPTION_ABORT);
        
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        JTextArea label = new JTextArea(t("This wizard will remove all files that are not changed compared to the default resource pack from this pack. Be careful, you cannot undo this operation."));
        label.setEditable(false);
        label.setCursor(null);
        label.setFocusable(false);
        label.setOpaque(false);
        label.setFont(getFont());
        label.setLineWrap(true);
        label.setWrapStyleWord(true);
        label.setPreferredSize(new Dimension(200, label.getPreferredSize().height));
        panel.add(label);
        
        defaultPacks = new JComboBox<>(otherPacks);
        defaultPacks.setRenderer(new DefaultListCellRenderer() {
            private static final long serialVersionUID = 1L;
            
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setText(((ResourceTree) value).getRoot().getName());
                return c;
            }
        });
        defaultPacks.setPreferredSize(new Dimension(200, defaultPacks.getPreferredSize().height));
        panel.add(new JLabel("Default texture pack:"));
        panel.add(defaultPacks);
        panel.setPreferredSize(new Dimension(200, Integer.MAX_VALUE));
        add(panel);
    }
    
    protected void abort() {
        dispose();
    }
    
    protected void done() {
        abort();
    }
    
    protected void next() {
        if (JOptionPane.showConfirmDialog(this, t("Are you sure you want to remove all duplicates? You cannot undo this operation!"), t("Compress"), JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            setMode(0);
            progress = new JProgressBar(0, 1);
            progressLog = new JTextArea();
            progressLog.setCursor(null);
            progressLog.setFocusable(false);
            progressLog.setEditable(false);
            panel.removeAll();
            panel.setLayout(new BorderLayout());
            panel.add(progress, BorderLayout.PAGE_START);
            panel.add(new JScrollPane(progressLog), BorderLayout.CENTER);
            panel.validate();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    ResourceTree defaults = (ResourceTree) defaultPacks.getSelectedItem();
                    for (FileSystem f : thisPack.getRoot().listChildren()) {
                        work(thisPack.getResource(f.getName()), defaults.getResource(f.getName()));
                    }
                    worker.shutdown();
                    try {
                        worker.awaitTermination(1, TimeUnit.DAYS);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    synchronized (progressLog) {
                        progressLog.setText(progressLog.getText() + t("Done!") + "\n");
                    }
                    thisPack.getRoot().flushManagingSystem();
                    thisPack.callUpdateListeners();
                    setMode(OPTION_DONE);
                }
            }).start();
        }
    }
    
    private void work(final Resource r1, final Resource r2) {
        final FileSystem f1 = r1.getFile();
        final FileSystem f2 = r2.getFile();
        
        if (f1.exists() && f2.exists() && !f1.isDirectory() && !f2.isDirectory()) {
            worker.execute(new Runnable() {
                @Override
                public void run() {
                    ResourceType type = ResourceTypes.getResourceType(r1);
                    if (type != null) {
                        if (!type.equals(r1, r2)) {
                            try {
                                f1.delete();
                                synchronized (progressLog) {
                                    progressLog.setText(progressLog.getText() + String.format(t("Deleted %s"), f1.getName()) + "\n");
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                                synchronized (progressLog) {
                                    progressLog.setText(progressLog.getText() + String.format(t("Could not delete %s"), f1.getName()) + "\n");
                                }
                            }
                        }
                    }
                    synchronized (progress) {
                        progress.setValue(progress.getValue() + 1);
                    }
                }
            });
            synchronized (progress) {
                progress.setMaximum(progress.getMaximum() + 1);
            }
        }
        
        for (FileSystem f : f1.listChildren()) {
            work(r1.getChildResource(f.getName()), r2.getChildResource(f.getName()));
        }
        
        if (f1.isDirectory()) {
            worker.execute(new Runnable() {
                @Override
                public void run() {
                    if (f1.listChildren().length == 0) {
                        try {
                            f1.delete();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
    }
}

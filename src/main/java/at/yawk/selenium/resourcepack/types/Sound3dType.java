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

import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import at.yawk.selenium.fs.FileSystem;
import at.yawk.selenium.resourcepack.Resource;
import at.yawk.selenium.resourcepack.ResourceType;
import at.yawk.selenium.resourcepack.ResourceTypes;
import at.yawk.selenium.sound.SoundManager;
import at.yawk.selenium.ui.Icons;
import at.yawk.selenium.ui.ResourceEditor;

public class Sound3dType implements ResourceType {
    private static final int LABEL_SPACING = 8;
    private static final int TICK_COUNT = 24;
    private static final int TICK_SIZE = 100;
    
    static {
        ResourceTypes.registerResourceType(new Sound3dType());
    }
    
    @Override
    public boolean matches(String filename) {
        String l = filename.toLowerCase();
        return l.endsWith(".ogg") || l.endsWith(".wav");
    }
    
    @Override
    public BufferedImage getPreview(Resource resource) throws IOException {
        return Icons.getIcon("sound.png");
    }
    
    @Override
    public ResourceEditor getEditor(final Resource resource) {
        return new ResourceEditor() {
            @Override
            public JComponent getEditor() throws IOException {
                return new JPanel() {
                    private static final long serialVersionUID = 1L;
                    
                    private float x, y, z;
                    private SoundManager playing;
                    private JButton play;
                    private JButton stop;
                    private JButton loadLwjgl;
                    private JLabel status;
                    
                    {
                        BoxLayout layout = new BoxLayout(this, BoxLayout.Y_AXIS);
                        setLayout(layout);
                        for (int i = 0; i < 2; i++) {
                            final int j = i;
                            DefaultBoundedRangeModel model = new DefaultBoundedRangeModel();
                            model.setMinimum(-TICK_SIZE * TICK_COUNT);
                            model.setMaximum(TICK_SIZE * TICK_COUNT);
                            final JSlider slider = new JSlider(model);
                            slider.setMinorTickSpacing(TICK_SIZE * LABEL_SPACING / 2);
                            slider.setMajorTickSpacing(TICK_SIZE * LABEL_SPACING);
                            slider.setPaintTicks(true);
                            slider.setPaintLabels(true);
                            Hashtable<Integer, JLabel> labels = new Hashtable<>();
                            for (int k = model.getMinimum(); k <= model.getMaximum(); k += TICK_SIZE * LABEL_SPACING) {
                                labels.put(k, new JLabel(String.valueOf(k * LABEL_SPACING / slider.getMajorTickSpacing())));
                            }
                            slider.setLabelTable(labels);
                            slider.addChangeListener(new ChangeListener() {
                                @Override
                                public void stateChanged(ChangeEvent arg0) {
                                    float v = slider.getValue() / TICK_SIZE;
                                    switch (j) {
                                    case 0:
                                        x = v;
                                        break;
                                    case 1:
                                        y = v;
                                        break;
                                    case 2:
                                        z = v;
                                        break;
                                    }
                                    updateSoundLocation();
                                }
                            });
                            add(slider);
                        }
                        JPanel statusHolder = new JPanel();
                        statusHolder.add(status = new JLabel(t("Stopped")));
                        add(statusHolder);
                        JPanel controls = new JPanel();
                        controls.add(play = new JButton(new AbstractAction(t("Play"), new ImageIcon(Icons.getIcon("play.png"))) {
                            private static final long serialVersionUID = 1L;
                            
                            @Override
                            public void actionPerformed(ActionEvent arg0) {
                                if (playing == null) {
                                    play();
                                }
                            }
                        }));
                        controls.add(stop = new JButton(new AbstractAction(t("Stop"), new ImageIcon(Icons.getIcon("stop.png"))) {
                            private static final long serialVersionUID = 1L;
                            
                            @Override
                            public void actionPerformed(ActionEvent arg0) {
                                if (playing != null) {
                                    stop();
                                }
                            }
                        }));
                        add(controls);
                        buttons(false);
                        
                        loadLwjgl = new JButton(new AbstractAction(t("Load LWJGL library natives for better sound")) {
                            @Override
                            public void actionPerformed(ActionEvent arg0) {
                                SoundManager.loadLwjgl();
                                loadLwjgl.setEnabled(!SoundManager.isLwjglLoaded());
                            }
                        });
                        loadLwjgl.setEnabled(!SoundManager.isLwjglLoaded());
                        add(loadLwjgl);
                    }
                    
                    private void play() {
                        new Thread(new Runnable() {
                            @SuppressWarnings("resource")
                            @Override
                            public void run() {
                                try {
                                    // FIXME sound cuts out
                                    buttons(true);
                                    status.setText(t("Buffering..."));
                                    FileSystem f = resource.getFile();
                                    File buffered = f.getBuffered();
                                    playing = new SoundManager().load(buffered.toURI().toURL()).setLocation(x, y, z).play();
                                    SoundManager.awaitProcess();
                                    do {
                                        status.setText(Math.round(playing.getMillisPlayed() / 100F) / 10F + " s");
                                        Thread.sleep(100);
                                    } while (!playing.hasTerminated());
                                    f.clearBuffer();
                                    playing.close();
                                    playing = null;
                                    buttons(false);
                                } catch (IOException | InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                    }
                    
                    private void stop() {
                        playing.terminate();
                    }
                    
                    private void buttons(boolean playing) {
                        if (!playing) {
                            status.setText(t("Stopped"));
                        }
                        play.setEnabled(!playing);
                        stop.setEnabled(playing);
                    }
                    
                    private void updateSoundLocation() {
                        if (playing != null) {
                            playing.setLocation(x, y, z);
                        }
                    }
                };
            }
        };
    }
    
    @Override
    public boolean equals(final Resource r1, final Resource r2) {
        try {
            return r1.getFile().contentEquals(r2.getFile());
        } catch (IOException e) {
            return false;
        }
    }
}

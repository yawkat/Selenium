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
                    private String playing;
                    private JButton play;
                    private JButton stop;
                    private JLabel status;
                    
                    {
                        BoxLayout layout = new BoxLayout(this, BoxLayout.Y_AXIS);
                        setLayout(layout);
                        for (int i = 0; i < 3; i++) {
                            final int j = i;
                            DefaultBoundedRangeModel model = new DefaultBoundedRangeModel();
                            model.setMinimum(-1000);
                            model.setMaximum(1000);
                            final JSlider slider = new JSlider(model);
                            slider.setMinorTickSpacing(50);
                            slider.setMajorTickSpacing(100);
                            slider.setPaintTicks(true);
                            slider.setPaintLabels(true);
                            Hashtable<Integer, JLabel> labels = new Hashtable<>();
                            for (int k = -1000; k <= 1000; k += 100) {
                                labels.put(k, new JLabel(String.valueOf(k / 100)));
                            }
                            slider.setLabelTable(labels);
                            slider.addChangeListener(new ChangeListener() {
                                @Override
                                public void stateChanged(ChangeEvent arg0) {
                                    float v = slider.getValue() / 100F;
                                    switch (j) {
                                    case 0:
                                        x = v;
                                        break;
                                    case 1:
                                        y = v;
                                        break;
                                    case 2:
                                        z = -v;
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
                    }
                    
                    private void play() {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    // FIXME sound cuts out
                                    buttons(true);
                                    status.setText(t("Buffering..."));
                                    FileSystem f = resource.getFile();
                                    File buffered = f.getBuffered();
                                    playing = SoundManager.play(buffered.toURI().toURL());
                                    updateSoundLocation();
                                    SoundManager.awaitProcess();
                                    do {
                                        status.setText(Math.round(SoundManager.getMillisPlayed(playing) / 100F) / 10F + " s");
                                        Thread.sleep(100);
                                    } while (!SoundManager.hasTerminated(playing));
                                    f.clearBuffer();
                                    playing = null;
                                    buttons(false);
                                } catch (IOException | InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                    }
                    
                    private void stop() {
                        SoundManager.terminate(playing);
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
                            SoundManager.setLocation(playing, x, y, z);
                        }
                    }
                };
            }
        };
    }
}

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
package at.yawk.selenium.sound;

import java.net.URL;

import paulscode.sound.CommandObject;
import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemConfig;
import paulscode.sound.codecs.CodecJOrbis;
import paulscode.sound.codecs.CodecWav;
import paulscode.sound.libraries.LibraryJavaSound;

public class SoundManager {
    private SoundManager() {}
    
    private static final Object soundSystemQueueNotify = new Object();
    private static SoundSystem soundSystem;
    
    static {
        SoundSystemConfig.setCodec("ogg", CodecJOrbis.class);
        SoundSystemConfig.setCodec("wav", CodecWav.class);
        SoundSystemConfig.addLibrary(LibraryJavaSound.class);
        soundSystem = new SoundSystem() {
            @Override
            public boolean CommandQueue(CommandObject newCommand) {
                boolean b = super.CommandQueue(newCommand);
                if (newCommand == null) {
                    synchronized (soundSystemQueueNotify) {
                        soundSystemQueueNotify.notifyAll();
                    }
                }
                return b;
            }
        };
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                soundSystem.cleanup();
            }
        }));
    }
    
    public static String play(URL url) {
        return soundSystem.quickPlay(false, url, soundSystem.randomNumberGenerator.nextInt() + ".ogg", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_LINEAR, 64);
    }
    
    public static void terminate(String id) {
        soundSystem.stop(id);
    }
    
    public static boolean hasTerminated(String id) {
        return !soundSystem.playing(id);
    }
    
    public static float getMillisPlayed(String id) {
        if (hasTerminated(id)) {
            return 0;
        }
        return soundSystem.millisecondsPlayed(id);
    }
    
    public static void awaitProcess() throws InterruptedException {
        synchronized (soundSystemQueueNotify) {
            soundSystemQueueNotify.wait();
        }
    }
    
    public static void setLocation(String id, float x, float y, float z) {
        soundSystem.setPosition(id, x, y, z);
    }
}

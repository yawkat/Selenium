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

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import com.google.common.io.ByteStreams;

import paulscode.sound.CommandObject;
import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemConfig;
import paulscode.sound.codecs.CodecJOrbis;
import paulscode.sound.codecs.CodecWav;
import paulscode.sound.libraries.LibraryJavaSound;
import paulscode.sound.libraries.LibraryLWJGLOpenAL;

public class SoundManager implements Closeable {
    private static final Object soundSystemQueueNotify = new Object();
    private static SoundSystem soundSystem;
    private static SoundManager currentManager;
    private static boolean loadedLwjgl = false;
    
    static {
        SoundSystemConfig.addLibrary(LibraryJavaSound.class);
        SoundSystemConfig.setCodec("ogg", CodecJOrbis.class);
        SoundSystemConfig.setCodec("wav", CodecWav.class);
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
        soundSystem.setListenerPosition(0, 0, 0);
        soundSystem.setListenerAngle(0);
        soundSystem.setListenerVelocity(0, 0, 0);
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                soundSystem.cleanup();
            }
        }));
    }
    
    public static void awaitProcess() throws InterruptedException {
        synchronized (soundSystemQueueNotify) {
            soundSystemQueueNotify.wait();
        }
    }
    
    public static boolean loadLwjgl() {
        if (loadedLwjgl) {
            return true;
        }
        
        String osName = System.getProperty("os.name").toLowerCase();
        String[] natives;
        if (osName.contains("win") || osName.contains("linux") || osName.contains("unix")) {
            boolean is64 = System.getProperty("os.name").contains("64");
            natives = is64 ? new String[] {
                    "/lwjgl",
                    "/OpenAL32" } : new String[] {
                    "/lwjgl64",
                    "/OpenAL64" };
            if (osName.contains("win")) {
                for (int i = 0; i < natives.length; i++) {
                    natives[i] += ".dll";
                }
            } else {
                for (int i = 0; i < natives.length; i++) {
                    natives[i] += ".so";
                }
            }
        } else if (osName.contains("mac")) {
            natives = new String[] {
                    "/liblwjgl.jnilib",
                    "/openal.dylib" };
        } else {
            return false;
        }
        try {
            final File tmp = File.createTempFile("natives", null);
            tmp.delete();
            tmp.mkdir();
            for (final String name : natives) {
                final File t = new File(tmp, name);
                try (InputStream in = SoundManager.class.getResourceAsStream(name); OutputStream out = new FileOutputStream(t)) {
                    ByteStreams.copy(in, out);
                    Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                        @Override
                        public void run() {
                            t.delete();
                        }
                    }));
                }
            }
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    tmp.delete();
                }
            }));
            System.setProperty("org.lwjgl.librarypath", tmp.getAbsolutePath());
            SoundSystemConfig.removeLibrary(LibraryJavaSound.class);
            SoundSystemConfig.addLibrary(LibraryLWJGLOpenAL.class);
            soundSystem.switchLibrary(LibraryLWJGLOpenAL.class);
            return loadedLwjgl = true;
        } catch (IOException | UnsatisfiedLinkError e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public static boolean isLwjglLoaded() {
        return loadedLwjgl;
    }
    
    private String soundId = Long.toHexString(soundSystem.randomNumberGenerator.nextLong());
    
    private boolean playing = false;
    private float x, y, z;
    
    public SoundManager() {}
    
    public SoundManager load(URL url) {
        if (playing) {
            throw new IllegalStateException();
        }
        soundSystem.newSource(false, soundId, url, soundId + ".ogg", false, x, y, z, SoundSystemConfig.ATTENUATION_LINEAR, 64);
        return this;
    }
    
    public SoundManager play() {
        if (currentManager != null) {
            currentManager.terminate();
            currentManager.close();
        }
        currentManager = this;
        playing = true;
        soundSystem.play(soundId);
        return this;
    }
    
    public SoundManager terminate() {
        soundSystem.stop(soundId);
        return this;
    }
    
    public SoundManager setLocation(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
        soundSystem.setPosition(soundId, x, y, z);
        return this;
    }
    
    public boolean hasTerminated() {
        return !soundSystem.playing(soundId);
    }
    
    public float getMillisPlayed() {
        if (hasTerminated()) {
            return 0;
        }
        return soundSystem.millisecondsPlayed(soundId);
    }
    
    @Override
    public void close() {
        soundSystem.removeSource(soundId);
    }
}

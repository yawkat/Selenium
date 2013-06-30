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
package at.yawk.selenium.fs;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.WeakHashMap;

import com.google.common.collect.Sets;
import de.schlichtherle.truezip.file.TFile;
import de.schlichtherle.truezip.file.TFileInputStream;
import de.schlichtherle.truezip.file.TFileOutputStream;

public class TrueZipFileSystem implements FileSystem, Closeable {
    private static final Collection<Closeable> closables = Sets.newSetFromMap(new WeakHashMap<Closeable, Boolean>());
    
    private final TFile file;
    private File buffered;
    
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                for (Closeable closeable : closables) {
                    try {
                        closeable.close();
                    } catch (IOException e) {}
                }
                closables.clear();
            }
        }));
    }
    
    private TrueZipFileSystem(TFile file) {
        this.file = file;
        closables.add(this);
    }
    
    public TrueZipFileSystem(File file) {
        this(new TFile(file));
    }
    
    @Override
    public boolean isDirectory() {
        return file.isDirectory();
    }
    
    @Override
    public boolean exists() {
        return file.exists();
    }
    
    @Override
    public FileSystem[] listChildren() {
        TFile[] files = file.listFiles();
        if (files == null) {
            return null;
        }
        FileSystem[] result = new FileSystem[files.length];
        for (int i = 0; i < files.length; i++) {
            result[i] = new TrueZipFileSystem(files[i]);
        }
        return result;
    }
    
    @Override
    public FileSystem getChild(String name) {
        return new TrueZipFileSystem(new TFile(file, name));
    }
    
    @Override
    public InputStream getInputStream() throws IOException {
        return new TFileInputStream(file);
    }
    
    @Override
    public OutputStream getOutputStream() throws IOException {
        return new TFileOutputStream(file);
    }
    
    @Override
    public String getName() {
        return file.getName();
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((file == null) ? 0 : file.hashCode());
        return result;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        TrueZipFileSystem other = (TrueZipFileSystem) obj;
        if (file == null) {
            if (other.file != null) {
                return false;
            }
        } else if (!file.equals(other.file)) {
            return false;
        }
        return true;
    }
    
    @Override
    public File getBuffered() throws IOException {
        if (buffered == null) {
            if (file.getTopLevelArchive() == null) {
                buffered = file;
            } else {
                buffered = File.createTempFile("tmp", getName());
                file.cp(buffered);
            }
        }
        return buffered;
    }
    
    @Override
    public void clearBuffer() {
        if (buffered != file && buffered != null) {
            buffered.delete();
            buffered = null;
        }
    }
    
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        close();
    }
    
    @Override
    public void close() throws IOException {
        clearBuffer();
    }
}

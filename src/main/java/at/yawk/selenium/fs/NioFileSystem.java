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

import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ServiceConfigurationError;
import java.util.WeakHashMap;

import at.yawk.selenium.ui.Icons;

import com.google.common.collect.Sets;

public class NioFileSystem implements FileSystem, Closeable {
    private static final Collection<Closeable> closables = Sets.newSetFromMap(new WeakHashMap<Closeable, Boolean>());
    
    private final Path file;
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
    
    public NioFileSystem(Path path) {
        this.file = path;
        closables.add(this);
    }
    
    @Override
    public boolean isDirectory() {
        try {
            return Files.isDirectory(file);
        } catch (ServiceConfigurationError e) {
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public boolean exists() {
        return Files.exists(file);
    }
    
    @Override
    public FileSystem[] listChildren() {
        try {
            DirectoryStream<Path> files = Files.newDirectoryStream(file);
            Collection<FileSystem> result = new ArrayList<FileSystem>();
            for (Path path : files) {
                result.add(new NioFileSystem(path));
            }
            return result.toArray(new FileSystem[result.size()]);
        } catch (IOException e) {
            return new FileSystem[0];
        }
    }
    
    @Override
    public FileSystem getChild(String name) {
        return new NioFileSystem(file.resolve(name));
    }
    
    @Override
    public InputStream getInputStream() throws IOException {
        return Files.newInputStream(file);
    }
    
    @Override
    public OutputStream getOutputStream() throws IOException {
        return Files.newOutputStream(file);
    }
    
    @Override
    public String getName() {
        return file.getFileName().toString();
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
        NioFileSystem other = (NioFileSystem) obj;
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
            if (isFile()) {
                buffered = file.toFile();
            } else {
                buffered = File.createTempFile("tmp", getName());
                Files.copy(file, buffered.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        }
        return buffered;
    }
    
    @Override
    public void clearBuffer() {
        if (buffered != null && !buffered.toPath().equals(file)) {
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
    
    @Override
    public BufferedImage getFileTypePreview() {
        return isDirectory() ? (isFile() ? Icons.getIcon("folder.png") : Icons.getIcon("package.png")) : Icons.getIcon("page.png");
    }
    
    private boolean isFile() {
        try {
            file.toFile().toURI().toURL();
            return true;
        } catch (UnsupportedOperationException | MalformedURLException e) {
            return false;
        }
    }
    
    @Override
    public String getRelativePath(FileSystem root) {
        if (!(root instanceof NioFileSystem)) {
            throw new UnsupportedOperationException();
        }
        return ((NioFileSystem) root).file.relativize(file).toString();
    }
    
    @Override
    public void delete() throws IOException {
        Files.deleteIfExists(file);
    }
    
    @Override
    public void flushManagingSystem() {
        // TODO
    }
}

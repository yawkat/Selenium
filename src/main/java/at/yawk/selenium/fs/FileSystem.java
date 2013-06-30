/*******************************************************************************
 * Selenium, Minecraft texture pack viewer and editor
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface FileSystem {
    boolean isDirectory();
    
    boolean exists();
    
    String getName();
    
    FileSystem[] listChildren();
    
    FileSystem getChild(String name);
    
    InputStream getInputStream() throws IOException;
    
    OutputStream getOutputStream() throws IOException;
    
    File getBuffered() throws IOException;

    void clearBuffer();
}

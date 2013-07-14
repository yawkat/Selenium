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
package at.yawk.selenium.resourcepack;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import at.yawk.selenium.fs.FileSystem;

public class McMeta {
    private final FileSystem file;
    private JSONObject rootObject;
    
    public McMeta(FileSystem file, boolean lazy) throws McMetaException {
        this.file = file;
        if (!lazy) {
            getRoot();
        }
    }
    
    public McMeta(FileSystem file) throws McMetaException {
        this(file, true);
    }
    
    public JSONObject getRoot() throws McMetaException {
        if (rootObject == null) {
            if (file.exists()) {
                JSONParser parser = new JSONParser();
                try (InputStream i = file.getInput()) {
                    rootObject = (JSONObject) parser.parse(new InputStreamReader(i));
                } catch (ParseException | IOException e) {
                    throw new McMetaException(e);
                }
            } else {
                rootObject = new JSONObject();
            }
        }
        return rootObject;
    }
    
    public FileSystem getFile() {
        return file;
    }
    
    public static class McMetaException extends Exception {
        private static final long serialVersionUID = 1L;
        
        private McMetaException(Throwable cause) {
            super(cause);
        }
        
        private McMetaException() {}
    }
}

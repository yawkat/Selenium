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
package at.yawk.selenium.resourcepack;

import at.yawk.selenium.fs.FileSystem;
import at.yawk.selenium.resourcepack.McMeta.McMetaException;

public class Resource {
    private final ResourceTree tree;
    private final String path;
    
    public Resource(ResourceTree tree, String path) {
        this.tree = tree;
        this.path = path;
    }
    
    public McMeta getMeta() throws McMetaException {
        return new McMeta(tree.getRoot().getChild(path + ".mcmeta"));
    }
    
    public String getPath() {
        return path;
    }
    
    public FileSystem getFile() {
        return tree.getRoot().getChild(path);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((path == null) ? 0 : path.hashCode());
        result = prime * result + ((tree == null) ? 0 : tree.hashCode());
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
        Resource other = (Resource) obj;
        if (path == null) {
            if (other.path != null) {
                return false;
            }
        } else if (!path.equals(other.path)) {
            return false;
        }
        if (tree == null) {
            if (other.tree != null) {
                return false;
            }
        } else if (!tree.equals(other.tree)) {
            return false;
        }
        return true;
    }
}

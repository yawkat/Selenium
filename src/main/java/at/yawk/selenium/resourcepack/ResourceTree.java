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

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArraySet;

import at.yawk.selenium.fs.FileSystem;

public class ResourceTree {
    private final FileSystem root;
    private Collection<ResourceTreeUpdateListener> updateListeners = new CopyOnWriteArraySet<>();
    
    public ResourceTree(FileSystem root) {
        this.root = root;
    }
    
    public FileSystem getRoot() {
        return root;
    }
    
    public Resource getResource(String path) {
        return new Resource(this, path);
    }
    
    public void addResourceTreeUpdateListener(ResourceTreeUpdateListener listener) {
        updateListeners.add(listener);
    }
    
    public boolean removeResourceTreeUpdateListener(ResourceTreeUpdateListener listener) {
        return updateListeners.remove(listener);
    }
    
    public void callUpdateListeners() {
        for (ResourceTreeUpdateListener listener : updateListeners) {
            listener.onTreeUpdate(this);
        }
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((root == null) ? 0 : root.hashCode());
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
        ResourceTree other = (ResourceTree) obj;
        if (root == null) {
            if (other.root != null) {
                return false;
            }
        } else if (!root.equals(other.root)) {
            return false;
        }
        return true;
    }
    
    public static interface ResourceTreeUpdateListener {
        void onTreeUpdate(ResourceTree tree);
    }
}

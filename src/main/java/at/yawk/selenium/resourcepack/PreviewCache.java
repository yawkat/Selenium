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

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import at.yawk.selenium.Util;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class PreviewCache {
    private PreviewCache() {}
    
    private static final LoadingCache<ResourceImageKey, BufferedImage> images = CacheBuilder.newBuilder().build(new CacheLoader<ResourceImageKey, BufferedImage>() {
        @Override
        public BufferedImage load(ResourceImageKey key) throws Exception {
            BufferedImage result = key.type.getPreview(key.resource);
            if (key.size.width != -1 && key.size.height != -1) {
                result = Util.resizeImage(result, key.size.width, key.size.height);
            }
            return result;
        }
    });
    
    public static final BufferedImage getPreview(Resource resource, ResourceType type) throws IOException {
        return getPreview(resource, type, -1, -1);
    }
    
    public static final BufferedImage getPreview(Resource resource, ResourceType type, int width, int height) throws IOException {
        try {
            return images.get(new ResourceImageKey(resource, type, new Dimension(width, height)));
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IOException) {
                throw (IOException) cause;
            } else {
                throw new RuntimeException(cause);
            }
        }
    }
    
    private static class ResourceImageKey {
        private final Resource resource;
        private final ResourceType type;
        private final Dimension size;
        
        public ResourceImageKey(Resource resource, ResourceType type, Dimension size) {
            this.resource = resource;
            this.type = type;
            this.size = size;
        }
        
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((resource == null) ? 0 : resource.hashCode());
            result = prime * result + ((size == null) ? 0 : size.hashCode());
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
            ResourceImageKey other = (ResourceImageKey) obj;
            if (resource == null) {
                if (other.resource != null) {
                    return false;
                }
            } else if (!resource.equals(other.resource)) {
                return false;
            }
            if (size == null) {
                if (other.size != null) {
                    return false;
                }
            } else if (!size.equals(other.size)) {
                return false;
            }
            return true;
        }
    }
}

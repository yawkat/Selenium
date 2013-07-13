package at.yawk.selenium.resourcepack.types.json;

import static at.yawk.selenium.resourcepack.types.json.Util.*;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public interface McMetaDefaults {
    String getName();
    
    void addDefaults(JSONObject object);
    
    public static final McMetaDefaults DEFAULTS_NONE = new McMetaDefaults() {
        @Override
        public String getName() {
            return "None";
        }
        
        @Override
        public void addDefaults(JSONObject object) {}
    };
    
    public static final McMetaDefaults DEFAULTS_TEXTURE = new McMetaDefaults() {
        @Override
        public String getName() {
            return "Texture";
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public void addDefaults(JSONObject object) {
            JSONObject animation = putIfNotExists(object, "animation", new JSONObject());
            putIfNotExists(animation, "frametime", 1);
            putIfNotExists(animation, "width", 16);
            putIfNotExists(animation, "height", 16);
            JSONArray frames = putIfNotExists(animation, "frames", new JSONArray());
            int ix = 0;
            for (Object o : frames) {
                if (o instanceof JSONObject) {
                    putIfNotExists((JSONObject) o, "time", 1);
                    putIfNotExists((JSONObject) o, "index", ix);
                }
                ix++;
            }
            
            JSONObject texture = putIfNotExists(object, "texture", new JSONObject());
            putIfNotExists(texture, "blur", false);
            putIfNotExists(texture, "clamp", false);
        }
    };
}

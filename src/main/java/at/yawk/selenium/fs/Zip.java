package at.yawk.selenium.fs;

import java.io.File;
import java.nio.file.Path;

import de.schlichtherle.truezip.nio.file.TPath;

public class Zip {
    private Zip() {}
    
    public static Path toPath(File file) {
        return new TPath(file);
    }
}

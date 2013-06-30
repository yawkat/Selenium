package at.yawk.selenium.fs;

import java.io.File;
import java.nio.file.Path;

import de.schlichtherle.truezip.file.TArchiveDetector;
import de.schlichtherle.truezip.file.TConfig;
import de.schlichtherle.truezip.fs.archive.zip.ZipDriverService;
import de.schlichtherle.truezip.fs.nio.file.FileDriverService;
import de.schlichtherle.truezip.nio.file.TPath;

public class Zip {
    static {
        TConfig.get().setArchiveDetector(new TArchiveDetector(TConfig.get().getArchiveDetector(), new ZipDriverService().get()));
        TConfig.get().setArchiveDetector(new TArchiveDetector(TConfig.get().getArchiveDetector(), new FileDriverService().get()));
    }
    
    private Zip() {}
    
    public static Path toPath(File file) {
        return new TPath(file);
    }
}

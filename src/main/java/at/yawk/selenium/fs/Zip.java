package at.yawk.selenium.fs;

import java.io.File;

import de.schlichtherle.truezip.file.TArchiveDetector;
import de.schlichtherle.truezip.file.TConfig;
import de.schlichtherle.truezip.fs.FsSyncException;
import de.schlichtherle.truezip.fs.FsSyncOption;
import de.schlichtherle.truezip.fs.archive.zip.ZipDriverService;
import de.schlichtherle.truezip.fs.nio.file.FileDriverService;
import de.schlichtherle.truezip.nio.file.TPath;
import de.schlichtherle.truezip.util.BitField;

public class Zip {
    static {
        TConfig.get().setArchiveDetector(new TArchiveDetector(TConfig.get().getArchiveDetector(), new ZipDriverService().get()));
        TConfig.get().setArchiveDetector(new TArchiveDetector(TConfig.get().getArchiveDetector(), new FileDriverService().get()));
    }
    
    private Zip() {}
    
    public static NioFileSystem toFileSystem(File file) {
        return new NioFileSystem(new TPath(file)) {
            @Override
            public void flushManagingSystem() {
                try {
                    ((TPath) getPath()).getFileSystem().sync(BitField.of(FsSyncOption.FORCE_CLOSE_INPUT, FsSyncOption.FORCE_CLOSE_OUTPUT, FsSyncOption.CLEAR_CACHE));
                } catch (FsSyncException e) {
                    e.printStackTrace();
                }
            }
        };
    }
}

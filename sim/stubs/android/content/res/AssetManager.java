package android.content.res;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class AssetManager {
    public InputStream open(String fileName) throws IOException {
        throw new FileNotFoundException(fileName);
    }
}

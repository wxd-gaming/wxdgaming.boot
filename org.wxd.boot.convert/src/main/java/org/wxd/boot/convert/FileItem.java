package org.wxd.boot.convert;

import java.io.File;
import java.io.Serializable;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2022-05-19 13:41
 **/
public class FileItem implements Serializable {

    private File file;

    public FileItem(File file) {
        this.file = file;
    }

    public String getName() {
        return file.getName();
    }

    public String getPath() {
        return file.getPath();
    }

    public String getDirectory() {
        return file.getAbsoluteFile().getParent();
    }

    public File getFile() {
        return file;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FileItem fileItem = (FileItem) o;

        return file != null ? getName().equalsIgnoreCase(fileItem.getName()) : fileItem.file == null;
    }

    @Override
    public int hashCode() {
        return file != null ? file.hashCode() : 0;
    }

    @Override
    public String toString() {
        return file.getName();
    }

}

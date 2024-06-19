package zone.cogni.core.util;

import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileHelper {
  private FileHelper() {
  }

  public static FileInputStream openInputStream(File file) {
    try {
      return new FileInputStream(file);
    }
    catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  public static FileOutputStream openOutputStream(@Nonnull File file) {
    try {
      Files.createParentDirs(file);
      return FileUtils.openOutputStream(file);
    }
    catch (IOException e) {
      throw new RuntimeException( "Failed to open outputstream to " + file, e);
    }
  }

  @Deprecated
  public static void writeStringToFile(File file, String data) {
    try {
      FileUtils.writeStringToFile(file, data);
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}

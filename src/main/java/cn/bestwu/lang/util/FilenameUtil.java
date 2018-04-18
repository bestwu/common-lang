package cn.bestwu.lang.util;

import java.io.File;
import org.springframework.util.Assert;

/**
 * 文件名工具
 *
 * @author Peter Wu
 */
public class FilenameUtil {

  /**
   * The extension separator character.
   */
  public static final char EXTENSION_SEPARATOR = '.';

  /**
   * The extension separator String.
   */
  public static final String EXTENSION_SEPARATOR_STR = Character
      .toString(EXTENSION_SEPARATOR);

  /**
   * The Unix separator character.
   */
  private static final char UNIX_SEPARATOR = '/';

  /**
   * The Windows separator character.
   */
  private static final char WINDOWS_SEPARATOR = '\\';

  public static String getBaseName(String filename) {
    return removeExtension(getName(filename));
  }

  public static String getName(String filename) {
    if (filename == null) {
      return null;
    }
    int index = indexOfLastSeparator(filename);
    return filename.substring(index + 1);
  }

  public static String removeExtension(String filename) {
    if (filename == null) {
      return null;
    }
    int index = indexOfExtension(filename);
    if (index == -1) {
      return filename;
    } else {
      return filename.substring(0, index);
    }
  }

  public static int indexOfExtension(String filename) {
    if (filename == null) {
      return -1;
    }
    int extensionPos = filename.lastIndexOf(EXTENSION_SEPARATOR);
    int lastSeparator = indexOfLastSeparator(filename);
    return lastSeparator > extensionPos ? -1 : extensionPos;
  }

  public static int indexOfLastSeparator(String filename) {
    if (filename == null) {
      return -1;
    }
    int lastUnixPos = filename.lastIndexOf(UNIX_SEPARATOR);
    int lastWindowsPos = filename.lastIndexOf(WINDOWS_SEPARATOR);
    return Math.max(lastUnixPos, lastWindowsPos);
  }

  public static String getExtension(File file) {
    Assert.notNull(file, "file must not be null");
    String name = file.getName();
    return getExtension(name);
  }

  public static String getExtension(String fileName) {
    Assert.notNull(fileName, "fileName must not be null");
    int dotIndex = fileName.lastIndexOf('.');
    if (dotIndex == -1) {
      return null;
    }
    return fileName.substring(dotIndex + 1);
  }
}

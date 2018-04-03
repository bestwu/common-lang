package cn.bestwu.lang.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 字符串工具类
 *
 * @author Peter Wu
 */
public class StringUtil {

  private static Logger log = LoggerFactory.getLogger(StringUtil.class);

  public static ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  public static ObjectMapper INDENT_OUTPUT_OBJECT_MAPPER = new ObjectMapper();

  static {
    OBJECT_MAPPER.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    INDENT_OUTPUT_OBJECT_MAPPER.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
  }

  /**
   * @param str 字符
   * @return 是否有长度
   */
  public static boolean hasLength(CharSequence str) {
    return (str != null && str.length() > 0);
  }

  /**
   * @param str 字符
   * @return 是否有字符
   */
  public static boolean hasText(CharSequence str) {
    if (!hasLength(str)) {
      return false;
    }
    int strLen = str.length();
    for (int i = 0; i < strLen; i++) {
      if (!Character.isWhitespace(str.charAt(i))) {
        return true;
      }
    }
    return false;
  }

  /**
   * @param s 字符串
   * @return 转换为带下划线的小写字符
   */
  public static String addUnderscores(String s) {
    StringBuilder buf = new StringBuilder(s.replace('.', '_'));
    for (int i = 1; i < buf.length() - 1; i++) {
      if (
          Character.isLowerCase(buf.charAt(i - 1)) &&
              Character.isUpperCase(buf.charAt(i)) &&
              Character.isLowerCase(buf.charAt(i + 1))
          ) {
        buf.insert(i++, '_');
      }
    }
    return buf.toString().toLowerCase(Locale.ROOT);
  }

  /**
   * 转换为字符串
   *
   * @param object 对象
   * @return 字符串
   */
  public static String valueOf(Object object) {
    return valueOf(object, false);
  }

  /**
   * 转换为字符串
   *
   * @param object 对象
   * @param format 是否格式化输出
   * @return 字符串
   */
  public static String valueOf(Object object, boolean format) {
    try {
      String string;
      if (format) {
        string = INDENT_OUTPUT_OBJECT_MAPPER.writeValueAsString(object);
      } else {
        string = OBJECT_MAPPER.writeValueAsString(object);
      }
      return string;
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
    return String.valueOf(object);
  }

  /**
   * 截取一定长度的字符
   *
   * @param str 字符串
   * @param length 长度
   * @return 截取后的字符串
   */
  public static String subString(String str, int length) {
    if (str == null) {
      return null;
    }
    int l = str.length();
    if (l > length) {
      return str.substring(0, length);
    } else {
      return str;
    }
  }

  /**
   * 截取一定长度的字符，结果以...结尾
   *
   * @param str 字符串
   * @param length 长度
   * @return 截取后的字符串
   */
  public static String subStringWithEllipsis(String str, int length) {
    if (str == null) {
      return null;
    }
    int l = str.length();
    if (l > length) {
      return str.substring(0, length - 3) + "...";
    } else {
      return str;
    }
  }

  /**
   * 计算字符串包含子字符串的个数
   *
   * @param str 字符串
   * @param sub 子字符串
   * @return 个数
   */
  public static int countSubString(String str, String sub) {
    if (str.contains(sub)) {
      return splitWorker(str, sub, -1, false).length - 1;
    } else {
      return 0;
    }
  }

  /**
   * 分割字符串
   *
   * @param str 字符串
   * @param separatorChars 分隔符
   * @param max 最大数量
   * @param preserveAllTokens preserveAllTokens
   * @return 分割后数组
   */
  private static String[] splitWorker(final String str, final String separatorChars, final int max,
      final boolean preserveAllTokens) {
    // Performance tuned for 2.0 (JDK1.4)
    // Direct code is quicker than StringTokenizer.
    // Also, StringTokenizer uses isSpace() not isWhitespace()

    if (str == null) {
      return null;
    }
    final int len = str.length();
    if (len == 0) {
      return new String[0];
    }
    final List<String> list = new ArrayList<>();
    int sizePlus1 = 1;
    int i = 0, start = 0;
    boolean match = false;
    boolean lastMatch = false;
    if (separatorChars == null) {
      // Null separator means use whitespace
      while (i < len) {
        if (Character.isWhitespace(str.charAt(i))) {
          if (match || preserveAllTokens) {
            lastMatch = true;
            if (sizePlus1++ == max) {
              i = len;
              lastMatch = false;
            }
            list.add(str.substring(start, i));
            match = false;
          }
          start = ++i;
          continue;
        }
        lastMatch = false;
        match = true;
        i++;
      }
    } else if (separatorChars.length() == 1) {
      // Optimise 1 character case
      final char sep = separatorChars.charAt(0);
      while (i < len) {
        if (str.charAt(i) == sep) {
          if (match || preserveAllTokens) {
            lastMatch = true;
            if (sizePlus1++ == max) {
              i = len;
              lastMatch = false;
            }
            list.add(str.substring(start, i));
            match = false;
          }
          start = ++i;
          continue;
        }
        lastMatch = false;
        match = true;
        i++;
      }
    } else {
      // standard case
      while (i < len) {
        if (separatorChars.indexOf(str.charAt(i)) >= 0) {
          if (match || preserveAllTokens) {
            lastMatch = true;
            if (sizePlus1++ == max) {
              i = len;
              lastMatch = false;
            }
            list.add(str.substring(start, i));
            match = false;
          }
          start = ++i;
          continue;
        }
        lastMatch = false;
        match = true;
        i++;
      }
    }
    if (match || preserveAllTokens && lastMatch) {
      list.add(str.substring(start, i));
    }
    return list.toArray(new String[0]);
  }

  /**
   * 压缩字符
   *
   * @param str 待压缩字符
   * @return 压缩后字符
   */
  public static String compress(String str) {
    if (str == null || str.length() == 0) {
      return str;
    }
    try {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      DeflaterOutputStream gzip = new DeflaterOutputStream(out);
      gzip.write(str.getBytes());
      gzip.close();
      return new String(out.toByteArray(), "ISO-8859-1");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

  }

  public static String decompress(String str) {
    if (str == null || str.length() == 0) {
      return str;
    }
    try {
      ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
          str.getBytes("ISO-8859-1"));
      InflaterInputStream zipInputStream = new InflaterInputStream(byteArrayInputStream);
      return copyToString(zipInputStream, Charset.forName("ISO-8859-1"));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static String copyToString(InputStream in, Charset charset) throws IOException {
    StringBuilder out = new StringBuilder();
    InputStreamReader reader = new InputStreamReader(in, charset);
    char[] buffer = new char[4096];
    int bytesRead;
    while ((bytesRead = reader.read(buffer)) != -1) {
      out.append(buffer, 0, bytesRead);
    }
    return out.toString();
  }

  /**
   * 格式化
   *
   * @param jsonStr jsonStr
   * @return String
   */
  public static String formatJson(String jsonStr) {
    if (null == jsonStr || "".equals(jsonStr)) {
      return "";
    }
    StringBuilder sb = new StringBuilder();
    char last;
    char current = '\0';
    int indent = 0;
    boolean isInQuotationMarks = false;
    for (int i = 0; i < jsonStr.length(); i++) {
      last = current;
      current = jsonStr.charAt(i);
      switch (current) {
        case '"':
          if (last != '\\') {
            isInQuotationMarks = !isInQuotationMarks;
          }
          sb.append(current);
          break;
        case '{':
        case '[':
          sb.append(current);
          if (!isInQuotationMarks) {
            sb.append('\n');
            indent++;
            addIndentBlank(sb, indent);
          }
          break;
        case '}':
        case ']':
          if (!isInQuotationMarks) {
            sb.append('\n');
            indent--;
            addIndentBlank(sb, indent);
          }
          sb.append(current);
          break;
        case ',':
          sb.append(current);
          if (last != '\\' && !isInQuotationMarks) {
            sb.append('\n');
            addIndentBlank(sb, indent);
          }
          break;
        default:
          sb.append(current);
      }
    }

    return sb.toString();
  }

  /**
   * 添加space
   *
   * @param sb sb
   * @param indent indent
   */
  private static void addIndentBlank(StringBuilder sb, int indent) {
    for (int i = 0; i < indent; i++) {
      sb.append("  ");
    }
  }
}

package cn.bestwu.lang.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Peter Wu
 */
public class StringUtilTest {

  @Test
  public void valueOf() {
    Assert.assertEquals("null", StringUtil.valueOf(null));
  }
}
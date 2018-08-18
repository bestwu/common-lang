package cn.bestwu.lang.util

import org.junit.Assert
import org.junit.Test

/**
 * @author Peter Wu
 */
class StringUtilTest {

    @Test
    fun valueOf() {
        Assert.assertEquals("null", StringUtil.valueOf(null))
    }
}
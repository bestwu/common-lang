package cn.bestwu.lang.util

import kotlin.experimental.and

/**
 * 字符工具类
 *
 * @author Peter Wu
 */
object CharUtil {

    /**
     * 将字符串转移为ASCII码
     *
     * @param str 字符串
     * @return ASCII码
     */
    fun getCnASCII(str: String): String {
        val sb = StringBuilder()
        val strByte = str.toByteArray()
        for (aStrByte in strByte) {
            sb.append(Integer.toHexString((aStrByte and 0xff.toByte()).toInt()))
        }
        return sb.toString()
    }


    /**
     * 是否为汉字
     *
     * @param c 字符
     * @return 是否为汉字
     */

    fun isCNChar(c: Char): Boolean {
        return Character.toString(c).matches("[\\u4E00-\\u9FA5]+".toRegex())
    }

    /**
     * 是否为大写字母
     *
     * @param capital capital
     * @return 是否为大写字母
     */
    fun isBigCapital(capital: String): Boolean {
        return capital.matches("[\\u0041-\\u005A]+".toRegex())
    }

    /**
     * 是否为汉字字符串(只要包含了一个汉字)
     *
     * @param str 字符
     * @return 是否为汉字字符串
     */
    fun hasCNStr(str: String): Boolean {
        for (c in str.toCharArray()) {
            if (isCNChar(c)) {// 如果有一个为汉字
                return true
            }
        }
        // 如果没有一个汉字，全英文字符串
        return false
    }
}
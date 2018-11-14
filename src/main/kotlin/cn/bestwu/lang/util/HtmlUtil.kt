package cn.bestwu.lang.util

import org.apache.lucene.analysis.charfilter.HTMLStripCharFilter
import org.apache.lucene.analysis.charfilter.MappingCharFilter
import org.apache.lucene.analysis.charfilter.NormalizeCharMap
import java.io.IOException
import java.io.StringReader

/**
 * HTML 工具类
 */
object HtmlUtil {

    /**
     * 截取纯文本内容
     *
     * @param inputString 输入HTML内容
     * @param length 截取长度
     * @return 纯文本内容
     */
    @JvmStatic
    fun subParseHtml(inputString: String?, length: Int): String? {
        if (inputString == null) {
            return null
        }
        val subHtml = parseHtml(inputString)
        return StringUtil.subString(subHtml, length)// 返回文本字符串
    }

    /**
     * 截取纯文本内容
     *
     * @param inputString 输入HTML内容
     * @param length 截取长度
     * @return 纯文本内容
     */
    @JvmStatic
    fun subParseHtmlRemoveBlank(inputString: String?, length: Int): String? {
        if (inputString == null) {
            return null
        }
        val subHtml = parseHtmlRemoveBlank(inputString)
        return StringUtil.subString(subHtml, length)// 返回文本字符串
    }

    /**
     * 截取纯文本内容
     *
     * @param inputString 输入HTML内容
     * @param length 截取长度
     * @return 纯文本内容
     */
    @JvmStatic
    fun subParseHtmlWithEllipsis(inputString: String?, length: Int): String? {
        if (inputString == null) {
            return null
        }
        val subHtml = parseHtmlRemoveBlank(inputString)
        return StringUtil.subStringWithEllipsis(subHtml, length)// 返回文本字符串
    }

    /**
     * @param inputString 输入HTML内容
     * @return 纯文本内容
     */
    @JvmStatic
    fun parseHtml(inputString: String?): String? {
        if (inputString == null) {
            return null
        }
        return try {
            // html过滤
            val htmlscript = HTMLStripCharFilter(StringReader(inputString))
            htmlscript.readText()
        } catch (e: IOException) {
            inputString
        }

    }

    /**
     * @param inputString 输入HTML内容
     * @return 去除空白内容的纯文本内容
     */
    @JvmStatic
    fun parseHtmlRemoveBlank(inputString: String?): String? {
        if (inputString == null) {
            return null
        }
        return try {
            // html过滤
            val htmlscript = HTMLStripCharFilter(StringReader(inputString))
            //增加映射过滤  主要过滤掉换行符
            val builder = NormalizeCharMap.Builder()
            builder.add("\r", "")//回车
            builder.add("\t", "")//横向跳格
            builder.add("\n", "")//换行
            builder.add(" ", "")//空白
            val cs = MappingCharFilter(builder.build(), htmlscript)
            cs.readText()
        } catch (e: IOException) {
            inputString
        }

    }
}

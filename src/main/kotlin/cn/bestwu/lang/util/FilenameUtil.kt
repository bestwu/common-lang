package cn.bestwu.lang.util

import java.io.File

/**
 * 文件名工具
 *
 * @author Peter Wu
 */
object FilenameUtil {

    fun getExtension(file: File): String {
        return file.extension
    }

    fun getNameWithoutExtension(file: File): String {
        return file.nameWithoutExtension
    }

    fun getExtension(fileName: String): String {
        return File(fileName).extension
    }

    fun getNameWithoutExtension(fileName: String): String {
        return File(fileName).nameWithoutExtension
    }
}

package cn.bestwu.lang.util

/**
 * Array 工具类
 *
 * @author Peter Wu
 */
object ArrayUtil {

    /**
     * @param array 数组
     * @param objectToFind 要查询的内容
     * @return 是否包含
     */
    fun contains(array: Array<Any>, objectToFind: Any): Boolean {
        return array.contains(objectToFind)
    }

    /**
     * @param array 数组
     * @param objectToFind 要查询的内容
     * @return 内容所在索引
     */
    fun indexOf(array: Array<Any>, objectToFind: Any): Int {
        return array.indexOf(objectToFind)
    }

    /**
     * 转换为数组
     *
     * @param items items
     * @param <T> T
     * @return 数组
    </T> */
    @SafeVarargs
    fun <T> toArray(vararg items: T): Array<out T> {
        return items
    }

    /**
     * @param array 数组
     * @return 是否不为空
     */
    fun isNotEmpty(array: Array<Any>): Boolean {
        return !isEmpty(array)
    }

    /**
     * @param array 数组
     * @return 是否为空
     */
    fun isEmpty(array: Array<Any>?): Boolean {
        return array == null || array.isEmpty()
    }

    /**
     * @param separator 分隔符
     * @param array 数组
     * @return toString
     */
    fun toString(separator: String, vararg array: Any): String {
        return array.joinToString(separator)
    }

    /**
     * @param array 数组
     * @return 默认 “,” 分隔的toString
     */
    fun toString(vararg array: Any): String {
        return toString(",", *array)
    }
}

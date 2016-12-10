package cn.bestwu.lang.util;

import java.util.Random;

/**
 * 随机工具类
 *
 * @author Peter Wu
 */
public class RandomUtil {
	private static final Random RANDOM = new Random();

	/**
	 * 随机数字字符串
	 *
	 * @param num 字符串长度
	 * @return 随机数字字符串
	 */
	public static String nextIntString(int num) {
		return nextString(num, "0123456789");
	}

	/**
	 * 随机字符串
	 *
	 * @param count 字符串长度
	 * @param chars 基本字符
	 * @return 随机字符串
	 */
	public static String nextString(int count, String chars) {
		if (chars == null) {
			return next(count, 0, 0, false, false, null, RANDOM);
		}
		return nextString(count, chars.toCharArray());
	}

	/**
	 * 随机字符串
	 *
	 * @param count 字符串长度
	 * @param chars 基本字符
	 * @return 随机字符串
	 */
	public static String nextString(int count, char[] chars) {
		if (chars == null) {
			return next(count, 0, 0, false, false, null, RANDOM);
		}
		return next(count, 0, chars.length, false, false, chars, RANDOM);
	}

	/**
	 * 随机字符串
	 *
	 * @param count   字符串长度
	 * @param start   起始索引
	 * @param end     结束索引
	 * @param letters 是否包含字母
	 * @param numbers 是否包含数字
	 * @param chars   随机字符范围
	 * @param random  随机
	 * @return 随机字符串
	 */
	public static String next(int count, int start, int end, boolean letters,
			boolean numbers, char[] chars, Random random) {
		if (count == 0) {
			return "";
		} else if (count < 0) {
			throw new IllegalArgumentException(
					"Requested next string length " + count
							+ " is less than 0.");
		}
		if ((start == 0) && (end == 0)) {
			end = 'z' + 1;
			start = ' ';
			if (!letters && !numbers) {
				start = 0;
				end = Integer.MAX_VALUE;
			}
		}

		char[] buffer = new char[count];
		int gap = end - start;

		while (count-- != 0) {
			char ch;
			if (chars == null) {
				ch = (char) (random.nextInt(gap) + start);
			} else {
				ch = chars[random.nextInt(gap) + start];
			}
			if ((letters && Character.isLetter(ch))
					|| (numbers && Character.isDigit(ch))
					|| (!letters && !numbers)) {
				if (ch >= 56320 && ch <= 57343) {
					if (count == 0) {
						count++;
					} else {
						// low surrogate, insert high surrogate after putting it
						// in
						buffer[count] = ch;
						count--;
						buffer[count] = (char) (55296 + random.nextInt(128));
					}
				} else if (ch >= 55296 && ch <= 56191) {
					if (count == 0) {
						count++;
					} else {
						// high surrogate, insert low surrogate before putting
						// it in
						buffer[count] = (char) (56320 + random.nextInt(128));
						count--;
						buffer[count] = ch;
					}
				} else if (ch >= 56192 && ch <= 56319) {
					// private high surrogate, no effing clue, so skip it
					count++;
				} else {
					buffer[count] = ch;
				}
			} else {
				count++;
			}
		}
		return new String(buffer);
	}

	/**
	 * 随机数字
	 *
	 * @param num 长度
	 * @return 随机数字
	 */
	public static int nextInt(int num) {
		return Integer.parseInt(nextIntString(num));
	}

	/**
	 * 随机字符串
	 *
	 * @param num 长度
	 * @return 随机字符串
	 */
	public static String nextString(int num) {
		return nextString(num, "abcdefghigklmnopqrstuvwxyz");
	}

	/**
	 * 随机字符串
	 *
	 * @param num 长度
	 * @return 随机字符串
	 */
	public static String nextString2(int num) {
		return nextString(num, "abcdefghigklmnopqrstuvwxyzABCDEFGHIGKLMNOPQRSTUVWXYZ0123456789");
	}
}

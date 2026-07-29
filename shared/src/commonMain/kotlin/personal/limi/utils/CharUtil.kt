package personal.limi.utils

/**
 * 检查字符是否是标准的 ASCII 字符 (编码在 0 到 127 之间)。
 */
fun Char.isAscii(): Boolean = this.code in 0..127

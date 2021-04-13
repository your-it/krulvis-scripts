package org.powbot.krulvis.api.utils

import kotlin.experimental.and
import kotlin.experimental.or

object Base64 {
    // Table for Base64 encoding
    private val base64_code = charArrayOf(
        '.', '/', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
        'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i',
        'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4',
        '5', '6', '7', '8', '9'
    )

    // Table for Base64 decoding
    private val index_64 = byteArrayOf(
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0, 1,
        54, 55, 56, 57, 58, 59, 60, 61, 62, 63, -1, -1, -1, -1, -1, -1, -1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14,
        15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, -1, -1, -1, -1, -1, -1, 28, 29, 30, 31, 32, 33, 34, 35, 36,
        37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, -1, -1, -1, -1, -1
    )

    /**
     * Look up the 3 bits base64-encoded by the specified character,
     * range-checking against conversion table
     *
     * @param x
     * the base64-encoded value
     * @return the decoded value of x
     */
    private fun char64(x: Char): Byte {
        return if (x.toInt() < 0 || x.toInt() > index_64.size) -1 else index_64[x.toInt()]
    }

    @Throws(IllegalArgumentException::class)
    fun encode(bytes: ByteArray): String = encode(bytes, bytes.size)

    /**
     * Encode a byte array using bcrypt's slightly-modified base64 encoding
     * scheme. Note that this is *not* compatible with the standard MIME-base64
     * encoding.
     *
     * @param d
     * the byte array to encode
     * @param len
     * the number of bytes to encode
     * @return base64-encoded string
     * @exception IllegalArgumentException
     * if the length is invalid
     */
    @Throws(IllegalArgumentException::class)
    fun encode(d: ByteArray, len: Int): String {
        var off = 0
        val rs = StringBuffer()
        var c1: Int
        var c2: Int
        require(!(len <= 0 || len > d.size)) { "Invalid len" }
        while (off < len) {
            c1 = d[off++].toInt() and 0xff
            rs.append(base64_code[c1 shr 2 and 0x3f])
            c1 = c1 and 0x03 shl 4
            if (off >= len) {
                rs.append(base64_code[c1 and 0x3f])
                break
            }
            c2 = d[off++].toInt() and 0xff
            c1 = c1 or (c2 shr 4 and 0x0f)
            rs.append(base64_code[c1 and 0x3f])
            c1 = c2 and 0x0f shl 2
            if (off >= len) {
                rs.append(base64_code[c1 and 0x3f])
                break
            }
            c2 = d[off++].toInt() and 0xff
            c1 = c1 or (c2 shr 6 and 0x03)
            rs.append(base64_code[c1 and 0x3f])
            rs.append(base64_code[c2 and 0x3f])
        }
        return rs.toString()
    }

    /**
     * Decode a string encoded using bcrypt's base64 scheme to a byte array.
     * Note that this is *not* compatible with the standard MIME-base64
     * encoding.
     *
     * @param s
     * the string to decode
     * @param maxolen
     * the maximum number of bytes to decode
     * @return an array containing the decoded bytes
     * @throws IllegalArgumentException
     * if maxolen is invalid
     */
    @Throws(IllegalArgumentException::class)
    fun decode(s: String, maxolen: Int): ByteArray {
        val rs = StringBuffer()
        var off = 0
        val slen = s.length
        var olen = 0
        val ret: ByteArray
        var c1: Byte
        var c2: Byte
        var c3: Byte
        var c4: Byte
        var o: Byte
        require(maxolen > 0) { "Invalid maxolen" }
        while (off < slen - 1 && olen < maxolen) {
            c1 = char64(s[off++])
            c2 = char64(s[off++])
            if (c1.toInt() == -1 || c2.toInt() == -1) break
            o = (c1.toInt() shl 2).toByte()
            o = o or ((c2 and 0x30).toInt() shr 4).toByte()
            rs.append(o.toChar())
            if (++olen >= maxolen || off >= slen) break
            c3 = char64(s[off++])
            if (c3.toInt() == -1) break
            o = (c2.toInt() and 0x0f shl 4).toByte()
            o = o or (c3.toInt() and 0x3c shr 2).toByte()
            rs.append(o.toChar())
            if (++olen >= maxolen || off >= slen) break
            c4 = char64(s[off++])
            o = (c3.toInt() and 0x03 shl 6).toByte()
            o = o or c4
            rs.append(o.toChar())
            ++olen
        }
        ret = ByteArray(olen)
        off = 0
        while (off < olen) {
            ret[off] = rs[off].toByte()
            off++
        }
        return ret
    }

    fun decode(base64: String): String {
        val bytes = decode(base64, base64.length)
        return String(bytes)
    }
}
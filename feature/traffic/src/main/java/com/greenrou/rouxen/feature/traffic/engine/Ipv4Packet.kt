package com.greenrou.rouxen.feature.traffic.engine

import java.net.InetAddress

class Ipv4Packet(private val raw: ByteArray) {

    val version: Int get() = (raw[0].toInt() shr 4) and 0x0F
    val ihl: Int get() = raw[0].toInt() and 0x0F
    val headerLength: Int get() = ihl * 4
    val totalLength: Int get() = readUInt16(2)
    val protocol: Int get() = raw[9].toInt() and 0xFF
    val sourceAddress: InetAddress get() = addressAt(12)
    val destinationAddress: InetAddress get() = addressAt(16)
    val payloadOffset: Int get() = headerLength
    val payloadLength: Int get() = totalLength - headerLength

    fun payload(): ByteArray = raw.copyOfRange(payloadOffset, totalLength)

    fun buildResponse(payload: ByteArray): ByteArray =
        buildPacket(destinationAddress, sourceAddress, protocol, payload)

    private fun readUInt16(offset: Int): Int =
        ((raw[offset].toInt() and 0xFF) shl 8) or (raw[offset + 1].toInt() and 0xFF)

    private fun addressAt(offset: Int): InetAddress =
        InetAddress.getByAddress(raw.copyOfRange(offset, offset + 4))

    companion object {
        const val PROTOCOL_TCP = 6
        const val PROTOCOL_UDP = 17
        const val HEADER_LENGTH = 20
        private const val DEFAULT_TTL: Byte = 64

        fun isIpv4(raw: ByteArray): Boolean =
            raw.isNotEmpty() && (raw[0].toInt() shr 4) and 0x0F == 4

        fun buildPacket(
            sourceAddress: InetAddress,
            destinationAddress: InetAddress,
            protocol: Int,
            payload: ByteArray,
        ): ByteArray {
            val total = HEADER_LENGTH + payload.size
            val out = ByteArray(total)
            out[0] = 0x45
            out[1] = 0
            writeUInt16(out, 2, total)
            out[4] = 0
            out[5] = 0
            out[6] = 0x40
            out[7] = 0
            out[8] = DEFAULT_TTL
            out[9] = protocol.toByte()
            out[10] = 0
            out[11] = 0
            System.arraycopy(sourceAddress.address, 0, out, 12, 4)
            System.arraycopy(destinationAddress.address, 0, out, 16, 4)
            System.arraycopy(payload, 0, out, HEADER_LENGTH, payload.size)

            val checksum = checksum(out, 0, HEADER_LENGTH)
            writeUInt16(out, 10, checksum)
            return out
        }

        fun writeUInt16(buffer: ByteArray, offset: Int, value: Int) {
            buffer[offset] = (value shr 8).toByte()
            buffer[offset + 1] = value.toByte()
        }

        fun checksum(data: ByteArray, offset: Int, length: Int): Int =
            foldChecksum(sumWords(data, offset, length))

        fun sumWords(data: ByteArray, offset: Int, length: Int): Long {
            var sum = 0L
            var i = offset
            val end = offset + length
            while (i + 1 < end) {
                sum += ((data[i].toInt() and 0xFF) shl 8) or (data[i + 1].toInt() and 0xFF)
                i += 2
            }
            if (i < end) {
                sum += (data[i].toInt() and 0xFF) shl 8
            }
            return sum
        }

        fun foldChecksum(sum: Long): Int {
            var s = sum
            while (s ushr 16 != 0L) {
                s = (s and 0xFFFF) + (s ushr 16)
            }
            return (s.inv() and 0xFFFF).toInt()
        }
    }
}

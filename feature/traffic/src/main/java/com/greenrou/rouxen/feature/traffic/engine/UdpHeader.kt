package com.greenrou.rouxen.feature.traffic.engine

class UdpHeader(private val raw: ByteArray) {

    val sourcePort: Int get() = readUInt16(0)
    val destinationPort: Int get() = readUInt16(2)
    val length: Int get() = readUInt16(4)
    val checksum: Int get() = readUInt16(6)

    fun data(): ByteArray = raw.copyOfRange(HEADER_LENGTH, raw.size)

    private fun readUInt16(offset: Int): Int =
        ((raw[offset].toInt() and 0xFF) shl 8) or (raw[offset + 1].toInt() and 0xFF)

    companion object {
        const val HEADER_LENGTH = 8

        fun build(sourcePort: Int, destinationPort: Int, data: ByteArray): ByteArray {
            val total = HEADER_LENGTH + data.size
            val out = ByteArray(total)
            Ipv4Packet.writeUInt16(out, 0, sourcePort)
            Ipv4Packet.writeUInt16(out, 2, destinationPort)
            Ipv4Packet.writeUInt16(out, 4, total)
            System.arraycopy(data, 0, out, HEADER_LENGTH, data.size)
            return out
        }
    }
}

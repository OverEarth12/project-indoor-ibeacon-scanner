package org.altbeacon.beaconreference

class fieldrssi(val fieldid: Int,
                val positionx: Int,
                val positiony: Int,
                val roomid: String,
                val rssi1: Int,
                val rssi2: Int,
                val rssi3: Int) {
    override fun toString(): String {
        return "PosX: $positionx, PosY: $positiony\n Scanner1: $rssi1 Scanner2: $rssi2 Scanner3: $rssi3"
    }
}
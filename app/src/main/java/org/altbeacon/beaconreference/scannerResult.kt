package org.altbeacon.beaconreference;

class scannerResult {
    var scannerid: String
        get() = field.capitalize()
        set(value){
            field = value.trim()
        }
    var roomid: String
        get()= field.capitalize()
        set(value){
            field = value.trim()
        }
    var beaconList: HashMap<String, Int> = HashMap<String, Int>()

    constructor(scannerid: String, roomid: String){
        this.scannerid = scannerid
        this.roomid = roomid
    }

    fun addBeacon(id: String, rssi: Int){
        beaconList[id] = rssi;
    }
}

package org.altbeacon.beaconreference

import com.google.gson.annotations.SerializedName

class beaconposition (
    val roomid: String,
    val scannerid: String,
    val rssi: Int,
    val uuid: String){

}
package org.altbeacon.beaconreference

import android.text.Editable
import androidx.lifecycle.Observer
import org.altbeacon.beacon.Beacon

class Position(val roomId: String, val scannerId: String, val rssi: Observer<Beacon>, val pos: List<Editable>) {

}
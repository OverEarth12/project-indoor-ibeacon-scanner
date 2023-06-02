package org.altbeacon.beaconreference

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main2.*
import okhttp3.*
import org.altbeacon.beacon.Beacon
import org.altbeacon.beacon.BeaconManager
import org.altbeacon.beacon.MonitorNotifier
import java.io.IOException
import java.net.URL

class MainActivity2 : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener, BeaconFound.OnButtonClickListener,
    BeaconFound.SelectBeaconListener, BeaconRegister.RegisterListener, beaconLogout.logoutListener,recordRadioMap.SentRssiListener
    ,Online.SendBeaconListener{
    lateinit var beaconReferenceApplication: BeaconReferenceApplication
    var alertDialog: AlertDialog? = null
    var neverAskAgainPermissions = ArrayList<String>()
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var container: FrameLayout
    private lateinit var controller: FrameLayout
    var selectedBeaconuuid: String = ""
    var selectedBeaconrssi: Int = 0
    var onlineStatus: Boolean = false
    var beaconsList = mutableListOf<String>()
    val beaconsLists = mutableMapOf<String, Int>()
    val averageBeacon = mutableMapOf<String, ArrayList<Int>>()
    private var updatetimes: Int = 0
    private var rssi: Int = 0

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        beaconReferenceApplication = application as BeaconReferenceApplication

        // Set up a Live Data observer for beacon data
        val regionViewModel = BeaconManager.getInstanceForApplication(this)
            .getRegionViewModel(beaconReferenceApplication.region)
        // observer will be called each time the monitored regionState changes (inside vs. outside region)
        regionViewModel.regionState.observe(this, monitoringObserver)
        // observer will be called each time a new list of beacons is ranged (typically ~1 second in the foreground)
        regionViewModel.rangedBeacons.observe(this, rangingObserver)
//        rangingButton = findViewById<Button>(R.id.rangingButton)
//        monitoringButton = findViewById<Button>(R.id.monitoringButton)
//        beaconListView = findViewById<ListView>(R.id.beaconList)
//        beaconCountTextView = findViewById<TextView>(R.id.beaconCount)
//        beaconCountTextView.text = "No beacons detected"
        container = findViewById(R.id.container)
        controller = findViewById(R.id.controller)
        bottomNavigationView = findViewById(R.id.bottom_navigation)

        bottomNavigationView.setOnItemSelectedListener(this);
        val containerfragment = BeaconFound();
        supportFragmentManager.beginTransaction().replace(R.id.container, containerfragment).commit()
        val controllerfragment = recordRadioMap();
        supportFragmentManager.beginTransaction().replace(R.id.controller, controllerfragment).commit()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Reset colors for all items
        for (i in 0 until bottomNavigationView.menu.size()) {
            val menuItem = bottomNavigationView.menu.getItem(i)
            menuItem.iconTintList = ContextCompat.getColorStateList(this, R.color.cardview_dark_background)
        }

        // Change color for the selected item
        println("Test")
        item.iconTintList = ContextCompat.getColorStateList(this, R.color.cardview_dark_background)
        item.title = item.title.toString()

        // Handle navigation logic for each item
        when (item.itemId) {
            R.id.main -> replaceController(recordRadioMap())
            R.id.online -> replaceController(Online())
            R.id.register -> replaceController(BeaconRegister())
            R.id.logout -> replaceController(beaconLogout())
            R.id.radiomap -> replaceController(RadioMapFragment())
        }
        return true
    }

    fun replaceController(fragment: Fragment){
        val fragmentManager = supportFragmentManager
        val framentTransaction = fragmentManager.beginTransaction()
        framentTransaction.replace(R.id.controller, fragment)
        framentTransaction.commit()
    }

//    override fun onRangeButtonClick(buttonId: Int){
//        when (buttonId) {
//            R.id.rangingButton -> {
//                // Handle button 1 click
//                println("Pressed rangingButton")
//            }
//            R.id.monitoringButton -> {
//                // Handle button 2 click
//                println("Pressed monitoringButton")
//            }
//            // Add more cases for other buttons if needed
//        }
//    }

    override fun toggleRanging() {
        val fragment = supportFragmentManager.findFragmentById(R.id.container) as? BeaconFound
        val beaconManager = BeaconManager.getInstanceForApplication(this)
        if (beaconManager.rangedRegions.size == 0) {
            beaconManager.startRangingBeacons(beaconReferenceApplication.region)
            fragment?.updaterangingButton(true);
//            rangingButton.text = "Stop Ranging"
//            beaconCountTextView.text = "Ranging enabled -- awaiting first callback"
        }
        else {
            beaconManager.stopRangingBeacons(beaconReferenceApplication.region)
            fragment?.updaterangingButton(false);
//            rangingButton.text = "Start Ranging"
//            beaconCountTextView.text = "Ranging disabled -- no beacons detected"
//            beaconListView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, arrayOf("--"))
        }
    }

    override fun toggleMonitoring() {
        val fragment = supportFragmentManager.findFragmentById(R.id.container) as? BeaconFound
        var dialogTitle = ""
        var dialogMessage = ""
        val beaconManager = BeaconManager.getInstanceForApplication(this)
        if (beaconManager.monitoredRegions.size == 0) {
            beaconManager.startMonitoring(beaconReferenceApplication.region)
            dialogTitle = "Beacon monitoring started."
            dialogMessage = "You will see a dialog if a beacon is detected, and another if beacons then stop being detected."
//            monitoringButton.text = "Stop Monitoring"
            fragment?.updatemonitoringButton(true)
        }
        else {
            beaconManager.stopMonitoring(beaconReferenceApplication.region)
            dialogTitle = "Beacon monitoring stopped."
            dialogMessage = "You will no longer see dialogs when becaons start/stop being detected."
//            monitoringButton.text = "Start Monitoring"
            fragment?.updatemonitoringButton(false)
        }
        val builder =
            AlertDialog.Builder(this)
        builder.setTitle(dialogTitle)
        builder.setMessage(dialogMessage)
        builder.setPositiveButton(android.R.string.ok, null)
        alertDialog?.dismiss()
        alertDialog = builder.create()
        alertDialog?.show()
    }

    override fun onPause() {
        Log.d(MainActivity.TAG, "onPause")
        super.onPause()
    }
    override fun onResume() {
        Log.d(MainActivity.TAG, "onResume")
        super.onResume()
        checkPermissions()
    }

    override fun getSelectedBeacon(data: String, rssi: Int){
        selectedBeaconuuid = data
        selectedBeaconrssi = rssi
    }

    fun getScannerId(): String?{
        val fragment = supportFragmentManager.findFragmentById(R.id.container) as? BeaconFound
        return fragment?.getScannerId()
    }

    fun getRoomId(): String?{
        val fragment = supportFragmentManager.findFragmentById(R.id.controller) as? Online
        return fragment?.getroomid()
    }

    fun sentBeacons(){
//        val foundBeacons = mutableListOf<beaconposition>()
        val result = getRoomId()?.let { getScannerId()?.let { it1 -> scannerResult(it1, it) } }
//        val room = roomId.text.toString()

        if (result != null) {
            if(result.roomid == ""){
                println("Roomid is empty")
            }else{
                for (element in beaconsLists){
    //                val foundbeacon = beaconposition(
    //                    roomid = room,
    //                    scannerid = this.scanner,
    //                    rssi = averageBeacon.getValue(element.key).sum()/averageBeacon.getValue(element.key).size,
    //                    uuid = element.key
    //                )
    //                foundBeacons.add(foundbeacon)
    //                val sortarray = averageBeacon[element.key]
    //                sortarray?.sort()
    //                if (sortarray != null) {
    //                    averageBeacon[element.key] = sortarray
    //                }
    //
    //                var median = (averageBeacon.getValue(element.key).get(4)+averageBeacon.getValue(element.key).get(5)
    //                        +averageBeacon.getValue(element.key).get(6)+averageBeacon.getValue(element.key).get(7))/4
                    result.addBeacon(element.key, averageBeacon.getValue(element.key).sum()/averageBeacon.getValue(element.key).size)
                    println("avg+ "+averageBeacon.getValue(element.key).sum())
    //                println("median"+ averageBeacon.values)
    //                result.addBeacon(element.key, median)

                }
                val json = Gson().toJson(result)
                println("Test1 "+json)
                val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json)
                val request = Request.Builder()
                    .url(URL("http://trackingposition.us-east-1.elasticbeanstalk.com:8080/saveBeacons"))
                    .post(body)
                    .build()
                val okHttpClient = OkHttpClient()
                okHttpClient.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        // Handle this
                        println("SentTEST " + e)
                    }

                    override fun onResponse(call: Call, response: Response) {
                        // Handle this
                        println("SentTEST " + response)
                    }
                })
            }
        }

    }

    fun updateAverage(){
        for ((key,item) in beaconsLists){
            if(key in averageBeacon.keys){
                var arrayinmap = averageBeacon.getValue(key)
                if(averageBeacon.getValue(key).size >= 10){
                    arrayinmap.removeAt(0)
                }
                arrayinmap.add(item)
                averageBeacon.set(key, arrayinmap)
            }else{
                averageBeacon.set(key, arrayListOf(item))
            }
            println(averageBeacon.getValue(key).toString())
            var size1 = averageBeacon.getValue(key).size
            var toprint = averageBeacon.getValue(key).sum()/size1
            println("$toprint and $size1")
        }
        updatetimes++;
        if(updatetimes >= 10 && onlineStatus){
            sentBeacons()
            updatetimes = 0;
        }
    }

    val monitoringObserver = Observer<Int> { state ->
        var dialogTitle = "Beacons detected"
        var dialogMessage = "didEnterRegionEvent has fired"
        var stateString = "inside"
        if (state == MonitorNotifier.OUTSIDE) {
            dialogTitle = "No beacons detected"
            dialogMessage = "didExitRegionEvent has fired"
            stateString == "outside"
//            beaconCountTextView.text = "Outside of the beacon region -- no beacons detected"
//            beaconListView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, arrayOf("--"))
        }
        else {
//            beaconCountTextView.text = "Inside the beacon region."
        }
        Log.d(MainActivity.TAG, "monitoring state changed to : $stateString")
        val builder =
            AlertDialog.Builder(this)
        builder.setTitle(dialogTitle)
        builder.setMessage(dialogMessage)
        builder.setPositiveButton(android.R.string.ok, null)
        alertDialog?.dismiss()
        alertDialog = builder.create()
        alertDialog?.show()
    }

    val rangingObserver = Observer<Collection<Beacon>> { beacons ->
        Log.d(MainActivity.TAG, "Ranged: ${beacons.count()} beacons")

        for (beacon: Beacon in beacons) {
//            Log.d(TAG, "$beacon about ${beacon.rssi} dB")
            this.rssi = "${beacon.rssi}".toInt()
            beaconsLists[beacon.id1.toString()] = beacon.rssi
        }
////         Log.d(TAG, "TestResult: "+beaconsLists)
        updateAverage()

        if (BeaconManager.getInstanceForApplication(this).rangedRegions.size > 0) {
            val found = beacons
                .sortedBy { it.id1 }
                .map { "${it.id1}\nid2: ${it.id2} id3:  rssi: ${it.rssi}\nest. distance: ${it.distance} m" }.toTypedArray()
            val foundList = beacons.map{it.id1}
            val notnulllist : List<String> = foundList.mapNotNull {  identifier ->
                identifier?.toString() }
            val fragment = supportFragmentManager.findFragmentById(R.id.container) as? BeaconFound
            fragment?.updateBeaconList(found)
            beaconsList = notnulllist.toMutableList()
//            fragment?.updateBeaconDropdown(notnulllist)

//            beaconCountTextView.text = "Ranging enabled: ${beacons.count()} beacon(s) detected"
//            beaconListView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1,
//                beacons
//                    .sortedBy { it.id1 }
//                    .map { "${it.id1}\nid2: ${it.id2} id3:  rssi: ${it.rssi}\nest. distance: ${it.distance} m" }.toTypedArray())
//            val toSentBeacon = findViewById<Spinner>(R.id.toSentBeacon)
//            val toSentBeaconAdapter = toSentBeacon.adapter as ArrayAdapter<String>
//            toSentBeaconAdapter.clear()
//            toSentBeaconAdapter.addAll(beacons.map{it.id1.toString()})
//            toSentBeaconAdapter.notifyDataSetChanged()



//            toSentBeacon.adapter =
//                ArrayAdapter(this, android.R.layout.simple_spinner_item,
//                    beacons.map{it.id1})
        }
    }



    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        for (i in 1..permissions.size-1) {
            Log.d(MainActivity.TAG, "onRequestPermissionResult for "+permissions[i]+":" +grantResults[i])
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                //check if user select "never ask again" when denying any permission
                if (!shouldShowRequestPermissionRationale(permissions[i])) {
                    neverAskAgainPermissions.add(permissions[i])
                }
            }
        }
    }


    fun checkPermissions() {
//         basepermissions are for M and higher
        var permissions = arrayOf( Manifest.permission.ACCESS_FINE_LOCATION)
        var permissionRationale ="This app needs fine location permission to detect beacons.  Please grant this now."
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions = arrayOf( Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.BLUETOOTH_SCAN)
            permissionRationale ="This app needs fine location permission, and bluetooth scan permission to detect beacons.  Please grant all of these now."
        }
        else if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if ((checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
                permissions = arrayOf( Manifest.permission.ACCESS_FINE_LOCATION)
                permissionRationale ="This app needs fine location permission to detect beacons.  Please grant this now."
            }
            else {
                permissions = arrayOf( Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                permissionRationale ="This app needs background location permission to detect beacons in the background.  Please grant this now."
            }
        }
        else if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissions = arrayOf( Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            permissionRationale ="This app needs both fine location permission and background location permission to detect beacons in the background.  Please grant both now."
        }
        var allGranted = true
        for (permission in permissions) {
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) allGranted = false;
        }
        if (!allGranted) {
            if (neverAskAgainPermissions.count() == 0) {
                val builder =
                    AlertDialog.Builder(this)
                builder.setTitle("This app needs permissions to detect beacons")
                builder.setMessage(permissionRationale)
                builder.setPositiveButton(android.R.string.ok, null)
                builder.setOnDismissListener {
                    requestPermissions(
                        permissions,
                        MainActivity.PERMISSION_REQUEST_FINE_LOCATION
                    )
                }
                builder.show()
            }
            else {
                val builder =
                    AlertDialog.Builder(this)
                builder.setTitle("Functionality limited")
                builder.setMessage("Since location and device permissions have not been granted, this app will not be able to discover beacons.  Please go to Settings -> Applications -> Permissions and grant location and device discovery permissions to this app.")
                builder.setPositiveButton(android.R.string.ok, null)
                builder.setOnDismissListener { }
                builder.show()
            }
        }
        else {
            if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                if (checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                        val builder =
                            AlertDialog.Builder(this)
                        builder.setTitle("This app needs background location access")
                        builder.setMessage("Please grant location access so this app can detect beacons in the background.")
                        builder.setPositiveButton(android.R.string.ok, null)
                        builder.setOnDismissListener {
                            requestPermissions(
                                arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                                MainActivity.PERMISSION_REQUEST_BACKGROUND_LOCATION
                            )
                        }
                        builder.show()
                    } else {
                        val builder =
                            AlertDialog.Builder(this)
                        builder.setTitle("Functionality limited")
                        builder.setMessage("Since background location access has not been granted, this app will not be able to discover beacons in the background.  Please go to Settings -> Applications -> Permissions and grant background location access to this app.")
                        builder.setPositiveButton(android.R.string.ok, null)
                        builder.setOnDismissListener { }
                        builder.show()
                    }
                }
            }
            else if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.S &&
                (checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN)
                        != PackageManager.PERMISSION_GRANTED)) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.BLUETOOTH_SCAN)) {
                    val builder =
                        AlertDialog.Builder(this)
                    builder.setTitle("This app needs bluetooth scan permission")
                    builder.setMessage("Please grant scan permission so this app can detect beacons.")
                    builder.setPositiveButton(android.R.string.ok, null)
                    builder.setOnDismissListener {
                        requestPermissions(
                            arrayOf(Manifest.permission.BLUETOOTH_SCAN),
                            MainActivity.PERMISSION_REQUEST_BLUETOOTH_SCAN
                        )
                    }
                    builder.show()
                } else {
                    val builder =
                        AlertDialog.Builder(this)
                    builder.setTitle("Functionality limited")
                    builder.setMessage("Since bluetooth scan permission has not been granted, this app will not be able to discover beacons  Please go to Settings -> Applications -> Permissions and grant bluetooth scan permission to this app.")
                    builder.setPositiveButton(android.R.string.ok, null)
                    builder.setOnDismissListener { }
                    builder.show()
                }
            }
            else {
                if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                    if (checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                        != PackageManager.PERMISSION_GRANTED
                    ) {
                        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                            val builder =
                                AlertDialog.Builder(this)
                            builder.setTitle("This app needs background location access")
                            builder.setMessage("Please grant location access so this app can detect beacons in the background.")
                            builder.setPositiveButton(android.R.string.ok, null)
                            builder.setOnDismissListener {
                                requestPermissions(
                                    arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                                    MainActivity.PERMISSION_REQUEST_BACKGROUND_LOCATION
                                )
                            }
                            builder.show()
                        } else {
                            val builder =
                                AlertDialog.Builder(this)
                            builder.setTitle("Functionality limited")
                            builder.setMessage("Since background location access has not been granted, this app will not be able to discover beacons in the background.  Please go to Settings -> Applications -> Permissions and grant background location access to this app.")
                            builder.setPositiveButton(android.R.string.ok, null)
                            builder.setOnDismissListener { }
                            builder.show()
                        }
                    }
                }
            }
        }

    }

    companion object {
        val TAG = "MainActivity"
        val PERMISSION_REQUEST_BACKGROUND_LOCATION = 0
        val PERMISSION_REQUEST_BLUETOOTH_SCAN = 1
        val PERMISSION_REQUEST_BLUETOOTH_CONNECT = 2
        val PERMISSION_REQUEST_FINE_LOCATION = 3
    }

    override fun getBeaconUuid(): String {
        return selectedBeaconuuid
    }

    override fun getUuidtologout(): String {
        return selectedBeaconuuid
    }

    override fun getRssi(): HashMap<String, String> {
        var data = hashMapOf<String, String>()
        data.put("uuid", selectedBeaconuuid)
        data.put("rssi", selectedBeaconrssi.toString())
        getScannerId()?.let { data.put("scanner", it) }
        return data
    }

    override fun toggleSendBeacon() {
        onlineStatus = !onlineStatus
    }


}
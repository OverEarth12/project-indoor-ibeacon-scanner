package org.altbeacon.beaconreference

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.*
import org.altbeacon.beacon.*
import org.altbeacon.beaconreference.databinding.ActivityMainBinding
import java.io.IOException
import java.net.URL

class MainActivity : AppCompatActivity() {
    lateinit var beaconListView: ListView
    lateinit var beaconCountTextView: TextView
    lateinit var monitoringButton: Button
    lateinit var rangingButton: Button
    lateinit var beaconReferenceApplication: BeaconReferenceApplication
    var alertDialog: AlertDialog? = null
    var neverAskAgainPermissions = ArrayList<String>()
    lateinit var textView2: TextView
    lateinit var textView3: TextView
    lateinit var textInputX: EditText
    lateinit var textInputY: EditText
    lateinit var Button1: Button
    lateinit var Button2: Button
    lateinit var RegButton: Button
    lateinit var roomId: EditText
    lateinit var scannerId: EditText
    lateinit var toSentBeacon: Spinner
    private var rssi: Int = 0
//    var idBeacon = ArrayList<String>()
    val beaconsLists = mutableMapOf<String, Int>()
    val averageBeacon = mutableMapOf<String, ArrayList<Int>>()
    private var updatetimes: Int = 0
    private var sentData: Boolean = false;
    private var scanner = "";

    private lateinit var binding: ActivityMainBinding

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomNavigationView.setOnNavigationItemReselectedListener {
            when(it.itemId){
                R.id.home -> {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                }
                R.id.off_Line -> replaceFragment(offLine())
                R.id.on_Line -> replaceFragment(onLine())
                R.id.Add_Persons -> replaceFragment(Persons())
            }
        }
//        setContentView(R.layout.activity_main)
        beaconReferenceApplication = application as BeaconReferenceApplication

        // Set up a Live Data observer for beacon data
        val regionViewModel = BeaconManager.getInstanceForApplication(this)
            .getRegionViewModel(beaconReferenceApplication.region)
        // observer will be called each time the monitored regionState changes (inside vs. outside region)
        regionViewModel.regionState.observe(this, monitoringObserver)
        // observer will be called each time a new list of beacons is ranged (typically ~1 second in the foreground)
        regionViewModel.rangedBeacons.observe(this, rangingObserver)
        rangingButton = findViewById<Button>(R.id.rangingButton)
        monitoringButton = findViewById<Button>(R.id.monitoringButton)
        beaconListView = findViewById<ListView>(R.id.beaconList)
        beaconCountTextView = findViewById<TextView>(R.id.beaconCount)
        beaconCountTextView.text = "No beacons detected"
        beaconListView.adapter =
            ArrayAdapter(this, android.R.layout.simple_list_item_1, arrayOf("--"))
        textView2 = findViewById(R.id.textView2)
        textView3 = findViewById(R.id.textView3)
        textInputX = findViewById(R.id.editTextNumberX)
        textInputY = findViewById(R.id.editTextNumberY)
        Button1 = findViewById(R.id.button1)
        Button2 = findViewById(R.id.button2)
        RegButton = findViewById(R.id.regButton)
        roomId = findViewById(R.id.roomId)
        scannerId = findViewById(R.id.scannerId)
        toSentBeacon = findViewById(R.id.toSentBeacon)
        toSentBeacon.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, arrayOf("--Select Beacon--"))
        switch1.setOnCheckedChangeListener { _ , isChecked ->
            Button1.isEnabled = !isChecked
            sentData = isChecked
        }
        switch1.isEnabled = false
        Button1.isEnabled = true
        scannerId.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                // This method is called after the text is changed.
                scanner = s.toString()
                switch1.isEnabled = scanner.length > 0
            }
        })
        button1.setOnClickListener {
            val textX = editTextNumberX.text.toString().toInt()
//            textInputX.inputType = InputType.TYPE_CLASS_NUMBER
//            textInputY.inputType = InputType.TYPE_CLASS_NUMBER
            val textY = editTextNumberY.text.toString().toInt()
//            val beaconHave = arrayListOf("00000000-0000-0000-0000-000000000011","00000000-0000-0000-0000-000000000100","00000000-0000-0000-0000-000000000001")
            val room = roomId.text.toString()

//            idBeacon.add(this.rssi.toString())
//            println("this is in String we got $idBeacon")
            println("This is your phone ${Build.BRAND}${Build.MODEL}")

            val json = Gson().toJson(
                RadioMap(
                    roomid = room,
                    scannerid = scanner,
                    rssi = beaconsLists.getValue(toSentBeacon.selectedItem.toString()),
                    uuid = toSentBeacon.selectedItem.toString()
                )
            )
            println(json+"");
            println("rssi sent:"+beaconsLists.getValue(toSentBeacon.selectedItem.toString()))
            val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json)
            val request = Request.Builder()
                .url(URL("http://trackingposition.us-east-1.elasticbeanstalk.com:8080/savepos/$textX/$textY"))
                .post(body)
                .build()
            val okHttpClient = OkHttpClient()
            okHttpClient.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    // Handle this
                    println("Test " + e)
                }

                override fun onResponse(call: Call, response: Response) {
                    // Handle this

                    println("Test " + response.body()?.string())
                }
            })

//            Toast.makeText(this, textX.toString(), Toast.LENGTH_SHORT).show()
//            Toast.makeText(this, textY.toString(), Toast.LENGTH_SHORT).show()
//            Toast.makeText(this, beaconNum, Toast.LENGTH_SHORT).show()

            println("hello world $rssi");

        }

        button2.setOnClickListener {
            sentBeacons();
        }

        RegButton.setOnClickListener{
            registerBeacon()
        }

    }

    private fun replaceFragment(fragment: Fragment){
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout,fragment)
        fragmentTransaction.commit()
    }

    override fun onPause() {
        Log.d(TAG, "onPause")
        super.onPause()
    }
    override fun onResume() {
        Log.d(TAG, "onResume")
        super.onResume()
        checkPermissions()
    }

    fun sentBeacons(){
//        val foundBeacons = mutableListOf<beaconposition>()
        val result = scannerResult(scanner, roomId.text.toString())
//        val room = roomId.text.toString()

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
                result.addBeacon(element.key, averageBeacon.getValue(element.key).sum()/averageBeacon.getValue(element.key).size)
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
        if(updatetimes >= 10 && sentData){
            sentBeacons()
            updatetimes = 0;
        }
    }

    fun registerBeacon(){
        var toregbeacon = toSentBeacon.selectedItem.toString()
        val json = Gson().toJson(regBeacon(
            uuid = toregbeacon,
            isactive = true
        ))
        val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json)
        val request = Request.Builder()
            .url(URL("http://trackingposition.us-east-1.elasticbeanstalk.com:8080/regbeacon"))
            .post(body)
            .build()
        val okHttpClient = OkHttpClient()
        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Handle this
                println("reged " + e)
            }

            override fun onResponse(call: Call, response: Response) {
                // Handle this
                println("reged " + response.body()?.string())
            }
        })
    }

    val monitoringObserver = Observer<Int> { state ->
        var dialogTitle = "Beacons detected"
        var dialogMessage = "didEnterRegionEvent has fired"
        var stateString = "inside"
        if (state == MonitorNotifier.OUTSIDE) {
            dialogTitle = "No beacons detected"
            dialogMessage = "didExitRegionEvent has fired"
            stateString == "outside"
            beaconCountTextView.text = "Outside of the beacon region -- no beacons detected"
            beaconListView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, arrayOf("--"))
        }
        else {
            beaconCountTextView.text = "Inside the beacon region."
        }
        Log.d(TAG, "monitoring state changed to : $stateString")
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
        Log.d(TAG, "Ranged: ${beacons.count()} beacons")
//         var testSub = "$beacons"
//         testSub = testSub.split(',')
//         Log.d(TAG, "this is wat we got $beacons")
//         this.beaconGot.add("$beacons")
//         Log.d(TAG, "This is what we got from this ${beacons::class.simpleName} ")
//         beacons.forEach {
//             println("The fuk is this ${it.id1}" +" Rssi : ${it.rssi}")
//             beaconGot.addAll(listOf("id1: ${it.id1}" + " rssi: ${it.rssi}"))
//             beaconGot = beacons.map {key=it.id1"${it.id1}${it.rssi}" }
//         }

         for (beacon: Beacon in beacons) {
//            Log.d(TAG, "$beacon about ${beacon.rssi} dB")
            this.rssi = "${beacon.rssi}".toInt()
             beaconsLists[beacon.id1.toString()] = beacon.rssi
         }
//         Log.d(TAG, "TestResult: "+beaconsLists)
         updateAverage()

        if (BeaconManager.getInstanceForApplication(this).rangedRegions.size > 0) {
            beaconCountTextView.text = "Ranging enabled: ${beacons.count()} beacon(s) detected"
            beaconListView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1,
                beacons
                    .sortedBy { it.id1 }
                    .map { "${it.id1}\nid2: ${it.id2} id3:  rssi: ${it.rssi}\nest. distance: ${it.distance} m" }.toTypedArray())
//            val toSentBeacon = findViewById<Spinner>(R.id.toSentBeacon)
//            val toSentBeaconAdapter = toSentBeacon.adapter as ArrayAdapter<String>
//            toSentBeaconAdapter.clear()
//            toSentBeaconAdapter.addAll(beacons.map{it.id1.toString()})
//            toSentBeaconAdapter.notifyDataSetChanged()



            toSentBeacon.adapter =
                ArrayAdapter(this, android.R.layout.simple_spinner_item,
                    beacons.map{it.id1})
        }
    }

    fun rangingButtonTapped(view: View) {
        val beaconManager = BeaconManager.getInstanceForApplication(this)
        if (beaconManager.rangedRegions.size == 0) {
            beaconManager.startRangingBeacons(beaconReferenceApplication.region)
            rangingButton.text = "Stop Ranging"
            beaconCountTextView.text = "Ranging enabled -- awaiting first callback"
        }
        else {
            beaconManager.stopRangingBeacons(beaconReferenceApplication.region)
            rangingButton.text = "Start Ranging"
            beaconCountTextView.text = "Ranging disabled -- no beacons detected"
            beaconListView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, arrayOf("--"))
        }
    }

    fun monitoringButtonTapped(view: View) {
        var dialogTitle = ""
        var dialogMessage = ""
        val beaconManager = BeaconManager.getInstanceForApplication(this)
        if (beaconManager.monitoredRegions.size == 0) {
            beaconManager.startMonitoring(beaconReferenceApplication.region)
            dialogTitle = "Beacon monitoring started."
            dialogMessage = "You will see a dialog if a beacon is detected, and another if beacons then stop being detected."
            monitoringButton.text = "Stop Monitoring"

        }
        else {
            beaconManager.stopMonitoring(beaconReferenceApplication.region)
            dialogTitle = "Beacon monitoring stopped."
            dialogMessage = "You will no longer see dialogs when becaons start/stop being detected."
            monitoringButton.text = "Start Monitoring"
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        for (i in 1..permissions.size-1) {
            Log.d(TAG, "onRequestPermissionResult for "+permissions[i]+":" +grantResults[i])
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
                        PERMISSION_REQUEST_FINE_LOCATION
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
                                PERMISSION_REQUEST_BACKGROUND_LOCATION
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
                            PERMISSION_REQUEST_BLUETOOTH_SCAN
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
                                    PERMISSION_REQUEST_BACKGROUND_LOCATION
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

}
//
//private operator fun Int.next(): Beacon {
//    return this.next()
//}
//
//private operator fun Int.hasNext(): Boolean {
//    return true
//}
//
//private operator fun Beacon.iterator(): Int {
//    return rssi
//}

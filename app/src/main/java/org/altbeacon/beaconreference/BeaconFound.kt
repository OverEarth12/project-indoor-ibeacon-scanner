package org.altbeacon.beaconreference

import android.app.AlertDialog
import android.content.Context
import android.content.res.TypedArray
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import org.altbeacon.beacon.BeaconManager
import org.w3c.dom.Text

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [BeaconFound.newInstance] factory method to
 * create an instance of this fragment.
 */
class BeaconFound : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var beaconList: ListView
    private lateinit var monitoringButton: Button
    private lateinit var rangingButton: Button
    private lateinit var beaconCount: TextView
    private lateinit var scannerId: EditText
    private lateinit var beaconsFound: TextView
    private var callback: OnButtonClickListener? = null
    private var SelectedBeaconData: SelectBeaconListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Check if the MainActivity implements the callback interface
        if (context is OnButtonClickListener) {
            callback = context
        } else {
            throw RuntimeException("$context must implement OnButtonClickListener")
        }

        if (context is SelectBeaconListener) {
            SelectedBeaconData = context
        } else {
            throw RuntimeException("$context must implement SelectBeaconListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        callback = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

    }

    interface OnButtonClickListener{
        fun toggleMonitoring()
        fun toggleRanging()
    }

    interface SelectBeaconListener {
        fun getSelectedBeacon(data: String, rssi: Int)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_beacon_found, container, false)
        beaconList = rootView.findViewById<ListView>(R.id.beaconList)
        monitoringButton = rootView.findViewById<Button>(R.id.monitoringButton)
        rangingButton = rootView.findViewById<Button>(R.id.rangingButton)
        beaconCount = rootView.findViewById<TextView>(R.id.beaconCount)
        scannerId = rootView.findViewById<EditText>(R.id.scannerId)
//        beaconsFound = rootView.findViewById<Spinner>(R.id.toSentBeacon)
        beaconsFound = rootView.findViewById<TextView>(R.id.toSentBeacon)
        beaconList.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, arrayOf("--"))
        monitoringButton.setOnClickListener{
            callback?.toggleMonitoring()
        }
        rangingButton.setOnClickListener{
            callback?.toggleRanging()
        }
        beaconList.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val selectedBeaconuuid = beaconList.adapter.getItem(position).toString()
            // Update the TextView below the ListView
            val uuid = selectedBeaconuuid.substring(0, selectedBeaconuuid.indexOf("\n"))
            var rssi = selectedBeaconuuid.substringAfter("rssi: ").substringBefore("\n").toInt()
            SelectedBeaconData?.getSelectedBeacon(uuid, rssi)
            beaconsFound.text = "Selected Beacon: $selectedBeaconuuid"
        }
//        beaconsFound.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
//                selectedPosition = position
//                selectedBeacon = beaconsFound.adapter.getItem(position).toString()
//                println(selectedBeacon)
//            }
//            override fun onNothingSelected(parent: AdapterView<*>) {
//                // Handle the case when no item is selected
//            }
//        }

        // Inflate the layout for this fragment
        return rootView
    }

    fun updateBeaconList( beacons : Array<String>){
        beaconList.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1,
            beacons
//                    .sortedBy { it.id1 }
//                    .map { "${it.id1}\nid2: ${it.id2} id3:  rssi: ${it.rssi}\nest. distance: ${it.distance} m" }.toTypedArray()
            )

    }

//    fun updateBeaconDropdown(beacons: List<String>){
//
//        beaconsFound.adapter =
//            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item,
//                beacons)
////        beaconsFound.setSelection(selectedPosition)
////        val toSentBeacon = beaconsFound
////        val toSentBeaconAdapter = toSentBeacon.adapter as ArrayAdapter<String>
////        toSentBeaconAdapter.clear()
////        toSentBeaconAdapter.addAll(beacons.toMutableList())
////        toSentBeaconAdapter.notifyDataSetChanged()
//    }

    fun updatemonitoringButton(status: Boolean){
        if(status){
            monitoringButton.text = "Stop Monitoring"
        }else{
            monitoringButton.text = "Start Monitoring"
        }
    }

    fun updaterangingButton(status: Boolean){
        if(status){
            rangingButton.text = "Stop Ranging"
            beaconCount.text = "Ranging enabled -- awaiting first callback"
        }else{
            rangingButton.text = "Start Ranging"
            beaconCount.text = "Ranging disabled -- no beacons detected"
            updateBeaconList(arrayOf("--"))
//            beaconListView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, arrayOf("--"))
        }

    }

    fun getScannerId(): String {
        return scannerId.text.toString()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment BeaconFound.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            BeaconFound().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
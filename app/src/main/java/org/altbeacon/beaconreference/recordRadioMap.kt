package org.altbeacon.beaconreference

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.*
import java.io.IOException
import java.net.URL

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [recordRadioMap.newInstance] factory method to
 * create an instance of this fragment.
 */
class recordRadioMap : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    lateinit var posX: EditText
    lateinit var posY: EditText
    lateinit var saveButton: Button
    lateinit var roomid: EditText
    lateinit var rssi: EditText
    lateinit var rssistatus: TextView
    private var toSentData: SentRssiListener? = null

    interface SentRssiListener {
        fun getRssi(): HashMap<String, String>
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Check if the MainActivity implements the callback interface
        if (context is SentRssiListener) {
            toSentData = context
        } else {
            throw RuntimeException("$context must implement OnButtonClickListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        toSentData = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_record_radio_map, container, false)
        // Inflate the layout for this fragment
        posX = rootView.findViewById<EditText>(R.id.editTextPosX)
        posY = rootView.findViewById<EditText>(R.id.editTextPosY)
        saveButton = rootView.findViewById<Button>(R.id.saveButton)
        roomid = rootView.findViewById<EditText>(R.id.roomId)
        rssi = rootView.findViewById<EditText>(R.id.editRssi)
        rssistatus = rootView.findViewById<TextView>(R.id.rssitouse)

        saveButton.setOnClickListener{
            val textX = posX.text.toString().toInt()
//            textInputX.inputType = InputType.TYPE_CLASS_NUMBER
//            textInputY.inputType = InputType.TYPE_CLASS_NUMBER
            val textY = posY.text.toString().toInt()
//            val beaconHave = arrayListOf("00000000-0000-0000-0000-000000000011","00000000-0000-0000-0000-000000000100","00000000-0000-0000-0000-000000000001")
            val room = roomid.text.toString()
            var data = toSentData?.getRssi()

            if(rssi.text.toString() != ""){
                data?.set("rssi", this.rssi.text.toString())
            }

            val json = Gson().toJson(
                data?.let { it1 ->
                    it1["rssi"]?.let { it2 ->
                        RadioMap(
                            roomid = room,
                            scannerid = it1["scanner"].toString(),
                            rssi = it2.toInt(),
                            uuid = it1["uuid"].toString()
                        )
                    }
                }
            )
            println("aaaa"+json)
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
        }
        rssi.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                if(s.toString() == ""){
                    rssistatus.text = "Save RSSI from Found Beacon"
                }else{
                    rssistatus.text = "Save your enter Caculated RSSI"
                }
            }
        })

        return rootView
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment recordRadioMap.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            recordRadioMap().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
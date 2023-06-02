package org.altbeacon.beaconreference

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import com.google.gson.Gson
import okhttp3.*
import java.io.IOException
import java.net.URL

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [RadioMapFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RadioMapFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    lateinit var roomid: EditText
    lateinit var viewButton: Button
    lateinit var radiomaplist: ListView

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
        // Inflate the layout for this fragment
        var rootView = inflater.inflate(R.layout.fragment_radio_map, container, false)
        radiomaplist = rootView.findViewById<ListView>(R.id.radiomapList)
        roomid = rootView.findViewById<EditText>(R.id.viewRoomid)
        viewButton = rootView.findViewById<Button>(R.id.viewButton)
        viewButton.setOnClickListener{
            var room = roomid.text.toString()
            val request = Request.Builder()
                .url(URL("http://trackingposition.us-east-1.elasticbeanstalk.com:8080/getsavedrssi/$room")).build()
            val okHttpClient = OkHttpClient()
            okHttpClient.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    // Handle this
                    println("Failed " + e)
                }

                override fun onResponse(call: Call, response: Response) {
                    // Handle this
                    val responseBodyString = response.body()?.string()
                    val gson = Gson()
                    val values: List<fieldrssi> = gson.fromJson(responseBodyString, Array<fieldrssi>::class.java).toList()
                    val displayvalues = values.map{it.toString()}.toTypedArray()
                    activity?.runOnUiThread{
                        radiomaplist.adapter = ArrayAdapter(
                            requireContext(),
                            android.R.layout.simple_list_item_1,
                            displayvalues
                        )
                    }
                }
            })
        }

        return rootView;
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment RadioMapFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            RadioMapFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
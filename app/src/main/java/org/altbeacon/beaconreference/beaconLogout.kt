package org.altbeacon.beaconreference

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
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
 * Use the [beaconLogout.newInstance] factory method to
 * create an instance of this fragment.
 */
class beaconLogout : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    lateinit var logoutButton: Button
    private var logoutLis: logoutListener? = null

    interface logoutListener {
        fun getUuidtologout(): String
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is logoutListener) {
            logoutLis = context
        } else {
            throw RuntimeException("$context must implement ButtonClickListener")
        }
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
        // Inflate the layout for this fragment
        val rootView =  inflater.inflate(R.layout.fragment_beacon_logout, container, false)
        logoutButton = rootView.findViewById<Button>(R.id.logoutButton)

        logoutButton.setOnClickListener{
//            logoutButton.text = logoutLis?.getUuidtologout()
            var uuid = logoutLis?.getUuidtologout()
            val request = Request.Builder()
                .url(URL("http://trackingposition.us-east-1.elasticbeanstalk.com:8080/userlogout/$uuid"))
                .post(RequestBody.create(null, ByteArray(0)))
                .build()
            val okHttpClient = OkHttpClient()
            okHttpClient.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    // Handle this
                    println("Failed " + e)
                }

                override fun onResponse(call: Call, response: Response) {
                    // Handle this
                    val responseData = response.body()?.string()
                    println("logouting:"+responseData)
                    activity?.runOnUiThread{
                        val alertDialogBuilder = AlertDialog.Builder(context)
                        alertDialogBuilder.setTitle("Alert Dialog")
                            .setMessage(responseData.toString())
                        val alertDialog = alertDialogBuilder.create()
                        alertDialog.show()
                    }

                }
            })
        }

        return rootView
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment beaconLogout.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            beaconLogout().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
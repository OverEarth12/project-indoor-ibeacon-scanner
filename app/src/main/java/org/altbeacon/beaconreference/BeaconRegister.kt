package org.altbeacon.beaconreference

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
 * Use the [BeaconRegister.newInstance] factory method to
 * create an instance of this fragment.
 */
class BeaconRegister : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    lateinit var registerButton: Button
    private var registerBeaconLis: RegisterListener? = null

    interface RegisterListener {
        fun getBeaconUuid(): String
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is RegisterListener) {
            registerBeaconLis = context
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
        val rootView = inflater.inflate(R.layout.fragment_beacon_register, container, false)
        registerButton = rootView.findViewById<Button>(R.id.RegisterButton)

        registerButton.setOnClickListener{
            registerBeacon(registerBeaconLis?.getBeaconUuid().toString())
        }

        return rootView;
    }

    fun registerBeacon(toregbeacon: String){
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

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment BeaconRegister.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            BeaconRegister().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
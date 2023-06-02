package org.altbeacon.beaconreference

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.widget.SwitchCompat

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Online.newInstance] factory method to
 * create an instance of this fragment.
 */
class Online : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    lateinit var roomId: EditText
    private var sendBeaconLis: SendBeaconListener? = null
    lateinit var switchSent: SwitchCompat

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Check if the MainActivity implements the callback interface
        if (context is SendBeaconListener) {
            sendBeaconLis = context
        } else {
            throw RuntimeException("$context must implement SendBeaconListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        sendBeaconLis = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }
    interface SendBeaconListener {
        fun toggleSendBeacon()
    }

    fun getroomid(): String{
        return roomId.text.toString()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_online, container, false)
        roomId = rootView.findViewById<EditText>(R.id.roomidsent)
        switchSent = rootView.findViewById<SwitchCompat>(R.id.switchSent)
        switchSent.setOnCheckedChangeListener{_ , isChecked ->
            sendBeaconLis?.toggleSendBeacon()
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
         * @return A new instance of fragment Online.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Online().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
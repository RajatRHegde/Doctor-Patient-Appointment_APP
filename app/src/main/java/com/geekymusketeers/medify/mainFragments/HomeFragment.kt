package com.geekymusketeers.medify.mainFragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.geekymusketeers.medify.AppointmentBooking
import com.geekymusketeers.medify.RemoveCountryCode
import com.geekymusketeers.medify.databinding.FragmentHomeBinding
import com.google.firebase.FirebaseError
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.ncorti.slidetoact.SlideToActView


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var db: DatabaseReference

    //Current User's data
    private lateinit var userName : String
    private lateinit var userEmail : String
    private lateinit var userPhone : String
    private lateinit var userPosition: String
    private lateinit var userType: String

    //Searched doctor's data
    private lateinit var searchedName : String
    private lateinit var searchedEmail : String
    private lateinit var searchedPhone : String
    private lateinit var searchedData : String
    private lateinit var searchedUID : String

    private lateinit var sharedPreference : SharedPreferences


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        firebaseAuth = FirebaseAuth.getInstance()
        val user = firebaseAuth.currentUser

        db = FirebaseDatabase.getInstance().reference
        sharedPreference = requireActivity().getSharedPreferences("UserData", Context.MODE_PRIVATE)

        getDataFromSharedPreference()


        binding.doctorData.setOnEditorActionListener { textView, i, keyEvent ->
            if (i == EditorInfo.IME_ACTION_DONE) {
                // Call your code here
                searchedData = binding.doctorData.text.toString().trim()
                if (searchedData.isNotEmpty()) {
                    if (RemoveCountryCode.remove(searchedData) == userPhone || searchedData == userPhone || searchedData == userEmail) {
                        Toast.makeText(requireActivity(), "Stop searching yourself", Toast.LENGTH_SHORT).show()
                        binding.cardView.isVisible = false
                        binding.slider.isVisible = false
                    }else {
                        doctorIsPresent()
                    }
                } else {
                    Toast.makeText(requireActivity(), "Enter doctor's email / phone", Toast.LENGTH_SHORT).show()
                }
                true
            }
            false
        }


        binding.slider.animDuration = 150
        binding.slider.onSlideCompleteListener = object : SlideToActView.OnSlideCompleteListener {
            override fun onSlideComplete(view: SlideToActView) {
                val intent =  Intent(requireActivity(), AppointmentBooking::class.java)
                intent.putExtra("uid", searchedUID)
                intent.putExtra("name", searchedName)
                intent.putExtra("email", searchedEmail)
                intent.putExtra("phone", searchedPhone)
                startActivity(intent)
                binding.slider.resetSlider()
            }
        }


        return binding.root
    }

    private fun doctorIsPresent() {

        db.child("Doctor").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (child in dataSnapshot.children) {
                    val map = child.value as HashMap<*, *>
                    val name = "Dr. " +map["name"].toString().trim()
                    val type = map["specialist"].toString().trim()
                    val email = map["email"].toString().trim()
                    val phone = map["phone"].toString().trim()
                    if (searchedData == email || searchedData == phone || RemoveCountryCode.remove(searchedData) == phone) {
                        searchedName = name
                        searchedEmail = email
                        searchedPhone = phone
                        searchedUID = child.value.toString().trim()
                        binding.textView3.isVisible = false
                        binding.cardView.isVisible = true
                        binding.slider.isVisible = true
                        binding.doctorName.text = name
                        binding.doctortype.text = type
                        binding.doctorEmail.text = email
                        binding.doctorPhone.text = phone
                        return
                    } else
                        binding.textView3.isVisible = true
                }
            }

            override fun onCancelled(error: DatabaseError) {}
            fun onCancelled(firebaseError: FirebaseError?) {}
        })
    }

    override fun onStart() {
        super.onStart()
        Handler().postDelayed({
            getDataFromSharedPreference()
        }, 1000)
    }

    @SuppressLint("SetTextI18n")
    private fun getDataFromSharedPreference() {
        userName = sharedPreference.getString("name","Not found").toString()
        userEmail = sharedPreference.getString("email","Not found").toString()
        userPhone = sharedPreference.getString("phone","Not found").toString()
        userPosition = sharedPreference.getString("isDoctor", "Not fount").toString()

        if (userPosition == "Doctor")
            binding.namePreview.text = "Dr. $userName"
        else
            binding.namePreview.text = userName

    }

}
package com.caravan.caravan.ui.fragment.edit

import android.app.Activity
import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import com.caravan.caravan.R
import com.caravan.caravan.databinding.FragmentEditProfileBinding
import com.caravan.caravan.ui.fragment.BaseFragment
import com.caravan.caravan.utils.Dialog
import com.caravan.caravan.utils.viewBinding
import com.sangcomz.fishbun.FishBun
import com.sangcomz.fishbun.adapter.image.impl.GlideAdapter
import java.text.SimpleDateFormat
import java.util.*


/**
 * A simple [Fragment] subclass.
 * Use the [EditProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class EditProfileFragment : BaseFragment(R.layout.fragment_edit_profile) {
    private val binding by viewBinding { FragmentEditProfileBinding.bind(it) }
    private var gender: String? = null
    private var pickedPhoto: Uri? = null
    private var allPhotos = ArrayList<Uri>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    private fun initViews() {
        manageGender()
        binding.ivGuide.setOnClickListener {
            pickPhoto()
        }

        binding.llCalendar.setOnClickListener {
            setBirthday()
        }

        binding.btnSave.setOnClickListener {
            saveProfileData()
        }

    }

    private fun saveProfileData() {
        //edit request

//        Dialog.showDialogMessage(requireContext(),"Saved",)
    }

    private fun setBirthday() {
        val datePicker = Calendar.getInstance()
        val date = DatePickerDialog.OnDateSetListener { picker, year, month, day ->
            datePicker[Calendar.YEAR] = year
            datePicker[Calendar.MONTH] = month
            datePicker[Calendar.DAY_OF_MONTH] = day
            val dateFormat = "dd.MM.yyyy"
            val simpleDateFormat = SimpleDateFormat(dateFormat, Locale.getDefault())
            binding.tvBirthday.text = simpleDateFormat.format(datePicker.time)
        }
        DatePickerDialog(
            requireContext(), date,
            datePicker[Calendar.YEAR],
            datePicker[Calendar.MONTH],
            datePicker[Calendar.DAY_OF_MONTH]
        ).show()
    }

    private fun manageGender() {
        binding.checkboxMale.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                binding.checkboxFemale.isChecked = false
                gender = getString(R.string.str_gender_male)
            }
        }
        binding.checkboxFemale.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                binding.checkboxMale.isChecked = false
                gender = getString(R.string.str_gender_female)
            }
        }
    }

    /**
     * pick photo using FishBun library
     */
    private fun pickPhoto() {

        FishBun.with(this)
            .setImageAdapter(GlideAdapter())
            .setMaxCount(1)
            .setMinCount(1)
            .setSelectedImages(allPhotos)
            .startAlbumWithActivityResultCallback(photoLauncher)
    }

    private val photoLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                allPhotos =
                    it.data?.getParcelableArrayListExtra(FishBun.INTENT_PATH) ?: arrayListOf()
                pickedPhoto = allPhotos[0]
                uploadUserPhoto()
            }
        }

    private fun uploadUserPhoto() {
        if (pickedPhoto == null) return
        //save photo to storage
        binding.ivGuide.setImageURI(pickedPhoto)
    }

}
package com.caravan.caravan.ui.fragment.details

import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.caravan.caravan.R
import com.caravan.caravan.adapter.CommentsAdapter
import com.caravan.caravan.adapter.FacilitiesAdapter
import com.caravan.caravan.adapter.TravelLocationsAdapter
import com.caravan.caravan.adapter.TripPhotosAdapter
import com.caravan.caravan.databinding.FragmentTripDetailsBinding
import com.caravan.caravan.databinding.OverlayViewBinding
import com.caravan.caravan.manager.SharedPref
import com.caravan.caravan.model.*
import com.caravan.caravan.model.Comment
import com.caravan.caravan.network.ApiService
import com.caravan.caravan.network.RetrofitHttp
import com.caravan.caravan.ui.fragment.BaseFragment
import com.caravan.caravan.utils.Dialog
import com.caravan.caravan.utils.OkInterface
import com.caravan.caravan.utils.UiStateObject
import com.caravan.caravan.viewmodel.details.TripDetailsRepository
import com.caravan.caravan.viewmodel.details.TripDetailsViewModel
import com.caravan.caravan.viewmodel.details.TripDetailsViewModelFactory
import com.stfalcon.imageviewer.StfalconImageViewer
import com.zhpan.indicator.enums.IndicatorSlideMode
import com.zhpan.indicator.enums.IndicatorStyle

class TripDetailsFragment : BaseFragment() {
    private lateinit var fragmentTripDetailsBinding: FragmentTripDetailsBinding
    private var tripId: String = "null"
    private lateinit var overlayViewBinding: OverlayViewBinding

    private lateinit var viewModel: TripDetailsViewModel
    private lateinit var trip: Trip

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            tripId = it.getString("tripId", "defaultValue")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fragmentTripDetailsBinding = FragmentTripDetailsBinding.inflate(layoutInflater)

        setUpViewModel()
        setUpObserves()

        initViews()
        return fragmentTripDetailsBinding.root
    }

    private fun setUpObserves() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.trip.collect {
                when(it) {
                    is UiStateObject.LOADING -> {
                        showLoading()
                    }
                    is UiStateObject.SUCCESS -> {
                        dismissLoading()

                        setUpDate(it.data)
                    }
                    is UiStateObject.ERROR -> {
                        dismissLoading()
                        Dialog.showDialogWarning(
                            requireContext(),
                            getString(R.string.str_no_connection),
                            getString(R.string.str_try_again),
                            object : OkInterface {
                                override fun onClick() {
                                    requireActivity().onBackPressed()
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    private fun setUpDate(data: Trip) {
        setViewPager(data.photos)
        setTravelLocations(data.places)
        setFacilities(data.facility)
        setCommentsRv(data.reviews)
        setLeaveCommentsPart(data.attendancesProfileId, data.reviews)

        fragmentTripDetailsBinding.tvTripPrice.text = setPrice(trip.price)
        fragmentTripDetailsBinding.tvGuidePrice.text = setPrice(trip.price)

    }

    private fun setUpViewModel() {
        viewModel = ViewModelProvider(this, TripDetailsViewModelFactory(TripDetailsRepository(RetrofitHttp.createServiceWithAuth(
            SharedPref(requireContext()), ApiService::class.java))))[TripDetailsViewModel::class.java]
    }

    private fun initViews() {
        overlayViewBinding = OverlayViewBinding.bind(
            LayoutInflater.from(requireContext())
                .inflate(R.layout.overlay_view, RelativeLayout(requireContext()), false)
        )

        fragmentTripDetailsBinding.guideProfile.setOnClickListener {
            Navigation.findNavController(requireActivity(), R.id.details_nav_fragment)
                .navigate(R.id.action_tripDetailsFragment_to_guideDetailsFragment);
        }

    }

    fun setImageViewer(position: Int) {

        val mView = LayoutInflater.from(requireContext())
            .inflate(R.layout.overlay_view, LinearLayout(requireContext()), false)

        overlayViewBinding.name.text =
            trip.photos[position].location.provence + ", " + trip.photos[position].location.district
        overlayViewBinding.tvDescription.text =
            trip.photos[position].location.description

        StfalconImageViewer.Builder(
            requireContext(),
            trip.photos
        ) { view, image ->


            Glide.with(requireContext()).load(image.url).into(view)
        }.withHiddenStatusBar(false)
            .withDismissListener {
                overlayViewBinding = OverlayViewBinding.bind(mView)
            }
            .withStartPosition(position)
            .withOverlayView(
                overlayViewBinding.root
            ).withImageChangeListener {
                overlayViewBinding.name.text =
                    trip.photos[it].location.district + ", " + trip.photos[it].location.district
                overlayViewBinding.tvDescription.text =
                    trip.photos[it].location.description
            }
            .show()
    }


    private fun setPrice(price: Price): Spannable {
        val text = "$${price.cost.toInt()}"
        val endIndex = text.length

        val outPutColoredText: Spannable = SpannableString("$text/${trip.price.type}")
        outPutColoredText.setSpan(RelativeSizeSpan(1.2f), 0, endIndex, 0)
        outPutColoredText.setSpan(
            ForegroundColorSpan(Color.parseColor("#167351")),
            0,
            endIndex,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        return outPutColoredText
    }

    private fun setLeaveCommentsPart(ids: ArrayList<String>?, reviews: ArrayList<Comment>?) {
        val profileId = SharedPref(requireContext()).getString("profileId")
        if (ids != null && ids.contains(profileId!!)) {

            if (!reviews.isNullOrEmpty()) {
                var isHave = true
                for (i in reviews) {
                    if (i.from.id == profileId) {
                        isHave = true
                        break
                    }
                }
                if (isHave)
                    fragmentTripDetailsBinding.leaveCommentPart.visibility = View.GONE
                else
                    fragmentTripDetailsBinding.leaveCommentPart.visibility = View.VISIBLE
            } else {
                fragmentTripDetailsBinding.leaveCommentPart.visibility = View.VISIBLE
            }
        } else {
            fragmentTripDetailsBinding.leaveCommentPart.visibility = View.GONE
        }
    }

    private fun setViewPager(photos: ArrayList<TourPhoto>) {
        fragmentTripDetailsBinding.apply {
            viewPager2.apply {
                adapter = TripPhotosAdapter(
                    this@TripDetailsFragment,
                    photos
                ) //TripDetailsFragment viewPager items, It should be Trip items and they should come from server
                setIndicator()
                registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                    override fun onPageSelected(position: Int) {
                        super.onPageSelected(position)

                    }
                })
            }
        }
    }

    private fun setCommentsRv(reviews: ArrayList<Comment>?) {
        fragmentTripDetailsBinding.fragmentTripCommentsRV.adapter = reviews?.let {
            CommentsAdapter(it)
        }
    }

    /*TripDetailsFragment viewPager items, It should be Trip items and they should come from server
    */

    private fun setTravelLocations(places: ArrayList<Location>) {
        fragmentTripDetailsBinding.apply {
            travelLocationsRV.adapter = TravelLocationsAdapter(places)
        }
    }

    private fun setFacilities(facility: ArrayList<Facility>) {
        fragmentTripDetailsBinding.apply {
            facilitiesRV.adapter = FacilitiesAdapter(facility)
        }
    }

    private fun setIndicator() {
        fragmentTripDetailsBinding.indicatorView.apply {
            setSliderColor(Color.parseColor("#b8d1d2"), Color.parseColor("#167351"))
            setSliderWidth(resources.getDimension(R.dimen.dp_20))
            setSliderHeight(resources.getDimension(R.dimen.dp_6))
            setSlideMode(IndicatorSlideMode.SMOOTH)
            setIndicatorStyle(IndicatorStyle.ROUND_RECT)
            setupWithViewPager(fragmentTripDetailsBinding.viewPager2)
        }
    }

}
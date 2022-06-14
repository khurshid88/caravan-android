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
import androidx.recyclerview.widget.RecyclerView
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
import com.caravan.caravan.model.more.ActionMessage
import com.caravan.caravan.model.review.Review
import com.caravan.caravan.network.ApiService
import com.caravan.caravan.network.RetrofitHttp
import com.caravan.caravan.ui.fragment.BaseFragment
import com.caravan.caravan.utils.Extensions.toast
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

    private var page = 0
    private var allPages = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            tripId = it.getString("tripId", null)
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
                when (it) {
                    is UiStateObject.LOADING -> {
                        fragmentTripDetailsBinding.apply {
                            llRoot.visibility = View.GONE
                        }
                        showLoading()
                    }
                    is UiStateObject.SUCCESS -> {
                        fragmentTripDetailsBinding.apply {
                            llRoot.visibility = View.VISIBLE
                        }
                        dismissLoading()
                        trip = it.data
                        setUpDate(it.data)
                    }
                    is UiStateObject.ERROR -> {
                        fragmentTripDetailsBinding.apply {
                            llRoot.visibility = View.GONE
                        }
                        dismissLoading()
                        showDialogWarning(
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
        viewModel = ViewModelProvider(
            this, TripDetailsViewModelFactory(
                TripDetailsRepository(
                    RetrofitHttp.createServiceWithAuth(
                        SharedPref(requireContext()), ApiService::class.java
                    )
                )
            )
        )[TripDetailsViewModel::class.java]
    }

    private fun initViews() {

        viewModel.getTrip(tripId)

        overlayViewBinding = OverlayViewBinding.bind(
            LayoutInflater.from(requireContext())
                .inflate(R.layout.overlay_view, RelativeLayout(requireContext()), false)
        )

        fragmentTripDetailsBinding.guideProfile.setOnClickListener {
            Navigation.findNavController(requireActivity(), R.id.details_nav_fragment)
                .navigate(R.id.action_tripDetailsFragment_to_guideDetailsFragment);
        }

        fragmentTripDetailsBinding.apply {

            btnSendComment.setOnClickListener {
                if (etLeaveComment.text.isNotEmpty()) {
                    val rate = ratingBarGuide.rating
                    val review =
                        Review(
                            rate.toInt(),
                            etLeaveComment.text.toString(),
                            "GUIDE",
                            null,
                            trip.guideProfile.id
                        )

                    setUpObservesReview()

                    viewModel.postReview(review)

                } else {
                    toast(getString(R.string.str_send_message))
                }
            }

        }

    }

    private fun setUpObservesReview() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.review.collect {
                when (it) {
                    is UiStateObject.LOADING -> {
                        showLoading()
                    }
                    is UiStateObject.SUCCESS -> {
                        dismissLoading()
                        setStatus(it.data)
                    }
                    is UiStateObject.ERROR -> {
                        dismissLoading()
                        showDialogWarning(
                            getString(R.string.str_no_connection),
                            getString(R.string.str_try_again),
                            object : OkInterface {
                                override fun onClick() {
                                    return
                                }
                            })
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.reviews.collect {
                when (it) {
                    is UiStateObject.LOADING -> {
                        showLoading()
                    }
                    is UiStateObject.SUCCESS -> {
                        dismissLoading()
                        allPages = it.data.totalPages
                        page = it.data.currentPageNumber

                        if (page + 1 <= allPages) {
                            page++
                        }

                        updateComments(it.data.comments)
                    }
                    is UiStateObject.ERROR -> {
                        dismissLoading()
                        showDialogWarning(
                            getString(R.string.str_no_connection),
                            getString(R.string.str_try_again),
                            object : OkInterface {
                                override fun onClick() {
                                    return
                                }
                            }
                        )
                    }
                    else -> Unit
                }
            }
        }

    }

    private fun updateComments(data: ArrayList<Comment>) {
        fragmentTripDetailsBinding.fragmentTripCommentsRV.adapter = CommentsAdapter(data)
        fragmentTripDetailsBinding.fragmentTripCommentsRV.adapter?.notifyDataSetChanged()
    }

    private fun setStatus(data: ActionMessage) {
        if (!data.status) {
            showDialogWarning(data.title!!, data.message!!, object : OkInterface {
                override fun onClick() {
                    viewModel.getReviews(page, tripId)
                }
            })
        } else {
            fragmentTripDetailsBinding.apply {
                etLeaveComment.setText("")
                leaveCommentPart.visibility = View.GONE
            }
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
                var isHave = false
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
                )
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

        fragmentTripDetailsBinding.fragmentTripCommentsRV.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                if (!recyclerView.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE) {
                    viewModel.getReviews(page, tripId)
                }
            }
        })
    }

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
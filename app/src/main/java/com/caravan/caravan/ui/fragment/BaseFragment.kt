package com.caravan.caravan.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.caravan.caravan.model.Comment
import com.caravan.caravan.model.Trip
import com.caravan.caravan.ui.activity.DetailsActivity
import com.caravan.caravan.ui.activity.GuideOptionActivity
import com.caravan.caravan.ui.fragment.guideOption.FeedbackRespondFragment

abstract class BaseFragment: Fragment() {

    open fun goToDetailsActivity(trip: Trip){
        val intent = Intent(requireContext(), DetailsActivity::class.java)
        intent.putExtra("tripId", trip.id.toString())
        startActivity(intent)
    }

}
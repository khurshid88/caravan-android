package com.caravan.caravan.adapter


import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.caravan.caravan.databinding.ItemTripsBinding
import com.caravan.caravan.model.Trip
import com.caravan.caravan.ui.fragment.BaseFragment

class TripAdapter(val context: Fragment, var items: ArrayList<Trip>) :
    RecyclerView.Adapter<TripAdapter.ViewHolder>() {

    inner class ViewHolder(private val itemTripsBinding: ItemTripsBinding) :
        RecyclerView.ViewHolder(itemTripsBinding.root) {
        fun bind(trip: Trip) {
            Glide.with(context).load(trip.photos[0].photo).into(itemTripsBinding.ivTripPhoto)
            itemTripsBinding.tvTripTitle.text = trip.name
            itemTripsBinding.ratingBarTrip.rating = trip.rate.toFloat()
            itemTripsBinding.tvTripCommentsCount.text = trip.reviews?.size.toString()
            itemTripsBinding.tvTripCommentsCount.text = "(${if (trip.reviews.isNullOrEmpty()) "0" else trip.reviews.size})"
            itemTripsBinding.tvPrice.text = price(trip)
            itemView.setOnClickListener {
                (context as BaseFragment).goToDetailsActivity(trip)
            }

        }

    }

    private fun price(trip: Trip): Spannable {
        val text = "$${trip.price.cost.toInt()}"
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripAdapter.ViewHolder {
        return ViewHolder(
            ItemTripsBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: TripAdapter.ViewHolder, position: Int) {
        holder.bind(items[position])
    }
}
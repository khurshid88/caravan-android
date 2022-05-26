package com.caravan.caravan.ui.fragment.guideOption

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.caravan.caravan.R
import com.caravan.caravan.adapter.TripAdapter
import com.caravan.caravan.databinding.FragmentTripListBinding
import com.caravan.caravan.model.*

class TripListFragment : Fragment() {

    private lateinit var binding : FragmentTripListBinding
    lateinit var tripAdapter: TripAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
       binding = FragmentTripListBinding.inflate(inflater,container,false)
        initViews()
        return binding.root
    }

    private fun initViews() {
        binding.recyclerViewMyTrips.layoutManager = GridLayoutManager(activity,1)

    }

    fun refreshAdapterTrip(list: ArrayList<Trip>) {
        tripAdapter = TripAdapter(this, list)
        binding.recyclerViewMyTrips.adapter = tripAdapter
    }

    fun loadTripsList():ArrayList<Trip>{

        val guide = GuideProfile(
            "100001",
            Profile(
                "1001",
                "Ogabek",
                "Matyakubov",
                "+998997492581",
                "ogabekdev@gmail.com",
                "GUIDE",
                "ACTIVE",
                "https://wanderingwheatleys.com/wp-content/uploads/2019/04/khiva-uzbekistan-things-to-do-see-islam-khoja-minaret-3-480x600.jpg",
                "MALE",
                null,
                "12.10.2022",
                null,
                "en",
                arrayListOf()
            ),
            "+998932037313",
            "Ogabek Matyakubov",
            true,
            4.5,
            Price(150.0, "USD", "day"),
            ArrayList<Language>().apply {
                add(Language("1", "English", "Advanced"))
                add(Language("2", "Uzbek", "Native"))
            },
            ArrayList<Location>().apply {
                add(Location("1", "Khorezm", "Khiva", "Ichan Qala"))
                add(Location("1", "Khorezm", "Khiva", "Ichan Qala"))
                add(Location("1", "Khorezm", "Khiva", "Ichan Qala"))
            },
            arrayListOf(),
            arrayListOf(),
            arrayListOf()
        )

        val list = ArrayList<Trip>()
        for (i in 0..10) {
            list.add(
                Trip("1", "Khiva in 3 days",
                    ArrayList<TourPhoto>().apply {
                        add(TourPhoto("1", 1, "jpg", Location("1", "Khorezm", "Khiva", "Ichan Qala"), "12.02.2022", null, "https://wanderingwheatleys.com/wp-content/uploads/2019/04/khiva-uzbekistan-things-to-do-see-islam-khoja-minaret-3-480x600.jpg"))
                        add(TourPhoto("1", 1, "jpg", Location("1", "Khorezm", "Khiva", "Ichan Qala"), "12.02.2022", null, "https://wanderingwheatleys.com/wp-content/uploads/2019/04/khiva-uzbekistan-things-to-do-see-islam-khoja-minaret-3-480x600.jpg"))
                        add(TourPhoto("1", 1, "jpg", Location("1", "Khorezm", "Khiva", "Ichan Qala"), "12.02.2022", null, "https://wanderingwheatleys.com/wp-content/uploads/2019/04/khiva-uzbekistan-things-to-do-see-islam-khoja-minaret-3-480x600.jpg"))
                    },
                    ArrayList<Facility>().apply {
                        add(Facility("1", "Moshina", "Moshina bilan taminliman"))
                        add(Facility("1", "Moshina", "Moshina bilan taminliman"))
                        add(Facility("1", "Moshina", "Moshina bilan taminliman"))
                    },
                    ArrayList<Location>().apply {
                        add(Location("1", "Khorezm", "Khiva", "Ichan Qala"))
                        add(Location("1", "Khorezm", "Khiva", "Ichan Qala"))
                        add(Location("1", "Khorezm", "Khiva", "Ichan Qala"))
                    },
                    "Khiva in 3 days",
                    Price(1200.0, "USD", "trip"),
                    5, 10,
                    guide,
                    "+998997492581",
                    4.5,
                    arrayListOf(),
                    null
                )
            )
        }
        return list
    }

}
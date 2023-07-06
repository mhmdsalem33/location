package com.alexon.geofance

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.alexon.geofance.databinding.FragmentHomeBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.android.PolyUtil


class HomeFragment : Fragment() , OnMapReadyCallback {

    private lateinit var binding : FragmentHomeBinding
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 123
        private const val REQUEST_LOCATION_PERMISSION = 1001

    }
    private lateinit var polygon: Polygon
    private var mLocationManager: LocationManager? = null
    private var mLocationListener: LocationListener? = null
    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener
    var mMap : GoogleMap ? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater , container , false)
        return binding.root
    }

    @SuppressLint("SuspiciousIndentation")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


         val mapFragment = childFragmentManager.findFragmentById(R.id.homeMap) as SupportMapFragment
             mapFragment.getMapAsync(this)



        if (ContextCompat.checkSelfPermission(requireContext() , android.Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            // Permission already granted, proceed with getting the location
        } else {
            // Permission not granted, request it
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }

//
    }

   private lateinit var fusedLocationClient: FusedLocationProviderClient
    override fun onMapReady(googleMap: GoogleMap) {
               mMap = googleMap
        getLocation(googleMap)

                val  uiSettings = googleMap.uiSettings
                     uiSettings.isZoomControlsEnabled = true



      val poylineOption =
            PolygonOptions()
                .add(LatLng(31.0287, 31.3954),
                    LatLng(31.0286 , 31.3954 ),
                    LatLng(31.0286, 31.3952  ),
                    LatLng(31.0287 , 31.3952 )
                )
                .strokeColor(Color.RED)
                .fillColor(Color.BLUE)



        polygon = googleMap.addPolygon(poylineOption)




        locationManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                val latitude = location.latitude
                val longitude = location.longitude

                checkInsidePolygon(longitude = location.longitude , latitude = location.latitude)
                Toast.makeText(
                    requireContext(),
                    "Latitude: $latitude, Longitude: $longitude",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        }

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request location permissions if not granted
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                REQUEST_LOCATION_PERMISSION
            )
        } else {
            // Start requesting location updates
            startLocationUpdates()
        }


    }



    private fun getLocation(googleMap : GoogleMap){


        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        googleMap.isMyLocationEnabled = true
        googleMap.uiSettings.isMyLocationButtonEnabled = true

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    // Handle the retrieved location here

                    checkInsidePolygon(longitude = location.longitude , latitude = location.latitude)

                } else {
//                    Toast.makeText(requireContext(), "Unable to retrieve location.", Toast.LENGTH_SHORT)
//                        .show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error getting location: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
    }


        override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Location permission granted, proceed with getting the location
            } else {
                // Location permission denied
                Toast.makeText(
                    requireContext(),
                    "Location permission denied. Unable to get current location.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            5000,
            10f,
            locationListener
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        // Stop requesting location updates when the activity is destroyed
        locationManager.removeUpdates(locationListener)
    }

    private var marker :Marker ?  = null

    private fun checkInsidePolygon(  latitude : Double ,longitude: Double ){


//        marker?.remove()

        val latLng = LatLng(latitude, longitude)
        mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng , 18f))
//                    googleMap.addMarker(MarkerOptions().position(latLng).title("el hoda wel nor").snippet("Borio coffe").icon(
//                        BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)))

        val currentLatLng = LatLng(latitude, longitude)







        if (PolyUtil.containsLocation(currentLatLng, polygon.points, true)) {
            Toast.makeText(requireContext(), "inside place ", Toast.LENGTH_SHORT).show()
           marker =  mMap?.addMarker(MarkerOptions().position(latLng).title("el hoda wel nor").snippet("Borio coffe").icon(
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)))

        } else {
            Toast.makeText(requireContext(), "outside place ", Toast.LENGTH_SHORT).show()
            marker =   mMap?.addMarker(MarkerOptions().position(latLng).title("el hoda wel nor").snippet("Borio coffe").icon(
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)))
        }
    }


}


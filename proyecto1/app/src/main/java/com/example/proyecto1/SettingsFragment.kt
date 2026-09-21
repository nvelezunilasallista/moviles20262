package com.example.proyecto1

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

class SettingsFragment : Fragment() {

    private lateinit var ivProfile: ImageView
    private lateinit var etLatitude: EditText
    private lateinit var etLongitude: EditText

    // Contrato para seleccionar imagen de la galería
    private val pickGalleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { ivProfile.setImageURI(it) }
    }

    // Contrato para tomar foto con la cámara (retorna miniatura Bitmap)
    private val takePhotoLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
        bitmap?.let { ivProfile.setImageBitmap(it) }
    }

    // Contrato para pedir permiso de ubicación
    private val requestLocationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            fetchLocation()
        } else {
            Toast.makeText(requireContext(), "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ivProfile = view.findViewById(R.id.ivProfile)
        etLatitude = view.findViewById(R.id.etLatitude)
        etLongitude = view.findViewById(R.id.etLongitude)

        val btnChangePhoto = view.findViewById<Button>(R.id.btnChangePhoto)
        val btnGetLocation = view.findViewById<Button>(R.id.btnGetLocation)

        val photoClickListener = View.OnClickListener { showImagePickerDialog() }
        ivProfile.setOnClickListener(photoClickListener)
        btnChangePhoto.setOnClickListener(photoClickListener)

        btnGetLocation.setOnClickListener {
            checkLocationPermissionAndFetch()
        }
    }

    private fun showImagePickerDialog() {
        val options = arrayOf("Tomar foto", "Elegir de la galería")
        AlertDialog.Builder(requireContext())
            .setTitle("Seleccionar foto de perfil")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> takePhotoLauncher.launch(null)
                    1 -> pickGalleryLauncher.launch("image/*")
                }
            }
            .show()
    }

    private fun checkLocationPermissionAndFetch() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fetchLocation()
        } else {
            requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    @SuppressLint("MissingPermission")
    private fun fetchLocation() {
        val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val providers = listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
            LocationManager.PASSIVE_PROVIDER
        )

        var bestLocation: Location? = null
        for (provider in providers) {
            try {
                val loc = locationManager.getLastKnownLocation(provider) ?: continue
                if (bestLocation == null || loc.accuracy < bestLocation.accuracy) {
                    bestLocation = loc
                }
            } catch (_: Exception) {}
        }

        if (bestLocation != null) {
            etLatitude.setText(bestLocation.latitude.toString())
            etLongitude.setText(bestLocation.longitude.toString())
        } else {
            Toast.makeText(requireContext(), "Buscando ubicación...", Toast.LENGTH_SHORT).show()
            try {
                val provider = if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    LocationManager.GPS_PROVIDER
                } else {
                    LocationManager.NETWORK_PROVIDER
                }
                locationManager.requestSingleUpdate(provider, object : LocationListener {
                    override fun onLocationChanged(loc: Location) {
                        etLatitude.setText(loc.latitude.toString())
                        etLongitude.setText(loc.longitude.toString())
                    }
                    override fun onProviderDisabled(provider: String) {}
                    override fun onProviderEnabled(provider: String) {}
                    @Deprecated("Deprecated in Java")
                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                }, null)
            } catch (_: Exception) {
                Toast.makeText(requireContext(), "No se pudo obtener la ubicación", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
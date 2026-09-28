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

/**
 * SettingsFragment:
 * Fragmento encargado de la pantalla de Ajustes/Configuración del usuario.
 *
 * Funcionalidades principales:
 * 1. Selección y cambio de foto de perfil (desde la cámara o galería).
 * 2. Consulta y visualización de la ubicación GPS actual (Latitud y Longitud).
 */
class SettingsFragment : Fragment() {

    // Referencias a los componentes del diseño (UI)
    private lateinit var ivProfile: ImageView
    private lateinit var etLatitude: EditText
    private lateinit var etLongitude: EditText

    /**
     * Contrato ActivityResult API para seleccionar un archivo multimedia de la GALERÍA.
     * 'GetContent' abre el selector nativo del sistema y nos devuelve la URI de la imagen elegida.
     */
    private val pickGalleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { ivProfile.setImageURI(it) }
    }

    /**
     * Contrato ActivityResult API para capturar una foto con la CÁMARA del dispositivo.
     * 'TakePicturePreview' abre la cámara por defecto y retorna directamente un Bitmap con la miniatura de la imagen.
     */
    private val takePhotoLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
        bitmap?.let { ivProfile.setImageBitmap(it) }
    }

    /**
     * Contrato ActivityResult API para solicitar PERMISOS en tiempo de ejecución.
     * Evalúa si el usuario concedió el permiso de ubicación; si es así, ejecuta 'fetchLocation()'.
     */
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
        // Infla (carga) la vista XML 'fragment_settings' para este fragmento
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializamos las vistas buscando sus IDs en el diseño inflado
        ivProfile = view.findViewById(R.id.ivProfile)
        etLatitude = view.findViewById(R.id.etLatitude)
        etLongitude = view.findViewById(R.id.etLongitude)

        val btnChangePhoto = view.findViewById<Button>(R.id.btnChangePhoto)
        val btnGetLocation = view.findViewById<Button>(R.id.btnGetLocation)

        // Asignamos la acción de cambiar foto tanto a la imagen como al botón
        val photoClickListener = View.OnClickListener { showImagePickerDialog() }
        ivProfile.setOnClickListener(photoClickListener)
        btnChangePhoto.setOnClickListener(photoClickListener)

        // Asignamos la acción de consultar la ubicación al botón correspondiente
        btnGetLocation.setOnClickListener {
            checkLocationPermissionAndFetch()
        }
    }

    /**
     * Despliega un diálogo de alerta (AlertDialog) que le permite al usuario elegir
     * entre tomar una foto con la cámara o seleccionar una existente en la galería.
     */
    private fun showImagePickerDialog() {
        val options = arrayOf("Tomar foto", "Elegir de la galería")
        AlertDialog.Builder(requireContext())
            .setTitle("Seleccionar foto de perfil")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> takePhotoLauncher.launch(null)           // Opción 0: Abrir cámara
                    1 -> pickGalleryLauncher.launch("image/*")  // Opción 1: Abrir galería de imágenes
                }
            }
            .show()
    }

    /**
     * Verifica si el permiso 'ACCESS_FINE_LOCATION' ya ha sido concedido por el usuario.
     * Si ya lo tiene, llama a 'fetchLocation()'. De lo contrario, solicita el permiso.
     */
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

    /**
     * fetchLocation:
     * Obtiene y muestra las coordenadas actuales de latitud y longitud del dispositivo.
     *
     * Flujo de trabajo:
     * 1. Obtiene el servicio de ubicación nativo del sistema (LocationManager).
     * 2. Evalúa los diferentes proveedores de ubicación disponibles (GPS, Red/Celular, Pasivo).
     * 3. Busca la "última ubicación conocida" (Last Known Location) con mayor precisión disponible.
     * 4. Si encuentra una ubicación previa válida, actualiza los EditText con las coordenadas.
     * 5. Si no hay ubicación previa guardada, registra un oyente (LocationListener) para solicitar
     *    una actualización de ubicación en tiempo real.
     */
    @SuppressLint("MissingPermission")
    private fun fetchLocation() {
        // 1. Acceso al LocationManager nativo del sistema Android
        val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // Lista de proveedores ordenados para consultar las ubicaciones en caché
        val providers = listOf(
            LocationManager.GPS_PROVIDER,       // GPS de hardware (alta precisión)
            LocationManager.NETWORK_PROVIDER,   // Basado en antenas celulares / Wi-Fi
            LocationManager.PASSIVE_PROVIDER   // Ubicaciones solicitadas por otras aplicaciones
        )

        var bestLocation: Location? = null

        // 2. Iteramos sobre los proveedores para obtener la mejor 'última ubicación conocida'
        for (provider in providers) {
            try {
                val loc = locationManager.getLastKnownLocation(provider) ?: continue
                // Seleccionamos la ubicación de menor margen de error (menor valor de accuracy en metros)
                if (bestLocation == null || loc.accuracy < bestLocation.accuracy) {
                    bestLocation = loc
                }
            } catch (_: Exception) {
                // Si un proveedor no está disponible, continuamos con el siguiente
            }
        }

        // 3. Si encontramos una ubicación válida previa, la mostramos en los EditText
        if (bestLocation != null) {
            etLatitude.setText(bestLocation.latitude.toString())
            etLongitude.setText(bestLocation.longitude.toString())
        } else {
            // 4. Si no existía ubicación almacenada en caché, solicitamos una nueva actualización en tiempo real
            Toast.makeText(requireContext(), "Buscando ubicación...", Toast.LENGTH_SHORT).show()
            try {
                // Seleccionamos el proveedor activo disponible (GPS o Red)
                val provider = if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    LocationManager.GPS_PROVIDER
                } else {
                    LocationManager.NETWORK_PROVIDER
                }

                // Solicitamos una única actualización de posición actual
                locationManager.requestSingleUpdate(provider, object : LocationListener {
                    override fun onLocationChanged(loc: Location) {
                        // Se ejecuta cuando el sensor responde con la nueva ubicación
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
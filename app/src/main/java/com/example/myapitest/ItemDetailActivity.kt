package com.example.myapitest

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapitest.databinding.ActivityItemDetailBinding
import com.example.myapitest.model.Item
import com.example.myapitest.service.Result
import com.example.myapitest.service.RetrofitClient
import com.example.myapitest.service.safeApiCall
import com.example.myapitest.ui.loadUrl
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ItemDetailActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityItemDetailBinding
    private lateinit var item: Item
    private var mMap: GoogleMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityItemDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val itemId = intent.getStringExtra(ARG_ID) ?: ""
        
        setupView()
        loadItem(itemId)
        setupGoogleMap()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        if (::item.isInitialized) {
            loadItemInGoogleMap()
        }
    }

    private fun setupView() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
        binding.deleteCTA.setOnClickListener {
            deleteItem()
        }
        binding.editCTA.setOnClickListener {
            editItem()
        }
    }

    private fun loadItem(itemId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val result = safeApiCall { RetrofitClient.apiService.getItem(itemId) }

            withContext(Dispatchers.Main) {
                when (result) {
                    is Result.Success -> {
                        item = result.data
                        handleSuccess()
                    }
                    is Result.Error -> {
                        Toast.makeText(
                            this@ItemDetailActivity,
                            "Erro ao carregar detalhes do item",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private fun handleSuccess() {
        // Usa os dados do nível principal ou de dentro de 'value' (modelo híbrido)
        val name = item.name ?: item.value?.name
        val year = item.year ?: item.value?.year
        val licence = item.licence ?: item.value?.licence
        val imageUrl = item.imageUrl ?: item.value?.imageUrl

        binding.name.setText(name ?: "")
        binding.year.setText(year ?: "")
        binding.licence.setText(licence ?: "")
        
        if (!imageUrl.isNullOrEmpty()) {
            binding.image.loadUrl(imageUrl)
        }
        
        loadItemInGoogleMap()
    }

    private fun loadItemInGoogleMap() {
        val place = item.place ?: item.value?.place
        val name = item.name ?: item.value?.name

        place?.let { locationData ->
            binding.googleMapContent.visibility = View.VISIBLE
            mMap?.let { googleMap ->
                val location = LatLng(locationData.lat, locationData.long)
                googleMap.addMarker(
                    MarkerOptions().position(location).title(name)
                )
                googleMap.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(location, 15f)
                )
            }
        } ?: run {
            binding.googleMapContent.visibility = View.GONE
        }
    }

    private fun setupGoogleMap() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }

    private fun deleteItem() {
        val idToDelete = item.id ?: item.value?.id ?: return
        CoroutineScope(Dispatchers.IO).launch {
            val result = safeApiCall { RetrofitClient.apiService.deleteItem(idToDelete) }

            withContext(Dispatchers.Main) {
                when (result) {
                    is Result.Success -> handleSuccessDelete()
                    is Result.Error -> {
                        Toast.makeText(
                            this@ItemDetailActivity,
                            "Erro ao deletar o item",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private fun editItem() {
        val idToUpdate = item.id ?: item.value?.id ?: return
        
        // Mantém a estrutura que a API espera enviar (plana ou com value conforme necessário)
        // Aqui criamos um objeto plano com os novos dados
        val updatedItem = Item(
            id = idToUpdate,
            name = binding.name.text.toString(),
            year = binding.year.text.toString(),
            licence = binding.licence.text.toString(),
            imageUrl = item.imageUrl ?: item.value?.imageUrl,
            place = item.place ?: item.value?.place
        )
        
        CoroutineScope(Dispatchers.IO).launch {
            val result = safeApiCall {
                RetrofitClient.apiService.updateItem(idToUpdate, updatedItem)
            }

            withContext(Dispatchers.Main) {
                when (result) {
                    is Result.Success -> {
                        Toast.makeText(this@ItemDetailActivity, R.string.success_update, Toast.LENGTH_LONG).show()
                        finish()
                    }
                    is Result.Error -> {
                        Toast.makeText(this@ItemDetailActivity, R.string.error_update, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun handleSuccessDelete() {
        Toast.makeText(this, "Item deletado com sucesso", Toast.LENGTH_LONG).show()
        finish()
    }

    companion object {
        const val ARG_ID = "arg_id"

        fun newIntent(context: Context, itemId: String): Intent {
            return Intent(context, ItemDetailActivity::class.java).apply {
                putExtra(ARG_ID, itemId)
            }
        }
    }
}

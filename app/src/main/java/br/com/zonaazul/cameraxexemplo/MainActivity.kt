package br.com.zonaazul.cameraxexemplo

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import br.com.zonaazul.cameraxexemplo.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // inflar a activity
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnOpenCamera.setOnClickListener{
            // solicitar permissões de câmera
            cameraProviderResult.launch(android.Manifest.permission.CAMERA)
        }
    }

    private val cameraProviderResult = registerForActivityResult(ActivityResultContracts.RequestPermission()){
        if(it){
            abrirTelaDePreview()
        }else{
            Snackbar.make(binding.root, "Não há permissões de Câmera!", Snackbar.LENGTH_INDEFINITE).show()
        }
    }

    private fun abrirTelaDePreview(){
        //Navegar para outra activity
        val intentCameraPreview = Intent(this, CameraPreviewActivity::class.java)
        startActivity(intentCameraPreview)
    }
}
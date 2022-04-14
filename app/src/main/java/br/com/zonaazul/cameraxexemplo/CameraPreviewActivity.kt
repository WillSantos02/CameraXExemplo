package br.com.zonaazul.cameraxexemplo

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.hardware.camera2.CameraManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import br.com.zonaazul.cameraxexemplo.databinding.ActivityCameraPreviewBinding
import com.google.common.util.concurrent.ListenableFuture
import java.io.File
import java.lang.Exception
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraPreviewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCameraPreviewBinding

    // processamento de imagem (não permitir ou controlar melhor o estado do driver da camera)
    private lateinit var cameraProviderFuture : ListenableFuture<ProcessCameraProvider>

    // para selecionar a camera
    private lateinit var cameraSelector : CameraSelector

    // imagem capturada
    private var imageCapture: ImageCapture? = null

    // executor de threads separada
    private lateinit var imgCaptureExecutorService: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCameraPreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        imgCaptureExecutorService = Executors.newSingleThreadExecutor()

        // chamar metodo startCamera
        starCamera()

        // atribuir função ao botão para tirar foto
        binding.btnTakePhoto.setOnClickListener {
            takePhoto()
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                blinkPreview()
            }
        }

        binding.btnReturn.setOnClickListener{
            voltarTelaDeInicio()
        }
    }

    private fun voltarTelaDeInicio(){
        //Navegar para outra activity
        val intentMainActivity = Intent(this, MainActivity::class.java)
        startActivity(intentMainActivity)
    }

    private fun starCamera(){

        cameraProviderFuture.addListener({

            imageCapture = ImageCapture.Builder().build()

            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.cameraPreview.surfaceProvider)
            }

            try{
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this,cameraSelector, preview, imageCapture)
            } catch (e: Exception){
                Log.e("CameraPreview", "Falha ao abrir a Câmera.")
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto(){
        // metodo de tirar foto
        imageCapture?.let {
            //nome do arquivo para gravar
            val fileName = "FOTO_JPEG_${System.currentTimeMillis()}.jpeg"
            val file = File(externalMediaDirs[0], fileName)

            val outputFileOptions = ImageCapture.OutputFileOptions.Builder(file).build()

            it.takePicture(
                outputFileOptions,
                imgCaptureExecutorService,
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        Log.i("CameraPreview", "Imagem salva em: ${file.toUri()}")
                    }

                    override fun onError(exception: ImageCaptureException) {
                        Toast.makeText(binding.root.context, "Erro ao salvar foto.", Toast.LENGTH_LONG).show()
                        Log.e("CameraPreview", "Exceção ao gravar arquivo da foto: $exception")
                    }
                })
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun blinkPreview(){
        // Piscar a tela
        binding.root.postDelayed({
            binding.root.foreground = ColorDrawable(Color.WHITE)
            binding.root.postDelayed({
                binding.root.foreground = null
            },50)
        },100)
    }
}
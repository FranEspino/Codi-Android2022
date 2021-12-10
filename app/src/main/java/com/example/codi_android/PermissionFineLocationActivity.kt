package com.example.codi_android
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.codi_android.objects.Permissions.hasFineLocationPermission
import com.example.codi_android.objects.Permissions.requestFineLocationPermission
import com.vmadalin.easypermissions.EasyPermissions


class PermissionFineLocationActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks{

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fine_permission)
        val btn_finelocation = findViewById<Button>(R.id.btn_finelocation)
        btn_finelocation.setOnClickListener{
          if(hasFineLocationPermission(this)){
              val intent = Intent(this, PermissionBackgroundActivity::class.java)
              startActivity(intent)
          }else{
               requestFineLocationPermission(this)
          }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode,permissions,grantResults, this)
    }


    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        if(EasyPermissions.somePermissionPermanentlyDenied(this,perms[0])){
            alertFineLocation()
        }else{
            requestFineLocationPermission(this)
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        val intent = Intent(this, PermissionBackgroundActivity::class.java)
        startActivity(intent)
    }

    fun alertFineLocation(){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Codi necesita los permisos de ubicacion")
        builder.setMessage("Para que la aplicación funcione correctamente necesitamos permisos de ubicacion, abre la pantalla de configuración de la aplicación para modificar los permisos de la aplicación.")
        builder.setPositiveButton(android.R.string.yes) { dialog, which ->
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val uri: Uri = Uri.fromParts("package", packageName, null)
            intent.data = uri
            startActivity(intent)
        }
        builder.setNegativeButton(android.R.string.no) { dialog, which ->
            Toast.makeText(this, "Codi no iniciara sino accedes los permisos", Toast.LENGTH_LONG).show()
        }
        builder.show()
    }

}
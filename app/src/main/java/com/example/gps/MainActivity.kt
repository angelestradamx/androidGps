package com.example.gps

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.gps.Tools.PermissionsApplications
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import com.google.android.gms.tasks.OnSuccessListener
import kotlinx.android.synthetic.main.activity_main.*


/*
Se agregaron las siguientes lineas  en gradle y manifest:

build.gradle
    implementation 'com.google.android.gms:play-services-location:17.0.0'

    **Agrega el Google Play Service de reconocimiento de ubicación y actividad.

AndroidManifest.xml
     <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
     <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />

     **Fine Location: Proporciona ubicaciones mejores y precisas. Otorga permisos para trabajar con el proveedor de GPS y el proveedor de red.
     **Coarse location: Proporciona ubicaciones menos precisas. Otorga permisos para trabajar solo con el proveedor de red.
*/


class MainActivity : AppCompatActivity()
    ,GoogleApiClient.ConnectionCallbacks
    ,GoogleApiClient.OnConnectionFailedListener
    ,com.google.android.gms.location.LocationListener{


    private val  UPDATE_INTERVAL = (3*1000).toLong()
    private val  FASTEST_INTERVAL:Long = 2000
    private  var location:Location?= null

    private var locationRequest: LocationRequest?= null
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationManager:LocationManager

    /*
        Se utiliza para recibir notificaciones de FusedLocationProviderApi cuando la ubicación
        del dispositivo ha cambiado o ya no se puede determinar.
        Se tiene que usar: com.google.android.gms.location.LocationListener
     */
    private var locationCallback: LocationCallback = object : LocationCallback(){
        override fun onLocationResult(p0: LocationResult?) {
            onLocationChanged(p0?.lastLocation)
        }
    }


    /*
        Si está habilitado  GPS_PROVIDER o NETWORK_PROVIDER devuelve true
    */
    private val isLocationEnabled:Boolean
        get() {
            locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                    locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        }


    /*
     Puede usar el objeto GoogleApiClient ("Cliente API de Google") para acceder a las API de Google
     proporcionadas en la biblioteca de servicios de Google Play (como Google Sign-In, Games y Drive).
     El cliente API de Google proporciona un punto de entrada común a los servicios de Google Play y
     gestiona la conexión de red entre el dispositivo del usuario y cada servicio de Google.
     */

    private  val REQUEST_CODE = 1
    private val persmission = PermissionsApplications(this@MainActivity)
    private val listPermissions = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION)

    /** GoogleApiClient.ConnectionCallbacks **/
    private var googleApiClient:GoogleApiClient?= null
    /****************************************/


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /** GoogleApiClient.ConnectionCallbacks **/
        /*      Crear una intancia  de  GoogleApiClient
         Después de vincular su proyecto a la biblioteca de servicios de Google Play.
         La clase GoogleApiClient.Builder proporciona métodos que le permiten especificar
         las API de Google que desea usar y los ámbitos deseados de OAuth 2.0.

         Para comenzar una conexión administrada automáticamente, debe especificar una
         implementación para que la interfaz OnConnectionFailedListener reciba errores
         de conexión irresolubles. Cuando su instancia de GoogleApiClient intente conectarse
         a las API de Google, mostrará automáticamente la interfaz de usuario para intentar
         solucionar cualquier falla de conexión que se pueda resolver (por ejemplo, si los
         servicios de Google Play deben actualizarse). Si se produce un error que no se puede
         resolver, recibirá una llamada a onConnectionFailed() .

          También puede especificar una implementación opcional para la interfaz ConnectionCallbacks
          si su aplicación necesita saber cuándo se establece o suspende la conexión administrada
          automáticamente. Por ejemplo, si su aplicación realiza llamadas para escribir datos
          en las API de Google, estas deben invocarse solo después de que se haya llamado
          al método onConnected().
        */

        googleApiClient = GoogleApiClient.Builder(this).apply {
            addConnectionCallbacks(this@MainActivity)
            addOnConnectionFailedListener(this@MainActivity)
            addApi(LocationServices.API)

        }.build()
        /****************************************/


        if(!persmission.hasPermissions(listPermissions)){
            persmission.acceptPermission(listPermissions,REQUEST_CODE)
        }
        else{
            startLocation()
        }

    }

    /** GoogleApiClient.ConnectionCallbacks **/
    override fun onConnected(p0: Bundle?) {
        startLocation()
    }

    override fun onConnectionSuspended(p0: Int) {
        //Para reconectar si se suspende la conexión
        googleApiClient!!.connect()
    }
    /****************************************/




    /** GoogleApiClient.OnConnectionFailedListener **/
    override fun onConnectionFailed(p0: ConnectionResult) {
        /*
              Se produjo un error irresoluble y una conexión a las API de Google no se
               pudo establecer. Mostrar un mensaje de error o manejar el fracaso en silencio.
         */
    }
    /****************************************/



    /** com.google.android.gms.location.LocationListener **/
    override fun onLocationChanged(p0: Location?) {
        //Se llama cuando la ubicación cambia
        if(p0!=null) {

                txvMyLocation.text= "Latitud: ${p0.latitude} \n Longitud: ${p0.longitude}"
                Log.d("UDELP", "Latitud: ${p0.latitude}  Longitud: ${p0.longitude}")
        }
        else
        {
            txvMyLocation.text= "Location null - onLocationChanged"
            Log.d("UDELP","Location null - onLocationChanged")
        }

    }
    /****************************************/




    /******* Method startLocation() *********/
    private fun startLocation(){

        if(isLocationEnabled) {

            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
            location = getLastlocation()

            if (location == null) {
                initLocation()
            }
            else{
                Log.d("UDELP","Location null - startLocation")
            }

        }
        else {
            Toast.makeText(this,R.string.mandatoryGPS,Toast.LENGTH_LONG).show()
            finish()
        }
    }
    /****************************************/



    /******* Method getLastlocation() *********/
    private fun getLastlocation():Location?{
        var lastLocation:Location?=null
        fusedLocationProviderClient.lastLocation.addOnSuccessListener {

            OnSuccessListener<Location>{
                if(it!=null){
                    lastLocation=it
                }
            }
        }
        return lastLocation
    }
    /****************************************/


    /******* Method initLocation() *********/

    /*
        PRIORITY
                PRIORITY_BALANCED_POWER_ACCURACY
                    Se usa para solicitar la precisión del nivel de "bloque".
                    Se considera que la precisión del nivel de bloque es de aproximadamente 100 metros de precisión.
                    El uso de una precisión gruesa como esta a menudo consume menos energía.
                PRIORITY_HIGH_ACCURACY
                    Se usa para solicitar las ubicaciones más precisas disponibles.
                    Esto devolverá la mejor ubicación disponible.
                PRIORITY_LOW_POWER
                    Se usa para solicitar la precisión de nivel de "ciudad".
                    Se considera que la precisión a nivel de ciudad es de aproximadamente 10 km de precisión.
                    El uso de una precisión gruesa como esta a menudo consume menos energía.
                PRIORITY_NO_POWER
                    Se usa  para solicitar la mejor precisión posible con cero consumo de energía adicional.
                    No se devolverán ubicaciones a menos que un cliente diferente haya solicitado actualizaciones de ubicación,
                    en cuyo caso esta solicitud actuará como un escucha pasivo de esas ubicaciones.

         FASTESTINTERVAL
                Establezca explícitamente el intervalo más rápido para las actualizaciones de ubicación, en milisegundos.

                Esto controla la velocidad más rápida a la que su aplicación recibirá actualizaciones de ubicación,
                que podrían ser más rápidas que setInterval(long) en algunas situaciones (por ejemplo, si otras
                aplicaciones están activando actualizaciones de ubicación).

                Esto permite que su aplicación adquiera ubicaciones pasivamente a un ritmo más rápido de lo que
                adquiere ubicaciones activamente, ahorrando energía.

         INTERVAL
                Establezca el intervalo deseado para las actualizaciones de ubicación activas, en milisegundos.

                El cliente de ubicación tratará activamente de obtener actualizaciones de ubicación para su
                aplicación en este intervalo, por lo que tiene una influencia directa en la cantidad de energía
                utilizada por su aplicación. Elige tu intervalo sabiamente.

                Este intervalo es inexacto. Es posible que no reciba actualizaciones en absoluto (si no hay fuentes
                de ubicación disponibles), o puede recibirlas más lentamente de lo solicitado. También puede
                recibirlos más rápido de lo solicitado (si otras aplicaciones solicitan ubicación en un intervalo más rápido).
                La tasa más rápida de que recibirá actualizaciones se puede controlar con FastestInterval.
                Por defecto, esta tasa más rápida es 6 veces la frecuencia del intervalo.

                Las aplicaciones con solo el permiso de ubicación aproximada pueden tener su intervalo silenciado silenciosamente.

                Se permite un intervalo de 0, pero no se recomienda, ya que las actualizaciones de ubicación pueden
                ser extremadamente rápidas en futuras implementaciones.

         Priority(int) e Interval(long) son los parámetros más importantes en una solicitud de ubicación.
     */

    private fun initLocation()
    {
        locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = UPDATE_INTERVAL
            fastestInterval = FASTEST_INTERVAL
        }

        /*
         FusedLocationProviderClient
            Es el punto de entrada principal para interactuar con el proveedor de ubicación fusionada.

         FusedLocationProviderClient.requestLocationUpdates(solicitud de LocationRequest,devolución de llamada de LocationCallback,Looper looper)
            Solicita actualizaciones de ubicación con una devolución de llamada en el hilo de Looper especificado.
            Este método es adecuado para los casos de uso en primer plano.
        */

        fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback, Looper.getMainLooper())
    }

    /****************************************/


    /******* Method stopLocation() *********/
    //Si se desea detener las actualizaciones de ubicación
    private fun stopLocation(){
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }
    /****************************************/

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        when(requestCode){

            REQUEST_CODE->{

                if(grantResults.size>0){

                    //Sólo se está validando el permiso ACCESS_FINE_LOCATION
                    if (grantResults[0] != PackageManager.PERMISSION_GRANTED  ) {
                        Toast.makeText(this,R.string.mandatoryPermissions,Toast.LENGTH_LONG).show()
                        finish()
                    }
                    else{
                        startLocation()
                    }

                }else{

                    Toast.makeText(this,R.string.mandatoryPermissions,Toast.LENGTH_LONG).show()
                    finish()

                }
            }

        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

}

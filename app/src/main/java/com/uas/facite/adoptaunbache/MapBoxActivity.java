package com.uas.facite.adoptaunbache;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.JsonObject;
import com.mapbox.api.geocoding.v5.GeocodingCriteria;
import com.mapbox.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;
import com.mapbox.core.exceptions.ServicesException;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Permissions;
import java.util.HashMap;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MapBoxActivity extends AppCompatActivity {
    private MapView mapView;
    private MapboxMap mapboxMap;
    private FloatingActionButton botonAgregarBache;
    //direccion a la cual enviaremos los datos del bache el cual s va a registrar
    private String WEB_SERVICE = "http://facite.uas.edu.mx/adoptaunbache/api/insertar_bache.php";
    BottomSheetBehavior bottomSheet;
    LinearLayout layout_capurarBache;
    TextView txt_direccion, txt_latitud, txt_longitud;
    ImageButton btn_camara;
    Button btn_adoptar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String key= getString(R.string.mapbox_key);
        Mapbox.getInstance(this, key);
        setContentView(R.layout.activity_map_box);
        //identificar las variables de nuestro diseno
        layout_capurarBache = (LinearLayout)findViewById(R.id.capturar_bache);
        bottomSheet = BottomSheetBehavior.from(layout_capurarBache);
        txt_direccion = (TextView)findViewById(R.id.txt_direccion);
        txt_latitud = (TextView)findViewById(R.id.txt_latitud);
        txt_longitud = (TextView)findViewById(R.id.txt_longitud);
        btn_camara = (ImageButton)findViewById(R.id.btn_camara);
        btn_adoptar = (Button)findViewById(R.id.btn_adoptar);
        btn_adoptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //obtener la imagen del boton de la camara
                Bitmap foto = ((BitmapDrawable)btn_camara.getDrawable()).getBitmap();
                //convertimos la imagen a un arreglo de bytes
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                foto.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] fotobytes = baos.toByteArray();
                //convertimos a base64 para guardarlo en la base de datos
                String fotostring = Base64.encodeToString(fotobytes, Base64.DEFAULT);
                //mandamos llamar la clase para registrar los datos, mandandole los parametros
                RegistrarBache registro = new RegistrarBache(txt_direccion.getText().toString(),txt_latitud.getText().toString(),txt_latitud.getText().toString(),fotostring);
                //ejecutamos la funcion de registrar bache
                registro.execute();
                //cerramos el bottomsheet
                bottomSheet.setState(BottomSheetBehavior.STATE_COLLAPSED);
                //limpiamos los controles del bottomsheet
                txt_direccion.setText("");
                txt_latitud.setText("");
                txt_longitud.setText("");
                btn_camara.setImageResource(R.drawable.ic_photo_camera);
                //btn_camara.setRotation(0);
                //btn_camara.setVisibility(View.VISIBLE);
            }
        });

        //funcionalidad del boton de la camara
        btn_camara.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //desplegar una alerta con opciones a realizar
                final CharSequence[] opciones = {"Tomar fotografia", "Desde galeria", "Cancelar"};
                AlertDialog.Builder alerta = new AlertDialog.Builder(MapBoxActivity.this);
                alerta.setTitle("Agregar fotografia del bache");
                alerta.setItems(opciones, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //programar la funcionalidad de las opciones
                        if (opciones[which].equals("Tomar fotografia")){
                            //verificar el SDK del telefono done se esta ejecutando la app
                            if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
                                //verificar si ya tiene permisos para la cmara
                                if(ContextCompat.checkSelfPermission(MapBoxActivity.this, Manifest.permission.CAMERA)== PackageManager.PERMISSION_GRANTED){
                                    Intent camara = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                    startActivityForResult(camara,1);
                                }else {
                                    //solicitar permisos a la camara en caso de no los tenga
                                    ActivityCompat.requestPermissions(MapBoxActivity.this, new String[]{Manifest.permission.CAMERA}, 507);
                                    return;
                                }
                            }else{
                                //si es android 5 o menor se abre directo
                                Intent camara = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                startActivityForResult(camara, 1);
                            }
                        }
                        else if (opciones[which].equals("Desde galeria")){
                            //solicitamos permisos para aceder a la galeria
                            if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
                                if(ContextCompat.checkSelfPermission(MapBoxActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED){
                                    Intent galeria = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                    startActivityForResult(galeria,2);
                                }else{
                                    ActivityCompat.requestPermissions(MapBoxActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},507);
                                    return;
                                }
                            }else{
                                //si es android 5 o menor se abre directo
                                Intent galeria = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                startActivityForResult(galeria,2);
                            }
                        }
                    }
                });
                alerta.show();
            }
        });
        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull final MapboxMap mapboxMap) {
                MapBoxActivity.this.mapboxMap = mapboxMap;
                mapboxMap.setStyle(Style.TRAFFIC_NIGHT, new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                        //cargar los baches
                        try {
                            style.addSource(new GeoJsonSource("GEOJSON_PUNTOS", new URI("http://facite.uas.edu.mx/adoptaunbache/api/getlugares.php")));
                        } catch (URISyntaxException e) {
                            Log.i("ERROR GEOJSON:",e.toString());
                        }
                        // creamos el icono personalizado para nuestro puntos
                        Bitmap icono = BitmapFactory.decodeResource(getResources(), R.drawable.marcador);
                        //agregar al estilo del mapa
                        style.addImage("BACHE_ICONO", icono);
                        //crear layer con los datos de GEOJSON
                        SymbolLayer capaBaches = new SymbolLayer("BACHES", "GEOJSON_PUNTOS");
                        //asignamos el icono personalizado a la capa del bache
                        capaBaches.setProperties(PropertyFactory.iconImage("BACHE_ICONO"));
                        //asignamosla capa al mapa
                        style.addLayer(capaBaches);
                        // Map is set up and the style has loaded. Now you can add data or make other map adjustments
                        com.mapbox.mapboxsdk.geometry.LatLng coor= new LatLng(24.782539,-107.3779);
                        CameraPosition camara = new CameraPosition.Builder()
                                .zoom(14)
                                .target(coor)
                                .bearing(0)
                                .tilt(0)
                                .build();
                        //mapboxMap.moveCamera(CameraUpdateFactory.newCameraPosition(camara));
                        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(camara),2000);
                        //mapboxMap.moveCamera(CameraUpdateFactory.zoomTo(9));
                        //cargar el pin en el centro del mapa
                        ImageView MarcadorPin;
                        MarcadorPin = new ImageView(MapBoxActivity.this);
                        MarcadorPin.setImageResource(R.drawable.ic_pinwarning);
                        //posicionar en el centro del mapa
                        FrameLayout.LayoutParams parametros = new FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                Gravity.CENTER);
                        //aplicamos
                        MarcadorPin.setLayoutParams(parametros);
                        mapView.addView(MarcadorPin);
                        //identificamos el boton
                        botonAgregarBache = (FloatingActionButton)findViewById(R.id.btnAgregarBache);
                        botonAgregarBache.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //obtener las coordenadas del marcador pin
                                final LatLng coordenadas = mapboxMap.getCameraPosition().target;
                                //mandamos un mensajito en una ventanita bonita
                                /*new SweetAlertDialog(MapBoxActivity.this, SweetAlertDialog.NORMAL_TYPE)
                                        .setTitleText("Coordenadas del marcador")
                                        .setContentText("Latitud: "+coordenadas.getLatitude() +"      Longitud: "+ coordenadas.getLongitude())
                                        .show();*/
                                //obtener la direccion con el metodo le enviamos el view con el boton
                                ObtenerDireccion(v);
                                //mostrar el layout de capturar bache
                                bottomSheet.setState(BottomSheetBehavior.STATE_EXPANDED);
                                txt_latitud.setText("Latitud: "+ coordenadas.getLatitude());
                                txt_longitud.setText("Longitud: "+ coordenadas.getLongitude());
                            }
                        });
                    }
                });
            }
        });

    }
    //metodo para guardar la fotografia tomada por la camara
    private File savebitmap(Bitmap bmp) {
        String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
        OutputStream outStream = null;
        // String temp = null;
        File file = new File(extStorageDirectory, "temp.png");
        if (file.exists()) {
            file.delete();
            file = new File(extStorageDirectory, "temp.png");
        }
        try {
            outStream = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, outStream);
            outStream.flush();
            outStream.close();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return file;
    }
    //Manejamos los resultados
    public void onActivityResult(int requestCode, int resultCode, Intent intent){
        MapBoxActivity.super.onActivityResult(requestCode,resultCode, intent);
        //si se toma una fotografia
        if (requestCode ==1){
            Bitmap foto = (Bitmap)intent.getExtras().get("data");
            //se genero un nuevo bitmap porque en mi celular salia rotado
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            Bitmap fotor = Bitmap.createBitmap(foto, 0, 0, foto.getWidth(), foto.getHeight(), matrix, true);
            Drawable fotobache = new BitmapDrawable(fotor);
            btn_camara.setImageDrawable(fotobache);
            //btn_camara.setRotation(90);
        }else if(requestCode==2){
            Uri rutaSelct = intent.getData();
            String[] rutafoto = {MediaStore.Images.Media.DATA};
            Cursor cursor = MapBoxActivity.this.getApplicationContext().getContentResolver().query(rutaSelct, rutafoto,null,null,null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(rutafoto[0]);
            String archivoFoto = cursor.getString(columnIndex);
            cursor.close();
            Bitmap foto = (BitmapFactory.decodeFile(archivoFoto));
            //se genero un nuevo bitmap porque en mi celular salia rotado
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            Bitmap fotor = Bitmap.createBitmap(foto, 0, 0, foto.getWidth(), foto.getHeight(), matrix, true);
            Drawable fotodrawable = new BitmapDrawable(foto);
            btn_camara.setImageDrawable(fotodrawable);
            //btn_camara.setRotation(90);
        }
    }
    //obtener la direccion en cuanto a la latitud y longitud
    public void ObtenerDireccion(final View view)
    {
        try {
            //obtener las coordenadas del pin en el mapa
            final LatLng coordenadas = mapboxMap.getCameraPosition().target;
            final Point punto = Point.fromLngLat(coordenadas.getLongitude(), coordenadas.getLatitude());
            //utilizar los servicios de mapbox para geocodificar las coordenadas a direcciones
            String key = getString(R.string.mapbox_key);
            MapboxGeocoding geoservicio = MapboxGeocoding.builder()
                    .accessToken(key)
                    .query(Point.fromLngLat(punto.longitude(), punto.latitude()))
                    .geocodingTypes(GeocodingCriteria.TYPE_ADDRESS)
                    .build();
            //ejecutar el servicio con los parametros que establecimos
            geoservicio.enqueueCall(new Callback<GeocodingResponse>() {
                @Override
                public void onResponse(Call<GeocodingResponse> call, Response<GeocodingResponse> response) {
                    //validacion para ver si encuentra una direccion
                    if (response.body() != null) {
                        List<CarmenFeature> resultados = response.body().features();
                        //obtenemos la direccion
                        CarmenFeature direccion = resultados.get(0);
                        //mostramos la direccion obtenida con una snackpbar
                        txt_direccion.setText(direccion.placeName());
                        //Snackbar.make(view, "Direccion: " + direccion.placeName(), Snackbar.LENGTH_LONG).show();
                    }
                }
                @Override
                public void onFailure(Call<GeocodingResponse> call, Throwable t) {
                    //mostramos que no hay direcciones de esas coordenadas
                    txt_direccion.setText("Sin Direccion");
                    //Snackbar.make(view, "Sin direcciones ", Snackbar.LENGTH_LONG).show();
                }
            });
        }catch (ServicesException serviceException){
            Log.i("err de servicio Mapbox", serviceException.toString());
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }
    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }
    //clase que se encarga de registrar el bache en el WEB SERVICE
    class RegistrarBache extends AsyncTask<Void, Void, String>{
        //crear las variables de los parametros que se ocupam em el web service
        String direccion, latitud, longitud, foto;
        //creamos un constructor
        RegistrarBache(String direccion, String latitud, String longitud, String foto){
            this.direccion = direccion;
            this.latitud = latitud;
            this.longitud = longitud;
            this.foto = foto;
        }
        @Override
        protected String doInBackground(Void... voids) {
            //crear un objeto de la clase RecuestHandler
            RequestHandler requestHandler = new RequestHandler();
            //creamos un hashmap con los parametros que se envian
            HashMap<String, String> parametros = new HashMap<>();
            parametros.put("nombre",direccion);
            parametros.put("lat",latitud);
            parametros.put("lon", longitud);
            parametros.put("img",foto);
            //retornamos la respuesta que nos regreso el WEB Service
            return requestHandler.sendPostRequest(WEB_SERVICE, parametros);
        }
        @Override
        protected void onPostExecute(String respuesta){
            super.onPostExecute(respuesta);
            try {
                JSONObject object = new JSONObject(respuesta);
                int status = object.getInt("status");
                if(status==1){
                    new SweetAlertDialog(MapBoxActivity.this, SweetAlertDialog.SUCCESS_TYPE)
                            .setTitleText("Registro nuevo")
                            .setContentText(object.getString("message"))
                            .show();

                }else {
                    new SweetAlertDialog(MapBoxActivity.this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Ups:( algo malio sal")
                            .setContentText(object.getString("message"))
                            .show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }


        }
    }
}


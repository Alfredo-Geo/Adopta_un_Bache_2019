package com.uas.facite.adoptaunbache;

import androidx.fragment.app.FragmentActivity;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.data.geojson.GeoJsonFeature;
import com.google.maps.android.data.geojson.GeoJsonLayer;
import com.google.maps.android.data.geojson.GeoJsonPointStyle;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class MapsActivity2 extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps2);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng Culiacan = new LatLng(24.8206351, -107.380578);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        new CargarBaches().execute("http://facite.uas.edu.mx/adoptaunbache/api/getlugares.php");

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(Culiacan, 12));
    }
    private class CargarBaches extends AsyncTask<String, String, String> {
        SweetAlertDialog pDialog = new SweetAlertDialog(MapsActivity2.this, SweetAlertDialog.PROGRESS_TYPE);
        protected void onPreExecute() {
            super.onPreExecute();
            //cargar progressbar
            pDialog.getProgressHelper().setBarColor(Color.parseColor("#B87624"));
            pDialog.setTitleText("Cargando Baches...");
            pDialog.setCancelable(false);
            pDialog.show();
        }
        protected String doInBackground(String... params) {
            //CREAMOS UN OBJETO DE LA CLASE RequestHandler
            RequestHandler requestHandler = new RequestHandler();
            //Crear los parametros que se enviaran al web service
            HashMap<String, String> parametros = new HashMap<>();
            //retornamos la respuesta del web service
            return requestHandler.sendPostRequest(params[0], parametros);
        }
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            //quitar la barra
            pDialog.dismiss();
            //convertir la respuesta del web service a un objeto JSON
            try {
                JSONObject lugares = new JSONObject(result);
                //Creamos el icono personalizado para nuestros marcadores (puntos)
                BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.marcador);
                //Creamos la capa
                GeoJsonLayer layer = new GeoJsonLayer(mMap, lugares);
                //Creamos un estilo para los puntos
                GeoJsonPointStyle pointStyle = new GeoJsonPointStyle();
                //agregamos la capa al mapa
                layer.addLayerToMap();
                //por cada punto mostramos el nombre y aplicamos el icono personalizado
                for (GeoJsonFeature feature : layer.getFeatures()) {
                    pointStyle.setSnippet(feature.getProperty("nombre"));
                    pointStyle.setIcon(icon);
                    feature.setPointStyle(pointStyle);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}

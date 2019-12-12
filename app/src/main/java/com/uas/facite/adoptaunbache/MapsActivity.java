package com.uas.facite.adoptaunbache;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import cn.pedant.SweetAlert.SweetAlertDialog;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

public class MapsActivity extends SupportMapFragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        getMapAsync(MapsActivity.this);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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
        LatLng Casa = new LatLng(24.8478548, -107.360899);
        //CARGAR LOS PUNTOS DEL GeoJson de nuestra Api
        new CargarBaches().execute("http://facite.uas.edu.mx/adoptaunbache/api/getlugares.php");

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(Culiacan, 12));
    }

    private class CargarBaches extends AsyncTask<String, String, String> {
        SweetAlertDialog pDialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.PROGRESS_TYPE);
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
                BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.warning_circle);
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

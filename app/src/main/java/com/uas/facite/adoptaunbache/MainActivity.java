package com.uas.facite.adoptaunbache;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private NavigationView navegacion;
    private ImageButton btn_menu;
    private DrawerLayout drawer;
    MenuItem btn_gmap, btn_osm, btn_mb, btn_ag;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        drawer = (DrawerLayout)findViewById(R.id.drawer_layout);
        btn_menu = (ImageButton)findViewById(R.id.btn_menu);
        navegacion = (NavigationView)findViewById(R.id.nav_view);
        navegacion.setNavigationItemSelectedListener(this);
        btn_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //ver si el drawer se encuentra abierto
                if(drawer.isDrawerOpen(Gravity.LEFT))
                    drawer.closeDrawer(Gravity.LEFT);
                else
                    drawer.openDrawer(Gravity.LEFT);
            }
        });

//        btn_gmap = (MenuItem)findViewById(R.id.nav_google);
//        btn_osm = (ImageButton)findViewById(R.id.nav_OSM);
//        btn_mb = (ImageButton)findViewById(R.id.nav_mapbox);
//        btn_ag = (ImageButton)findViewById(R.id.nav_arcgis);
//        btn_gmap.setOnClickListener(gmapListener);
//        btn_osm.setOnClickListener(osmListener);
//        btn_mb.setOnClickListener(mbListener);
//        btn_ag.setOnClickListener(agListener);
    }
    private View.OnClickListener gmapListener= new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            abrirGmap();
        }
    };
    private View.OnClickListener osmListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            abrirOsm();
        }
    };
    private View.OnClickListener mbListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            abrirMb();
        }
    };
    private View.OnClickListener agListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            abrirAg();
        }
    };

    public void abrirGmap(){
        Intent ventana = new Intent(this,MapsActivity2.class);
        startActivity(ventana);
    }
    public void abrirOsm(){
        Intent ventana = new Intent(this,OSMActivity.class);
        startActivity(ventana);
    }
    public void abrirMb(){
        Intent ventana = new Intent(this,MapBoxActivity.class);
        startActivity(ventana);
    }
    public void abrirAg(){
        Intent ventana = new Intent(this, ArcGisActivity.class);
        startActivity(ventana);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        //obtenemos el id del elemento del menu
        int id = menuItem.getItemId();
        switch (id){
            case R.id.nav_google:
                abrirGmap();
                break;
            case R.id.nav_mapbox:
                abrirMb();
                break;
            case R.id.nav_OSM:
                abrirOsm();
                break;
            case R.id.nav_arcgis:
                abrirAg();
                break;
            default:
                return true;

        }
        return true;
    }
}

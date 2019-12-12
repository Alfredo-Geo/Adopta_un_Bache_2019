package com.uas.facite.adoptaunbache;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class RegistroActivity extends AppCompatActivity {
    EditText nombre,usuario,password;
    Button btn_registrar;
    String URL_WEB_SERVICE = "http://facite.uas.edu.mx/adoptaunbache/api/registro_usuario.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);
        nombre = (EditText)findViewById(R.id.txt_nombre);
        usuario = (EditText)findViewById(R.id.txt_usuarior);
        password = (EditText)findViewById(R.id.txt_passwordr);
        btn_registrar = (Button)findViewById(R.id.btn_loginr);
        btn_registrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registrarUsuario();
            }
        });
    }
    //metodo para realizar el registro en segundo plano
    private void registrarUsuario() {
        //obtenemos los valores de los editText
        String nom = nombre.getText().toString();
        String usu = usuario.getText().toString();
        String pass = password.getText().toString();
        RegistroUsuario registroObject = new RegistroUsuario(nom, usu, pass);
        registroObject.execute();
    }

    //Clase en segundo plano
    class RegistroUsuario extends AsyncTask<Void,Void,String>{
        String nombre, usuario, password;

        RegistroUsuario(String nombre, String usuario,String password) {
            this.nombre = nombre;
            this.usuario = usuario;
            this.password = password;
        }
        @Override
        protected String doInBackground(Void... voids) {
            RequestHandler requestHandler = new RequestHandler();
            HashMap<String, String> parametros = new HashMap<>();
            parametros.put("nombre", nombre);
            parametros.put("usuario", usuario);
            parametros.put("pass", password);
            return requestHandler.sendPostRequest(URL_WEB_SERVICE, parametros);
        }
        @Override
        protected void onPostExecute(String s){
            super.onPostExecute(s);
            try {
                JSONObject obj = new JSONObject(s);
                if(obj.getInt("status")==1){
                    new SweetAlertDialog(RegistroActivity.this, SweetAlertDialog.SUCCESS_TYPE)
                            .setTitleText("Registrado")
                            .setContentText(obj.getString("message"))
                            .show();
//                    Intent ventana = new Intent(RegistroActivity.this,LoginActivity.class);
//                    startActivity(ventana);
                }
                else {
                    new SweetAlertDialog(RegistroActivity.this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Ups!")
                            .setContentText(obj.getString("message"))
                            .show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }
    public void RegresarLogin(View v){
        Intent ventana = new Intent(this,LoginActivity.class);
        startActivity(ventana);
    }
}

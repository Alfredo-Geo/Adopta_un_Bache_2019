package com.uas.facite.adoptaunbache;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {
    //Creamos las variables para los controles que usaremos
    private EditText txt_user, txt_passw;
    Button btn_login;
    ProgressDialog progressDialog;
    String URL_WEB_SERVICE = "http://facite.uas.edu.mx/adoptaunbache/api/get_usuarios.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //identificar los controles del layout
        txt_user = (EditText)findViewById(R.id.txt_usuario);
        txt_passw = (EditText)findViewById(R.id.txt_password);
        btn_login = (Button)findViewById(R.id.cirLoginButton);
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hacerLogin();
            }
        });
    }
    class UsuarioLogin extends AsyncTask<Void, Void, String>{
        //ProgressBar barra;
        String usuario, password;
        UsuarioLogin(String usuario, String password){
            this.usuario = usuario;
            this.password = password;
        }
        @Override
        protected void onPreExecute(){
            super.onPreExecute();
        }
        @Override
        protected void onPostExecute(String s){
            super.onPostExecute(s);
            try{
                JSONObject obj = new JSONObject(s);
                if(obj.getInt("status")==0){
                    Toast.makeText(getApplicationContext(), obj.getString("message"), Toast.LENGTH_LONG).show();
                    Intent ventana = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(ventana);
                }
                else{
                    Toast.makeText(getApplicationContext(), obj.getString("message"), Toast.LENGTH_LONG).show();
                }
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
        @Override
        protected String doInBackground(Void... voids) {
            RequestHandler requestHandler = new RequestHandler();
            HashMap<String, String> parametros = new HashMap<>();
            parametros.put("usuario", usuario);
            parametros.put("password",password);
            return requestHandler.sendPostRequest(URL_WEB_SERVICE, parametros);
        }

    }
    public void Abrirregistro(View v){
        Intent intent = new Intent(this, RegistroActivity.class);
        startActivity(intent);
    }
    public void hacerLogin(){
        //obtener los valores de los editText
        String usuario= txt_user.getText().toString();
        String passw = txt_passw.getText().toString();
        UsuarioLogin ul= new UsuarioLogin(usuario,passw);
        ul.execute();
    }
}

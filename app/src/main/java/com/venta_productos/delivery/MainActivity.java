package com.venta_productos.delivery;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.FirebaseDatabase;
import com.venta_productos.delivery.servicio.servicio_del_pedido_activo;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    Toolbar toolbar;
    FloatingActionButton fab;
    boolean agregar_o_modificar_direccion_boolean = true;
    AlertDialog alertDialog;
    int monto_total, monto_delivery, monto_comision_app;
    String usuario;
    SharedPreferences sharpref;
    Context contex;

    //de guardar delivery
    public String que_delivery_usar;
    public String delivery_elegido;
    public GeoPoint mi_gps;
    public HashMap<String, String> hash_multiple_delivery_mainact = new HashMap<>();

    //ubicacion para mandar
    public String ubicacion_elegida;
    public String gps_elegido;
    public String ubicacion_calle_y_altura;
    int precio_productos;

    //agregar gps de la ubicacion en los mapas
    String str_elegido_en_el_mapa = null;
    boolean boolean_agregar_gps_en_el_mapa = false;
    String str_conservar_datos = null;
    String str_elegir_gps_en_el_mapa_donde_cargar_los_datos_en_los_et = null;

    //solo mostrar las areas del mapa
    boolean boolean_solo_mostrar_las_areas_del_mapa = false;

    String TAG = "asdf";

    //ver que en el carrito no esten los negocios no disponibles
    ArrayList<String> array_mainact_negocios_disponibles = new ArrayList<>();
    boolean boolean_main_ver_negocios_sin_coneccion_igualmente = false;

    //pasar el modo de pago a guardar pedido
    public String str_main_modo_de_pago;
    public double double_interes_aplicado;

    //contiene pedido llevar al menu principal
    public boolean ir_a_ver_progreso_o_al_menu_principal;

    //ingreso a la app ubicacion
    boolean boolean_ingreso_a_la_app_sin_ubicacion = true;

    boolean boolean_ver_negocios_igualmente = false;

     ArrayList<String> array_mostar_negocios_previamente_cargados = null;

    Drawable drawable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_activity_main);

        contex = this;
        sharpref = getSharedPreferences("usar_app", MODE_PRIVATE);
        String shared_ubicacion = sharpref.getString("ubicacion", null);
        String shared_ubicacion_gps = sharpref.getString("ubicacion_gps", null);
        String shared_ubicacion_calle_y_altura = sharpref.getString("ubicacion_calle_y_altura", null);
        usuario = sharpref.getString("usuario", null);

        if (shared_ubicacion != null) {
            ubicacion_elegida = shared_ubicacion;
            gps_elegido = shared_ubicacion_gps;
            ubicacion_calle_y_altura = shared_ubicacion_calle_y_altura;
        }

        String string_guardado = getIntent().getStringExtra("puntuar_pedido"); // obtiene el put extra del intent de la notificacion

        if (string_guardado != null) { // si es null no se guardo nada

            if (string_guardado.equals("c_guardar_pedidos_ver_pedidos")) {
                main_cambiar_fragment("c_guardar_pedidos_ver_pedidos");
            } else {
                String puntuar_pedido = sharpref.getString("puntuar_pedido", null);
                if (puntuar_pedido != null) {
                    metodos.method_puntuar_pedido(this, puntuar_pedido,usuario);

                }
                FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
                tx.replace(R.id.content_main, new c_principal());
                tx.commit();
            }
        } else {

            String puntuar_pedido = sharpref.getString("puntuar_pedido", null);
            if (puntuar_pedido != null) {
                metodos.method_puntuar_pedido(this, puntuar_pedido,usuario);
            }

            FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
            tx.replace(R.id.content_main, new c_principal());
            tx.commit();
        }

        toolbar = findViewById(R.id.toolbar);
        //toolbar.setNavigationIcon(R.drawable.ic_toolbar_estrella);

        drawable = getResources().getDrawable(R.drawable.ic_toolbar_tringualo);
        toolbar.setOverflowIcon(drawable);
        settitletoolbar("Negocios");
        setSupportActionBar(toolbar);

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                method_ir_al_carrito();
            }
        });
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

    }

    private void method_ir_al_carrito() {
        if (ubicacion_elegida != null) {
            if (array_mainact_negocios_disponibles != null) {
                if (array_mainact_negocios_disponibles.size() != 0) {
                    ir_a_ver_progreso_o_al_menu_principal = true;
                    main_cambiar_fragment("c_carrito");
                } else {
                    Toast.makeText(MainActivity.this, "deliverys o negocios no disponibles", Toast.LENGTH_LONG).show();
//                    metodos.alertdialog_doble_accion(this, "deliverys o negocios no disponibles", "el servicio no se encuentra disponible continuar igualmente?", "continuar", "cerrar", "c_carrito", null);
                    ir_a_ver_progreso_o_al_menu_principal = true;
                    main_cambiar_fragment("c_carrito");
                }
            } else {
                Toast.makeText(MainActivity.this, "deliverys o negocios no disponibles", Toast.LENGTH_LONG).show();
//                metodos.alertdialog_doble_accion(this, "deliverys o negocios no disponibles", "el servicio no se encuentra disponible continuar igualmente?", "continuar", "cerrar", "c_carrito", null);
                ir_a_ver_progreso_o_al_menu_principal = true;
                main_cambiar_fragment("c_carrito");
            }
        } else {
            Toast.makeText(MainActivity.this, "Ubicacion para enviar el pedido no encontrado", Toast.LENGTH_LONG).show();
        }
    }

    public boolean boolean_todo_correcto_para_pedir() {
        boolean todo_correcto;
        if (ubicacion_elegida != null) {
            if (array_mainact_negocios_disponibles != null) {
                todo_correcto = array_mainact_negocios_disponibles.size() != 0;
            } else {
                todo_correcto = false;
            }
        } else {
            todo_correcto = false;
        }
        return todo_correcto;
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {

            if (!ir_a_ver_progreso_o_al_menu_principal) {
                main_cambiar_fragment("c_principal");
                ir_a_ver_progreso_o_al_menu_principal = true;
            } else { // poner otro else if, que sea que si estoy en el fragment principal se salga ( que en el on pause o on destroy el boolean vuelva al estado normal)
                super.onBackPressed();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        //noinspection SimplifiableIfStatement
        if (id == R.id.toolbal_menu_opciones) {
            if (usuario != null) {
                main_cambiar_fragment("c_editar_usuario_y_log_out");

            } else {
                metodos.alertdialog_ingresar_a_la_cuenta(this);
            }
            return true;
        } else if (id == R.id.toolbal_menu_ver_carrito) {
            if (usuario != null) {
                method_ir_al_carrito();

            } else {
                metodos.alertdialog_ingresar_a_la_cuenta(this);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        String usuario = sharpref.getString("usuario", null);

        if (id == R.id.nav_opciones) {
            if (usuario != null) {
                main_cambiar_fragment("c_editar_usuario_y_log_out");

            } else {
                metodos.alertdialog_ingresar_a_la_cuenta(this);
            }
        } else if (id == R.id.nav_ver_carrito) {
            if (usuario != null) {
                method_ir_al_carrito();

            } else {
                metodos.alertdialog_ingresar_a_la_cuenta(this);
            }
        } else if (id == R.id.nav_error) {

            borrar_datos_del_pedido();

        } else if (id == R.id.nav_comentarios) {
            if (usuario != null) {
                metodos.alertdialog_comentario_o_bug(this, true, usuario);
            } else {
                metodos.alertdialog_ingresar_a_la_cuenta(this);
            }

        } else if (id == R.id.nav_bug) {
            if (usuario != null) {
                metodos.alertdialog_comentario_o_bug(this, false, usuario);
            } else {
                metodos.alertdialog_ingresar_a_la_cuenta(this);
            }

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void settitletoolbar(String title) {
        toolbar.setTitle(title);
        //toolbar.setNavigationIcon(R.drawable.ic_toolbar_estrella);
        toolbar.setOverflowIcon(drawable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (FirebaseDatabase.getInstance() != null) {
            FirebaseDatabase.getInstance().goOnline();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        boolean servicio_activo = servicio_del_pedido_activo.estado_servicio();
        if (!servicio_activo) {
            if (FirebaseDatabase.getInstance() != null) {
                FirebaseDatabase.getInstance().goOffline();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        boolean servicio_activo = servicio_del_pedido_activo.estado_servicio();
        if (!servicio_activo) {
            if (FirebaseDatabase.getInstance() != null) {
                FirebaseDatabase.getInstance().goOffline();
            }
        }
    }

    public void mostrar_fab(boolean mostrar) {
        if (mostrar) {
            fab.show();
        } else {
            fab.hide();
        }
    }

    public HashMap<String, Integer[]> hash_multiples_deliverys_montos = new HashMap<>();

    public void montos_cobrar(int method_monto_total, int method_monto_delivery, int method_monto_comision_app) {

        monto_total = method_monto_total;
        monto_delivery = method_monto_delivery;
        monto_comision_app = method_monto_comision_app;

    }

    public int[] devolver_montos() {
        //       int [] montos= {monto_total,monto_delivery,monto_comision_app};
        return new int[]{monto_total, monto_delivery, monto_comision_app};
    }

    public void method_iniciar_servicio() {
        Intent intent = new Intent(this, servicio_del_pedido_activo.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }

    public void guardar_pedido(String negocio, String producto, String precio, String tiene_categoria, String parent_producto, String ubicacion, String cantidad) {
        String valor_guerdado = sharpref.getString("pedido", null);
        SharedPreferences.Editor editor = sharpref.edit();

        String ubicacion_mejorada = ubicacion.replace(" ", "");

        HashMap<String, String> hash_shared_pref = new HashMap<>();
        String pedido_a_guardar = negocio + "#" + producto + "#" + precio + "#" + tiene_categoria + "#" + parent_producto + "#" + ubicacion_mejorada + "#" + cantidad + "·";

        //armo el hashmap
        if (valor_guerdado != null) {
            //le saco las {}
            valor_guerdado = valor_guerdado.substring(1, valor_guerdado.length() - 1);

            String[] cantidad_ubicaciones = valor_guerdado.split(",");
            for (int g = 0; g < cantidad_ubicaciones.length; g++) {
                String[] dividir_key_y_valor = cantidad_ubicaciones[g].split("=");
                String key_mejorada = dividir_key_y_valor[0].replace(" ", "");
                if (dividir_key_y_valor[1].equals("null")) {
                    dividir_key_y_valor[1] = null;
                }
                hash_shared_pref.put(key_mejorada, dividir_key_y_valor[1]);
            }
        }

        //veo si existe algun valor en donde quiero guardar
        String valor_anterior = hash_shared_pref.get(ubicacion_mejorada);

        //guardo el valor en el hash
        if (valor_anterior == null) {
            //esto guarda s1 en s
            hash_shared_pref.put(ubicacion_mejorada, pedido_a_guardar);
        } else {
            String string_a_guardar = hash_shared_pref.get(ubicacion_mejorada) + pedido_a_guardar;
            hash_shared_pref.put(ubicacion_mejorada, string_a_guardar);
        }

        //guardo el hash en sharedpref
        String str_guardar_en_shared = hash_shared_pref.toString();
        editor.putString("pedido", str_guardar_en_shared);
        editor.apply();

    }

    private void borrar_datos_del_pedido() {
        AlertDialog.Builder alerta = new AlertDialog.Builder(
                MainActivity.this);

        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.ad_basico_titulo_texto_botones, null);
        TextView tv_titulo = dialogView.findViewById(R.id.ad_tv_titulo);
        TextView tv_mensaje = dialogView.findViewById(R.id.ad_tv_mensaje);
        Button btn_aceptar = dialogView.findViewById(R.id.btn_aceptar);
        Button btn_cancelar = dialogView.findViewById(R.id.btn_cancelar);

        tv_titulo.setText("Borrar datos de los pedidos");
        tv_mensaje.setText("Confirmar borrar los datos");
        btn_aceptar.setText("Borrar los datos");
        btn_cancelar.setText("ups, missclick =D");

        btn_aceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
                sharpref.edit().remove("pedido").apply();
                Toast.makeText(MainActivity.this, "datos borrados", Toast.LENGTH_LONG).show();
            }
        });
        btn_cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });

        alerta.setView(dialogView);
        alertDialog = alerta.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();
    }

    public void main_cambiar_fragment(String fragment) {

        FragmentTransaction tx = getSupportFragmentManager().beginTransaction();

        if (fragment.equals("c_principal")) {
            tx.replace(R.id.content_main, new c_principal()).addToBackStack("c_principal");
        }
        if (fragment.equals("c_ingresar_direccion")) {
            tx.replace(R.id.content_main, new c_ingresar_direccion()).addToBackStack("c_ingresar_direccion");
        }
        if (fragment.equals("c_mapas_ver_covertura")) {
            tx.replace(R.id.content_main, new c_mapas_ver_covertura()).addToBackStack("c_mapas_ver_covertura");
        }
        if (fragment.equals("c_carrito")) {
            tx.replace(R.id.content_main, new c_carrito()).addToBackStack("c_carrito");
        }
        if (fragment.equals("c_editar_usuario_y_log_out")) {
            tx.replace(R.id.content_main, new c_editar_usuario_y_log_out()).addToBackStack("c_editar_usuario_y_log_out");
        }
        if (fragment.equals("c_ingreso_a_la_app")) {
            tx.replace(R.id.content_main, new c_ingreso_a_la_app()).addToBackStack("c_ingreso_a_la_app");
        }
        if (fragment.equals("c_guardar_pedidos_ver_pedidos")) {
            tx.replace(R.id.content_main, new c_guardar_pedidos_ver_pedidos()).addToBackStack("c_ingreso_a_la_app");
        }
        tx.commit();
    }

    public void chekear_internet() {
        ConnectivityManager cm = (ConnectivityManager) contex.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
        } else {

            Toast.makeText(contex, "Sin Internet", Toast.LENGTH_LONG).show();

            AlertDialog.Builder alerta_sin_internet = new AlertDialog.Builder(
                    MainActivity.this);

            LayoutInflater inflater = this.getLayoutInflater();
            final View dialogView = inflater.inflate(R.layout.ad_basico_titulo_texto_botones, null);
            TextView tv_titulo = dialogView.findViewById(R.id.ad_tv_titulo);
            TextView tv_mensaje = dialogView.findViewById(R.id.ad_tv_mensaje);
            Button btn_aceptar = dialogView.findViewById(R.id.btn_aceptar);
            Button btn_cancelar = dialogView.findViewById(R.id.btn_cancelar);

            btn_aceptar.setText("volver a intentar");
            btn_cancelar.setVisibility(View.GONE);

            tv_titulo.setText("Sin coneccion a internet");
            tv_mensaje.setText("para seguir usando la app se necesita coneccion a internet");

            btn_aceptar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    alertDialog.dismiss();
                    chekear_internet();
                }
            });

            alerta_sin_internet.setView(dialogView);
            alertDialog = alerta_sin_internet.create();
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            alertDialog.show();

        }
    }
}

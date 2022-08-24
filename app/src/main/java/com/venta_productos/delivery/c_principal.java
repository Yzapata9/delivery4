package com.venta_productos.delivery;


import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.snatik.polygon.Point;
import com.snatik.polygon.Polygon;
import com.venta_productos.delivery.adapter.adaptador_fragment_principal_mejorado;

import org.osmdroid.util.GeoPoint;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;


/**
 * A simple {@link Fragment} subclass.
 */
public class c_principal extends Fragment {


    public c_principal() {
        // Required empty public constructor
    }

    FirebaseAuth mAuth;
    Context contex;
    SharedPreferences sharpref;
    String TAG = "asdf";

    //oncreate
    GridView gridview;
    Button btn_cambiar_de_ubicacion;
    DatabaseReference database_deliverys;
    DatabaseReference database_negocios;

    //ubicacion cliente
    ArrayList<String> array_ubicaciones_disponibles = new ArrayList<>();
    HashMap<String, String> hash_ubicaciones_gps_del_cliente = new HashMap<>();

    //cargar negocios
    ArrayList<String> array_negocios_en_rango_del_delivery = new ArrayList<>();
    ArrayList<String> array_negocios_mostrar = new ArrayList<>();

    //buscar negocios disponibles
    HashMap<String, String> hash_todos_los_negocios = new HashMap<>();
    ArrayList<String> array_negocios_con_horario_abierto_y_estado_on = new ArrayList<>();

    String str_deliverys_pasar_al_carrito;

    //xml ver negocios o deliverys sin servicio
    boolean boolean_tv_con_servicio_delivery = false, boolean_tv_con_servicio_negocios = false;

    String lugar_elegido_guardado = null;
    String gps_elegido_guardado = null;

    LinearLayout linear_cambiar_de_ubicacion;
    int int_cantidad_de_ubicaciones;
    String str_ubicacion_unica;
    String str_ubicacion_calle_y_altura;

    SwipeRefreshLayout swipe;

    AlertDialog alertDialog_simple = null;
    AlertDialog alertDialog_listview = null;

    //buscar deliverys y negocios
    boolean boolean_estado_delivery;
    boolean boolean_el_cliente_esta_cubierto_por_el_delivery_y_delivery_estado_on;
    boolean boolean_el_cliente_esta_cubierto_por_algun_delivery;

    ArrayList<String> array_deliverys_que_cubren_la_ubicacion_elegida_del_cliente = new ArrayList<>();
    ArrayList<String> deliverys_on_y_con_ultima_coneccion = new ArrayList<>();
    HashMap<String, String> hash_delivery_contiene_estos_negocios = new HashMap<>();

    boolean boolean_se_cambio_el_gridview = false;

    LinearLayout linear_buscar;
    EditText autoCompleteTextView;
    ImageButton btn_busqueda_teclado, btn_busqueda_voz;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View Principal_mejorado = inflater.inflate(R.layout.f_principal_mejorado, container, false);
        gridview = Principal_mejorado.findViewById(R.id.lvv);
        linear_cambiar_de_ubicacion = Principal_mejorado.findViewById(R.id.linear_cambiar_direccion);
        swipe = Principal_mejorado.findViewById(R.id.swiper);
        btn_cambiar_de_ubicacion = Principal_mejorado.findViewById(R.id.btn_principal_cambiar_direccion);

        linear_buscar = Principal_mejorado.findViewById(R.id.linear_principal_buscar);
        autoCompleteTextView = Principal_mejorado.findViewById(R.id.editxt_principal_busqueda);
        btn_busqueda_teclado = Principal_mejorado.findViewById(R.id.btn_principal_busqueda_teclado);
        btn_busqueda_voz = Principal_mejorado.findViewById(R.id.btn_principal_busqueda_voz);

        if (getActivity() != null) {
            ((MainActivity) getActivity()).settitletoolbar("Negocios");
            ((MainActivity) getActivity()).mostrar_fab(true);
            lugar_elegido_guardado = ((MainActivity) getActivity()).ubicacion_elegida;
            gps_elegido_guardado = ((MainActivity) getActivity()).gps_elegido;
            str_ubicacion_calle_y_altura = ((MainActivity) getActivity()).ubicacion_calle_y_altura;
        }

        if (lugar_elegido_guardado != null) {
            btn_cambiar_de_ubicacion.setVisibility(View.VISIBLE);
            btn_cambiar_de_ubicacion.setText("ubicacion " + lugar_elegido_guardado + ", cambiar");
        }

        contex = getActivity();
        sharpref = getContext().getSharedPreferences("usar_app", Context.MODE_PRIVATE);

        database_deliverys = FirebaseDatabase.getInstance().getReference().child("deliverys");
        database_negocios = FirebaseDatabase.getInstance().getReference().child("negocios");

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                String[] separar_datos;
                if (boolean_se_cambio_el_gridview) {
                    separar_datos = array_negocios_mostrar.get(i).split("€€");
                } else {
                    separar_datos = ((MainActivity) getActivity()).array_mostar_negocios_previamente_cargados.get(i).split("€€");
                }

                if (separar_datos[4].equals("si")) { //es categoria
                    metodos.main_cambiar_fragment(getActivity(), "c_lista_categorias_negocios");
                    metodos.pasar_datos_del_negocio_al_fragment_categorias = separar_datos[5];//paso el nombre

                } else {
                    metodos.pasar_datos_productos_negocio = separar_datos[5];//paso el nombre
                    metodos.main_cambiar_fragment(getActivity(), "c_lista_productos_negocios");
                }
            }
        });

        btn_cambiar_de_ubicacion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btn_cambiar_de_ubicacion.setClickable(false);
                method_1_1_direcion_buscar_db(true);
            }
        });

        if (getActivity() != null) {
            ((MainActivity) getActivity()).chekear_internet();
        }

        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                method_1_1_direcion_buscar_db(false);
            }
        });

        btn_busqueda_teclado.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btn_busqueda_voz.getVisibility() == View.VISIBLE) {
                    btn_busqueda_voz.setVisibility(View.GONE);
                    autoCompleteTextView.setVisibility(View.VISIBLE);
                    btn_busqueda_teclado.setImageResource(R.drawable.ic_keyboard_voice_black_24dp);

                } else {
                    btn_busqueda_voz.setVisibility(View.VISIBLE);
                    autoCompleteTextView.setVisibility(View.GONE);
                    btn_busqueda_teclado.setImageResource(R.drawable.ic_keyboard_black_24dp);
                }
            }
        });
        btn_busqueda_voz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    metodos.alerdialog_pedir_permisos(getActivity(), 4);
                    Toast.makeText(getActivity(), "Busqueda por voz no ejecutada", Toast.LENGTH_SHORT).show();
                } else {
                    metodos_busqueda.realizar_busqueda(getActivity(), null);
                }
            }
        });

        autoCompleteTextView.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    if (autoCompleteTextView.getText().toString().length() > 3) {
                        String str_input = autoCompleteTextView.getText().toString();
                        metodos_busqueda.realizar_busqueda(getActivity(), str_input);
                    } else {
                        Toast.makeText(getActivity(), "minimo 4 caracteres", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
                return false;
            }
        });

        return Principal_mejorado;
    }

    //busca ubicaciones, deliverys, negocios

    private void method_1_1_direcion_buscar_db(final boolean cambiar_ubicacion) {

        Log.d(TAG, "method_1_1_direcion_buscar_db: " + str_ubicacion_calle_y_altura);

        hash_ubicaciones_gps_del_cliente.clear();
        array_ubicaciones_disponibles.clear();
        int_cantidad_de_ubicaciones = 0;
        if (getActivity() != null) {
            if (((MainActivity) getActivity()).array_mostar_negocios_previamente_cargados == null) {
                metodos.alerdialog_descargando_informacion(getActivity(), true, "buscando ubicaciones del usuario (1/2)");
            }
        }
        final String valor_guerdado = sharpref.getString("usuario", "no hay dato");
        String[] dato = valor_guerdado.split(",");
        DatabaseReference chekear_domicilio = FirebaseDatabase.getInstance().getReference().child("usuario").child("clientes").child(dato[1]).child("ubicacion");
        chekear_domicilio.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    if (dataSnapshot.getChildrenCount() == 1) {
                        linear_cambiar_de_ubicacion.setVisibility(View.GONE);
                    }
                    if (cambiar_ubicacion | lugar_elegido_guardado == null | gps_elegido_guardado == null | str_ubicacion_calle_y_altura == null) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                            String ubicacion_nombre = (String) snapshot.child("nombre").getValue();
                            String nombre_calle = (String) snapshot.child("calle").getValue();
                            String altura_calle = (String) snapshot.child("altura").getValue();
                            str_ubicacion_calle_y_altura = ubicacion_nombre + "," + nombre_calle + "," + altura_calle;

                            str_ubicacion_unica = (String) snapshot.child("nombre").getValue();
                            array_ubicaciones_disponibles.add(ubicacion_nombre);
                            String ubicacion_gps = (String) snapshot.child("gps").getValue();
                            hash_ubicaciones_gps_del_cliente.put(ubicacion_nombre, ubicacion_gps);
                            int_cantidad_de_ubicaciones++;
                        }

                        //pasa al paso numero 2
                        if (int_cantidad_de_ubicaciones > 1) { //si tiene mas de una ubicacion que la elija cual usar
                            method_1_2_ubicacion_elegir(array_ubicaciones_disponibles);
                        } else {//si tiene menos de una que se elija sin preguntar
                            method_1_3_ubicacion_guardar_datos(str_ubicacion_unica);
                        }
                    } else {//carga los datos guardados y pasa al paso numero 2
                        hash_ubicaciones_gps_del_cliente.put(lugar_elegido_guardado, gps_elegido_guardado);
                        method_1_3_ubicacion_guardar_datos(lugar_elegido_guardado);
                    }
                } else {
                    alertdialog_simple(0,
                            "Sin ubicacion o domicio",
                            "no tiene una ubicacion guardada, desea agregarla ahora?",
                            "AGREGAR UBICACION",
                            "Ver negocios",
                            false);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });


    } //chequea que el usuario tenga ubicacion (ya sea guardada o no)

    private void method_1_2_ubicacion_elegir(final ArrayList<String> ubicaciones) {

        btn_cambiar_de_ubicacion.setClickable(true);
        metodos.alerdialog_descargando_informacion(getActivity(), false, "");
        String[] pasar_datos_alertdialog = ubicaciones.toArray(new String[0]);
        alertdialog_listview("cual ubicacion desea elegir?", pasar_datos_alertdialog, 0, true);

    } //se selecciona una ubicacion

    private void method_1_3_ubicacion_guardar_datos(final String STR_ubicacion) {

        btn_cambiar_de_ubicacion.setText("ubicacion = " + lugar_elegido_guardado + ", cambiar");

        //obtengo el gps del cliente
        String[] mi_ubicacion = hash_ubicaciones_gps_del_cliente.get(STR_ubicacion).split(",");
        double lat = Double.parseDouble(mi_ubicacion[0]);
        double long_ = Double.parseDouble(mi_ubicacion[1]);

        //Guarda el delivery disponible y el gps del cliente
        ((MainActivity) getActivity()).mi_gps = new GeoPoint(lat, long_);
        ((MainActivity) getActivity()).ubicacion_elegida = STR_ubicacion;
        ((MainActivity) getActivity()).ubicacion_calle_y_altura = str_ubicacion_calle_y_altura;
        ((MainActivity) getActivity()).gps_elegido = mi_ubicacion[0] + "," + mi_ubicacion[1];

//guardo para futuros usos
        SharedPreferences.Editor editor = sharpref.edit();
        editor.putString("ubicacion", STR_ubicacion);
        editor.putString("ubicacion_gps", mi_ubicacion[0] + "," + mi_ubicacion[1]);
        editor.putString("ubicacion_calle_y_altura", str_ubicacion_calle_y_altura);
        editor.apply();
        //va al paso n5
        armar_los_negocios_disponibles(mi_ubicacion[0] + "," + mi_ubicacion[1]);

    } //guarda los datos de la ubicacion elegida

    private void armar_los_negocios_disponibles(String str_input_gps) {

        method_2_deliverys_ubicaciones_negocios(str_input_gps, new FirebaseCallBack_buscar_negocios_disponibles() {
            @Override
            public void onCallBack(HashMap<String, String> hash_con_negocios) {
                //armo los negocios en rango

                ArrayList<String> delivery_on_y_cubre_la_ubicacion = new ArrayList<>();
                //se fija si los deliverys disponibles Y cubre la ubicacion del usuario
                for (String delivery : deliverys_on_y_con_ultima_coneccion) {
                    if (array_deliverys_que_cubren_la_ubicacion_elegida_del_cliente.contains(delivery)) {
                        delivery_on_y_cubre_la_ubicacion.add(delivery);
                    }
                }

                ArrayList<String> negocios_con_delivery_disponible_cliente_con_ubicacion_y_estado_on = new ArrayList<>();

                for (String delivery : delivery_on_y_cubre_la_ubicacion) {
                    if (hash_delivery_contiene_estos_negocios.containsKey(delivery)) {

                        String[] negocios = hash_delivery_contiene_estos_negocios.get(delivery).split(",");

                        if (negocios.length > 0) {
                            ((MainActivity) getActivity()).boolean_ver_negocios_igualmente = false;
                        }

                        for (String negocios_comparar : negocios) {
                            if (array_negocios_con_horario_abierto_y_estado_on.contains(negocios_comparar)) {
                                negocios_con_delivery_disponible_cliente_con_ubicacion_y_estado_on.add(negocios_comparar);
                            }
                        }

                        if (str_deliverys_pasar_al_carrito == null) { //guarda directo si no hay nada guardado
                            str_deliverys_pasar_al_carrito = delivery;
                        } else {
                            //si ya se guardo algo me fijo que no se guarde el delivery 2 o + veces
                            String[] control_para_no_repetir_el_negocio = str_deliverys_pasar_al_carrito.split(",");
                            boolean contiene_el_delivery = Arrays.asList(control_para_no_repetir_el_negocio).contains(delivery);
                            if (!contiene_el_delivery) {
                                str_deliverys_pasar_al_carrito = str_deliverys_pasar_al_carrito + "," + delivery;
                            }
                        }
                    }
                }

                //logica de si tengo covertura

                //algun delivery lo cubre alguna vez
                if (boolean_el_cliente_esta_cubierto_por_algun_delivery) {
                    //el cliente elige ver los negocios igualmente
                    if (!((MainActivity) getActivity()).boolean_ver_negocios_igualmente) {
                        //el delivery lo cubre actualmente
                        if (boolean_el_cliente_esta_cubierto_por_el_delivery_y_delivery_estado_on) {
                            //hay negocios disponibles
                            if (negocios_con_delivery_disponible_cliente_con_ubicacion_y_estado_on.size() != 0) {
                                boolean_tv_con_servicio_negocios = true;
                                method_3_1_llenar_gridview(negocios_con_delivery_disponible_cliente_con_ubicacion_y_estado_on);
                            } else {
                                alertdialog_simple(1,
                                        "Negocios no disponibles temporalmente",
                                        null,
                                        "Ok, entiendo",
                                        "Probar con otra direccion",
                                        false);
                            }
                        } else {
                            String[] opciones = {"Ok, entiendo", "Probar con otra direccion", "Agregar nueva ubicacion"};
                            alertdialog_listview(
                                    "Servicio no disponible",
                                    opciones,
                                    2,
                                    false);
                        }
                    } else {
                        method_3_1_llenar_gridview(array_negocios_en_rango_del_delivery);
                    }
                } else {
                    if (int_cantidad_de_ubicaciones == 1) {
                        alertdialog_simple(2,
                                "ningun delivery cubre su ubicacion",
                                null,
                                "agregar otra ubicacion",
                                "mirar donde esta disponible el servicio",
                                false);
                    } else {
                        String[] opciones = {"Probar con otra direccion", "Agregar nueva ubicacion", "ver donde esta disponible el servicio"};
                        alertdialog_listview(
                                "su ubicacion no la cubre un delivery",
                                opciones,
                                1,
                                false);
                    }
                }
            }
        });
    }


    public void method_2_deliverys_ubicaciones_negocios(final String str_gps, final FirebaseCallBack_buscar_negocios_disponibles firebaseCallBack_buscar_negocios_disponibles) {

        //borra los datos antiguos
        str_deliverys_pasar_al_carrito = null;
        array_negocios_con_horario_abierto_y_estado_on.clear();
        hash_todos_los_negocios.clear();

        boolean_el_cliente_esta_cubierto_por_el_delivery_y_delivery_estado_on = false; //para saber si el cliente esta cubierto
        boolean_el_cliente_esta_cubierto_por_algun_delivery = false; //para saber si el cliente esta cubierto

        deliverys_on_y_con_ultima_coneccion.clear();
        array_deliverys_que_cubren_la_ubicacion_elegida_del_cliente.clear();
        hash_delivery_contiene_estos_negocios.clear();
        array_negocios_en_rango_del_delivery.clear();

        //busca la hora actual
        Calendar cal = Calendar.getInstance();
        long time_in_ms = cal.getTimeInMillis();
        long restar = 900000;
        final long horario_del_cliente_en_ms = time_in_ms - restar;

        boolean_tv_con_servicio_delivery = false;
        if (getActivity() != null) {
            metodos.alerdialog_descargando_informacion(getActivity(), false, "");
            if (getActivity() != null) {
                if (((MainActivity) getActivity()).array_mostar_negocios_previamente_cargados == null) {
                    metodos.alerdialog_descargando_informacion(getActivity(), true, "buscando deliverys y negocios disponibles (2/2)");
                }
            }
        }

        database_negocios.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //busca TODOS los negocios
                for (DataSnapshot snap_negocios : dataSnapshot.getChildren()) {

                    String ubicacion_nombre = snap_negocios.getKey();
                    String ubicacion_gps = (String) snap_negocios.child("gps_negocio").getValue();
                    String negocio_estado = (String) snap_negocios.child("estado_negocio").getValue();
                    String str_horario = (String) snap_negocios.child("horario").getValue();

                    if (ubicacion_gps != null & negocio_estado != null & str_horario != null) {
                        boolean abierto_o_cerrado = chequear_horario(str_horario);
                        boolean estado_on = negocio_estado.equals("on");

                        if (abierto_o_cerrado & estado_on) {
                            array_negocios_con_horario_abierto_y_estado_on.add(ubicacion_nombre);
                            hash_todos_los_negocios.put(ubicacion_nombre, ubicacion_gps);
                        } else {
                            hash_todos_los_negocios.put(ubicacion_nombre, ubicacion_gps);
                        }
                    } else {
                        Toast.makeText(getActivity(), "error con el negocio= " + ubicacion_nombre, Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        database_deliverys.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //busca todos los deliverys
                for (DataSnapshot snap_deliverys : dataSnapshot.getChildren()) {
                    String estado = (String) snap_deliverys.child("estado").getValue();
                    String delivery_numero = snap_deliverys.getKey();
                    boolean_estado_delivery = false;
                    String conversion = null;

                    //busca la ultima coneccion
                    long ultima_conecciondel_delivery_en_ms = 0;
                    if (snap_deliverys.child("ultima_coneccion").hasChildren()) {
                        ultima_conecciondel_delivery_en_ms = (long) snap_deliverys.child("ultima_coneccion").child("en_ms").getValue();
                    } //chekea si esta disponible (<15min)
                    boolean boolean_ultima_coneccion_menos_de_15_min = false;
                    if (horario_del_cliente_en_ms < ultima_conecciondel_delivery_en_ms) {
                        boolean_ultima_coneccion_menos_de_15_min = true;
                    }

                    if (estado != null & delivery_numero != null) {
                        //busca el estado del delivery
                        if (estado.equals("on") & boolean_ultima_coneccion_menos_de_15_min) {
                            boolean_tv_con_servicio_delivery = true; //tiene servicio de algun delivery
                            boolean_estado_delivery = true;
                            deliverys_on_y_con_ultima_coneccion.add(delivery_numero);
                        }

                        //busca las areas del delivery
                        for (DataSnapshot snap_area : dataSnapshot.child(delivery_numero).child("area").getChildren()) {
                            //creo el polygon del area 
                            String str_area_input = (String) snap_area.getValue();
                            Polygon polygon_area = method_crear_polygon(str_area_input);

                            //me fijo si el cliente esta dentro o no
                            boolean cliente_adentro_o_afuera_de_algun_poly = method_esta_dentro_del_poli(polygon_area, str_gps);
                            if (cliente_adentro_o_afuera_de_algun_poly) { //esta dentro de un poly
                                if (boolean_estado_delivery) { //tiene covertura y estado deliv on
                                    if (!boolean_el_cliente_esta_cubierto_por_el_delivery_y_delivery_estado_on) {
                                        boolean_el_cliente_esta_cubierto_por_el_delivery_y_delivery_estado_on = true;
                                        boolean_el_cliente_esta_cubierto_por_algun_delivery = true;
                                        array_deliverys_que_cubren_la_ubicacion_elegida_del_cliente.add(delivery_numero);
                                    }
                                } else { //tiene covertura pero estado deliv off
                                    if (!boolean_el_cliente_esta_cubierto_por_algun_delivery) {
                                        boolean_el_cliente_esta_cubierto_por_algun_delivery = true;
                                        array_deliverys_que_cubren_la_ubicacion_elegida_del_cliente.add(delivery_numero);
                                    }
                                }
                            }
                            //me fijo dentro de todos los negocios cuales estan en el area
                            for (String negocio : hash_todos_los_negocios.keySet()) {
                                String gps_point_negocio = hash_todos_los_negocios.get(negocio);
                                boolean negocio_adentro_o_afuera_de_algun_poly = method_esta_dentro_del_poli(polygon_area, gps_point_negocio);
                                if (negocio_adentro_o_afuera_de_algun_poly) {
                                    boolean contiene_el_negocio = array_negocios_en_rango_del_delivery.contains(negocio);
                                    if (!contiene_el_negocio) {
                                        array_negocios_en_rango_del_delivery.add(negocio);
                                        if (conversion == null) {
                                            conversion = negocio;
                                        } else {
                                            conversion = conversion + "," + negocio;
                                        }
                                    }
                                }
                            }
                        }
                        if (conversion != null) {
                            hash_delivery_contiene_estos_negocios.put(delivery_numero, conversion);
                        }
                    }
                }
                firebaseCallBack_buscar_negocios_disponibles.onCallBack(hash_delivery_contiene_estos_negocios);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    } //busca deliverys disponibles y los compara con las areas de las ubicaciones


    public void method_3_1_llenar_gridview(final ArrayList<String> negocios_disponibles) {
        if (getActivity() != null) {
            ((MainActivity) getActivity()).que_delivery_usar = null;
            ((MainActivity) getActivity()).array_mainact_negocios_disponibles = null;
        }
        method_3_2_buscar_datos_negocios(negocios_disponibles, new FirebaseCallBack_pasar_al_adaptador() {
            @Override
            public void onCallBack(ArrayList<String> negocios_2) {

                if (getActivity() != null) {
                    metodos.alerdialog_descargando_informacion(getActivity(), false, "");
                }
                method_4_llenar_gridview(negocios_2);

                //guarda los deliverys que cubren la ubicacion
                if (str_deliverys_pasar_al_carrito != null) {
                    ((MainActivity) getActivity()).que_delivery_usar = str_deliverys_pasar_al_carrito;
                    if (boolean_tv_con_servicio_negocios) {
                        ((MainActivity) getActivity()).array_mainact_negocios_disponibles = negocios_disponibles;
                    }
                }
            }
        });
    } // este es el que activa los firebase callback y muestra el gridview

    private void method_3_2_buscar_datos_negocios(final ArrayList<String> negocios, final FirebaseCallBack_pasar_al_adaptador firebaseCallBack_pasar_al_adaptador) {
        array_negocios_mostrar.clear();

        database_negocios.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snap_negocios) {

                for (int n = 0; n < negocios.size(); n++) {
                    DataSnapshot snap_negocio = snap_negocios.child(negocios.get(n));
                    String key = snap_negocio.getKey();
                    String nombre = (String) snap_negocio.child("nombre").getValue();
                    String horario = (String) snap_negocio.child("horario").getValue();
                    String rubro = (String) snap_negocio.child("rubro").getValue();
                    String imagen = (String) snap_negocio.child("imagen").getValue();
                    String str_es_categoria_rubro = (String) snap_negocio.child("tiene_categorias").getValue();
                    if (str_es_categoria_rubro == null) {
                        str_es_categoria_rubro = "no";
                    }

                    String juntar_datos = nombre + "€€" + horario + "€€" + rubro + "€€" + imagen + "€€" + str_es_categoria_rubro + "€€" + key;
                    array_negocios_mostrar.add(juntar_datos);
                }
                firebaseCallBack_pasar_al_adaptador.onCallBack(array_negocios_mostrar);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    } //busca los datos de los negocios que estan disponibles

    private void method_4_llenar_gridview(ArrayList<String> negocios_2) {

        linear_buscar.setVisibility(View.VISIBLE);

        if (getActivity() != null) {
            if (((MainActivity) getActivity()).array_mostar_negocios_previamente_cargados == null) {
                ((MainActivity) getActivity()).array_mostar_negocios_previamente_cargados = negocios_2;
                boolean_se_cambio_el_gridview = true;
                String[] pasar_datos_al_adapter = negocios_2.toArray(new String[0]);
                final adaptador_fragment_principal_mejorado adapter_productos = new adaptador_fragment_principal_mejorado(getActivity(), pasar_datos_al_adapter);
                gridview.setAdapter(adapter_productos);
            } else if (((MainActivity) getActivity()).array_mostar_negocios_previamente_cargados != negocios_2) {
                ((MainActivity) getActivity()).array_mostar_negocios_previamente_cargados = negocios_2;
                boolean_se_cambio_el_gridview = true;
                String[] pasar_datos_al_adapter = negocios_2.toArray(new String[0]);
                final adaptador_fragment_principal_mejorado adapter_productos = new adaptador_fragment_principal_mejorado(getActivity(), pasar_datos_al_adapter);
                gridview.setAdapter(adapter_productos);
            } else if (((MainActivity) getActivity()).array_mostar_negocios_previamente_cargados == negocios_2) {
                boolean_se_cambio_el_gridview = false;
            }
        }
        if (!boolean_tv_con_servicio_delivery | !boolean_tv_con_servicio_negocios) {
            Toast.makeText(getActivity(), "Sin servicio de delivery disponible", Toast.LENGTH_SHORT).show();
        }
        if (swipe.isRefreshing()) {
            swipe.setRefreshing(false);
        }
    }

    private void alertdialog_simple(final int int_n_alert_dilog, final String str_titulo, String str_mensaje, String str_btn_aceptar, String str_btn_cancelar, boolean es_cancelable) {

        // int 0=sin ubicacion o domicilio
        // 1 = ver negocios igualmente
        // agregar ubicacion, ver donde esta disponible el servicio

        AlertDialog.Builder alerta = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.ad_basico_titulo_texto_botones, null);
        alerta.setView(dialogView);
        alertDialog_simple = alerta.create();
        alertDialog_simple.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        if (!es_cancelable) {
            alertDialog_simple.setCancelable(false);
        }

        TextView tv_titulo = dialogView.findViewById(R.id.ad_tv_titulo);
        TextView tv_mensaje = dialogView.findViewById(R.id.ad_tv_mensaje);
        Button btn_aceptar = dialogView.findViewById(R.id.btn_aceptar);
        Button btn_cancelar = dialogView.findViewById(R.id.btn_cancelar);

        tv_titulo.setText(str_titulo);
        if (str_mensaje == null) {
            tv_mensaje.setVisibility(View.GONE);
        } else {
            tv_mensaje.setText(str_mensaje);
        }
        btn_aceptar.setText(str_btn_aceptar);
        if (str_btn_cancelar == null) {
            btn_cancelar.setVisibility(View.GONE);
        } else {
            btn_cancelar.setText(str_btn_cancelar);
        }

        btn_aceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (int_n_alert_dilog == 0) {
                    metodos.main_cambiar_fragment(getActivity(), "c_ingresar_direccion");
                }
                if (int_n_alert_dilog == 1) {
                    metodos.alerdialog_descargando_informacion(getActivity(), false, "");
                    ((MainActivity) getActivity()).boolean_ver_negocios_igualmente = true; // esto hace que no vuelva a este punto
                    ((MainActivity) getActivity()).boolean_main_ver_negocios_sin_coneccion_igualmente = true;
                    method_3_1_llenar_gridview(array_negocios_en_rango_del_delivery);
                }
                if (int_n_alert_dilog == 2) {
                    // agregar ubicacion
                    metodos.main_cambiar_fragment(getActivity(), "c_editar_usuario_y_log_out");
                }
                alertDialog_simple.dismiss();
            }
        });
        btn_cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (int_n_alert_dilog == 0) {
                    metodos.main_cambiar_fragment(getActivity(), "c_ver_negocios_sin_coneccion");
                }
                if (int_n_alert_dilog == 1) {
                    method_1_1_direcion_buscar_db(true);
                }
                if (int_n_alert_dilog == 2) {
                    // ver donde esta disponible el servicio
                    ((MainActivity) getActivity()).boolean_solo_mostrar_las_areas_del_mapa = true;
                    metodos.main_cambiar_fragment(getActivity(), "c_mapas_ver_covertura");
                }
                alertDialog_simple.dismiss();
            }
        });
        alertDialog_simple.show();
    }

    private void alertdialog_listview(final String str_titulo, final String[] items, final int cual_alert_es, Boolean boolean_es_cancelable) {

        // 0 = mostrar cual ubicacion desea elegir
        // 1 = sin deliverys nunca para la ubicacion actual
        // 2 = sin deliverys actualmente para las ubicaciones

        final AlertDialog.Builder alerta = new AlertDialog.Builder(getActivity());
        final LayoutInflater inflater = getActivity().getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.ad_listview, null);
        alerta.setView(dialogView);
        alertDialog_listview = alerta.create();
        alertDialog_listview.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        TextView tv_titulo = dialogView.findViewById(R.id.ad_tv_titulo);
        ListView ad_listview = dialogView.findViewById(R.id.ad_listview);
        Button btn_aceptar = dialogView.findViewById(R.id.btn_aceptar);
        Button btn_cancelar = dialogView.findViewById(R.id.btn_cancelar);
        if (!boolean_es_cancelable) {
            alertDialog_listview.setCancelable(false);
        }
        btn_aceptar.setVisibility(View.GONE);
        btn_cancelar.setVisibility(View.GONE);

        tv_titulo.setText(str_titulo);

        ArrayAdapter<String> adapter
                = new ArrayAdapter<>(getActivity(),
                R.layout.ad_row_texto_estilo, //en que layout se encuentra el texto
                R.id.texto_spinner, // como es el id del texto
                items); //valor que tiene que mostrar
        ad_listview.setAdapter(adapter);

        ad_listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                if (cual_alert_es == 0) {
                    if (position == 0) {
                        ((MainActivity) getActivity()).ubicacion_elegida = null;
                        ((MainActivity) getActivity()).gps_elegido = null;
                        ((MainActivity) getActivity()).ubicacion_calle_y_altura = null;
                        method_1_3_ubicacion_guardar_datos(items[0]);
                        alertDialog_listview.dismiss();
                    }
                    if (position == 1) {
                        ((MainActivity) getActivity()).ubicacion_elegida = null;
                        ((MainActivity) getActivity()).gps_elegido = null;
                        ((MainActivity) getActivity()).ubicacion_calle_y_altura = null;
                        method_1_3_ubicacion_guardar_datos(items[1]);
                        alertDialog_listview.dismiss();
                    }
                    if (position == 2) {
                        ((MainActivity) getActivity()).ubicacion_elegida = null;
                        ((MainActivity) getActivity()).gps_elegido = null;
                        ((MainActivity) getActivity()).ubicacion_calle_y_altura = null;
                        method_1_3_ubicacion_guardar_datos(items[2]);
                        alertDialog_listview.dismiss();
                    }
                }
                if (cual_alert_es == 1) {
                    if (position == 0) {
                        method_1_1_direcion_buscar_db(true);
                        alertDialog_listview.dismiss();
                    }
                    if (position == 1) {
                        metodos.main_cambiar_fragment(getActivity(), "c_editar_usuario_y_log_out");
                        alertDialog_listview.dismiss();
                    }
                    if (position == 2) {
                        ((MainActivity) getActivity()).boolean_ver_negocios_igualmente = true; // esto hace que no vuelva a este punto
                        ((MainActivity) getActivity()).boolean_main_ver_negocios_sin_coneccion_igualmente = true;
                        metodos.alerdialog_descargando_informacion(getActivity(), true, "cargando negocios");
                        method_3_1_llenar_gridview(array_negocios_en_rango_del_delivery);
                        alertDialog_listview.dismiss();
                    }
                }
                if (cual_alert_es == 2) {
                    if (position == 0) {
                        ((MainActivity) getActivity()).boolean_ver_negocios_igualmente = true; // esto hace que no vuelva a este punto
                        ((MainActivity) getActivity()).boolean_main_ver_negocios_sin_coneccion_igualmente = true;
                        metodos.alerdialog_descargando_informacion(getActivity(), true, "cargando negocios");
                        method_3_1_llenar_gridview(array_negocios_en_rango_del_delivery);
                        alertDialog_listview.dismiss();
                    }
                    if (position == 1) {
                        method_1_1_direcion_buscar_db(true);
                        alertDialog_listview.dismiss();
                    }
                    if (position == 2) {
                        metodos.main_cambiar_fragment(getActivity(), "c_editar_usuario_y_log_out");
                        alertDialog_listview.dismiss();
                    }

                }
                alertDialog_listview.dismiss();
            }
        });
        alertDialog_listview.show();
    }

    //otros


    private interface FirebaseCallBack_pasar_al_adaptador {
        void onCallBack(ArrayList<String> negocios_2);
    }

    private interface FirebaseCallBack_buscar_negocios_disponibles {
        void onCallBack(HashMap<String, String> hash_con_negocios);
    }

    private boolean chequear_horario(String horario_input) {

        Calendar cal = Calendar.getInstance();
        int dia_de_la_semana = cal.get(Calendar.DAY_OF_WEEK);
        int int_horas = cal.get(Calendar.HOUR_OF_DAY);
        int int_minutos = cal.get(Calendar.MINUTE);

        boolean negocio_abierto_o_cerrado = false;

        String[] dias = horario_input.split("€");
        String dia_actual = dias[dia_de_la_semana - 1];

        if (dia_actual.length() > 2) {

            String[] horarios_del_dia = dia_actual.split("·");
            for (int h = 0; h < horarios_del_dia.length; h++) {
                String[] horario_comienzo_cierre = horarios_del_dia[h].split("-");

                String horario_inicio = horario_comienzo_cierre[0];
                String horario_cierre = horario_comienzo_cierre[1];

                try {
                    Date time1 = new SimpleDateFormat("HH:mm").parse(horario_inicio);
                    Calendar calendar1 = Calendar.getInstance();
                    calendar1.setTime(time1);

                    Date time2 = new SimpleDateFormat("HH:mm").parse(horario_cierre);
                    Calendar calendar2 = Calendar.getInstance();
                    calendar2.setTime(time2);

                    String hora_actual = String.valueOf(int_horas) + ":" + String.valueOf(int_minutos);
                    Date d = new SimpleDateFormat("HH:mm").parse(hora_actual);
                    Calendar calendar3 = Calendar.getInstance();
                    calendar3.setTime(d);

                    Date actual = calendar3.getTime();
                    Date inicio = calendar1.getTime();
                    Date cierre = calendar2.getTime();

                    if (actual.after(inicio) && actual.before(cierre)) {
                        negocio_abierto_o_cerrado = true;
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
        return negocio_abierto_o_cerrado;
    }

    @Override
    public void onResume() {

        if (alertDialog_listview != null) {
            alertDialog_listview.dismiss();
        }
        if (alertDialog_simple != null) {
            alertDialog_simple.dismiss();
        }

        if (((MainActivity) getActivity()).array_mostar_negocios_previamente_cargados == null) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                    R.layout.row_spinner_text, R.id.texto_spinner, new String[]{"\n .: cargando negocios :. \n"});
            gridview.setAdapter(adapter);
        } else {
            boolean_se_cambio_el_gridview = false;
            linear_buscar.setVisibility(View.VISIBLE);
            String[] pasar_datos_al_adapter = ((MainActivity) getActivity()).array_mostar_negocios_previamente_cargados.toArray(new String[0]);
            final adaptador_fragment_principal_mejorado adapter_productos = new adaptador_fragment_principal_mejorado(getActivity(), pasar_datos_al_adapter);
            gridview.setAdapter(adapter_productos);
        }

        // consultar si esta logeado y si tiene ubicacion
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            method_1_1_direcion_buscar_db(false);
            metodos.checkear_estado_del_usuario(getActivity(), currentUser.getUid());
        } else {
            metodos.alerdialog_descargando_informacion(getActivity(), false, "");
            if (!metodos.ingresando) {
                metodos.main_cambiar_fragment(getActivity(), "c_ingreso_a_la_app");
            } else {
                metodos.alerdialog_descargando_informacion(getActivity(),true,"Esperando respuesta de google");
            }
        }

        super.onResume();
    }

    private boolean method_esta_dentro_del_poli(Polygon input_poly, String input_gps) {
        boolean esta_adentro_del_poli;

        String[] coord = input_gps.split(",");
        double coord1 = Double.parseDouble(coord[0]);
        double coord2 = Double.parseDouble(coord[1]);
        Point point = new Point(coord1, coord2);
        esta_adentro_del_poli = input_poly.contains(point);
        return esta_adentro_del_poli;
    }

    private Polygon method_crear_polygon(String str_area_input) {
        Polygon.Builder poly2 = new Polygon.Builder();
        String[] areas = str_area_input.split(",");
        for (int i_poly = 0; i_poly < 4; i_poly++) {
            String[] coord = areas[i_poly].split("€");
            double coord1 = Double.parseDouble(coord[0]);
            double coord2 = Double.parseDouble(coord[1]);
            Point point = new Point(coord1, coord2);
            poly2.addVertex(point);
        }
        return poly2.build();
    }
}


package com.venta_productos.delivery;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.venta_productos.delivery.adapter.adaptador_grid_carrito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * A simple {@link Fragment} subclass.
 */
public class c_carrito extends Fragment {


    public c_carrito() {
        // Required empty public constructor
    }

    GridView gridview;
    Button btn_borrar, btn_siguiente;
    ArrayList<String> players = new ArrayList<>();
    ArrayList<String> nuevo_array_despues_de_borrar = new ArrayList<>();
    ArrayList<String> productos_no_encontrados = new ArrayList<>();
    TextView TV_precio_final_total, tv_ubicacion_actual;
    SharedPreferences sharpref;
    ArrayList<String> precios = new ArrayList<>();

    ArrayList<String> negocios_involucrados = new ArrayList<>();

    int precio_total_de_los_productos = 0;
    String STR_verificar_pedido_activo_n_random;
    String TAG = "asdf";

    String ubicacion_para_el_pedido;

    HashMap<String, String> hash_ubicaciones_involucradas = new HashMap<>();

    HashMap<String, String> hash_pedido_guardado = new HashMap<>();
    String pedido_guardado = null;

    //buscar pedidos activos
    String str_anio, str_mes, str_dia;
    String str_get_anio, str_get_mes, str_get_dia;
    int int_verificar_el_ultimo_pedido = 1; //ponerle un numero de veces para que se repita el loop sin cargar los datos en el grid
    boolean hay_un_pedido_activo = false;

    //boolean para saber si los negocios no disponibles fueron borrados
    int int_negocios_o_productos_a_borrar = 0;
    boolean boolean_mostrar_solo_un_alertdialog_para_borrar = false;

    //saber si es multy delivery
    boolean boolean_es_multi_delivery = false;

    //borrar todos los items del negocio
    String str_borrar_todos_los_items_de_este_negocio = " ";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View Fragment_carrito = inflater.inflate(R.layout.f_carrito, container, false);
        if (getActivity() != null) {
            ((MainActivity) getActivity()).settitletoolbar("Carrito");
            ((MainActivity) getActivity()).mostrar_fab(false);
        }

        gridview = Fragment_carrito.findViewById(R.id.lvv);
        btn_borrar = Fragment_carrito.findViewById(R.id.borrar_lista);
        btn_siguiente = Fragment_carrito.findViewById(R.id.siguiente_2);

        TV_precio_final_total = Fragment_carrito.findViewById(R.id.TV_precio_final_total);
        tv_ubicacion_actual = Fragment_carrito.findViewById(R.id.tv_carrito_ubicacion_actual);

        ubicacion_para_el_pedido = ((MainActivity) getActivity()).ubicacion_elegida;

        String str_tv_ubicacion_del_pedido = "Ubicacion actual= " + ubicacion_para_el_pedido;
        tv_ubicacion_actual.setText(str_tv_ubicacion_del_pedido);


        sharpref = getContext().getSharedPreferences("usar_app", Context.MODE_PRIVATE);
        pedido_guardado = sharpref.getString("pedido", null);

        if (pedido_guardado != null) {
            //le saco las {}
            pedido_guardado = pedido_guardado.substring(1, pedido_guardado.length() - 1);

            String[] cantidad_ubicaciones = pedido_guardado.split(",");
            for (int g = 0; g < cantidad_ubicaciones.length; g++) {
                String[] dividir_key_y_valor = cantidad_ubicaciones[g].split("=");
                String mejorar_key = dividir_key_y_valor[0].replace(" ", "");
                if (dividir_key_y_valor[1].equals("null")) {
                    dividir_key_y_valor[1] = null;
                }
                hash_pedido_guardado.put(mejorar_key, dividir_key_y_valor[1]);
            }
            String ubicacion_mejorada = ubicacion_para_el_pedido.replace(" ", "");
            pedido_guardado = hash_pedido_guardado.get(ubicacion_mejorada);
        }

        metodos.alerdialog_descargando_informacion(getActivity(), true, "buscando productos en la base de datos");

        //aca se llena el carrito
        method_verificar_pedido_activo();

        btn_borrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alert_borrar_lista_entera();

            }
        });

        btn_siguiente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String usuario = sharpref.getString("usuario", "no hay dato");
                if (pedido_guardado == null) {
                    Toast.makeText(getActivity(), "debe guardar productos para continuar", Toast.LENGTH_SHORT).show();
                } else {
                    metodos.alerdialog_descargando_informacion(getActivity(), true, "buscando datos");

                    if (usuario.equals("no hay dato")) {
                        Toast.makeText(getActivity(), "Para pedir tiene que estar ingresado", Toast.LENGTH_LONG).show();
                        metodos.alerdialog_descargando_informacion(getActivity(), false, "");
                        final FragmentTransaction ft;
                        if (getFragmentManager() != null) {
                            ft = getFragmentManager().beginTransaction();
                            ft.replace(R.id.content_main, new c_ingreso_a_la_app()).addToBackStack(toString());
                            ft.commit();
                        }
                    } else {
                        metodos.alerdialog_descargando_informacion(getActivity(), false, "");
                        //method_verificar_pedido_activo();
                        if (((MainActivity) getActivity()).boolean_todo_correcto_para_pedir()) {
                            if (int_negocios_o_productos_a_borrar == 0) {
                                ((MainActivity) getActivity()).ir_a_ver_progreso_o_al_menu_principal = true;
                                cambiar_de_fragment(false);
                            } else {
                                Toast.makeText(getActivity(), "Tiene productos o negocios no disponibles", Toast.LENGTH_LONG).show();
                                verificar_pecios_actualizados_en_firebase();
                            }
                        } else {
                            metodos.alertdialog_error_accion_solo_cerrar_el_alert(getActivity(), "deliverys o negocios no disponibles", "el servicio no se encuentra disponible actualmente", "Cerrar",false);

                        }
                    }
                }
            }
        });

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                int leng = gridview.getChildCount();
                if (leng == 1) {
                    alert_borrar_lista_entera();
                } else {
                    alert_borrar_item(i);
                }
            }
        });

        if (getActivity() != null) {
            ((MainActivity) getActivity()).chekear_internet();
        }
        return Fragment_carrito;
    }

    //BUSCAR PEDIDO ACTIVO

    private void method_verificar_pedido_activo() {

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR_OF_DAY, -4); //esto deberia restarle 4 horas al calendario asi hasta las 4 am, sigue siendo el mismo dia, para pedidos nocturnos
        int year = cal.get(Calendar.YEAR);
        int dayofmonth = cal.get(Calendar.DAY_OF_MONTH);
        int month = cal.get(Calendar.MONTH);

        str_anio = String.valueOf(year);
        str_mes = String.valueOf(month + 1);
        str_dia = String.valueOf(dayofmonth);

        //buscar dia del pedido anterior
        final String datos_para_verificar = sharpref.getString("verificar_pedidos_activos", null);
        if (datos_para_verificar != null) {
            String[] getrawdate = datos_para_verificar.split(",");
            String[] getdate = getrawdate[0].split("-");
            str_get_anio = getdate[0];
            str_get_mes = getdate[1];
            str_get_dia = getdate[2];
        }


        if (datos_para_verificar == null) {
            //si alguna vez guardo datos en sharedpref
            verificar_pecios_actualizados_en_firebase();
        } else {
            String[] datos = datos_para_verificar.split(",");
            STR_verificar_pedido_activo_n_random = datos[1];

            if (!str_anio.equals(str_get_anio) | !str_mes.equals(str_get_mes) | !str_dia.equals(str_get_dia)) {
                //si no es el dia =false
                int_verificar_el_ultimo_pedido = -1;
                verificar_pecios_actualizados_en_firebase();
            } else {
                if (datos.length > 2) {
                    boolean_es_multi_delivery = true;
                    int_verificar_el_ultimo_pedido = datos.length - 1;

                    //que lo haga muchas veces
                    for (int n_veces_random = 1; n_veces_random < datos.length; n_veces_random++) {

                        STR_verificar_pedido_activo_n_random = datos[n_veces_random];
                        method_verificar_pedidos_repetir_si_es_necesario(); // pasar el numero random y repetir... guardar en el boolean si esta  ok
                    }
                } else {
                    method_verificar_pedidos_repetir_si_es_necesario(); // pasar el numero random y repetir... guardar en el boolean si esta ok
                }
            }
        }
    }

    private void method_verificar_pedidos_repetir_si_es_necesario() {

        final List<String> myArrayList = Arrays.asList(getResources().getStringArray(R.array.array_posibles_estados_del_servicio_que_llevan_a_ver_progreso_pedido));

        buscar_pedido_activo_en_firebase(new FirebaseCallBack_pedido_activo() {
            @Override
            public void onCallBack_pedido_activo(String pedido) {

                if (myArrayList.contains(pedido)) {

                    //si hay un pedido activo
                    int_verificar_el_ultimo_pedido--; //si hay un pedido activo que no se repita el loop
                    // preguntar si quiere ir a
                    hay_un_pedido_activo = true;
                    if (int_verificar_el_ultimo_pedido == 0) { //solo si llega al ultimo loop o no tiene multy delivery se carge
                        cambiar_de_fragment(true);
                    }

                } else {
                    //si no hay nada

                    if (boolean_es_multi_delivery) {
                        int_verificar_el_ultimo_pedido--; //le saco una busqueda para que repita el loop..
                        if (int_verificar_el_ultimo_pedido == 0) { //solo si llega al ultimo loop o no tiene multy delivery se carge
                            if (hay_un_pedido_activo) {
                                cambiar_de_fragment(true);
                            } else {
                                if (getActivity() != null) {
                                    ((MainActivity) getActivity()).ir_a_ver_progreso_o_al_menu_principal = true;
                                }
                                verificar_pecios_actualizados_en_firebase();
                            }
                        }
                    } else {
                        if (hay_un_pedido_activo) {
                            cambiar_de_fragment(true);
                        } else {
                            if (getActivity() != null) {
                                ((MainActivity) getActivity()).ir_a_ver_progreso_o_al_menu_principal = true;
                            }
                            verificar_pecios_actualizados_en_firebase();
                        }
                    }

                }
                metodos.alerdialog_descargando_informacion(getActivity(), false, "");
            }
        });

    }

    private void buscar_pedido_activo_en_firebase(final FirebaseCallBack_pedido_activo firebaseCallBack_pedido_activo) {
        //chekea si hay datos

        DatabaseReference chekear_pedidos_activos = FirebaseDatabase.getInstance().getReference().child("pedidos").child(str_anio).child(str_mes).child(str_dia).child(STR_verificar_pedido_activo_n_random).child("pedido_en_actividad");
        chekear_pedidos_activos.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String estado = (String) dataSnapshot.getValue();
                if (estado == null) {
                    int_verificar_el_ultimo_pedido = 0; //si por algun motivo no se guardo estado_pedido..... innecesario.. no se repita el loop
                    verificar_pecios_actualizados_en_firebase();
                } else {
                    firebaseCallBack_pedido_activo.onCallBack_pedido_activo(estado);
                }
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private interface FirebaseCallBack_pedido_activo {
        void onCallBack_pedido_activo(String pedido);
    }

    //BORRAR

    public void borrar_item_del_lv(int n_borrar) {
        nuevo_array_despues_de_borrar.clear();

        if (pedido_guardado == null) {

            Toast.makeText(getActivity(), "No se pudo borrar", Toast.LENGTH_SHORT).show();

        } else {
            String[] datos = pedido_guardado.split("·");
            int cantidad_datos = datos.length;

            for (int i = 0; i < cantidad_datos; i++) {
                if (i != n_borrar) {
                    nuevo_array_despues_de_borrar.add(datos[i]);
                }
            }

            String guardar_nuevo_pedido = TextUtils.join("·", nuevo_array_despues_de_borrar) + "·";

            String ubicacion_mejorada = ubicacion_para_el_pedido.replace(" ", "");
            hash_pedido_guardado.put(ubicacion_mejorada, guardar_nuevo_pedido);
            pedido_guardado = guardar_nuevo_pedido;

            SharedPreferences.Editor editor = sharpref.edit();
            sharpref.edit().remove("pedido").apply();
            editor.putString("pedido", hash_pedido_guardado.toString()).apply();

            metodos.alerdialog_descargando_informacion(getActivity(), true, "buscando precios actualizados");
            verificar_pecios_actualizados_en_firebase();

        }
    }

    public void alert_borrar_lista_entera() {

        alert_dialog(1, "Confirmar Borrar Lista entera=", null, "CERRAR", null, "BORRAR", 0, null);

    }

    private void method_borrar_lista_entera() {

        // sharpref.edit().remove("pedido").apply();
        String ubicacion_mejorada = ubicacion_para_el_pedido.replace(" ", "");
        hash_pedido_guardado.put(ubicacion_mejorada, null);
        pedido_guardado = null;

        SharedPreferences.Editor editor = sharpref.edit();
        sharpref.edit().remove("pedido").apply();
        editor.putString("pedido", hash_pedido_guardado.toString()).apply();

        llenar_lv();
        precio_total_de_los_productos = 0;
        TV_precio_final_total.setText("0");

    }

    public void alert_borrar_item(final int n_borrar) {

        final String que_estoy_borrando = players.get(n_borrar);
        alert_dialog(2, "Confirmar Borrar Producto=", que_estoy_borrando, "CERRAR", "Ir al negocio", "BORRAR", n_borrar, null);

    }

    //BUSCAR PRECIOS ACTUALIZADOS

    private void verificar_pecios_actualizados_en_firebase() {
        boolean_mostrar_solo_un_alertdialog_para_borrar = false;
        if (pedido_guardado == null) {
            players.add("no hay datos guardados");
            String[] pasar_datos_al_adapter = players.toArray(new String[0]);
            final adaptador_grid_carrito adapter_productos = new adaptador_grid_carrito(getActivity(), pasar_datos_al_adapter);
            gridview.setAdapter(adapter_productos);
            metodos.alerdialog_descargando_informacion(getActivity(), false, "");
        } else {
            readdata_buscar_los_precios_de_los_productos(new FirebaseCallBack() {
                @Override
                public void onCallBack(List<String> precios, List<String> productos_faltantes) {
                    metodos.alerdialog_descargando_informacion(getActivity(), false, "");
                    llenar_lv();
                }
            });
        }
    } // si hay productos busca,

    private void readdata_buscar_los_precios_de_los_productos(final FirebaseCallBack firebaseCallBack) {
        hash_ubicaciones_involucradas.clear();
        negocios_involucrados.clear();
        precios.clear();
        int_negocios_o_productos_a_borrar = 0;

        if (pedido_guardado != null) {
            final String[] datos_array = pedido_guardado.split("·");
            DatabaseReference chekear_precios = FirebaseDatabase.getInstance().getReference();
            chekear_precios.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    productos_no_encontrados.clear();

                    for (int i = 0; i < datos_array.length; i++) {
                        //busca cada negocio
                        String[] datos_dentro_de_datos = datos_array[i].split("#");
                        String negocio = datos_dentro_de_datos[0];
                        String tiene_categoria = datos_dentro_de_datos[3];
                        String cantidad = datos_dentro_de_datos[6];

                        negocios_involucrados.add(negocio);

                        //esto busca si el negocio esta disponible
                        String parent_producto = datos_dentro_de_datos[4];
                        String ubicaciones_involucradas = datos_dentro_de_datos[5];
                        hash_ubicaciones_involucradas.put(ubicaciones_involucradas, negocio);


                        String str_negocio_disponible;
                        String precio;
                        String esta_disponible;
                        boolean boolean_negocio_disponible = false;

                        if (tiene_categoria.equals("no")) {
                            str_negocio_disponible = (String) dataSnapshot.child("negocios").child(negocio).child("estado_negocio").getValue();
                            if (str_negocio_disponible != null) {
                                if (str_negocio_disponible.equals("on")) {
                                    boolean_negocio_disponible = true;
                                }
                            }

                            precio = (String) dataSnapshot.child("negocios").child(negocio).child("productos").child(parent_producto).child("precio").getValue();
                            esta_disponible = (String) dataSnapshot.child("negocios").child(negocio).child("productos").child(parent_producto).child("disponible").getValue();

                            if (esta_disponible == null) {
                                esta_disponible = "no";
                            }
                        } else {
                            str_negocio_disponible = (String) dataSnapshot.child("negocios").child(negocio).child("estado_negocio").getValue();
                            if (str_negocio_disponible != null) {
                                if (str_negocio_disponible.equals("on")) {
                                    boolean_negocio_disponible = true;
                                }
                            }

                            precio = (String) dataSnapshot.child("negocios").child(negocio).child("categorias").child(tiene_categoria).child("productos").child(parent_producto).child("precio").getValue();
                            esta_disponible = (String) dataSnapshot.child("negocios").child(negocio).child("categorias").child(tiene_categoria).child("productos").child(parent_producto).child("disponible").getValue();

                            if (esta_disponible == null) {
                                esta_disponible = "no";
                            }
                        }


                        boolean contiene_el_delivery = false;
                        boolean todo_correcto_para_pedir = false;


                        if (getActivity() != null) {
                            todo_correcto_para_pedir = ((MainActivity) getActivity()).boolean_todo_correcto_para_pedir();
                            if (todo_correcto_para_pedir) {
                                contiene_el_delivery = ((MainActivity) getActivity()).array_mainact_negocios_disponibles.contains(negocio); //si no esta disponible en el c_principal que no lo guarde en carrito
                            } else {
                                boolean_negocio_disponible = true;
                                contiene_el_delivery = true;
                            }
                        }

                        if (contiene_el_delivery & boolean_negocio_disponible) {
                            if (precio != null & esta_disponible.equals("si")) {
                                int int_precio = Integer.parseInt(precio);
                                int int_cantidad = Integer.parseInt(cantidad);
                                int int_precio_final = int_precio * int_cantidad;
                                String str_precio_final = String.valueOf(int_precio_final);
                                precios.add(str_precio_final);
                            } else {
                                precios.add("1000");
                                int_negocios_o_productos_a_borrar++;
                                if (!boolean_mostrar_solo_un_alertdialog_para_borrar & todo_correcto_para_pedir) {
                                    alert_dialog_confirmar_borrar_producto_no_encontrado(false, datos_dentro_de_datos[1], i);
                                    boolean_mostrar_solo_un_alertdialog_para_borrar = true;
                                }
                            }
                        } else {
                            precios.add("1000");
                            int_negocios_o_productos_a_borrar++;
                            if (!boolean_mostrar_solo_un_alertdialog_para_borrar | negocio.equals(str_borrar_todos_los_items_de_este_negocio) & todo_correcto_para_pedir) {
                                alert_dialog_confirmar_borrar_producto_no_encontrado(true, negocio, i);
                                boolean_mostrar_solo_un_alertdialog_para_borrar = true;
                            }
                        }
                    }
                    //este callback hace que se ejecute cuando ya esta completa la lista
                    firebaseCallBack.onCallBack(precios, productos_no_encontrados);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        } else {
            Toast.makeText(getActivity(), "sin pedidos", Toast.LENGTH_SHORT).show();
            if (getActivity() != null) {
                metodos.alerdialog_descargando_informacion(getActivity(), false, "");
            }
            players.add("no hay datos guardados");
            String[] pasar_datos_al_adapter = players.toArray(new String[0]);
            final adaptador_grid_carrito adapter_productos = new adaptador_grid_carrito(getActivity(), pasar_datos_al_adapter);
            gridview.setAdapter(adapter_productos);
        }
    } //busca los precios de los productos

    private void alert_dialog_confirmar_borrar_producto_no_encontrado(final boolean es_negocio, final String str_mensaje_borrar, final int numero_index) {
        if (es_negocio & str_mensaje_borrar.equals(str_borrar_todos_los_items_de_este_negocio)) {
            //este loop/if es para que no pregunte multiple veces borrar producto del negocio
            if (pedido_guardado != null) {

                int_negocios_o_productos_a_borrar--;
                String[] datos = pedido_guardado.split("·");
                if (datos.length == 1) {
                    method_borrar_lista_entera();
                    int_negocios_o_productos_a_borrar = 0;
                } else {
                    borrar_item_del_lv(numero_index);
                }
            }
        } else {
            if (es_negocio) {
                String[] opciones = {"Borrar productos del negocio", "Volver a la pagina principal", "cerrar"};
                String titulo = "el negocio " + str_mensaje_borrar + " no se encontro, puede ser debido a que este cerrado";
                alertdialog_listview(titulo, opciones, 0, true, numero_index, str_mensaje_borrar);
            }
            if (!es_negocio) {
                String[] opciones = {"Borrar Producto", "Volver a la pagina principal", "cerrar"};
                String titulo = "el producto " + str_mensaje_borrar + " no esta disponible puede=\n1_que no este disponible actualmente\n2_que se alla modificado/eliminado del negocio";
                alertdialog_listview(titulo, opciones, 1, true, numero_index, str_mensaje_borrar);
            }
        }
    }


    private interface FirebaseCallBack {
        void onCallBack(List<String> precios, List<String> productos_faltantes);
    }

    //GENERAL

    public void llenar_lv() {
        players.clear();
        precio_total_de_los_productos = 0;

        metodos.alerdialog_descargando_informacion(getActivity(), false, "");
        if (pedido_guardado == null) {
            players.add("no hay datos guardados");

            String[] pasar_datos_al_adapter = players.toArray(new String[0]);
            final adaptador_grid_carrito adapter_productos = new adaptador_grid_carrito(getActivity(), pasar_datos_al_adapter);
            gridview.setAdapter(adapter_productos);
        } else {

            String[] datos = pedido_guardado.split("·");
            int cantidad_datos = datos.length;

            if (precios.size() == 0) {
                metodos.alerdialog_descargando_informacion(getActivity(), true, "buscando precios de los productos");
            } else {
                metodos.alerdialog_descargando_informacion(getActivity(), false, "");
            }

            if (cantidad_datos != 0) {
                for (int i = 0; i < cantidad_datos; i++) {
                    String[] datos_dentro_de_datos = datos[i].split("#");

                    String str_precio = "1000";
                    if (precios.size() > 0) {
                        str_precio = precios.get(i);
                    }

                    String nuevo = "Negocio= " + datos_dentro_de_datos[0] + "\nProducto= " + datos_dentro_de_datos[1] +
                            "\nPrecio= " + str_precio;

                    precio_total_de_los_productos = precio_total_de_los_productos + Integer.parseInt(str_precio);

                    players.add(nuevo);

                }
            } else {
                String nuevo = "sin pedidos";
                players.add(nuevo);

            }

            String[] pasar_datos_al_adapter = players.toArray(new String[0]);
            final adaptador_grid_carrito adapter_productos = new adaptador_grid_carrito(getActivity(), pasar_datos_al_adapter);
            gridview.setAdapter(adapter_productos);

            //calcula el precio del delivery
            // Set no deja que se sobreescriban los datos
            Set<String> set = new HashSet<>(negocios_involucrados);
            negocios_involucrados.clear();
            negocios_involucrados.addAll(set);

            TV_precio_final_total.setText(String.valueOf(precio_total_de_los_productos));

            if (getActivity() != null) {
                ((MainActivity) getActivity()).precio_productos = precio_total_de_los_productos;
            }
        }

    }

    private void cambiar_de_fragment(boolean ir_a_ver_progreso_pedido) {
        //este metodo se ejecuta cuando se inicia el fragment o cuando quiero pasar al siguiente

        boolean ir_a_ver_progreso_o_menu_principal = false;

        if (getActivity() != null) {
            ir_a_ver_progreso_o_menu_principal = ((MainActivity) getActivity()).ir_a_ver_progreso_o_al_menu_principal;
        }

        //si quiero guardar= false , si vengo de tengo un pedido activo=true
        if (ir_a_ver_progreso_pedido) {
            //si quiero ir al menu principal =false , si quiero ir a ver el pedido true
            if (ir_a_ver_progreso_o_menu_principal) {
                metodos.main_cambiar_fragment(getActivity(), "c_guardar_pedidos_ver_pedidos");
            } else {
                metodos.main_cambiar_fragment(getActivity(), "c_principal");
            }
        } else {
            if (precios.size() > 0) {
                SharedPreferences.Editor editor = sharpref.edit();
                sharpref.edit().remove("precio").apply();
                editor.putString("precio", precios.toString()).apply();
                precios.clear();
                if (getActivity() != null) {
                    metodos.alerdialog_descargando_informacion(getActivity(), false, "");
                }
                metodos.main_cambiar_fragment(getActivity(), "c_elegir_delivery_confirmacion");
            } else {
                Toast.makeText(getActivity(), "Buscando Precios Actualizados", Toast.LENGTH_SHORT).show();
            }
        }


    }

    private void ir_a_donde_esta_el_prodcuto_method(int n_borrar) {

        String[] datos_array = pedido_guardado.split("·");
        String[] datos_dentro_de_datos = datos_array[n_borrar].split("#");
        String tiene_categoria = datos_dentro_de_datos[3];
        if (tiene_categoria == null) {
            metodos.pasar_datos_productos_negocio = datos_dentro_de_datos[0];
            metodos.main_cambiar_fragment(getActivity(), "c_lista_productos_negocios");
        } else {
            metodos.pasar_datos_categorias_negocios = datos_dentro_de_datos[0] + "," + datos_dentro_de_datos[3];
            metodos.main_cambiar_fragment(getActivity(), "c_lista_categorias_productos_negocios");
        }
    } //sirve para ir al negocio que tiene el producto

    private void alert_dialog(final int int_que_alert_es, String str_titulo, final String str_mensaje, String str_btn_izquierda, String str_btn_medio, String str_btn_derecha, final int position_index, final String negocio) {

        // 1 = alert_borrar_lista_entera
        // 2 = alert_borrar_item

        if (getActivity() != null) {
            final AlertDialog.Builder alerta = new AlertDialog.Builder(getActivity());
            final LayoutInflater inflater = getActivity().getLayoutInflater();
            final View dialogView = inflater.inflate(R.layout.ad_agregar_producto_por_unidad, null);
            alerta.setView(dialogView);
            final AlertDialog alertDialog = alerta.create();
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            TextView tv_titulo = dialogView.findViewById(R.id.TV_alertdialog_agregar_producto_nombre);
            TextView tv_descripcion = dialogView.findViewById(R.id.TV_alertdialog_agregar_producto_precio);
            Button btn_izquierda = dialogView.findViewById(R.id.BTN_alertdialog_ver_detalles_agregar_productos_cerrar);
            Button btn_medio = dialogView.findViewById(R.id.BTN_alertdialog_ver_detalles_agregar_productos_ver_detalles);
            Button btn_derecha = dialogView.findViewById(R.id.BTN_alertdialog_ver_detalles_agregar_productos_agregar_productos);

            LinearLayout Linear_botones = dialogView.findViewById(R.id.LINEAR_alertdialog_agregar_productos);
            final LinearLayout Linear_cantidad = dialogView.findViewById(R.id.ad_agregar_producto_linear_cantidad);
            FrameLayout frame_imagenes = dialogView.findViewById(R.id.frame_imagen);

            frame_imagenes.setVisibility(View.GONE);
            Linear_cantidad.setVisibility(View.GONE);

            //------------------ titulo - mensaje -------------------

            tv_titulo.setText(str_titulo);
            if (str_mensaje != null) {
                tv_descripcion.setText(str_mensaje);
            } else {
                tv_descripcion.setVisibility(View.GONE);
            }

            //------------------ btn_medio -------------------
            if (str_btn_medio != null) {
                btn_medio.setText(str_btn_medio);
                btn_medio.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //if (int_que_alert_es == 1) { "sin btn medio" }
                        if (int_que_alert_es == 2) {
                            ir_a_donde_esta_el_prodcuto_method(position_index);
                            alertDialog.dismiss();
                        }

                    }
                });
            } else {
                btn_medio.setVisibility(View.GONE);
                Linear_botones.setWeightSum(2);
            }

            //------------------ btn_izquierda -------------------

            btn_izquierda.setText(str_btn_izquierda);
            btn_izquierda.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    alertDialog.dismiss();
                }
            });

            //------------------ btn_derecha -------------------

            btn_derecha.setText(str_btn_derecha);
            btn_derecha.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (int_que_alert_es == 1) {
                        method_borrar_lista_entera();
                        alertDialog.dismiss();
                    }
                    if (int_que_alert_es == 2) {
                        borrar_item_del_lv(position_index);
                        alertDialog.dismiss();
                    }
                }
            });
            alertDialog.show();
        }
    }

    AlertDialog alertDialog_listview = null;

    private void alertdialog_listview(final String str_titulo, final String[] items, final int cual_alert_es, Boolean boolean_es_cancelable, final int numero_index, final String negocio) {

        // 0 = alert_dialog_confirmar_borrar_producto_no_encontrado (negocio)
        // 1 = alert_dialog_confirmar_borrar_producto_no_encontrado (producto)

        if (getActivity() != null) {

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

                            int_negocios_o_productos_a_borrar--;
                            String[] datos = pedido_guardado.split("·");
                            if (datos.length == 1) {
                                method_borrar_lista_entera();
                                int_negocios_o_productos_a_borrar = 0;
                            } else {
                                borrar_item_del_lv(numero_index);
                            }
                            str_borrar_todos_los_items_de_este_negocio = negocio;

                            alertDialog_listview.dismiss();
                        }
                        if (position == 1) {
                            metodos.main_cambiar_fragment(getActivity(), "c_principal");

                            alertDialog_listview.dismiss();
                        }
                        if (position == 2) {
                            alertDialog_listview.dismiss();
                        }
                    }
                    if (cual_alert_es == 1) {
                        if (position == 0) {
                            int_negocios_o_productos_a_borrar--;
                            String[] datos = pedido_guardado.split("·");
                            if (datos.length == 1) {
                                method_borrar_lista_entera();
                                int_negocios_o_productos_a_borrar = 0;
                            } else {
                                borrar_item_del_lv(numero_index);
                            }
                            alertDialog_listview.dismiss();
                        }
                        if (position == 1) {
                            metodos.main_cambiar_fragment(getActivity(), "c_principal");

                            alertDialog_listview.dismiss();
                        }
                        if (position == 2) {
                            alertDialog_listview.dismiss();
                        }
                    }
                }
            });
            alertDialog_listview.show();
        }

    }
}

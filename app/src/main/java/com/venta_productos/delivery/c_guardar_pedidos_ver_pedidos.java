package com.venta_productos.delivery;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.venta_productos.delivery.adapter.adaptador_grid_ver_progreso_pedido;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Random;


/**
 * A simple {@link Fragment} subclass.
 */
public class c_guardar_pedidos_ver_pedidos extends Fragment {


    public c_guardar_pedidos_ver_pedidos() {
        // Required empty public constructor
    }

    private ArrayList<String> array_pedido_con_negocios_estado_delivery = new ArrayList<>();
    private ArrayList<String> negocios_pedidos = new ArrayList<>();
    DatabaseReference nDataBase, agregar_dentro_del_pedido, db_ver_estado_del_pedido, chekear_pedidos_activos, db_ver_montos;
    String usuario, n_pedido_random, str_verificar_pedido_activo_n_random, str_ubicacion, str_usuario, str_horario_pedido, str_modo_de_pago, str_anio, str_mes, str_dia;
    TextView prueba1;
    GridView grid_progreso_pedido;
    Button btn_actualizar_pedido, btn_ir_al_menu_principal;
    int pedido_hora, pedido_minuto;
    boolean cargar_unasola_vez_la_actualziacion = true;

    SharedPreferences sharpref;
    Context contex;
    String pedido_guardado = null, ubicacion_para_el_pedido;
    HashMap<String, String> hash_pedido_guardado = new HashMap<>();

    String TAG = "asdf";

    //buscar pedidos activos
    int int_verificar_el_ultimo_pedido = 1; //ponerle un numero de veces para que se repita el loop sin guardar los datos en la fb..
    boolean hay_un_pedido_activo = false;
    String datos_para_verificar, str_get_anio, str_get_mes, str_get_dia;


    //ver montos
    Button btn_ver_montos;

    //llenar el adapter
    String str_ingresar_al_adapter_negocios_con_sus_productos = "";
    String str_ingresar_al_adapter_delivery = "";
    String str_ingresar_al_adapter_estado = "";

    boolean boolean_se_cargo_los_productos = false;
    boolean boolean_se_cargo_el_delivery = false;
    boolean boolean_se_cargo_el_estado = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View Ver_progreso_pedido = inflater.inflate(R.layout.f_ver_progreso_del_pedido, container, false);

        if (getActivity() != null) {
            ((MainActivity) getActivity()).settitletoolbar("Progreso pedido");
        }

        prueba1 = Ver_progreso_pedido.findViewById(R.id.texto1);
        grid_progreso_pedido = Ver_progreso_pedido.findViewById(R.id.gridview_progreso_pedido);
        btn_actualizar_pedido = Ver_progreso_pedido.findViewById(R.id.BTN_progreso_pedido_actualizar);
        btn_ver_montos = Ver_progreso_pedido.findViewById(R.id.BTN_progreso_pedido_ver_montos);
        btn_ir_al_menu_principal = Ver_progreso_pedido.findViewById(R.id.BTN_progreso_pedido_ir_al_menu_principal);

        contex = getActivity();
        sharpref = getContext().getSharedPreferences("usar_app", Context.MODE_PRIVATE);
        pedido_guardado = sharpref.getString("pedido", null);

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR_OF_DAY, -4); //esto deberia restarle 4 horas al calendario asi hasta las 4 am, sigue siendo el mismo dia, para pedidos nocturnos
        int year = cal.get(Calendar.YEAR);
        int dayofmonth = cal.get(Calendar.DAY_OF_MONTH);
        int month = cal.get(Calendar.MONTH);
        pedido_hora = cal.get(Calendar.HOUR_OF_DAY) + 4;
        pedido_minuto = cal.get(Calendar.MINUTE);

        str_anio = String.valueOf(year);
        str_mes = String.valueOf(month + 1);
        str_dia = String.valueOf(dayofmonth);

        metodos.alerdialog_descargando_informacion(getActivity(), true, "Actualizando pedido");

        db_ver_estado_del_pedido = FirebaseDatabase.getInstance().getReference().child("pedidos").child(str_anio).child(str_mes).child(str_dia);
        //si tengo un pedido guardado busco la fecha

        datos_para_verificar = sharpref.getString("verificar_pedidos_activos", null);
        if (datos_para_verificar != null) {
            String[] datos = datos_para_verificar.split(",");
            String[] getdate = datos[0].split("-");
            str_get_anio = getdate[0];
            str_get_mes = getdate[1];
            str_get_dia = getdate[2];
        }


        //armo el pedido
        if (pedido_guardado != null) {
            //le saco las {}
            pedido_guardado = pedido_guardado.substring(1, pedido_guardado.length() - 1);

            //harmo el hashmap, para luego buscar la ubicacion que preciso
            String[] cantidad_ubicaciones = pedido_guardado.split(",");
            for (int g = 0; g < cantidad_ubicaciones.length; g++) {
                String[] dividir_key_y_valor = cantidad_ubicaciones[g].split("=");
                String mejorar_key = dividir_key_y_valor[0].replace(" ", "");
                if (dividir_key_y_valor[1].equals("null")) {
                    dividir_key_y_valor[1] = null;
                }
                hash_pedido_guardado.put(mejorar_key, dividir_key_y_valor[1]);
            }

            //saco la ubicacion que preciso del hashmap
            ubicacion_para_el_pedido = ((MainActivity) getActivity()).ubicacion_elegida;
            String ubicacion_mejorada = ubicacion_para_el_pedido.replace(" ", "");
            pedido_guardado = hash_pedido_guardado.get(ubicacion_mejorada);

        }

        method_para_verificar_pedidos_activos();

        String[] datos = sharpref.getString("usuario", "no hay dato").split(",");
        usuario = datos[0];
        prueba1.setText("Numero de telefono= " + usuario);

        btn_actualizar_pedido.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "Actualizando pedido", Toast.LENGTH_SHORT).show();
                ver_estado_pedido();
                method_iniciar_servicio();
            }
        });
        btn_ir_al_menu_principal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                metodos.main_cambiar_fragment(getActivity(), "c_principal");
            }
        });

        btn_ver_montos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                metodos.alerdialog_descargando_informacion(getActivity(), true, "Buscando montos");
                method_ver_montos(new FirebaseCallBack_ver_montos() {
                    @Override
                    public void onCallBack_montos(HashMap<String, String[]> hash_ver_montos) {
                        metodos.alerdialog_descargando_informacion(getActivity(), false, "");
                        alert_dialog_ver_montos(hash_ver_montos);
                    }
                });
            }
        });


        if (getActivity() != null) {
            ((MainActivity) getActivity()).chekear_internet();
            ((MainActivity) getActivity()).ir_a_ver_progreso_o_al_menu_principal = false;
        }

        return Ver_progreso_pedido;
    }

    private void method_ver_montos(final FirebaseCallBack_ver_montos callBack_ver_montos) {

        //ver el dia y los numero random
        final HashMap<String, String[]> hash_ver_montos = new HashMap<>();
        db_ver_montos = FirebaseDatabase.getInstance().getReference();
        final String datos_para_verificar = sharpref.getString("verificar_pedidos_activos", null);

        if (datos_para_verificar != null) {
            final String[] datos = datos_para_verificar.split(",");
            str_verificar_pedido_activo_n_random = datos[1];

            db_ver_montos.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    String str_interes_aplicado = (String) dataSnapshot.child("precio_delivery").child("comision_tarjeta").getValue();
                    double double_interes_aplicado = Double.parseDouble(str_interes_aplicado);

                    DataSnapshot snap_pedidos = dataSnapshot.child("pedidos").child(str_anio).child(str_mes).child(str_dia);
                    for (int cant = 1; cant < datos.length; cant++) {
                        DataSnapshot snap_numero_random = snap_pedidos.child(datos[cant]);


                        String delivery = (String) snap_numero_random.child("delivery").getValue();

                        String total_cobrar = (String) snap_numero_random.child("montos").child("total_cobrar").getValue();
                        String forma_de_pago = (String) snap_numero_random.child("montos").child("forma_de_pago").getValue();

                        int int_suma_productos = 0;
                        for (DataSnapshot snap_montos_negocios : snap_numero_random.child("montos").child("negocios").getChildren()) {
                            for (DataSnapshot snap_negocios_productos : snap_montos_negocios.getChildren()) {
                                String precios_productos = (String) snap_negocios_productos.getValue();
                                if (precios_productos != null) {
                                    precios_productos = precios_productos.replace(" ", "");
                                }
                                int_suma_productos = int_suma_productos + Integer.valueOf(precios_productos);
                            }
                        }
                        if (forma_de_pago != null) {
                            if (forma_de_pago.equals("tarjeta")) {
                                int_suma_productos = (int) (int_suma_productos * double_interes_aplicado);
                            } else {
                                Toast.makeText(getActivity(), "error con la forma de pago", Toast.LENGTH_LONG).show();
                            }
                        }

                        int int_precio_servicio_y_delivery = Integer.valueOf(total_cobrar) - int_suma_productos;

                        hash_ver_montos.put(delivery, new String[]{total_cobrar, forma_de_pago, String.valueOf(int_suma_productos), String.valueOf(int_precio_servicio_y_delivery)});
                    }
                    callBack_ver_montos.onCallBack_montos(hash_ver_montos);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


        } else {
            Toast.makeText(getActivity(), "no ha guardado un pedido", Toast.LENGTH_SHORT).show();
        }
    }

    private void alert_dialog_ver_montos(HashMap<String, String[]> hash_ver_montos) {

        if (getActivity() != null) {
            final AlertDialog.Builder alerta = new AlertDialog.Builder(getActivity());

            String mensaje = "";
            for (String deliverys : hash_ver_montos.keySet()) {
                String[] imput = hash_ver_montos.get(deliverys);
                String int_total = imput[0];
                String forma_de_pago = imput[1];
                String suma_productos = imput[2];
                String precio_servicio = imput[3];

                mensaje = mensaje + "Delivery = " + deliverys + "\nsuma compra= " + int_total + "\nsuma productos= " + suma_productos + "\nPrecio servicio= " + precio_servicio + "\nForma de pago= " + forma_de_pago + "\n\n";

            }

            LayoutInflater inflater = getActivity().getLayoutInflater();
            final View dialogView = inflater.inflate(R.layout.ad_basico_titulo_texto_botones, null);
            alerta.setView(dialogView);
            final AlertDialog alertDialog = alerta.create();
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            TextView tv_titulo = dialogView.findViewById(R.id.ad_tv_titulo);
            TextView tv_mensaje = dialogView.findViewById(R.id.ad_tv_mensaje);
            Button btn_aceptar = dialogView.findViewById(R.id.btn_aceptar);
            Button btn_cancelar = dialogView.findViewById(R.id.btn_cancelar);

            tv_titulo.setText("Montos=");
            tv_mensaje.setText(mensaje);
            btn_aceptar.setText("cerrar");
            btn_cancelar.setVisibility(View.GONE);

            btn_aceptar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    alertDialog.dismiss();
                }
            });

            alertDialog.show();
        }
    }

    private interface FirebaseCallBack_ver_montos {
        void onCallBack_montos(HashMap<String, String[]> hash_ver_montos);
    }

    private void method_para_verificar_pedidos_activos() {
        if (datos_para_verificar == null) {
            //si alguna vez guardo datos en sharedpref
            int_verificar_el_ultimo_pedido--;
            if (int_verificar_el_ultimo_pedido == 0) {
                guardar_pedido();
            }
        } else {
            String[] datos = datos_para_verificar.split(",");
            str_verificar_pedido_activo_n_random = datos[1];
            if (!str_anio.equals(str_get_anio) | !str_mes.equals(str_get_mes) | !str_dia.equals(str_get_dia)) {
                //si no es el dia =false
                int_verificar_el_ultimo_pedido--;
                if (int_verificar_el_ultimo_pedido == 0) {
                    guardar_pedido();
                }
            } else {
                if (datos.length > 2) {
                    int_verificar_el_ultimo_pedido = datos.length - 1;

                    for (int n_veces_random = 1; n_veces_random < datos.length; n_veces_random++) {
                        str_verificar_pedido_activo_n_random = datos[n_veces_random];
                        method_verificar_pedidos_repetir_si_es_necesario();
                    }
                } else {
                    method_verificar_pedidos_repetir_si_es_necesario();
                }
            }
        }
    }

    private void method_verificar_pedidos_repetir_si_es_necesario() {

        final List<String> myArrayList = Arrays.asList(getResources().getStringArray(R.array.array_posibles_estados_del_servicio_que_llevan_a_ver_progreso_pedido));

        buscar_pedido_activo_en_firebase(new FirebaseCallBack() {
            @Override
            public void onCallBack(String estado) {
//                if (estado.equals("on")) { // original
                if (myArrayList.contains(estado)) {
                    int_verificar_el_ultimo_pedido--; //si hay un pedido activo que no se repita el loop
                    hay_un_pedido_activo = true;
                    if (int_verificar_el_ultimo_pedido == 0) { //solo si llega al ultimo loop o no tiene multy delivery se carge
                        ver_estado_pedido();
                    }

                } else {
                    int_verificar_el_ultimo_pedido--; //le saco una busqueda para que repita el loop..
                    if (int_verificar_el_ultimo_pedido == 0) { //solo si llega al ultimo loop o no tiene multy delivery se carge
                        if (hay_un_pedido_activo) {
                            ver_estado_pedido();
                        } else {
                            guardar_pedido();
                        }
                    }
                }
                if (getActivity() != null) {
                    metodos.alerdialog_descargando_informacion(getActivity(), false, "");
                }
            }
        });
    }

    private void buscar_pedido_activo_en_firebase(final FirebaseCallBack firebaseCallBack) {
        //chekea si hay datos
        chekear_pedidos_activos = FirebaseDatabase.getInstance().getReference().child("pedidos").child(str_anio).child(str_mes).child(str_dia).child(str_verificar_pedido_activo_n_random).child("pedido_en_actividad");
        chekear_pedidos_activos.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String estado = (String) dataSnapshot.getValue();
                if (estado == null) {
                    int_verificar_el_ultimo_pedido--;
                    if (int_verificar_el_ultimo_pedido == 0) {
                        guardar_pedido();
                    }
                    if (getActivity() != null) {
                        metodos.alerdialog_descargando_informacion(getActivity(), false, "");
                    }
                } else {
                    firebaseCallBack.onCallBack(estado);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private interface FirebaseCallBack {
        void onCallBack(String estado);
    }

    private int generar_numero() {
        final int min = 1000;
        final int max = 8000;
        final int suma = new Random().nextInt((max - min) + 1) + min;

        int nueva_hora = 50 - pedido_hora;
        int nueva_minuto = 90 - pedido_minuto;

        String str_nueva_hora = String.valueOf(nueva_hora);
        String str_nuevo_minuto = String.valueOf(nueva_minuto);
        if (nueva_hora < 10) {
            str_nueva_hora = "0" + str_nueva_hora;
        }
        if (nueva_minuto < 10) {
            if (nueva_minuto == 0) {
                str_nuevo_minuto = "00";
            } else {
                str_nuevo_minuto = "0" + str_nuevo_minuto;
            }
        }

        String agregar_hora = str_nueva_hora + str_nuevo_minuto + String.valueOf(suma);

        return Integer.parseInt(agregar_hora);
    }

    private void ver_estado_pedido() {
        array_pedido_con_negocios_estado_delivery.clear();
        negocios_pedidos.clear();

        final String datos_para_verificar = sharpref.getString("verificar_pedidos_activos", "no hay dato");
        final String[] dividir_pedidos = datos_para_verificar.split(",");
        for (int n_veces_random = 1; n_veces_random < dividir_pedidos.length; n_veces_random++) {

            str_verificar_pedido_activo_n_random = dividir_pedidos[n_veces_random];

            boolean_se_cargo_los_productos = false;
            boolean_se_cargo_el_delivery = false;
            boolean_se_cargo_el_estado = false;
            str_ingresar_al_adapter_negocios_con_sus_productos = " ";
            str_ingresar_al_adapter_delivery = " ";
            str_ingresar_al_adapter_estado = " ";

            DatabaseReference ver_estado_del_pedido = db_ver_estado_del_pedido.child(str_verificar_pedido_activo_n_random);
            final int finalN_veces_random = n_veces_random;

            ver_estado_del_pedido.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {

                    String key = dataSnapshot.getKey();

                    if (key != null) {
                        if (key.equals("pedido_en_actividad")) {
                            str_ingresar_al_adapter_estado = (String) dataSnapshot.getValue();
                            boolean_se_cargo_el_estado = true;
                            if (boolean_se_cargo_el_delivery & boolean_se_cargo_el_estado & boolean_se_cargo_los_productos & dividir_pedidos.length == finalN_veces_random + 1) {
                                String todo_junto = str_ingresar_al_adapter_delivery + "€€€" + str_ingresar_al_adapter_estado + "€€€" + str_ingresar_al_adapter_negocios_con_sus_productos;
                                array_pedido_con_negocios_estado_delivery.add(todo_junto);
                                llenar_el_grid_view();
                            }
                        }
                        if (key.equals("delivery")) {
                            str_ingresar_al_adapter_delivery = (String) dataSnapshot.getValue();
                            boolean_se_cargo_el_delivery = true;
                            if (boolean_se_cargo_el_delivery & boolean_se_cargo_el_estado & boolean_se_cargo_los_productos & dividir_pedidos.length == finalN_veces_random + 1) {
                                String todo_junto = str_ingresar_al_adapter_delivery + "€€€" + str_ingresar_al_adapter_estado + "€€€" + str_ingresar_al_adapter_negocios_con_sus_productos;
                                array_pedido_con_negocios_estado_delivery.add(todo_junto);
                                llenar_el_grid_view();
                            }
                        }

                        if (key.equals("negocios")) {

                            for (DataSnapshot snap_negocios : dataSnapshot.getChildren()) {
                                String negocio = snap_negocios.getKey();
                                String estado = (String) snap_negocios.child("estado").getValue();
                                for (DataSnapshot snap_pedidos_productos : snap_negocios.getChildren()) {
                                    //para saber los pedidos dentro de key_que es negocio
                                    String pedidos = (String) snap_pedidos_productos.getValue();
                                    negocios_pedidos.add(pedidos);
                                }
                                if (!negocios_pedidos.toString().equals("[pedido enviado]")) {

                                    String str_agregar_negocio_con_productos = negocio + "€" + estado + "€" + negocios_pedidos + "€€";
                                    str_ingresar_al_adapter_negocios_con_sus_productos = str_ingresar_al_adapter_negocios_con_sus_productos + str_agregar_negocio_con_productos;

                                }
                                negocios_pedidos.clear();
                            }
                            boolean_se_cargo_los_productos = true;
                            if (boolean_se_cargo_el_delivery & boolean_se_cargo_el_estado & boolean_se_cargo_los_productos & dividir_pedidos.length == finalN_veces_random + 1) {
                                String todo_junto = str_ingresar_al_adapter_delivery + "€€€" + str_ingresar_al_adapter_estado + "€€€" + str_ingresar_al_adapter_negocios_con_sus_productos;
                                array_pedido_con_negocios_estado_delivery.add(todo_junto);
                                llenar_el_grid_view();
                            }
                        }
                    } else {
                        Toast.makeText(getActivity(), "Error inesperado buscando el pedido :(", Toast.LENGTH_LONG).show();
                    }

                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String s) {
                    if (cargar_unasola_vez_la_actualziacion) {

                        cargar_unasola_vez_la_actualziacion = false;
                        ver_estado_pedido();

                    }
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });

        }

    }


    private void llenar_el_grid_view() {

        metodos.alerdialog_descargando_informacion(getActivity(), false, "");

        String[] pasar_datos_al_adapter = array_pedido_con_negocios_estado_delivery.toArray(new String[0]);
        final adaptador_grid_ver_progreso_pedido adapter_productos = new adaptador_grid_ver_progreso_pedido(getActivity(), pasar_datos_al_adapter);
        grid_progreso_pedido.setAdapter(adapter_productos);
        cargar_unasola_vez_la_actualziacion = true;

    }


    private void guardar_pedido() {

        if (getActivity() != null) {

            final String[] delivery = ((MainActivity) getActivity()).delivery_elegido.split(",");
            str_modo_de_pago = ((MainActivity) getActivity()).str_main_modo_de_pago;

            if (delivery.length > 1) {
                method_guardar_multiple_deliverys();


            } else {

                final String unico_delivery = ((MainActivity) getActivity()).delivery_elegido;

                final String precio_guerdado = sharpref.getString("precio", "no hay dato");

                if (pedido_guardado.equals("no hay dato")) {
                    Toast.makeText(getActivity(), "no hay informacion para mandar, su pedido ya fue guardado", Toast.LENGTH_SHORT).show();
                } else {
                    n_pedido_random = String.valueOf(generar_numero());
                    str_verificar_pedido_activo_n_random = n_pedido_random;

                    nDataBase = FirebaseDatabase.getInstance().getReference().child("pedidos").child(str_anio).child(str_mes).child(str_dia).child(n_pedido_random);
                    nDataBase.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            agregar_dentro_del_pedido = nDataBase;

                            //datos
                            str_ubicacion = ((MainActivity) getActivity()).ubicacion_calle_y_altura;
                            str_usuario = sharpref.getString("usuario", "no hay dato");

                            Calendar cal = Calendar.getInstance();
                            long time_in_ms = cal.getTimeInMillis();

                            //para arreglar el bug que si es <10 me aparece como x ej= 12:8
                            String minuto = String.valueOf(pedido_minuto);
                            if (minuto.length() == 1) {
                                minuto = "0" + minuto;
                            }
                            if (minuto.length() == 0) {
                                minuto = "00";
                            }
                            str_horario_pedido = String.valueOf(pedido_hora) + ":" + minuto;


                            agregar_dentro_del_pedido.child("cliente").setValue(str_usuario);
                            agregar_dentro_del_pedido.child("pedido_en_actividad").setValue("confirmar pedido");
                            agregar_dentro_del_pedido.child("lugar_entrega").setValue(str_ubicacion);
                            agregar_dentro_del_pedido.child("hora_pedido").setValue(str_horario_pedido + " ");
                            agregar_dentro_del_pedido.child("delivery").setValue(unico_delivery);
                            agregar_dentro_del_pedido.child("timers").child("en_ms").child("1_horario_pedido").setValue(time_in_ms);
                            agregar_dentro_del_pedido.child("timers").child("hora_normal").child("1_horario_pedido").setValue(str_horario_pedido);

                            //podria manejar el total cobrar en pedidos y el desglose de los montos en una pestaña aparte, aunque sea medio molesto
                            int[] montos = ((MainActivity) getActivity()).devolver_montos();
                            // montos[0]=monto total montos[1]= monto delivery montos[2]=monto comision

                            agregar_dentro_del_pedido.child("montos").child("total_cobrar").setValue(String.valueOf(montos[0]));
                            agregar_dentro_del_pedido.child("montos").child("delivery").setValue(String.valueOf(montos[1]));
                            agregar_dentro_del_pedido.child("montos").child("comision_app").setValue(String.valueOf(montos[2]));
                            agregar_dentro_del_pedido.child("montos").child("forma_de_pago").setValue(str_modo_de_pago);

                            //guardo las variables dia_actual y n_pedido_random para verificar luego si estan activas
                            SharedPreferences.Editor editor = sharpref.edit();
                            String str_dia_actual = str_anio + "-" + str_mes + "-" + str_dia;
                            editor.putString("verificar_pedidos_activos", str_dia_actual + "," + n_pedido_random);
                            editor.apply();


                            //separo los pedidos
                            String[] datos = pedido_guardado.split("·");
                            String precio_guerdado_2 = precio_guerdado.substring(1, precio_guerdado.length() - 1);
                            String[] precios = precio_guerdado_2.split(",");
                            int cantidad_datos = datos.length;

                            for (int i = 0; i < cantidad_datos; i++) {
                                String[] datos_dentro_de_datos = datos[i].split("#");
                                String nuevo = "";

                                String nombre_empresa = datos_dentro_de_datos[0];
                                String n_producto = String.valueOf(i);

                                nuevo = "Producto= " + datos_dentro_de_datos[1] + " Precio= " + precios[i] + " ";

                                agregar_dentro_del_pedido.child("negocios").child(nombre_empresa).child("estado").setValue("pedido enviado");
                                agregar_dentro_del_pedido.child("negocios").child(nombre_empresa).child(n_producto).setValue(nuevo);
                                agregar_dentro_del_pedido.child("montos").child("negocios").child(nombre_empresa).child(n_producto).setValue(precios[i]);
                            }
                            metodos.alerdialog_descargando_informacion(getActivity(), false, "");
                            ver_estado_pedido();
                            Toast.makeText(getActivity(), "todo guardado correctamente", Toast.LENGTH_SHORT).show();
                            guardar_el_pedido_en_el_usuario();
                            method_iniciar_servicio();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
                }
            }
        } else {
            Toast.makeText(getActivity(), "Error guardando el pedido", Toast.LENGTH_SHORT).show();
        }
    }

    private void method_guardar_multiple_deliverys() {

        str_verificar_pedido_activo_n_random = null;

        if (getActivity() != null) {

            final HashMap<String, String> hash_deliverys_y_negocios = ((MainActivity) getActivity()).hash_multiple_delivery_mainact;


            for (final String h_s_delivery : hash_deliverys_y_negocios.keySet()) {

                n_pedido_random = String.valueOf(generar_numero());
                if (str_verificar_pedido_activo_n_random == null) {
                    str_verificar_pedido_activo_n_random = n_pedido_random;
                } else {
                    str_verificar_pedido_activo_n_random = str_verificar_pedido_activo_n_random + "," + n_pedido_random;
                }

                final DatabaseReference nDataBase_multiple = FirebaseDatabase.getInstance().getReference().child("pedidos").child(str_anio).child(str_mes).child(str_dia).child(n_pedido_random);
                nDataBase_multiple.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        DatabaseReference agregar_dentro_del_pedido_multiple = nDataBase_multiple;

                        //datos
                        str_ubicacion = ((MainActivity) getActivity()).ubicacion_calle_y_altura;
//                        str_ubicacion = ((MainActivity) getActivity()).ubicacion_elegida;
                        str_usuario = sharpref.getString("usuario", "no hay dato");

                        //para arreglar el bug que si es <10 me aparece como x ej= 12:8
                        String minuto = String.valueOf(pedido_minuto);
                        if (minuto.length() == 1) {
                            minuto = "0" + minuto;
                        }
                        if (minuto.length() == 0) {
                            minuto = "00";
                        }
                        str_horario_pedido = String.valueOf(pedido_hora) + ":" + minuto;

                        agregar_dentro_del_pedido_multiple.child("cliente").setValue(str_usuario);
                        agregar_dentro_del_pedido_multiple.child("pedido_en_actividad").setValue("confirmar pedido");



                        agregar_dentro_del_pedido_multiple.child("lugar_entrega").setValue(str_ubicacion);
                        agregar_dentro_del_pedido_multiple.child("hora_pedido").setValue(str_horario_pedido + " ");
                        agregar_dentro_del_pedido_multiple.child("delivery").setValue(h_s_delivery);

                        //separo los pedidos
                        final String precio_guerdado = sharpref.getString("precio", "no hay dato");
                        String[] datos = pedido_guardado.split("·");
                        String precio_guerdado_2 = precio_guerdado.substring(1, precio_guerdado.length() - 1);
                        String[] precios = precio_guerdado_2.split(",");
                        int cantidad_datos = datos.length;

                        String input_negocios = hash_deliverys_y_negocios.get(h_s_delivery);

                        int int_sumar_los_precios_de_los_productos = 0;

                        String[] negocios_array = input_negocios.split("€");

                        for (int i = 0; i < cantidad_datos; i++) {
                            String[] datos_dentro_de_datos = datos[i].split("#");
                            String nuevo = "";

                            String nombre_empresa = datos_dentro_de_datos[0];

                            boolean contiene_el_negocio = Arrays.asList(negocios_array).contains(nombre_empresa);

                            if (contiene_el_negocio) {

                                String n_producto = String.valueOf(i);
                                nuevo = "Producto= " + datos_dentro_de_datos[1] + " Precio= " + precios[i] + " ";

                                agregar_dentro_del_pedido_multiple.child("negocios").child(nombre_empresa).child("estado").setValue("pedido enviado"); //este lo puedo agregar con los hashmaps
                                agregar_dentro_del_pedido_multiple.child("negocios").child(nombre_empresa).child(n_producto).setValue(nuevo); //puedo hacer un arraylist con los valores
                                agregar_dentro_del_pedido_multiple.child("montos").child("negocios").child(nombre_empresa).child(n_producto).setValue(precios[i]);

                                String mejorar_el_precio = precios[i].replace(" ", "");

                                int_sumar_los_precios_de_los_productos = int_sumar_los_precios_de_los_productos + Integer.parseInt(mejorar_el_precio);
                            }
                        }
                        Integer[] montos = ((MainActivity) getActivity()).hash_multiples_deliverys_montos.get(h_s_delivery);
                        // montos[0]=monto total montos[1]= monto delivery montos[2]=monto comision

                        int total_delivery_servicio_y_productos;

                        int int_delivery_mas_servicio = montos[0];
                        if (str_modo_de_pago.equals("efectivo")) {
                            total_delivery_servicio_y_productos = int_delivery_mas_servicio + int_sumar_los_precios_de_los_productos;
                            agregar_dentro_del_pedido_multiple.child("montos").child("total_cobrar").setValue(String.valueOf(total_delivery_servicio_y_productos));

                        }
                        if (str_modo_de_pago.equals("tarjeta")) {
                            //tengo el interes
                            double interes_aplicado = ((MainActivity) getActivity()).double_interes_aplicado;

                            //solo le aplico el interes a los productos, ya que el servicio y el delivery ya se los aplique en c_confirmacion
                            double double_precio_final = (int_sumar_los_precios_de_los_productos * interes_aplicado) + int_delivery_mas_servicio;

                            //tengo el resultado
                            int int_precio_final_tarjeta = ((int) double_precio_final);

                            agregar_dentro_del_pedido_multiple.child("montos").child("total_cobrar").setValue(String.valueOf(int_precio_final_tarjeta));

                        }


                        agregar_dentro_del_pedido_multiple.child("montos").child("delivery").setValue(String.valueOf(montos[1]));
                        agregar_dentro_del_pedido_multiple.child("montos").child("comision_app").setValue(String.valueOf(montos[2]));
                        agregar_dentro_del_pedido_multiple.child("montos").child("forma_de_pago").setValue(str_modo_de_pago);

                        guardar_el_pedido_en_el_usuario();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


            }

            //guardo las variables dia_actual y n_pedido_random para verificar luego si estan activas
            SharedPreferences.Editor editor = sharpref.edit();
            String str_dia_actual = str_anio + "-" + str_mes + "-" + str_dia;
            editor.putString("verificar_pedidos_activos", str_dia_actual + "," + str_verificar_pedido_activo_n_random);
            editor.apply();
            ver_estado_pedido();
        }
    }

    private void guardar_el_pedido_en_el_usuario() {
        String[] usuario = str_usuario.split(",");
        DatabaseReference guardar = FirebaseDatabase.getInstance().getReference().child("usuario").child("clientes").child(usuario[1]).child("pedidos").child(str_anio).child(str_mes).child(str_dia);
        guardar.push().child("n_random").setValue(n_pedido_random);
    }

    private void method_iniciar_servicio() {
        if (getActivity() != null) {
            ((MainActivity) getActivity()).method_iniciar_servicio();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (str_ingresar_al_adapter_estado.equals("finalizado")) {
            String puntuar_pedido = sharpref.getString("puntuar_pedido", null);
            if (puntuar_pedido != null) {
                String[] obtener_datos = puntuar_pedido.split(",");
                String obtener_dia = obtener_datos[2];
                if (str_dia.equals(obtener_dia)) {
                    metodos.method_puntuar_pedido(getActivity(), puntuar_pedido, usuario);
                }
            }
        }
    }
}

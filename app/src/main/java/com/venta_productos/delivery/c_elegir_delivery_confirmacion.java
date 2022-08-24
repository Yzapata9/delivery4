package com.venta_productos.delivery;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.snatik.polygon.Point;
import com.snatik.polygon.Polygon;
import com.venta_productos.delivery.adapter.adaptador_grid_elegir_delivery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;


/**
 * A simple {@link Fragment} subclass.
 */
public class c_elegir_delivery_confirmacion extends Fragment {


    public c_elegir_delivery_confirmacion() {
        // Required empty public constructor
    }

    DatabaseReference buscar_datos, buscar_negocios, buscar_precio_servicio;

    Context contex;
    SharedPreferences sharpref;

    String TAG = "asdf";

    HashMap<String, String> hash_negocios = new HashMap<>();
    HashMap<String, String> hash_negocios_gps = new HashMap<>();

    String ubicacion_para_el_pedido, precio_productos;
    HashMap<String, String> hash_pedido_guardado = new HashMap<>();
    String pedido_guardado = null;

    TextView tv_ubicacion_cliente, tv_precio_productos, tv_confirmacion_precio_final, tv_confirmacion_cantidad_negocios;

    //forma de pago
    Button btn_modo_de_pago_efectivo, btn_modo_de_pago_tarjeta, btn_modo_de_pago_online;

    String deliverys_fragment_principal;

    //precio del servicio
    int int_precio_servicio;
    double double_comision_tarjeta;
    double double_precio_final;
    TextView tv_comision_aplicada;
    int precio_elegido_final_total_no_jodas_mas;

    //llenar gridview
    GridView gridview;

    //elegir delivery
    HashMap<String, String> hash_delivery_cubre_el_negocio = new HashMap<>();
    ArrayList<String> array_grid_delivery_elegir = new ArrayList<>();
    ArrayList<Integer> array_precio_deliverys_total = new ArrayList<>();
    ArrayList<Integer> array_precio_deliverys_por_negocio = new ArrayList<>();

    Button btn_confirmacion_siguiente;

    //alert confirmacion
    String str_delivery_seleccionado = null, str_forma_de_pago = null;
    int int_precio_final_efectivo, int_cantidad_productos = 0, int_precio_delivery_mas_servicio, int_precio_final_tarjeta;

    //ponerle un fondo al adapter cuando se clickea
    ArrayList<String> pasarle_datos_al_adapter = new ArrayList<>();

    //ver el dia actual
    int year, month, dayofmonth;
    String str_anio, str_mes, str_dia;

    //buscar pedidos en espera
    int int_pedidos_en_espera = 0;
    final HashMap<String, Integer> hash_delivery_espera = new HashMap<>();
    final HashMap<String, Integer> hash_delivery_espera_pasar_al_adapter = new HashMap<>();
    HashMap<String, Integer> hash_deliverys_prioridad = new HashMap<>();
    HashMap<String, Integer> hash_deliverys_multiple_precio = new HashMap<>();

    //multiple deliverys
    boolean boolean_tiene_multiple_delivery = false, boolean_multiple_delivery_estoy_de_acuerdo = false;
    LinearLayout linear_multiple_delivery;
    Button btn_multiple_delivery;
    TextView tv__multiple_delivery;

    //que no se ejecute nada si quiero ir desde progreso pedido a c_principal
    boolean no_ejecutar_el_codigo_xq_quiero_ir_al_fragment_princiapal = false;

    List<String> myArrayList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View Confirmacion_botones = inflater.inflate(R.layout.f_confirmacion_pedido, container, false);
        if (getActivity() != null) {
            ((MainActivity) getActivity()).settitletoolbar("Confirmar");
            metodos.alerdialog_descargando_informacion(getActivity(), true, "buscando deliverys");
        }

        myArrayList = Arrays.asList(getResources().getStringArray(R.array.array_posibles_estados_del_servicio_que_llevan_a_ver_progreso_pedido));

        cambiar_de_fragment(false);

        gridview = Confirmacion_botones.findViewById(R.id.lvv);

        tv_ubicacion_cliente = Confirmacion_botones.findViewById(R.id.tv_confirmacion_ubicacion_cliente);
        tv_precio_productos = Confirmacion_botones.findViewById(R.id.tv_confirmacion_precio_productos);
        tv_confirmacion_precio_final = Confirmacion_botones.findViewById(R.id.tv_confirmacion_precio_final);
        tv_confirmacion_cantidad_negocios = Confirmacion_botones.findViewById(R.id.tv_confirmacion_cantidad_negocios);
        btn_confirmacion_siguiente = Confirmacion_botones.findViewById(R.id.btn_confirmacion_siguiente);

        btn_modo_de_pago_efectivo = Confirmacion_botones.findViewById(R.id.btn_modo_de_pago_efectivo);
        btn_modo_de_pago_tarjeta = Confirmacion_botones.findViewById(R.id.btn_modo_de_pago_tarjeta);
        btn_modo_de_pago_online = Confirmacion_botones.findViewById(R.id.btn_modo_de_pago_online);

        linear_multiple_delivery = Confirmacion_botones.findViewById(R.id.linear_confirmacion_multiple_deliverys);
        btn_multiple_delivery = Confirmacion_botones.findViewById(R.id.btn_confirmacion_multiples_deliverys);
        tv__multiple_delivery = Confirmacion_botones.findViewById(R.id.tv_confirmacion_alerta_multiples_deliverys);
        tv_comision_aplicada = Confirmacion_botones.findViewById(R.id.tv_confirmacion_interes_aplicado_tarjeta);

        buscar_datos = FirebaseDatabase.getInstance().getReference();
        buscar_negocios = FirebaseDatabase.getInstance().getReference().child("negocios");
        buscar_precio_servicio = FirebaseDatabase.getInstance().getReference().child("precio_delivery");

        ubicacion_para_el_pedido = ((MainActivity) getActivity()).ubicacion_elegida;

        precio_productos = String.valueOf(((MainActivity) getActivity()).precio_productos);

        tv_ubicacion_cliente.setText(ubicacion_para_el_pedido);
        tv_precio_productos.setText(precio_productos);

        //ver cuantos pedidos pendiente/s tiene y el precio

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR_OF_DAY, -4); //esto deberia restarle 4 horas al calendario asi hasta las 4 am, sigue siendo el mismo dia, para pedidos nocturnos
        year = cal.get(Calendar.YEAR);
        dayofmonth = cal.get(Calendar.DAY_OF_MONTH);
        month = cal.get(Calendar.MONTH);

        str_anio = String.valueOf(year);
        str_mes = String.valueOf(month + 1);
        str_dia = String.valueOf(dayofmonth);

        btn_modo_de_pago_efectivo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (boolean_tiene_multiple_delivery) {
                    btn_modo_de_pago_efectivo.setBackgroundResource(R.drawable.btn_confirmacion_elegir_opcion);
                    btn_modo_de_pago_tarjeta.setBackgroundResource(R.drawable.input_outline);
                    btn_modo_de_pago_online.setBackgroundResource(R.drawable.input_outline);
                    str_forma_de_pago = "efectivo";
                    method_llenar_grid_view_multiple_delivery();
                } else {
                    if (str_delivery_seleccionado != null) {
                        btn_modo_de_pago_efectivo.setBackgroundResource(R.drawable.btn_confirmacion_elegir_opcion);
                        btn_modo_de_pago_tarjeta.setBackgroundResource(R.drawable.input_outline);
                        btn_modo_de_pago_online.setBackgroundResource(R.drawable.input_outline);
                        str_forma_de_pago = "efectivo";
                        tv_confirmacion_precio_final.setText(String.valueOf(int_precio_final_efectivo));
                    } else {
                        Toast.makeText(getActivity(), "debe elegir un delivery primero", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
        btn_modo_de_pago_tarjeta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (boolean_tiene_multiple_delivery) {

                    btn_modo_de_pago_efectivo.setBackgroundResource(R.drawable.input_outline);
                    btn_modo_de_pago_tarjeta.setBackgroundResource(R.drawable.btn_confirmacion_elegir_opcion);
                    btn_modo_de_pago_online.setBackgroundResource(R.drawable.input_outline);
                    str_forma_de_pago = "tarjeta";
                    method_llenar_grid_view_multiple_delivery();
                } else {
                    if (str_delivery_seleccionado != null) {

                        btn_modo_de_pago_efectivo.setBackgroundResource(R.drawable.input_outline);
                        btn_modo_de_pago_tarjeta.setBackgroundResource(R.drawable.btn_confirmacion_elegir_opcion);
                        btn_modo_de_pago_online.setBackgroundResource(R.drawable.input_outline);
                        str_forma_de_pago = "tarjeta";

                        double_precio_final = int_precio_final_efectivo * double_comision_tarjeta;
                        int_precio_final_tarjeta = ((int) double_precio_final);

                        tv_confirmacion_precio_final.setText(String.valueOf(int_precio_final_tarjeta));

                    } else {
                        Toast.makeText(getActivity(), "debe elegir un delivery primero", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
        btn_modo_de_pago_online.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btn_modo_de_pago_efectivo.setBackgroundResource(R.drawable.input_outline);
                btn_modo_de_pago_tarjeta.setBackgroundResource(R.drawable.input_outline);
                btn_modo_de_pago_online.setBackgroundResource(R.drawable.btn_confirmacion_elegir_opcion);
                str_forma_de_pago = "online";
            }
        });

        contex = getActivity();
        sharpref = getContext().getSharedPreferences("usar_app", Context.MODE_PRIVATE);
        pedido_guardado = sharpref.getString("pedido", null);

        if (pedido_guardado != null & !no_ejecutar_el_codigo_xq_quiero_ir_al_fragment_princiapal) {
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
            String ubicacion_mejorada = ubicacion_para_el_pedido.replace(" ", "");
            pedido_guardado = hash_pedido_guardado.get(ubicacion_mejorada);

            //divido en pedidos
            String[] dividir_pedidos = pedido_guardado.split("·");
            for (int c = 0; c < dividir_pedidos.length; c++) {
                String[] dividir_pedido = dividir_pedidos[c].split("#");
                String negocio_key = dividir_pedido[0];
                String pedido_value = dividir_pedido[1]; // no lo necesito
                int_cantidad_productos++;
                hash_negocios.put(negocio_key, null);
            }

            //busco los deliverys que me dieron acceso a los negocios
            deliverys_fragment_principal = ((MainActivity) getActivity()).que_delivery_usar;
            tv_confirmacion_cantidad_negocios.setText(String.valueOf(hash_negocios.size()));
            method_buscar_negocios_gps(new FirebaseCallBack_negocios_gps() {
                @Override
                public void onCallBack(HashMap<String, String> negocios) {
                    method_ver_si_entran_en_un_solo_delivery(negocios);
                }
            });
        } //cargo los datos

        buscar_precio_servicio.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String str_precio_servicio = (String) dataSnapshot.child("precio_servicio").getValue();
                String str_comision_tarjeta = (String) dataSnapshot.child("comision_tarjeta").getValue();
                int_precio_servicio = Integer.parseInt(str_precio_servicio);
                double_comision_tarjeta = Double.valueOf(str_comision_tarjeta);
                if (getActivity() != null) {
                    ((MainActivity) getActivity()).double_interes_aplicado = double_comision_tarjeta;
                }
                int double_cuanto_es_la_comision = (int) ((double_comision_tarjeta - 1) * 100);
                String str_cuanto_es_la_comision = "se aplica una comision del \n" + String.valueOf(double_cuanto_es_la_comision) + "%";

                tv_comision_aplicada.setText(str_cuanto_es_la_comision);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                if (!boolean_tiene_multiple_delivery) { //si no tiene multiple delivery que se pueda seleccionar los deliverys
                    String deliv_elegido = array_grid_delivery_elegir.get(i);
                    int_precio_delivery_mas_servicio = array_precio_deliverys_total.get(i);

                    if (str_forma_de_pago == null) {
                        int_precio_final_efectivo = ((MainActivity) getActivity()).precio_productos + int_precio_delivery_mas_servicio;
                        tv_confirmacion_precio_final.setText(String.valueOf(int_precio_final_efectivo));
                    } else {
                        if (str_forma_de_pago.equals("efectivo")) {
                            int_precio_final_efectivo = ((MainActivity) getActivity()).precio_productos + int_precio_delivery_mas_servicio;
                            tv_confirmacion_precio_final.setText(String.valueOf(int_precio_final_efectivo));
                        }
                        if (str_forma_de_pago.equals("tarjeta")) {
                            int_precio_final_efectivo = ((MainActivity) getActivity()).precio_productos + int_precio_delivery_mas_servicio;
                            double_precio_final = int_precio_final_efectivo * double_comision_tarjeta;
                            int_precio_final_tarjeta = ((int) double_precio_final);
                            tv_confirmacion_precio_final.setText(String.valueOf(int_precio_final_tarjeta));
                        }
                    }
                    str_delivery_seleccionado = deliv_elegido;

                    ArrayList<String> array_nuevo_pasarle_al_adapter = new ArrayList<>();
                    for (int del = 0; del < pasarle_datos_al_adapter.size(); del++) {
                        String valor_viejo = pasarle_datos_al_adapter.get(del);
                        if (i != del) {
                            //si no es el elegido que le pase no
                            valor_viejo = valor_viejo + "€no";
                        } else {
                            valor_viejo = valor_viejo + "€si";
                        }
                        array_nuevo_pasarle_al_adapter.add(valor_viejo);
                    }
                    String[] pasar_datos_al_adapter = array_nuevo_pasarle_al_adapter.toArray(new String[0]);
                    final adaptador_grid_elegir_delivery adapter_productos = new adaptador_grid_elegir_delivery(getActivity(), pasar_datos_al_adapter);
                    gridview.setAdapter(adapter_productos);
                }
            }
        });
        btn_confirmacion_siguiente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (boolean_tiene_multiple_delivery) {
                    if (boolean_multiple_delivery_estoy_de_acuerdo) {
                        alertdialog_confirmar_envio();
                    } else {
                        Toast.makeText(getActivity(), "debe haceptar que esta de acuerdo con recibir su pedido con mas de un delivery", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    alertdialog_confirmar_envio();
                }
            }
        });
        return Confirmacion_botones;
    }


    private void method_ver_si_entran_en_un_solo_delivery(final HashMap<String, String> negocios) {

        DatabaseReference buscar_deliverys = buscar_datos.child("deliverys");

        buscar_deliverys.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snap_deliverys) {
                // deliverys_fragment_principal // tengo los deliverys
                // negocios // tengo los negocios

                if (deliverys_fragment_principal != null) {
                    final String[] separa_deliverys = deliverys_fragment_principal.split(",");
                    hash_delivery_cubre_el_negocio.clear();

                    for (String str_delivery : separa_deliverys) {
                        hash_delivery_cubre_el_negocio.put(str_delivery, "");
                    }

                    for (String str_negocio : hash_negocios_gps.keySet()) {

                        String gps_point = hash_negocios_gps.get(str_negocio);
                        String[] coord = gps_point.split(",");
                        double coord1 = Double.parseDouble(coord[0]);
                        double coord2 = Double.parseDouble(coord[1]);
                        Point point = new Point(coord1, coord2);

                        for (String str_delivery : separa_deliverys) {
                            //busco el area del delivery

                            ArrayList<String> borrar_negocios_repetidos = new ArrayList<>();
                            for (DataSnapshot snap_area : snap_deliverys.child(str_delivery).child("area").getChildren()) {
                                Polygon.Builder poly2 = new Polygon.Builder();
                                String str_area_input = (String) snap_area.getValue();

                                if (str_area_input != null) {
                                    String[] areas = str_area_input.split(",");
                                    for (int i_poly = 0; i_poly < 4; i_poly++) {
                                        String[] coord_deliv = areas[i_poly].split("€");
                                        double coord_deliv_1 = Double.parseDouble(coord_deliv[0]);
                                        double coord_deliv_2 = Double.parseDouble(coord_deliv[1]);
                                        Point point_deliv = new Point(coord_deliv_1, coord_deliv_2);
                                        poly2.addVertex(point_deliv);
                                    }
                                    Polygon polygon1 = poly2.build();

                                    boolean adentro_o_afuera_de_algun_poly;
                                    adentro_o_afuera_de_algun_poly = polygon1.contains(point);
                                    if (adentro_o_afuera_de_algun_poly) {
                                        borrar_negocios_repetidos.add(str_negocio);
                                    }
                                }
                            }
                            Set<String> set = new HashSet<>(borrar_negocios_repetidos);
                            borrar_negocios_repetidos.clear();
                            borrar_negocios_repetidos.addAll(set);

                            if (String.valueOf(borrar_negocios_repetidos).length() > 3) {

                                String negocios_sin_corchetes = String.valueOf(borrar_negocios_repetidos).substring(1, String.valueOf(borrar_negocios_repetidos).length() - 1);
                                String valor_anterior_del_hash = hash_delivery_cubre_el_negocio.get(str_delivery);
                                String delivery_negocios = valor_anterior_del_hash + negocios_sin_corchetes + "€";
                                hash_delivery_cubre_el_negocio.put(str_delivery, delivery_negocios);
                            }
                        }
                    }
                    int cantidad_negocios = hash_negocios_gps.size();

                    int cantidad_deliverys_que_pueden_hacer_el_pedido_entero = 0;
                    ArrayList<String> array_deliverys_desponibles = new ArrayList<>();

                    for (String delivery : hash_delivery_cubre_el_negocio.keySet()) {
                        String valor_delivery = hash_delivery_cubre_el_negocio.get(delivery);
                        if (valor_delivery.length() > 3) {

                            String[] str_delivery_cantidad_negocios = valor_delivery.split("€");

                            int delivery_cantidad_negocios = str_delivery_cantidad_negocios.length;
                            if (cantidad_negocios == delivery_cantidad_negocios) {
                                cantidad_deliverys_que_pueden_hacer_el_pedido_entero++;
                                array_deliverys_desponibles.add(delivery);
                            }
                        }
                    }
                    deliverys_disponibles_para_el_pedido(cantidad_deliverys_que_pueden_hacer_el_pedido_entero, array_deliverys_desponibles, new FirebaseCallBack_deliverys_cargar_datos() {
                        @Override
                        public void onCallBack(ArrayList<String> deliverys, ArrayList<Integer> precios_x_negocio, ArrayList<Integer> precios, ArrayList<String> espera) {

                            llenar_grid_view_deliverys(deliverys, precios_x_negocio, precios, espera);
                        }
                    });
                } else {
                    method_deliverys_no_disponibles();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void method_deliverys_no_disponibles() {
        if (getActivity() != null) {
            metodos.alerdialog_descargando_informacion(getActivity(), false, "");
            metodos.alertdialog_doble_accion(getActivity(), "Deliverys no disponibles",
                    "debido a que no se encontraron deliverys disponibles en la pagina principal, usted no deberia haber llegado a este lugar, disculpe las molestias",
                    "ir al menu principal",
                    "Cerrar",
                    "c_elegir_delivery_confirmacion",
                    "method_deliverys_no_disponibles",false);

        }
    }


    private void deliverys_disponibles_para_el_pedido(int cantidad_deliverys_que_pueden_hacer_el_pedido_entero, final ArrayList<String> array_deliverys_desponibles, final FirebaseCallBack_deliverys_cargar_datos firebaseCallBack_deliverys_cargar_datos) {

        final String str_delivery_mejorado = array_deliverys_desponibles.toString().substring(1, array_deliverys_desponibles.toString().length() - 1);
        array_precio_deliverys_total.clear();
        array_precio_deliverys_por_negocio.clear();
        final ArrayList<String> array_pedidos_en_espera = new ArrayList<>();
        if (cantidad_deliverys_que_pueden_hacer_el_pedido_entero == 0) {

            //buscar pedidos en espera
            hash_delivery_espera.clear();
            hash_delivery_espera_pasar_al_adapter.clear();

            DatabaseReference db_buscar_deliverys = buscar_datos;
            db_buscar_deliverys.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snap_buscar) {

                    DataSnapshot snap_dia_actual = snap_buscar.child("pedidos").child(str_anio).child(str_mes).child(str_dia);

                    for (String delivery : hash_delivery_cubre_el_negocio.keySet()) {

                        int_pedidos_en_espera = 0;
                        //busca la cantidad de pedidos en espera

                        for (DataSnapshot snap_pedidos : snap_dia_actual.getChildren()) {
                            //busca en el dia actual los pedidos.
                            String delivery_asignado = (String) snap_pedidos.child("delivery").getValue();
                            if (delivery_asignado != null) {
                                if (delivery_asignado.equals(delivery)) {
                                    //si el delivery asignado y el delivery que se quiere consultar son iguales
                                    String estado_pedido = (String) snap_pedidos.child("pedido_en_actividad").getValue();
                                    if (estado_pedido != null) {
                                        if (myArrayList.contains(estado_pedido)) {
                                            //si encuentra al delivery, que se fije en estado pedido, si !=terminado int_pedidos_en_espera++

                                            int_pedidos_en_espera++;

                                        }
                                    } else {
                                        Toast.makeText(getActivity(), "error buscando el estado del delivery", Toast.LENGTH_SHORT).show();
                                        array_pedidos_en_espera.add("error buscando pedidos en espera");
                                    }
                                }
                            } else {
                                hash_delivery_espera.put(delivery, int_pedidos_en_espera);
                                hash_delivery_espera_pasar_al_adapter.put(delivery, int_pedidos_en_espera);
                                Toast.makeText(getActivity(), "error buscando los pedidos en espera de los deliverys", Toast.LENGTH_SHORT).show();
                                array_pedidos_en_espera.add("error buscando pedidos en espera");
                            }

                        }
                        hash_delivery_espera.put(delivery, int_pedidos_en_espera);
                        hash_delivery_espera_pasar_al_adapter.put(delivery, int_pedidos_en_espera);
                    }

                    //busca la prioridad del delivery

                    hash_deliverys_prioridad.clear();

                    DataSnapshot snap_deliverys = snap_buscar.child("deliverys");
                    for (String delivery : hash_delivery_cubre_el_negocio.keySet()) {

                        DataSnapshot snap_deliverys_prioridad = snap_deliverys.child(delivery).child("prioridad");
                        String prioridad = (String) snap_deliverys_prioridad.getValue();
                        hash_deliverys_prioridad.put(delivery, Integer.valueOf(prioridad));

                        String que_precio_usar = (String) snap_deliverys.child(delivery).child("precios").child("cual_usar").getValue();
                        if (que_precio_usar != null) {
                            method(snap_deliverys, que_precio_usar, delivery);
                        } else {
                            Toast.makeText(getActivity(), "problema con que precio usar", Toast.LENGTH_SHORT).show();
                        }
                    }
                    method_elegir_multiples_deliverys();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }

                private void method(DataSnapshot snap, String que_precio_usar, String delivery) {
                    String precio = (String) snap.child(delivery).child("precios").child("precios").child(que_precio_usar).getValue();
                    int precio_delivery = Integer.parseInt(precio);
                    hash_deliverys_multiple_precio.put(delivery, precio_delivery);

                    /*int cantidad_negocios = hash_negocios.size();
                    int precio_total = (precio_delivery + int_precio_servicio) * cantidad_negocios;
                    array_precio_deliverys_total.add(precio_total);
                    array_precio_deliverys_por_negocio.add(precio_delivery + int_precio_servicio);
                    array_pedidos_en_espera.add(String.valueOf(int_pedidos_en_espera));*/
                }
            });
        }
        if (cantidad_deliverys_que_pueden_hacer_el_pedido_entero == 1) {
            gridview.setNumColumns(1);

            int_pedidos_en_espera = 0;
            DatabaseReference db_buscar_deliverys = buscar_datos;
            db_buscar_deliverys.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snap_buscar) {
                    ///busca la cantidad de pedidos en espera
                    //buscar en todos los childs del dia actual
                    DataSnapshot snap_dia_actual = snap_buscar.child("pedidos").child(str_anio).child(str_mes).child(str_dia);

                    for (DataSnapshot snap_pedidos : snap_dia_actual.getChildren()) {
                        //busca en el dia actual los pedidos.
                        String delivery_asignado = (String) snap_pedidos.child("delivery").getValue();
                        if (delivery_asignado != null) {
                            if (delivery_asignado.equals(str_delivery_mejorado)) {
                                //si el delivery asignado y el delivery que se quiere consultar son iguales
                                String estado_pedido = (String) snap_pedidos.child("pedido_en_actividad").getValue();
                                if (estado_pedido != null) {
                                    if (myArrayList.contains(estado_pedido)) {
                                        //si encuentra al delivery, que se fije en estado pedido, si !=terminado int_pedidos_en_espera++
                                        int_pedidos_en_espera++;
                                    }
                                } else {
                                    Toast.makeText(getActivity(), "error buscando el estado del delivery", Toast.LENGTH_SHORT).show();
                                    array_pedidos_en_espera.add("error buscando pedidos en espera");
                                }
                            }
                        } else {
                            Toast.makeText(getActivity(), "error buscando los pedidos en espera de los deliverys", Toast.LENGTH_SHORT).show();
                            array_pedidos_en_espera.add("error buscando pedidos en espera");
                        }

                    }

                    ///busca el precio de los deliverys
                    DataSnapshot snap_delivery = snap_buscar.child("deliverys").child(str_delivery_mejorado);
                    String que_precio_usar = (String) snap_delivery.child("precios").child("cual_usar").getValue();
                    if (que_precio_usar != null) {
                        method(snap_delivery, que_precio_usar);
                    } else {
                        Toast.makeText(getActivity(), "problema con que precio usar", Toast.LENGTH_SHORT).show();
                    }
                }

                private void method(DataSnapshot snap, String que_precio_usar) {
                    String precio = (String) snap.child("precios").child("precios").child(que_precio_usar).getValue();
                    int precio_delivery = Integer.parseInt(precio);
                    int cantidad_negocios = hash_negocios.size();
                    int precio_total = (precio_delivery + int_precio_servicio) * cantidad_negocios;
                    array_precio_deliverys_total.add(precio_total);
                    array_precio_deliverys_por_negocio.add(precio_delivery + int_precio_servicio);

                    //guarda los pedidos en espera
                    array_pedidos_en_espera.add(String.valueOf(int_pedidos_en_espera));


                    firebaseCallBack_deliverys_cargar_datos.onCallBack(array_deliverys_desponibles, array_precio_deliverys_por_negocio, array_precio_deliverys_total, array_pedidos_en_espera);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }

        if (cantidad_deliverys_que_pueden_hacer_el_pedido_entero >= 2) {
            if (cantidad_deliverys_que_pueden_hacer_el_pedido_entero == 2) {
                gridview.setNumColumns(2);
            }

            DatabaseReference db_buscar_deliverys = buscar_datos;

            db_buscar_deliverys.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snap_buscar) {

                    DataSnapshot snap_dia_actual = snap_buscar.child("pedidos").child(str_anio).child(str_mes).child(str_dia);
                    DataSnapshot snap_delivery = snap_buscar.child("deliverys");

                    for (String delivery : array_deliverys_desponibles) {
                        int_pedidos_en_espera = 0;
                        //busca la cantidad de pedidos en espera

                        for (DataSnapshot snap_pedidos : snap_dia_actual.getChildren()) {
                            //busca en el dia actual los pedidos.
                            String delivery_asignado = (String) snap_pedidos.child("delivery").getValue();
                            if (delivery_asignado != null) {
                                if (delivery_asignado.equals(delivery)) {
                                    //si el delivery asignado y el delivery que se quiere consultar son iguales
                                    String estado_pedido = (String) snap_pedidos.child("pedido_en_actividad").getValue();
                                    if (estado_pedido != null) {
                                        if (myArrayList.contains(estado_pedido)) {
                                            //si encuentra al delivery, que se fije en estado pedido, si !=terminado int_pedidos_en_espera++
                                            int_pedidos_en_espera++;

                                        }
                                    } else {
                                        Toast.makeText(getActivity(), "error buscando el estado del delivery", Toast.LENGTH_SHORT).show();
                                        array_pedidos_en_espera.add("error buscando pedidos en espera");
                                    }
                                }
                            } else {
                                Toast.makeText(getActivity(), "error buscando los pedidos en espera de los deliverys", Toast.LENGTH_SHORT).show();
                                array_pedidos_en_espera.add("error buscando pedidos en espera");
                            }
                        }

                        //busca el precio de los deliverys
                        String que_precio_usar = (String) snap_delivery.child(delivery).child("precios").child("cual_usar").getValue();
                        if (que_precio_usar != null) {
                            method(snap_delivery, que_precio_usar, delivery);

                        } else {
                            Toast.makeText(getActivity(), "problema con que precio usar", Toast.LENGTH_SHORT).show();
                        }
                    }


                    firebaseCallBack_deliverys_cargar_datos.onCallBack(array_deliverys_desponibles, array_precio_deliverys_por_negocio, array_precio_deliverys_total, array_pedidos_en_espera);

                }

                private void method(DataSnapshot snap, String que_precio_usar, String delivery) {
                    String precio = (String) snap.child(delivery).child("precios").child("precios").child(que_precio_usar).getValue();
                    int precio_delivery = Integer.parseInt(precio);
                    int cantidad_negocios = hash_negocios.size();
                    int precio_total = (precio_delivery + int_precio_servicio) * cantidad_negocios;
                    array_precio_deliverys_total.add(precio_total);
                    array_precio_deliverys_por_negocio.add(precio_delivery + int_precio_servicio);
                    array_pedidos_en_espera.add(String.valueOf(int_pedidos_en_espera));


                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }


    }

    private void method_elegir_multiples_deliverys() {

        /// desde aca creo que lo puedo manejar con un method

        //elegir el que tenga menos pedidos
        HashMap<String, String> hash_usar_para_elegir_el_delivery_paso_1 = new HashMap<>();
        int int_minimo_cantidad_pedidos = 99;
        for (String deliv_espera : hash_delivery_espera.keySet()) {
            int comparar_con_minimo = hash_delivery_espera.get(deliv_espera);

            if (comparar_con_minimo < int_minimo_cantidad_pedidos) {
                int_minimo_cantidad_pedidos = comparar_con_minimo;
                hash_usar_para_elegir_el_delivery_paso_1.clear();
                hash_usar_para_elegir_el_delivery_paso_1.put(deliv_espera, null);
                //que borre los resultados antiguos
            } else if (comparar_con_minimo == int_minimo_cantidad_pedidos) {
                //que se agrege a un hashmap_que sea el que los compare luego
                hash_usar_para_elegir_el_delivery_paso_1.put(deliv_espera, null);
            }

        }
        //Log.d(TAG, "paso 1 elegir minimo: " + hash_usar_para_elegir_el_delivery_paso_1);

        HashMap<String, String> hash_usar_para_elegir_el_delivery_paso_2 = new HashMap<>();
        int int_prioridad_deliverys = 0;
        for (String deliv_prioridad : hash_usar_para_elegir_el_delivery_paso_1.keySet()) {
            int comparar_con_prioridad = hash_deliverys_prioridad.get(deliv_prioridad);

            if (comparar_con_prioridad > int_prioridad_deliverys) {
                int_prioridad_deliverys = comparar_con_prioridad;
                hash_usar_para_elegir_el_delivery_paso_2.clear();
                hash_usar_para_elegir_el_delivery_paso_2.put(deliv_prioridad, null);
                //que borre los resultados antiguos
            } else if (comparar_con_prioridad == int_prioridad_deliverys) {
                //que se agrege a un hashmap_que sea el que los compare luego
                hash_usar_para_elegir_el_delivery_paso_2.put(deliv_prioridad, null);
            }
        }

        //Log.d(TAG, "paso 2 elegir prioridad: " + hash_usar_para_elegir_el_delivery_paso_2);

        String delivery_elegido;
        if (hash_usar_para_elegir_el_delivery_paso_2.size() > 1) {

            Random random = new Random();
            List<String> keys = new ArrayList<>(hash_usar_para_elegir_el_delivery_paso_2.keySet());
            //String value = hash_usar_para_elegir_el_delivery_paso_2.get(randomKey); // obtengo el valor
            delivery_elegido = keys.get(random.nextInt(keys.size()));  //obtengo el keyrandomKey;
        } else {

            delivery_elegido = String.valueOf(hash_usar_para_elegir_el_delivery_paso_2);
            delivery_elegido = delivery_elegido.substring(1, delivery_elegido.length() - 1);
            String[] dividir = delivery_elegido.split("=");
            delivery_elegido = dividir[0];

        }

        String negocios_del_delivery_elegido = hash_delivery_cubre_el_negocio.get(delivery_elegido);

        if (getActivity() != null) {
            ((MainActivity) getActivity()).hash_multiple_delivery_mainact.put(delivery_elegido, negocios_del_delivery_elegido);
        }

        hash_delivery_cubre_el_negocio.remove(delivery_elegido);
        hash_deliverys_prioridad.remove(delivery_elegido);
        hash_delivery_espera.remove(delivery_elegido);


        for (String deliv_sobrantes : hash_delivery_cubre_el_negocio.keySet()) { //divido por negocios
            String valor_de_los_deliverys = hash_delivery_cubre_el_negocio.get(deliv_sobrantes); //obtengo los negocios de los deliv sobrantes
            String[] negocios_que_tengo_que_borrar = negocios_del_delivery_elegido.split("€"); // divido los negocios que quiero sacar
            for (String negocio : negocios_que_tengo_que_borrar) {
                String remplazar = negocio + "€";
                valor_de_los_deliverys = valor_de_los_deliverys.replace(remplazar, "");
            }
            //valor_de_los_deliverys=valor_de_los_deliverys.substring(0,valor_de_los_deliverys.length()-1);

            if (valor_de_los_deliverys.length() > 3) {
                hash_delivery_cubre_el_negocio.put(deliv_sobrantes, valor_de_los_deliverys);
            } else {
                hash_delivery_cubre_el_negocio.remove(deliv_sobrantes);
            }

        }

        if (hash_delivery_cubre_el_negocio.size() != 0) {
            method_elegir_multiples_deliverys();
        } else {

            method_llenar_grid_view_multiple_delivery();

        }


    }

    private void method_llenar_grid_view_multiple_delivery() {

        StringBuilder deliverys_seleccionados = null;
        int int_suma_de_precios_multiple_deliverys = 0;
        int cantidad_deliverys_para_el_adapter = 0;

        if (getActivity() != null) {
            cantidad_deliverys_para_el_adapter = ((MainActivity) getActivity()).hash_multiple_delivery_mainact.size();
        }

        if (cantidad_deliverys_para_el_adapter == 2) {
            gridview.setNumColumns(2);
        }

        boolean_tiene_multiple_delivery = true;
        linear_multiple_delivery.setVisibility(View.VISIBLE);
        tv__multiple_delivery.setText("debido a que los negocios seleccionados no estan en el area de un solo delivery. se necesitan " + cantidad_deliverys_para_el_adapter + "deliverys para completar el pedido");
        btn_multiple_delivery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean_multiple_delivery_estoy_de_acuerdo = true;
                btn_multiple_delivery.setBackgroundResource(R.drawable.btn_confirmacion_elegir_opcion);
            }
        });


        pasarle_datos_al_adapter.clear();
        for (String deliverys_elegidos_pasar_al_adapter : ((MainActivity) getActivity()).hash_multiple_delivery_mainact.keySet()) {

            String imput_negocios = ((MainActivity) getActivity()).hash_multiple_delivery_mainact.get(deliverys_elegidos_pasar_al_adapter);
            String[] str_array_cant_negocios = imput_negocios.split("€");
            int cantidad_negocios = str_array_cant_negocios.length;
            int precio_por_negocio = hash_deliverys_multiple_precio.get(deliverys_elegidos_pasar_al_adapter) + int_precio_servicio;
            int precio_del_delivery_total_cobrar = precio_por_negocio * cantidad_negocios;

            //primer parametro total a cobrar, segundo cuanto se queda el delivery, tercero la comision para la app
            int comision_de_la_app = int_precio_servicio * cantidad_negocios;
            int precio_para_el_que_hace_el_delivery = hash_deliverys_multiple_precio.get(deliverys_elegidos_pasar_al_adapter) * cantidad_negocios;

            //guarda los precios de los deliverys en un hashmaps

            if (str_forma_de_pago == null) {
                ((MainActivity) getActivity()).hash_multiples_deliverys_montos.put(deliverys_elegidos_pasar_al_adapter, new Integer[]{precio_del_delivery_total_cobrar, precio_para_el_que_hace_el_delivery, comision_de_la_app});
            } else {
                if (str_forma_de_pago.equals("tarjeta")) {
                    double_precio_final = precio_del_delivery_total_cobrar * double_comision_tarjeta;
                    int_precio_final_tarjeta = ((int) double_precio_final);
                    precio_del_delivery_total_cobrar = int_precio_final_tarjeta;
                    ((MainActivity) getActivity()).hash_multiples_deliverys_montos.put(deliverys_elegidos_pasar_al_adapter, new Integer[]{precio_del_delivery_total_cobrar, precio_para_el_que_hace_el_delivery, comision_de_la_app});
                }
                if (str_forma_de_pago.equals("efectivo")) {
                    ((MainActivity) getActivity()).hash_multiples_deliverys_montos.put(deliverys_elegidos_pasar_al_adapter, new Integer[]{precio_del_delivery_total_cobrar, precio_para_el_que_hace_el_delivery, comision_de_la_app});
                }
            }
            pasarle_datos_al_adapter.add(deliverys_elegidos_pasar_al_adapter + "€" + String.valueOf(precio_por_negocio) + "€" + String.valueOf(precio_del_delivery_total_cobrar) + "€" + hash_delivery_espera_pasar_al_adapter.get(deliverys_elegidos_pasar_al_adapter));

            if (deliverys_seleccionados == null) {
                deliverys_seleccionados = new StringBuilder(deliverys_elegidos_pasar_al_adapter);
            } else {
                deliverys_seleccionados.append(",").append(deliverys_elegidos_pasar_al_adapter);
            }
            int_suma_de_precios_multiple_deliverys = int_suma_de_precios_multiple_deliverys + precio_del_delivery_total_cobrar;
        }


        metodos.alerdialog_descargando_informacion(getActivity(), false, "");

        String[] pasar_datos_al_adapter = pasarle_datos_al_adapter.toArray(new String[0]);
        final adaptador_grid_elegir_delivery adapter_productos = new adaptador_grid_elegir_delivery(getActivity(), pasar_datos_al_adapter);
        gridview.setAdapter(adapter_productos);

        if (str_forma_de_pago == null) {

            int_precio_final_efectivo = ((MainActivity) getActivity()).precio_productos + int_suma_de_precios_multiple_deliverys;
            tv_confirmacion_precio_final.setText(String.valueOf(int_precio_final_efectivo));

        } else {
            if (str_forma_de_pago.equals("tarjeta")) {

                int precio_productos_aplicar_comision = ((MainActivity) getActivity()).precio_productos;

                double_precio_final = precio_productos_aplicar_comision * double_comision_tarjeta;
                int precio_productos_mas_la_comision = ((int) double_precio_final);

                int_precio_final_tarjeta = precio_productos_mas_la_comision + int_suma_de_precios_multiple_deliverys;
                tv_confirmacion_precio_final.setText(String.valueOf(int_precio_final_tarjeta));

            }
            if (str_forma_de_pago.equals("efectivo")) {
                int_precio_final_efectivo = ((MainActivity) getActivity()).precio_productos + int_suma_de_precios_multiple_deliverys;
                tv_confirmacion_precio_final.setText(String.valueOf(int_precio_final_efectivo));
            }
        }
        str_delivery_seleccionado = deliverys_seleccionados.toString();
    }

    private void llenar_grid_view_deliverys(ArrayList<String> array_deliverys_desponibles, ArrayList<Integer> array_precio_deliverys_por_negocio, ArrayList<Integer> array_precio_deliverys_total, ArrayList<String> array_pedidos_en_espera) {

        pasarle_datos_al_adapter.clear();
        if (array_deliverys_desponibles != null) {
            if (array_deliverys_desponibles.size() == 1) { //si hay un solo delivery que no lo tenga que cliquear que se carge solo
                str_delivery_seleccionado = array_deliverys_desponibles.get(0);
                int_precio_delivery_mas_servicio = array_precio_deliverys_total.get(0);
                if (getActivity() != null) {
                    if (str_forma_de_pago == null) {
                        int_precio_final_efectivo = ((MainActivity) getActivity()).precio_productos + int_precio_delivery_mas_servicio;
                        tv_confirmacion_precio_final.setText(String.valueOf(int_precio_final_efectivo));
                    } else {
                        if (str_forma_de_pago.equals("efectivo")) {
                            int_precio_final_efectivo = ((MainActivity) getActivity()).precio_productos + int_precio_delivery_mas_servicio;
                            tv_confirmacion_precio_final.setText(String.valueOf(int_precio_final_efectivo));
                        }
                        if (str_forma_de_pago.equals("tarjeta")) {
                            int_precio_final_efectivo = ((MainActivity) getActivity()).precio_productos + int_precio_delivery_mas_servicio;
                            double_precio_final = int_precio_final_efectivo * double_comision_tarjeta;
                            int_precio_final_tarjeta = ((int) double_precio_final);
                            tv_confirmacion_precio_final.setText(String.valueOf(int_precio_final_tarjeta));
                        }
                    }
                }
            }
            for (int del = 0; del < array_deliverys_desponibles.size(); del++) {

                pasarle_datos_al_adapter.add(array_deliverys_desponibles.get(del) + "€" + String.valueOf(array_precio_deliverys_por_negocio.get(del)) + "€" + String.valueOf(array_precio_deliverys_total.get(del)) + "€" + array_pedidos_en_espera.get(del));
                array_grid_delivery_elegir.add(array_deliverys_desponibles.get(del));
            }
            if (getActivity() != null) {
                metodos.alerdialog_descargando_informacion(getActivity(), false, "");
            }

            String[] pasar_datos_al_adapter = pasarle_datos_al_adapter.toArray(new String[0]);
            final adaptador_grid_elegir_delivery adapter_productos = new adaptador_grid_elegir_delivery(getActivity(), pasar_datos_al_adapter);
            gridview.setAdapter(adapter_productos);
        }
    }

    private void method_buscar_negocios_gps(final FirebaseCallBack_negocios_gps firebaseCallBack_negocios_gps) {

        DatabaseReference buscar_ubicacion_gps = buscar_negocios;
        buscar_ubicacion_gps.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (String key : hash_negocios.keySet()) {
                    String ubicacion = (String) dataSnapshot.child(key).child("gps_negocio").getValue();
                    hash_negocios_gps.put(key, ubicacion);
                }

                firebaseCallBack_negocios_gps.onCallBack(hash_negocios_gps);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }


    private interface FirebaseCallBack_negocios_gps {
        void onCallBack(HashMap<String, String> negocios);
    }

    private interface FirebaseCallBack_deliverys_cargar_datos {
        void onCallBack(ArrayList<String> deliverys, ArrayList<Integer> precios_x_negocio, ArrayList<Integer> precios, ArrayList<String> espera);
    }

    private void alertdialog_confirmar_envio() {

        if (str_delivery_seleccionado != null) {
            if (str_forma_de_pago != null&getActivity()!=null) {
                AlertDialog.Builder alerta = new AlertDialog.Builder(
                        getActivity());
                LayoutInflater inflater = getActivity().getLayoutInflater();
                final View dialogView = inflater.inflate(R.layout.ad_confirmar_pedido_final, null);
                alerta.setView(dialogView);
                final AlertDialog alertDialog = alerta.create();

                TextView tv_cantidad_productos = dialogView.findViewById(R.id.TV_alertdialog_confirmar_cantidad_productos);
                TextView tv_precio_final_total = dialogView.findViewById(R.id.TV_alertdialog_confirmar_precio_final);
                TextView tv_forma_de_pago = dialogView.findViewById(R.id.TV_alertdialog_confirmar_forma_de_pago);
                TextView tv_ubicacion = dialogView.findViewById(R.id.TV_alertdialog_confirmar_ubicacion);
                Button btn_aceptar = dialogView.findViewById(R.id.ad_confirmar_aceptar);
                Button btn_cancelar = dialogView.findViewById(R.id.ad_confirmar_cancelar);

                tv_ubicacion.setText(ubicacion_para_el_pedido);
                tv_cantidad_productos.setText(String.valueOf(int_cantidad_productos));

                if (str_forma_de_pago.equals("efectivo")) {
                    tv_precio_final_total.setText(String.valueOf(int_precio_final_efectivo));
                    precio_elegido_final_total_no_jodas_mas = int_precio_final_efectivo;
                }
                if (str_forma_de_pago.equals("tarjeta")) {
                    tv_precio_final_total.setText(String.valueOf(int_precio_final_tarjeta));
                    precio_elegido_final_total_no_jodas_mas = int_precio_final_tarjeta;

                }

                tv_forma_de_pago.setText(str_forma_de_pago);

                btn_cancelar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alertDialog.dismiss();
                    }
                });
                btn_aceptar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ((MainActivity) getActivity()).ir_a_ver_progreso_o_al_menu_principal = true;

                        // guardo los montos, menos los delivery_elegido los negocios por separado que eso lo hago despues
                        ((MainActivity) getActivity()).delivery_elegido = str_delivery_seleccionado;
                        ((MainActivity) getActivity()).str_main_modo_de_pago = str_forma_de_pago;


                        if (!boolean_tiene_multiple_delivery) {
                            int cantidad_negocios = hash_negocios.size();
                            int_precio_servicio = int_precio_servicio * cantidad_negocios;
                            int precio_delivery = int_precio_delivery_mas_servicio - int_precio_servicio;

                            ((MainActivity) getActivity()).montos_cobrar(precio_elegido_final_total_no_jodas_mas, precio_delivery, int_precio_servicio);
                        }

                        cambiar_de_fragment(true);
                        alertDialog.dismiss();
                    }
                });

                alertDialog.show();

            } else {
                Toast.makeText(getActivity(), "debe elegir una forma de pago", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getActivity(), "debe elegir un delivery", Toast.LENGTH_SHORT).show();
        }
    }

    private void cambiar_de_fragment(boolean boton_siguiente_o_cargar_fragment_principal) {
        //si llego aca por la carga del fragment y tengo un pedido activo que me lleve al fragment principal
        boolean ir_a_ver_progreso_o_menu_principal = false;

        if (getActivity() != null) {
            ir_a_ver_progreso_o_menu_principal = ((MainActivity) getActivity()).ir_a_ver_progreso_o_al_menu_principal;
        }

        if (boton_siguiente_o_cargar_fragment_principal) {
            metodos.main_cambiar_fragment(getActivity(),"c_guardar_pedidos_ver_pedidos");

        } else {
            if (!ir_a_ver_progreso_o_menu_principal) {
                no_ejecutar_el_codigo_xq_quiero_ir_al_fragment_princiapal = true;
                metodos.main_cambiar_fragment(getActivity(),"c_principal");
            }
        }
    }
}

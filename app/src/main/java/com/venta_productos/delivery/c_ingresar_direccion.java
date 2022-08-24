package com.venta_productos.delivery;


import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.snatik.polygon.Point;
import com.snatik.polygon.Polygon;
import com.venta_productos.delivery.adapter.MyLocation;

import java.util.ArrayList;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.Context.LOCATION_SERVICE;

/**
 * A simple {@link Fragment} subclass.
 */
public class c_ingresar_direccion extends Fragment {


    public c_ingresar_direccion() {
        // Required empty public constructor
    }

    EditText ET_ingresar_nombre, ET_ingresar_calle, ET_ingresar_altura, ET_ingresar_descripcion, ET_agregar_pregunta_1;
    Button BTN_agregar_gps_actual, BTN_ingresar_gps_en_el_mapa, BTN_Agregar_direccion;
    Spinner SPN_tipo_de_domicilio;
    LinearLayout LINEAR_agregar_preguntas, LINEAR_agregar_descripcion;
    String[] tipo_domicilio = {"elija un tipo de domicilio", "Casa", "Departamento", "Cabaña", "hotel", "negocio - oficina", "otro"};
    String str_nombre, str_calle, str_altura, str_descripcion, str_pregunta_1, str_gps, str_tipo_de_domicilio;
    int int_que_opcion_esta_seleccionada_en_el_spineer;

    DatabaseReference buscar_datos_de_la_bd, db_donde_guardar;
    TextView tv_direccion_gps;
    Context contex;
    SharedPreferences sharpref;

    Boolean boolean_guardar_o_modificar = false;
    ArrayList<String> childs = new ArrayList<>();
    ArrayList<String> parent_childs = new ArrayList<>();
    String luego_de_modificar_a_donde_guardar;

    boolean gps_encontrado = false;
    String texto_boton;
    int control_int = 0;

    //borrar ubicacion elegida en el ondestroy (si pretendo modificar una ubicacion y salgo que no se guarde el str_array_datos_usuario)
    boolean borrar_el_dato_de_donde_voy_a_cargar_los_ed = true;

    //para chequear si esta dentro de algun area
    ArrayList<Polygon> array_poligon = new ArrayList<>();

    String TAG = "asdf";

    boolean boolean_encontro_las_areas = false;

    //datos usuario
    String valor_guerdado_usuario;
    String[] str_array_datos_usuario;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View Ingresar_direccion = inflater.inflate(R.layout.f_ingresar_direccion, container, false);
        if (getActivity() != null) {
            boolean_guardar_o_modificar = ((MainActivity) getActivity()).agregar_o_modificar_direccion_boolean;
            metodos.alerdialog_descargando_informacion(getActivity(), true, "buscando informacion");
        }

        ET_ingresar_calle = Ingresar_direccion.findViewById(R.id.ET_ingresar_calle);
        ET_ingresar_nombre = Ingresar_direccion.findViewById(R.id.ET_ingresar_nombre_identificador);
        ET_ingresar_altura = Ingresar_direccion.findViewById(R.id.ET_ingresar_altura);
        ET_ingresar_descripcion = Ingresar_direccion.findViewById(R.id.ET_ingresar_descripcion);
        ET_agregar_pregunta_1 = Ingresar_direccion.findViewById(R.id.ET_agregar_pregunta_1);
        BTN_agregar_gps_actual = Ingresar_direccion.findViewById(R.id.BTN_ingresar_gps_actual);
        BTN_ingresar_gps_en_el_mapa = Ingresar_direccion.findViewById(R.id.BTN_ingresar_gps_en_el_mapa);
        BTN_Agregar_direccion = Ingresar_direccion.findViewById(R.id.BNT_agregar_dirrecion);
        SPN_tipo_de_domicilio = Ingresar_direccion.findViewById(R.id.SPN_tipo_domicilio);
        LINEAR_agregar_preguntas = Ingresar_direccion.findViewById(R.id.linear_agregar_preguntas);
        LINEAR_agregar_descripcion = Ingresar_direccion.findViewById(R.id.linear_agregar_descripcion);
        tv_direccion_gps = Ingresar_direccion.findViewById(R.id.tv_ingresar_direccion_gps);

        contex = getActivity();
        sharpref = getContext().getSharedPreferences("usar_app", Context.MODE_PRIVATE);
        str_gps = ((MainActivity) getActivity()).str_elegido_en_el_mapa;

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                R.layout.row_spinner_text, R.id.texto_spinner, tipo_domicilio);
        SPN_tipo_de_domicilio.setAdapter(adapter);

        //busca las areas para saber si esta en rango
        buscar_area_de_los_deliverys(new FirebaseCallBack_buscar_areas() {
            @Override
            public void onCallBack(ArrayList<Polygon> array_polys) {
                if (array_polys.size() > 0) {
                    boolean_encontro_las_areas = true;
                }
            }
        });

        SPN_tipo_de_domicilio.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                LINEAR_agregar_preguntas.setVisibility(View.VISIBLE);
//                ET_agregar_pregunta_1.setText("");
                if (i == 0) {
                    LINEAR_agregar_preguntas.setVisibility(View.GONE);
                    LINEAR_agregar_descripcion.setVisibility(View.GONE);
                }
                if (i == 1) {
                    LINEAR_agregar_preguntas.setVisibility(View.VISIBLE);
                    ET_agregar_pregunta_1.setHint("Su Casa se ve desde la calle?");
                    LINEAR_agregar_descripcion.setVisibility(View.VISIBLE);
                }
                if (i == 2) {
                    LINEAR_agregar_preguntas.setVisibility(View.VISIBLE);
                    ET_agregar_pregunta_1.setHint("En que piso y numero de departamento se encuentra?");
                    LINEAR_agregar_descripcion.setVisibility(View.VISIBLE);
                }
                if (i == 3) {
                    LINEAR_agregar_preguntas.setVisibility(View.VISIBLE);
                    ET_agregar_pregunta_1.setHint("Como se llaman las cabañas?, en cual esta?");
                    LINEAR_agregar_descripcion.setVisibility(View.GONE);
                }
                if (i == 4) {
                    LINEAR_agregar_preguntas.setVisibility(View.VISIBLE);
                    ET_agregar_pregunta_1.setHint("Como se llama el hotel? en que habitacion esta?");
                    LINEAR_agregar_descripcion.setVisibility(View.GONE);
                }
                if (i == 5) {
                    LINEAR_agregar_preguntas.setVisibility(View.VISIBLE);
                    ET_agregar_pregunta_1.setHint("Como se llama el negocio?");
                    LINEAR_agregar_descripcion.setVisibility(View.VISIBLE);
                }
                if (i == 6) {
                    LINEAR_agregar_preguntas.setVisibility(View.VISIBLE);
                    ET_agregar_pregunta_1.setHint("En donde se encuentra?");
                    LINEAR_agregar_descripcion.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        if (str_gps != null) {
            //si esta variable es != null significa que vengo desde los mapas..

            if (((MainActivity) getActivity()).str_elegir_gps_en_el_mapa_donde_cargar_los_datos_en_los_et != null) {
                //si  esta variable es != null significa que quiero modificar una ubicacion
                luego_de_modificar_a_donde_guardar = ((MainActivity) getActivity()).str_elegir_gps_en_el_mapa_donde_cargar_los_datos_en_los_et;
                boolean_guardar_o_modificar = false;
                metodos.alerdialog_descargando_informacion(getActivity(), false, "");
            }

            tv_direccion_gps.setText("GPS = " + str_gps);

            String[] devolver_datos = ((MainActivity) getActivity()).str_conservar_datos.split(",");
            if (!devolver_datos[0].equals(" ")) {
                ET_ingresar_nombre.setText(devolver_datos[0]);
            }
            if (!devolver_datos[1].equals(" ")) {
                ET_ingresar_calle.setText(devolver_datos[1]);

            }
            if (!devolver_datos[2].equals(" ")) {
                ET_ingresar_altura.setText(devolver_datos[2]);
            }
            if (!devolver_datos[3].equals(" ")) {
                ET_agregar_pregunta_1.setText(devolver_datos[3]);
            }
            if (!devolver_datos[4].equals(" ")) {
                str_tipo_de_domicilio = devolver_datos[4];
                if (str_tipo_de_domicilio.equals("Casa")) {
                    SPN_tipo_de_domicilio.setSelection(1);
                }
                if (str_tipo_de_domicilio.equals("Departamento")) {
                    SPN_tipo_de_domicilio.setSelection(2);
                }
                if (str_tipo_de_domicilio.equals("Cabaña")) {
                    SPN_tipo_de_domicilio.setSelection(3);
                }
                if (str_tipo_de_domicilio.equals("hotel")) {
                    SPN_tipo_de_domicilio.setSelection(4);
                }
                if (str_tipo_de_domicilio.equals("negocio - oficina")) {
                    SPN_tipo_de_domicilio.setSelection(5);
                }
                if (str_tipo_de_domicilio.equals("otro")) {
                    SPN_tipo_de_domicilio.setSelection(6);
                }
            }
            if (!devolver_datos[5].equals(" ")) {
                ET_ingresar_descripcion.setText(devolver_datos[5]);
            }
        }

        final String valor_guerdado_usuario = sharpref.getString("usuario", null);
        if (valor_guerdado_usuario == null) {
            alertdialog_ingresar_cuenta();
        } else {
            str_array_datos_usuario = valor_guerdado_usuario.split(",");
        }

        if (!boolean_guardar_o_modificar) {

            if (((MainActivity) getActivity()).str_elegir_gps_en_el_mapa_donde_cargar_los_datos_en_los_et == null) {

                buscar_datos_de_la_bd = FirebaseDatabase.getInstance().getReference().child("usuario").child("clientes").child(str_array_datos_usuario[1]);
                buscar_datos_de_la_bd.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String string_cantidad_guardados = (String) dataSnapshot.child("cantidad_modificaciones").getValue();
                        int cantidad_guardados = Integer.parseInt(string_cantidad_guardados);

                        if (cantidad_guardados < 1) {
                            metodos.alerdialog_descargando_informacion(getActivity(), false, "");
                            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setTitle("maximas guardados alcanzado");
                            builder.setMessage("solo se puede modificar los datos 4 veces, esto es para asegurar nuestra base de datos de hackers, porfavor contacte a un administrador y sin problema podra seguir modificando los datos, gracias por entender :)");
                            builder.setCancelable(false);
                            builder.setNegativeButton("llamar a un administrador", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    if (ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                                        metodos.alerdialog_pedir_permisos(getActivity(), 1);
                                        Toast.makeText(getActivity(), "La llamada no pudo ser realizada", Toast.LENGTH_SHORT).show();
                                    } else {
                                        String telefono_a_llamar = getString(R.string.telefono_administrador);
                                        Intent callintent = new Intent(Intent.ACTION_CALL);
                                        //callintent.setData(Uri.parse("tel:2944954574"));
                                        String l = "tel:" + telefono_a_llamar;
                                        callintent.setData(Uri.parse(l));
                                        startActivity(callintent);
                                    }
                                }
                            });
                            builder.setPositiveButton("Volver", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    getActivity().onBackPressed();
                                }
                            });
                            builder.show();
                        } else {
                            ver_cantidad_de_childs(false);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }


        } else {
            buscar_datos_de_la_bd = FirebaseDatabase.getInstance().getReference().child("usuario").child("clientes").child(str_array_datos_usuario[1]).child("ubicacion");
            buscar_datos_de_la_bd.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    //chekeo cuantos childs tengo
                    metodos.alerdialog_descargando_informacion(getActivity(), false, "");

                    int cantidad_childs = (int) dataSnapshot.getChildrenCount();

                    if (cantidad_childs == 3) {

                        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle("maximas ubicaciones alcanzadas");
                        builder.setMessage("Solo pueden guardarse 3 ubicaciones, porfabor modifique una ubicacion");
                        builder.setCancelable(false);
                        builder.setPositiveButton("Volver", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                getActivity().onBackPressed();
                            }
                        });
                        builder.show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }

        BTN_agregar_gps_actual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    metodos.alerdialog_pedir_permisos(getActivity(), 2);
                    Toast.makeText(getActivity(), "Buscar ubicacion no ejecutado", Toast.LENGTH_SHORT).show();
                } else {

                    final LocationManager manager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);
                    if (manager != null) {
                        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                            Toast.makeText(getActivity(), "Buscar ubicacion no ejecutado", Toast.LENGTH_SHORT).show();
                            mainbuildAlertMessageNoGps();
                        } else {
                            if (boolean_encontro_las_areas) {
                                gps_encontrado = false;
                                method_mostrar_alert_dialog_buscando_gps();
                            } else {
                                Toast.makeText(getActivity(), "Buscando las areas del servicio", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
            }

        });

        BTN_ingresar_gps_en_el_mapa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                method_ver_mapa_agregar_ubicacion();

            }
        });


        BTN_Agregar_direccion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //method_guardar_direccion();
                if (boolean_guardar_o_modificar) {
                    ver_cantidad_de_childs(true);
                    metodos.alerdialog_descargando_informacion(getActivity(), true, "revisando datos");
                } else {
                    if (luego_de_modificar_a_donde_guardar != null) {
                        method_guardar_direccion_chekear_datos_en_los_campos(luego_de_modificar_a_donde_guardar);
                    } else {
                        Toast.makeText(getActivity(), "no se ha elegido ninguna ubicacion para sobre escribir", Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });

        if (getActivity() != null) {
            ((MainActivity) getActivity()).chekear_internet();
        }

        return Ingresar_direccion;
    }

    private void method_ver_mapa_agregar_ubicacion() {

        borrar_el_dato_de_donde_voy_a_cargar_los_ed = false; //si me meto en los mapas que no se  borre el str_array_datos_usuario de donde guardar

        String nombre_ubicacion = ET_ingresar_nombre.getText().toString();
        String calle = ET_ingresar_calle.getText().toString();
        String altura = ET_ingresar_altura.getText().toString();
        String pregunta_1 = ET_agregar_pregunta_1.getText().toString();
        String tipo_de_domicilio = SPN_tipo_de_domicilio.getSelectedItem().toString();
        String descripcion = ET_ingresar_descripcion.getText().toString();


        if (nombre_ubicacion.equals("")) {
            nombre_ubicacion = " ";
        }
        if (calle.equals("")) {
            calle = " ";
        }
        if (altura.equals("")) {
            altura = " ";
        }
        if (pregunta_1.equals("")) {
            pregunta_1 = " ";
        }
        if (tipo_de_domicilio.equals("")) {
            tipo_de_domicilio = " ";
        }
        if (descripcion.equals("")) {
            descripcion = " ";
        }

        if (getActivity() != null) {
            ((MainActivity) getActivity()).str_conservar_datos = nombre_ubicacion + "," + calle + "," + altura + "," + pregunta_1 + "," + tipo_de_domicilio + "," + descripcion;
            ((MainActivity) getActivity()).boolean_agregar_gps_en_el_mapa = true;
            ((MainActivity) getActivity()).boolean_solo_mostrar_las_areas_del_mapa = false;
        }

        metodos.main_cambiar_fragment(getActivity(), "c_mapas_ver_covertura");
    }

    public void method_mostrar_alert_dialog_buscando_gps() {

        texto_boton = "buscando su ubicacion, por fabor espere";
        BTN_agregar_gps_actual.setText(texto_boton);
        method_buscar_direccion_gps();

    }

    public void method_buscar_direccion_gps() {

        MyLocation.LocationResult locationResult = new MyLocation.LocationResult() {
            @Override
            public void gotLocation(Location location) {

                if (location != null) {
                    final Point point = new Point(location.getLatitude(), location.getLongitude());
                    str_gps = location.getLatitude() + " , " + location.getLongitude();

                    getActivity().runOnUiThread(new Runnable() {

                        @Override
                        public void run() {

                            boolean adentro_o_afuera_de_algun_poly = false;
                            boolean ejecutarse_solo_una_vez = false;

                            for (Polygon poly : array_poligon) {
                                if (!ejecutarse_solo_una_vez) {
                                    adentro_o_afuera_de_algun_poly = poly.contains(point);
                                    if (adentro_o_afuera_de_algun_poly) {
                                        BTN_agregar_gps_actual.setText("se encuentra dentro de la cobertura");
                                        ejecutarse_solo_una_vez = true;
                                    }
                                }
                            }

                            if (!adentro_o_afuera_de_algun_poly) {
                                BTN_agregar_gps_actual.setText("no esta en cobertura");

                                metodos.alertdialog_error_accion_solo_cerrar_el_alert(getActivity(), "La ubicacion elegida no tiene cobertura",
                                        "Apriete el boton ver mapas de covertura para fijarse donde estan las areas de cobertura, tambien se puede dar el caso que la ubicacion del gps no es precisa",
                                        "Cerrar",false);

                                method_guardar_direccion_sin_ubicacion(str_gps);
                            }

                            tv_direccion_gps.setText("GPS = " + str_gps);
                        }
                    });

                    gps_encontrado = true;

                } else {

                    getActivity().runOnUiThread(new Runnable() {

                        @Override
                        public void run() {

                            if (control_int == 0 | control_int == 1 | control_int == 2) {
                                method_buscar_direccion_gps();
                                BTN_agregar_gps_actual.setText("buscando, intento = " + String.valueOf(control_int + 1) + "/3");
                                control_int++;
                            } else {
                                BTN_agregar_gps_actual.setText("ubicacion no encontrada, haga click aqui de nuevo o busque en el mapa");
                                control_int = 0;
                            }

                            Toast.makeText(getActivity(), "direccion gps no encontrada intente de nuevo o busque en el mapa", Toast.LENGTH_SHORT).show();

                        }
                    });

                }

            }
        };
        MyLocation myLocation = new MyLocation();
        myLocation.getLocation(getActivity(), locationResult);
    }

    private void method_guardar_direccion_sin_ubicacion(String geo_point) {

        final String valor_guerdado_ubicacion = sharpref.getString("sin_ubicacion", "0");

        int contador = Integer.parseInt(valor_guerdado_ubicacion);
        if (contador < 3) {

            contador++;
            String guardar_str = String.valueOf(contador);

            String str_guardar = geo_point + "," + valor_guerdado_usuario;
            DatabaseReference guardar_ubicacion = FirebaseDatabase.getInstance().getReference().child("ubicacion_sin_covertura");
            guardar_ubicacion.child("ubicacion_sin_covertura").push().setValue(str_guardar);

            SharedPreferences.Editor editor = sharpref.edit();
            editor.putString("sin_ubicacion", guardar_str);
            editor.apply();
        }
    }

    private void ver_cantidad_de_childs(final Boolean guardar_o_modificar_childs_count) {
        //chekeo que tengo que hacer si guardar o modificar
        //true = guardar - false = modificar

        buscar_datos_de_la_bd = FirebaseDatabase.getInstance().getReference().child("usuario").child("clientes").child(str_array_datos_usuario[1]).child("ubicacion");

        buscar_datos_de_la_bd.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //chekeo cuantos childs tengo

                int cantidad_childs = (int) dataSnapshot.getChildrenCount();
                if (cantidad_childs >= 1) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String key = (String) snapshot.child("nombre").getValue();
                        childs.add(key);

                        String parent_key = snapshot.getKey();
                        parent_childs.add(parent_key);

                    }
                    if (guardar_o_modificar_childs_count) { //estoy guardando un lugar nuevo

                        //si tengo 1 child guado en 2, si tengo 2 childs guardo en 3
                        if (cantidad_childs == 1) {
                            method_guardar_direccion_chekear_datos_en_los_campos(" ");
                        }
                        if (cantidad_childs == 2) {
                            method_guardar_direccion_chekear_datos_en_los_campos(" ");
                        }
                        if (cantidad_childs == 3) {

                            if (getActivity() != null) {
                                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                metodos.alerdialog_descargando_informacion(getActivity(), false, "");
                                builder.setTitle("maximas ubicaciones alcanzadas");
                                builder.setMessage("Solo pueden guardarse 3 ubicaciones, porfabor modifique una ubicacion");

                                builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                    }
                                });
                                builder.show();
                            }
                        }
                    } else {


                        //muesta el alertdialog con las opciones a modificar
                        alertdialog_que_ubicacion_modificar();
                    }


                } else { //si solo tengo 1 child guardo en ubicacion 1
                    if (guardar_o_modificar_childs_count) {
                        method_guardar_direccion_chekear_datos_en_los_campos(" ");
                    } else {
                        luego_de_modificar_a_donde_guardar = " ";
                        Toast.makeText(getActivity(), "no hay ubicaciones para modificar ", Toast.LENGTH_LONG).show();
                    }
                }
                if (getActivity() != null) {
                    metodos.alerdialog_descargando_informacion(getActivity(), false, "");
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    private void alertdialog_que_ubicacion_modificar() {

        //que ubicacion deseo modificar

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Que ubicacion desea modificar?");

        String[] mStringArray = new String[childs.size()];
        mStringArray = childs.toArray(mStringArray);
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                getActivity().onBackPressed();
            }
        });

        builder.setItems(mStringArray, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int position) {
                if (position == 0) {
                    poner_los_datos_en_los_edittext(parent_childs.get(position));
                    luego_de_modificar_a_donde_guardar = parent_childs.get(position);
                }
                if (position == 1) {
                    poner_los_datos_en_los_edittext(parent_childs.get(position));
                    luego_de_modificar_a_donde_guardar = parent_childs.get(position);
                }
                if (position == 2) {
                    poner_los_datos_en_los_edittext(parent_childs.get(position));
                    luego_de_modificar_a_donde_guardar = parent_childs.get(position);
                }
                dialog.dismiss();
            }
        });

        builder.show();


    }

    private void poner_los_datos_en_los_edittext(String ubicacion) {

        if (getActivity() != null) {
            ((MainActivity) getActivity()).str_elegir_gps_en_el_mapa_donde_cargar_los_datos_en_los_et = ubicacion;
        }

        buscar_datos_de_la_bd = FirebaseDatabase.getInstance().getReference().child("usuario").child("clientes").child(str_array_datos_usuario[1]).child("ubicacion").child(ubicacion);

        buscar_datos_de_la_bd.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                str_nombre = (String) dataSnapshot.child("nombre").getValue();
                str_calle = (String) dataSnapshot.child("calle").getValue();
                str_altura = (String) dataSnapshot.child("altura").getValue();
                str_gps = (String) dataSnapshot.child("gps").getValue();
                str_pregunta_1 = (String) dataSnapshot.child("pregunta").getValue();
                str_tipo_de_domicilio = (String) dataSnapshot.child("tipo_domicilio").getValue();
                str_descripcion = (String) dataSnapshot.child("descripcion").getValue();

                ET_ingresar_nombre.setText(str_nombre);
                ET_ingresar_calle.setText(str_calle);
                ET_ingresar_altura.setText(str_altura);

                BTN_agregar_gps_actual.setText("cambiar gps a direccion actual");
                tv_direccion_gps.setText("GPS = " + str_gps);

                // "Casa", "Departamento", "Cabaña", "hotel", "negocio - oficina", "otro"
                if (str_tipo_de_domicilio.equals("Casa")) {
                    SPN_tipo_de_domicilio.setSelection(1);
                    ET_agregar_pregunta_1.setText(str_pregunta_1);
                }
                if (str_tipo_de_domicilio.equals("Departamento")) {
                    SPN_tipo_de_domicilio.setSelection(2);
                    ET_agregar_pregunta_1.setText(str_pregunta_1);
                }
                if (str_tipo_de_domicilio.equals("Cabaña")) {
                    SPN_tipo_de_domicilio.setSelection(3);
                    ET_agregar_pregunta_1.setText(str_pregunta_1);
                }
                if (str_tipo_de_domicilio.equals("hotel")) {
                    SPN_tipo_de_domicilio.setSelection(4);
                    ET_agregar_pregunta_1.setText(str_pregunta_1);
                }
                if (str_tipo_de_domicilio.equals("negocio - oficina")) {
                    SPN_tipo_de_domicilio.setSelection(5);
                    ET_agregar_pregunta_1.setText(str_pregunta_1);
                }
                if (str_tipo_de_domicilio.equals("otro")) {
                    SPN_tipo_de_domicilio.setSelection(6);
                    ET_agregar_pregunta_1.setText(str_pregunta_1);
                }

                if (str_descripcion != null) {
                    ET_ingresar_descripcion.setText(str_descripcion);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void alertdialog_ingresar_cuenta() {

        AlertDialog.Builder alerta = new AlertDialog.Builder(
                getActivity());
        alerta.setTitle("Aviso");
        alerta.setMessage("Usted no esta ingresado con ninguna cuenta");
        alerta.setPositiveButton("AGEGAR CUENTA", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                final FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.content_main, new c_ingreso_a_la_app()).addToBackStack(toString());
                ft.commit();

                dialog.dismiss();
            }

        });
        alerta.setNegativeButton("VOLVER ATRAS", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                ((MainActivity) getActivity()).boolean_solo_mostrar_las_areas_del_mapa = false;
                final FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.content_main, new c_mapas_ver_covertura()).addToBackStack(toString());
                ft.commit();

                dialog.dismiss();
            }
        });


        alerta.show();

    }

    private void method_guardar_direccion_chekear_datos_en_los_campos(String donde_guardar) {
        if (getActivity() != null) {
            metodos.alerdialog_descargando_informacion(getActivity(), false, "");
        }
        int_que_opcion_esta_seleccionada_en_el_spineer = SPN_tipo_de_domicilio.getSelectedItemPosition();

        boolean los_caracteres_estan_bien = metodos.method_chekear_texto_de_los_edittext(getActivity(), ET_ingresar_nombre.getText().toString());
        boolean los_caracteres_estan_bien2 = metodos.method_chekear_texto_de_los_edittext(getActivity(), ET_ingresar_calle.getText().toString());
        boolean los_caracteres_estan_bien3 = metodos.method_chekear_texto_de_los_edittext(getActivity(), ET_ingresar_altura.getText().toString());
        boolean los_caracteres_estan_bien4 = metodos.method_chekear_texto_de_los_edittext(getActivity(), ET_agregar_pregunta_1.getText().toString());
        boolean los_caracteres_estan_bien5 = metodos.method_chekear_texto_de_los_edittext(getActivity(), ET_ingresar_descripcion.getText().toString());

        if (los_caracteres_estan_bien & los_caracteres_estan_bien2 & los_caracteres_estan_bien3 & los_caracteres_estan_bien4 & los_caracteres_estan_bien5) {

            //campos opcionales
            if (int_que_opcion_esta_seleccionada_en_el_spineer != 0) {
                str_tipo_de_domicilio = SPN_tipo_de_domicilio.getSelectedItem().toString();
            } else {
                str_tipo_de_domicilio = "sin tipo de domicilio";
            }
            if (ET_agregar_pregunta_1.getText().length() > 1) {
                str_pregunta_1 = ET_agregar_pregunta_1.getText().toString();
            } else {
                str_pregunta_1 = " ";
            }
            if (ET_ingresar_descripcion.getText().length() > 1) {
                str_descripcion = ET_ingresar_descripcion.getText().toString();
            } else {
                str_descripcion = " ";
            }

            if (ET_ingresar_nombre.getText().length() > 1) {
                if (ET_ingresar_calle.getText().length() > 1) {
                    if (ET_ingresar_altura.getText().length() > 1) {
                        if (str_gps != null) {
                            str_nombre = ET_ingresar_nombre.getText().toString();
                            str_calle = ET_ingresar_calle.getText().toString();
                            str_altura = ET_ingresar_altura.getText().toString();
                            str_tipo_de_domicilio = SPN_tipo_de_domicilio.getSelectedItem().toString();
                            method_guardar_direccion_en_firebase(donde_guardar);
                        } else {
                            Toast.makeText(getActivity(), "Agrege una direccion gps", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getActivity(), "Agrege la altura de la calle", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getActivity(), "Agrege la calle", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getActivity(), "Agrege la nombre", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void method_guardar_direccion_en_firebase(String donde_guardar) {
        if (getActivity() != null) {
            metodos.alerdialog_descargando_informacion(getActivity(), true, "Guardando direccion");
        }

        if (donde_guardar.equals(" ")) {
            db_donde_guardar = FirebaseDatabase.getInstance().getReference().child("usuario").child("clientes").child(str_array_datos_usuario[1]).child("ubicacion").push();
        } else {
            db_donde_guardar = FirebaseDatabase.getInstance().getReference().child("usuario").child("clientes").child(str_array_datos_usuario[1]).child("ubicacion").child(donde_guardar);
        }
        final DatabaseReference nDataBase_2 = FirebaseDatabase.getInstance().getReference().child("usuario").child("clientes").child(str_array_datos_usuario[1]);
        nDataBase_2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String string_cantidad_guardados = (String) dataSnapshot.child("cantidad_modificaciones").getValue();
                int cantidad_guardados = Integer.parseInt(string_cantidad_guardados);
                cantidad_guardados = cantidad_guardados - 1;
                nDataBase_2.child("cantidad_modificaciones").setValue(String.valueOf(cantidad_guardados)).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        ((MainActivity) getActivity()).ubicacion_elegida = str_nombre;
                        ((MainActivity) getActivity()).ubicacion_calle_y_altura =str_nombre+"," + str_calle+","+str_altura;
                        ((MainActivity) getActivity()).gps_elegido = str_gps;
                        db_donde_guardar.child("nombre").setValue(str_nombre);
                        db_donde_guardar.child("calle").setValue(str_calle);
                        db_donde_guardar.child("altura").setValue(str_altura);
                        db_donde_guardar.child("gps").setValue(str_gps);
                        db_donde_guardar.child("tipo_domicilio").setValue(str_tipo_de_domicilio);
                        db_donde_guardar.child("pregunta").setValue(str_pregunta_1);
                        db_donde_guardar.child("descripcion").setValue(str_descripcion).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                metodos.alerdialog_descargando_informacion(getActivity(), false, "");
                                Toast.makeText(getActivity(), "ubicacion guardada correctamente", Toast.LENGTH_SHORT).show();
                                ((MainActivity) getActivity()).str_elegir_gps_en_el_mapa_donde_cargar_los_datos_en_los_et = null;
                                ((MainActivity) getActivity()).str_elegido_en_el_mapa = null;
                                ((MainActivity) getActivity()).str_conservar_datos = null;
                                luego_de_modificar_a_donde_guardar = null;

                                final FragmentTransaction ft = getFragmentManager().beginTransaction();
                                ft.replace(R.id.content_main, new c_principal()).addToBackStack(toString());
                                ft.commit();
                            }
                        });
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void buscar_area_de_los_deliverys(final FirebaseCallBack_buscar_areas firebaseCallBack) {

        DatabaseReference database_areas_delivery = FirebaseDatabase.getInstance().getReference().child("deliverys");
        database_areas_delivery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snap_deliverys : dataSnapshot.getChildren()) {
                    for (DataSnapshot snap_areas : snap_deliverys.child("area").getChildren()) {

                        Polygon.Builder poly2 = new Polygon.Builder();
                        String str_area_input = (String) snap_areas.getValue();
                        String[] areas = str_area_input.split(",");
                        for (int i_poly = 0; i_poly < 4; i_poly++) {
                            String[] coord = areas[i_poly].split("€");
                            double coord1 = Double.parseDouble(coord[0]);
                            double coord2 = Double.parseDouble(coord[1]);
                            Point point = new Point(coord1, coord2);
                            poly2.addVertex(point);
                        }
                        Polygon polygon1 = poly2.build();
                        array_poligon.add(polygon1);
                    }
                }
                firebaseCallBack.onCallBack(array_poligon);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private interface FirebaseCallBack_buscar_areas {
        void onCallBack(ArrayList<Polygon> array_polys);
    }

    //permisos y antibugs

    @Override
    public void onPause() {
        MyLocation myLocation = new MyLocation();
        myLocation.cancelTimer();
        gps_encontrado = true;
        super.onPause();
    }

    public void mainbuildAlertMessageNoGps() {
        ActivityCompat.requestPermissions(getActivity(), new String[]{ACCESS_FINE_LOCATION}, 1);
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getActivity());
        View bottomSheetview = getLayoutInflater().inflate(R.layout.bottom_sheet_coneccion_gps, null);
        bottomSheetDialog.setContentView(bottomSheetview);
        bottomSheetDialog.show();
        Button boton_si = bottomSheetDialog.findViewById(R.id.button);
        Button boton_no = bottomSheetDialog.findViewById(R.id.button2);

        boton_si.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= 23) {
                    if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        metodos.alerdialog_pedir_permisos(getActivity(), 2);
                    } else {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        bottomSheetDialog.dismiss();
                    }
                } else {
                    startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    bottomSheetDialog.dismiss();
                }
            }
        });
        boton_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.dismiss();
            }
        });
    }

    @Override
    public void onDestroy() {
        if (borrar_el_dato_de_donde_voy_a_cargar_los_ed) {
            if (getActivity() != null) {
                ((MainActivity) getActivity()).str_elegir_gps_en_el_mapa_donde_cargar_los_datos_en_los_et = null;
            }
        }
        super.onDestroy();
    }
}

package com.venta_productos.delivery;


import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * A simple {@link Fragment} subclass.
 */
public class c_editar_usuario_y_log_out extends Fragment {


    public c_editar_usuario_y_log_out() {
        // Required empty public constructor
    }

    Button salir_de_la_cuenta, modificar_direccion, agregar_direccion, cambiar_numero_telefono_menu, BTN_guardar_numerotelefono, btn_borrar_direccion;
    EditText ET_numero_telefono;
    LinearLayout LINEAR_modificar_numero_telefono;
    private FirebaseAuth mAuth;
    SharedPreferences sharpref;
    Context contex;
    String TAG = "asdf", str_usuario;
    DatabaseReference buscar_datos_del_cliente_en_la_bd;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View Log_out = inflater.inflate(R.layout.f_log_out, container, false);
        mAuth = FirebaseAuth.getInstance();

        salir_de_la_cuenta = Log_out.findViewById(R.id.BTN_salir_de_la_cuenta);
        agregar_direccion = Log_out.findViewById(R.id.BTN_agregar_direcion);
        modificar_direccion = Log_out.findViewById(R.id.BTN_modificar_direccion);
        btn_borrar_direccion = Log_out.findViewById(R.id.BTN_borrar_direccion);

        cambiar_numero_telefono_menu = Log_out.findViewById(R.id.BTN_editar_numero_menu);
        BTN_guardar_numerotelefono = Log_out.findViewById(R.id.BTN_editar_usuario_editar_numero_telefono);
        ET_numero_telefono = Log_out.findViewById(R.id.ET_editar_usuario_editar_numerotelefono);
        LINEAR_modificar_numero_telefono = Log_out.findViewById(R.id.Linear_editar_numerotelefono);

        contex = getActivity();
        sharpref = getContext().getSharedPreferences("usar_app", Context.MODE_PRIVATE);
        final String valor_guerdado = sharpref.getString("forma_ingreso", "no hay dato");

        String input_datos_usuario = sharpref.getString("usuario", "no hay dato");
        final String[] dato = input_datos_usuario.split(",");
        str_usuario = dato[1];
        buscar_datos_del_cliente_en_la_bd = FirebaseDatabase.getInstance().getReference().child("usuario").child("clientes").child(str_usuario);


        if (mAuth == null) {
            Toast.makeText(getActivity(), "usted no esta ingresado", Toast.LENGTH_SHORT).show();
        }

        cambiar_numero_telefono_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cambiar_numero_telefono_menu.setVisibility(View.GONE);
                LINEAR_modificar_numero_telefono.setVisibility(View.VISIBLE);
            }
        });

        BTN_guardar_numerotelefono.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                method_guardar_nuevo_num_telefono();
            }
        });

        agregar_direccion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getActivity() != null) {
                    ((MainActivity) getActivity()).agregar_o_modificar_direccion_boolean = true;
                }
                metodos.main_cambiar_fragment(getActivity(), "c_ingresar_direccion");
            }
        });

        modificar_direccion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getActivity() != null) {
                    ((MainActivity) getActivity()).agregar_o_modificar_direccion_boolean = false;
                }
                metodos.main_cambiar_fragment(getActivity(), "c_ingresar_direccion");
            }
        });

        salir_de_la_cuenta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (getActivity() != null) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Desea salir de la cuenta?");
                    builder.setPositiveButton("salir", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (mAuth != null) {
                                mAuth.signOut();
                                if (!valor_guerdado.equals("no hay dato")) {
                                    LoginManager.getInstance().logOut();
                                }
                                sharpref.edit().remove("usuario").apply();
                                sharpref.edit().remove("forma_ingreso").apply();

                                metodos.main_cambiar_fragment(getActivity(), "c_ingreso_a_la_app");

                            } else {
                                Toast.makeText(getActivity(), "usted no esta ingresado", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    builder.setNegativeButton("Cerrar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    builder.show();
                }
            }
        });

        btn_borrar_direccion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                method_borrar_direccion();
            }
        });

        if (getActivity() != null) {
            ((MainActivity) getActivity()).chekear_internet();
        }
        return Log_out;
    }

    private void method_borrar_direccion() {
        btn_borrar_direccion.setClickable(false);

        buscar_datos_del_cliente_en_la_bd.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("ubicacion")) {

                    HashMap<String, String> hash_con_las_ubicaciones = new HashMap<>();
                    ArrayList<String> array_pasar_nombres = new ArrayList<>();

                    for (DataSnapshot snap_ubicaciones : dataSnapshot.child("ubicacion").getChildren()) {
                        String nombre_ubicacion = (String) snap_ubicaciones.child("nombre").getValue();
                        String key_ubicacion = snap_ubicaciones.getKey();
                        hash_con_las_ubicaciones.put(nombre_ubicacion, key_ubicacion);
                        array_pasar_nombres.add(nombre_ubicacion);
                    }

                    Log.d(TAG, "hash: " + hash_con_las_ubicaciones);
                    Log.d(TAG, "array: " + array_pasar_nombres);
                    alertdialog_que_ubicacion_borrar(hash_con_las_ubicaciones, array_pasar_nombres);
                    btn_borrar_direccion.setClickable(true);

                } else {
                    Toast.makeText(getActivity(), "no tiene ubicaciones para borrar", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void alertdialog_que_ubicacion_borrar(final HashMap<String, String> hash_ubicaciones, ArrayList<String> items) {

        //que ubicacion deseo modificar

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Que ubicacion desea borrar?");

        String[] mStringArray = new String[items.size()];
        mStringArray = items.toArray(mStringArray);

        final String[] finalMStringArray = mStringArray;
        builder.setItems(mStringArray, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int position) {
                alertdialog_confirmar_borrar(hash_ubicaciones.get(finalMStringArray[position]), finalMStringArray[position]);
                dialog.dismiss();
            }
        });

        builder.show();


    }

    private void alertdialog_confirmar_borrar(final String key, final String nombre) {
        Log.d(TAG, "key: " + key);
        Log.d(TAG, "nombre: " + nombre);

        AlertDialog.Builder alerta = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.ad_basico_titulo_texto_botones, null);
        alerta.setView(dialogView);
        final AlertDialog alertDialog_simple = alerta.create();
        alertDialog_simple.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView tv_titulo = dialogView.findViewById(R.id.ad_tv_titulo);
        TextView tv_mensaje = dialogView.findViewById(R.id.ad_tv_mensaje);
        Button btn_aceptar = dialogView.findViewById(R.id.btn_aceptar);
        Button btn_cancelar = dialogView.findViewById(R.id.btn_cancelar);

        tv_titulo.setText("Confirmar Borrar= " + nombre);
        tv_mensaje.setVisibility(View.GONE);

        btn_aceptar.setText("confirmar");


        btn_aceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                method_borrar_ubicacion_en_firebase(key,nombre);
                alertDialog_simple.dismiss();
            }
        });
        btn_cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog_simple.dismiss();
            }
        });
        alertDialog_simple.show();
    }

    private void method_borrar_ubicacion_en_firebase(String key, String nombre) {

        Log.d(TAG, "main ubicacion: " + ((MainActivity) getActivity()).ubicacion_elegida);
        Log.d(TAG, "ubicacion: " + nombre);
        Log.d(TAG, "key: " + key);

        if (getActivity() != null) {
            if (((MainActivity) getActivity()).ubicacion_elegida != null) {
                if (((MainActivity) getActivity()).ubicacion_elegida.equals(nombre)) {
                    ((MainActivity) getActivity()).ubicacion_elegida = null;
                    ((MainActivity) getActivity()).ubicacion_calle_y_altura = null;
                }
            }
            Log.d(TAG, "ejecutado");
            buscar_datos_del_cliente_en_la_bd.child("ubicacion").child(key).removeValue();
        }

    }

    private void method_guardar_nuevo_num_telefono() {
        buscar_datos_del_cliente_en_la_bd.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String string_cantidad_guardados = (String) dataSnapshot.child("cantidad_modificaciones").getValue();
                int cantidad_guardados = Integer.parseInt(string_cantidad_guardados);

                if (cantidad_guardados < 1) {
                    method_mostrar_alert_no_tiene_mas_guardados();
                } else {
                    if (ET_numero_telefono.getText().length() < 8 || ET_numero_telefono.getText().length() > 14) {
                        Toast.makeText(getActivity(), "error formato del numero de telefono", Toast.LENGTH_SHORT).show();
                    } else {

                        method_alertdialog_confirmacion();


                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void method_alertdialog_confirmacion() {

        final String nuevo_numero = ET_numero_telefono.getText().toString();

        if (getActivity() != null) {


            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Confirmar nuevo numero");
            builder.setMessage("desea agregar este numero = " + nuevo_numero);
            builder.setCancelable(false);
            builder.setPositiveButton("Guardar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    method_guardar_en_firebase();
                }
            });
            builder.setNegativeButton("Cerrar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            builder.show();
        }
    }

    private void method_guardar_en_firebase() {

        metodos.alerdialog_descargando_informacion(getActivity(), true, "Guardando informacion");

        final String nuevo_numero = ET_numero_telefono.getText().toString();
        buscar_datos_del_cliente_en_la_bd.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String string_cantidad_guardados = (String) dataSnapshot.child("cantidad_modificaciones").getValue();
                int cantidad_guardados = Integer.parseInt(string_cantidad_guardados);
                cantidad_guardados = cantidad_guardados - 1;
                buscar_datos_del_cliente_en_la_bd.child("cantidad_modificaciones").setValue(String.valueOf(cantidad_guardados)).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        buscar_datos_del_cliente_en_la_bd.child("telefono").setValue(nuevo_numero).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                metodos.alerdialog_descargando_informacion(getActivity(), false, "");
                                Toast.makeText(getActivity(), "guardado correctamente", Toast.LENGTH_SHORT).show();
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

    private void method_mostrar_alert_no_tiene_mas_guardados() {

        if (getActivity() != null) {
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
                        String l = "tel:" + telefono_a_llamar;
                        callintent.setData(Uri.parse(l));
                        startActivity(callintent);
                    }
                }
            });
            builder.setPositiveButton("Cerrar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            builder.show();
        }
    }

}

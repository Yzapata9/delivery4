package com.venta_productos.delivery;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CALL_PHONE;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class metodos {

    static String pasar_datos_del_negocio_al_fragment_categorias = null;//le pasa al fragment categorias principal

    static String pasar_datos_categorias_negocios = null; //muestra las listas de productos
    static String pasar_datos_productos_negocio = null;

    static String pasar_producto_seleccionado = null; // muestra los detalles del producto seleccionado
    static String pasar_categoria_producto_seleccionado = null;

    private static AlertDialog alertDialog_descargando_informacion = null;
    private static AlertDialog alertDialog_puntuar_pedido = null;
    private static AlertDialog alertDialog_cerrar_solamente = null;
    public static String TAG = "asdf";

    public static boolean ingresando = false;


    public static void alerdialog_descargando_informacion(Context context, Boolean abrir_o_cerrar, String texto_mostrar) {

        if (abrir_o_cerrar) {
            if (alertDialog_descargando_informacion != null) {
                alertDialog_descargando_informacion.dismiss();
                alertDialog_descargando_informacion = null;
            }
            // lo puse aca para que no se ejecute nada amenos que sea necesario
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View dialogView = inflater.inflate(R.layout.ad_cargando_informacion, null);
            dialogBuilder.setView(dialogView);
            alertDialog_descargando_informacion = dialogBuilder.create();
            alertDialog_descargando_informacion.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            TextView mostrar_texto = dialogView.findViewById(R.id.progress_bar_texto_mostrar);
            mostrar_texto.setText(texto_mostrar);

            if (texto_mostrar != null) {
                if (texto_mostrar.equals("Escuchando")) {
                    alertDialog_descargando_informacion.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialogInterface) {
                            if (metodos_busqueda.recognizer != null) {
                                metodos_busqueda.recognizer.cancel();
                            }
                        }
                    });
                }
            }
            alertDialog_descargando_informacion.show();

        } else if (!abrir_o_cerrar & alertDialog_descargando_informacion != null) {
            alertDialog_descargando_informacion.dismiss();
            alertDialog_descargando_informacion = null;
        }
    }

    static void alertdialog_comentario_o_bug(final Context context, final boolean es_comentario_o_bug, final String str_usuario) {

        String[] datos_usuario = str_usuario.split(",");

        String str_que_es = " ";
        if (es_comentario_o_bug) {
            str_que_es = "mejora";
        } else {
            str_que_es = "bug";
        }

        Calendar cal = Calendar.getInstance();
        String anio = String.valueOf(cal.get(Calendar.YEAR));
        String mes = String.valueOf(cal.get(Calendar.MONTH) + 1);
        String dia = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));

        final DatabaseReference db_guardar_mejora = FirebaseDatabase.getInstance().getReference().child("feedback").child(anio).child(mes).child(dia).child(str_que_es).child(datos_usuario[1]);
        final DatabaseReference db_buscar_contador_de_mejoras = FirebaseDatabase.getInstance().getReference().child("usuario").child("clientes").child(datos_usuario[1]);
        db_buscar_contador_de_mejoras.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final long cantidad = (long) dataSnapshot.child("cantidad_mejoras_bug").getValue();

                if (cantidad > 0) {
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
                    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View dialogView = inflater.inflate(R.layout.ad_puntuar_pedido, null);
                    dialogBuilder.setView(dialogView);
                    alertDialog_puntuar_pedido = dialogBuilder.create();
                    alertDialog_puntuar_pedido.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                    TextView ad_titulo = dialogView.findViewById(R.id.ad_puntuar_titulo);
                    final EditText ed_mejora = dialogView.findViewById(R.id.ad_puntuar_ed);
                    Button btn_agregar = dialogView.findViewById(R.id.ad_puntuar_btn_puntuar);
                    ImageView imagen1 = dialogView.findViewById(R.id.ad_puntuar_iv_positivo);
                    ImageView imagen2 = dialogView.findViewById(R.id.ad_puntuar_iv_negativo);

                    imagen1.setVisibility(View.GONE);
                    imagen2.setVisibility(View.GONE);
                    btn_agregar.setText("enviar");

                    String str_titulo = "";
                    if (es_comentario_o_bug) {
                        str_titulo = "Que mejora le haria a la app?";
                        ed_mejora.setHint("escriba aqui su mejora");
                    } else {
                        str_titulo = "Que bug encontro";
                        ed_mejora.setHint("escriba aqui el bug");
                    }
                    ad_titulo.setText(str_titulo);

                    btn_agregar.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (ed_mejora.getText().toString().length() < 5) {
                                if (ed_mejora.getText().toString().length() > 500) {
                                    String str_mejora = ed_mejora.getText().toString();
                                    long cantidad_de_guardados_disponibles = cantidad - 1;
                                    db_buscar_contador_de_mejoras.child("cantidad_mejoras_bug").setValue(cantidad_de_guardados_disponibles);
                                    db_guardar_mejora.push().setValue(str_mejora);
                                    Toast.makeText(context, "mejoras disponibles restantes= " + (cantidad - 1), Toast.LENGTH_SHORT).show();
                                    alertDialog_puntuar_pedido.dismiss();
                                } else {
                                    Toast.makeText(context, "mejora demaciado larga", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(context, "Debe escribir una mejora", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    alertDialog_puntuar_pedido.show();
                } else {
                    Toast.makeText(context, "se quedo sin mejoras disponibles", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    static boolean method_chekear_texto_de_los_edittext(Activity activity, String input_et) {
        boolean boolean_el_texto_esta_correcto = true;
        if (input_et != null) {
            if (input_et.contains(",") | input_et.contains("=") | input_et.contains("€") | input_et.contains("#") | input_et.contains("·") | input_et.contains("-")) {
                boolean_el_texto_esta_correcto = false;
                alertdialog_error_accion_solo_cerrar_el_alert(activity, "caracteres reservados", "los caracteres  = , € # · -  estan reservados para el sistema, por favor, modifique el texto ingresado ", null, false);
            } else if (input_et.length() > 250) {
                boolean_el_texto_esta_correcto = false;
                Toast.makeText(activity, "El texto ingresado es demaciado largo, max 250 caracteres. ingresados= " + input_et.length(), Toast.LENGTH_SHORT).show();

            }
        }
        return boolean_el_texto_esta_correcto;
    }

    static void alertdialog_error_accion_solo_cerrar_el_alert(final Activity activity, String str_titulo, String str_mensaje, String str_btn_cerrar, final boolean boolean_no_cancelable) {

        final AlertDialog.Builder alerta = new AlertDialog.Builder(activity);

        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View dialogView = inflater.inflate(R.layout.ad_basico_titulo_texto_botones, null);
        TextView tv_titulo = dialogView.findViewById(R.id.ad_tv_titulo);
        TextView tv_mensaje = dialogView.findViewById(R.id.ad_tv_mensaje);
        Button btn_aceptar = dialogView.findViewById(R.id.btn_aceptar);
        Button btn_cancelar = dialogView.findViewById(R.id.btn_cancelar);

        alerta.setView(dialogView);
        alertDialog_cerrar_solamente = alerta.create();
        alertDialog_cerrar_solamente.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        if (boolean_no_cancelable) {
            alertDialog_cerrar_solamente.setCancelable(false);
        }
        tv_titulo.setText(str_titulo);
        if (str_mensaje != null) {
            tv_mensaje.setText(str_mensaje);
        }
        if (str_btn_cerrar != null) {
            btn_cancelar.setText(str_btn_cerrar);
        }
        btn_aceptar.setVisibility(View.GONE);

        btn_cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (boolean_no_cancelable) {
                    llamar(activity);
                } else {
                    alertDialog_cerrar_solamente.dismiss();
                }
            }
        });


        alertDialog_cerrar_solamente.show();
    }

    private static AlertDialog aler_doble_accion = null;

    static void alertdialog_doble_accion(final Activity activity, String str_titulo, String str_mensaje, String str_btn_aceptar, String str_btn_cerrar, final String str_fragment, String str_method, boolean es_cancelable) {

        final AlertDialog.Builder alerta = new AlertDialog.Builder(activity);
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View dialogView = inflater.inflate(R.layout.ad_basico_titulo_texto_botones, null);
        TextView tv_titulo = dialogView.findViewById(R.id.ad_tv_titulo);
        TextView tv_mensaje = dialogView.findViewById(R.id.ad_tv_mensaje);
        Button btn_aceptar = dialogView.findViewById(R.id.btn_aceptar);
        Button btn_cancelar = dialogView.findViewById(R.id.btn_cancelar);

        tv_titulo.setText(str_titulo);

        if (str_mensaje != null) {
            tv_mensaje.setText(str_mensaje);
        }
        if (str_btn_cerrar != null) {
            btn_cancelar.setText(str_btn_cerrar);
        }
        if (str_btn_aceptar != null) {
            btn_aceptar.setText(str_btn_aceptar);
        }

        btn_aceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                aler_doble_accion.dismiss();

                if (str_fragment.equals("c_principal")) {
                    main_cambiar_fragment(activity, "c_ingresar_direccion");
                }
                if (str_fragment.equals("c_elegir_delivery_confirmacion")) {
                    main_cambiar_fragment(activity, "c_principal");
                }
                if (str_fragment.equals("c_carrito")) {
                    main_cambiar_fragment(activity, "c_carrito");
                }
                if (str_fragment.equals("metodos")) {
                    main_cambiar_fragment(activity, "c_puntuar");
                }
            }
        });

        btn_cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                aler_doble_accion.dismiss();
                if (str_fragment.equals("c_principal")) {
                    main_cambiar_fragment(activity, "c_mapas_ver_covertura");
                }
                if (str_fragment.equals("c_elegir_delivery_confirmacion")) {
                    aler_doble_accion.dismiss();
                }
                if (str_fragment.equals("c_carrito")) {
                    aler_doble_accion.dismiss();
                }
                if (str_fragment.equals("metodos")) {

                    SharedPreferences sharpref = activity.getSharedPreferences("usar_app", Context.MODE_PRIVATE);
                    sharpref.edit().remove("puntuar_pedido").apply();

                    aler_doble_accion.dismiss();
                }
            }
        });

        alerta.setView(dialogView);
        aler_doble_accion = alerta.create();

        if (!es_cancelable) {
            aler_doble_accion.setCancelable(false);
        }
        aler_doble_accion.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        aler_doble_accion.show();


    }

    public static void checkear_estado_del_usuario(final Activity activity, final String str_usuario) {
        if (activity != null & str_usuario != null) {
            final DatabaseReference checkear_estado = FirebaseDatabase.getInstance().getReference().child("usuario").child("clientes").child(str_usuario);
            checkear_estado.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String estado = (String) dataSnapshot.child("estado").getValue();
                    if (estado != null) {
                        if (!estado.equals("ok")) {
                            //mostrar alert
                            alertdialog_error_accion_solo_cerrar_el_alert(activity, "Querido Cliente= ", estado, "llamar", true);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }
    }

    private static AlertDialog Alert_ingresar_a_la_cuenta;

    static void alertdialog_ingresar_a_la_cuenta(final Activity activity) {

        AlertDialog.Builder alerta = new AlertDialog.Builder(
                activity);

        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View dialogView = inflater.inflate(R.layout.ad_basico_titulo_texto_botones, null);
        TextView tv_titulo = dialogView.findViewById(R.id.ad_tv_titulo);
        TextView tv_mensaje = dialogView.findViewById(R.id.ad_tv_mensaje);
        Button btn_aceptar = dialogView.findViewById(R.id.btn_aceptar);
        Button btn_cancelar = dialogView.findViewById(R.id.btn_cancelar);

        tv_titulo.setText("Sin cuenta");
        tv_mensaje.setText("Debe estar logeado para continuar");
        btn_aceptar.setText("Ingresar!");
        btn_cancelar.setVisibility(View.GONE);

        btn_aceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Alert_ingresar_a_la_cuenta.dismiss();
                main_cambiar_fragment(activity, "c_ingreso_a_la_app");
            }
        });

        alerta.setView(dialogView);
        Alert_ingresar_a_la_cuenta = alerta.create();
        Alert_ingresar_a_la_cuenta.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        Alert_ingresar_a_la_cuenta.show();
    }

    static void method_puntuar_pedido(final Activity activity, final String input, String str_usuario) {

        if (input != null) {
            String[] datos_input = input.split("€");
            String[] datos = datos_input[0].split(",");
            if (datos.length == 4) {

                String str_anio = datos[0];
                String str_mes = datos[1];
                String str_dia = datos[2];
                String n_random = datos[3];

                final DatabaseReference buscar_pedido = FirebaseDatabase.getInstance().getReference().child("pedidos").child(str_anio).child(str_mes).child(str_dia).child(n_random);
                buscar_pedido.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.hasChild("puntuacion")) {

                            alertdialog_doble_accion(activity, "Desea Puntuar su ultimo pedido?", "Nos ayuda a mejorar el servicio y a servirle de la mejor manera posible", "Puntuar", "No puntuar", "metodos", null, false);

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        }
    }

    static void main_cambiar_fragment(Activity activity, String fragment) {

        FragmentTransaction tx = ((MainActivity) activity).getSupportFragmentManager().beginTransaction();

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
        if (fragment.equals("c_puntuar")) {
            tx.replace(R.id.content_main, new c_puntuar()).addToBackStack("c_ingreso_a_la_app");
        }
        if (fragment.equals("c_guardar_pedidos_ver_pedidos")) {
            tx.replace(R.id.content_main, new c_guardar_pedidos_ver_pedidos()).addToBackStack("c_guardar_pedidos_ver_pedidos");
        }
        if (fragment.equals("c_elegir_delivery_confirmacion")) {
            tx.replace(R.id.content_main, new c_elegir_delivery_confirmacion()).addToBackStack("c_elegir_delivery_confirmacion");
        }
        if (fragment.equals("c_lista_productos_negocios")) {
            tx.replace(R.id.content_main, new c_lista_productos_negocios()).addToBackStack("c_lista_productos_negocios");
        }
        if (fragment.equals("c_lista_categorias_productos_negocios")) {
            tx.replace(R.id.content_main, new c_lista_categorias_productos_negocios()).addToBackStack("c_lista_categorias_productos_negocios");
        }
        if (fragment.equals("c_lista_categorias_negocios")) {
            tx.replace(R.id.content_main, new c_lista_categorias_negocios()).addToBackStack("c_lista_categorias_negocios");
        }
        if (fragment.equals("c_ver_negocios_sin_coneccion")) {
            tx.replace(R.id.content_main, new c_ver_negocios_sin_coneccion()).addToBackStack("c_ver_negocios_sin_coneccion");
        }
        if (fragment.equals("c_productos_detalles")) {
            tx.replace(R.id.content_main, new c_productos_detalles()).addToBackStack("c_productos_detalles");
        }
        if (fragment.equals("c_categoria_productos_detalles")) {
            tx.replace(R.id.content_main, new c_categoria_productos_detalles()).addToBackStack("c_categoria_productos_detalles");
        }
        tx.commit();
    }

    static void alerdialog_pedir_permisos(final Activity activity, final int int_que_permiso_preguntar) {

        //int 0 = almacenamiento
        //1 = telefono
        //2 = ubicacion gps - boton busqueda
        //3 = ubicacion gps - mostrar en el mapa
        //4 = grabar voz

        AlertDialog.Builder alerta = new AlertDialog.Builder(
                activity);

        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View ad_permisos_view = inflater.inflate(R.layout.ad_basico_titulo_texto_botones, null);

        TextView tv_titulo = ad_permisos_view.findViewById(R.id.ad_tv_titulo);
        TextView tv_mensaje = ad_permisos_view.findViewById(R.id.ad_tv_mensaje);
        Button btn_aceptar = ad_permisos_view.findViewById(R.id.btn_aceptar);
        Button btn_cancelar = ad_permisos_view.findViewById(R.id.btn_cancelar);


        alerta.setView(ad_permisos_view);
        final AlertDialog ad_permisos = alerta.create();
        ad_permisos.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        if (int_que_permiso_preguntar == 0) {
            tv_titulo.setText("Se necesita guardar los mapas");
            tv_mensaje.setText("Para poder mostrar los mapas se necesita guardarlos y leerlos desde el telefono");
        }
        if (int_que_permiso_preguntar == 1) {
            tv_titulo.setText("Se necesita hacer una llamada");
            tv_mensaje.setText("Se necesita aceder al sistema de marcado para realizar una llamada");
        }
        if (int_que_permiso_preguntar == 2) {
            tv_titulo.setText("Se necesita saber la ubicacion");
            tv_mensaje.setText("Para esta accion se necesita la ubicacion GPS");
        }
        if (int_que_permiso_preguntar == 3) {
            tv_titulo.setText("Se necesita saber la ubicacion");
            tv_mensaje.setText("Para mostrar su ubicacion en el mapa se necesita la ubicacion GPS");
        }
        if (int_que_permiso_preguntar == 4) {
            tv_titulo.setText("Se necesita escuchar el audio");
            tv_mensaje.setText("Para hacer una busqueda por voz se necesita acceder al microfono");
        }

        btn_aceptar.setText("Permitir");

        btn_aceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (int_que_permiso_preguntar == 0) {
                    ad_permisos.dismiss();
                    ActivityCompat.requestPermissions(activity, new String[]{WRITE_EXTERNAL_STORAGE}, 1);
                }
                if (int_que_permiso_preguntar == 1) {
                    ad_permisos.dismiss();
                    ActivityCompat.requestPermissions(activity, new String[]{CALL_PHONE}, 1);
                }
                if (int_que_permiso_preguntar == 2) {
                    ad_permisos.dismiss();
                    ActivityCompat.requestPermissions(activity, new String[]{ACCESS_FINE_LOCATION}, 1);
                }
                if (int_que_permiso_preguntar == 3) {
                    ad_permisos.dismiss();
                    ActivityCompat.requestPermissions(activity, new String[]{ACCESS_FINE_LOCATION}, 1);
                }
                if (int_que_permiso_preguntar == 4) {
                    ad_permisos.dismiss();
                    ActivityCompat.requestPermissions(activity, new String[]{RECORD_AUDIO}, 1);
                }
            }
        });

        btn_cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ad_permisos.dismiss();
            }
        });


        ad_permisos.show();
    }

    private static void llamar(Activity activity) {
        if (ActivityCompat.checkSelfPermission(activity.getApplicationContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            metodos.alerdialog_pedir_permisos(activity, 1);
            Toast.makeText(activity, "Llamada no realizada", Toast.LENGTH_SHORT).show();
        } else {
            String telefono_a_llamar_2 = activity.getString(R.string.telefono_servicio_delivery);
            Intent callintent = new Intent(Intent.ACTION_CALL);
            String l = "tel:" + telefono_a_llamar_2;
            callintent.setData(Uri.parse(l));
            activity.startActivity(callintent);
        }
    }
}

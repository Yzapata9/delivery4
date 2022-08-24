package com.venta_productos.delivery.servicio;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.venta_productos.delivery.MainActivity;
import com.venta_productos.delivery.R;

import java.util.Arrays;
import java.util.Calendar;

public class servicio_del_pedido_activo extends IntentService {
    public servicio_del_pedido_activo() {
        super("Servicio del pedido activo");
    }

    String TAG = "asdf";
    SharedPreferences sharpref;
    Context contex;

    String datos_para_verificar, str_get_anio, str_get_mes, str_get_dia;
    String str_anio, str_mes, str_dia;

    Boolean boolean_frenar_servicio = false;
    static Boolean servicio_activo = false;

    DatabaseReference db_buscar_pedido, db_no_confirmado, db_revisar_chat;

    String estado_pedido = "confirmar pedido";

    String[] datos;

    boolean solo_sonar_una_vez_pedido_confirmado = false;
    boolean solo_sonar_una_vez_pedido_finalizado = false;
    boolean solo_sonar_una_vez_pedido_cancelado = false;

    String negocios_involucrados = null;

    boolean boolean_pedido_en_actividad=false;
    int int_espera_para_la_confirmacion_del_pedido = 0;

    int int_espera_para_chat_del_pedido = 0;
    Integer[] array_espera = {0, 6, 12, 18, 24, 30, 36, 42, 48, 54, 60}; // suena hasta los 5 minutos, cada 30 segs

    @Override
    public void onCreate() {
        super.onCreate();


        contex = this;
        sharpref = getSharedPreferences("usar_app", MODE_PRIVATE);

        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int dayofmonth = cal.get(Calendar.DAY_OF_MONTH);
        int month = cal.get(Calendar.MONTH);

        str_anio = String.valueOf(year);
        str_mes = String.valueOf(month + 1);
        str_dia = String.valueOf(dayofmonth);

        datos_para_verificar = sharpref.getString("verificar_pedidos_activos", null);
        if (datos_para_verificar != null) {
            datos = datos_para_verificar.split(",");
            String[] getdate = datos[0].split("-");
            str_get_anio = getdate[0];
            str_get_mes = getdate[1];
            str_get_dia = getdate[2];

            if (!str_anio.equals(str_get_anio) | !str_mes.equals(str_get_mes) | !str_dia.equals(str_get_dia)) {
                boolean_frenar_servicio = true;
            } else {
                crear_notificacion("esperando confirmacion (5 min max)", "", 0);
            }
        } else {
            boolean_frenar_servicio = true;
        }

    }

    @Override
    public void onHandleIntent(@Nullable Intent intent) {

        for (int i = 0; i < 8640; i++) {

            if (i == 9) { // loop infinito
                i = 0;
            }
            servicio_activo = true;

            if (datos.length > 2) {  //method_verificar_si_es_multiple_delivery();
                for (int n_veces_random = 1; n_veces_random < datos.length; n_veces_random++) { //si tiene + de un pedido=
                    method_buscar_estado_en_fire_base(datos[n_veces_random]);
                }
            } else {
                method_buscar_estado_en_fire_base(datos[1]);
            }

            revisar_mensajes(datos[1]);

            method_listener_para_confirmar_el_pedido();

            try {
                Thread.sleep(5000); //cada cuanto se vuelve a ejecutar el codigo;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (boolean_frenar_servicio) {
                break;
            }
        }
    }

    private void method_listener_para_confirmar_el_pedido() {
        if (!boolean_pedido_en_actividad) { //listener para confirmar el pedido
            int_espera_para_la_confirmacion_del_pedido++;
            if (int_espera_para_la_confirmacion_del_pedido == 24) {  // 2 minutos
                method_pedido_no_confirmado(datos[1], true);
            }
            if (int_espera_para_la_confirmacion_del_pedido == 60) { // 5 minutos
                method_pedido_no_confirmado(datos[1], false);
            }
        }
    }

    private void revisar_mensajes(final String n_random) {

        // TODO probar si funciona
        db_revisar_chat = FirebaseDatabase.getInstance().getReference().child("pedidos").child(str_anio).child(str_mes).child(str_dia).child(n_random);
        db_revisar_chat.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("chat")) {
                    if (Arrays.asList(array_espera).contains(int_espera_para_chat_del_pedido)) {
                        Log.d(TAG, "chat activo: " + int_espera_para_chat_del_pedido);
                        String str_mensaje = (String) dataSnapshot.child("chat").child("cliente_delivery").child("cliente").getValue(); // cliente-delivery,  cliente-negocio
                        crear_notificacion(str_mensaje,n_random, 5);
                    }
                    int_espera_para_chat_del_pedido++;

                    if (int_espera_para_chat_del_pedido>30) {

                        String cliente = (String) dataSnapshot.child("cliente").getValue();
                        String[] dividir_datos= cliente.split(",");
                        String numero = dividir_datos[0];

                        DatabaseReference dejar_mensaje= FirebaseDatabase.getInstance().getReference().child("pedidos").child(str_anio).child(str_mes).child(str_dia).child(n_random)
                                .child("chat").child("cliente_delivery").child("delivery");
                        dejar_mensaje.setValue(numero);
                    }

                    //de este loop salgo borrando el child chat
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void method_pedido_no_confirmado(final String n_random, boolean advertencia_o_finalizar) {

        //TODO que me mande una notificacion o me llame o algo a la cuenta administrador

        if (advertencia_o_finalizar) {
            db_no_confirmado = FirebaseDatabase.getInstance().getReference().child("no_confirmado").child("advertencia").child(str_anio).child(str_mes).child(str_dia).child(n_random);
            db_no_confirmado.setValue("advertencia");
        } else {
            db_no_confirmado = FirebaseDatabase.getInstance().getReference().child("no_confirmado").child("pedido_cancelado").child(str_anio).child(str_mes).child(str_dia).child(n_random);
            db_no_confirmado.setValue("pedido no confirmado");
            crear_notificacion("pedido sin confirmacion", null, 4);
            boolean_frenar_servicio = true;
        }
    }

    public void method_buscar_estado_en_fire_base(final String n_random) {
        db_buscar_pedido = FirebaseDatabase.getInstance().getReference().child("pedidos").child(str_anio).child(str_mes).child(str_dia).child(n_random);
        db_buscar_pedido.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                estado_pedido = (String) dataSnapshot.child("pedido_en_actividad").getValue();
                method_checkear_estado(estado_pedido, n_random);

                if (negocios_involucrados == null) {
                    for (DataSnapshot snap : dataSnapshot.child("negocios").getChildren()) {
                        String key = snap.getKey();
                        if (negocios_involucrados == null) {
                            negocios_involucrados = "delivery," + key;
                        } else {
                            negocios_involucrados = negocios_involucrados + "," + key;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void method_checkear_estado(String estado_pedido, String n_random) {

        if (estado_pedido.equals("confirmar pedido")) {
            //no pasa nada pero tampoco quiero que me frene el servicio llegando al else
        } else if (estado_pedido.equals("confirmado")) {
            if (!solo_sonar_una_vez_pedido_confirmado) { //que solo suene una vez
                boolean_pedido_en_actividad = true;
                crear_notificacion("Pedido Confirmado", n_random, 1);
                solo_sonar_una_vez_pedido_confirmado = true;
            }
        } else if (estado_pedido.equals("finalizado")) {
            if (!solo_sonar_una_vez_pedido_finalizado) {
                crear_notificacion("Pedido Finalizado, precione aqui para puntuar", n_random, 2);
                boolean_frenar_servicio = true;
                solo_sonar_una_vez_pedido_finalizado = true;
            }
        } else {
            if (!solo_sonar_una_vez_pedido_cancelado) {
                boolean_frenar_servicio = true;
                estado_pedido = estado_pedido.replace(",", " motivo= ");
                crear_notificacion("Pedido " + estado_pedido, n_random, 3);
                solo_sonar_una_vez_pedido_cancelado = true;
            }
        }
    }

    public void crear_notificacion(String str_estado_pedido, String n_random, int int_que_accion_realizar) {

        //0 - confirmar pedido
        //1 - confirmado
        //2 - finalizado, puntar
        //3 - finalizado cancelado
        //4 - no confirmado
        //5 - mensaje

//        Uri sound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE+"://" + contex.getPackageName() + "/" +R.raw.ring_tone_xd);
        // Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder nb = new NotificationCompat.Builder(this, "servicio_delivery");
        nb.setContentText(str_estado_pedido);
        nb.setContentTitle("Estado del pedido");
        nb.setSmallIcon(R.drawable.icono_principal_recortado);
        nb.setColor(Color.BLUE);
        //nb.setSound(sound);

        if (Build.VERSION.SDK_INT >= 26) {
            String CHANNEL_ID = "my_channel_01";// The id of the channel.
            nb.setChannelId(CHANNEL_ID);
        }

        String guardar_en_el_intent = null;

        if (int_que_accion_realizar == 1 | int_que_accion_realizar == 0) {
            guardar_en_el_intent = "c_guardar_pedidos_ver_pedidos";
        }
        if (int_que_accion_realizar == 2) {
            guardar_en_el_intent = str_anio + "," + str_mes + "," + str_dia + "," + n_random + "€" + negocios_involucrados;
            SharedPreferences.Editor editor = sharpref.edit();
            editor.putString("puntuar_pedido", guardar_en_el_intent);
            editor.apply();
        }

        Intent notificationIntent = new Intent(contex, MainActivity.class);
        notificationIntent.putExtra("puntuar_pedido", guardar_en_el_intent);
        PendingIntent contentIntent = PendingIntent.getActivity(contex, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        nb.setContentIntent(contentIntent);
        nb.setAutoCancel(true);

        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.notify(1, nb.build());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        boolean_frenar_servicio = true;
        servicio_activo = false;
    }

    public static boolean estado_servicio() {
        return servicio_activo;
    }

}

package com.venta_productos.delivery;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;

public class metodos_busqueda {

    static private String TAG = "asdf";

    public static void realizar_busqueda(final Activity activity, final String ingresar_texto) {

        buscar_productos_en_la_bd(new FCB_buscar_productos() {
            @Override
            public void onCallBack(HashMap<String, String> hash_ubicaciones) {
                if (hash_ubicaciones != null) {
                    if (hash_ubicaciones.size() > 0) {
                        if (ingresar_texto == null) {
                            comparar_con_la_voz(activity);
                        } else {
                            String remover_valores = removertildes(ingresar_texto);
                            comparar_con_texto(activity, remover_valores.toLowerCase());
                        }
                    } else {
                        Toast.makeText(activity, "error buscando productos en la base de datos", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(activity, "error buscando productos en la base de datos", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private static void comparar_con_texto(Activity activity, String input_texto) {
        HashMap<String, String> hash_resultados_que_coinciden = new HashMap<>();

        for (String key : hash_productos_ubicacion.keySet()) {
            if (key.contains(input_texto)) {
                hash_resultados_que_coinciden.put(key, hash_productos_ubicacion.get(key));
            }
        }
        if (hash_resultados_que_coinciden.size() == 0) {
            Toast.makeText(activity, "Sin resultados para " + input_texto, Toast.LENGTH_SHORT).show();
        } else if (hash_resultados_que_coinciden.size() == 1) {
            //cambiar al al fragment donde este el resultado
            String input = String.valueOf(hash_resultados_que_coinciden);
            String mejorar = input.substring(1, input.length() - 1);
            method_buscar_el_producto_que_coincidio(activity, mejorar);
        } else {
            ArrayList<String> array_productos_que_coinciden_datos = new ArrayList<>();
            ArrayList<String> array_productos_que_coinciden_texto = new ArrayList<>();
            for (String nombre : hash_resultados_que_coinciden.keySet()) {
                String[] valor = hash_resultados_que_coinciden.get(nombre).split("€");

                switch (valor[0]) {
                    case "negocio_con_categorias":
                        array_productos_que_coinciden_texto.add(nombre + ", es un negocio" );
                        break;
                    case "negocio_sin_categorias":
                        array_productos_que_coinciden_texto.add(nombre + ", es un negocio");
                        break;
                    case "categoria":
                        array_productos_que_coinciden_texto.add(nombre + ", es una categoria de un negocio");
                        break;
                    case "categoria_producto":
                        array_productos_que_coinciden_texto.add(nombre + ", es un producto");
                        break;
                    case "producto":
                        array_productos_que_coinciden_texto.add(nombre + ", es un producto");
                        break;
                }

                array_productos_que_coinciden_datos.add(nombre + ", es un=" + valor[0]);
            }
            String[] pasar_datos_al_adapter_datos = array_productos_que_coinciden_datos.toArray(new String[0]);
            String[] pasar_datos_al_adapter_texto = array_productos_que_coinciden_texto.toArray(new String[0]);

            // muestra un listview, con los posibles resultados
            alertdialog_listview(activity, pasar_datos_al_adapter_datos, pasar_datos_al_adapter_texto);

        }

    }

    private static String removertildes(String string) {
        return Normalizer.normalize(string, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }

    public static SpeechRecognizer recognizer;

    private static void comparar_con_la_voz(final Activity activity) {
        metodos.alerdialog_descargando_informacion(activity, true, "Escuchando");

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                "com.domain.app");

        recognizer = SpeechRecognizer.createSpeechRecognizer(activity.getApplicationContext());

        RecognitionListener listener = new RecognitionListener() {
            @Override
            public void onResults(Bundle results) {
                metodos.alerdialog_descargando_informacion(activity, false, null);

                ArrayList<String> voiceResults = results
                        .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (voiceResults == null) {
                    System.out.println("No voice results");
                } else {
                    System.out.println("Printing matches: ");
                    HashMap<String, String> hash_resultados_que_coinciden = new HashMap<>();
                    for (String match : voiceResults) {
                        System.out.println(match);

                        for (String key : hash_productos_ubicacion.keySet()) {
                            if (key.contains(match)) {
                                hash_resultados_que_coinciden.put(key, hash_productos_ubicacion.get(key));
                            }
                        }

                       /* if (hash_productos_ubicacion.containsKey(match)) {
                            Log.d(TAG, "ejecutado ");
                            hash_resultados_que_coinciden.put(match, hash_productos_ubicacion.get(match));
                        }*/
                    }
                    if (hash_resultados_que_coinciden.size() == 0) {
                        Toast.makeText(activity, "Sin resultados para " + voiceResults, Toast.LENGTH_SHORT).show();
                    } else if (hash_resultados_que_coinciden.size() == 1) {
                        //cambiar al al fragment donde este el resultado
                        String input = String.valueOf(hash_resultados_que_coinciden);
                        String mejorar = input.substring(1, input.length() - 1);
                        method_buscar_el_producto_que_coincidio(activity, mejorar);
                    } else {
                        ArrayList<String> array_productos_que_coinciden_datos = new ArrayList<>();
                        ArrayList<String> array_productos_que_coinciden_texto = new ArrayList<>();

                        for (String nombre : hash_resultados_que_coinciden.keySet()) {
                            String str_nombre = nombre;
                            String[] valor = hash_resultados_que_coinciden.get(nombre).split("€");

                            switch (valor[0]) {
                                case "negocio_con_categorias":
                                    array_productos_que_coinciden_texto.add(nombre + ", es un negocio" );
                                    break;
                                case "negocio_sin_categorias":
                                    array_productos_que_coinciden_texto.add(nombre + ", es un negocio");
                                    break;
                                case "categoria":
                                    array_productos_que_coinciden_texto.add(nombre + ", es una categoria de un negocio");
                                    break;
                                case "categoria_producto":
                                    array_productos_que_coinciden_texto.add(nombre + ", es un producto");
                                    break;
                                case "producto":
                                    array_productos_que_coinciden_texto.add(nombre + ", es un producto");
                                    break;
                            }

                            array_productos_que_coinciden_datos.add(str_nombre + ", es un=" + valor[0]);
                        }
                        String[] pasar_datos_al_adapter = array_productos_que_coinciden_datos.toArray(new String[0]);
                        String[] pasar_datos_al_adapter_texto = array_productos_que_coinciden_texto.toArray(new String[0]);

                        // muestra un listview, con los posibles resultados
                        alertdialog_listview(activity, pasar_datos_al_adapter,pasar_datos_al_adapter_texto);

                    }
                }
            }

            @Override
            public void onReadyForSpeech(Bundle params) {
                System.out.println("Ready for speech");
            }

            @Override
            public void onError(int error) {
                String str_error = "error desconocido";
                if (error == 1) {
                    str_error = "ERROR_NETWORK_TIMEOUT = 1";
                }
                if (error == 2) {
                    str_error = "ERROR_NETWORK = 2";
                }
                if (error == 3) {
                    str_error = "ERROR_AUDIO = 3";
                }
                if (error == 4) {
                    str_error = "ERROR_SERVER = 4";
                }
                if (error == 5) {
                    str_error = "ERROR_CLIENT = 5";
                }
                if (error == 6) {
                    str_error = "ERROR_SPEECH_TIMEOUT = 6";
                }
                if (error == 7) {
                    str_error = "ERROR_NO_MATCH = 7";
                }
                if (error == 8) {
                    str_error = "ERROR_RECOGNIZER_BUSY = 8";
                }
                if (error == 9) {
                    str_error = "ERROR_INSUFFICIENT_PERMISSIONS = 9";
                }

                System.err.println("Error listening for speech: " + str_error);
            }

            @Override
            public void onBeginningOfSpeech() {
                System.out.println("Speech starting");
            }

            @Override
            public void onBufferReceived(byte[] buffer) {
                // TODaO Auto-generated method stub

            }

            @Override
            public void onEndOfSpeech() {
                // TODaO Auto-generated method stub

            }

            @Override
            public void onEvent(int eventType, Bundle params) {
                // TODaO Auto-generated method stub

            }

            @Override
            public void onPartialResults(Bundle partialResults) {
                // TODaO Auto-generated method stub

            }

            @Override
            public void onRmsChanged(float rmsdB) {
                // TODaO Auto-generated method stub

            }
        };
        recognizer.setRecognitionListener(listener);
        recognizer.startListening(intent);
    }

    private static void method_buscar_el_producto_que_coincidio(Activity activity, String input) {
        Log.d(TAG, "method_buscar_el_producto_que_coincidio: " + input);
        String[] dividir_input_1 = input.split("=");
        String[] dividir_input_2 = dividir_input_1[1].split("€");

        String nombre = dividir_input_1[0]; //nombre producto
        String que_es = dividir_input_2[0]; // que es negocio, categoria, producto
        //String key_negocio = dividir_input_2[1]; key negocio
        //String key_categoria_o_producto = dividir_input_2[2]; key categoria o del negocio
        //String key_producto_de_la_categoria = dividir_input_2[3]; key del producto de la categoria

        if (que_es.equals("negocio_sin_categorias")) {
            metodos.pasar_datos_productos_negocio = dividir_input_2[1];
            metodos.main_cambiar_fragment(activity, "c_lista_productos_negocios");
        }
        if (que_es.equals("negocio_con_categorias")) {
            metodos.pasar_datos_del_negocio_al_fragment_categorias = dividir_input_2[1];
            metodos.main_cambiar_fragment(activity, "c_lista_categorias_negocios");
        }
        if (que_es.equals("producto")) {
            metodos.pasar_datos_productos_negocio = dividir_input_2[1]; // negocio
            metodos.pasar_producto_seleccionado = dividir_input_2[2] + "·" + nombre; // key y nombre
            metodos.main_cambiar_fragment(activity, "c_productos_detalles");
        }
        if (que_es.equals("categoria")) {
            metodos.pasar_datos_categorias_negocios = dividir_input_2[1] + "," + dividir_input_2[2];
            metodos.main_cambiar_fragment(activity, "c_lista_categorias_productos_negocios");
        }
        if (que_es.equals("categoria_producto")) {
            metodos.pasar_categoria_producto_seleccionado = dividir_input_2[3] + "·" + nombre; // key y nombre
            metodos.pasar_datos_categorias_negocios = dividir_input_2[1] + "," + dividir_input_2[2];
            metodos.main_cambiar_fragment(activity, "c_categoria_productos_detalles");
        }

    }

    private static HashMap<String, String> hash_productos_ubicacion = new HashMap<>();

    private static void buscar_productos_en_la_bd(final FCB_buscar_productos fcb_buscar_productos) {
        DatabaseReference db_lista = FirebaseDatabase.getInstance().getReference().child("negocios");

        db_lista.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snap_root_negocios) {
                for (DataSnapshot snap_negocios : snap_root_negocios.getChildren()) {
                    String nombre_negocio = (String) snap_negocios.child("nombre").getValue();
                    String key_negocio = snap_negocios.getKey();
                    String rubro = (String) snap_negocios.child("rubro").getValue();
                    String tiene_categoria = (String) snap_negocios.child("tiene_categorias").getValue();

                    if (tiene_categoria != null) {
                        if (tiene_categoria.equals("si")) {
                            String negocio_ubicacion = "negocio_con_categorias€" + key_negocio;

                            hash_productos_ubicacion.put(removertildes(nombre_negocio.toLowerCase()), negocio_ubicacion);
                            hash_productos_ubicacion.put(removertildes(rubro.toLowerCase()), negocio_ubicacion);

                            for (DataSnapshot snap_categorias : snap_negocios.child("categorias").getChildren()) {
                                String nombre_categoria = (String) snap_categorias.child("nombre").getValue();
                                String key_categoria = snap_categorias.getKey();
                                String categoria_ubicacion = "categoria€" + key_negocio + "€" + key_categoria;
                                hash_productos_ubicacion.put(removertildes(nombre_categoria.toLowerCase()), categoria_ubicacion);
                                for (DataSnapshot snap_categorias_productos : snap_categorias.child("productos").getChildren()) {
                                    String nombre_categoria_categoria = (String) snap_categorias_productos.child("nombre").getValue();
                                    String key_categoria_categoria = snap_categorias_productos.getKey();
                                    String categoria_ubicacion_categoria = "categoria_producto€" + key_negocio + "€" + key_categoria + "€" + key_categoria_categoria;
                                    hash_productos_ubicacion.put(removertildes(nombre_categoria_categoria.toLowerCase()), categoria_ubicacion_categoria);
                                }
                            }
                        } else {
                            String negocio_ubicacion = "negocio_sin_categorias€" + key_negocio;

                            hash_productos_ubicacion.put(removertildes(nombre_negocio.toLowerCase()), negocio_ubicacion);
                            hash_productos_ubicacion.put(removertildes(rubro.toLowerCase()), negocio_ubicacion);

                            for (DataSnapshot snap_productos : snap_negocios.child("productos").getChildren()) {
                                String nombre_producto = (String) snap_productos.child("nombre").getValue();
                                String key_producto = snap_productos.getKey();
                                String ubicacion_producto = "producto€" + key_negocio + "€" + key_producto;
                                hash_productos_ubicacion.put(removertildes(nombre_producto.toLowerCase()), ubicacion_producto);
                            }
                        }
                    } else {
                        String negocio_ubicacion = "negocio_sin_categorias€" + key_negocio;

                        hash_productos_ubicacion.put(removertildes(nombre_negocio.toLowerCase()), negocio_ubicacion);
                        hash_productos_ubicacion.put(removertildes(rubro.toLowerCase()), negocio_ubicacion);

                        for (DataSnapshot snap_productos : snap_negocios.child("productos").getChildren()) {
                            String nombre_producto = (String) snap_productos.child("nombre").getValue();
                            String key_producto = snap_productos.getKey();
                            String ubicacion_producto = "producto€" + key_negocio + "€" + key_producto;
                            hash_productos_ubicacion.put(removertildes(nombre_producto.toLowerCase()), ubicacion_producto);
                        }
                    }
                }
                Log.d(TAG, "busqueda resultado: " + hash_productos_ubicacion);
                fcb_buscar_productos.onCallBack(hash_productos_ubicacion);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private static AlertDialog alertDialog_listview;

    private static void alertdialog_listview(final Activity activity, final String[] array_con_datos, final String[] array_texto_corregido) {

        // 0 = mostrar cual ubicacion desea elegir
        // 1 = sin deliverys nunca para la ubicacion actual
        // 2 = sin deliverys actualmente para las ubicaciones

        final AlertDialog.Builder alerta = new AlertDialog.Builder(activity);
        final LayoutInflater inflater = activity.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.ad_listview, null);
        alerta.setView(dialogView);
        alertDialog_listview = alerta.create();
        alertDialog_listview.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView tv_titulo = dialogView.findViewById(R.id.ad_tv_titulo);
        ListView ad_listview = dialogView.findViewById(R.id.ad_listview);
        Button btn_aceptar = dialogView.findViewById(R.id.btn_aceptar);
        Button btn_cancelar = dialogView.findViewById(R.id.btn_cancelar);

        btn_aceptar.setText("Cerrar");
        btn_cancelar.setVisibility(View.GONE);

        tv_titulo.setText("se encotraron varios articulos, cual desea ver");

        ArrayAdapter<String> adapter
                = new ArrayAdapter<>(activity,
                R.layout.ad_row_texto_estilo, //en que layout se encuentra el texto
                R.id.texto_spinner, // como es el id del texto
                array_texto_corregido); //valor que tiene que mostrar
        ad_listview.setAdapter(adapter);

        ad_listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                alertDialog_listview.dismiss();
                String[] key = array_con_datos[position].split(",");
                String nombre = key[0];
                String valor = hash_productos_ubicacion.get(nombre);
                String juntar = nombre + "=" + valor;

                method_buscar_el_producto_que_coincidio(activity, juntar);

            }
        });

        btn_aceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog_listview.dismiss();
            }
        });

        alertDialog_listview.show();
    }


    private interface FCB_buscar_productos {
        void onCallBack(HashMap<String, String> hash_ubicaciones);
    }
}

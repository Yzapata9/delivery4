package com.venta_productos.delivery;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;


/**
 * A simple {@link Fragment} subclass.
 */
public class c_puntuar extends Fragment {


    public c_puntuar() {
        // Required empty public constructor
    }

    Context contex;
    SharedPreferences sharpref;
    String TAG = "asdf";
    EditText ed_comentario;
    TableLayout tableLayout;
    Button btn_puntuar, btn_volver;
    String str_usuario, input_pedido;
    Drawable manito_arriba, manito_abajo, background_row;
    HashMap<String, String> hash_puntuacion = new HashMap<>();
    String str_anio, str_mes, str_dia, n_random;
    String[] datos_usuario;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View Puntuar = inflater.inflate(R.layout.f_puntuar, container, false);

        tableLayout = Puntuar.findViewById(R.id.layout_tablerow);
        btn_puntuar = Puntuar.findViewById(R.id.ad_puntuar_btn_puntuar);
        btn_volver = Puntuar.findViewById(R.id.ad_puntuar_btn_volver);
        ed_comentario = Puntuar.findViewById(R.id.puntar_dejar_un_comentario);

        manito_arriba = getResources().getDrawable(R.drawable.ic_puntuar_manito_arriba);
        manito_abajo = getResources().getDrawable(R.drawable.ic__puntuar_manito_abajo);
        background_row = getResources().getDrawable(R.drawable.input_outline_transparente);

        contex = getActivity();
        sharpref = getContext().getSharedPreferences("usar_app", Context.MODE_PRIVATE);

        str_usuario = sharpref.getString("usuario", "no hay dato");
        input_pedido = sharpref.getString("puntuar_pedido", null);

        String[] array__buscar_negocios = input_pedido.split("€");
        String[] array_input_pedido = array__buscar_negocios[0].split(",");
        String[] negocios_involucrados = array__buscar_negocios[1].split(",");

        datos_usuario = str_usuario.split(",");
        str_anio = array_input_pedido[0];
        str_mes = array_input_pedido[1];
        str_dia = array_input_pedido[2];
        n_random = array_input_pedido[3];


        TableLayout.LayoutParams params_del_tablelayout = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT, 1.0f);
        TableRow.LayoutParams params_del_row = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT, 1.0f);
        LinearLayout.LayoutParams params_del_linear = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f);
        TableLayout.LayoutParams params_del_linear_para_el_relative_2 = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, 2);

        for (final String negocio : negocios_involucrados) {

            params_del_tablelayout.setMargins(5, 5, 5, 5);
            final TableRow tablerow_agregar_negocio = new TableRow(getActivity());
            tablerow_agregar_negocio.setLayoutParams(params_del_tablelayout);
            tablerow_agregar_negocio.setBackground(background_row);

            LinearLayout linear_mayor = new LinearLayout(getActivity());
            linear_mayor.setLayoutParams(params_del_row);
            linear_mayor.setOrientation(LinearLayout.VERTICAL);

            LinearLayout linear_imagenes = new LinearLayout(getActivity());
            linear_imagenes.setLayoutParams(params_del_linear);
            linear_imagenes.setWeightSum(2);
            linear_imagenes.setOrientation(LinearLayout.HORIZONTAL);

           /* RelativeLayout linear_separar_2 = new RelativeLayout(getActivity());
            linear_separar_2.setLayoutParams(params_del_linear_para_el_relative_2);
            linear_separar_2.setBackgroundResource(R.color.negro);*/

            TextView tv_negocios_titulo = new TextView(getActivity());
            tv_negocios_titulo.setLayoutParams(params_del_linear);
            tv_negocios_titulo.setGravity(Gravity.CENTER);
            tv_negocios_titulo.setText(negocio);
            tv_negocios_titulo.setTextAppearance(getActivity(), R.style.texview_inflado_al_tablelayout);
            tv_negocios_titulo.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            tv_negocios_titulo.setPadding(0, 3, 0, 3);
/*
            EditText ed_comentario = new EditText(getActivity());
            ed_comentario.setLayoutParams(params_del_linear);
            ed_comentario.setGravity(Gravity.CENTER);
            ed_comentario.setHint("dejar un comentario");*/

            final ImageView image_positiva = new ImageView(getActivity());
            image_positiva.setLayoutParams(params_del_linear);
            image_positiva.setImageDrawable(manito_arriba);


            final ImageView image_negativa = new ImageView(getActivity());
            image_negativa.setLayoutParams(params_del_linear);
            image_negativa.setImageDrawable(manito_abajo);

            image_positiva.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    image_negativa.setScaleX(0.5f);
                    image_negativa.setScaleY(0.5f);
                    image_positiva.setScaleX(1.25f);
                    image_positiva.setScaleY(1.25f);
                    hash_puntuacion.put(negocio, "positivo");
                }
            });

            image_negativa.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    image_positiva.setScaleX(0.5f);
                    image_positiva.setScaleY(0.5f);
                    image_negativa.setScaleX(1.25f);
                    image_negativa.setScaleY(1.25f);
                    hash_puntuacion.put(negocio, "negativo");
                }
            });

            linear_imagenes.addView(image_negativa);
            linear_imagenes.addView(image_positiva);
            linear_mayor.addView(tv_negocios_titulo);
            linear_mayor.addView(linear_imagenes);
//            linear_mayor.addView(ed_comentario);
            tablerow_agregar_negocio.addView(linear_mayor);
            tableLayout.addView(tablerow_agregar_negocio);
//            tableLayout.addView(linear_separar_2);

        }

        btn_volver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });

        btn_puntuar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if (hash_puntuacion.size() > 0 | ed_comentario.getText().toString().length() > 7) {

                    if (ed_comentario.getText().toString().length() > 7) {
                        boolean esta_bien_escrito = metodos.method_chekear_texto_de_los_edittext(getActivity(), ed_comentario.getText().toString());
                        if (esta_bien_escrito) {
                            method_guardar_puntuacion();
                        }
                    } else if (ed_comentario.getText().toString().length() == 0) {
                        method_guardar_puntuacion();
                    } else {
                        Toast.makeText(getActivity(), "el comentario ingresado es demaciado pequeño", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getActivity(), "Debe puntuar o comentar primero", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return Puntuar;
    }

    private void method_guardar_puntuacion() {

        final DatabaseReference buscar_pedido = FirebaseDatabase.getInstance().getReference().child("pedidos").child(str_anio).child(str_mes).child(str_dia).child(n_random).child("puntuacion");
        buscar_pedido.setValue("puntuado");

        if (hash_puntuacion.size() > 0) {
            final DatabaseReference buscar_negocio = FirebaseDatabase.getInstance().getReference().child("negocios");
            for (String negocios : hash_puntuacion.keySet()) {
                if (negocios.equals("delivery")) {
                    String valor_puntuado = hash_puntuacion.get(negocios);
                    if (valor_puntuado.equals("positivo")) {
                        final DatabaseReference puntuar_pedido = FirebaseDatabase.getInstance().getReference().child("feedback").child(str_anio).child(str_mes).child(str_dia).child("delivery").child("positivo").child(datos_usuario[1]).push();
                        puntuar_pedido.setValue("bien hecho!");
                    } else {
                        final DatabaseReference puntuar_pedido = FirebaseDatabase.getInstance().getReference().child("feedback").child(str_anio).child(str_mes).child(str_dia).child("delivery").child("negativo").child(datos_usuario[1]).push();
                        puntuar_pedido.setValue("algo anda mal");
                    }
                } else {
                    String valor_puntuado = hash_puntuacion.get(negocios);
                    if (valor_puntuado.equals("positivo")) {
                        DatabaseReference db_guardar = buscar_negocio.child(negocios).child("puntuacion").child("positivo").push();
                        db_guardar.setValue(datos_usuario[1]);
                    } else {
                        DatabaseReference db_guardar = buscar_negocio.child(negocios).child("puntuacion").child("negativo").push();
                        db_guardar.setValue(datos_usuario[1]);
                    }
                }
            }
        }
        if (ed_comentario.getText().toString().length() > 7) {
            final DatabaseReference puntuar_pedido = FirebaseDatabase.getInstance().getReference().child("feedback").child(str_anio).child(str_mes).child(str_dia).child("comentario").child(datos_usuario[1]).push();
            puntuar_pedido.setValue(ed_comentario.getText().toString());
        }

        Toast.makeText(getActivity(), "gracias por su tiempo", Toast.LENGTH_SHORT).show();

        SharedPreferences sharpref = getActivity().getSharedPreferences("usar_app", Context.MODE_PRIVATE);
        sharpref.edit().remove("puntuar_pedido").apply();

        final FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.content_main, new c_principal()).addToBackStack(toString());
        ft.commit();
    }

}

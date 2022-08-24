package com.venta_productos.delivery;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.venta_productos.delivery.adapter.adaptador_lista_productos_negocios;

import java.util.ArrayList;

import static android.view.View.GONE;


/**
 * A simple {@link Fragment} subclass.
 */
public class c_lista_categorias_productos_negocios extends Fragment {

    ArrayList<String> array_productos = new ArrayList<>();
    DatabaseReference db_productos_negocios;
    GridView gridview;
    String str_negocio,str_categoria;
    LinearLayout linear_botones_ubicacion;
    SwipeRefreshLayout swipe;

    String TAG = "asdf";

    Boolean se_cargo_la_categoria = false;

    public c_lista_categorias_productos_negocios() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View Lista_productos_negocios = inflater.inflate(R.layout.f_principal_mejorado, container, false);


        if (getActivity() != null) {
            ((MainActivity) getActivity()).settitletoolbar("Productos =");
            ((MainActivity) getActivity()).mostrar_fab(true);
        }
        swipe = Lista_productos_negocios.findViewById(R.id.swiper);
        linear_botones_ubicacion = Lista_productos_negocios.findViewById(R.id.linear_cambiar_direccion);
        linear_botones_ubicacion.setVisibility(GONE);
        gridview = Lista_productos_negocios.findViewById(R.id.lvv);

        String[] input_datos = metodos.pasar_datos_categorias_negocios.split(",");
        str_negocio = input_datos[0];
        str_categoria = input_datos[1];


        db_productos_negocios = FirebaseDatabase.getInstance().getReference().child("negocios").child(str_negocio).child("categorias").child(str_categoria);
        metodos.alerdialog_descargando_informacion(getActivity(), true, "Descargando Productos");

        method_buscar_productos_negocio(new FirebaseCallBack_pasar_al_adaptador() {
            @Override
            public void onCallBack(ArrayList<String> negocios_2) {
                llenar_grid_view(negocios_2);
            }
        });

        if (getActivity() != null) {
            ((MainActivity) getActivity()).chekear_internet();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                R.layout.row_spinner_text, R.id.texto_spinner, new String[]{"\n .: cargando negocios :. \n"});
        gridview.setAdapter(adapter);

        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                method_buscar_productos_negocio(new FirebaseCallBack_pasar_al_adaptador() {
                    @Override
                    public void onCallBack(ArrayList<String> negocios_2) {
                        llenar_grid_view(negocios_2);
                    }
                });
            }
        });

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (se_cargo_la_categoria) {
                    alert_dialog_producto(i);
                } else {
                    //no se cargaron datos todavia
                    Toast.makeText(getActivity(), "Cargando datos", Toast.LENGTH_LONG).show();
                }
            }
        });

        return Lista_productos_negocios;
    }

    private void method_buscar_productos_negocio(final FirebaseCallBack_pasar_al_adaptador firebaseCallBack_pasar_al_adaptador) {
        array_productos.clear();
        db_productos_negocios.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //lo guardo en el str_negocio
                se_cargo_la_categoria = true;

                for (DataSnapshot snapshot : dataSnapshot.child("productos").getChildren()) {
                    String key = snapshot.getKey();
                    String disponible = (String) snapshot.child("disponible").getValue();
                    String nombre = (String) snapshot.child("nombre").getValue();
                    String precio = (String) snapshot.child("precio").getValue();
                    String detalles = (String) snapshot.child("str_detalles").getValue();
                    String imagen = (String) snapshot.child("imagen").getValue();
                    String str_producto_por_cantidad = (String) snapshot.child("es_por_cantidad").getValue();

                    String unir_datos = key + "€" + nombre + "€" + precio + "€" + disponible + "€" + detalles +
                            "€" + imagen + "€" + str_producto_por_cantidad;

                    array_productos.add(unir_datos);
                }

                firebaseCallBack_pasar_al_adaptador.onCallBack(array_productos);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void llenar_grid_view(ArrayList<String> productos) {
        metodos.alerdialog_descargando_informacion(getActivity(), false, "");

        String[] pasar_datos_al_adapter = productos.toArray(new String[0]);
        final adaptador_lista_productos_negocios adapter_productos = new adaptador_lista_productos_negocios(getActivity(), pasar_datos_al_adapter);
        gridview.setAdapter(adapter_productos);

        if (swipe.isRefreshing()) {
            swipe.setRefreshing(false);
        }
    }

    private void alert_dialog_producto(final int position) {

        String[] datos_input = array_productos.get(position).split("€");

        final String str_key = datos_input[0];
        final String str_nombre = datos_input[1];
        final String str_precio = datos_input[2];
        String str_disponible = datos_input[3];
        final String str_detalles = datos_input[4];
        String str_imagen = datos_input[5];
        final String str_por_cantidad = datos_input[6];

        final AlertDialog.Builder alerta = new AlertDialog.Builder(getActivity());
        final LayoutInflater inflater = getActivity().getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.ad_agregar_producto_por_unidad, null);
        alerta.setView(dialogView);
        final AlertDialog alertDialog = alerta.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView nombre_producto = dialogView.findViewById(R.id.TV_alertdialog_agregar_producto_nombre);
        TextView precio_producto = dialogView.findViewById(R.id.TV_alertdialog_agregar_producto_precio);
        Button cerrar = dialogView.findViewById(R.id.BTN_alertdialog_ver_detalles_agregar_productos_cerrar);
        Button ver_descripcion = dialogView.findViewById(R.id.BTN_alertdialog_ver_detalles_agregar_productos_ver_detalles);
        Button agregar_producto = dialogView.findViewById(R.id.BTN_alertdialog_ver_detalles_agregar_productos_agregar_productos);
        LinearLayout Linear_botones = dialogView.findViewById(R.id.LINEAR_alertdialog_agregar_productos);
        final LinearLayout Linear_cantidad = dialogView.findViewById(R.id.ad_agregar_producto_linear_cantidad);
        ImageView Post_image = dialogView.findViewById(R.id.post_image);

        final TextView tv_precio_final = dialogView.findViewById(R.id.ad_agregar_producto_tv_precio_final);
        final EditText ed_cantidad = dialogView.findViewById(R.id.ad_agregar_producto_ed_cantidad);

        if (str_por_cantidad.equals("si")) {
            Linear_cantidad.setVisibility(View.VISIBLE);
            ed_cantidad.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    if (ed_cantidad.getText().toString().length() > 0) {
                        int precio = Integer.parseInt(str_precio);
                        int cantidad = Integer.parseInt(ed_cantidad.getText().toString());
                        int precio_total = precio * cantidad;
                        String str_precio_total = String.valueOf(precio_total);
                        tv_precio_final.setText(str_precio_total);
                    } else {
                        tv_precio_final.setText("0");
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });
        }

        nombre_producto.setText(str_nombre);
        precio_producto.setText("Precio = \n" + str_precio);
        if (str_detalles.length() < 1) {
            ver_descripcion.setVisibility(GONE);
            Linear_botones.setWeightSum(2);
        }

        if (str_imagen != null) {
            if (str_imagen.length() > 7) {

                Picasso.with(getContext())
                        .load(str_imagen)
                        .placeholder(R.drawable.progress_animation)
                        .into(Post_image);
            }
        }

        cerrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });

        ver_descripcion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (str_detalles.length() > 1) {
                    metodos.pasar_categoria_producto_seleccionado = str_key + "·" + str_nombre + "·" + str_precio;
                    alertDialog.dismiss();

                    final FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.replace(R.id.content_main, new c_categoria_productos_detalles()).addToBackStack(toString());
                    ft.commit();

                } else {
                    Toast.makeText(getActivity(), "no tiene descripcion", Toast.LENGTH_SHORT).show();
                }
            }
        });

        agregar_producto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (str_por_cantidad.equals("si")) {
                    if (ed_cantidad.getText().toString().length() > 0) {
                        if (!ed_cantidad.getText().toString().equals("0")) {
                            int precio = Integer.parseInt(str_precio);
                            int cantidad = Integer.parseInt(ed_cantidad.getText().toString());
                            int precio_total = precio * cantidad;
                            String str_precio_total = String.valueOf(precio_total);
                            method_guardar(ed_cantidad.getText().toString(), str_precio_total);
                        } else {
                            Toast.makeText(getActivity(), "el valor debe ser diferente a 0", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getActivity(), "debe poner algun valor en cantidad", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    method_guardar(null, str_precio);
                }


            }

            private void method_guardar(String cantidad, String precio) {
                String str_cambiado;
                if (cantidad != null) {
                    str_cambiado = str_nombre + " Cantidad  " + cantidad;
                } else {
                    str_cambiado = str_nombre;
                    cantidad = "1";
                }
                String ubicacicion = ((MainActivity) getActivity()).ubicacion_elegida;
                ((MainActivity) getActivity()).guardar_pedido(str_negocio, str_cambiado, precio, str_categoria, str_key, ubicacicion, cantidad);
                Toast.makeText(getActivity(), "Producto guardado", Toast.LENGTH_SHORT).show();
                alertDialog.dismiss();
            }
        });

        alertDialog.show();

    }

    private interface FirebaseCallBack_pasar_al_adaptador {
        void onCallBack(ArrayList<String> negocios_2);
    }
}

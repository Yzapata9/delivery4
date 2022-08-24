package com.venta_productos.delivery;


import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.venta_productos.delivery.adapter.adaptador_imagenes_detalle;

import static android.view.View.GONE;


/**
 * A simple {@link Fragment} subclass.
 */
public class c_categoria_productos_detalles extends Fragment {


    public c_categoria_productos_detalles() {
        // Required empty public constructor
    }

    DatabaseReference mDatabase_2, nDataBase;
    String[] productos_nombre;
    String str_key_producto_seleccionado, STR_nombre_producto, str_negocio,str_categoria, str_precio_prodcuto;
    TextView titulo, descripcion, precio;
    RecyclerView RV_ver_fotos_productos;
    Button BTN_agregar_producto;
    String str_por_cantidad, str_imagen, str_key;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View Productos_detalles = inflater.inflate(R.layout.f_productos_detalles_fotos_descipcion, container, false);

        if (getActivity() != null) {
            ((MainActivity) getActivity()).mostrar_fab(true);
            metodos.alerdialog_descargando_informacion(getActivity(), true, "Buscando informacion");
        }
        titulo = Productos_detalles.findViewById(R.id.TV_productos_detalles_titulo);
        descripcion = Productos_detalles.findViewById(R.id.TV_productos_detalles_descripcion);
        precio = Productos_detalles.findViewById(R.id.TV_productos_detalles_precio);
        RV_ver_fotos_productos = Productos_detalles.findViewById(R.id.recycler_productos_detalles_fotos);
        BTN_agregar_producto = Productos_detalles.findViewById(R.id.BTN_productos_detalles_descripcion_guardar_pedido);
        BTN_agregar_producto.setClickable(false);

        productos_nombre = metodos.pasar_categoria_producto_seleccionado.split("·");
        str_key_producto_seleccionado = productos_nombre[0];
        STR_nombre_producto = productos_nombre[1];
        String[] input_datos = metodos.pasar_datos_categorias_negocios.split(",");
        str_negocio = input_datos[0];
        str_categoria = input_datos[1];

        titulo.setText(STR_nombre_producto);

        RV_ver_fotos_productos.setNestedScrollingEnabled(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        RV_ver_fotos_productos.setLayoutManager(layoutManager);

        mDatabase_2 = FirebaseDatabase.getInstance().getReference().child("negocios").child(str_negocio).child("categorias").child(str_categoria).child("productos").child(str_key_producto_seleccionado);
        nDataBase = mDatabase_2.child("detalles").child("fotos");
        mDatabase_2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String desc = (String) dataSnapshot.child("detalles").child("descripcion").getValue();
                str_por_cantidad = (String) dataSnapshot.child("es_por_cantidad").getValue();
                str_precio_prodcuto = (String) dataSnapshot.child("precio").getValue();
                str_imagen = (String) dataSnapshot.child("imagen").getValue();
                str_key = dataSnapshot.getKey();
                BTN_agregar_producto.setClickable(true);
                if (desc != null) {
                    descripcion.setText(desc);
                } else {
                    descripcion.setText("sin descripcion");
                }
                if (str_precio_prodcuto!=null) {
                    precio.setText(str_precio_prodcuto);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        BTN_agregar_producto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

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
                LinearLayout Linear = dialogView.findViewById(R.id.LINEAR_alertdialog_agregar_productos);
                final LinearLayout Linear_cantidad = dialogView.findViewById(R.id.ad_agregar_producto_linear_cantidad);
                ImageView Post_image = dialogView.findViewById(R.id.post_image);

                final TextView tv_precio_final = dialogView.findViewById(R.id.ad_agregar_producto_tv_precio_final);
                final EditText ed_cantidad = dialogView.findViewById(R.id.ad_agregar_producto_ed_cantidad);

                if (str_por_cantidad != null) {
                    if (str_por_cantidad.equals("si")) {
                        Linear_cantidad.setVisibility(View.VISIBLE);
                        ed_cantidad.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                            }

                            @Override
                            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                                if (ed_cantidad.getText().toString().length() > 0) {
                                    int precio = Integer.parseInt(str_precio_prodcuto);
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
                }
                nombre_producto.setText(STR_nombre_producto);
                precio_producto.setText("Precio = \n" + str_precio_prodcuto);

                ver_descripcion.setVisibility(GONE);
                Linear.setWeightSum(2);

                if (str_imagen != null) {
                    Picasso.with(getContext())
                            .load(str_imagen)
                            .placeholder(R.drawable.progress_animation)
                            .into(Post_image);
                }

                cerrar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alertDialog.dismiss();
                    }
                });

                agregar_producto.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (str_por_cantidad != null) {
                            if (str_por_cantidad.equals("si")) {
                                if (ed_cantidad.getText().toString().length() > 0) {
                                    if (!ed_cantidad.getText().toString().equals("0")) {
                                        int precio = Integer.parseInt(str_precio_prodcuto);
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
                                method_guardar(null, str_precio_prodcuto);
                            }
                        } else {
                            method_guardar(null, str_precio_prodcuto);
                        }
                    }

                    private void method_guardar(String cantidad, String precio) {
                        String str_cambiado;
                        if (cantidad != null) {
                            str_cambiado = STR_nombre_producto + " Cantidad  " + cantidad;
                        } else {
                            str_cambiado = STR_nombre_producto;
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
        });

        if (getActivity() != null) {
            ((MainActivity) getActivity()).chekear_internet();
        }
        return Productos_detalles;
    }

    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<adaptador_imagenes_detalle, BlogViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<adaptador_imagenes_detalle, BlogViewHolder>(
                adaptador_imagenes_detalle.class,
                R.layout.row_fotos_detalle_productos,
                BlogViewHolder.class,
                nDataBase
        ) {

            @Override
            protected void populateViewHolder(BlogViewHolder viewHolder, final adaptador_imagenes_detalle model, final int position) {
                viewHolder.setImage(getContext(), model.getImage());

            }
        };
        RV_ver_fotos_productos.setAdapter(firebaseRecyclerAdapter);
        metodos.alerdialog_descargando_informacion(getActivity(), false, "");
    }

    public static class BlogViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public BlogViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setImage(Context ctx, String image) {
            ImageView Post_image = mView.findViewById(R.id.post_image);
            Picasso.with(ctx)
                    .load(image)
                    .placeholder(R.drawable.progress_animation)
                    .into(Post_image);
        }

    }
}

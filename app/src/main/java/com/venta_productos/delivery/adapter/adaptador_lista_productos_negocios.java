package com.venta_productos.delivery.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.venta_productos.delivery.R;


import static android.view.View.GONE;

/**
 * Created by windows hdrp on 13/03/2017.
 */

public class adaptador_lista_productos_negocios extends BaseAdapter {

    private String input_string[];
    private Context context;
    String TAG = "asdf";

    public adaptador_lista_productos_negocios(Context context, String input_string[]) {
        this.context = context;
        this.input_string = input_string;
    }

    @Override
    public int getCount() {
        return input_string.length;
    }

    @Override
    public Object getItem(int position) {
        return input_string[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View gridview = convertView;
        String[] separar_datos = input_string[position].split("€");

        String str_key = separar_datos[0];
        String str_nombre = separar_datos[1];
        String str_precio = separar_datos[2];
        String str_disponible = separar_datos[3];
        String str_detalles = separar_datos[4];
        String str_imagen = separar_datos[5];

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (inflater != null) {
                gridview = inflater.inflate(R.layout.row_productos_original, null);
            } else {
                Toast.makeText(context, "Error cargando el inflater del row", Toast.LENGTH_SHORT).show();
            }
        }

        FrameLayout esta_disponible = gridview.findViewById(R.id.linear_row);
        final ImageView Post_image = gridview.findViewById(R.id.post_image);
        TextView tv_nombre_producto = gridview.findViewById(R.id.post_titlee);
        TextView tv_precio = gridview.findViewById(R.id.post_rubro);

        tv_nombre_producto.setText(str_nombre);
        str_precio = "Precio= " + str_precio;
        tv_precio.setText(str_precio);

        if (!str_disponible.equals("si")) {
            esta_disponible.setVisibility(GONE);
            esta_disponible.setLayoutParams(new LinearLayout.LayoutParams(1, 1));
            esta_disponible.setClickable(false);
        }

        if (str_imagen != null) {
            if (str_imagen.length() > 7) {
                Picasso.with(context)
                        .load(str_imagen)
                        .placeholder(R.drawable.progress_animation)
                        .into(Post_image, new Callback() {
                            @Override
                            public void onSuccess() {
                                Post_image.setScaleType(ImageView.ScaleType.FIT_XY);
//                            Post_image.getLayoutParams().height=340;
//                            Post_image.getLayoutParams().height=FrameLayout.LayoutParams.MATCH_PARENT;
                                /*Post_image.getLayoutParams().width = FrameLayout.LayoutParams.MATCH_PARENT;
                                Post_image.requestLayout();*/
                            }

                            @Override
                            public void onError() {
                            }
                        });
            }
        }

        return gridview;
    }
}

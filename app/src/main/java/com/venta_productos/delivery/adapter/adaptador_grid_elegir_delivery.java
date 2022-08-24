package com.venta_productos.delivery.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.venta_productos.delivery.R;

/**
 * Created by windows hdrp on 13/03/2017.
 */

public class adaptador_grid_elegir_delivery extends BaseAdapter {

    private String letters[];

    private Context context;

    String TAG = "asdf";

    public adaptador_grid_elegir_delivery(Context context, String letters[]) {
        this.context = context;
        this.letters = letters;
    }

    @Override
    public int getCount() {
        return letters.length;
    }

    @Override
    public Object getItem(int position) {
        return letters[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View gridview = convertView;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (inflater != null) {
                gridview = inflater.inflate(R.layout.row_elegir_delivery, null);
            }
        }


        TextView nombre_delivery = gridview.findViewById(R.id.nombre_delivery);
        TextView precio_delivery_por_negocio = gridview.findViewById(R.id.precio_delivery_por_negocio);
        TextView precio_delivery = gridview.findViewById(R.id.precio_delivery);
        TextView cantidad_pedidos_delivery = gridview.findViewById(R.id.cantidad_pedidos_delivery);
        LinearLayout linear_row_elegir_delivery = gridview.findViewById(R.id.linear_row_elegir_delivery);

        String[] delivery_imput = letters[position].split("€");
        String str_nombre = delivery_imput[0];
        String str_precio_x_negocio = delivery_imput[1];
        String str_precio = delivery_imput[2];
        String str_cantidad_pedidos = delivery_imput[3];

        //le paso un parametro adicional si es una eleccion de delivery
        if (delivery_imput.length == 5) {
            String poner_background = delivery_imput[4];
            if (poner_background.equals("si")) {
                linear_row_elegir_delivery.setBackgroundResource(R.drawable.btn_confirmacion_elegir_opcion);
            }
        }
        String str_precio_por_negocio = "Precio por negocio= \n" + str_precio_x_negocio;
        String str_precio_total = "Precio Delivery= \n" + str_precio;
        String str_pedidos_en_espera = "pedidos en espera= \n" + str_cantidad_pedidos;

        nombre_delivery.setText(str_nombre);
        precio_delivery_por_negocio.setText(str_precio_por_negocio);
        precio_delivery.setText(str_precio_total);
        cantidad_pedidos_delivery.setText(str_pedidos_en_espera);

        return gridview;
    }
}

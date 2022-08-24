package com.venta_productos.delivery.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.venta_productos.delivery.R;

/**
 * Created by windows hdrp on 13/03/2017.
 */

public class adaptador_grid_carrito extends BaseAdapter {

    private String letters[];

    private Context context;

    public adaptador_grid_carrito(Context context, String letters[]) {
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
                gridview = inflater.inflate(R.layout.row_carrito, null);
            }
        }

        TextView letter = gridview.findViewById(R.id.letters);
        TextView numero = gridview.findViewById(R.id.TV_row_carrito_numero);

        String corregir_texto= letters[position].replace("Negocio", "N").replace("Producto", "P").replace("Precio","$");

//        letter.setText(letters[position]);
        letter.setText(corregir_texto);
        numero.setText(String.valueOf(position + 1));

        return gridview;
    }
}

package com.venta_productos.delivery.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.venta_productos.delivery.R;

/**
 * Created by windows hdrp on 13/03/2017.
 */

public class adaptador_fragment_principal_mejorado extends BaseAdapter {

    private final String[] letters;


    private Context context;

    String TAG = "asdf";

    public adaptador_fragment_principal_mejorado(Context context, String letters[]) {
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
                gridview = inflater.inflate(R.layout.row_principal_negocios, null);
            }
        }

        String[] separar_datos = letters[position].split("€€");

        String nombre = separar_datos[0];
        String horario = separar_datos[1];
        String rubro = separar_datos[2];
        String imagen = separar_datos[3];

        final ImageView im_degrade = gridview.findViewById(R.id.im_principal_degrade);
        TextView post_title = gridview.findViewById(R.id.post_titlee);
        post_title.setText(nombre);

        TextView post_rubro = gridview.findViewById(R.id.post_rubro);
        post_rubro.setText(rubro);

        final ImageView Post_image = gridview.findViewById(R.id.post_image_negocios);

        if (!imagen.equals("sin_imagen")) {

            Picasso.with(context)
                    .load(imagen)
                    .placeholder(R.drawable.progress_animation)
                    .into(Post_image, new Callback() {
                        @Override
                        public void onSuccess() {
                            Post_image.setScaleType(ImageView.ScaleType.FIT_XY);
                            Post_image.getLayoutParams().height = 340;
//                            Post_image.getLayoutParams().height=FrameLayout.LayoutParams.MATCH_PARENT;
                            Post_image.getLayoutParams().width = FrameLayout.LayoutParams.MATCH_PARENT;
                            Post_image.requestLayout();
                            im_degrade.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onError() {
                        }
                    });
        }
        return gridview;
    }
}

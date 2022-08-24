package com.venta_productos.delivery;


import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toolbar;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.venta_productos.delivery.adapter.adaptador_de_recyclerview;

import java.util.Calendar;

import static android.view.View.GONE;

public class c_ver_negocios_sin_coneccion extends Fragment {

    DatabaseReference mDatabase_2;
    RecyclerView mBloglist;
    TextView titulo;
    Toolbar toolbar;
    static int dia_de_la_semana;
    static int int_horas;
    static int int_minutos;
    private FirebaseAuth mAuth;

    public c_ver_negocios_sin_coneccion() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.f_recycler_view, container, false);


        if (getActivity() != null) {
            ((MainActivity) getActivity()).settitletoolbar("negocios");
            ((MainActivity) getActivity()).mostrar_fab(false);
            metodos.alerdialog_descargando_informacion(getActivity(),true,"Descargando Negocios");
        }


        titulo = view.findViewById(R.id.lv_titulo);

        Calendar cal = Calendar.getInstance();
        dia_de_la_semana = cal.get(Calendar.DAY_OF_WEEK);
        int_horas = cal.get(Calendar.HOUR_OF_DAY);
        int_minutos = cal.get(Calendar.MINUTE);

        titulo.setText("Negocios = ");
        titulo.setPadding(0, 5, 0, 5);

        mDatabase_2 = FirebaseDatabase.getInstance().getReference().child("negocios");
        mBloglist = view.findViewById(R.id.blog_list);
        mBloglist.setHasFixedSize(true);
        mBloglist.setLayoutManager(new LinearLayoutManager(getActivity()));


        if (getActivity() != null) {
            ((MainActivity) getActivity()).chekear_internet();
        }

        return view;
    }

    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<adaptador_de_recyclerview, BlogViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<adaptador_de_recyclerview, BlogViewHolder>(

                adaptador_de_recyclerview.class,
                R.layout.row_principal_negocios,
                BlogViewHolder.class,
                mDatabase_2

        ) {

            @Override
            protected void populateViewHolder(BlogViewHolder viewHolder, final adaptador_de_recyclerview model, final int position) {

                viewHolder.setNombre(model.getNombre());
                viewHolder.setRubro(model.getRubro());
                viewHolder.setImagen(getContext(), model.getImagen());
                if (getActivity() != null) {
                metodos.alerdialog_descargando_informacion(getActivity(),false,"");
                }

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mAuth = FirebaseAuth.getInstance();
                        FirebaseUser currentUser = mAuth.getCurrentUser();
                        if (currentUser == null) {
                            metodos.alertdialog_ingresar_a_la_cuenta(getActivity());
                        } else {
                            alertdialog_agregar_ubicacion();
                        }
                    }
                });
            }
        };
        mBloglist.setAdapter(firebaseRecyclerAdapter);

    }

    private void alertdialog_agregar_ubicacion() {
        AlertDialog.Builder alerta = new AlertDialog.Builder(
                getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.ad_basico_titulo_texto_botones, null);
        alerta.setView(dialogView);
        final AlertDialog alertDialog = alerta.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView tv_titulo = dialogView.findViewById(R.id.ad_tv_titulo);
        TextView tv_mensaje = dialogView.findViewById(R.id.ad_tv_mensaje);
        Button btn_aceptar = dialogView.findViewById(R.id.btn_aceptar);
        Button btn_cancelar = dialogView.findViewById(R.id.btn_cancelar);

        tv_titulo.setText("debe agregar una ubicacion para continuar");
        tv_mensaje.setVisibility(GONE);
        btn_aceptar.setText("AGREGAR UBICACION");
        btn_cancelar.setVisibility(GONE);

        btn_aceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.content_main, new c_ingresar_direccion()).addToBackStack(toString());
                ft.commit();
                alertDialog.dismiss();
            }
        });


        alertDialog.show();

    }


    public static class BlogViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public BlogViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }


        private void setNombre(String nombre) {

            TextView post_title = mView.findViewById(R.id.post_titlee);
            post_title.setText(nombre);
        }


        private void setRubro(String rubro) {

            TextView post_rubro = mView.findViewById(R.id.post_rubro);
            post_rubro.setText(rubro);
        }

        private void setImagen(Context ctx, String image) {

            final ImageView Post_image = mView.findViewById(R.id.post_image_negocios);
            Picasso.with(ctx)
                    .load(image)
                    .placeholder(R.drawable.progress_animation)
                    .into(Post_image, new Callback() {
                        @Override
                        public void onSuccess() {
                            Post_image.setScaleType(ImageView.ScaleType.FIT_XY);
                        }
                        @Override
                        public void onError() {
                        }
                    });
        }
    }
}

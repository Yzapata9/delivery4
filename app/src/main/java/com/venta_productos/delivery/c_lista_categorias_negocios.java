package com.venta_productos.delivery;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.venta_productos.delivery.adapter.adaptador_lista_categorias_negocios;

import java.util.ArrayList;

import static android.view.View.GONE;


/**
 * A simple {@link Fragment} subclass.
 */
public class c_lista_categorias_negocios extends Fragment {

    ArrayList<String> array_productos = new ArrayList<>();
    DatabaseReference db_categoria_negocios;
    GridView gridview;
    String str_negocio;
    LinearLayout linear_botones_ubicacion;
    SwipeRefreshLayout swipe;

    String TAG = "asdf";

    Boolean se_cargo_la_categoria = false;

    public c_lista_categorias_negocios() {
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

        str_negocio = metodos.pasar_datos_del_negocio_al_fragment_categorias;

        db_categoria_negocios = FirebaseDatabase.getInstance().getReference().child("negocios").child(str_negocio);
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

                    //pasarle el negocio y la categoria que fue elegida
                    String[] get_datos = array_productos.get(i).split("€");

                    String str_guardar_key = get_datos[0];
                    String str_nombre = get_datos[1];

                    metodos.pasar_datos_categorias_negocios =str_negocio+","+str_guardar_key;
                    metodos.main_cambiar_fragment(getActivity(),"c_lista_categorias_productos_negocios");

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
        db_categoria_negocios.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //lo guardo en el str_negocio
                se_cargo_la_categoria = true;

                for (DataSnapshot snapshot : dataSnapshot.child("categorias").getChildren()) {
                    String key = snapshot.getKey();
                    String disponible = (String) snapshot.child("disponible").getValue();
                    String nombre = (String) snapshot.child("nombre").getValue();
                    String imagen = (String) snapshot.child("imagen").getValue();

                    if (!snapshot.hasChild("productos")) {
                        disponible = "no";
                    }

                    String unir_datos = key + "€" + nombre + "€" + disponible +"€" + imagen;

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
        final adaptador_lista_categorias_negocios adapter_productos = new adaptador_lista_categorias_negocios(getActivity(), pasar_datos_al_adapter);
        gridview.setAdapter(adapter_productos);

        if (swipe.isRefreshing()) {
            swipe.setRefreshing(false);
        }
    }

    private interface FirebaseCallBack_pasar_al_adaptador {
        void onCallBack(ArrayList<String> negocios_2);
    }
}

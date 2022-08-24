package com.venta_productos.delivery;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.snatik.polygon.Point;
import com.snatik.polygon.Polygon;

import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.PathOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.Collections;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;


/**
 * A simple {@link Fragment} subclass.
 */
public class c_mapas_ver_covertura extends Fragment {


    public c_mapas_ver_covertura() {
        // Required empty public constructor
    }

    private MyLocationNewOverlay mLocationOverlay;
    DatabaseReference database_areas_delivery;
    Drawable marker;
    Polygon.Builder poly2 = null;
    int cuantos_puntos_guardar = 4;
    ArrayList<OverlayItem> overlayArray = new ArrayList<>();
    ArrayList<OverlayItem> overlayArray_gps_negocios = new ArrayList<>();
    PathOverlay myPath1;
    MapView map;
    FloatingActionButton fab;
    Button btn_ingresar_cuenta, btn_ver_negocios;
    FloatingActionButton btn_mi_ubicacion;

    ItemizedIconOverlay<OverlayItem> items_mapa_1_overlay;
    ItemizedIconOverlay<OverlayItem> items_mapa_gps_negocios;

    String userid;

    //agregar direccion gps a la ubicacion
    boolean agregar_direccion_gps_a_la_ubicacion = false;
    String str_gps_elegido_en_el_mapa;
    Overlay touchOverlay;
    ItemizedIconOverlay<OverlayItem> mostrar_un_icono_en_el_mapa = null;
    TextView tv_que_hacer;

    String TAG = "asdf";

    //solo mostrar areas
    boolean boolean_solo_mostar_areas;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View Mapas_ver_covertura = inflater.inflate(R.layout.f_mapas_ver_covertura, container, false);

        permisos_gps();
        permiso_escribir_memoria_externa();

        ((MainActivity) getActivity()).mostrar_fab(false);
        agregar_direccion_gps_a_la_ubicacion = ((MainActivity) getActivity()).boolean_agregar_gps_en_el_mapa;
        boolean_solo_mostar_areas = ((MainActivity) getActivity()).boolean_solo_mostrar_las_areas_del_mapa;

        btn_ingresar_cuenta = Mapas_ver_covertura.findViewById(R.id.bottom_sheet_ingresar);
        btn_mi_ubicacion = Mapas_ver_covertura.findViewById(R.id.boton_mi_ubicacion);
        btn_ver_negocios = Mapas_ver_covertura.findViewById(R.id.btn_mapas_ver_negocios);
        tv_que_hacer = Mapas_ver_covertura.findViewById(R.id.tv_mapas_que_hacer);

        map = Mapas_ver_covertura.findViewById(R.id.map);
        btn_ingresar_cuenta = Mapas_ver_covertura.findViewById(R.id.bottom_sheet_ingresar);

        map.setTileSource(TileSourceFactory.MAPNIK);

        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);

        BoundingBoxE6 bbox_bariloche = new BoundingBoxE6(-41.033770, -71.591065, -41.237056, -71.111444);
        map.setScrollableAreaLimit(bbox_bariloche);
        map.setMinZoomLevel(12);
        map.setMaxZoomLevel(17);


        final IMapController mapController = map.getController();
        mapController.setZoom(13);
        GeoPoint startPoint = new GeoPoint(-41.15, -71.3);
        mapController.setCenter(startPoint);

        if (!boolean_solo_mostar_areas) { //esto sirve para solo ver las areas de cobertura y agregar la direccion
            if (!agregar_direccion_gps_a_la_ubicacion) { //esto sirve para saber si quiero guardar el gps en el mapa

                //esto como es true sirve para manejar si un usuario llega desde el c_principal

                btn_ver_negocios.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final FragmentTransaction ft = getFragmentManager().beginTransaction();
                        ft.replace(R.id.content_main, new c_ver_negocios_sin_coneccion()).addToBackStack(toString());
                        ft.commit();
                    }
                });

                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                FirebaseUser currentUser = mAuth.getCurrentUser();
                if (currentUser == null) {
                    btn_ingresar_cuenta.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            btn_ingresar_cuenta.setText("ingresar cuenta");
                            final FragmentTransaction ft = getFragmentManager().beginTransaction();
                            ft.replace(R.id.content_main, new c_ingreso_a_la_app()).addToBackStack(toString());
                            ft.commit();
                        }
                    });
                } else {

                    checkear_si_el_usuario_guardo_domicilio(new FirebaseCallBack() {
                        @Override
                        public void onCallBack(String estado) {
                            if (estado.equals("con_ubicacion")) {

                                metodos.alerdialog_descargando_informacion(getActivity(), false, "");
                                final FragmentTransaction ft = getFragmentManager().beginTransaction();
                                ft.replace(R.id.content_main, new c_principal()).addToBackStack(toString());
                                ft.commit();

                            } else {
                                btn_ingresar_cuenta.setText("agregar ubicacion");
                                btn_ingresar_cuenta.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        final FragmentTransaction ft = getFragmentManager().beginTransaction();
                                        ft.replace(R.id.content_main, new c_ingresar_direccion()).addToBackStack(toString());
                                        ft.commit();
                                    }
                                });

                            }
                        }
                    });


                }
            } else {
                //si quiero elegir un gps en los mapas

                touchOverlay = new Overlay(getActivity()) {
                    @Override
                    protected void draw(Canvas arg0, MapView arg1, boolean arg2) {

                    }

                    @Override
                    public boolean onSingleTapConfirmed(final MotionEvent e, final MapView mapView) {

                        //transforma el click en un point, se puede optimizar
                        Projection proj = mapView.getProjection();
                        GeoPoint loc = (GeoPoint) proj.fromPixels((int) e.getX(), (int) e.getY());
                        String longitude = Double.toString(((double) loc.getLongitudeE6()) / 1000000);
                        String latitude = Double.toString(((double) loc.getLatitudeE6()) / 1000000);
                        String cordenadas = latitude + "," + longitude;
                        str_gps_elegido_en_el_mapa = latitude + "," + longitude;
                        String[] coord = cordenadas.split(",");
                        double coord1 = Double.parseDouble(coord[0]);
                        double coord2 = Double.parseDouble(coord[1]);
                        Point point = new Point(coord1, coord2);

                        //muestra un marker en el mapa
                        ArrayList<OverlayItem> overlayArray = new ArrayList<>();
                        OverlayItem mapItem = new OverlayItem("", "", new GeoPoint((((double) loc.getLatitudeE6()) / 1000000), (((double) loc.getLongitudeE6()) / 1000000)));
                        marker = getActivity().getApplicationContext().getResources().getDrawable(R.drawable.ic_mapas_location_round_rojo);
                        mapItem.setMarker(marker);
                        overlayArray.add(mapItem);

                        if (mostrar_un_icono_en_el_mapa == null) {
                            //esto hace que si no existe algun punto lo muestre
                            mostrar_un_icono_en_el_mapa = new ItemizedIconOverlay<>(getActivity().getApplicationContext(), overlayArray, null);
                            mapView.getOverlays().add(mostrar_un_icono_en_el_mapa);
                            mapView.invalidate();
                        } else {
                            //esto hace que si EXISTE algun punto borra el anterior y muestra uno nuevo
                            mapView.getOverlays().remove(mostrar_un_icono_en_el_mapa);
                            mapView.invalidate();
                            mostrar_un_icono_en_el_mapa = new ItemizedIconOverlay<>(getActivity().getApplicationContext(), overlayArray, null);
                            mapView.getOverlays().add(mostrar_un_icono_en_el_mapa);
                        }


                        return true;
                    }

                }; // aca esta el listener que maneja los nuevos marcadores
                map.getOverlays().add(touchOverlay); // esto inserta el touchovertlay en el mapa

                //tuve que poner el boton de ver negocios como agregar ubicacion xq en el xml tiene alingparentbottom=true
                btn_ingresar_cuenta.setVisibility(View.GONE);
                btn_ver_negocios.setText("agregar ubicacion");
                tv_que_hacer.setText("Zonas donde esta disponible el delivery \nHaga click en el mapa para agregar una ubicacion");
                btn_ver_negocios.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        method_agregar_gps_a_la_ubicacion();
                    }
                });

            }
        } else {
            btn_ingresar_cuenta.setVisibility(View.GONE);
            btn_ver_negocios.setText("agregar ubicacion");
            tv_que_hacer.setText("Zonas donde esta disponible el delivery");
            btn_ver_negocios.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.replace(R.id.content_main, new c_editar_usuario_y_log_out()).addToBackStack(toString());
                    ft.commit();
                }
            });
        }


        database_areas_delivery = FirebaseDatabase.getInstance().getReference();
        database_areas_delivery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snap_deliverys : dataSnapshot.child("deliverys").getChildren()) {
                    for (DataSnapshot snap_areas : snap_deliverys.child("area").getChildren()) {
                        ArrayList<String> area = new ArrayList<>();
                        String area_string = (String) snap_areas.getValue();

                        if (area_string != null) {
                            String[] puntos_del_area_string = area_string.split(",");
                            Collections.addAll(area, puntos_del_area_string);

                            if (getActivity() != null) {
                                hacer_un_poli(area);
                            }
                        }
                    }
                }
                ArrayList<String> gps = new ArrayList<>();
                for (DataSnapshot snap_negocios : dataSnapshot.child("negocios").getChildren()) {
                    String area_string = (String) snap_negocios.child("gps_negocio").getValue();
                    gps.add(area_string);
                }
                if (getActivity() != null) {
                    hacer_marcador_de_negocios(gps);
                    metodos.alerdialog_descargando_informacion(getActivity(), false, "");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        this.mLocationOverlay = new MyLocationNewOverlay(getContext(), new GpsMyLocationProvider(getContext()), map);
        this.mLocationOverlay.enableMyLocation();
        map.getOverlays().add(this.mLocationOverlay);

        btn_mi_ubicacion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                permisos_gps();
                if (Build.VERSION.SDK_INT >= 23) {
                    if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        metodos.alerdialog_pedir_permisos(getActivity(), 3);
                    } else {
                        GeoPoint g = mLocationOverlay.getMyLocation();
                        if (g != null) {
                            mapController.setCenter(g);
                            mapController.setZoom(13);
                        } else {
                            Toast.makeText(getActivity(), "Buscando Ubicacion", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    GeoPoint g = mLocationOverlay.getMyLocation();
                    if (g != null) {
                        mapController.setCenter(g);
                        mapController.setZoom(13);
                    } else {
                        Toast.makeText(getActivity(), "Buscando Ubicacion", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        return Mapas_ver_covertura;
    }

    private void method_agregar_gps_a_la_ubicacion() {

        if (str_gps_elegido_en_el_mapa != null) {
            ((MainActivity) getActivity()).str_elegido_en_el_mapa = str_gps_elegido_en_el_mapa;

            final FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.content_main, new c_ingresar_direccion()).addToBackStack(toString());
            ft.commit();

        } else {
            Toast.makeText(getActivity(), "elija un punto en el mapa", Toast.LENGTH_SHORT).show();
        }


    }

    public void hacer_marcador_de_negocios(ArrayList<String> array_gps) {
        for (int i_gps = 0; i_gps < array_gps.size(); i_gps++) {

            String[] coord = array_gps.get(i_gps).split(",");

            double coord1 = Double.parseDouble(coord[0]);
            double coord2 = Double.parseDouble(coord[1]);

            OverlayItem mapItem_gps_negocios = new OverlayItem("", "", new GeoPoint(coord1, coord2));

            Drawable marcador = ContextCompat.getDrawable(getContext(), R.drawable.ic_store_mall_directory_negocio);
            final Bitmap bitmap = ((BitmapDrawable) marcador).getBitmap();
            int ancho = 50;
            int alto = 50;
            Drawable d = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, ancho, alto, true));
            mapItem_gps_negocios.setMarker(d);

            overlayArray_gps_negocios.add(mapItem_gps_negocios);

        }

        items_mapa_gps_negocios = new ItemizedIconOverlay<>(getActivity().getApplicationContext(), overlayArray_gps_negocios, null);
        map.getOverlays().add(items_mapa_gps_negocios);
        map.invalidate();

    }

    public void hacer_un_poli(ArrayList<String> array_geopoint) {

        poly2 = new Polygon.Builder();
        PathOverlay myPath = new PathOverlay(Color.RED, getActivity());

        GeoPoint primero = null;

        for (int i_poly = 0; i_poly < cuantos_puntos_guardar; i_poly++) {
            String[] coord = array_geopoint.get(i_poly).split("€");

            double coord1 = Double.parseDouble(coord[0]);
            double coord2 = Double.parseDouble(coord[1]);

            OverlayItem mapItem = new OverlayItem("", "", new GeoPoint(coord1, coord2));

            Drawable marcador = ContextCompat.getDrawable(getContext(), R.drawable.ic_deliv_round);
            final Bitmap bitmap = ((BitmapDrawable) marcador).getBitmap();
            int ancho = 50;
            int alto = 50;
            Drawable d = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, ancho, alto, true));
            mapItem.setMarker(d);


            overlayArray.add(mapItem);

            if (i_poly == 0) {
                primero = new GeoPoint(coord1, coord2);
            }

            GeoPoint gPt0 = new GeoPoint(coord1, coord2);
            myPath.addPoint(gPt0);


            Point point = new Point(coord1, coord2);
            poly2.addVertex(point);
        }

        myPath.addPoint(primero);

        //cargarlos

        myPath1 = myPath;
        map.getOverlays().add(myPath1);
        String coord_poly_1 = String.valueOf(array_geopoint).substring(1, array_geopoint.toString().length() - 1);
        //ArrayList<OverlayItem> overlayArray_1 = overlayArray;

        items_mapa_1_overlay = new ItemizedIconOverlay<>(getActivity().getApplicationContext(), overlayArray, null);
        map.getOverlays().add(items_mapa_1_overlay);
        map.invalidate();
    }

    @Override
    public void onResume() {
        this.mLocationOverlay = new MyLocationNewOverlay(getContext(), new GpsMyLocationProvider(getContext()), map);
        this.mLocationOverlay.enableMyLocation();
        map.getOverlays().add(this.mLocationOverlay);

        super.onResume();
    }

    @Override
    public void onDestroy() {
        map.getOverlays().remove(this.mLocationOverlay);
        super.onDestroy();
    }

    private void checkear_si_el_usuario_guardo_domicilio(final FirebaseCallBack firebaseCallBack) {

        Context contex;
        SharedPreferences sharpref;
        contex = getActivity();
        sharpref = getContext().getSharedPreferences("usar_app", Context.MODE_PRIVATE);

        String valor_guerdado = sharpref.getString("usuario", "no hay dato");
        String[] dato = valor_guerdado.split(",");
        if (!valor_guerdado.equals("no hay dato")) {
            userid = dato[1];
        }
        DatabaseReference chekear_domicilio = FirebaseDatabase.getInstance().getReference().child("usuario").child("clientes").child(userid).child("ubicacion");
        chekear_domicilio.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    firebaseCallBack.onCallBack("con_ubicacion");
                } else {
                    firebaseCallBack.onCallBack("sin_ubicacion");
                }
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private interface FirebaseCallBack {
        void onCallBack(String estado);
    }

    public void permisos_gps() {

        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            metodos.alerdialog_pedir_permisos(getActivity(), 3);
        } else {

            final LocationManager manager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                mainbuildAlertMessageNoGps();
            }
        }

    }

    public void mainbuildAlertMessageNoGps() {

        ActivityCompat.requestPermissions(getActivity(), new String[]{ACCESS_FINE_LOCATION}, 1);

        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getActivity());
        View bottomSheetview = getLayoutInflater().inflate(R.layout.bottom_sheet_coneccion_gps, null);
        bottomSheetDialog.setContentView(bottomSheetview);
        bottomSheetDialog.show();
        TextView texto = bottomSheetDialog.findViewById(R.id.tv_bottomsheet);
        Button boton_si = bottomSheetDialog.findViewById(R.id.button);
        Button boton_no = bottomSheetDialog.findViewById(R.id.button2);

        texto.setText("para ver su ubicacion active el servicio gps");

        boton_si.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= 23) {
                    if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        metodos.alerdialog_pedir_permisos(getActivity(), 3);
                    } else {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        bottomSheetDialog.dismiss();
                    }
                } else {
                    startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    bottomSheetDialog.dismiss();
                }
            }
        });

        boton_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.dismiss();
            }
        });
    }

    private void permiso_escribir_memoria_externa() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                metodos.alerdialog_pedir_permisos(getActivity(), 0);
            }
        }
    }
}

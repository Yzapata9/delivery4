package com.venta_productos.delivery.adapter;

import android.content.Context;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.venta_productos.delivery.R;

import java.util.ArrayList;

/**
 * Created by windows hdrp on 13/03/2017.
 */

public class adaptador_grid_ver_progreso_pedido extends BaseAdapter {

    private String input_string[];

    private Context context;

    boolean me_ejecute = false;

    String TAG = "asdf";

    public adaptador_grid_ver_progreso_pedido(Context context, String input_string[]) {
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
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            gridview = inflater.inflate(R.layout.row_progreso_del_pedido, null);
        }

        if (!me_ejecute) {
            me_ejecute = true;

            //€€€ divide el estado y el delivery de los negocios y sus productos
            //€€ divide los diferentes negocios
            //€ divide los productos de los negocios

            // 1 - €€€ divide el estado y el delivery de los negocios y sus productos
            String[] input_datos = input_string[position].split("€€€");

            String str_delivery = input_datos[0];
            String str_estado = input_datos[1];

            TextView tv_delivery = gridview.findViewById(R.id.tv_row_progreso_pedido_delivery);
            TextView tv_delivery_estado = gridview.findViewById(R.id.tv_row_estado_delivery);
            TableLayout tableLayout = gridview.findViewById(R.id.layout_tablerow);

            tv_delivery.setText(str_delivery);
            tv_delivery_estado.setText(str_estado);

            // 2 - €€ divide los diferentes negocios

            //obtengo los negocios
            String[] sty_negocios_involucrados = input_datos[2].split("€€");
            int int_cantidad_de_negocios = sty_negocios_involucrados.length;
            // y los divido

            //creo los params
            TableRow.LayoutParams params_del_row_texto = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
            TableRow.LayoutParams params_del_row = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 1.0f);
            LinearLayout.LayoutParams params_del_linear = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
            TableLayout.LayoutParams params_del_tablelayout = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT, 1.0f);
            LinearLayout.LayoutParams params_del_relative_parent = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            LinearLayout.LayoutParams params_del_linear_para_el_relative_2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2);

            int no_mostrar_la_separacion_en_el_ultimo_negocio = 0;
            for (String input_negocios_involucrados : sty_negocios_involucrados) {
                no_mostrar_la_separacion_en_el_ultimo_negocio++;
                //obtengo los datos de los negocios
                String[] input_negocios = input_negocios_involucrados.split("€");
                String negocio_nombre = input_negocios[0];
                String estado_negocio = input_negocios[1];
                String negocio_productos = input_negocios[2];

                String str_productos = null;

                String substring_del_array = negocio_productos.substring(1, negocio_productos.length()); // obtengo el string limpio de []
                String[] pedido_productos = substring_del_array.split(","); // divido para obtener los productos
                int int_cantidad_de_productos = pedido_productos.length - 1; //este menos 1 es para sacar el ultimo child que es el estado del negocio ,que no lo necesito

                for (int i = 0; i < int_cantidad_de_productos; i++) {
                    ArrayList<String> array_productos = new ArrayList<>();
                    array_productos.add(pedido_productos[i].replace("Producto= ", "")); //limpia el string de "Producto="
                    for (int p = 0; p < array_productos.size(); p++) {
                        String[] cortar_producto_y_precio = array_productos.get(p).split("Precio="); //limpia el string del "Precio= int"
                        String str_numero = String.valueOf(i + 1);
                        String dato_producto = str_numero + "- " + cortar_producto_y_precio[0] + "\n";
                        if (str_productos == null) {
                            str_productos = dato_producto;
                        } else {
                            str_productos = str_productos + dato_producto;
                        }
                    }
                }

                final LinearLayout relative_parent = new LinearLayout(context);
                relative_parent.setLayoutParams(params_del_tablelayout);
                relative_parent.setOrientation(LinearLayout.VERTICAL);

                //tablerows
                final TableRow table_row_agregar_table_row_negocio_estado = new TableRow(context);
                table_row_agregar_table_row_negocio_estado.setLayoutParams(params_del_relative_parent);

                final TableRow table_row_agregar_table_row_productos = new TableRow(context);
                table_row_agregar_table_row_productos.setLayoutParams(params_del_relative_parent);

                //linears
                LinearLayout linear_negocio_estado = new LinearLayout(context);
                linear_negocio_estado.setLayoutParams(params_del_row);
                linear_negocio_estado.setWeightSum(2);
                linear_negocio_estado.setOrientation(LinearLayout.HORIZONTAL);

                LinearLayout linear_negocio = new LinearLayout(context);
                linear_negocio.setLayoutParams(params_del_linear);
                linear_negocio.setWeightSum(2);
                linear_negocio.setOrientation(LinearLayout.VERTICAL);

                LinearLayout linear_estado = new LinearLayout(context);
                linear_estado.setLayoutParams(params_del_linear);
                linear_estado.setWeightSum(2);
                linear_estado.setOrientation(LinearLayout.VERTICAL);

                LinearLayout linear_productos = new LinearLayout(context);
                linear_productos.setLayoutParams(params_del_row);
                linear_productos.setWeightSum(2);
                linear_productos.setOrientation(LinearLayout.VERTICAL);


                //textos
                TextView tv_negocio_texto = new TextView(context);
                tv_negocio_texto.setLayoutParams(params_del_linear);
                tv_negocio_texto.setGravity(Gravity.CENTER);
                tv_negocio_texto.setText("Negocio=");
                tv_negocio_texto.setTextAppearance(context, R.style.texview_inflado_al_tablelayout);
                tv_negocio_texto.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                tv_negocio_texto.setPadding(0, 3, 0, 3);

                TextView tv_negocio = new TextView(context);
                tv_negocio.setLayoutParams(params_del_linear);
                tv_negocio.setGravity(Gravity.CENTER);
                tv_negocio.setText(negocio_nombre);
                tv_negocio.setTextAppearance(context, R.style.texview_inflado_al_tablelayout);
                tv_negocio.setPadding(0, 3, 0, 3);

                TextView tv_estado_texto = new TextView(context);
                tv_estado_texto.setLayoutParams(params_del_linear);
                tv_estado_texto.setGravity(Gravity.CENTER);
                tv_estado_texto.setText("Estado=");
                tv_estado_texto.setTextAppearance(context, R.style.texview_inflado_al_tablelayout);
                tv_estado_texto.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                tv_estado_texto.setPadding(0, 3, 0, 3);

                TextView tv_estado = new TextView(context);
                tv_estado.setLayoutParams(params_del_linear);
                tv_estado.setGravity(Gravity.CENTER);
                tv_estado.setText(estado_negocio);
                tv_estado.setTextAppearance(context, R.style.texview_inflado_al_tablelayout);
                tv_estado.setPadding(0, 3, 0, 3);


                TextView tv_productos_texto = new TextView(context);
                tv_productos_texto.setLayoutParams(params_del_row_texto);
                tv_productos_texto.setGravity(Gravity.CENTER);
                tv_productos_texto.setText("Productos=");
                tv_productos_texto.setTextAppearance(context, R.style.texview_inflado_al_tablelayout);
                tv_productos_texto.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                tv_productos_texto.setPadding(0, 3, 0, 3);

                TextView tv_productos = new TextView(context);
                tv_productos.setLayoutParams(params_del_row);
//                tv_productos.setGravity(Gravity.CENTER);
                tv_productos.setText(str_productos);
                tv_productos.setTextAppearance(context, R.style.texview_inflado_al_tablelayout);
                tv_productos.setPadding(23, 0, 0, 23);

                //relative
                RelativeLayout linear_separar_2 = new RelativeLayout(context);
                linear_separar_2.setLayoutParams(params_del_linear_para_el_relative_2);
                linear_separar_2.setBackgroundResource(R.color.blanco);


                //AGREGO LAS VIEWS

                //junto los 2 textview al linear
                linear_negocio.addView(tv_negocio_texto);
                linear_negocio.addView(tv_negocio);
                linear_estado.addView(tv_estado_texto);
                linear_estado.addView(tv_estado);
                linear_productos.addView(tv_productos_texto);
                linear_productos.addView(tv_productos);

                //agrego al linear negocio y estado
                linear_negocio_estado.addView(linear_negocio);
                linear_negocio_estado.addView(linear_estado);


                //agrego a los rows
                table_row_agregar_table_row_negocio_estado.addView(linear_negocio_estado);
                table_row_agregar_table_row_productos.addView(linear_productos);

                //agrego tod o al parent
                relative_parent.addView(table_row_agregar_table_row_negocio_estado);
                relative_parent.addView(table_row_agregar_table_row_productos);

                if (no_mostrar_la_separacion_en_el_ultimo_negocio != int_cantidad_de_negocios) {
                    relative_parent.addView(linear_separar_2);
                }

                //agrego al table layout
                tableLayout.addView(relative_parent);

            }
        }
        return gridview;

    }
}

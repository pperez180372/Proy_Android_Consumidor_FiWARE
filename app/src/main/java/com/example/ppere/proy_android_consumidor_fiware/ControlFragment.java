package com.example.ppere.proy_android_consumidor_fiware;

import android.support.v4.app.Fragment;
//import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

/**
 * Created by ppere on 31/10/2017.
 */

public class ControlFragment extends Fragment {

    ToggleButton Botonsubscribe;
    Button BotonActualiza;
    ToggleButton LED1, LED2, LED3, LED4;
    ToggleButton BotonSTEELSKIN;
    SeekBar SeekBarVelocity;

    View fragmentview=null;


    Paint paintred;
    Paint paintwhite;
    BitmapDrawable BitmapD_1;
    BitmapDrawable BitmapD_2;
    int mx, Mx, my, My;
    int bmpdx = 200, bmpdy = 100;
    float[] buffer = new float[3600];
    int puntero_vector = 0;
    int num_muestras = 0;
    Canvas grafica_1;
    Canvas grafica_2;
    int canvas_visible;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        fragmentview=null;
        final View view=inflater.inflate(R.layout.control_layout, container, false);

        /* Datos del dispositivo */

        TextView tx = (TextView) view.findViewById(R.id.textView_user_name);
        tx.setText("alopez");
        tx = (TextView) view.findViewById(R.id.textView_device_name);
        tx.setText("SensorSEU27");
        tx = (TextView) view.findViewById(R.id.textView_service);
        tx.setText("ciudad_seu_1718");
        tx = (TextView) view.findViewById(R.id.textView_subservice);
        tx.setText("/distritonorte");

        tx = (TextView) view.findViewById(R.id.textView_passwd);
        tx.setText("alopezSEU");


        paintred = new Paint();
        paintred.setColor(Color.parseColor("#CD5C5C"));
        paintred.setStyle(Paint.Style.FILL_AND_STROKE);
        paintred.setAntiAlias(false);
        paintwhite = new Paint();
        paintwhite.setColor(Color.parseColor("#FFFFFF"));

        my = 15; // temperatura minima;
        My = 40; //temperatura máxima;
        mx = 0;  // tiempo minimo; en segundos // dependerá del numero de muestras
        Mx = 60;




        canvas_visible = 1;
        FrameLayout ll = (FrameLayout) view.findViewById(R.id.Grafica);
        ll.setBackgroundDrawable(BitmapD_1);

        ((MainActivity)getActivity()).setFragment(this);

        SeekBarVelocity = (SeekBar) view.findViewById(R.id.seekBar_Velocity);
        SeekBarVelocity.setOnSeekBarChangeListener ( new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                String user_name = ((TextView) (view.findViewById(R.id.textView_user_name))).getText().toString();
                String user_passwd = ((TextView) (view.findViewById(R.id.textView_passwd))).getText().toString();
                String device_name = ((TextView) (view.findViewById(R.id.textView_device_name))).getText().toString();
                String attribute_name = ((TextView) (view.findViewById(R.id.textView_rr_Velocity_command))).getText().toString();
                String service = ((TextView) (view.findViewById(R.id.textView_service))).getText().toString();
                String subservice = ((TextView) (view.findViewById(R.id.textView_subservice))).getText().toString();


                if (((MainActivity)getActivity()).IPL!=null) {

                    ((MainActivity)getActivity()).RunningUpdateVelocityContextTask(user_name,user_passwd,device_name,attribute_name,service,subservice,""+progress);
                    ((MainActivity)getActivity()).RunningQueryContextTask_INFO(user_name,user_passwd,device_name,attribute_name+"_info",service,subservice,"INFO",""+R.id.textView_velocity_Info);
                    ((MainActivity)getActivity()).RunningQueryContextTask_INFO(user_name,user_passwd,device_name,attribute_name+"_status",service,subservice,"RESULT",""+R.id.textView_velocity_status);
                }
                else {

                    ((MainActivity)getActivity()).imprimirln("No hay IP asignada (compruebe la conexión IP)");

                }



            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                System.out.println("progress"+seekBar);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                System.out.println("progress"+seekBar);
            }
        });

        BotonSTEELSKIN =(ToggleButton) view.findViewById(R.id.button_LOGIN);

        BotonSTEELSKIN.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                String user_name = ((TextView) (view.findViewById(R.id.textView_user_name))).getText().toString();
                String user_passwd = ((TextView) (view.findViewById(R.id.textView_passwd))).getText().toString();
                String device_name = ((TextView) (view.findViewById(R.id.textView_device_name))).getText().toString();
                String service = ((TextView) (view.findViewById(R.id.textView_service))).getText().toString();
                String subservice = ((TextView) (view.findViewById(R.id.textView_subservice))).getText().toString();

                if (((MainActivity)getActivity()).IPL!=null) {
                    ToggleButton bt = (ToggleButton) v;
                    if ((bt.isChecked()))
                        ((MainActivity)getActivity()).RunningLoginTask(user_name,user_passwd,device_name,service,subservice);
                    else
                        (((MainActivity)getActivity())).usertoken="";
                }else {
                    ((MainActivity)getActivity()).imprimirln("No hay IP asignada (compruebe la conexión IP)");
                }
                // cuando termine de ejecutarse la clase será destruida si ya no tiene referencias, por ejemplo la primera de dos ejecuciones.
            } // del if


            ;
        });


        Botonsubscribe = (ToggleButton) view.findViewById(R.id.buttonSubscribe);
        Botonsubscribe.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                String user_name = ((TextView) (view.findViewById(R.id.textView_user_name))).getText().toString();
                String user_passwd = ((TextView) (view.findViewById(R.id.textView_passwd))).getText().toString();
                String device_name = ((TextView) (view.findViewById(R.id.textView_device_name))).getText().toString();
                String attribute_name = ((TextView) (view.findViewById(R.id.textView_temperature_name_attribute))).getText().toString();
                String service = ((TextView) (view.findViewById(R.id.textView_service))).getText().toString();
                String subservice = ((TextView) (view.findViewById(R.id.textView_subservice))).getText().toString();

               if (((MainActivity)getActivity()).IPL!=null) {
                   ToggleButton bt = (ToggleButton) v;
                   if (!(bt.isChecked()))
                       ((MainActivity)getActivity()).RunningUnSuscribeTask(user_name,user_passwd,device_name,attribute_name,service,subservice);
                        else
                       ((MainActivity)getActivity()).RunningSuscribeTask(user_name,user_passwd,device_name,attribute_name,service,subservice);
                }
                else {
                    ((MainActivity)getActivity()).imprimirln("No hay IP asignada (compruebe la conexión IP)");
                }
                // cuando termine de ejecutarse la clase será destruida si ya no tiene referencias, por ejemplo la primera de dos ejecuciones.
            } // del if


            ;
        });

        BotonActualiza= (Button) view.findViewById(R.id.buttonActualize);
        BotonActualiza.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                String user_name = ((TextView) (view.findViewById(R.id.textView_user_name))).getText().toString();
                String user_passwd = ((TextView) (view.findViewById(R.id.textView_passwd))).getText().toString();
                String device_name = ((TextView) (view.findViewById(R.id.textView_device_name))).getText().toString();
                String attribute_name = ((TextView) (view.findViewById(R.id.textView_temperature_name_attribute))).getText().toString();
                String service = ((TextView) (view.findViewById(R.id.textView_service))).getText().toString();
                String subservice = ((TextView) (view.findViewById(R.id.textView_subservice))).getText().toString();
                if (((MainActivity)getActivity()).IPL!=null) {

                    ((MainActivity)getActivity()).RunningQueryContextTask(user_name,user_passwd,device_name,attribute_name,service,subservice);
                }
                else {

                    ((MainActivity)getActivity()).imprimirln("No hay IP asignada (compruebe la conexión IP)");

                }
                // cuando termine de ejecutarse la clase será destruida si ya no tiene referencias, por ejemplo la primera de dos ejecuciones.
            } // del if


            ;
        });

        LED1 = (ToggleButton) view.findViewById(R.id.buttonLED1);
        LED1.setOnClickListener(new View.OnClickListener() { public void onClick(View v) {

            String user_name = ((TextView) (view.findViewById(R.id.textView_user_name))).getText().toString();
            String user_passwd = ((TextView) (view.findViewById(R.id.textView_passwd))).getText().toString();
            String device_name = ((TextView) (view.findViewById(R.id.textView_device_name))).getText().toString();
            String attribute_name = ((TextView) (view.findViewById(R.id.textView_rr_led_command))).getText().toString();
            String service = ((TextView) (view.findViewById(R.id.textView_service))).getText().toString();
            String subservice = ((TextView) (view.findViewById(R.id.textView_subservice))).getText().toString();

            sendLED(user_name,user_passwd, device_name,attribute_name,service,subservice);

        }});
        LED2 = (ToggleButton) view.findViewById(R.id.buttonLED2);
        LED2.setOnClickListener(new View.OnClickListener() {  public void onClick(View v) {
            String user_name = ((TextView) (view.findViewById(R.id.textView_user_name))).getText().toString();
            String user_passwd = ((TextView) (view.findViewById(R.id.textView_passwd))).getText().toString();
            String device_name = ((TextView) (view.findViewById(R.id.textView_device_name))).getText().toString();
            String attribute_name = ((TextView) (view.findViewById(R.id.textView_rr_led_command))).getText().toString();
            String service = ((TextView) (view.findViewById(R.id.textView_service))).getText().toString();
            String subservice = ((TextView) (view.findViewById(R.id.textView_subservice))).getText().toString();
            sendLED(user_name,user_passwd, device_name,attribute_name,service,subservice);

        }   });
        LED3 = (ToggleButton) view.findViewById(R.id.buttonLED3);
        LED3.setOnClickListener(new View.OnClickListener() {  public void onClick(View v) {
            String user_name = ((TextView) (view.findViewById(R.id.textView_user_name))).getText().toString();
            String user_passwd = ((TextView) (view.findViewById(R.id.textView_passwd))).getText().toString();
            String device_name = ((TextView) (view.findViewById(R.id.textView_device_name))).getText().toString();
            String attribute_name = ((TextView) (view.findViewById(R.id.textView_rr_led_command))).getText().toString();
            String service = ((TextView) (view.findViewById(R.id.textView_service))).getText().toString();
            String subservice = ((TextView) (view.findViewById(R.id.textView_subservice))).getText().toString();
            sendLED(user_name, user_passwd, device_name, attribute_name, service, subservice);}   });
        LED4 = (ToggleButton) view.findViewById(R.id.buttonLED4);
        LED4.setOnClickListener(new View.OnClickListener() {  public void onClick(View v) {
            String user_name = ((TextView) (view.findViewById(R.id.textView_user_name))).getText().toString();
            String user_passwd = ((TextView) (view.findViewById(R.id.textView_passwd))).getText().toString();
            String device_name = ((TextView) (view.findViewById(R.id.textView_device_name))).getText().toString();
            String attribute_name = ((TextView) (view.findViewById(R.id.textView_rr_led_command))).getText().toString();
            String service = ((TextView) (view.findViewById(R.id.textView_service))).getText().toString();
            String subservice = ((TextView) (view.findViewById(R.id.textView_subservice))).getText().toString();
            sendLED(user_name, user_passwd, device_name, attribute_name, service, subservice);}   });




        fragmentview=view;

        return view;
    }

    void sendLED(String user_name, String user_passwd, String device_name, String attribute_name, String service, String subservice)
    {
        String L1, L2, L3, L4;

        if (((MainActivity)getActivity()).IPL==null)
            ((MainActivity)getActivity()).imprimirln("No hay IP asignada (compruebe la conexión IP)");

        if (fragmentview!=null) {
            ToggleButton LED1=((ToggleButton) (fragmentview.findViewById(R.id.buttonLED1)));
            ToggleButton LED2=((ToggleButton) (fragmentview.findViewById(R.id.buttonLED2)));
            ToggleButton LED3=((ToggleButton) (fragmentview.findViewById(R.id.buttonLED3)));
            ToggleButton LED4=((ToggleButton) (fragmentview.findViewById(R.id.buttonLED4)));
            String nameentity=((TextView) (fragmentview.findViewById(R.id.textView_device_name))).getText().toString();

            if (LED1.isChecked())
                L1="1";
            else
                L1="0";

            if (LED2.isChecked())
                L2="1";
            else
                L2="0";
            if (LED3.isChecked())
                L3="1";
            else
                L3="0";
            if (LED4.isChecked())
                L4="1";
            else
                L4="0";

            ((MainActivity)getActivity()).RunningUpdateLEDTask(user_name,user_passwd,device_name,attribute_name,service,subservice, L1, L2, L3, L4);
    // no da tiempo a que se actualicen los estados a veces no está actualizado
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            ((MainActivity)getActivity()).RunningQueryContextTask_INFO(user_name,user_passwd,device_name,attribute_name+"_info",service,subservice,"INFO",""+R.id.textView_rr_led_name_info);
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            ((MainActivity) getActivity()).RunningQueryContextTask_INFO(user_name, user_passwd, device_name, attribute_name + "_status", service, subservice, "RESULT", "" + R.id.textView_rr_led_result);

        }
    }

    /* Funciones utiles */

    // añade un dato a la gŕafica

public void setSUBSCRIPTIONID(String id)
{
    if (fragmentview!=null) {
        TextView tv =(TextView) (fragmentview.findViewById(R.id.textView_temperature_subscription_id));
        tv.setText(id);


    }
}


    public String getSUBSCRIPTIONID()
    {
        if (fragmentview!=null) {
            TextView tv =(TextView) (fragmentview.findViewById(R.id.textView_temperature_subscription_id));
            return (String) tv.getText();

        }
        else
            return "Error en el fragment";
    }

    public void nuevamuestra(float dato) {

        final float datum = dato;
        final int num_muestrum = num_muestras++;

        if (fragmentview!=null) {
            FrameLayout ly =(FrameLayout) (fragmentview.findViewById(R.id.Grafica));
            bmpdy=ly.getHeight();
            bmpdx=ly.getWidth();

            TextView tv =(TextView) (fragmentview.findViewById(R.id.textView_temperature_name_attribute_value));
            tv.setText(""+dato);


        }

        Bitmap bg_1 = Bitmap.createBitmap(bmpdx, bmpdy, Bitmap.Config.ARGB_8888);
        Bitmap bg_2 = Bitmap.createBitmap(bmpdx, bmpdy, Bitmap.Config.ARGB_8888);
        grafica_1 = new Canvas(bg_1);
        grafica_2 = new Canvas(bg_2);
        BitmapD_1 = new BitmapDrawable(getResources(), bg_1);
        BitmapD_2 = new BitmapDrawable(getResources(), bg_2);

        Canvas grafica;
        canvas_visible = (canvas_visible + 1) % 2;
        if (canvas_visible == 1)
            grafica = grafica_1;
        else
            grafica = grafica_2;
        // borrar esta escalado.
        grafica.drawRect(0, 0, bmpdx, bmpdy, paintwhite);
        float sy = (float) (bmpdy / (My - my)); // en grados
        float sx = (float) bmpdx / (Mx - mx);   // en segundos

        //añadir dato
        buffer[puntero_vector++] = dato;
        if ((puntero_vector >= Mx)) {

            puntero_vector = 0;
        }

        {
            // grafica de lineas no se escala la x
            int t;
            for (t = 0; t < Mx - 1; t++) {
                float va, va1;
                va = buffer[t];
                if (va > My) va = My;
                if (va < my) va = my;
                va1 = buffer[t + 1];
                if (va1 > My) va1 = My;
                if (va1 < my) va1 = my;
                float x1, y1, x2, y2;

                x1 = t * sx;
                y1 = bmpdy - (va - my) * sy;
                x2 = (t + 1) * sx;
                y2 = bmpdy - (va1 - my) * sy;

                grafica.drawLine(x1, y1, x2, y2, paintred);

            }

            if (fragmentview!=null)
            if (canvas_visible == 1) {

                FrameLayout ll = ((FrameLayout)  fragmentview.findViewById(R.id.Grafica));
                ll.setBackgroundDrawable(BitmapD_1);

                /*
                runOnUiThread(new Runnable() {
                    public void run() {

                        FrameLayout ll = ((FrameLayout)  vv.findViewById(R.id.Grafica));
                        ll.setBackgroundDrawable(BitmapD_1);

                    }
                });
*/
            } else {
                FrameLayout ll = ((FrameLayout) fragmentview.findViewById(R.id.Grafica));
                ll.setBackgroundDrawable(BitmapD_2);

/*                runOnUiThread(new Runnable() {
                    public void run() {
                        FrameLayout ll = ((FrameLayout) vv.findViewById(R.id.Grafica));
                        ll.setBackgroundDrawable(BitmapD_2);

                    }
                });
                */
            }
        }


    }


}

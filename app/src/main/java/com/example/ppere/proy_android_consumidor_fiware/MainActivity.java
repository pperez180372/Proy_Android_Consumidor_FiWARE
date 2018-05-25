package com.example.ppere.proy_android_consumidor_fiware;


import android.support.v4.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;


import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.ProtocolException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import com.example.ppere.proy_android_consumidor_fiware.*;


class HTTPheader {
    String cad;
HTTPheader(String info)
{
    cad=info;
}
String getHeader(String clave)
{
        int idx=cad.indexOf(clave);

        if (idx!=-1)
        {
            int idxe=cad.indexOf("\n",idx);
            int idxv=cad.indexOf(":",idx);
            if ((idxe==-1)||(idxv==-1)) return "";
            return cad.substring(idxv+1,idxe);
        }
        return "";


}
}

public class MainActivity extends FragmentActivity {
    private FragmentTabHost tabHost;

    private ControlFragment CF=null;

    public int FragmentControlId=-1;
    public int FragmentStatusId=-1;

    String LOG;


    MainActivity myself;

    /* autenticate */
    public String usertoken = "";
    String userdomain ="";


    boolean SubsOngoing=false;
    String Subsnameentity;
    String Subsnameattribute;



    //Addressing

    InetAddress IPL=null;
    // int puerto_de_escucha = 54545;
    int IPPORT=6070;
    ServerSocket sk;




    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        myself = this;

        tabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        tabHost.setup(this, getSupportFragmentManager(), android.R.id.tabcontent);

        // Set the Tab name and Activity
        // that will be opened when particular Tab will be selected

        tabHost.addTab(tabHost.newTabSpec("tab1").setIndicator("Controls"), ControlFragment.class, null);
        tabHost.addTab(tabHost.newTabSpec("tab2").setIndicator("Status"), StatusFragment.class, null);


        tabHost.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {

            @Override
            public void onViewDetachedFromWindow(View v) {}

            @Override
            public void onViewAttachedToWindow(View v) {
                tabHost.getViewTreeObserver().removeOnTouchModeChangeListener(tabHost);
            }
        });



// thread para almacenar la IP asignada
        new Thread(new Runnable() {
            public void run() {

                int i=0;
                while(true)
                {
                    IPL = getLocalAddress();
                    try {
                        // imprimirln("Linea "+i);
                        i++;
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        }

        ).start();





    // hilo de subscripción se crea y se queda dormido esperando conexiones la IP debe ser pública o accesible

        new Thread(new Runnable() {
            String SubsID;
            public void run() {

                while (true) {
                    try {
                        while (true) {
        //  java.net.InetAddress addr = java.net.InetAddress.getLocalHost();
        //                String g=addr.getHostAddress();
                            InetAddress g1 = getLocalAddress();
                            sk = new ServerSocket(IPPORT, 0, g1);
                            StringBuilder sb = null, sbj = null;

                            if (CF!=null) SubsID=CF.getSUBSCRIPTIONID();

                            while (!sk.isClosed()) {
                                sbj = null;
                                sb = null;
                                Socket cliente = sk.accept();
                                if (SubsOngoing) {
                                    cliente.close();
                                    continue;
                                }
                                BufferedReader entrada = new BufferedReader(
                                        new InputStreamReader(cliente.getInputStream()));
                                PrintWriter salida = new PrintWriter(
                                        new OutputStreamWriter(cliente.getOutputStream()), true);
                                String line = null;
                                sb = new StringBuilder();
                                try {
                                    do {
                                        line = entrada.readLine();
                                        sb.append(line + "\n");
                                        //hacking
                                        if ((sbj == null) && (line.contains("{"))) // date
                                            sbj = new StringBuilder();
                                        if (sbj != null) {
                                            sbj.append(line + "\n");
                                        }
                                        System.out.println("" + sb);
                                    }
                                    while (entrada.ready());
                                } catch (IOException e1) {
                                    e1.printStackTrace();
                                }
                                //System.out.println("Fin de lectura");
                                JSONObject jObject = null;
                                try {
                                    jObject = new JSONObject(sbj.toString());


                                    String IDSubs_rcv = jObject.getString("subscriptionId");
                                    imprimirln("new conection: " + IDSubs_rcv);
                                    if (CF!=null) SubsID=CF.getSUBSCRIPTIONID();
                                    if (SubsID != null) {
                                        if ((!(SubsID.matches(IDSubs_rcv))))
                                        {
                                        imprimirln("Subscripcion Act: " + SubsID);

                                            imprimirln("eliminar subscripción " + IDSubs_rcv + "   " + sb);
                                            HTTPheader cl=new HTTPheader(sb.substring(0));
                                            String s1, s2;
                                            s1=cl.getHeader("fiware-service");
                                            s2=cl.getHeader("Fiware-ServicePath");
                                            RunningCleanUnSuscribeTask(s1, s2, IDSubs_rcv);
                                        }

                                        }



                                    JSONArray jArray = jObject.getJSONArray("contextResponses");
                                    JSONObject c = jArray.getJSONObject(0);
                                    JSONObject jo = c.getJSONObject("contextElement");
                                    String name = jo.getString("id");


                                    if (Subsnameentity != null) {

                                        if (name.contains(Subsnameentity)) {

                                            JSONArray jArrayattr = jo.getJSONArray("attributes");
                                            for (int i = 0; i < jArrayattr.length(); i++) {

                                                    JSONObject ca = jArrayattr.getJSONObject(i);

                                                    if (ca.getString("name").contains(Subsnameattribute)) {

                                                        final double va = ca.getDouble("value");
                                                        nuevamuestra((float) va);

                                                      }
                                            } // del for
                                        }
                                    }


                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }


                                salida.println("POST HTTP/1.1 200");
                                cliente.close();
                            }
                            Thread.sleep(1000);

                        }
                    } catch (IOException e) {
                        System.out.println(e);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }


                }
                }

        }
    ).start();

        };


    public void setFragment(ControlFragment fg)
        {
            CF=fg;
        }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
       }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.

    }

    /*****************************************************************************************************************************************************************************************
     * ****************************************************************************************************************************************************************************************
     * Obtener el token de usuario una vez autenticado
     * ****************************************************************************************************************************************************************************************
     * ****************************************************************************************************************************************************************************************
     * ****************************************************************************************************************************************************************************************
     */
    public void RunningLoginTask(String... urls)
    {
        LoginTask ay=new MainActivity.LoginTask();
        ay.execute(urls);
    }


    class LoginTask extends AsyncTask<String, Void, String> {

        String res;

        protected String doInBackground(String... urls) {

            Map<String, List<String>> rr;
            String res = "";
            InputStream is = null;
            // Only display the first 500 characters of the retrieved
            // web page content.
            int len = 500;
            int pos;

            pos = 0;
            String username = (String) urls[0];
            String domainname = (String) urls[1];

            String passwd = (String) urls[2];
            imprimirln(username + "(" + domainname + ")=" + passwd);

            String payload = "{ \"auth\": {\"identity\": {\"methods\": [\"password\"],\"password\": {" +
                    "\"user\": {\"name\": \"" + username + "\",\"domain\": { \"name\": \"" + domainname + "\" }," +
                    "\"password\": \"" + passwd + "\"}}}}}";



            String HeaderAccept = "application/json";
            String HeaderContent = "application/json";
            String leng = null;
            String resp = "none";

            try {
                leng = Integer.toString(payload.getBytes("UTF-8").length);

                OutputStreamWriter wr = null;
                BufferedReader rd = null;
                StringBuilder sb = null;

                URL url = null;
                url = new URL("http://pperez-seu-ks.disca.upv.es:5000/v3/auth/tokens");

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000); // miliseconds
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");

                conn.setRequestProperty("Accept", HeaderAccept);
                conn.setRequestProperty("Content-type", HeaderContent);
                //conn.setRequestProperty("Fiware-Service", HeaderService);
                conn.setRequestProperty("Content-Length", leng);
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                os.write(payload.getBytes("UTF-8"));
                os.flush();
                os.close();


                int rc = conn.getResponseCode();

                resp = conn.getContentEncoding();
                is = conn.getInputStream();

                if (rc == 201) {
                    String token = conn.getHeaderField("X-Subject-Token");

                    System.out.println("Token: " + token);
                    ponTextoTextView(token, R.id.textView_token);

                } else {
                    resp = "locaL: ERROR de conexión";
                    System.out.println("http response code error: " + rc + "\n");
                    ponTextoTextView("no hay token", R.id.textView_token);

                }



            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                usertoken = "";
                ponTextoTextView("IOExcep no hay token", R.id.textView_token);
                e.printStackTrace();
            }
            if (usertoken.length() == 0) {

                runOnUiThread(new Runnable() {
                    public void run() {
                        ToggleButton bt = (ToggleButton) findViewById(R.id.button_LOGIN);
                        bt.setChecked(false);
                    }
                });
            }
            return resp;
        }


    }






    private static InetAddress getLocalAddress() {
        InetAddress inetAddr = null;

        //
        // 1) If the property java.rmi.server.hostname is set and valid, use it
        //

        try {
            //System.out.println("Attempting to resolve java.rmi.server.hostname");
            String hostname = System.getProperty("java.rmi.server.hostname");
            if (hostname != null) {
                inetAddr = InetAddress.getByName(hostname);
                if (!inetAddr.isLoopbackAddress()) {
                    return inetAddr;
                } else {
                    //System.out                     .println("java.rmi.server.hostname is a loopback interface.");
                }

            }
        } catch (SecurityException e) {
            System.out                    .println("Caught SecurityException when trying to resolve java.rmi.server.hostname");
        } catch (UnknownHostException e) {
            System.out
                    .println("Caught UnknownHostException when trying to resolve java.rmi.server.hostname");
        }

        // 2) Try to use InetAddress.getLocalHost
        try {
            //System.out                    .println("Attempting to resolve InetADdress.getLocalHost");
            InetAddress localHost = null;
            localHost = InetAddress.getLocalHost();
            if (!localHost.isLoopbackAddress()) {
                return localHost;
            } else {
                //System.out                        .println("InetAddress.getLocalHost() is a loopback interface.");
            }

        } catch (UnknownHostException e1) {
            System.out                    .println("Caught UnknownHostException for InetAddress.getLocalHost()");
        }

        // 3) Enumerate all interfaces looking for a candidate
        Enumeration ifs = null;
        try {
            //System.out                    .println("Attempting to enumerate all network interfaces");
            ifs = NetworkInterface.getNetworkInterfaces();

            // Iterate all interfaces
            while (ifs.hasMoreElements()) {
                NetworkInterface iface = (NetworkInterface) ifs.nextElement();

                // Fetch all IP addresses on this interface
                Enumeration ips = iface.getInetAddresses();

                // Iterate the IP addresses
                while (ips.hasMoreElements()) {
                    InetAddress ip = (InetAddress) ips.nextElement();
                    if ((ip instanceof Inet4Address) && !ip.isLoopbackAddress()) {
                        return (InetAddress) ip;
                    }
                }
            }
        } catch (SocketException se) {
            System.out.println("Could not enumerate network interfaces");
        }

        // 4) Epic fail
        //System.out                .println("Failed to resolve a non-loopback ip address for this host.");
        return null;
    }





    synchronized void ponTextoTextView(final String cad, final int id) {
        final String g = cad;

        runOnUiThread(new Runnable() {
            public void run() {
                TextView ll = (TextView) findViewById(id);
                if (ll != null) ll.setText(cad);
            }
        });
    }

    synchronized void imprimir(final String cad) {
        final String g = cad;

        LOG=LOG+cad;

    }

    public synchronized  String getLOG() {return LOG;};


    public void imprimirln(final String cad) {
        imprimir(cad + "\r\n");
    };

    public void RunningQueryContextTask_INFO(String... urls)
    {
        QueryContextTask_INFO ay=new MainActivity.QueryContextTask_INFO();
        ay.execute(urls);
    }

    class QueryContextTask_INFO extends AsyncTask<String, Void, String> {

        String res;

        protected String doInBackground(String... urls) {

            Map<String, List<String>> rr;
            String res = "";

            InputStream is = null;

            // Only display the first 500 characters of the retrieved
            // web page content.
            int len = 500;

            String nameresource = (String) urls[0];
            int pos = nameresource.indexOf(".");// calling ay.execute(user_name,user_passwd,device_name,attribute_name,service,subservice );

            String user_name = (String) urls[0];
            String user_passwd = (String) urls[1];
            String device_name = (String) urls[2];
            String service = (String) urls[4];
            String subservice = (String) urls[5];
            String Attribute=(String) urls[3];
            String OP=(String) urls[6];
            String ID=(String) urls[7];


            String HeaderAccept = "application/json";
            String HeaderContent = "application/json";

            String payload = "{\"entities\": [{\"type\": \"sensoractuator\",\"isPattern\": \"false\",\"id\": \"" + device_name + "\"}]}";
            String leng = null;
            String resp = "none";
            imprimirln(payload);
            if (usertoken.length() == 0) {

                imprimirln("acceso Directo a Orion");


                try {
                    leng = Integer.toString(payload.getBytes("UTF-8").length);

                    OutputStreamWriter wr = null;
                    BufferedReader rd = null;
                    StringBuilder sb = null;

                    URL url = null;

                    url = new URL("http://pperez-seu-or.disca.upv.es:1026/v1/queryContext");

                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(10000); // miliseconds
                    conn.setConnectTimeout(15000);
                    conn.setRequestMethod("POST");

                    conn.setRequestProperty("Accept", HeaderAccept);
                    conn.setRequestProperty("Content-type", HeaderContent);
                    conn.setRequestProperty("fiware-service",service);
                    conn.setRequestProperty("fiware-servicepath", subservice);
                    conn.setRequestProperty("Content-Length", leng);
                    conn.setDoOutput(true);

                    OutputStream os = conn.getOutputStream();
                    os.write(payload.getBytes("UTF-8"));
                    os.flush();
                    os.close();


                    int rc = conn.getResponseCode();

                    resp = conn.getContentEncoding();
                    is = conn.getInputStream();

                    if (rc == 200) {

                        resp = "OK";
                        //read the result from the server
                        rd = new BufferedReader(new InputStreamReader(is));
                        sb = new StringBuilder();

                        String line = null;
                        while ((line = rd.readLine()) != null) {
                            sb.append(line + "\n");
                        }
                        String result = sb.toString();


                        JSONObject jObject = null;
                        try {
                            jObject = new JSONObject(result);

                            JSONArray jArray = jObject.getJSONArray("contextResponses");
                            JSONObject c = jArray.getJSONObject(0);
                            JSONObject jo = c.getJSONObject("contextElement");
                            JSONArray jArrayattr = jo.getJSONArray("attributes");

                            for (int i = 0; i < jArrayattr.length(); i++) {
                                try {
                                    JSONObject ca = jArrayattr.getJSONObject(i);
                                    imprimirln("" + ca.getString("name").contains(Attribute));
                                    if (ca.getString("name").contains(Attribute)) {
                                        int id=Integer.parseInt(ID);
                                        setTEXT(ca.getString("value"),id);
                                        imprimirln(device_name + "." + Attribute + "=" );

                                    }



                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            } // del for


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                        // cabeceras de recepcion
                        rr = conn.getHeaderFields();
                        System.out.println("headers: " + rr.toString());

                    } else {
                        resp = "ERROR de conexión";
                        System.out.println("http response code error: " + rc + "\n");

                    }


                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (ProtocolException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }


                return resp;
            } else {
                imprimirln("através de PEPPropxy");


                try {
                    leng = Integer.toString(payload.getBytes("UTF-8").length);

                    OutputStreamWriter wr = null;
                    BufferedReader rd = null;
                    StringBuilder sb = null;

                    URL url = null;

                    url = new URL("http://pperez-seu-or.disca.upv.es:1028/v1/queryContext");

                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(10000); // miliseconds
                    conn.setConnectTimeout(15000);
                    conn.setRequestMethod("POST");

                    conn.setRequestProperty("Accept", HeaderAccept);
                    conn.setRequestProperty("Content-type", HeaderContent);
                    //conn.setRequestProperty("Fiware-Service", HeaderService);
                    conn.setRequestProperty("Content-Length", leng);
                    conn.setRequestProperty("fiware-service",userdomain);
                    conn.setRequestProperty("fiware-servicepath", "/");
                    conn.setRequestProperty("x-auth-token", usertoken);
                    conn.setDoOutput(true);

                    OutputStream os = conn.getOutputStream();
                    os.write(payload.getBytes("UTF-8"));
                    os.flush();
                    os.close();


                    int rc = conn.getResponseCode();
                    imprimirln("RC "+rc);
                    resp = conn.getContentEncoding();
                    is = conn.getInputStream();
                    imprimirln(resp);
                    if (rc == 403) {

                        resp = "OK";
                        //read the result from the server
                        rd = new BufferedReader(new InputStreamReader(is));
                        sb = new StringBuilder();

                        String line = null;
                        while ((line = rd.readLine()) != null) {
                            sb.append(line + "\n");
                        }
                        String result = sb.toString();

                        imprimirln("result "+result);
                        JSONObject jObject = null;
                        try {
                            jObject = new JSONObject(result);

                            JSONArray jArray = jObject.getJSONArray("contextResponses");
                            JSONObject c = jArray.getJSONObject(0);
                            JSONObject jo = c.getJSONObject("contextElement");
                            JSONArray jArrayattr = jo.getJSONArray("attributes");

                            for (int i = 0; i < jArrayattr.length(); i++) {
                                try {
                                    JSONObject ca = jArrayattr.getJSONObject(i);
                                    imprimirln("" + ca.getString("name").contains(Attribute));
                                    if (ca.getString("name").contains(Attribute)) {

                                        final double va = ca.getDouble("value");
                                        //nuevamuestra((float) va,view);
                                        imprimirln(device_name + "." + Attribute + "=" + (float) va);

                                    }


                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            } // del for


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                        // cabeceras de recepcion
                        rr = conn.getHeaderFields();
                        System.out.println("headers: " + rr.toString());

                    } else {
                        resp = "ERROR de conexión";
                        System.out.println("http response code error: " + rc + "\n");

                    }


                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (ProtocolException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }


                return resp;
            }



        }
    }




    public void RunningQueryContextTask(String... urls)
    {
        QueryContextTask ay=new MainActivity.QueryContextTask();
        ay.execute(urls);
    }

    class QueryContextTask extends AsyncTask<String, Void, String> {

        String res;

        protected String doInBackground(String... urls) {

            Map<String, List<String>> rr;
            String res = "";

            InputStream is = null;

            // Only display the first 500 characters of the retrieved
            // web page content.
            int len = 500;

            String nameresource = (String) urls[0];
            int pos = nameresource.indexOf(".");// calling ay.execute(user_name,user_passwd,device_name,attribute_name,service,subservice );

            String user_name = (String) urls[0];
            String user_passwd = (String) urls[1];
            String device_name = (String) urls[2];
            String service = (String) urls[4];
            String subservice = (String) urls[5];
            String Attribute=(String) urls[3];


            String HeaderAccept = "application/json";
            String HeaderContent = "application/json";

            String payload = "{\"entities\": [{\"type\": \"sensoractuator\",\"isPattern\": \"false\",\"id\": \"" + device_name + "\"}]}";
            String leng = null;
            String resp = "none";
            imprimirln(payload);
            if (usertoken.length() == 0) {

                imprimirln("acceso Directo a Orion");


                try {
                    leng = Integer.toString(payload.getBytes("UTF-8").length);

                    OutputStreamWriter wr = null;
                    BufferedReader rd = null;
                    StringBuilder sb = null;

                    URL url = null;

                    url = new URL("http://pperez-seu-or.disca.upv.es:1026/v1/queryContext");

                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(10000); // miliseconds
                    conn.setConnectTimeout(15000);
                    conn.setRequestMethod("POST");

                    conn.setRequestProperty("Accept", HeaderAccept);
                    conn.setRequestProperty("Content-type", HeaderContent);
                    conn.setRequestProperty("fiware-service",service);
                    conn.setRequestProperty("fiware-servicepath", subservice);
                    conn.setRequestProperty("Content-Length", leng);
                    conn.setDoOutput(true);

                    OutputStream os = conn.getOutputStream();
                    os.write(payload.getBytes("UTF-8"));
                    os.flush();
                    os.close();


                    int rc = conn.getResponseCode();

                    resp = conn.getContentEncoding();
                    is = conn.getInputStream();

                    if (rc == 200) {

                        resp = "OK";
                        //read the result from the server
                        rd = new BufferedReader(new InputStreamReader(is));
                        sb = new StringBuilder();

                        String line = null;
                        while ((line = rd.readLine()) != null) {
                            sb.append(line + "\n");
                        }
                        String result = sb.toString();


                        JSONObject jObject = null;
                        try {
                            jObject = new JSONObject(result);

                            JSONArray jArray = jObject.getJSONArray("contextResponses");
                            JSONObject c = jArray.getJSONObject(0);
                            JSONObject jo = c.getJSONObject("contextElement");
                            JSONArray jArrayattr = jo.getJSONArray("attributes");

                            for (int i = 0; i < jArrayattr.length(); i++) {
                                try {
                                    JSONObject ca = jArrayattr.getJSONObject(i);
                                    imprimirln("" + ca.getString("name").contains(Attribute));
                                    if (ca.getString("name").contains(Attribute)) {

                                        final double va = ca.getDouble("value");
                                        nuevamuestra((float) va);


                                        imprimirln(device_name + "." + Attribute + "=" + (float) va);

                                    }



                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            } // del for


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                        // cabeceras de recepcion
                        rr = conn.getHeaderFields();
                        System.out.println("headers: " + rr.toString());

                    } else {
                        resp = "ERROR de conexión";
                        System.out.println("http response code error: " + rc + "\n");

                    }


                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (ProtocolException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }


                return resp;
            } else {
                imprimirln("através de PEPPropxy");


                try {
                    leng = Integer.toString(payload.getBytes("UTF-8").length);

                    OutputStreamWriter wr = null;
                    BufferedReader rd = null;
                    StringBuilder sb = null;

                    URL url = null;

                    url = new URL("http://pperez-seu-or.disca.upv.es:1028/v1/queryContext");

                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(10000); // miliseconds
                    conn.setConnectTimeout(15000);
                    conn.setRequestMethod("POST");

                    conn.setRequestProperty("Accept", HeaderAccept);
                    conn.setRequestProperty("Content-type", HeaderContent);
                    //conn.setRequestProperty("Fiware-Service", HeaderService);
                    conn.setRequestProperty("Content-Length", leng);
                    conn.setRequestProperty("fiware-service",userdomain);
                    conn.setRequestProperty("fiware-servicepath", "/");
                    conn.setRequestProperty("x-auth-token", usertoken);
                    conn.setDoOutput(true);

                    OutputStream os = conn.getOutputStream();
                    os.write(payload.getBytes("UTF-8"));
                    os.flush();
                    os.close();


                    int rc = conn.getResponseCode();
                    imprimirln("RC "+rc);
                    resp = conn.getContentEncoding();
                    is = conn.getInputStream();
                    imprimirln(resp);
                    if (rc == 403) {

                        resp = "OK";
                        //read the result from the server
                        rd = new BufferedReader(new InputStreamReader(is));
                        sb = new StringBuilder();

                        String line = null;
                        while ((line = rd.readLine()) != null) {
                            sb.append(line + "\n");
                        }
                        String result = sb.toString();

                        imprimirln("result "+result);
                        JSONObject jObject = null;
                        try {
                            jObject = new JSONObject(result);

                            JSONArray jArray = jObject.getJSONArray("contextResponses");
                            JSONObject c = jArray.getJSONObject(0);
                            JSONObject jo = c.getJSONObject("contextElement");
                            JSONArray jArrayattr = jo.getJSONArray("attributes");

                            for (int i = 0; i < jArrayattr.length(); i++) {
                                try {
                                    JSONObject ca = jArrayattr.getJSONObject(i);
                                    imprimirln("" + ca.getString("name").contains(Attribute));
                                    if (ca.getString("name").contains(Attribute)) {

                                        final double va = ca.getDouble("value");
                                        //nuevamuestra((float) va,view);
                                        imprimirln(device_name + "." + Attribute + "=" + (float) va);

                                    }


                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            } // del for


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                        // cabeceras de recepcion
                        rr = conn.getHeaderFields();
                        System.out.println("headers: " + rr.toString());

                    } else {
                        resp = "ERROR de conexión";
                        System.out.println("http response code error: " + rc + "\n");

                    }


                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (ProtocolException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }


                return resp;
            }



        }
    }



    void setTEXT(final String cad,final int id)
    {
        if (CF!=null)
        {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    //stuff that updates ui
                    TextView tv=(TextView)CF.fragmentview.findViewById(id);
                    tv.setText(cad);
                }
            });

        }
    }


    void nuevamuestra(final float dato)
    {
        if (CF!=null)
        {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    //stuff that updates ui
                    CF.nuevamuestra(dato);
                }
            });

        }
    }

    public void RunningSuscribeTask(String... urls)
    {
        subscribeContextTask ayyy = new subscribeContextTask();
        ayyy.execute(urls);
    }
    public void RunningUnSuscribeTask(String... urls)
    {
        imprimirln("eliminar subscripcion");
        unsubscribeContextTask ayy = new unsubscribeContextTask();
        ayy.execute(urls);
    }
    public void RunningCleanUnSuscribeTask(String... urls)
    {
        imprimirln("eliminar subscripcion");
        cleanunsubscribeContextTask ayy = new cleanunsubscribeContextTask();
        ayy.execute(urls);
    }


    class unsubscribeContextTask extends AsyncTask<String, Void, String> {

        String res;

        protected String doInBackground(String... urls) {

            Map<String, List<String>> rr;
            String res = "";
            InputStream is = null;
            int len = 500;

            //(user_name,user_passwd,device_name,attribute_name,service,subservice);
            String user_name = (String) urls[0];
            String user_passwd = (String) urls[1];
            String device_name = (String) urls[2];
            String service = (String) urls[4];
            String subservice = (String) urls[5];
            String Attribute=(String) urls[3];


            String HeaderAccept = "application/json";
            String HeaderContent = "application/json";
            String subsID_aux="";

            if (CF!=null) {
                subsID_aux=CF.getSUBSCRIPTIONID();
                if (subsID_aux!=null)
                {if (subsID_aux.length()==0) {
                    imprimirln("Error! ya no esta subscribe");
                    return "";
                }
                }else
                {
                    imprimirln("Error__1! ya no esta subscribe");
                    return "";
                }
            }


            String payload =  "{\"subscriptionId\" : \""+subsID_aux+"\"}";

            String leng = null;
            try {
                leng = Integer.toString(payload.getBytes("UTF-8").length);

                OutputStreamWriter wr = null;
                BufferedReader rd = null;
                StringBuilder sb = null;


                URL url = null;

                url = new URL("http://pperez-seu-or.disca.upv.es:1026/v1/unsubscribeContext");

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000 ); // miliseconds
                conn.setConnectTimeout(15000 );
                conn.setRequestMethod("POST");

                conn.setRequestProperty("Accept", HeaderAccept);
                conn.setRequestProperty("Content-type", HeaderContent);
                conn.setRequestProperty("fiware-service",service);
                conn.setRequestProperty("fiware-servicepath", subservice);
                //conn.setRequestProperty("Fiware-Service", HeaderService);
                conn.setRequestProperty("Content-Length", leng);
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                os.write(payload.getBytes("UTF-8"));
                os.flush();
                os.close();


                int rc = conn.getResponseCode();
                String resp = conn.getContentEncoding();
                is = conn.getInputStream();

                if (rc == 200) {

                    resp = "OK";


                        Subsnameentity=null;
                        Subsnameattribute=null;
                        if (CF!=null)
                        {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {


                                    CF.setSUBSCRIPTIONID("");
                                }
                            });

                        }



                    //read the result from the server
                    rd = new BufferedReader(new InputStreamReader(is));
                    sb = new StringBuilder();

                    String line = null;
                    while ((line = rd.readLine()) != null)
                    {
                        sb.append(line + "\n");
                    }
                    String result = sb.toString();


                    JSONObject jObject = null;
                    try {
                        jObject = new JSONObject(result);

                        JSONObject jo = jObject.getJSONObject("statusCode");

                        final String err= jo.getString("reasonPhrase");
                        imprimirln(err);

                    } catch (JSONException e) {

                        imprimirln("Error parsing json");

                        e.printStackTrace();
                    }






                } else {
                    resp = "ERROR de conexión";
                    System.out.println("http response code error: " + rc + "\n");

                }



                return resp;


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


            return "error";
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
        }
    }

    class cleanunsubscribeContextTask extends AsyncTask<String, Void, String> {

        String res;

        protected String doInBackground(String... urls) {

            Map<String, List<String>> rr;
            String res = "";
            InputStream is = null;
            int len = 500;

            //(user_name,user_passwd,device_name,attribute_name,service,subservice);

            String HeaderAccept = "application/json";
            String HeaderContent = "application/json";
            String subsID_aux=urls[2];
            String service = (String) urls[0];
            String subservice = (String) urls[1];

            String payload =  "{\"subscriptionId\" : \""+subsID_aux+"\"}";

            String leng = null;
            try {
                leng = Integer.toString(payload.getBytes("UTF-8").length);

                OutputStreamWriter wr = null;
                BufferedReader rd = null;
                StringBuilder sb = null;


                URL url = null;

                url = new URL("http://pperez-seu-or.disca.upv.es:1026/v1/unsubscribeContext");

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000 ); // miliseconds
                conn.setConnectTimeout(15000 );
                conn.setRequestMethod("POST");

                conn.setRequestProperty("Accept", HeaderAccept);
                conn.setRequestProperty("Content-type", HeaderContent);
                conn.setRequestProperty("fiware-service",service);
                conn.setRequestProperty("fiware-servicepath", subservice);

                conn.setRequestProperty("Content-Length", leng);
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                os.write(payload.getBytes("UTF-8"));
                os.flush();
                os.close();


                int rc = conn.getResponseCode();
                String resp = conn.getContentEncoding();
                is = conn.getInputStream();

                if (rc == 200) {

                    resp = "OK";


                    Subsnameentity=null;
                    Subsnameattribute=null;



                    //read the result from the server
                    rd = new BufferedReader(new InputStreamReader(is));
                    sb = new StringBuilder();

                    String line = null;
                    while ((line = rd.readLine()) != null)
                    {
                        sb.append(line + "\n");
                    }
                    String result = sb.toString();


                    JSONObject jObject = null;
                    try {
                        jObject = new JSONObject(result);

                        JSONObject jo = jObject.getJSONObject("statusCode");

                        final String err= jo.getString("reasonPhrase");
                        imprimirln(err);

                    } catch (JSONException e) {

                        imprimirln("Error parsing json");

                        e.printStackTrace();
                    }






                } else {
                    resp = "ERROR de conexión";
                    System.out.println("http response code error: " + rc + "\n");

                }



                return resp;


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


            return "error";
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
        }
    }

    class subscribeContextTask extends AsyncTask<String, Void, String> {

        String res;

        protected String doInBackground(String... urls) {

            Map<String, List<String>> rr;
            String res = "";
            InputStream is = null;
            int len = 500;
            //(user_name,user_passwd,device_name,attribute_name,service,subservice);
            String user_name = (String) urls[0];
            String user_passwd = (String) urls[1];
            String device_name = (String) urls[2];
            String service = (String) urls[4];
            String subservice = (String) urls[5];
            String Attribute=(String) urls[3];

            String subsID_aux="";

            if (CF!=null) {
                subsID_aux=CF.getSUBSCRIPTIONID();
                if (subsID_aux!=null)
                {if (subsID_aux.length()>0) {
                    imprimirln("Error! ya esta subscribe");
                    return "";
                }
                }
            }
            String HeaderAccept = "application/json";
            String HeaderContent = "application/json";


            if (sk == null)
                return "error socket no abierto";

            String urlEs = "http://" + sk.getInetAddress().getHostAddress() + ":" + sk.getLocalPort();
            String payload = "{\"entities\" : [{\"type\": \"sensoractuator\",\"isPattern\": \"false\",\"id\": \""+device_name+"\"}],\"attributes\": [\""+Attribute+"\"], \"reference\": \"" + urlEs + "\", \"duration\": \"P1M\",\"notifyConditions\": [{    \"type\": \"ONCHANGE\", \"condValues\": [\""+Attribute+"\" ] } ], \"throttling\": \"PT1S\"}";
            imprimirln(payload);
            // String encodedData = URLEncoder.encode(payload, "UTF-8");
            // String encodedData = payload;
            String leng = null;
            try {
                try {
                    leng = Integer.toString(payload.getBytes("UTF-8").length);
                } catch (UnsupportedEncodingException e1) {
                    e1.printStackTrace();
                }

                OutputStreamWriter wr = null;
                BufferedReader rd = null;
                StringBuilder sb = null;



                SubsOngoing=true;


                URL url = null;

                url = new URL("http://pperez-seu-or.disca.upv.es:1026/v1/subscribeContext");

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000); // miliseconds
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");

                conn.setRequestProperty("Accept", HeaderAccept);
                conn.setRequestProperty("Content-type", HeaderContent);
                conn.setRequestProperty("fiware-service",service);
                conn.setRequestProperty("fiware-servicepath", subservice);
                conn.setRequestProperty("Content-Length", leng);
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                os.write(payload.getBytes("UTF-8"));
                os.flush();
                os.close();


                int rc = conn.getResponseCode();
                String resp = conn.getContentEncoding();
                is = conn.getInputStream();

                if (rc == 200) {

                    resp = "OK";
                    //read the result from the server
                    rd = new BufferedReader(new InputStreamReader(is));
                    sb = new StringBuilder();

                    String line = null;
                    while ((line = rd.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    String result = sb.toString();


                    JSONObject jObject = null;
                    try {
                        jObject = new JSONObject(result);

                        JSONObject jo = jObject.getJSONObject("subscribeResponse");
                        Subsnameentity= device_name;
                        Subsnameattribute=Attribute;

                        final String subsIDl = jo.getString("subscriptionId");
                        imprimirln(subsIDl);
                        if (CF!=null)
                        {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    //stuff that updates ui
                                    CF.setSUBSCRIPTIONID(subsIDl);

                                }
                            });

                        }


                    } catch (JSONException e) {

                        e.printStackTrace();
                    }


                } else {

                    resp = "ERROR de conexión";
                    System.out.println("http response code error: " + rc + "\n");

                }

                SubsOngoing=false;
                return resp;


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            SubsOngoing=false;
            return "error";
        }
    }


    public void RunningUpdateLEDTask(String... urls)
    {
        updateLEDContextTask ayyy = new updateLEDContextTask();
        ayyy.execute(urls);
    }

    class updateLEDContextTask extends AsyncTask<String, Void, String> {

        String res;

        protected String doInBackground(String... urls) {

            Map<String, List<String>> rr;
            String res = "";
            InputStream is = null;
            // Only display the first 500 characters of the retrieved
            // web page content.
            int len = 500;
            // View rootView = findViewById(R.layout.fragment_main);


            String user_name=urls[0];
            String user_passwd=urls[1];
            String device_name=urls[2];
            String attribute_name=urls[3];
            String service=urls[4];
            String subservice=urls[5];
            String LED1_ST=urls[6];
            String LED2_ST=urls[7];
            String LED3_ST=urls[8];
            String LED4_ST=urls[9];

            imprimirln("" + " " + device_name + ".LED1="+LED1_ST+" "+ ".LED2="+LED2_ST+" "+ ".LED3="+LED3_ST+" "+ ".LED4="+LED4_ST+" ");

            String HeaderAccept = "application/json";
            String HeaderContent = "application/json";
            String payload_updateContext = "{ \"contextElements\": [" +
                    "{\"type\": \"sensoractuator\", \"isPattern\": \"false\",\"id\": \""+device_name+"\",\"attributes\": [" +
                    "{\"name\":\""+attribute_name+"\",\"type\": \"binary\",\"value\": \"" + LED1_ST +LED2_ST +LED3_ST +LED4_ST +" \"}]}],\"updateAction\": \"UPDATE\"}";

            imprimirln(payload_updateContext);

            String leng = null;
            String resp=        "none";

            try {
                leng = Integer.toString(payload_updateContext.getBytes("UTF-8").length);

                OutputStreamWriter wr = null;
                BufferedReader rd = null;
                StringBuilder sb = null;


                URL url = null;

                url = new URL("http://pperez-seu-or.disca.upv.es:1026/v1/updateContext");

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000); // miliseconds
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");

                conn.setRequestProperty("Accept", HeaderAccept);
                conn.setRequestProperty("Content-type", HeaderContent);
                conn.setRequestProperty("fiware-service", service);
                conn.setRequestProperty("fiware-servicepath",  subservice);
                conn.setRequestProperty("Content-Length", leng);
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                os.write(payload_updateContext.getBytes("UTF-8"));
                os.flush();
                os.close();


                int rc = conn.getResponseCode();

                resp = conn.getContentEncoding();
                is = conn.getInputStream();

                if (rc == 200) {

                    resp = "OK";
                    //read the result from the server
                    rd = new BufferedReader(new InputStreamReader(is));
                    sb = new StringBuilder();

                    String line = null;
                    while ((line = rd.readLine()) != null)
                    {
                        sb.append(line + "\n");
                    }
                    String result = sb.toString();
                    imprimirln("UPDATE CONTEXT: "+result);

                } else {
                    resp = "ERROR de conexión";
                    System.out.println("http response code error: " + rc + "\n");
                    imprimirln("UPDATE CONTEXT: "+"http response code error: " + rc + "\n");

                }


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


            return resp;




        }
    }

    public void RunningUpdateVelocityContextTask(String... urls)
    {
        updateVelocityContextTask ayyy = new updateVelocityContextTask();
        ayyy.execute(urls);
    }

    class updateVelocityContextTask extends AsyncTask<String, Void, String> {

        String res;

        protected String doInBackground(String... urls) {

            Map<String, List<String>> rr;
            String res = "";
            InputStream is = null;
            // Only display the first 500 characters of the retrieved
            // web page content.
            int len = 500;
            // View rootView = findViewById(R.layout.fragment_main);

            String user_name=urls[0];
            String user_passwd=urls[1];
            String device_name=urls[2];
            String attribute_name=urls[3];
            String service=urls[4];
            String subservice=urls[5];
            String velocity=urls[6];


            imprimirln("" + " " + attribute_name + "= "+velocity);

            String HeaderAccept = "application/json";
            String HeaderContent = "application/json";
            String payload_updateContext = "{ \"contextElements\": [" +
                    "{\"type\": \"sensoractuator\", \"isPattern\": \"false\",\"id\": \""+device_name+"\",\"attributes\": [" +
                    "{\"name\":\""+attribute_name+"\",\"type\": \"integer\",\"value\": \"" + velocity + "\"}]}],\"updateAction\": \"UPDATE\"}";
            imprimirln(payload_updateContext);

            String leng = null;
            String resp=        "none";

            try {
                leng = Integer.toString(payload_updateContext.getBytes("UTF-8").length);

                OutputStreamWriter wr = null;
                BufferedReader rd = null;
                StringBuilder sb = null;


                URL url = null;

                url = new URL("http://pperez-seu-or.disca.upv.es:1026/v1/updateContext");

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000); // miliseconds
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");

                conn.setRequestProperty("Accept", HeaderAccept);
                conn.setRequestProperty("Content-type", HeaderContent);
                conn.setRequestProperty("fiware-service", service);
                conn.setRequestProperty("fiware-servicepath",  subservice);
                conn.setRequestProperty("Content-Length", leng);
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                os.write(payload_updateContext.getBytes("UTF-8"));
                os.flush();
                os.close();


                int rc = conn.getResponseCode();

                resp = conn.getContentEncoding();
                is = conn.getInputStream();

                if (rc == 200) {

                    resp = "OK";
                    //read the result from the server
                    rd = new BufferedReader(new InputStreamReader(is));
                    sb = new StringBuilder();

                    String line = null;
                    while ((line = rd.readLine()) != null)
                    {
                        sb.append(line + "\n");
                    }
                    String result = sb.toString();
                    imprimirln("UPDATE CONTEXT: "+result);

                } else {
                    resp = "ERROR de conexión";
                    System.out.println("http response code error: " + rc + "\n");
                    imprimirln("UPDATE CONTEXT: "+"http response code error: " + rc + "\n");

                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


            return resp;




        }
    }




}



package com.mindsmack.blogreader;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;


public class MainListActivity extends ListActivity {

    //protected String [] mBlogPostTitles; Utilizaremos ArrayList y no una Array.


    public static final int NUMBER_OF_POSTS = 20;
    public static final String TAG = MainListActivity.class.getSimpleName();
    protected JSONObject mBlogData;
    protected ProgressBar mProgressBar;
    private final String KEY_TITLE = "title";
    private final String KEY_AUTHOR = "author";


    /*
        Hemos guardado el valor de la array dentro del string.xml. en resources. La inicializaremos
        luego de setContentView y antes de setListAdapter (Caso contrario siempre mostraría la
        string empty).
        El metodo para obtener el array es getStringArray() pero no esta disponible desde el contexto
        o las subclases de la actividad. Es parte del objeto Resources, al cual podemos acceder
        con el metodo getResources() (Que está disponible desde el contexto o las subclases)
        1. Creamos un objeto de la clase Resources
        2. A la array la inicializamos con el metodo getStringArray(R.array.nombre_array)
     */


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_list);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        if (isNetworkAvailable()) {
            mProgressBar.setVisibility(View.VISIBLE);
            getBlogPostsTask getBlogPostsTask = new getBlogPostsTask(); // crea la ASyncTask
            getBlogPostsTask.execute();// la ejecutamos.
        } else{
            Toast.makeText(this, getString(R.string.no_network), Toast.LENGTH_LONG).show();
        }

        Resources resources= getResources();

        //ArrayAdapter <String> adapter = new ArrayAdapter<String>
               // (this, android.R.layout.simple_list_item_1, mBlogPostTitles);
        //setListAdapter(adapter);


        /*
        La clase ArrayAdapter se puede usar para adaptar cualquier tipo de Array para luego mostrar
        en una lista.
        Dentro de los <> se determina el tipo de array que adaptaremos.
        Utilizamos el constructor con 3 parametros:
        1 el contexto (Al ser activity una subclase de Context podemos usar la palabra this.)
        2 El layout para utilizar con cada item de la lista. Utilizamos un layout provisto por andr.
        3 Array a adaptar.
         */

        // Para obtener una string resource se escribe el metodo getString(R.string.nombre_string)

    }

    // El metodo onListItemClick es el que determina que accion se lleva a cabo cuando se pulsa
    //sobre un elemento en la lista. Se usa un intent para llevar a cabo la accion de ir a la web
    // del elemento.

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        //  Es un metodo de la clase ListActivity para realizar alguna accion cuando se toca sobre
        // un elemento de la lista. Se debe sobreescribir. @Override
        // Este metodo acepta 4 parametros, no se los pasamos sino que los maneja ListActivity y
        // estan disponibles si los queremos usar.
        // 1- ListView en la cual el item es tocado.
        // 2- View que representa el item en la lista que fue tocado.
        // 3- Posicion numerica en la lista
        // 4- Id de la View que muestra la fila que ha sido tocada.


        try {

            JSONArray jsonPosts = mBlogData.getJSONArray("posts");
            JSONObject jsonPost = jsonPosts.getJSONObject(position);
            String blogUrl = jsonPost.getString("url");
            Intent intent = new Intent(this, BlogWebViewActivity.class);


            //ACTION_VIEW es una accion que realiza la accion mas razonable en determinado lugar.
            //en este caso le pasamos una url, por lo tanto sabrá que se debe abrir el explorador.

            intent.setData(Uri.parse(blogUrl));
            startActivity(intent);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        boolean isAvailable = false;

        if (networkInfo != null && networkInfo.isConnected()){
            isAvailable = true;
        }
        return isAvailable;
    }

    private void handleBlogResponse() {
        if (mBlogData == null){
            mProgressBar.setVisibility(View.INVISIBLE);
            updateDisplayForError();
        } else {
            try {
                JSONArray jsonPosts = mBlogData.getJSONArray("posts");
                int length = jsonPosts.length();
                mProgressBar.setVisibility(View.INVISIBLE);


                //mBlogPostTitles = new String [length];


                ArrayList <HashMap<String, String>> blogPosts =
                        new ArrayList<HashMap<String, String>>();


// ArrayList es una clase hija de List, que soporta una array. Mediante operaciones se pueden agregar
// eliminar o reemplazar elementos en la array.


                for (int i = 0; i < length; i++){
                    JSONObject post = jsonPosts.getJSONObject(i);
                    String title = post.getString(KEY_TITLE);
                    title = Html.fromHtml(title).toString();


    //Html.fromHtml(string) convierte los caracteres especiales de html a comunes(´ 'a " etc)


                    //mBlogPostTitles[i]= title;

                    String author = post.getString(KEY_AUTHOR);
                    author = Html.fromHtml(author).toString();

                    HashMap<String, String> blogPost = new HashMap<String, String>();


// Un HashMap es una implementación de Map. Todos los elementos estan permitidos como pares clave,
// valor.

                    blogPost.put(KEY_TITLE, title );

// El metodo put mapea la clave especificada con el valor correspondiente. .put(clave,valor)


                    blogPost.put(KEY_AUTHOR, author );

                    blogPosts.add(blogPost);

// El metodo add de ArrayList agrega el elemento especificado al final de la ArrayList


                }

                String [] keys = {KEY_TITLE, KEY_AUTHOR};
                int [] ids = {android.R.id.text1, android.R.id.text2};


//Array de claves y de ids que se corresponden con la layout que se actualizara.Las usa SimpleAdapter. .


                SimpleAdapter adapter = new SimpleAdapter
                        (this, blogPosts, android.R.layout.simple_list_item_2, keys, ids);


/*
Esta clase es un adaptador de data del tipo map a views definidas en un archivo XML. Se especifica
la data que ira en la lista como una ArrayList de Maps. Cada entrada en la ArrayList se corresponde
Con una fila en la lista.
El constructor toma 5 parametros.
    1- contexto: this
    2- data: Una List de Maps. Cada entrada en la List corresponde a una fila en la lista.
    3- resource: un Resource identificador de una vista que define la disposicion de este item.
    4- from: Una lista de nombres que seran agregados al mapa asociado con cada item. (columnas)
    5- to: Las vistas donde  se  mostraran las columnas. Deben ser todas TextViews. Las primeras n
     vistas en esta lista se corresponderan con las primeras n columnas en el parametro from.
 */



                //ArrayAdapter<String> adapter = new ArrayAdapter<String>
                  //      (this,android.R.layout.simple_list_item_1, mBlogPostTitles );
                setListAdapter(adapter);

            } catch (JSONException e) {
                logException(e);
            }
        }
    }

    private void logException(Exception e) {
        Log.e(TAG, "Exception caught!", e);
    }

    private void updateDisplayForError() {


    /*
    Creamos un dialogo para mostrar que hay problemas obteniendo info del blog.
     */


        AlertDialog.Builder builder= new AlertDialog.Builder(this);// constructor de AlertDialog
        builder.setTitle(getString(R.string.title))// lo poblamos
                .setMessage(getString(R.string.error_message))
                .setPositiveButton(getString(R.string.ok_dialog_button), null);
        AlertDialog dialog = builder.create();// creamos el AlertDialog
        dialog.show(); // lo mostramos


// Si no queremos realizar ninguna accion cuando se hace click en el boton, se pone null en lugar de
// OnClickListener.


        TextView emptyTextView = (TextView) getListView().getEmptyView();
        emptyTextView.setText(getString(R.string.no_items));
    }


    /*
    Creamos una clase anidada en la actividad. Esto se puede hacer cuando la clase sera utilizada
     dentro de otra y no tenemos necesidad de utilizarla en  otro lugar.
     Se podria haber hecho una clase publica y utilizarla, pero requeriria de codigo extra.
     AsyncTask requiere de tres tipos de definición dentro de los <>
     1- El primer parametro es para el tipo de parametro enviado a la tarea durante ejecucion.
     2- El segundo (progress) es el tipo de unidad para medir el progreso mientras dure la tarea
     se usa si vamos a mantener un registro de la misma.
     3- Se usa para definir el tipo del resultado de la operación en background.

     Para marcar un parametro como sin uso, se usa la palabra Void
     */


    private class getBlogPostsTask extends AsyncTask<Object,Void, JSONObject>{


        // este metodo es obligatorio.


        @Override
        protected JSONObject doInBackground(Object[] params) {
            JSONObject jsonResponse = null;
            int responseCode = -1;
            try {
                URL blogFeedUrl =
                        new URL("http://blog.teamtreehouse.com/api/get_recent_summary/?count=" +
                                NUMBER_OF_POSTS);
                HttpURLConnection connection = (HttpURLConnection)blogFeedUrl.openConnection();
                connection.connect(); // lanza una exception del tipo networkOnMainThreadException.
                responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK){ // si la respuesta es positiva (code==200)


                    /*
                    Cuando una respuesta es exitosa, la data se guarda en un flujo de entrada dentro
                    del objeto Connection.
                    Necesitamos un objeto Reader para leer nuestra data y almacenarla en nuestras
                    variables.
                     */


                    InputStream inputStream = connection.getInputStream();
                    Reader reader = new InputStreamReader(inputStream);


    // El reader se usará para leer caracter a caracter el inputStream, luego los almacenamos en una array
    //int contentLength = connection.getContentLength();
    //contentLength() permite saber cuantos caracteres tiene el inputStream que leeremos.

                    int nextCharacter;
                    String responseData ="";
                    while(true){
                        nextCharacter = reader.read();
                        if(nextCharacter == -1){
                            break;
                        }
                        responseData += (char) nextCharacter;
                    }

                    //char [] charArray = new char[contentLength];
                    //reader.read(charArray);


    // Lee del inputStream y almacena la data en el array de chars.


                    //String responseData = new String(charArray);
                    jsonResponse = new JSONObject(responseData);


    // a partir de este objeto podemos empezar a obtener las propiedades de los objetos JSON mediante
    // los metodos apropiados.


                } else {
                    Log.i(TAG, "unsuccessful HTTP response code: " + responseCode);
                }
            } catch (MalformedURLException e) {
                logException(e);
            } catch (IOException e) {
                logException(e);
            } catch (Exception e){
                logException(e);
            }
            return jsonResponse;
        }


        /*
        Viendo la documentacion de ASyncTask vemos que el metodo execute se lleva a cabo en 4 pasos
        http://developer.android.com/intl/es/reference/android/os/AsyncTask.html
        doInBackground() es un metodo que devuelve un tipo de dato, y se ejecuta en un background
        thread. Por lo tanto no puede interactuar ni actualizar  la UI.
        El metodo que si puede hacerlo es onPostExecute() que lleva como argumento el resultado que
        devuelve doInBackground().

         */


        @Override
        protected void onPostExecute(JSONObject result){
            mBlogData = result;
            handleBlogResponse();
        }


        // tenemos que solicitar permiso para acceder a internet. Sino nos lanza una exception
        // Esto se hace desde el manifest.


    }


}

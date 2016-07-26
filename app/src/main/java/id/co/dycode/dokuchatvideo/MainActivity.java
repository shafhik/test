package id.co.dycode.dokuchatvideo;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import id.co.dycode.dokuchatvideolibrary.Authentication;

public class MainActivity extends Activity {
    Button btn_libs, btn_libs2, btn_libs3;
    EditText txt_token, txt_id, txt_email, txt_user_name, txt_avatar, txt_color;

    URL url;
    SharedPreferences pref;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_libs = (Button) findViewById(R.id.btn_lib);
        btn_libs2 = (Button) findViewById(R.id.btn_lib2);
        txt_token = (EditText) findViewById(R.id.txt_token);
        txt_id = (EditText) findViewById(R.id.txt_id);
        txt_email = (EditText) findViewById(R.id.txt_email);
        txt_user_name = (EditText) findViewById(R.id.txt_user_name);
        txt_avatar = (EditText) findViewById(R.id.txt_avatar);
        txt_color = (EditText) findViewById(R.id.txt_color);


        pref = getApplicationContext().getSharedPreferences("doku_user_prefence", 0); // 0 - for private mode
        editor = pref.edit();

        btn_libs.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {

                Authentication auth = new Authentication();
                auth.AcessVideo(MainActivity.this, txt_email.getText().toString(), txt_token.getText().toString(), txt_avatar.getText().toString(), txt_color.getText().toString(), txt_id.getText().toString());
            }
        });

        btn_libs2.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {

                Authentication auth = new Authentication();
                auth.AcessChat(MainActivity.this, txt_email.getText().toString(), txt_user_name.getText().toString(), txt_color.getText().toString(), txt_id.getText().toString());
            }
        });

    }

    class TaskGetListRoom extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... post_request) {

            HttpURLConnection connection = null;

            try {
                //Create connection
                url = new URL(post_request[0]);
                connection = (HttpURLConnection) url.openConnection();
                //connection.connect();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Content-Language", "en-US");

                //Get Response

                Integer responseCode = connection.getResponseCode();

                String responseMessage = connection.getResponseMessage();

                InputStream is = connection.getInputStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                String line;
                StringBuffer response = new StringBuffer();
                while ((line = rd.readLine()) != null) {
                    response.append(line);
                    response.append('\r');
                }
                rd.close();


                return responseCode.toString();

            } catch (Exception e) {
                e.printStackTrace();
                return "500";

            } finally {

                if (connection != null) {
                    connection.disconnect();
                }
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if(result.equals("200")) {

                editor.putString("user_token", txt_token.getText().toString()); // Storing string
                editor.putString("user_id", txt_id.getText().toString()); // Storing string
                editor.putString("user_email", txt_email.getText().toString()); // Storing string
                editor.putString("user_name", txt_user_name.getText().toString()); // Storing string
                editor.putString("user_avatar", txt_avatar.getText().toString()); // Storing string
                editor.putString("user_color", txt_color.getText().toString()); // Storing string

                editor.commit(); // commit changes

                //Intent intent = new Intent(MainActivity.this, Chat.class);

                //startActivity(intent);

            } else {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);

                // Setting Dialog Title
                alertDialog.setTitle("Doku Chat");

                // Setting Dialog Message
                alertDialog.setMessage("Anda belum mengaktifkan akun chatting Doku. Aktifkan sekarang?");

                // Setting Icon to Dialog
                //alertDialog.setIcon(R.drawable.delete);

                // Setting Positive "Yes" Button
                alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int which) {

                        // Write your code here to invoke YES event
                        //Toast.makeText(getApplicationContext(), "You clicked on YES", Toast.LENGTH_SHORT).show();
                    }
                });

                // Setting Negative "NO" Button
                alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Write your code here to invoke NO event
                        //Toast.makeText(getApplicationContext(), "You clicked on NO", Toast.LENGTH_SHORT).show();
                        dialog.cancel();
                    }
                });

                // Showing Alert Message
                alertDialog.show();
            }
        }
    }

}
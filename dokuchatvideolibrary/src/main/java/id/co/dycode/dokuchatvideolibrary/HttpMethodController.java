package id.co.dycode.dokuchatvideolibrary;

import org.json.JSONObject;

import java.io.File;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by 1 on 7/5/2016.
 */
public class HttpMethodController {
    OkHttpClient client = new OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .writeTimeout(5, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build();
    OutputStream out = null;


    public String[] PostFileMethod(String api_url, String form_field, String file_path, String file_extension, File file ){

        try {
            RequestBody formBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart(form_field, (String) file_path,
                            RequestBody.create(MediaType.parse(file_extension), (File) file))
                    .build();
            Request request = new Request.Builder().url(api_url).post(formBody).build();
            Response response = this.client.newCall(request).execute();
            String[] result = new String[2];

            result[0] = response.body().string();
            result[1] = String.valueOf(response.code());

            return result;

        } catch (Exception ex) {
            String[] result = new String[2];
            result[0] = null;
            result[1] = null;

            return result;
        }

    }

    public String[] GetMethod(String url, HashMap<String, String> parameter){

        try {
            HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();

            for ( Map.Entry<String, String> entry : parameter.entrySet() ) {
                urlBuilder.addQueryParameter(entry.getKey(), entry.getValue() );
            }
            String url_builder = urlBuilder.build().toString();

            Request request = new Request.Builder()
                    .url(url_builder)
                    .build();

            Response response = client.newCall(request).execute();
            String[] result = new String[2];

            result[0] = response.body().string();
            result[1] = String.valueOf(response.code());

            return result;

        } catch (Exception e) {
            e.printStackTrace();
            String[] result = new String[2];
            result[0] = null;
            result[1] = null;

            return result;
        }
    }



    public String[] PostMethod(String url, HashMap<String, String> parameter, HashMap<String, String> form_body){

        try {

            HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();
            for ( Map.Entry<String, String> entry : parameter.entrySet() ) {
                urlBuilder.addQueryParameter(entry.getKey(), entry.getValue());
            }
            String url_builder = urlBuilder.build().toString();

            FormBody.Builder formBuilder = new FormBody.Builder();
            for ( Map.Entry<String, String> entry_form : form_body.entrySet() ) {
                formBuilder.add(entry_form.getKey(), entry_form.getValue());
            }
            RequestBody formBody = formBuilder.build();


            Request request = new Request.Builder()
                    .url(url_builder)
                    .post(formBody)
                    .build();

            Response response = client.newCall(request).execute();
            String[] result = new String[2];

            result[0] = response.body().string();
            result[1] = String.valueOf(response.code());

            return result;

        } catch (Exception e) {
            e.printStackTrace();
            String[] result = new String[2];
            result[0] = null;
            result[1] = null;

            return result;
        }
    }



    public String[] PostJsonMethod(String url, HashMap<String, String> parameter, JSONObject json_body){
        OkHttpClient client = new OkHttpClient();
        final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

        try {

            HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();
            for ( Map.Entry<String, String> entry : parameter.entrySet() ) {
                urlBuilder.addQueryParameter(entry.getKey(), entry.getValue());
            }
            String url_builder = urlBuilder.build().toString();

            RequestBody body = RequestBody.create(JSON, String.valueOf(json_body));

            Request request = new Request.Builder()
                    .url(url_builder)
                    .post(body)
                    .build();

            Response response = client.newCall(request).execute();
            String[] result = new String[2];

            result[0] = response.body().string();
            result[1] = String.valueOf(response.code());

            return result;

        } catch (Exception e) {
            e.printStackTrace();
            String[] result = new String[2];
            result[0] = null;
            result[1] = null;

            return result;
        }
    }
}

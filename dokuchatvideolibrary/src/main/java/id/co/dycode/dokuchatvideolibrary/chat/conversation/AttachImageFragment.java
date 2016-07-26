package id.co.dycode.dokuchatvideolibrary.chat.conversation;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import id.co.dycode.dokuchatvideolibrary.R;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by fahmi on 21/07/2016.
 */
public class AttachImageFragment extends BottomSheetDialogFragment {

    public static final int CAMERA_REQUEST = 2888;
    public static final int SELECT_PICTURE = 2999;
    private static final String LAST_TOPIC_ID = "id.co.dycode.dokuchatvideolibrary.chat.conversation.AttachImageFragment.LAST_TOPIC_ID";

    String mCurrentPhotoPath, lastTopicId, extension;
    File image;
    SharedPreferences pref;
    URL url;
    Button btnCamera, btnImage;
    Context context;


    public static AttachImageFragment newInstance(String lastTopicId) {
        Bundle bundle = new Bundle();
        bundle.putString(LAST_TOPIC_ID, lastTopicId);
        AttachImageFragment attachImageFragment = new AttachImageFragment();
        attachImageFragment.setArguments(bundle);
        return attachImageFragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    private BottomSheetBehavior.BottomSheetCallback mBottomSheetBehaviorCallback = new BottomSheetBehavior.BottomSheetCallback() {

        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                dismiss();
            }

        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
        }
    };


    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        View contentView = View.inflate(getContext(), R.layout.fragment_attachment, null);
        btnCamera = (Button) contentView.findViewById(R.id.button_cam);
        btnImage = (Button) contentView.findViewById(R.id.button_img);
        btnCamera.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPermisson(CAMERA_REQUEST))
                    cameraIntent();
            }
        });

        btnImage.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPermisson(SELECT_PICTURE))
                    galleryIntent();
            }
        });
        dialog.setContentView(contentView);



        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) ((View) contentView.getParent()).getLayoutParams();
        CoordinatorLayout.Behavior behavior = params.getBehavior();

        if (behavior != null && behavior instanceof BottomSheetBehavior) {
            ((BottomSheetBehavior) behavior).setBottomSheetCallback(mBottomSheetBehaviorCallback);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pref = context.getApplicationContext().getSharedPreferences("doku_user_prefence", 0);
        lastTopicId = getArguments().getString(LAST_TOPIC_ID);

    }

    private boolean checkPermisson(int requestCode) {
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions((MessageActivity) context,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    requestCode);
            return false;

        } else {
            return true;
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case CAMERA_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    cameraIntent();
                }
                break;
            }
            case SELECT_PICTURE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    galleryIntent();
                }
                break;


        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == CAMERA_REQUEST) {

                // Get the dimensions of the bitmap
                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                bmOptions.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
                int photoW = bmOptions.outWidth;
                int photoH = bmOptions.outHeight;

                // Determine how much to scale down the image
                int scaleFactor = Math.min(photoW, photoH);

                // Decode the image file into a Bitmap sized to fill the View
                bmOptions.inJustDecodeBounds = false;
                bmOptions.inSampleSize = scaleFactor << 1;
                bmOptions.inPurgeable = true;

                Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);

                Matrix mtx = new Matrix();
                mtx.postRotate(90);
                // Rotating Bitmap
                Bitmap rotatedBMP = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), mtx, true);

                if (rotatedBMP != bitmap) {
                    bitmap.recycle();
                }

                image = new File(mCurrentPhotoPath);

                extension = MimeTypeMap.getFileExtensionFromUrl(mCurrentPhotoPath);

                String urlUploadImage = getString(R.string.API_URL_UPLOAD) + "?topic_id=" + lastTopicId + "&hashing=ok&username=" + (pref.getString("user_email", null));

                new TaskPostImage().execute(urlUploadImage, mCurrentPhotoPath, image);

            } else if (requestCode == SELECT_PICTURE) {
                Uri selectedImageUri = data.getData();
                String imagePath = getPath(selectedImageUri);

                image = new File(imagePath);
                extension = MimeTypeMap.getFileExtensionFromUrl(imagePath);

                String url_upload_image = getString(R.string.API_URL_UPLOAD) + "?topic_id=" + lastTopicId + "&hashing=ok&username=" + (pref.getString("user_email", null));

                new TaskPostImage().execute(url_upload_image, imagePath, image);

            }
        }
    }

    //======================
    // Start Camera
    //======================

    private void cameraIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(context.getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                ((MessageActivity) context).startActivityForResult(takePictureIntent, CAMERA_REQUEST);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String imageFileName = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.ENGLISH).format(new Date());
        String storageDir = Environment.getExternalStorageDirectory() + "/DokuChat";
        File dir = new File(storageDir);
        if (!dir.exists())
            dir.mkdir();

        image = new File(storageDir + "/" + imageFileName + ".jpg");

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }


    //======================
    // Start Gallery
    //======================

    private void galleryIntent() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        ((MessageActivity) context).startActivityForResult(Intent.createChooser(intent,
                "Select Picture"), SELECT_PICTURE);

    }

    public String getPath(Uri uri) {
        // just some safety built in
        if (uri == null) {
            // TODO perform some logging or show user feedback
            return null;
        }
        // try to retrieve the image from the media store first
        // this will only work for images selected from gallery
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = ((Activity) context).managedQuery(uri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        // this is our fallback here
        return uri.getPath();
    }


    //===============================
    // Start AsyncTask
    //===============================

    class TaskPostImage extends AsyncTask<Object, String, String> {

        OkHttpClient client = new OkHttpClient();
        OutputStream out = null;
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("Uploading..");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(Object... post_request) {

            String result;
            try {
                String url = String.valueOf(post_request[0]);
                RequestBody formBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("file", (String) post_request[1],
                                RequestBody.create(MediaType.parse("image/" + extension), (File) post_request[2]))
                        .build();
                Request request = new Request.Builder().url((String) post_request[0]).post(formBody).build();
                Response response = this.client.newCall(request).execute();
                result = response.body().string();
            } catch (Exception ex) {
                result = ex.toString();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressDialog.dismiss();

            if (result == null) {
                AlertDialog alertDialog = new AlertDialog.Builder(context)
                        .setTitle(context.getString(R.string.internet_connection_alert_tittle))
                        .setMessage(context.getString(R.string.internet_connection_alert_message))
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .create();

                alertDialog.show();
            } else {
                HttpURLConnection connection = null;

                JSONObject jsonObject = null;

                String url_post_chat = getString(R.string.API_URL_MOBILE) + "postcomment?token=" + (pref.getString("user_token", null)) +
                        "&topic_id=" + lastTopicId;
                try {
                    jsonObject = new JSONObject(result);
                    String post_url = jsonObject.getString("url");

                    String parameters = "comment=" + URLEncoder.encode("[file] " + post_url + " [/file]", "UTF-8");

                    new TaskPostChat().execute(url_post_chat, parameters);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class TaskPostChat extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... post_request) {

            HttpURLConnection connection = null;
            try {
                //Create connection
                url = new URL(post_request[0]);
                connection = (HttpURLConnection) url.openConnection();
                //connection.connect();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type",
                        "application/x-www-form-urlencoded");

                connection.setRequestProperty("Content-Length", "" +
                        Integer.toString(post_request[1].getBytes().length));
                connection.setRequestProperty("Content-Language", "en-US");

                connection.setUseCaches(false);
                connection.setDoInput(true);
                connection.setDoOutput(true);

                //Send request
                DataOutputStream wr = new DataOutputStream(
                        connection.getOutputStream());
                wr.writeBytes(post_request[1]);
                wr.flush();
                wr.close();


                InputStream is = connection.getInputStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                String line;
                StringBuilder response = new StringBuilder();
                while ((line = rd.readLine()) != null) {
                    response.append(line);
                    response.append('\r');
                }
                rd.close();

                return response.toString();

            } catch (Exception e) {
                e.printStackTrace();
                return null;

            } finally {

                if (connection != null) {
                    connection.disconnect();
                }
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            getDialog().dismiss();

        }

    }

}

package com.example.saipavan.uploading;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class upload_img extends AppCompatActivity implements View.OnClickListener  {
    private static final int RESULT_LOAD_IMAGE=1;
    private static final String SERVER_ADDRESS="www.google.com";
    ImageView imageToUpload,downloadImage;
    Button bUploadImage,bDownloadImage ;
    EditText uploadImageName ,downloadImageName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_img);
        imageToUpload = (ImageView) findViewById(R.id.imageToUpload);
        downloadImage=(ImageView) findViewById(R.id.downloadImage);

        bUploadImage= (Button) findViewById(R.id.bUploadImage);
        bDownloadImage= (Button) findViewById(R.id.bDownloadImage);

        uploadImageName= (EditText) findViewById(R.id.etUploadName);
        downloadImageName= (EditText) findViewById(R.id.etDownloadName);

        imageToUpload.setOnClickListener(this);
        bUploadImage.setOnClickListener(this);
        bDownloadImage.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.imageToUpload:
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, RESULT_LOAD_IMAGE);
                break;
            case R.id.bUploadImage:
                Bitmap image =((BitmapDrawable) imageToUpload.getDrawable()).getBitmap();
                new uploadImage(image,uploadImageName.getText().toString());
                break;
            case R.id.bDownloadImage:
                new DownloadImage(downloadImageName.getText().toString()).execute();
                break;
        }
    }
        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
          super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==RESULT_LOAD_IMAGE && resultCode==RESULT_OK && data!=null){
          Uri selectedImage = data.getData();
        imageToUpload.setImageURI(selectedImage);
        }
        }


        private class uploadImage extends AsyncTask<Void,Void,Void>{
                Bitmap image;
                String name;

                public uploadImage(Bitmap image,String name)
            {
                this.image = image;
                this.name = name;
            }


         @Override
            protected Void doInBackground(Void... params) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream);
            String encodedImage = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);
            ArrayList<NameValuePair> dataToSend = new ArrayList<>();
            dataToSend.add(new BasicNameValuePair("image",encodedImage));
            dataToSend.add(new BasicNameValuePair("name",name));
            HttpParams httpRequestParams = getHttpRequestParams();
            HttpClient client = new DefaultHttpClient(getHttpRequestParams());
            HttpPost post=new HttpPost(SERVER_ADDRESS+"SavePicture.php");
            try
            {
                post.setEntity(new UrlEncodedFormEntity(dataToSend));
                client.execute(post);
            }catch(Exception e)
            {
                e.printStackTrace();
            }
            return null; }
         @Override
            protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
             Toast.makeText(getApplicationContext(),"Image uploaded",Toast.LENGTH_SHORT).show();
    }
}
private class DownloadImage extends AsyncTask<Void,Void ,Bitmap> {
        String name;

    public DownloadImage(String name) {
       this.name = name;
    }
    @Override
    protected Bitmap doInBackground(Void... params) {
        String url = SERVER_ADDRESS + "pictures/" + name  + ".JPG";
        try{
            URLConnection connection=new URL(url).openConnection();
            connection.setConnectTimeout(1000*30);
            connection.setReadTimeout(1000*30);

            return BitmapFactory.decodeStream((InputStream) connection.getContent(),null,null);
        }catch(Exception e) {
            e.printStackTrace();
            return null;

        }
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        if(bitmap!=null){
            downloadImage.setImageBitmap(bitmap);
        }
    }
}


private HttpParams getHttpRequestParams()
{
    HttpParams httpRequestParams =new BasicHttpParams();
    HttpConnectionParams.setConnectionTimeout(httpRequestParams,1000*30);
    HttpConnectionParams.setSoTimeout(httpRequestParams,1000*30);
    return httpRequestParams;
}
}




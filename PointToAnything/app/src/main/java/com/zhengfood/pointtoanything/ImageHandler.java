package com.zhengfood.pointtoanything;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.app.ProgressDialog;
import android.widget.Toast;

/**
 * Created by robert on 4/15/15.
 */
public class ImageHandler extends AsyncTask<String, String, Bitmap> {
    Context _context;
    ImageView _imageView;
    String _url;
    ProgressDialog pDialog;
    Bitmap bitmap;

    public ImageHandler(Context context, ImageView imageView) {
        this._imageView = imageView;
        this._context = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        pDialog = new ProgressDialog(_context);
        pDialog.setMessage("Loading Image ...");
        pDialog.show();
    }

    @Override
    protected Bitmap doInBackground(String[] urls) {
        try {
            bitmap = BitmapFactory.decodeStream((InputStream) new URL(urls[0]).getContent());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    protected void onPostExecute(Bitmap image){
        if (image != null){
            Log.d("wtf", "1");
            _imageView.setImageBitmap(image);
            pDialog.dismiss();
        } else {
            pDialog.dismiss();
            Toast.makeText(_context,"Image Does Not exist or Network Error", Toast.LENGTH_SHORT).show();
        }

    }

}

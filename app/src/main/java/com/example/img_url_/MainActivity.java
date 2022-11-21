package com.example.img_url_;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.Toast;


import com.example.img_url_.databinding.ActivityMainBinding;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    Handler mainHandler = new Handler();
    ProgressDialog progressDialog;
    String url = "https://www.forbes.ru/rating/403469-40-samyh-uspeshnyh-zvezd-rossii-do-40-let-reyting-forbes";
    List<String> urls = new ArrayList<String>();
    List<String> names = new ArrayList<String>();
    int index_with_shift;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getContent();

        int a = (int) ( Math.random() * (urls.size()-1));
        index_with_shift = urls.size() - a - 1;
        String url1 = urls.get(index_with_shift);
        url1 = "https://cdn.forbes.ru/files/348x232/profile/" + url1;
        new FetchImage(url1).start();
        String real_name = names.get(index_with_shift);
        Log.i("Real_name", real_name);

        //Очистка поля ввода
        binding.clearbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.etURL.setText("");
            }
        });

        binding.fetchbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String guess = binding.etURL.getText().toString();
                String real_name = names.get(index_with_shift);
                Log.i("My_guess", guess);
                if(guess.equals(real_name)){
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "И правда!", Toast.LENGTH_SHORT);
                    toast.show();
                } else{
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "Неправда!", Toast.LENGTH_SHORT);
                    toast.show();
                }
                //Для следующего угадывания
                int a = (int) ( Math.random() * (urls.size()-1));
                index_with_shift = urls.size() - a - 1;
                String url1 = urls.get(index_with_shift);
                url1 = "https://cdn.forbes.ru/files/348x232/profile/" + url1;
                new FetchImage(url1).start();
                real_name = names.get(index_with_shift);
                Log.i("Real_name", real_name);
            }
        });

    }

    class FetchImage extends Thread{

        String URL;
        Bitmap bitmap;

        FetchImage(String URL){

            this.URL = URL;

        }

        @Override
        public void run() {

            mainHandler.post(new Runnable() {
                @Override
                public void run() {

                    progressDialog = new ProgressDialog(MainActivity.this);
                    progressDialog.setMessage("Getting your pic....");
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                }
            });

            InputStream inputStream = null;
            try {
                inputStream = new URL(URL).openStream();
                bitmap = BitmapFactory.decodeStream(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }

            mainHandler.post(new Runnable() {
                @Override
                public void run() {

                    if (progressDialog.isShowing())
                        progressDialog.dismiss();
                    binding.imageView.setImageBitmap(bitmap);

                }
            });

        }
    }

    private static class DownloadContentTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            URL url = null;
            HttpURLConnection urlConnection = null;
            StringBuilder result = new StringBuilder();
            try {
                url = new URL(strings[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = urlConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader reader = new BufferedReader(inputStreamReader);
                String line = reader.readLine();
                while (line != null) {
                    result.append(line);
                    line = reader.readLine();
                }
                Log.i("MyResult", result.toString());
                return result.toString();
            }
            catch (MalformedURLException e)
            {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null)
                    urlConnection.disconnect();
            }
            return null;
        }
    }

    private  static class DownloadImageTask extends AsyncTask<String, Void, Bitmap>
    {
        @Override
        protected  Bitmap doInBackground(String...strings) {
            URL url =  null;
            HttpURLConnection urlConnection = null;
            StringBuilder result = new StringBuilder();
            try {
                url = new URL(strings[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = urlConnection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                return bitmap;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection!=null)
                    urlConnection.disconnect();
            }
            return null;
        }
    }

    private  void getContent()
    {
        DownloadContentTask task = new DownloadContentTask();
        try {
            String content = task.execute(url).get();
            String start = "profile:";
            String finish = "title:a,alt:a";
            Pattern pattern = Pattern.compile(start+ "(.*?)"+ finish);
            Matcher matcher = pattern.matcher(content);
            String splitContent = "";
            while (matcher.find())
                splitContent = matcher.group(1);

            Pattern paternImg = Pattern.compile("photo:.*?uid:.*?,filename:\"(.*?)\"");
            //Pattern paternImg = Pattern.compile("title:\"(.*?)\"");
            Pattern patternName = Pattern.compile("type:m,title:\"(.*?)\"");
            Matcher matcherImg = paternImg.matcher(content);
            Matcher matcherName = patternName.matcher(content);
            while (matcherImg.find())
                urls.add(matcherImg.group(1));
                urls.remove(16);
                urls.remove(17);
                for (String url : urls)
                {
                    Log.i("My_urls", url.toString());
                }
            while (matcherName.find())
                names.add(matcherName.group(1));

            for (String s:names)
                 Log.i("NAMES", s);
            //Log.i("NAMES", names.toString());


        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

}

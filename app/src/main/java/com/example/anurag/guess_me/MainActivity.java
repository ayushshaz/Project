package com.example.anurag.guess_me;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpRetryException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    ArrayList<String> animeURLs = new ArrayList<String>();
    ArrayList<String> animeNames =new ArrayList<String>();
    int chosenanime =0;
    ImageView imageview;
    int locationOfCorrectAnswer=0;
    String[] answer = new String[4];
    Button Button1;
    Button Button2;
    Button Button3;
    Button Button4;

    private ProgressDialog progressBar;


    //firebase auth object
    private FirebaseAuth firebaseAuth;

    public void animechosen(View view){
        if(view.getTag().toString().equals(Integer.toString(locationOfCorrectAnswer))){

            Toast.makeText(getApplicationContext(),"Correct!",Toast.LENGTH_LONG).show();
    }else{

            Toast.makeText(getApplicationContext(),"Wrong! It was"+animeNames.get(chosenanime),Toast.LENGTH_LONG).show();

        }
        createNewQuestion();
    }
//
//    public class ImageDownloader extends AsyncTask<String, Void, Bitmap> {
//
//
//
//        @Override
//        protected Bitmap doInBackground(String... urls) {
//
//            try {
//                URL url = new URL(urls[0]);
//                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
//                connection.connect();
//                InputStream inputStream = connection.getInputStream();
//                Bitmap myBitmap = BitmapFactory.decodeStream(inputStream);
//                return myBitmap;
//
//
//            } catch (MalformedURLException e) {
//                e.printStackTrace();
//            } catch (IOException e){
//                e.printStackTrace();
//
//            }
//
//
//            return null;
//        }
//    }

    public class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progressBar.dismiss();
        }

        @Override
        protected String doInBackground(String... urls) {

        String result ="";
        URL url;

            HttpsURLConnection urlconnection= null;
            try {
                url =new URL(urls[0]);
                urlconnection = (HttpsURLConnection) url.openConnection();

                InputStream in = urlconnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();
                while(data!= -1){
                    char current =(char) data;
                    result += current;
                    data = reader.read();

                }
                 return result;

            }catch(Exception e){

                e.printStackTrace();

            }



            return null;
        }
    }






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = new ProgressDialog(MainActivity.this);
        progressBar.setTitle("Loading page");
        progressBar.setMessage("Please wait while loading anime page...");
        progressBar.setCancelable(false);
        progressBar.show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //initializing firebase authentication object
                        firebaseAuth = FirebaseAuth.getInstance();

                        //if the user is not logged in
                        //that means current user will return null
                        if(firebaseAuth.getCurrentUser() == null){

                            //starting login activity
                            startActivity(new Intent(MainActivity.this, EmailPasswordLoginActivity.class));

                            //closing this activity
                            finish();

                        }

                        imageview =(ImageView)findViewById(R.id.imageview);
                        Button1 =(Button)findViewById(R.id.Button1);
                        Button2 =(Button)findViewById(R.id.Button2);
                        Button3 =(Button)findViewById(R.id.Button3);
                        Button4 =(Button)findViewById(R.id.Button4);


                        //getting current user
                        FirebaseUser user = firebaseAuth.getCurrentUser();

                        DownloadTask task =new DownloadTask();
                        String result = null;

                        try {
                            result =task.execute("https://www.watchcartoononline.com").get();
                            String[] splitResult = result.split("Recent Releases - <a href=\"https://www.watchcartoononline.io/last-50-recent-release\">Last 50 Releases</a>");
                            String[] splitResult1 = splitResult[1].split("Series Recently Added");

                            Pattern p =Pattern.compile("src=\"(.*?)\"");
                            Matcher m =p.matcher(splitResult1[0]);

                            while(m.find()){
                                animeURLs.add(m.group(1));
                                System.out.println(m.group(1));
                            }
                            p =Pattern.compile("alt=\"(.*?)\"");
                            m =p.matcher(splitResult1[0]);

                            while(m.find()){

                                animeNames.add(m.group(1));
                                System.out.println(m.group(1));
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }
                        createNewQuestion();
                    }
                });
            }
        }, 1000);
    }

    public void createNewQuestion(){

        Random random = new Random();
        chosenanime =random.nextInt(animeURLs.size());
        //ImageDownloader imagetask = new ImageDownloader();
        Bitmap animeImage;

        try{
            //animeImage =imagetask.execute(animeURLs.get(chosenanime)).get();

            Picasso.get().load(animeURLs.get(chosenanime)).into(imageview);
            //imageview.setImageBitmap(animeImage);
            locationOfCorrectAnswer =random.nextInt(4);
            int inCorrectAnswerLocation;
            for(int i=0;i<4;i++){
                if(i==locationOfCorrectAnswer){
                    answer[i]=animeNames.get(chosenanime);

                }else{
                    inCorrectAnswerLocation = random.nextInt(animeURLs.size());
                    while(inCorrectAnswerLocation == chosenanime){
                        inCorrectAnswerLocation = random.nextInt(animeURLs.size());
                    }

                    answer[i]=animeNames.get(inCorrectAnswerLocation);
                }

            }
            Button1.setText(answer[0]);
            Button2.setText(answer[1]);
            Button3.setText(answer[2]);
            Button4.setText(answer[3]);


        }catch (Exception e){
            e.printStackTrace();
        }


    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.logOut){
            //logging out the user
            firebaseAuth.signOut();
            //closing activity
            finish();
            //starting login activity
            startActivity(new Intent(this, EmailPasswordLoginActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }


}

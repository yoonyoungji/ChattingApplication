package com.example.youngji.myapplication;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.UiThread;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created By youngji...
 * 기능 : 기본 socket을 통해 message send/recieve하는 기본 chatting program
 */

public class MainActivity extends AppCompatActivity {


    private EditText textField;
    private Button button;
    private TextView textView;
    private Socket client;
    private PrintWriter printWriter;
    private BufferedReader bufferedReader;

    private String realMessage = "";

    private String Server_IP = "192.168.0.197";
    private int connect_Port  = 7777;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textField = (EditText) findViewById(R.id.editText1);
        button = (Button) findViewById(R.id.button1);
        textView = (TextView) findViewById(R.id.textView1);

        ChatOperator chatOperator = new ChatOperator();
        chatOperator.execute();

    }

    /**
     * This AsyncTask create the connection with the server and initialize the
     * chat senders and receivers.
     */
    private class ChatOperator extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... arg0) {
            try {
                client = new Socket(Server_IP, connect_Port); // Creating the server socket.

                if (client != null) {
                    System.out.println("Connect to Server");
                    printWriter = new PrintWriter(client.getOutputStream(), true);
                    InputStreamReader inputStreamReader = new InputStreamReader(client.getInputStream());
                    bufferedReader = new BufferedReader(inputStreamReader);
                } else {
                    System.out.println("Server has not bean started on port 8080.");
                }
            } catch (UnknownHostException e) {
                System.out.println("Faild to connect server " + Server_IP);
                e.printStackTrace();
            } catch (IOException e) {
                System.out.println("Faild to connect server " + Server_IP);
                e.printStackTrace();
            }
            return null;
        }

        /**
         * Following method is executed at the end of doInBackground method.
         */
        @Override
        protected void onPostExecute(Void result) {
            button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    realMessage = textField.getText().toString();
                    final Sender messageSender = new Sender(); // Initialize chat sender AsyncTask.
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        messageSender.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    } else {
                        messageSender.execute();
                    }
                }
            });

            Receiver receiver = new Receiver(); // Initialize chat receiver AsyncTask.
            receiver.execute();

        }

    }

    /**
     * This AsyncTask continuously reads the input buffer and show the chat
     * message if a message is availble.
     */
    private class Receiver extends AsyncTask<Void, Void, Void> {

        private String message;

        @Override
        protected Void doInBackground(Void... params) {
            while (true) {
                try {
                    if (bufferedReader.ready()) {
                        message = bufferedReader.readLine();
                        publishProgress(null);
                    }
                }
                catch (NullPointerException e){
                    e.printStackTrace();
                }
                catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                }
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            textView.append("Server: " + message + "\n");
        }

    }

    /**
     * This AsyncTask sends the chat message through the output stream.
     */
    private class Sender extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {

            try{
                printWriter.write(realMessage + "\n");
                printWriter.flush();
            }
            catch (Exception e){
                e.printStackTrace();
            }


            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            textField.setText(""); // Clear the chat box
            textView.append("Client: " + realMessage + "\n");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

}

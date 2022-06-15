package com.example.dial_test;

import android.os.Bundle;

import com.example.simpleconnect.dial.Device;
import com.example.simpleconnect.dial.*;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.dial_test.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        MainActivity context = this;
        SimpleConnect dial = new Dial();
        dial.requestDevices(devices -> {
            System.out.println("Devices detected through DIAL: " + devices.size());
            if (devices.size() > 0) {
                LinearLayout buttonHolder = (LinearLayout) findViewById(R.id.buttonHolder);
                for (int i = 0; i < devices.size(); i++) {
                    Device device = devices.get(i);
                    Button button = new Button(context);
                    button.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    button.setId(View.generateViewId());
                    button.setText(device.friendlyName);
                    buttonHolder.addView(button);
                    button.setOnClickListener(click -> {
                        System.out.println("Device info: " + device.toString());
                        device.get("Netflix", application -> {
                            if (application == null) {
                                System.out.println("Application is not installed.");
                            } else {
                                System.out.println("Application is: " + application.state);
                                if (!application.isRunning()) {
                                    device.launch(application, null, success -> {
                                        System.out.println("Application is launched with location " + application.instanceUrl);
                                    });
                                }

                            }
                        });

                    });
                }
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

}
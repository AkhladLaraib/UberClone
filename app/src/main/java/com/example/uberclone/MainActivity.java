package com.example.uberclone;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.parse.ParseAnonymousUtils;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.shashank.sony.fancytoastlib.FancyToast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText edtUsername, edtPassword, edtOneTime;
    private Button btnSingupLogin, btnOneTime;
    private RadioButton radioButtonPassenger, radioButtonDriver;

    @Override
    public void onClick(View v) {

        if (edtOneTime.getText().toString().equals("Driver") ||
                edtOneTime.getText().toString().equals("Passenger")) {

            if (ParseUser.getCurrentUser() == null) {
                ParseAnonymousUtils.logIn((user, e) -> {

                    if (user != null && e == null) {
                        FancyToast.makeText(MainActivity.this, "We have an anonymous user",
                                FancyToast.LENGTH_SHORT, FancyToast.SUCCESS, false).show();

                        user.put("as", edtOneTime.getText().toString());

                        user.saveInBackground(e1 -> {

                            transitionToPassengerActivity();
                        });
                    }
                });
            }
        } else {
            FancyToast.makeText(MainActivity.this, "Are you a Driver or Passenger???",
                    FancyToast.LENGTH_SHORT, FancyToast.INFO, false).show();
            return;
        }

    }

    enum State {
        SIGNUP, LOGIN
    }

    private State state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.myToolbar);
        setSupportActionBar(toolbar);

        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        edtOneTime = findViewById(R.id.edtOneTime);

        btnSingupLogin = findViewById(R.id.btnSingupLogin);
        btnOneTime = findViewById(R.id.btnOneTime);

        radioButtonPassenger = findViewById(R.id.radioButtonPassenger);
        radioButtonDriver = findViewById(R.id.radioButtonDriver);

        btnOneTime.setOnClickListener(MainActivity.this);

        ParseInstallation.getCurrentInstallation().saveInBackground();

        if (ParseUser.getCurrentUser() != null) {

//            ParseUser.logOut();
            transitionToPassengerActivity();
        }

        state = State.SIGNUP;

        btnSingupLogin.setOnClickListener(v -> {

            if (state == State.SIGNUP) {

                if (!radioButtonDriver.isChecked() && !radioButtonPassenger.isChecked()) {

                    FancyToast.makeText(MainActivity.this, "Are you a driver or a passenger?",
                            FancyToast.LENGTH_SHORT, FancyToast.INFO, false).show();
                    return;
                }
                ParseUser appUser = new ParseUser();
                appUser.setUsername(edtUsername.getText().toString());
                appUser.setPassword(edtPassword.getText().toString());

                if (radioButtonDriver.isChecked())
                    appUser.put("as", "Driver");

                else if (radioButtonPassenger.isChecked())
                    appUser.put("as", "Passenger");

                appUser.signUpInBackground((e) -> {
                    if (e == null)
                        FancyToast.makeText(MainActivity.this, "Signed Up",
                                FancyToast.LENGTH_SHORT, FancyToast.SUCCESS,
                                false).show();
                    transitionToPassengerActivity();
                });
            } else if (state == State.LOGIN) {
                ParseUser.logInInBackground(edtUsername.getText().toString(),
                        edtPassword.getText().toString(),
                        (user, e) -> {
                            if (user != null && e == null) {
                                FancyToast.makeText(MainActivity.this, "Logged In",
                                        FancyToast.LENGTH_SHORT, FancyToast.SUCCESS,
                                        false).show();
                                transitionToPassengerActivity();
                            }
                        });
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_signup_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.loginItem) {

            if (state == State.SIGNUP) {
                state = State.LOGIN;
                item.setTitle(R.string.sign_up);
                btnSingupLogin.setText(R.string.login);
            } else if (state == State.LOGIN) {
                state = State.SIGNUP;
                item.setTitle(R.string.login);
                btnSingupLogin.setText(R.string.sign_up);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void transitionToPassengerActivity() {

        if (ParseUser.getCurrentUser() != null) {
            if (ParseUser.getCurrentUser().get("as").equals("Passenger")) {

                startActivity(new Intent(MainActivity.this, PassengerActivity.class));

            }
        }
    }

}
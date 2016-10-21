package ch.kurky.beerapp;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.baasbox.android.BaasBox;
import com.baasbox.android.BaasClientException;
import com.baasbox.android.BaasException;
import com.baasbox.android.BaasResult;
import com.baasbox.android.BaasServerException;
import com.baasbox.android.BaasUser;

public class LoginActivity extends AppCompatActivity {

    private LoginTask loginTask;

    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button loginButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if ( BaasUser.current().isAuthentcated()) {
            onUserLogged();
            return;
        }

        setTitle("Login");

        setContentView(R.layout.activity_login);

        usernameEditText = (EditText) findViewById(R.id.txtName);
        passwordEditText = (EditText) findViewById(R.id.txtPassword);
        loginButton = (Button) findViewById(R.id.btnLogin);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                //onClickLogin(username, password);
                onClickLogin("admin", "kugelschreiber");
            }
        });

        /*findViewById(R.id.signupLink).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickSignup();
            }
        });*/
    }

    private void onUserLogged() {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("login", true);
        setResult(RESULT_OK,returnIntent);
        finish();
    }

    protected void onClickSignup() {
        //Intent intent = new Intent(this, SignupActivity.class);
        //startActivity(intent);
    }

    protected void onClickLogin(String username, String password) {
        loginTask = new LoginTask();
        loginTask.execute(username, password);
    }

    protected void onLogin(BaasResult<BaasUser> result) {
        try {
            result.get();
            onUserLogged();
        } catch (BaasClientException e) {
            if (e.httpStatus == 401) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setCancelable(true);
                builder.setTitle("Login failed");
                builder.setMessage("Invalid username or password");
                builder.setNegativeButton("Cancel", null);
                builder.create().show();
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setCancelable(true);
                builder.setTitle("Error");
                builder.setMessage("Error: " + e);
                builder.setNegativeButton("Cancel", null);
                builder.create().show();
            }
        } catch (BaasServerException e) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(true);
            builder.setTitle("Error");
            builder.setMessage("Error: " + e);
            builder.setNegativeButton("Cancel", null);
            builder.create().show();
        } catch (BaasException e) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(true);
            builder.setTitle("Error");
            builder.setMessage("Error: " + e);
            builder.setNegativeButton("Cancel", null);
            builder.create().show();
        }
    }

    public class LoginTask extends AsyncTask<String, Void, BaasResult<BaasUser>> {

        @Override
        protected void onPreExecute() {
            loginButton.setEnabled(false);
        }

        @Override
        protected BaasResult<BaasUser> doInBackground(String... params) {
            BaasUser user = BaasUser.withUserName(params[0]);
            user.setPassword(params[1]);
            return user.loginSync();
        }

        @Override
        protected void onPostExecute(BaasResult<BaasUser> result) {
            loginButton.setEnabled(true);
            onLogin(result);
        }
    }
}

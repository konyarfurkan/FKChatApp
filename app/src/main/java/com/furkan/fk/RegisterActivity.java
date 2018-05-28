package com.furkan.fk;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;


public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout displayName, email, password;
    private Button create_Account;
    private Toolbar toolbar;
    private ProgressDialog progressDialog;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference firebaseDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        toolbar = findViewById(R.id.register_page_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Create Account");

        progressDialog = new ProgressDialog(this);

        firebaseAuth = FirebaseAuth.getInstance();

        displayName = findViewById(R.id.register_display_name);
        email = findViewById(R.id.register_email);
        password = findViewById(R.id.register_password);
        create_Account = findViewById(R.id.register_createAccount_button);

        create_Account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String display_name = displayName.getEditText().getText().toString();
                String account_email = email.getEditText().getText().toString();
                String account_password = password.getEditText().getText().toString();

                if (!TextUtils.isEmpty(display_name) || !TextUtils.isEmpty(account_email) || !TextUtils.isEmpty(account_password)) {

                    progressDialog.setTitle("Registering User");
                    progressDialog.setMessage("Please wait while we create your account !");
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();

                    registerUser(display_name, account_email, account_password);

                }

            }
        });

    }

    private void registerUser(final String display_name, String account_email, String account_password) {

        firebaseAuth.createUserWithEmailAndPassword(account_email, account_password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()) {

                    FirebaseUser currentUser=FirebaseAuth.getInstance().getCurrentUser();
                    String uid=currentUser.getUid();
                    String deviceToken= FirebaseInstanceId.getInstance().getToken();

                    firebaseDatabase=FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

                    HashMap<String,String> userMap=new HashMap<>();
                    userMap.put("device_token",deviceToken);
                    userMap.put("display_name",display_name);
                    userMap.put("status","Hi there I'm using FK");
                    userMap.put("image","default");
                    userMap.put("thumb_image","default");

                    firebaseDatabase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            progressDialog.dismiss();

                            Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
                            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(mainIntent);
                            finish();

                        }
                    });

                } else {

                    progressDialog.hide();
                    Toast.makeText(RegisterActivity.this, "Please check the form and try again", Toast.LENGTH_SHORT).show();

                }

            }
        });

    }
}

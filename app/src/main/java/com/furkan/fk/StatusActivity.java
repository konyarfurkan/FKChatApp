package com.furkan.fk;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

public class StatusActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private Button save_changes;
    private TextInputLayout status;
    private ProgressDialog progressDialog;

    private DatabaseReference usersDatabaseReference;
    private FirebaseUser current_user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        toolbar = findViewById(R.id.status_appBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Account Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        String current_status = getIntent().getStringExtra("current_status");

        current_user = FirebaseAuth.getInstance().getCurrentUser();
        String current_user_uid = current_user.getUid();
        usersDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(current_user_uid);

        status = findViewById(R.id.status_input);
        status.getEditText().setText(current_status);
        save_changes = findViewById(R.id.status_saveChanges_button);

        save_changes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                progressDialog = new ProgressDialog(StatusActivity.this);
                progressDialog.setTitle("Saving Changes");
                progressDialog.setMessage("Please wait while we save the changes");
                progressDialog.show();

                String update_status = status.getEditText().getText().toString();
                usersDatabaseReference.child("status").setValue(update_status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {

                            progressDialog.dismiss();

                        } else {

                            Toast.makeText(StatusActivity.this, "There was some error in saving changes", Toast.LENGTH_SHORT).show();

                        }

                    }
                });

            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();

        FirebaseDatabase.getInstance().getReference().child("Users").child(current_user.getUid()).child("online").setValue(ServerValue.TIMESTAMP);


    }

    @Override
    protected void onResume() {
        super.onResume();

        FirebaseDatabase.getInstance().getReference().child("Users").child(current_user.getUid()).child("online").setValue("true");


    }

}

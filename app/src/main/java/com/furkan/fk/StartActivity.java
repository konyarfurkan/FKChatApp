package com.furkan.fk;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenManager;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;


import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.util.HashMap;


public class StartActivity extends AppCompatActivity {

    private Button register_button, login_button;

    private String image;
    private ProgressDialog gprogressDialog;
    private ProgressDialog fprogressDialog;
    private static int RC_SIGN_IN = 101;

    private GoogleSignInClient googleSignInClient;
    private SignInButton googleSignInButton;

    private CallbackManager callbackManager;
    private LoginButton facebookSignInButton;

    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        keyhash();
        firebaseAuth = FirebaseAuth.getInstance();
        login_button = findViewById(R.id.start_LoginButton);
        register_button = findViewById(R.id.start_register_button);
        googleSignInButton = findViewById(R.id.googleSignIn_button);
        callbackManager = CallbackManager.Factory.create();
        facebookSignInButton = findViewById(R.id.facebookSignIn_button);
        gprogressDialog = new ProgressDialog(this);
        fprogressDialog = new ProgressDialog(this);


        register_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent registerIntent = new Intent(StartActivity.this, RegisterActivity.class);
                startActivity(registerIntent);

            }
        });

        login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent loginIntent = new Intent(StartActivity.this, LoginActivity.class);
                startActivity(loginIntent);

            }
        });

        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);

        googleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent signInIntent = googleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);

            }
        });

        facebookSignInButton.setReadPermissions("email", "public_profile");

        facebookSignInButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d("", "facebook:onSuccess:" + loginResult);

                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        Log.d("response", response.toString());
                        getData(object);

                    }
                });

                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,gender,link,email,birthday,friends");
                request.setParameters(parameters);
                request.executeAsync();

                handleFacebookAccessToken(loginResult.getAccessToken());


            }

            @Override
            public void onCancel() {
                Log.d("", "facebook:onCancel");
                // ...
            }

            @Override
            public void onError(FacebookException error) {
                Log.d("", "facebook:onError", error);
                // ...
            }
        });


    }

    private void getData(JSONObject object) {

        try {
            URL profile_picture = new URL("https://graph.facebook.com/" + object.getString("id") + "/picture?width=350&height=350");
            image = profile_picture.toString();
        } catch (MalformedURLException e) {

            e.printStackTrace();

        } catch (JSONException e) {

            e.printStackTrace();

        }

    }

    private void handleFacebookAccessToken(AccessToken accessToken) {

        Log.d("", "handleFacebookAccessToken:" + accessToken);

        AuthCredential credential = FacebookAuthProvider.getCredential(accessToken.getToken());
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("", "signInWithCredential:success");

                            gprogressDialog.setTitle("Logging in");
                            gprogressDialog.setMessage("Please wait while we check your credentials");
                            gprogressDialog.setCanceledOnTouchOutside(false);
                            gprogressDialog.show();

                            final FirebaseUser current_user = firebaseAuth.getCurrentUser();
                            final String current_user_id = current_user.getUid();

                            databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(current_user_id);

                            FirebaseDatabase.getInstance().getReference().child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    if (!dataSnapshot.hasChild(current_user_id)) {


                                        HashMap<String, String> userMap = new HashMap<>();

                                        String deviceToken = FirebaseInstanceId.getInstance().getToken();

                                        userMap.put("display_name", current_user.getDisplayName());
                                        userMap.put("status", "Hi there I'm using FK");
                                        userMap.put("image", image);
                                        userMap.put("thumb_image", "default");
                                        userMap.put("device_token",deviceToken);

                                        databaseReference.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                gprogressDialog.dismiss();

                                                Intent mainIntent = new Intent(StartActivity.this, MainActivity.class);
                                                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(mainIntent);
                                                finish();

                                            }
                                        });

                                    } else {

                                        String deviceToken = FirebaseInstanceId.getInstance().getToken();

                                        databaseReference.child("device_token").setValue(deviceToken).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {

                                                Intent mainIntent = new Intent(StartActivity.this, MainActivity.class);
                                                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(mainIntent);
                                                finish();

                                            }
                                        });


                                    }

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });


                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("", "signInWithCredential:failure", task.getException());
                            Toast.makeText(StartActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        }

                    }
                });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {

            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {

                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);

            } catch (ApiException e) {

                Log.w("TAG", "Google sign in failed", e);

            }
        }
        callbackManager.onActivityResult(requestCode, resultCode, data);


    }


    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);

        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            gprogressDialog.setTitle("Logging in");
                            gprogressDialog.setMessage("Please wait while we check your credentials");
                            gprogressDialog.setCanceledOnTouchOutside(false);
                            gprogressDialog.show();

                            final FirebaseUser current_user = firebaseAuth.getCurrentUser();
                            final String current_user_id = current_user.getUid();

                            databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(current_user_id);

                            FirebaseDatabase.getInstance().getReference().child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    if (!dataSnapshot.hasChild(current_user_id)) {

                                        String deviceToken = FirebaseInstanceId.getInstance().getToken();

                                        HashMap<String, String> userMap = new HashMap<>();
                                        userMap.put("display_name", current_user.getDisplayName());
                                        userMap.put("status", "Hi there I'm using FK");
                                        userMap.put("image", "default");
                                        userMap.put("thumb_image", "default");
                                        userMap.put("device_token",deviceToken);

                                        databaseReference.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                gprogressDialog.dismiss();

                                                Intent mainIntent = new Intent(StartActivity.this, MainActivity.class);
                                                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(mainIntent);
                                                finish();

                                            }
                                        });

                                    } else {

                                        String deviceToken = FirebaseInstanceId.getInstance().getToken();

                                        databaseReference.child("device_token").setValue(deviceToken).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {

                                                Intent mainIntent = new Intent(StartActivity.this, MainActivity.class);
                                                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(mainIntent);
                                                finish();

                                            }
                                        });

                                    }

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });


                        } else {

                            Toast.makeText(getApplicationContext(), "User could not log in", Toast.LENGTH_SHORT).show();

                        }

                    }
                });
    }

    private void keyhash() {


        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.furkan.fk",//Projenin paket ismini yazıyoruz
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
                System.out.println("KKKKK: " + Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.d("KeyHash:", e.toString());
        } catch (NoSuchAlgorithmException e) {
            Log.d("KeyHash:", e.toString());
        }

    }

}

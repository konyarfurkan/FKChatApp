package com.furkan.fk;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;


    private Toolbar toolbar;
    private ViewPager viewPager;
    private SectionsPagerAdapter sectionsPagerAdapter;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar=findViewById(R.id.main_page_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("FK");

        firebaseAuth=FirebaseAuth.getInstance();

        viewPager=findViewById(R.id.main_tabPager);
        sectionsPagerAdapter=new SectionsPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(sectionsPagerAdapter);
        tabLayout=findViewById(R.id.main_tabs);
        tabLayout.setupWithViewPager(viewPager);

    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if(currentUser==null){

            sendToStartActivity();

        }
    }

    private void sendToStartActivity() {

        Intent startIntent=new Intent(MainActivity.this,StartActivity.class);
        startActivity(startIntent);
        finish();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.main_menu,menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if(item.getItemId()==R.id.main_logout_button){

            LoginManager.getInstance().logOut(); //for fb button
            FirebaseAuth.getInstance().signOut();
            sendToStartActivity();

        }

        if(item.getItemId()==R.id.main_settings_button){

            Intent settingsIntent=new Intent(MainActivity.this,SettingsActivity.class);
            startActivity(settingsIntent);

        }

        if(item.getItemId()==R.id.main_allUsers_button){

            Intent settingsIntent=new Intent(MainActivity.this,UsersActivity.class);
            startActivity(settingsIntent);

        }



        return true;
    }
}

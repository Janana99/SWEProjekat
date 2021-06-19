package com.example.projekatproba;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    FirebaseAuth baseAuth;
    private DrawerLayout drawer;
    private FirebaseFirestore docRef= FirebaseFirestore.getInstance();

    FirebaseUser user;
    TextView username;
    TextView email;
    ImageView srch;
    ImageView image;
    ImageView userProfiles;
    ImageView adding;
    Boolean userA;
    RecyclerView recyclerView;
    List<Recept> receptList;
    Recept recept;
    RatingBar ratingBar;
    Button ocenite;
    AdapterReceptiProfiliKorisnika adapterReceptiProfiliKorisnika;
    String userIdValue;

    List<String> datum;
    List<String> mail;
    List<String> ime;
    List<String> sifra;
    List<String> slikeUrl;
    List<String> prezime;
    List<String> korime;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        srch= findViewById(R.id.search);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        baseAuth=FirebaseAuth.getInstance();
        user=baseAuth.getCurrentUser();
        userA=baseAuth.getCurrentUser().isAnonymous();
        adding= findViewById(R.id.add);

        recyclerView= findViewById(R.id.dataListRHome);
        receptList=new ArrayList<>();
        ratingBar=findViewById(R.id.rating_bar);
        ocenite=findViewById(R.id.button_recenzija);
        userIdValue=FirebaseAuth.getInstance().getCurrentUser().getUid();
        Context ctx = this;

        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        CollectionReference receptRef= docRef.collection("recepti");
        receptRef.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {

                                GridLayoutManager gridLayoutManager= new GridLayoutManager(ctx, 1);
                                recyclerView.setLayoutManager(gridLayoutManager);

                                recept =new Recept(document.getId(), document.getString("naziv"),document.getString("priprema"),document.getString("sastojci"),Long.parseLong( document.get("datum").toString()),document.getString("idPublishera"),document.getString("Img"),document.getString("ocena"));
                                Log.d("TAG", "onComplete:"+recept.getNaziv());
                                receptList.add(recept);
                                adapterReceptiProfiliKorisnika= new AdapterReceptiProfiliKorisnika(ctx, receptList);
                                recyclerView.setAdapter(adapterReceptiProfiliKorisnika);


                            }
                        } else {
                            Log.d("TAG", "Error getting documents: ", task.getException());
                        }
                    }
                });


        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_home);
        }

        View headerView=navigationView.getHeaderView(0);
        username=headerView.findViewById(R.id.usrnameNav);
        email=headerView.findViewById(R.id.emailNav);
        image= headerView.findViewById(R.id.profilePicture);
        userProfiles=findViewById(R.id.users);
        String user1=user.getUid();
        Log.d("TAG", user1+" AAAAAAAAAAAAAAAAAAAAAA");
        DocumentReference documentReference=docRef.collection("korisnici").document(user1);
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        username.setText(document.get("username").toString());
                        email.setText(document.get("email").toString());
                        if(!(document.get("profileImageUrl").toString().equals("default")))
                        Picasso.get().load(document.get("profileImageUrl").toString()).into(image);
                        Log.d("TAG:", "DocumentSnapshot data: " + document.getData());
                    } else {
                        Log.d("TAG:", "No such document");
                    }
                } else {
                    Log.d("TAG", "get failed with ", task.getException());
                }
            }
        });

        if(userA == true){
            userProfiles.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getBaseContext(), "Morate se registrovati kako biste imali pristup! ", Toast.LENGTH_SHORT).show();
                }
            });

            adding.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getBaseContext(), "Morate se registrovati kako biste imali pristup! ", Toast.LENGTH_SHORT).show();
                }
            });

        }
        else{
            userProfiles.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EditText pretraga = new EditText (v.getContext());
                    AlertDialog.Builder pretragaDijalog = new AlertDialog.Builder(v.getContext());
                    pretragaDijalog.setTitle("Pretraga profila");
                    pretragaDijalog.setMessage("Unesite username za profil za koji želite da vršite pretragu");
                    pretragaDijalog.setView(pretraga);

                    pretragaDijalog.setPositiveButton("Pretraga", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            Log.d("TAG:","MAJKOOOOOOOOOOOOOOOOOOO " + pretraga.getText().toString());

                            CollectionReference usersRef= docRef.collection("korisnici");
                            Query query=usersRef.whereEqualTo("username",pretraga.getText().toString());

                            query.get()
                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            if (task.isSuccessful()) {
                                                for (QueryDocumentSnapshot document : task.getResult()) {
                                                    String pom = document.getString("username");

                                                    if(pom.equals(pretraga.getText().toString()))
                                                    {
                                                        ArrayList<String> lista;
                                                        lista=new ArrayList<>();

                                                        lista.add(document.get("email").toString());
                                                        lista.add(document.get("name").toString());
                                                        lista.add(document.get("surname").toString());
                                                        lista.add(document.get("profileImageUrl").toString());
                                                        lista.add(document.get("username").toString());
                                                        lista.add(document.getId());


                                                        Intent intent=new Intent(HomeActivity.this, ProfileActivity.class);
                                                        intent.putStringArrayListExtra("List", lista);
                                                        startActivity(intent);
                                                    }
                                                    Log.d("TAG","user exist");
                                                }
                                            }
                                            else {
                                                Log.d("TAG", "Error getting documents: ", task.getException());
                                            }
                                            if(task.getResult().size()==0){
                                                Toast.makeText(getBaseContext(), "Traženi korisnik ne postoji! ", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }
                    });

                    pretragaDijalog.setNegativeButton("Odustani", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Toast.makeText(HomeActivity.this, "Neuspesno! ", Toast.LENGTH_SHORT).show();
                        }
                    });
                    pretragaDijalog.create().show();
                }
            });

            adding.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(getBaseContext(), DodavanjeRecepataActivity.class));
                }
            });



        }


        srch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getBaseContext(), SearchActivity.class));
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_home:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
                break;
            case R.id.nav_profile:
                if(userA == true){
                    Toast.makeText(getBaseContext(), "Morate se registrovati kako biste imali pristup! ", Toast.LENGTH_SHORT).show();
                }
                else{
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ProfileFragment()).commit();
                }
                break;
            case R.id.nav_notifications:
                if(userA == true){
                    Toast.makeText(getBaseContext(), "Morate se registrovati kako biste imali pristup! ", Toast.LENGTH_SHORT).show();
                }
                else{
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new NotificationsFragment()).commit();
                }                break;
            case R.id.nav_settings_username:
                if(userA == true){
                    Toast.makeText(getBaseContext(), "Morate se registrovati kako biste imali pristup! ", Toast.LENGTH_SHORT).show();
                }
                else{
                    startActivity(new Intent(getApplicationContext(), ChangeUsernameActivity.class));
                }
                break;
            case R.id.nav_settings_password:
                if(userA == true){
                    Toast.makeText(getBaseContext(), "Morate se registrovati kako biste imali pristup! ", Toast.LENGTH_SHORT).show();
                }
                else{
                    startActivity(new Intent(getApplicationContext(), ChangePasswordActivity.class));
                }
                break;
            case R.id.nav_settings_delete_account:
                if(userA == true){
                    Toast.makeText(getBaseContext(), "Morate se registrovati kako biste imali pristup! ", Toast.LENGTH_SHORT).show();
                }
                else{
                    startActivity(new Intent(getApplicationContext(), DeleteAccountActivity.class));
                }
                break;
            case R.id.nav_logout:
                if(userA == true){
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    finish();
                }
                else{
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(getApplicationContext(), LogInActivity.class));
                    finish();
                }
                break;
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
package com.example.hungerescape;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    String DATA_URL = "https://developers.zomato.com/api/v2.1/cities?q=";
    TextView location_text;

    List<Address> addresses;
    String current_City;
    Geocoder geocoder;
    Double lat,lon;
    String JSON_ARRAY = "location_suggestions";
    String class_name,session;
    SearchView searchView;
    ArrayList<ModelCity> arrayList;
    CityAdapter cityAdapter;
    RecyclerView recyclerView;
    private RequestQueue mQueue;
    ProgressDialog loading;
    Button button;
    private FusedLocationProviderClient fusedLocationProviderClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        location_text=findViewById(R.id.loc);
        fusedLocationProviderClient= LocationServices.getFusedLocationProviderClient(this);
        getLocation();
//        if(!current_City.isEmpty()){
//        getData(current_City);}

        searchView=findViewById(R.id.searchView);

        button=findViewById(R.id.search);
        mQueue= Volley.newRequestQueue(this);


        arrayList=new ArrayList<>();

        recyclerView=(RecyclerView) findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        cityAdapter = new CityAdapter(arrayList, MainActivity.this);
        recyclerView.setAdapter(cityAdapter);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence charSequence=searchView.getQuery();
                String str=""+charSequence;
                getData(str);
            }
        });

        recyclerView.setAdapter(cityAdapter);


    }

    public void getData(String query) {



        loading = ProgressDialog.show(this, "Please wait...", "Fetching...", false, false);


        String url = DATA_URL + query;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        loading.dismiss();
                        showJSON(response);

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, ""+error, Toast.LENGTH_SHORT).show();
            }


        }) {
            @Override
            public Map getHeaders() throws AuthFailureError {
                Map headers = new HashMap<>();

                headers.put("Accept", "application/json");
                headers.put("user-key", "2531c045d84e4f82f595063c9eb8a149");
                return headers;
            }
        };
        mQueue.add(request);
    }
    private void showJSON(JSONObject response){

        try {
            JSONObject jsonObject = new JSONObject(String.valueOf(response));
            JSONArray result = jsonObject.getJSONArray(JSON_ARRAY);
            for (int i=0; i<result.length(); i++ ){
                JSONObject ob=result.getJSONObject(i);

                ModelCity history=new ModelCity(ob.getString("country_flag_url")
                        ,ob.getString("name"),ob.getString("country_name"),ob.getString("id"));

                arrayList.add(history);
            }



        } catch (JSONException e) {
            e.printStackTrace();
        }

        recyclerView.setAdapter(cityAdapter);
        cityAdapter.notifyDataSetChanged();

    }
    public void getLocation(){
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.M){
            if(getApplicationContext().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)==
                    PackageManager.PERMISSION_GRANTED){
                fusedLocationProviderClient.getLastLocation()
                        .addOnSuccessListener(new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                if(location!=null){
                                    lat=location.getLatitude();
                                    lon= location.getLongitude();

                                    Toast.makeText(MainActivity.this,"location accessed successfully!",Toast.LENGTH_LONG).show();


                                    geocoder = new Geocoder(MainActivity.this, Locale.getDefault());

                                    try {

                                        addresses = geocoder.getFromLocation(lat, lon, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                    String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                                   current_City = addresses.get(0).getLocality();
                                    String state = addresses.get(0).getAdminArea();
                                    String country = addresses.get(0).getCountryName();
                                    String postalCode = addresses.get(0).getPostalCode();
                                    String knownName = addresses.get(0).getFeatureName(); // Only if available else return NULL
                                    location_text.setText("You are at "+current_City+","+state+"!");
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this,"location cannot be accessed!",Toast.LENGTH_LONG).show();

                    }
                });

            }else{
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},200);
            }
        }
    }


}

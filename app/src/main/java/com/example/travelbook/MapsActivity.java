package com.example.travelbook;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap;  //harita nesnesi
    LocationManager locationManager;   //kullanicin yerini bulmak için lmanager sınıfı kullanılır
    LocationListener locationListener;   //lmanagera yardımcı
    static SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {  //harita açıldıgında koşan fonksiyon
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {  //harita hazır oldugunda yapılacak işlemler
        mMap = googleMap;
        mMap.setOnMapLongClickListener(this);
        Intent intent = getIntent();
        String info = intent.getStringExtra("info");

        if (info.matches("new")) { //yeni bir yer eklemem gerekiyorsa(gelen position)
            //locationManager ve locationListener tanımlamalarımı yaptım
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    //kaydetmemiz gereken şeyleri sharedpreferencesin içine kaydederiz
                    SharedPreferences sharedPreferences = MapsActivity.this.getSharedPreferences("com.example.travelbook", MODE_PRIVATE);
                    boolean firstTimeCheck = sharedPreferences.getBoolean("notFirstTime", false);
                    if (!firstTimeCheck) { //kullanici bu appi ilk defa kullanıyorsa false gelir
                         //userLocationu güncellerim
                        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
                   
                        sharedPreferences.edit().putBoolean("notFirstTime", true).apply();
                    }


                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            };
            if (Build.VERSION.SDK_INT >= 23) {
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                   //izin yoksa izin istenir
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                } else {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                    mMap.clear();
                    //son bilinen lokasyonu alma
                    Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    //lokasyonu enlem boylam cinsinden olan br Latlnga çevirdim
                    LatLng lastUserLocation = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation, 15));
                }
            } else { //gelen position yeni bir pozisyon  değilse
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (lastLocation != null) {
                    LatLng lastUserLocation = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation, 15));
                }
            }


        }
        else {
            mMap.clear();
            int position=intent.getIntExtra("position",0);
            LatLng location=new LatLng(MainActivity.locations.get(position).latitude,MainActivity.locations.get(position).longitude);
            String placeName=MainActivity.names.get(position);

            mMap.addMarker(new MarkerOptions().title(placeName).position(location));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location,15));
        }
    }
    //izin varsa
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(grantResults.length>0){ //bir sonuç geldiyse(grandResult sonuç demek)
            if(requestCode==1){
                if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);


                    Intent intent=getIntent();
                    String info=intent.getStringExtra("info");
                    if(info.matches("new")){
                        Location lastLocation=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                        if(lastLocation!=null){
                            LatLng lastUserLocation=new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation,15));
                        }

                    }else{
                        int position=intent.getIntExtra("position",0);
                        LatLng location=new LatLng(MainActivity.locations.get(position).latitude,MainActivity.locations.get(position).longitude);
                        String placeName=MainActivity.names.get(position);

                        mMap.addMarker(new MarkerOptions().title(placeName).position(location));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location,15));
                    }

                }
            }
        }
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        Geocoder geocoder=new Geocoder(getApplicationContext(), Locale.getDefault());

        //aldığım adresleri bir stringde tutuyorum
        String address="";

        try {
            //bir adres listesi oluşturuyorum
            List<Address> addressList=geocoder.getFromLocation(latLng.latitude,latLng.longitude,1);
            if(addressList!=null && addressList.size()>0){
                if(addressList.get(0).getThoroughfare()!=null) {  //cadde adı alma
                    address += addressList.get(0).getThoroughfare();

                    if (addressList.get(0).getSubThoroughfare() != null) { //caddedeki numarası(sokak adı gibi)
                        address += addressList.get(0).getSubThoroughfare();
                    }
                }

            }
            else{ //adres boşsa
                address="new Place";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        //adresi alamadıysak:
        if(address.matches("")){
            address="no address";
        }
        //marker oluşturma
        mMap.addMarker(new MarkerOptions().position(latLng).title(address));
        Toast.makeText(getApplicationContext(),"New Place OK!",Toast.LENGTH_SHORT).show();

        MainActivity.names.add(address);
        MainActivity.locations.add(latLng);
        MainActivity.arrayAdapter.notifyDataSetChanged();

        try {
            Double l1=latLng.latitude;
            Double l2=latLng.longitude;

            String coord1=l1.toString();
            String coord2=l2.toString();

            database=this.openOrCreateDatabase("Places",MODE_PRIVATE,null);//places isimli database

            database.execSQL("CREATE TABLE IF NOT EXISTS places(name VARCHAR,latitude VARCHAR,longitude VARCHAR)");
            String toCompile="INSERT INTO places(name,latitude,longitude) VALUES(?,?,?)";
            SQLiteStatement sqLiteStatement=database.compileStatement(toCompile);
            sqLiteStatement.bindString(1,address);
            sqLiteStatement.bindString(2,coord1);
            sqLiteStatement.bindString(3,coord2);

            sqLiteStatement.execute();


        }catch (Exception e){

        }

    }
}
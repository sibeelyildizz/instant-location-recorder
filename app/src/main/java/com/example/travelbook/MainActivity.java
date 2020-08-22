package com.example.travelbook;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import static android.widget.ListView.*;

public class MainActivity extends AppCompatActivity {

    static ArrayList<String> names = new ArrayList<String>();
    static ArrayList<LatLng> locations = new ArrayList<LatLng>();
    static ArrayAdapter arrayAdapter;


    //menuuyu bağlamak için
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.add_place, menu);
        return super.onCreateOptionsMenu(menu);
    }
     //menüye tıklandıgında
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.add_place) {
            Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
            intent.putExtra("info","new"); //yeni yer
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toast.makeText(getApplicationContext(),"if you want to remove from list, you have to press long click item. ", Toast.LENGTH_LONG).show();

        ListView listView = (ListView) findViewById(R.id.listView);

        try {

            MapsActivity.database = this.openOrCreateDatabase("Places", MODE_PRIVATE, null);//ilgili veritabanını açma
            Cursor cursor = MapsActivity.database.rawQuery("SELECT * FROM places", null);

            int nameIx = cursor.getColumnIndex("name");
            int latitudeIx = cursor.getColumnIndex("latitude");
            int longitudeIX = cursor.getColumnIndex("longitude");


            while (cursor.moveToNext()) {
                //verileri alma
                String nameFromDatabase = cursor.getString(nameIx);
                String latitudeFromDatabase = cursor.getString(latitudeIx);
                String longitudeFromDatabase = cursor.getString(longitudeIX);

                //arraylistlere ekleme
                names.add(nameFromDatabase);
                Double l1 = Double.parseDouble(latitudeFromDatabase);
                Double l2 = Double.parseDouble(longitudeFromDatabase);

                LatLng locationFromDatabase = new LatLng(l1, l2);
                locations.add(locationFromDatabase);

            }

            cursor.close();


        } catch (Exception e) {

        }


        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, names);
        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent,View view, int position, long id) {
                Intent intent=new Intent(getApplicationContext(),MapsActivity.class); //oluşturduğum intent beni Maps.Activityye götürecek
                intent.putExtra("info","old"); //kayıtlı yer=old
                intent.putExtra("position",position);
                startActivity(intent);


            }

        });
        //silme
        listView.setLongClickable(true);
        listView.setOnItemLongClickListener(new OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id) {

                names.remove(position);
                Toast.makeText(getApplicationContext(),"Place removed successfully", Toast.LENGTH_LONG).show();

                arrayAdapter.notifyDataSetChanged();
                return true;
            }});




    }

}
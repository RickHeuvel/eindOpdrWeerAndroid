package com.example.eindopdrweer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class ContactActivity extends AppCompatActivity {

    ListView contactsListView;
    ArrayList<String> StoreContacts ;
    ArrayAdapter<String> arrayAdapter ;
    Cursor cursor ;
    String name;
    String phonenumber ;
    public  static final int RequestPermissionCode  = 1 ;
    Button btLoadContacts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.layout_contactlist);

        contactsListView = (ListView)findViewById(R.id.contactsList);
        btLoadContacts = (Button)findViewById(R.id.loadContactsButton);
        StoreContacts = new ArrayList<String>();



        btLoadContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getApplicationContext().checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED){
                    GetContactsIntoArrayList();
                    arrayAdapter = new ArrayAdapter<String>(
                            ContactActivity.this,
                            R.layout.contact_items_listview,
                            R.id.contactItemText, StoreContacts
                    );
                    contactsListView.setAdapter(arrayAdapter);
                }else{
                    ActivityCompat.requestPermissions(ContactActivity.this,new String[]{
                            Manifest.permission.READ_CONTACTS}, RequestPermissionCode);
                }

            }
        });

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onRequestPermissionsResult(int RC, String per[], int[] PResult) {

        switch (RC) {
            case RequestPermissionCode:

                if (PResult.length > 0 && PResult[0] == PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(ContactActivity.this,"Permission Granted!", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(ContactActivity.this,"Permission Canceled, you need permission to show your contacts", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    public void GetContactsIntoArrayList(){

        cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null, null, null);

        while (cursor.moveToNext()) {

            name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));

            phonenumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

            StoreContacts.add(name + " "  + ":" + " " + phonenumber);
        }

        cursor.close();

    }

}

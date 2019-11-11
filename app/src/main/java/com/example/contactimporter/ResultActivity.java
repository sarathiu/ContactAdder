package com.example.contactimporter;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ListView;

import com.example.contactimporter.adapters.ContactListAdapter;
import com.example.contactimporter.models.Contact;

import java.util.ArrayList;

public class ResultActivity extends AppCompatActivity {

    ArrayList<Contact> mContactArrayList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        Bundle bundle = getIntent().getExtras();
        mContactArrayList = bundle.getParcelableArrayList("contacList");

        ContactListAdapter contactListAdapter = new ContactListAdapter(this,mContactArrayList);
        ListView contactListView = findViewById(R.id.contactListView);
        contactListView.setAdapter(contactListAdapter);
    }
}

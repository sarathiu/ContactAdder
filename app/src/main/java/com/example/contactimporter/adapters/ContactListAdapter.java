package com.example.contactimporter.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.contactimporter.R;
import com.example.contactimporter.models.Contact;

import java.util.ArrayList;

import androidx.annotation.ColorRes;

public class ContactListAdapter extends BaseAdapter {

    Context context;
    ArrayList<Contact> contactList;

    TextView nameTextView;
    TextView numberTextView;
    TextView infoTextView;
    public ContactListAdapter(Context context, ArrayList<Contact> list){
        this.context = context;
        this.contactList = list;

    }

    @Override
    public int getCount() {
        return contactList.size();
    }

    @Override
    public Object getItem(int position) {
        return contactList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView = LayoutInflater.from(context).
                    inflate(R.layout.contact_list_view_row_item,parent,false);
        }

        Contact currentContact = (Contact) getItem(position);

        nameTextView = convertView.findViewById(R.id.nameTextView);
        numberTextView = convertView.findViewById(R.id.numberTextView);
        infoTextView = convertView.findViewById(R.id.infoTextView);

        nameTextView.setText(currentContact.getName());
        numberTextView.setText(currentContact.getPhoneNumber());
        infoTextView.setText(getStatus(currentContact));
        return convertView;
    }

    private String getStatus(Contact currentContact){
       Contact.ImportStatus status = currentContact.getStatus();
       String statusString = null;
       switch (status){
           case Done:
                infoTextView.setTextColor(Color.GREEN);
               statusString= "Done";
               break;
           case Exist:
               infoTextView.setTextColor(Color.BLUE);
               statusString= "Already Exist";
               break;
           case Failed:
               infoTextView.setTextColor(Color.RED);
               statusString= "Failed";
               break;
           case InvalidName:
               infoTextView.setTextColor(Color.GRAY);
               statusString= "Invalid Name";
               break;
           case InvalidNumber:
               infoTextView.setTextColor(Color.LTGRAY);
               statusString= "Invalid Number";
               break;
       }
       return statusString;
    }
}

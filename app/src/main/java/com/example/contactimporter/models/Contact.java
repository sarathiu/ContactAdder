package com.example.contactimporter.models;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.ContactsContract;

import java.util.EnumMap;

public class Contact implements Parcelable {

    String name;
    String phoneNumber;
    ImportStatus status;


    public enum ImportStatus{
        Done,
        Failed,
        InvalidName,
        InvalidNumber,
        Exist
    }

    public Contact(String name, String phoneNumber){
        this.name = name;
        this.phoneNumber = phoneNumber;
    }

    public String getName(){
        return name;
    }

    public String getPhoneNumber(){
        return phoneNumber;
    }

    public ImportStatus getStatus(){
        return status;
    }

    public Boolean isValidPhoneNumber(){
        String regex = "^((\\+)?(\\d{2}))?(\\d{10}){1}?$";
        return phoneNumber.matches(regex);
    }

    public Boolean isValidName(){
        String regex ="[a-zA-Z0-9\\_]*";
        return name.matches(regex);
    }

    public void setStatus(ImportStatus status){
        this.status = status;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(phoneNumber);
        dest.writeString(status.name());
    }

    public Contact(Parcel in){
        name = in.readString();
        phoneNumber = in.readString();
        status = ImportStatus.valueOf(in.readString());
    }

    public static final Parcelable.Creator<Contact> CREATOR = new Parcelable.Creator<Contact>() {
        public Contact createFromParcel(Parcel in) {
            return new Contact(in);
        }

        public Contact[] newArray(int size) {
            return new Contact[size];
        }
    };

    public Boolean contactExist(Activity _activity,String number){

        if(number != null){
            Uri lookupUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,Uri.encode(number));
            String[] mPhoneNumberProjection = {ContactsContract.PhoneLookup._ID, ContactsContract.PhoneLookup.NUMBER, ContactsContract.PhoneLookup.DISPLAY_NAME};
            Cursor cursor = _activity.getContentResolver().query(lookupUri,mPhoneNumberProjection,null,null,null);
            try{
                if(cursor.moveToFirst()){
                    return true;
                }
            }finally {
                if(cursor != null)
                    cursor.close();
            }
            return false;
        }else
            return false;
    }
}



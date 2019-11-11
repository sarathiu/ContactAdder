package com.example.contactimporter.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MainActivityViewModel extends ViewModel {

    private MutableLiveData<Boolean> mIsValidFile;

    public void init(){

    }

    public LiveData<Boolean> getIsValidFile(){
        return mIsValidFile;
    }
}

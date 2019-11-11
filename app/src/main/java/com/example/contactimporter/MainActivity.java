package com.example.contactimporter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProviders;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.ContentProviderOperation;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.ContactsContract;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.contactimporter.models.Contact;
import com.example.contactimporter.viewmodels.MainActivityViewModel;
import com.google.android.material.snackbar.Snackbar;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity {

    private Button mSelectionButton;
    private View mMainLayout;
    private TextView mFilenameTextView;

    private MainActivityViewModel mMainActivityViewModel;
    private ParcelFileDescriptor mInputFD;
    private XSSFWorkbook mWorkbook;
    private ArrayList<Contact> mContactArrayList;
    private Boolean mIsFileSelected = false;


    private static final String TAG = "MainActivity";
    private static final int FILE_SELECTION_CODE = 100;
    private static final int CONTACT_REQUEST_READ_PERMISSION = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSelectionButton = findViewById(R.id.selectionButton);
        mFilenameTextView = findViewById(R.id.filenameTextView);
        mMainLayout = findViewById(R.id.main_layout);

        mSelectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mIsFileSelected){
                    addContacts();
                }else {
                    showFilePicker();
                }
            }
        });
    }

    private void showFilePicker(){

        String[] mimeTypes = {"application/x-excel","application/vnd.ms-excel"};
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try{
            startActivityForResult(Intent.createChooser(intent,"Select a file to add contacts"),
                    FILE_SELECTION_CODE);

        }catch (ActivityNotFoundException ex){
            Toast.makeText(this,ex.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void pickFile(Uri uri){

        try{
            mInputFD = getContentResolver().openFileDescriptor(uri,"r");
            String fileName = getFileName(uri);

            mFilenameTextView.setText(fileName);
            if(FileUtils.isExcelFile(fileName)){
                FileDescriptor fileDescriptor = mInputFD.getFileDescriptor();
                FileInputStream fileInputStream = new FileInputStream(fileDescriptor);
                mSelectionButton.setText(R.string.add_contacts);
                mSelectionButton.setBackgroundColor(getResources().getColor(R.color.contactAdd));
                mWorkbook = new XSSFWorkbook(fileInputStream);
                mIsFileSelected = true;
            }else {
                Toast.makeText(getApplicationContext(),R.string.invalid_excel, Toast.LENGTH_SHORT).show();
                mFilenameTextView.setText("");
                mSelectionButton.setText(R.string.pick_a_file);
                mIsFileSelected = false;
            }
        }catch (FileNotFoundException ex){
            Log.d(TAG,"File not found : "+ex);
        }
        catch (Exception ex){
            Log.d(TAG,"Exception : "+ex);
            Toast.makeText(this, TAG + "Exception : "+ex.getMessage(),Toast.LENGTH_LONG).show();
        }
    }

    private void addContacts(){

        try{
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                if (mWorkbook != null) {
                    XSSFSheet workSheet = mWorkbook.getSheetAt(0);
                    Iterator<Row> iterator = workSheet.iterator();
                    mContactArrayList = new ArrayList<>();
                    while (iterator.hasNext()) {
                        Row currentRow = iterator.next();
                        Cell nameCell = currentRow.getCell(0);
                        Cell numberCell = currentRow.getCell(1);

                        Contact newContact = null;
                        if (numberCell != null) {
                            switch (numberCell.getCellType()) {
                                case Cell.CELL_TYPE_NUMERIC:
                                    newContact = new Contact(nameCell.getStringCellValue(), Double.toString(numberCell.getNumericCellValue()));
                                    Log.d(TAG, "Name : " + nameCell.getStringCellValue() + " Number: " + numberCell.getNumericCellValue());
                                    break;
                                case Cell.CELL_TYPE_STRING:
                                    newContact = new Contact(nameCell.getStringCellValue(), numberCell.getStringCellValue());
                                    Log.d(TAG, "Name : " + nameCell.getStringCellValue() + " Number: " + numberCell.getStringCellValue());
                                    break;
                                default:
                                    Log.d(TAG, "Blank cell");
                                    break;
                            }
                            mContactArrayList.add(newContact);
                        }
                    }
                    if (!mContactArrayList.isEmpty()) {

                        for (Contact item : mContactArrayList) {
                            if (!item.isValidName()) {
                                item.setStatus(Contact.ImportStatus.InvalidName);
                            }
                            else if (!item.isValidPhoneNumber()) {
                                item.setStatus(Contact.ImportStatus.InvalidNumber);
                            }
                            else if (item.contactExist(this, item.getPhoneNumber())) {
                                item.setStatus(Contact.ImportStatus.Exist);
                            }else {
                                ArrayList < ContentProviderOperation > ops = new ArrayList < ContentProviderOperation > ();

                                ops.add(ContentProviderOperation.newInsert(
                                        ContactsContract.RawContacts.CONTENT_URI)
                                        .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                                        .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                                        .build());

                                //------------------------------------------------------ Names
                                if (item.getName() != null) {
                                    ops.add(ContentProviderOperation.newInsert(
                                            ContactsContract.Data.CONTENT_URI)
                                            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                                            .withValue(ContactsContract.Data.MIMETYPE,
                                                    ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                                            .withValue(
                                                    ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
                                                    item.getName()).build());
                                }

                                //------------------------------------------------------ Mobile Number
                                if (item.getPhoneNumber() != null) {
                                    ops.add(ContentProviderOperation.
                                            newInsert(ContactsContract.Data.CONTENT_URI)
                                            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                                            .withValue(ContactsContract.Data.MIMETYPE,
                                                    ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                                            .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, item.getPhoneNumber())
                                            .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
                                                    ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                                            .build());
                                }
                                try {
                                    getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
                                    item.setStatus(Contact.ImportStatus.Done);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    item.setStatus(Contact.ImportStatus.Failed);
                                    Toast.makeText(this, "Exception: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }

                        Intent contactIntent = new Intent(this, ResultActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putParcelableArrayList("contacList", mContactArrayList);
                        contactIntent.putExtras(bundle);
                        this.startActivity(contactIntent);
                    }
                }

                Toast.makeText(getApplicationContext(), R.string.operation_successful, Toast.LENGTH_SHORT).show();
                mIsFileSelected = false;
                mSelectionButton.setText(R.string.pick_a_file);
                mSelectionButton.setBackgroundColor(getResources().getColor(R.color.defaultcolor));
                mFilenameTextView.setText("");
            }else {
                requestContactPermission();
            }

        }catch (Exception ex){
            Log.d(TAG,ex.getMessage());
        }
    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    public  void requestContactPermission(){

        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_CONTACTS)) {
            Snackbar.make(mMainLayout,R.string.contact_access_required,Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.READ_CONTACTS,Manifest.permission.WRITE_CONTACTS},
                                    CONTACT_REQUEST_READ_PERMISSION);
                        }
                    }).show();
        } else {
            // No explanation needed; request the permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS,Manifest.permission.WRITE_CONTACTS},
                    CONTACT_REQUEST_READ_PERMISSION);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode){
            case FILE_SELECTION_CODE:
                pickFile(data.getData());
                break;
            case  CONTACT_REQUEST_READ_PERMISSION:
                addContacts();
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}

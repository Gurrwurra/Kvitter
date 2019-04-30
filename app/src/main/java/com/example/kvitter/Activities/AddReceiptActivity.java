package com.example.kvitter.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kvitter.R;
import com.example.kvitter.Util.ImageHelper;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class AddReceiptActivity extends AppCompatActivity {


    private ImageButton recieptPic;
    private Button imageUpload;
    private Button save;
    private Button PDFUpload;
    private EditText title;
    private EditText amount;
    private EditText supplier;
    private EditText comment;
    private TextView file;

    private static final int PICK_IMAGE = 100;

    private static final int PICK_PDF = 1000;

    private static final int PERMISSION_REQUEST_CODE = 1;


    private String currentPhoto;

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final String CURRENT_PHOTO = "currentPhoto";

    int validate = 0;
    Uri photoURI = null;
    File photoFile = null;

    Uri fileUri = null;

    String fileName = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_receipt);


        if (Build.VERSION.SDK_INT >= 23)
        {
            if (checkPermission())
            {
                bindViews();
                setListiners();
            } else {
                requestPermission(); // Code for permission
            }
        }
    }

    private void bindViews(){
        recieptPic = findViewById(R.id.receiptImage);
        imageUpload = findViewById(R.id.btn_upload_image);
        PDFUpload = findViewById(R.id.btn_pdf_upload);
        save = findViewById(R.id.btn_save);
        title = findViewById(R.id.etxt_name);
        amount = findViewById(R.id.etxt_total_amount);
        supplier = findViewById(R.id.etxt_supplier);
        comment = findViewById(R.id.etxt_note_reciept);
        file = findViewById(R.id.txt_file_path);
    }

    private void setListiners(){
        recieptPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               takePhoto();
            }
        });

        imageUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        PDFUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDownloads();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddReceiptActivity.this, Validate_reciept.class);
                intent.putExtra("name", title.getText().toString());
                intent.putExtra("amount", amount.getText().toString());
                intent.putExtra("supplier", supplier.getText().toString());
                intent.putExtra("comment", comment.getText().toString());
                intent.putExtra("photoPath", currentPhoto);
                intent.putExtra("validate", validate);

                if(photoURI != null) {
                    intent.putExtra("uri", photoURI.toString());
                    intent.putExtra("fileOfPhoto", photoFile.toString());
                }else if(fileUri != null) {
                    intent.putExtra("fileUri", fileUri.toString());
                    intent.putExtra("fileName", fileName);
                }
                startActivity(intent);
            }
        });
    }

    private void takePhoto() {
        dispatchTakePictureIntent();
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
             photoFile = null;
            try {
                photoFile = ImageHelper.createImageFile(getApplicationContext());
            } catch (IOException ex) {
            }
            if (photoFile != null) {
                currentPhoto = photoFile.getAbsolutePath();
                photoURI = FileProvider.getUriForFile(this,
                        "com.example.KvitterApp",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

                if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
                    try {
                        setPic();
                        validate = 0;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (requestCode == PICK_IMAGE && resultCode == RESULT_OK ) {
                    photoURI = data.getData();
                    photoFile = new File(ImageHelper.getRealPathFromURI(getApplicationContext(), photoURI));
                    try {
                        setPic();
                        validate = 0;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }else if(requestCode == PICK_PDF && resultCode == RESULT_OK){
                    fileUri = data.getData();

                    fileName = getFileName(fileUri);
                    validate = 1;
                }
    }

    private void setPic() throws IOException {
        Uri uri = Uri.fromFile(photoFile);
        Bitmap imageBitmap = ImageHelper.getCorrectlyOrientedImage(getApplicationContext(), uri);
        recieptPic.setImageBitmap(imageBitmap);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString(CURRENT_PHOTO, currentPhoto);
        super.onSaveInstanceState(savedInstanceState);
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        currentPhoto = savedInstanceState.getString(CURRENT_PHOTO);
        recieptPic.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                recieptPic.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                try {
                    setPic();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void openGallery(){
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, PICK_IMAGE);
    }

    private void openDownloads(){
        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_PDF);
    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(AddReceiptActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    private void requestPermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(AddReceiptActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(AddReceiptActivity.this, "Write External Storage permission allows us to do store images. Please allow this permission in App Settings.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(AddReceiptActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("value", "Permission Granted, Now you can use local drive .");
                } else {
                    Log.e("value", "Permission Denied, You cannot use local drive .");
                }
                break;
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

}

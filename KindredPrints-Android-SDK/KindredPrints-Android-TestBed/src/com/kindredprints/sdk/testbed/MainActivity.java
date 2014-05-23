package com.kindredprints.sdk.testbed;

import java.util.ArrayList;

import com.kindredprints.sdk.testbed.R;
import com.kindredprints.android.sdk.KCustomPhoto;
import com.kindredprints.android.sdk.KLOCPhoto;
import com.kindredprints.android.sdk.KPhoto;
import com.kindredprints.android.sdk.KindredOrderFlowActivity;
import com.kindredprints.android.sdk.KURLPhoto;
import com.kindredprints.android.sdk.KindredOrderFlow;

import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {
	private static int RESULT_GALLERY_LOAD_IMAGE = 1;
	private static int RESULT_IMAGE_CAPTURE = 2;
	
	private final static String KINDRED_APP_KEY = "YOUR TEST KEY";
	
	EditText editTxtUrl;
	Button cmdAddUrl;
	Button cmdAddThree;
	Button cmdAddMany;
	Button cmdAddCustom;
	
	Activity activity_;
	
	Button cmdTakePhoto;
	Button cmdPickFromGallery;
	
	EditText editTxtEmail;
	Button cmdRegister;
	
	Button cmdShowCart;
		
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.activity_ = this;
        		
		this.cmdAddCustom = (Button) findViewById(R.id.cmdAddSpecial);
		this.cmdAddCustom.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
		        KindredOrderFlow orderFlow  = new KindredOrderFlow(activity_, KINDRED_APP_KEY);
		        orderFlow.setImageBorderDisabled(false);
				orderFlow.addImageToCart(new KCustomPhoto("0", "allthecooks", "http://www.allthecooks.com/amies-achara.html"));
				showToast("image added");
				Intent i = new Intent(getApplicationContext(), KindredOrderFlowActivity.class);
				startActivityForResult(i, 0);
			}
		});


        this.cmdShowCart = (Button) findViewById(R.id.cmdShowCart);
        this.cmdShowCart.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent i = new Intent(getApplicationContext(), KindredOrderFlowActivity.class);
				startActivityForResult(i, 0);
			}
        });
        
        this.editTxtUrl = (EditText) findViewById(R.id.editTxtUrl);
        this.cmdAddUrl = (Button) findViewById(R.id.cmdAddUrl);
        this.cmdAddUrl.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (editTxtUrl.getText().toString().length() > 0) {
					KindredOrderFlow orderFlow  = new KindredOrderFlow(activity_, KINDRED_APP_KEY);
				    orderFlow.setImageBorderDisabled(false);
					orderFlow.addImageToCart(new KURLPhoto(editTxtUrl.getText().toString(), editTxtUrl.getText().toString()));
					showToast("image added");
					Intent i = new Intent(getApplicationContext(), KindredOrderFlowActivity.class);
					startActivityForResult(i, 0);
				}
			}
        });
        this.cmdAddThree = (Button) findViewById(R.id.cmdAddThree);
        this.cmdAddThree.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				KindredOrderFlow orderFlow  = new KindredOrderFlow(activity_, KINDRED_APP_KEY);
			    orderFlow.setImageBorderDisabled(false);
				orderFlow.addImageToCart(new KURLPhoto("http://dev.kindredprints.com/img/horizRect.jpg", "http://dev.kindredprints.com/img/horizRect.jpg"));
				orderFlow.addImageToCart(new KURLPhoto("http://dev.kindredprints.com/img/squareTest.jpg", "http://dev.kindredprints.com/img/squareTest.jpg"));
				orderFlow.addImageToCart(new KURLPhoto("http://kindredprints.com/img/alex.png", "http://kindredprints.com/img/alex.png"));
				Intent i = new Intent(getApplicationContext(), KindredOrderFlowActivity.class);
				startActivityForResult(i, 0);
				showToast("images added");
			}
        });
        
        this.cmdAddMany = (Button) findViewById(R.id.cmdAddMany);
        this.cmdAddMany.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				KindredOrderFlow orderFlow  = new KindredOrderFlow(activity_, KINDRED_APP_KEY);
			    orderFlow.setImageBorderDisabled(false);
				ArrayList<KPhoto> photosToAdd = new ArrayList<KPhoto>();
				for (int i = 0; i < 11; i++) {
					photosToAdd.add(new KURLPhoto("https://s3-us-west-1.amazonaws.com/kindredmetaimages/electronics.jpg", "https://s3-us-west-1.amazonaws.com/kindredmetaimages/electronics.jpg"));
				}
				orderFlow.addImagesToCart(photosToAdd);
				Intent i = new Intent(getApplicationContext(), KindredOrderFlowActivity.class);
				startActivityForResult(i, 0);
			}
        });
        
        this.cmdTakePhoto = (Button) findViewById(R.id.cmdTakePhoto);
        this.cmdTakePhoto.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
			        startActivityForResult(takePictureIntent, RESULT_IMAGE_CAPTURE);
			    }
			}
        });
        this.cmdPickFromGallery = (Button) findViewById(R.id.cmdFromGallery);
        this.cmdPickFromGallery.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                 
                startActivityForResult(i, RESULT_GALLERY_LOAD_IMAGE);
			}        	
        });
        
        this.editTxtEmail = (EditText) findViewById(R.id.editTxtEmail);
        this.cmdRegister = (Button) findViewById(R.id.cmdRegister);
        this.cmdRegister.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (editTxtEmail.getText().toString().length() > 0) {
					KindredOrderFlow orderFlow  = new KindredOrderFlow(activity_, KINDRED_APP_KEY);
					orderFlow.setImageBorderDisabled(false);
					orderFlow.preRegisterEmail(editTxtEmail.getText().toString());
				}
			}
        });
    }

    private void showToast(String message) {
    	Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
    	toast.show();
    }
    
 
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (resultCode == KindredOrderFlowActivity.KP_RESULT_CANCELLED) {
    		Log.i("TestActivity", "User cancelled Kindred purchase");
    	} else if (resultCode == KindredOrderFlowActivity.KP_RESULT_PURCHASED) {
    		Log.i("TestActivity", "User completed Kindred purchase!");
    	} else if (requestCode == RESULT_GALLERY_LOAD_IMAGE & resultCode == RESULT_OK && data != null) {
    		Log.i("TestActivity", "Grabbed a gallery image");
    		Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);

            cursor.close();

            KindredOrderFlow orderFlow  = new KindredOrderFlow(activity_, KINDRED_APP_KEY);
		    orderFlow.setImageBorderDisabled(false);
            if (picturePath.contains("http"))
            	orderFlow.addImageToCart(new KURLPhoto(picturePath));
            else
            	orderFlow.addImageToCart(new KLOCPhoto(picturePath));
			showToast("image added");
			Intent i = new Intent(getApplicationContext(), KindredOrderFlowActivity.class);
			startActivityForResult(i, 0);
    	} else if (requestCode == RESULT_IMAGE_CAPTURE && resultCode == RESULT_OK) {
    		Uri selectedImage = data.getData();
    		
    		Log.i("TestActivity", "grabbed url " + selectedImage.getPath());

            String[] filePathColumn = { MediaStore.Images.Media.DATA };
 
            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            KindredOrderFlow orderFlow  = new KindredOrderFlow(activity_, KINDRED_APP_KEY);
		    orderFlow.setImageBorderDisabled(false);
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
			orderFlow.addImageToCart(new KLOCPhoto(picturePath));
			showToast("image added");
			Intent i = new Intent(getApplicationContext(), KindredOrderFlowActivity.class);
			startActivityForResult(i, 0);
    	}
    }
}

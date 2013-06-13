package com.cloudSync;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;

public class FileUploadActivity extends ParentActivity {
	
   	//Variable declaration    
	private Button selectFileButton = null;
	private Button uploadFileButton = null;
	private TextView myFileNamePath = null;
	private CheckBox uploadToPublicFolder = null;
	
	private static final int PHOTO_SELECTED = 1;
	
	public static String ACCESS_KEY_ID = null;
	public static String SECRET_KEY = null;
	public static String USERNAME = null;
	
	private String mFilePath = null;
	
	private AmazonS3Client s3Client =null;
	
	private ProgressDialog m_UploadProgressDialog = null;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_upload);
        setTitleFromActivityLabel (R.id.title_text);
        
        Bundle extras = this.getIntent().getExtras();
    	ACCESS_KEY_ID = extras.getString("access_key");
    	SECRET_KEY = extras.getString("secret_key");
    	USERNAME = extras.getString("username");
        
        
        selectFileButton = (Button) findViewById(R.id.select_file_btn);
        selectFileButton.setOnClickListener(new View.OnClickListener() {
        	
			public void onClick(View v) {
				// Start the image picker.
				Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
				intent.setType("*/*");//allow one to select all file types
				startActivityForResult(intent, PHOTO_SELECTED);
			}
		});
        
        s3Client =   new AmazonS3Client( new BasicAWSCredentials( ACCESS_KEY_ID, SECRET_KEY ) );
        
        uploadFileButton = (Button) findViewById(R.id.upload_file_btn);
        uploadFileButton.setOnClickListener(new View.OnClickListener() {
			//@Override
			public void onClick(View v) 
			{
				if(mFilePath == null || myFileNamePath == null)
				{
					displayAlert("No File selected", "Please select a file to upload");
				}
				else if(isOnline())
				{
					FileUploadAsyncTask uploadTask = new FileUploadAsyncTask();
					uploadTask.execute("test");
				}
				else
				{
					displayAlert("Network Error!", "Your device does not have internet connection.");
				}
               
			}
		});//end of anonymous inner class
        
        
       myFileNamePath = (TextView)findViewById(R.id.hidden_file_upload_2_edittext);
       uploadToPublicFolder = (CheckBox)findViewById(R.id.chk_upload_to_public_folder);
    }//end of onCreate()
    
    
    public void uploadFile() throws Exception
    {
    	//use transfer manager because its thread safe                	 
        if(uploadToPublicFolder.isChecked())
        {
        	//upload files to public folder
        	try 
        	{
        		TransferManager tx = new TransferManager(s3Client);
        		File uploadFile = new java.io.File( mFilePath);
        		String  uploadPath = "manuelmhtr/Users/"+USERNAME+"/"+encodeURLString("My Public Folder");
        		Upload myUpload = tx.upload(uploadPath, uploadFile.getName(), uploadFile );
        		
        	}
            catch ( Exception exception ) 
            {
            	throw new RuntimeException("Error Upoading File!",exception);
            }
        }
        else
        {
        	//upload to normal or private folder
        	try
        	{
        		TransferManager tx = new TransferManager(s3Client);
        		File uploadFile = new java.io.File( mFilePath);
        		Upload myUpload = tx.upload("manuelmhtr/Users/"+USERNAME, uploadFile.getName(), uploadFile);
        		
        	}
        	catch ( Exception exception )
        	{
        		throw new RuntimeException("Error Upoading File!",exception);
        	}
        }//end of if
    }
    
    
    // This method is automatically called by the image picker when an image is selected. 
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) { 
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent); 

        switch(requestCode) { 
        case PHOTO_SELECTED:
            if (resultCode == RESULT_OK) {  
            	// THe file location of the image selected.
                Uri selectedImage = imageReturnedIntent.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};

                Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String filePath = cursor.getString(columnIndex);
                cursor.close();
                mFilePath = filePath;
                
                myFileNamePath.setText("Selected file: "+ mFilePath);
            }
        }
    }
    
    /*
     * Displays an Alert message for an error or failure.
     */
    protected void displayAlert( String title, String message ) {
        AlertDialog.Builder confirm = new AlertDialog.Builder( this );
        confirm.setTitle( title);
        confirm.setMessage( message );
        confirm.setNegativeButton( "OK", new DialogInterface.OnClickListener() {
            public void onClick( DialogInterface dialog, int which ) {
            }
        } );
        confirm.show().show();                
    }//end of displayAlert()
    
    /*
     * Encodes a String Object to be passed to a URL
     */
    public String encodeURLString(String input)
    {
    	String encodedString = null;
    	try {
    		encodedString = URLEncoder.encode(input, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			displayAlert("URL encodeing Error", e.getMessage());
		}
    	
    	return encodedString;
    }// end of encodeURLString()
    
    /**
     * Handle the click on the home button.
     * 
     * @param v View
     * @return void
     */

    public void onClickHome (View v)
    {
        goHome (this);
    }

    /**
     * Go back to the home activity.
     * 
     * @param context Context
     * @return void
     */

    public void goHome(Context context) 
    {
        final Intent intent = new Intent(context, HomeScreenActivity.class);
        intent.putExtra("access_key", ACCESS_KEY_ID);
        intent.putExtra("secret_key",SECRET_KEY);
        intent.putExtra("username",USERNAME);
        
        intent.setFlags (Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity (intent);
    }
    
    private class FileUploadAsyncTask extends AsyncTask<Object, Boolean, String> 
	{

		String result = null;
		boolean isUploadFailed = false;
		
		@Override
		protected String doInBackground(Object... params)
		{
		String key = (String) params[0];
		try {
			uploadFile();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			result = e.getMessage();
			isUploadFailed = true;		}
		return null;
		}

		

		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			m_UploadProgressDialog.dismiss();
			
				if(isUploadFailed)
				{
					displayAlert("File Upload Failed", result);
				}
				else
				{
					displayAlert("File Upload", "File Upload was successful.");
				}
			super.onPostExecute(result);
		}



		@Override
		protected void onPreExecute() {
			m_UploadProgressDialog = ProgressDialog.show(FileUploadActivity.this,    
  	              "Please wait...", "Uploading file ...", true);
		super.onPreExecute();
		}



	}//end of inner class
    
    /*
     * Checks whether the device is connected to internet
     */
    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo wifiNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiNetwork != null && wifiNetwork.isConnected()) {
          return true;
        }

        NetworkInfo mobileNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (mobileNetwork != null && mobileNetwork.isConnected()) {
          return true;
        }

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected()) {
          return true;
        }

        return false;
    }//end of isOnline();
    
}//end of class


package com.cloudSync;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class HomeScreenActivity extends ParentActivity 
{
	
	//Variable declaration
	Button mFileDownloadButton  = null;
	Button mFileUploadButton = null;
	public static  String ACCESS_KEY_ID = null;
	public static  String SECRET_KEY = null;
	public static  String USERNAME = null;
	
/**
 * onCreate
 *
 * Called when the activity is first created. 
 * This is where you should do all of your normal static set up: create views, bind data to lists, etc. 
 * This method also provides you with a Bundle containing the activity's previously frozen state, if there was one.
 * 
 * Always followed by onStart().
 *
 * @param savedInstanceState Bundle
 */

protected void onCreate(Bundle savedInstanceState) 
{
    super.onCreate(savedInstanceState);
    setContentView (R.layout.activity_home_screen);
    setTitleFromActivityLabel (R.id.title_text);
    
    Bundle extras = this.getIntent().getExtras();
	ACCESS_KEY_ID = extras.getString("access_key");
	SECRET_KEY = extras.getString("secret_key");
	USERNAME = extras.getString("username");
    
    //find views by id
    mFileDownloadButton = (Button)findViewById(R.id.file_download_btn);
    mFileUploadButton = (Button)findViewById(R.id.file_upload_btn);
    
    //Add onClick() Lister to mFileDownloadButton
    mFileDownloadButton.setOnClickListener(new OnClickListener() {
		
		public void onClick(View v) {
			// TODO Launch DownloadActivity
            Intent startDownloadScreenIntent = new Intent(getApplicationContext(), FileDownloadActivity1.class);
            startDownloadScreenIntent.putExtra("access_key", ACCESS_KEY_ID);
            startDownloadScreenIntent.putExtra("secret_key",SECRET_KEY );
            startDownloadScreenIntent.putExtra("username",USERNAME);
            
            startActivity(startDownloadScreenIntent);
		}
	});//end of anonymous inner class 
    
    
    //Add onClick() Listener to mFileDownloadButton
    mFileUploadButton.setOnClickListener(new OnClickListener() {
		
		public void onClick(View v) {
			// TODO Navigate user to file upload activity
			Intent startUploadScreenIntent = new Intent(getApplicationContext(), FileUploadActivity.class);
	        startUploadScreenIntent.putExtra("access_key", ACCESS_KEY_ID);
	        startUploadScreenIntent.putExtra("secret_key",SECRET_KEY );
	        startUploadScreenIntent.putExtra("username",USERNAME);
	            
	        startActivity(startUploadScreenIntent);
			
		}
	});//end of anonymous inner class
}//end of onCreate()
    
}//end class

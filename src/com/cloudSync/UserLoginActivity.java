package com.cloudSync;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class UserLoginActivity extends ParentActivity 
{
	
	//Variable declaration
	Button mLoginButton = null;
	Button mCancelButton = null;
	EditText inputUsername;
    EditText inputPassword;
    TextView loginErrorMsg = null;
	
	//Testing in localhost using wamp 
    //use http://10.0.2.2/ to connect to your localhost ie http://localhost/
    private static String loginURL = "http://cloudsync.com/androidLoginServer.php";//HERE...
    private static String login_tag = "login";
    private JSONParser jsonParser = new JSONParser();
    
    // JSON Response node names
    private static String TAG_SUCCESS = "success";
    private static String TAG_MY_ACCESS_KEY = "access_key";
    private static String TAG_MY_SECRET_KEY = "secret_key";


/**
 * onCreate - called when the activity is first created.
 * Called when the activity is first created. 
 * This is where you should do all of your normal static set up: create views, bind data to lists, etc. 
 * This method also provides you with a Bundle containing the activity's previously frozen state, if there was one.
 * 
 * Always followed by onStart().
 *
 */

protected void onCreate(Bundle savedInstanceState) 
{
    super.onCreate(savedInstanceState);
    setContentView(R.layout.login_activity);
    setTitleFromActivityLabel (R.id.title_text);
    
    //find views  by id
    mLoginButton = (Button)findViewById(R.id.login_btn);
    mCancelButton = (Button)findViewById(R.id.cancel_btn);
    inputUsername = (EditText) findViewById(R.id.username_edittext);
    inputPassword = (EditText) findViewById(R.id.password_edittext);
    loginErrorMsg = (TextView)findViewById(R.id.hidden_login_response);
    
    //add login logic
    mLoginButton.setOnClickListener(new OnClickListener() {
		
		public void onClick(View v) {
			// TODO On successful login
			 String email = inputUsername.getText().toString();
             String password = inputPassword.getText().toString();
            
             

             
             try {
            	 	//check if device is connected to internet.
                 	if(isOnline() == false)
                 	{
                	 	//No internet connection.Alert user
                	 	displayAlert("Network Error!", "Your device does not have internet connection.");
                	 	return;
                 	}
                 
            	 JSONObject json = loginUser(email, password);
            	// check for login response
                 if (json.getString(TAG_SUCCESS) != null) {
                     loginErrorMsg.setText("");
                     String res = json.getString(TAG_SUCCESS);
                     if(Integer.parseInt(res) == 1){
                         // user successfully logged in
                         String access_key = json.getString(TAG_MY_ACCESS_KEY);
                         String secrete_key = json.getString(TAG_MY_SECRET_KEY);
                         
                         String username = inputUsername.getText().toString();
                         
                         //Launch HomeScreenActivity
                         Intent startHomeScreenIntent = new Intent(getApplicationContext(), HomeScreenActivity.class);
                         startHomeScreenIntent.putExtra("access_key", access_key);
                         startHomeScreenIntent.putExtra("secret_key",secrete_key );
                         startHomeScreenIntent.putExtra("username",username);
                         
                         startActivity(startHomeScreenIntent);

                         // Close Login Screen
                         finish();
                     }else{
                         // Error in login
                    	 loginErrorMsg.setText(json.getString("error_msg"));
                     }
                 }
             } catch (Exception e) {
            	 //network error
            	 displayAlert("Network Error!", "Remote server not Available. Please check your network connection.");
             }
			
		}
	});//end of anonymous inner class
    
    //add listener to cancel button
    mCancelButton.setOnClickListener(new OnClickListener() {
		
		public void onClick(View v) {
			// TODO Reset password and Username Fields
			inputPassword.setText(null);
			inputUsername.setText(null);			
		}
	});//end of anonymous inner class
}
    
/**
 * onDestroy
 * The final call you receive before your activity is destroyed. 
 * This can happen either because the activity is finishing (someone called finish() on it, 
 * or because the system is temporarily destroying this instance of the activity to save space. 
 * You can distinguish between these two scenarios with the isFinishing() method.
 *
 */

protected void onDestroy ()
{
   super.onDestroy ();
}

/**
 * onPause
 * Called when the system is about to start resuming a previous activity. 
 * This is typically used to commit unsaved changes to persistent data, stop animations 
 * and other things that may be consuming CPU, etc. 
 * Implementations of this method must be very quick because the next activity will not be resumed 
 * until this method returns.
 * Followed by either onResume() if the activity returns back to the front, 
 * or onStop() if it becomes invisible to the user.
 *
 */

protected void onPause ()
{
   super.onPause ();
}

/**
 * onRestart
 * Called after your activity has been stopped, prior to it being started again.
 * Always followed by onStart().
 *
 */

protected void onRestart ()
{
   super.onRestart ();
}

/**
 * onResume
 * Called when the activity will start interacting with the user. 
 * At this point your activity is at the top of the activity stack, with user input going to it.
 * Always followed by onPause().
 *
 */

protected void onResume ()
{
   super.onResume ();
}

/**
 * onStart
 * Called when the activity is becoming visible to the user.
 * Followed by onResume() if the activity comes to the foreground, or onStop() if it becomes hidden.
 *
 */

protected void onStart ()
{
   super.onStart ();
}

/**
 * onStop
 * Called when the activity is no longer visible to the user
 * because another activity has been resumed and is covering this one. 
 * This may happen either because a new activity is being started, an existing one 
 * is being brought in front of this one, or this one is being destroyed.
 *
 * Followed by either onRestart() if this activity is coming back to interact with the user, 
 * or onDestroy() if this activity is going away.
 */

protected void onStop ()
{
   super.onStop ();
}

/**
 * function make Login Request
 * @param email
 * @param password
 * @throws Exception 
 * */
public JSONObject loginUser(String email, String password) throws Exception{
    // Building Parameters
    List<NameValuePair> params = new ArrayList<NameValuePair>();
    params.add(new BasicNameValuePair("tag", login_tag));
    params.add(new BasicNameValuePair("email", email));
    params.add(new BasicNameValuePair("password", password));
    JSONObject json = jsonParser.getJSONFromUrl(loginURL, params);
    
    return json;
}


protected void displayAlert( String title, String message ) {
    AlertDialog.Builder confirm = new AlertDialog.Builder( this );
    confirm.setTitle( title);
    confirm.setMessage( message );
    confirm.setNegativeButton( "OK", new DialogInterface.OnClickListener() {
        public void onClick( DialogInterface dialog, int which ) {
        }
    } );
    confirm.show().show();                
}

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

}//end class

package com.cloudSync;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;

public class FileDownloadActivity1  extends ListActivity
{
	//variable declaration
	private ProgressDialog m_ProgressDialog = null;
	private ProgressDialog m_DownloadProgressDialog = null;
    private ArrayList<ListItem> m_orders = null;
    private ListItemAdapter m_adapter;
    private Runnable viewOrders;
    
    public static final String ACCESS_KEY_ID = HomeScreenActivity.ACCESS_KEY_ID;
	public static final String SECRET_KEY = HomeScreenActivity.SECRET_KEY;
	private AmazonS3Client s3Client = new AmazonS3Client( new BasicAWSCredentials( ACCESS_KEY_ID, SECRET_KEY ) );
	
	protected static final int CONTEXTMENU_DOWNLOAD_FILE = 0;
	String mUsername = HomeScreenActivity.USERNAME;
	String myBucketName = "manuelmhtr";
	String myFilterKey = "Users/"+mUsername;
	String rootFolder = "Users/"+mUsername;
	
	EditText mAddressBar = null;
	Button goButton = null;
	Button refreshButton = null;
	Button backButton = null;

	private static String currentS3ObjectKey = null;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_download);
		setTitleFromActivityLabel (R.id.title_text);
		
		Bundle extras = this.getIntent().getExtras();
		String newFilterKey = extras.getString("filter_key");
		
			if(newFilterKey != null)
			{
				myFilterKey = newFilterKey;
			}
		
		//find views by id.If construct to allow subdir navigation
		refreshButton = (Button)findViewById(R.id.address_refresh_btn);
		backButton = (Button)findViewById(R.id.address_back_btn);
		
		mAddressBar = (EditText)findViewById(R.id.file_download_address_bar);
		
			if(endsWithFolderSeparator(myFilterKey))
			{
				mAddressBar.setText("/" + updateAddressBarWithPath(myFilterKey,rootFolder));
			}
			else
			{
				mAddressBar.setText("/" + updateAddressBarWithPath(myFilterKey,rootFolder));
			}
		
		goButton = (Button)findViewById(R.id.address_go_btn);
		
		m_orders = new ArrayList<ListItem>();
        this.m_adapter = new ListItemAdapter(this, R.layout.row, m_orders);
        setListAdapter(this.m_adapter);
       
        viewOrders = new Runnable(){
            public void run() {
            	Looper.prepare();
                getOrders();
                Looper.loop();
            }
        };
    Thread thread =  new Thread(null, viewOrders, "MagentoBackground");
        thread.start();
        m_ProgressDialog = ProgressDialog.show(FileDownloadActivity1.this,    
              "Please wait...", "Retrieving data ...", true);
        
        getListView().setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                int position, long id) {
        
            	ListItem item = m_orders.get(position);
            	
            	if(item.getItemType().equals("file"))
            	{
            		//download file
            		currentS3ObjectKey = item.getKey();
            		
            		FileDownloadAsyncTask fdat = new FileDownloadAsyncTask();
            		fdat.execute(item.getKey());

            	}
            	else if(item.getItemType().equals("folder"))
            	{
            		//start new file download activity with key item.getItemKey()
            		Intent startDownloadScreenIntent = new Intent(getApplicationContext(), FileDownloadActivity1.class);
                    startDownloadScreenIntent.putExtra("filter_key", item.getKey());
                     
                    startActivity(startDownloadScreenIntent);
            	}
            }
          });//end of method
        
        //wire in the go button
        goButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				String passToNewActivity = null;
					if(mAddressBar.getText().toString().equals(""))
					{
						passToNewActivity = rootFolder;
					}
					else
					{
						passToNewActivity=rootFolder + mAddressBar.getText().toString();
					}
				// TODO Start new File download activty with key entered in the address ba
        		Intent startDownloadScreenIntent = new Intent(getApplicationContext(), FileDownloadActivity1.class);
                startDownloadScreenIntent.putExtra("filter_key", passToNewActivity);
                 
                startActivity(startDownloadScreenIntent);
			}
		});
        
      //refreshes the current Activity
      refreshButton.setOnClickListener(new OnClickListener() {
      			
      		public void onClick(View v) {
      			// TODO Auto-generated method stub
      			startActivity(getIntent()); 
      			finish();

      		}
      });
      		
      //Back button takes you back 
      backButton.setOnClickListener(new OnClickListener() {
      			
    	  public void onClick(View v) {
      	       // TODO Auto-generated method stub
      				onBackPressed();
      		}
      });
	}//end of onCreate()
    
    
    //inner class
	private class ListItemAdapter extends ArrayAdapter<ListItem> {

        private ArrayList<ListItem> items;

        public ListItemAdapter(Context context, int textViewResourceId, ArrayList<ListItem> items) {
                super(context, textViewResourceId, items);
                this.items = items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
                View v = convertView;
                if (v == null) {
                    LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    v = vi.inflate(R.layout.row, null);
                }
                ListItem o = items.get(position);
                if (o != null) {
                        TextView tt = (TextView) v.findViewById(R.id.item_title);
                        TextView bt = (TextView) v.findViewById(R.id.item_type);
                        ImageView icon=(ImageView)v.findViewById(R.id.list_item_icon);
                        
                        if (tt != null) {
                              tt.setText(o.getItemTitle());                            }
                        
                        //set icons
                        if (o.getItemType().equals("file")) {
                        	icon.setImageResource(R.drawable.file_icon);
                        }
                        else if (o.getItemType().equals("folder")) {
                        	icon.setImageResource(R.drawable.folder_icon);
                        }
                }
                return v;
        }//end of getView()
	}//end of inner class
	
	private void getOrders(){
        try{
        	//load list items from Amazon s3 servers on production system
        	List<String> list = getObjectNamesForBucket(myBucketName,myFilterKey);
        	
        	String tests [] = list.toArray(new String[list.size()]);
        	
            m_orders = new ArrayList<ListItem>();
            
            	String rootFolder;
            	
            	if(endsWithFolderSeparator(myFilterKey))
            	{
            		rootFolder = myFilterKey;
            	}
            	else
            	{
            		rootFolder = myFilterKey+"/";
            	}
            	
            	
            for(int i = 0;i < tests.length;i++)
            {
            	String key = tests[i];
                
                if(isKeyFile(key,rootFolder))
                {
                    m_orders.add(new ListItem(getFileNameFromKey(key), key, "file"));
                }
                else if(isKeyFolder(key,rootFolder))
                {
                	m_orders.add(new ListItem(getFolderNameFromKey(key), key, "folder"));
                }
                
                
            }
            
            Log.i("ARRAY", ""+ m_orders.size());
          } catch (Exception e) {
            Log.e("BACKGROUND_PROC", e.getMessage());
          }
          runOnUiThread(returnRes);
      }//end of getOrders()
	
	private Runnable returnRes = new Runnable() {

        public void run() {
            if(m_orders != null && m_orders.size() > 0){
                m_adapter.notifyDataSetChanged();
                for(int i=0;i<m_orders.size();i++)
                m_adapter.add(m_orders.get(i));
            }
            m_ProgressDialog.dismiss();
            m_adapter.notifyDataSetChanged();
        }
    };//end of runnable  returnRes
    
    /*
     * Extracts file name with extension from Amazon S3 Key string
     */
    public String getFileNameFromKey(String key)
    {
    	String input = null;
    	for(int i = key.length()-1;i >=0;i--)
    	{
    		if(key.charAt(i) == '/')
    		{
    			input = key.substring(i+1, key.length());
    			break;
    		}
    	}
    	
    	return input;
    }//end of method
    
    /*
     * Extracts folder name from Amazon S3 Key string
     */
    public String getFolderNameFromKey(String key)
    {
    	String input = null;
    	int counter = 0;
    	for(int i = key.length()-1;i >=0;i--)
    	{
    		if(key.charAt(i) == '/')
    		{
    			counter++;
    			if(counter == 2)
    			{
    				input = key.substring(i+1, key.length());
        			break;
    			}
    		}
    	}
    	
    	return input;
    }//end of method
    /*
     * Displays an alert dialog to user
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
    }
	
    private void downloadFile(String key) throws Exception
    {
    	GetObjectRequest gor = new GetObjectRequest(myBucketName, key);

		s3Client.getObject(gor, saveFileToSDCard(getFileNameFromKey(key)));
		
		
    } 
    
    
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
        intent.putExtra("username",mUsername);
        
        intent.setFlags (Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity (intent);
    }

    /*
	 * Checks if a key represents a Folder in Amazon s3
	 */
	public boolean isKeyFolder(String key,String rootFolder)
	{
		boolean result  = false;
		int keyFolderSeparators = 0;
		int rootFolderSeparators = 0;
		
		for(int i = 0;i < key.length();i++)
		{
			if(key.charAt(i) == '/')
			{
				keyFolderSeparators++;
			}
		}
		
		for(int i = 0;i < rootFolder.length();i++)
		{
			if(rootFolder.charAt(i) == '/')
			{
				rootFolderSeparators++;
			}
		}
		
		int diff = keyFolderSeparators - rootFolderSeparators;
		
		int len = key.length();
			if(diff == 1 && key.charAt(len-1) == '/')
			{
				result = true;
			}
		
		return result;
	}
	
	
	/*
	 * Checks if a key represents a file in Amazon s3
	 */
	public boolean isKeyFile(String key,String rootFolder)
	{
		boolean result  = false;
		int keyFolderSeparators = 0;
		int rootFolderSeparators = 0;
		
		for(int i = 0;i < key.length();i++)
		{
			if(key.charAt(i) == '/')
			{
				keyFolderSeparators++;
			}
		}
		
		for(int i = 0;i < rootFolder.length();i++)
		{
			if(rootFolder.charAt(i) == '/')
			{
				rootFolderSeparators++;
			}
		}
		
		int diff = keyFolderSeparators - rootFolderSeparators;
	
		int len = key.length();
			if(diff == 0 && key.charAt(len-1) != '/')
			{
				result = true;
			}
		
		return result;
	}
	
	/*
	 * Checks if a Folder name ends with folder separator
	 */
	public boolean endsWithFolderSeparator(String key)
	{
		boolean result  = false;
		
		if(key.charAt(key.length() - 1) == '/')
		{
			result = true;
		}
		return result;
	}
	
	/*
	 * Removes trailing folder separation character from a folder name
	 */
	public static String removeTrailingFolderSeparator(String key)
	{
		int len = key.length();
		return key.substring(0, len - 1);
	}
	
	 /*
	  * This method returns all keys in a given bucket.
	  */
	public List<String> getObjectNamesForBucket( String bucketName,String prefix )
	{
		ObjectListing objects = s3Client.listObjects( bucketName,prefix );
		
		List<String> objectNames = new ArrayList<String>( objects.getObjectSummaries().size() );

		Iterator<S3ObjectSummary> oIter = objects.getObjectSummaries().iterator();
		
			while(oIter.hasNext())
			{
				objectNames.add(oIter.next().getKey());
			}
		
		return objectNames;
	}//end of getObjectNames() 
	
	/*
	 * Saves a given file to micro sd card. This method should be thread safe
	 */
	private File saveFileToSDCard(String filename) throws Exception
	{
		File image = null;
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
		    //We can read and write the media
		    
		    //write file to sd card
			File sdCardDirectory = Environment.getExternalStorageDirectory();
		    image = new File(sdCardDirectory, filename);
		    
		    boolean success = false;

		    //Encode the file as a PNG image.
		    FileOutputStream outStream;
		    try {

		        outStream = new FileOutputStream(image);

		        ObjectOutputStream oos = new ObjectOutputStream(outStream);
		       	        
		        oos.close();
		        outStream.flush();
		        outStream.close();
		        success = true;
		    } 
		    catch (Exception e) 
		    {
		        //handle exception
		    	throw new RuntimeException("Error Saving File!",e);
		    } 

		} 
		else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) 
		{
		    // We can only read the media
			throw new RuntimeException("External storage is mounted in READ ONLY State!");
		} 
		else
		{
		    //Something else is wrong. It may be one of many other states, but all we need
		    //to know is we can neither read nor write
			throw new RuntimeException("External Stoarge is not available!");
		}
			
		return image;
	}//end of saveFileToSdCard()
	
    //overide life cycle methods
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
	}
	

	private class FileDownloadAsyncTask extends AsyncTask<Object, Boolean, String> 
	{

		String result = null;
		boolean isDownloadFailed = false;
		
		@Override
		protected String doInBackground(Object... params)
		{
		String key = (String) params[0];
		try {
			downloadFile(key);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			result = e.getMessage();
			isDownloadFailed = true;		}
		return null;
		}

		

		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			m_DownloadProgressDialog.dismiss();
			
				if(isDownloadFailed)
				{
					displayAlert("Download Failed", result);
				}
				else
				{
					displayAlert("File Download","File download was successful.");
				}
			super.onPostExecute(result);
		}



		@Override
		protected void onPreExecute() {
			m_DownloadProgressDialog = ProgressDialog.show(FileDownloadActivity1.this,    
  	              "Please wait...", "Downloading file ...", true);
		super.onPreExecute();
		}



	}//end of inner class
	
	/*
	 * Updates address bar
	 */
	public String updateAddressBarWithPath(String key,String rootFolder)
	{
		int keyLen = key.length();
		int len = rootFolder.length();
		
		if(keyLen == len)
		{
			return "";
		}
		else
		{
			return key.substring(len + 1, keyLen);
		}
	}
	
	/*
	 * Sets the Activity's Title
	 */
	public void setTitleFromActivityLabel (int textViewId)
	{
	    TextView tv = (TextView) findViewById (textViewId);
	    if (tv != null) tv.setText (getTitle ());
	}//end setTitleText

}//end of class

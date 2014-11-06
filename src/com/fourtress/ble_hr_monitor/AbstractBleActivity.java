package com.fourtress.ble_hr_monitor;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class AbstractBleActivity extends Activity
{
	private ServiceConnection sConn;
	private Messenger messenger;
	
	private BleServiceBroadcastReceiver bleServiceBroadcastReceiver;
	
	public class BleServiceBroadcastReceiver extends BroadcastReceiver 
	{
		@Override
		public void onReceive( Context context, Intent intent ) 
		{
			String receiveStr = intent.getStringExtra( BleService.EXTRA_KEY_OUT );
			BleServiceCallback( receiveStr );
		}
	}
	
	static class BleResponseHandler extends Handler // This class handles the Service response
	{
	    @Override
	    public void handleMessage( Message msg ) 
	    {
	        switch ( msg.what ) // get the message type
	        {
		        case BleService.RESPONSE: 
		        {
		            String result = msg.getData().getString( "respData" );
		            Log.d( "DEBUG", "HEARTRATEACTIVITY RECEIVED: " + result );
		        }
	        }
	    }
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		createMessageHandler();
		initBroadcastIntents();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		return super.onCreateOptionsMenu(menu);
	}
	

	@Override
	protected void onResume() 
	{
		super.onResume();
		createMessageHandler();
		initBroadcastIntents();
		// We bind to the service
		bindService( new Intent( this, BleService.class ), sConn, Context.BIND_AUTO_CREATE );
	}

	@Override
	protected void onPause() 
	{
		super.onPause();
		unbindService( sConn );
	}

	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
		return super.onOptionsItemSelected(item);
	}
	
	protected void createMessageHandler()
	{
		// Service Connection to handle system callbacks
        sConn = new ServiceConnection() 
        {
            @Override
            public void onServiceDisconnected( ComponentName name ) 
            {
                messenger = null;
            }

			@Override
			public void onServiceConnected( ComponentName name, IBinder service ) 
			{
				messenger = new Messenger( service ); // We are connected to the service
			}
        };
	}
	
	protected void sendRequestToBleService( String str, int flag )
	{
        Message msg = Message.obtain( null, flag );
 
        msg.replyTo = new Messenger( new BleResponseHandler() );

        Bundle b = new Bundle();
        b.putString( "data", str );
 
        msg.setData( b );
        
        try 
        {
            messenger.send( msg );
        } 
        catch ( RemoteException e )
        {                    
            e.printStackTrace();
        }
	}
	
	private void initBroadcastIntents()
	{
		Intent intentMyIntentService = new Intent( this, BleService.class );
		intentMyIntentService.putExtra( BleService.EXTRA_KEY_IN, "test" );
		startService( intentMyIntentService );
		
		bleServiceBroadcastReceiver = new BleServiceBroadcastReceiver();
		
		//register BroadcastReceiver
		IntentFilter intentFilter = new IntentFilter( BleService.ACTION_MyIntentService );
		intentFilter.addCategory( Intent.CATEGORY_DEFAULT );
		registerReceiver( bleServiceBroadcastReceiver, intentFilter );
	}
	
	protected void BleServiceCallback( String cmd )
	{
		// this only serves as a callback to the concrete BleActivity class.
	}
	
}

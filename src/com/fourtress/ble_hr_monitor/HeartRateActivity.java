package com.fourtress.ble_hr_monitor;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class HeartRateActivity extends Activity implements OnClickListener
{	
	private int cnt = 60;
	
	private ServiceConnection sConn;
	private Messenger messenger;
	
	private Button TestButton1, TestButton2;
	private TextView VirtualHeartRate;
	
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
	protected void onCreate( Bundle savedInstanceState ) 
	{
		super.onCreate( savedInstanceState );
		
		setContentView( R.layout.heartrate );
		
		TestButton1 = (Button) findViewById(R.id.bTestButton1);
		TestButton2 = (Button) findViewById(R.id.bTestButton2);
		VirtualHeartRate = (TextView) findViewById(R.id.tvHeartRate);
		
		VirtualHeartRate.setOnClickListener(this);
		TestButton1.setOnClickListener(this);
		TestButton2.setOnClickListener(this);
		
		VirtualHeartRate.setText( Integer.toString( cnt ) );
		
		createMessageHandler();
	}
	
	@Override
	public boolean onCreateOptionsMenu( Menu menu ) 
	{
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate( R.menu.main, menu );
	    return true;
	}

	@Override
	protected void onResume() 
	{
		super.onResume();
		//startService( new Intent( this, BleService.class ) );
	}

	@Override
	protected void onPause() 
	{
		super.onPause();
	}

	@Override
	public boolean onOptionsItemSelected( MenuItem item ) 
	{
		Intent NextActivity;
		switch(item.getItemId())
		{
		case R.id.action_ble_test:
			NextActivity = new Intent("com.fourtress.ble_hr_monitor.BLE_TEST_TEST"); // start ble_debug activity
			startActivity(NextActivity);
			return true;
		case R.id.action_show_devices:
			return super.onOptionsItemSelected( item );
		case R.id.action_settings:
			NextActivity = new Intent("com.fourtress.ble_hr_monitor.SETTINGS"); // start settings activity
			startActivity(NextActivity);
			return true;
		case R.id.action_help:
			// show help popup
			return super.onOptionsItemSelected( item );
		default:
			return super.onOptionsItemSelected( item );
		}
	}

	@Override
	public void onClick(View v) 
	{
		switch( v.getId() )
		{
		case R.id.bTestButton1:
			Log.d( "DEBUG", "button1 clicked" );
			sendRequestToBleService();
//			Intent bleServiceIntent = new Intent( this, BleService.class );
//			bleServiceIntent.putExtra("activity", "previousActivity");
//			startService( bleServiceIntent );
			break;
		case R.id.bTestButton2:
			Log.d( "DEBUG", "button2 clicked" );
//			stopService( new Intent( this, BleService.class ) );
			break;
		default:
		}
	}

	private void createMessageHandler()
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
		// We bind to the service
        bindService( new Intent( this, BleService.class ), sConn, Context.BIND_AUTO_CREATE );
	}
	
	private void sendRequestToBleService()
	{
        Message msg = Message.obtain( null, BleService.START_SCAN );
 
        msg.replyTo = new Messenger( new BleResponseHandler() );

        Bundle b = new Bundle();
        b.putString("data", "Activity-To-Service-String");
 
        msg.setData(b);
        
        try 
        {
            messenger.send(msg);
        } 
        catch (RemoteException e)
        {                    
            e.printStackTrace();
        }
	}
}

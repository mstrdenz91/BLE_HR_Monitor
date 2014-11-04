package com.fourtress.ble_hr_monitor;

import java.util.Scanner;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
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
import android.widget.Toast;

public class HeartRateActivity extends Activity implements OnClickListener
{	
	private ServiceConnection sConn;
	private Messenger messenger;
	
	private BleServiceBroadcastReceiver bleServiceBroadcastReceiver;
	
	private Button TestButton1, TestButton2;
	private TextView RSSILabel, RSSI, HeartRate;
	
	Intent enableBleIntent = new Intent( BluetoothAdapter.ACTION_REQUEST_ENABLE );
	
	public class BleServiceBroadcastReceiver extends BroadcastReceiver 
	{
		@Override
		public void onReceive( Context context, Intent intent ) 
		{
			int readData = 0;
			int signalColor = Color.DKGRAY;
			String receiveStr = intent.getStringExtra( BleService.EXTRA_KEY_OUT );
			if( receiveStr.equalsIgnoreCase( "Hardware_Not_Available") )
			{
				Toast.makeText( context, "Hardware is NOT BLE compatible!", Toast.LENGTH_LONG ).show();
			}
			else if( receiveStr.equalsIgnoreCase( "Request_Ble_Enable" ) )
			{
				enableBleIntent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP );
				HeartRateActivity.this.startActivity( enableBleIntent );
			}
			else if( receiveStr.equalsIgnoreCase( "Device_Found" ) )
			{
				Toast.makeText( context, "RFduino Found!", Toast.LENGTH_LONG ).show();
			}
			else if( receiveStr.equalsIgnoreCase( "Device_Connected" ) )
			{
				Toast.makeText( context, "RFduino Connected!", Toast.LENGTH_LONG ).show();
			}
			else if( receiveStr.equalsIgnoreCase( "Device_Disconnected" ) )
			{
				Toast.makeText( context, "RFduino Disconnected!", Toast.LENGTH_LONG ).show();
			}
			else if( receiveStr.startsWith( "Data_Read" ) )
			{
				Scanner parse = new Scanner( receiveStr ).useDelimiter("[^0-9]+"); // Regular Expressions
				readData = parse.nextInt();
				HeartRate.setText( Integer.toString( readData ) );
			}
			else if( receiveStr.startsWith( "New_RSSI" ) )
			{
				Scanner parse = new Scanner( receiveStr ).useDelimiter("[^0-9]+"); // Regular Expressions
				readData = parse.nextInt();
				if( readData < 30 ) { signalColor = Color.RED; }
				else if( readData < 40 ) { signalColor = 0xffff8800; } // Orange 
				else { signalColor = 0xff008800; } // Dark Green
				RSSILabel.setTextColor( signalColor );
				RSSI.setTextColor( signalColor );
				RSSI.setText( Integer.toString( readData ) );
			}
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
	protected void onCreate( Bundle savedInstanceState ) 
	{
		super.onCreate( savedInstanceState );
		
		setContentView( R.layout.heartrate );
		
		TestButton1 = (Button) 		findViewById( R.id.bTestButton1 );
		TestButton2 = (Button) 		findViewById( R.id.bTestButton2 );
		HeartRate 	= (TextView) 	findViewById( R.id.tvHeartRate );
		RSSILabel	= (TextView)	findViewById( R.id.tvRSSILabel );
		RSSI 		= (TextView) 	findViewById( R.id.tvRSSI );
		
		HeartRate.setOnClickListener(this);
		TestButton1.setOnClickListener(this);
		TestButton2.setOnClickListener(this);
		
		RSSI.setText( "..." );
		HeartRate.setText( "..." );
		
		createMessageHandler();
		
		initBroadcastIntents();
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
		case R.id.action_hr_graph:
			NextActivity = new Intent( "com.fourtress.ble_hr_monitor.HRGRAPH" );
			startActivity( NextActivity );
			return super.onOptionsItemSelected( item );
		case R.id.action_show_devices:
			return super.onOptionsItemSelected( item );
		case R.id.action_settings:
			NextActivity = new Intent( "com.fourtress.ble_hr_monitor.SETTINGS" ); // start settings activity
			startActivity( NextActivity );
			return true;
		case R.id.action_about:
			NextActivity = new Intent( "com.fourtress.ble_hr_monitor.ABOUT" );
			startActivity( NextActivity );
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
			sendRequestToBleService( "Activity-To-Service-String", BleService.START_SCAN );
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
	
	private void sendRequestToBleService( String str, int flag )
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
	
}

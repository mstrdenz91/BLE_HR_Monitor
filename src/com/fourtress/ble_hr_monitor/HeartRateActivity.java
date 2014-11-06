package com.fourtress.ble_hr_monitor;

import java.util.Scanner;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class HeartRateActivity extends AbstractBleActivity implements OnClickListener
{	
	private Button TestButton1, TestButton2;
	private TextView RSSILabel, RSSI, HeartRate;
	
	Intent enableBleIntent = new Intent( BluetoothAdapter.ACTION_REQUEST_ENABLE );
	
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
	
	protected void BleServiceCallback( String cmd )
	{
		super.BleServiceCallback(cmd);
		int readData = 0;
		int signalColor = Color.DKGRAY;
		if( cmd.equalsIgnoreCase( "Hardware_Not_Available") )
		{
			Toast.makeText( this, "Hardware is NOT BLE compatible!", Toast.LENGTH_LONG ).show();
		}
		else if( cmd.equalsIgnoreCase( "Request_Ble_Enable" ) )
		{
			enableBleIntent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP );
			startActivity( enableBleIntent );
		}
		else if( cmd.equalsIgnoreCase( "Device_Found" ) )
		{
			Toast.makeText( this, "RFduino Found!", Toast.LENGTH_LONG ).show();
		}
		else if( cmd.equalsIgnoreCase( "Device_Connected" ) )
		{
			Toast.makeText( this, "RFduino Connected!", Toast.LENGTH_LONG ).show();
		}
		else if( cmd.equalsIgnoreCase( "Device_Disconnected" ) )
		{
			Toast.makeText( this, "RFduino Disconnected!", Toast.LENGTH_LONG ).show();
		}
		else if( cmd.startsWith( "Data_Read" ) )
		{
			Scanner parse = new Scanner( cmd ).useDelimiter("[^0-9]+"); // Regular Expressions
			readData = parse.nextInt();
			HeartRate.setText( Integer.toString( readData ) );
		}
		else if( cmd.startsWith( "New_RSSI" ) )
		{
			Scanner parse = new Scanner( cmd ).useDelimiter("[^0-9]+"); // Regular Expressions
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

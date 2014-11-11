package com.fourtress.ble_hr_monitor;

import java.util.Scanner;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.TextView;
import android.widget.Toast;

public class HeartRateActivity extends AbstractBleActivity
{	
	private TextView RSSILabel, RSSI, HeartRate;
	
	Intent enableBleIntent = new Intent( BluetoothAdapter.ACTION_REQUEST_ENABLE );
	
	@Override
	protected void onCreate( Bundle savedInstanceState ) 
	{
		super.onCreate( savedInstanceState );
		
		setContentView( R.layout.heartrate );
		
		HeartRate 	= (TextView) 	findViewById( R.id.tvHeartRate );
		RSSILabel	= (TextView)	findViewById( R.id.tvRSSILabel );
		RSSI 		= (TextView) 	findViewById( R.id.tvRSSI );
		
		RSSI.setText( "..." );
		HeartRate.setText( "..." );
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate( R.menu.main_menu, menu );
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
			//Toast.makeText( this, "RFduino Found!", Toast.LENGTH_LONG ).show();
		}
		else if( cmd.equalsIgnoreCase( "Device_Connected" ) )
		{
			//Toast.makeText( this.getApplication(), "RFduino Connected!", Toast.LENGTH_SHORT ).show();
		}
		else if( cmd.equalsIgnoreCase( "Device_Disconnected" ) )
		{
			//Toast.makeText( this, "RFduino Disconnected!", Toast.LENGTH_SHORT ).show();
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
			if( readData == 0 ){ HeartRate.setText( "..." ); }
		}
	}

}

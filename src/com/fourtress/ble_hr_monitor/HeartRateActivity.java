package com.fourtress.ble_hr_monitor;

import com.fourtress.ble_accelerator_lib.BleFacade;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
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
	//BleFacade bleHandler = null;
	
	private int cnt = 60;
	private boolean deviceFound = false;
	
	private Button TestButton1, TestButton2;
	private TextView VirtualHeartRate;

	@Override
	protected void onCreate( Bundle savedInstanceState ) 
	{
		super.onCreate( savedInstanceState );
		
//		bleHandler = new BleFacade( this );
		
		setContentView( R.layout.heartrate );
		
		TestButton1 = (Button) findViewById(R.id.bTestButton1);
		TestButton2 = (Button) findViewById(R.id.bTestButton2);
		VirtualHeartRate = (TextView) findViewById(R.id.tvHeartRate);
		
		VirtualHeartRate.setOnClickListener(this);
		TestButton1.setOnClickListener(this);
		TestButton2.setOnClickListener(this);
		
		VirtualHeartRate.setText( Integer.toString( cnt ) );
		
//		bleHandler.checkHardwareAvailable();
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
		
		Log.d("DEBUG", "Start intent");
		startService( new Intent( this, BleService.class ) );
		
		
//		if( bleHandler.enable() )
//		{
//			Log.d("DEBUG", "Bluetooth enabled");
//			VirtualHeartRate.setText( Integer.toString( ++cnt ) );
//			bleHandler.startScan();
//			Log.d("DEBUG", "Start scan");
//			customDelay( 8000 );
//			if( deviceFound )
//			{
//				Log.d("DEBUG", "Device found");
//				customDelay( 2000 );
//				VirtualHeartRate.setText( Integer.toString( ++cnt ) );
//				Log.d("DEBUG", "Stop scan");
//				bleHandler.stopScan();
//				if (bleHandler.connect( "RFduino" ) )
//				{
//					Log.d("DEBUG", "RFduino connected");
//					VirtualHeartRate.setText( Integer.toString( ++cnt ) );
//					byte[] led = new byte[]{1};
//					Log.d("DEBUG", "Toggle LED");
//					if( bleHandler.sendRFduinoData( led ) )
//					{
//						VirtualHeartRate.setText( Integer.toString( ++cnt ) );
//						Log.d("DEBUG", "Toggle LED succeeded");
//					}
//				}
//			}
//		}
//		Log.d("DEBUG", "Ble not enabled?");
	}

	@Override
	protected void onPause() 
	{
		super.onPause();
//		bleHandler.disconnect();
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

	public void bleDeviceFoundCallback( BluetoothDevice device, final int rssi )
	{
		deviceFound = true;
	}
	
	public void bleDataReadCallback( byte receivedByte )
	{
		VirtualHeartRate.setText( receivedByte );
	}

	@Override
	public void onClick(View v) 
	{
		switch( v.getId() )
		{
		case R.id.bTestButton1:
			Log.d("DEBUG", "button1 clicked");
			Intent intent = new Intent(this, BleService.class);
			startService(intent);
			break;
		case R.id.bTestButton2:
			Log.d("DEBUG", "button2 clicked");
			stopService( new Intent( this, BleService.class ) );
			break;
		default:
		}
		
//		if( v.getId() == R.id.tvHeartRate )
//		{
//			cnt++;
//			VirtualHeartRate.setText( Integer.toString( cnt ) );
//		}
//		switch(cnt)
//		{
//		case 61:
//			bleHandler.enable();
//			break;
//		case 62:
//			bleHandler.startScan();
//			break;
//		case 63:
//			bleHandler.stopScan();
//			break;
//		case 64:
//			bleHandler.connect( "RFduino" );
//			break;
//		case 65:
//			byte[] led = new byte[]{1};
//			bleHandler.sendRFduinoData( led );
//			break;
//		default:
//			bleHandler.disconnect();
//		}
	}

//	private void customDelay( int ms )
//	{
//		try 
//		{
//			Thread.sleep( ms );
//		} 
//		catch  (InterruptedException e ) 
//		{
//			e.printStackTrace();
//		}
//	}
}

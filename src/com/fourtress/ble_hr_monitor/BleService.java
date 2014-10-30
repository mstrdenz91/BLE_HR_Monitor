package com.fourtress.ble_hr_monitor;

import com.fourtress.ble_accelerator_lib.BleWrapper;
import com.fourtress.ble_accelerator_lib.BleWrapperUiCallbacks;

import android.R.string;
import android.app.Activity;
import android.app.IntentService;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class BleService extends IntentService 
{	
	/////////////////////OLD BLE SERVICE/////////////////////////////////
	final static private int STATE_DISABLED 	= 1;
	final static private int STATE_ENABLED 		= 2;
	final static private int STATE_SCANNING 	= 3;
	final static private int STATE_FOUND 		= 4;
	final static private int STATE_CONNECTED 	= 5;
	static private int connectionState = STATE_DISABLED;
//	BleFacade bleHandler = null;
	/////////////////////////////////////////////////////////////////////
	
	private BleWrapper bleWrapper = null;
//	private Activity CurrentActivity = null;
	
	////////////////Functions:////////////////////
	
	public BleService() { super( "BleService" ); } // empty constructor

	@Override
	public int onStartCommand( Intent intent, int flags, int startId ) 
	{
		Log.d( "DEBUG", "onStartCommand" );
		setCallbacks();
	    Toast.makeText( this, "service started", Toast.LENGTH_SHORT ).show();
	    
//	    String activityName = intent.getStringExtra( "activity" );

//			Class callerClass = Class.forName( activityName );
//			CurrentActivity = (Activity) callerClass;		
	    
	    //CurrentActivity = (CurrentActivity) intent.getClass();
	    return super.onStartCommand( intent, flags, startId );
	}
	
	@Override
	public IBinder onBind( Intent intent ) 
	{
		return super.onBind(intent);
	}
	
	@Override
	public void onDestroy() 
	{
		Log.d( "DEBUG", "onStopCommand" );
		Toast.makeText( this, "service stopped", Toast.LENGTH_SHORT ).show();
		super.onDestroy();
	}

	@Override
	protected void onHandleIntent(Intent intent) 
	{
		if( !checkHardwareAvailable() )
		{
			stopSelf();
		}
		while( true )
		{
			switch( connectionState )
			{
			case STATE_DISABLED:
				if( enable() )
				{
					connectionState = STATE_ENABLED;
				}
				break;
			case STATE_ENABLED:
				//bleHandler.startScan();
				connectionState = STATE_SCANNING;
				break;
			case STATE_SCANNING:
				// nothing to do...?
				break;
			case STATE_FOUND:
				
				break;
			case STATE_CONNECTED:
				break;
			}
		}
	}

	public void bleDeviceFoundCallback( BluetoothDevice device, final int rssi )
	{
		connectionState = STATE_FOUND;
	}
	
	private void serviceDelay( int ms )
	{
		try 
		{
			Thread.sleep( ms );
		} 
		catch  (InterruptedException e ) 
		{
			e.printStackTrace();
		}
	}

	/////////////BleFacade:////////////////////////
	
	private boolean checkHardwareAvailable()
	{
		if ( bleWrapper != null )
		{
			if( bleWrapper.checkBleHardwareAvailable() == false )
			{
				Toast.makeText( this, "Hardware not BLE compatible!", Toast.LENGTH_SHORT ).show();
			}
			else
			{
				return true;
			}
		}
		return false;
	}
	
	private void setCallbacks() // Set the callback for the bleWrapper:
	{
		bleWrapper = new BleWrapper( this, new BleWrapperUiCallbacks.Null()
		{
			@Override
			public void uiDeviceFound( final BluetoothDevice device, final int rssi, final byte[] record )
			{
				super.uiDeviceFound( device, rssi, record );
			}
			
			@Override
			public void uiNewValueForCharacteristic( BluetoothGatt gatt, BluetoothDevice device, BluetoothGattService service, 
					BluetoothGattCharacteristic ch, String strValue, int intValue, byte[] rawValue, String timestamp )
			{
				super.uiNewValueForCharacteristic( gatt, device, service, ch, strValue, intValue, rawValue, timestamp);
				Log.d("DEBUG", "uiNewValueForCharacteristic");
			}
		} );
	}

	public void setCurrentActivity()
	{
//		if( CurrentActivity != null )
//		{
//			bleWrapper.setCurrentActivity( CurrentActivity );
//		}
	}
	
	private boolean enable()
	{
		if ( bleWrapper.isBtEnabled() == false ) // check if Bluetooth is enabled
		{
			// Bluetooth is not enabled. Request to user to turn it on
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			.startActivity( enableBtIntent );
		}
		boolean initialized = bleWrapper.initialize();
		if( bleWrapper.isBtEnabled() && initialized )
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	private void startScan()
	{
		bleWrapper.startScanning();
	}
	
	private void stopScan()
	{
		bleWrapper.stopScanning();
	}

	private void scanForDuration(int ms)
	{
		
	}

}

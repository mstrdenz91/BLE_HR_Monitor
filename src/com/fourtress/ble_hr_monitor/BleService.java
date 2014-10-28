package com.fourtress.ble_hr_monitor;

import com.fourtress.ble_accelerator_lib.BleFacade;

import android.app.IntentService;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class BleService extends IntentService 
{	
	final static private int STATE_DISABLED 	= 1;
	final static private int STATE_ENABLED 		= 2;
	final static private int STATE_SCANNING 	= 3;
	final static private int STATE_FOUND 		= 4;
	final static private int STATE_CONNECTED 	= 5;
	
	static private int connectionState = STATE_DISABLED;
	
	BleFacade bleHandler = null;
	
	public BleService() // empty constructor
	{
		super("BleService");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) 
	{
		Log.d("DEBUG", "onStartCommand");
	    Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();
	    return super.onStartCommand(intent,flags,startId);
	}
	
	@Override
	public IBinder onBind(Intent intent) 
	{
		return super.onBind(intent);
	}
	
	@Override
	public void onDestroy() 
	{
		Log.d("DEBUG", "onStopCommand");
		Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();
		super.onDestroy();
	}

	@Override
	protected void onHandleIntent(Intent intent) 
	{
		if( !bleHandler.checkHardwareAvailable() )
		{
			stopSelf();
		}
		while( true )
		{
			switch( connectionState )
			{
			case STATE_DISABLED:
				if( bleHandler.enable() )
				{
					connectionState = STATE_ENABLED;
				}
				break;
			case STATE_ENABLED:
				bleHandler.startScan();
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
}

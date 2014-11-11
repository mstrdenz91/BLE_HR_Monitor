package com.fourtress.ble_hr_monitor;

import com.fourtress.ble_accelerator_lib.BleDefinedUUIDs;
import com.fourtress.ble_accelerator_lib.BleWrapper;
import com.fourtress.ble_accelerator_lib.BleWrapperUiCallbacks;

import android.app.IntentService;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

public class BleService extends IntentService 
{	
	/////////////////////OLD BLE SERVICE/////////////////////////////////
	final static private int STATE_DISABLED 	= 1;
	final static private int STATE_SCANNING 	= 2;
	final static private int STATE_FOUND 		= 3;
	final static private int STATE_CONNECTED 	= 4;
	static private int connectionState 			= STATE_DISABLED;
	/////////////////////////////////////////////////////////////////////
	
	private BleWrapper 					bleWrapper 				= null;
	private BluetoothDevice 			bleDevice 				= null;
	private BluetoothGatt 				bleGatt 				= null;
	private BluetoothGattService 		bleGattService 			= null;
	private BluetoothGattCharacteristic bleGattCharacteristic 	= null;
	
	byte[] sendVal = new byte[]{0};
	
	final static public int START_SCAN		= 234;
	final static public int RESPONSE 		= 123;
	
	public static final String ACTION_MyIntentService = "com.fourtress.ble_hr_monitor.MyBroadcastReceiver";
	public static final String EXTRA_KEY_IN = "EXTRA_IN";
	public static final String EXTRA_KEY_OUT = "EXTRA_OUT";
	
	static class ActivityRequestHandler extends Handler
	{
		public void handleMessage( Message msg )
		{
			switch( msg.what ) // get the message type
			{
			    case START_SCAN: 
			    {
			        try 
			        {  // Incoming data
			            String data = msg.getData().getString( "data" );
			            Message resp = Message.obtain( null, RESPONSE );
			            
			            Bundle bResp = new Bundle();
			            bResp.putString("respData", "Service-To-Activity-String" );
			            resp.setData( bResp );
			            
			            msg.replyTo.send( resp );
			            
			            Log.d( "DEBUG", "BLESERVICE RECEIVED: " + data );
			        } 
			        catch ( RemoteException e ) 
			        {
			            e.printStackTrace();
			        }
			        break;
			    }
			    default: 
			        super.handleMessage( msg );
		    }
		}
	}
	
	private Messenger msg = new Messenger( new ActivityRequestHandler() );
	
	////////////////Functions:////////////////////
	
	public BleService() { super( "BleService" ); } // empty constructor

	@Override
	public int onStartCommand( Intent intent, int flags, int startId ) 
	{
//		Log.d( "DEBUG", "onStartCommand" );
//		setCallbacks();
//	    Toast.makeText( this, "service started", Toast.LENGTH_SHORT ).show();
	    
//	    String activityName = intent.getStringExtra( "activity" );

//			Class callerClass = Class.forName( activityName );
//			CurrentActivity = (Activity) callerClass;		
	    
	    //CurrentActivity = (CurrentActivity) intent.getClass();
	    return super.onStartCommand( intent, flags, startId );
	}
	
	@Override
	public void onCreate() 
	{
		super.onCreate();
		setCallbacks();
	}
	
	@Override
	public void onDestroy() 
	{
		Log.d( "DEBUG", "onStopCommand" );
		Toast.makeText( this, "service stopped", Toast.LENGTH_SHORT ).show();
		super.onDestroy();
	}

	@Override
	public IBinder onBind( Intent intent ) 
	{
		Log.d( "DEBUG", "onBind" );
		return msg.getBinder();
		//return super.onBind(intent);
	}
	
	@Override
	public boolean onUnbind(Intent intent) 
	{
		Log.d( "DEBUG", "onUnbind" );
		return super.onUnbind(intent);
	}

	@Override
	protected void onHandleIntent(Intent intent) 
	{
		if( !checkHardwareAvailable() )
		{
			customBroadcast( "Hardware_Not_Available!" );
			stopSelf();
		}
		while( true )
		{
			switch( connectionState )
			{
			case STATE_DISABLED:
				if( enable() )
				{
					connectionState = STATE_SCANNING;
				}
				break;
			case STATE_SCANNING:
				scanForDuration( 2000 );
				break;
			case STATE_FOUND:
				if( connect( "RFduino" ) )
				{
					connectionState = STATE_CONNECTED;
				}
				break;
			case STATE_CONNECTED:
				sendVal[0] ^= 1;
				sendRFduinoData( sendVal );
				receiveRFduinoData();
				break;
			}
			serviceDelay( 1000 );
		}
	}

	public void bleDeviceFoundCallback( BluetoothDevice device, final int rssi )
	{
		if( device.getName().equalsIgnoreCase( "RFduino" ) )
		{
			//customBroadcast( "Device_Found" );
			stopScan();
			bleDevice = device;
		}
		connectionState = STATE_FOUND;
	}
	
	public void bleNewRssiCallback( BluetoothDevice device, final int rssi )
	{
		if( device.getName().equalsIgnoreCase( "RFduino" ) )
		{
			customBroadcast( "New_RSSI " + ( rssi + 100 ) );
		}
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

	private void customBroadcast( String str )
	{
	   Intent broadcastMessage = new Intent();
	   broadcastMessage.setAction( ACTION_MyIntentService );
	   broadcastMessage.addCategory( Intent.CATEGORY_DEFAULT );
	   broadcastMessage.putExtra( EXTRA_KEY_OUT, str );
	   sendBroadcast( broadcastMessage );
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
				bleDeviceFoundCallback( device, rssi );
			}
			
			@Override
			public void uiNewRssiAvailable(BluetoothGatt gatt, BluetoothDevice device, int rssi) 
			{
				super.uiNewRssiAvailable(gatt, device, rssi);
				bleNewRssiCallback( device, rssi );
			}

			@Override
			public void uiDeviceConnected(BluetoothGatt gatt, BluetoothDevice device) 
			{
				super.uiDeviceConnected(gatt, device);
				connectionState = STATE_CONNECTED;
				customBroadcast( "Device_Connected" );
			}

			@Override
			public void uiDeviceDisconnected(BluetoothGatt gatt, BluetoothDevice device) 
			{
				super.uiDeviceDisconnected(gatt, device);
				connectionState = STATE_SCANNING;
				customBroadcast( "Device_Disconnected" );
				bleNewRssiCallback( device, -100 );
			}

			@Override
			public void uiNewValueForCharacteristic( BluetoothGatt gatt, BluetoothDevice device, BluetoothGattService service, 
					BluetoothGattCharacteristic ch, String strValue, int intValue, byte[] rawValue, String timestamp )
			{
				super.uiNewValueForCharacteristic( gatt, device, service, ch, strValue, intValue, rawValue, timestamp);
				customBroadcast( "Data_Read " + intValue );
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
			customBroadcast( "Request_Ble_Enable" ); // send message to Activity
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
		startScan();
		serviceDelay( ms );
		stopScan();
	}

	private boolean connect( String name )
	{
		if( bleDevice!= null && bleDevice.getName().equalsIgnoreCase( "RFduino" ) )
		{
			if( bleWrapper.connect( bleDevice.getAddress().toString() ) )
			{
				return true;
			}
			else
			{
				Log.d("DEBUG", "uiDeviceFound: Connection problem");
				return false;
			}
		}
		else
		{
			return false;
		}
	}
	
	public void disconnect()
	{
		bleWrapper.diconnect();
		bleWrapper.close();
	}

	public boolean sendRFduinoData( byte[] val )
	{
		bleGatt = bleWrapper.getGatt();
		bleGattService = bleGatt.getService( BleDefinedUUIDs.Service.RFDUINO_UUID_SERVICE );
		bleGattCharacteristic = bleGattService.getCharacteristic( BleDefinedUUIDs.Characteristic.RFDUINO_UUID_SEND );
		
        	// toggle the first bit
        bleGattCharacteristic.setValue( val );
        bleGattCharacteristic.setWriteType( BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE );
        return bleGatt.writeCharacteristic( bleGattCharacteristic );
    }

	public void receiveRFduinoData()
	{
		bleGatt = bleWrapper.getGatt();
		bleGattService = bleGatt.getService(BleDefinedUUIDs.Service.RFDUINO_UUID_SERVICE);
		bleGattCharacteristic = bleGattService.getCharacteristic(BleDefinedUUIDs.Characteristic.RFDUINO_UUID_RECEIVE);
		bleWrapper.requestCharacteristicValue( bleGattCharacteristic );
	}

}

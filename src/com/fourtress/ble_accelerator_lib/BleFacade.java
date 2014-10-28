package com.fourtress.ble_accelerator_lib;

//import com.fourtress.ble_hr_monitor.BleTestTest;
import com.fourtress.ble_hr_monitor.HeartRateActivity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class BleFacade 
{
	final static public int CONNECT_CLOSEST = 1;
	
	private BleWrapper bleWrapper = null;
	private BluetoothDevice bleDevice = null;
	
	private BluetoothGatt bleGatt = null;
	private BluetoothGattService bleGattService = null;
	private BluetoothGattCharacteristic bleGattCharacteristic = null;
	
	private Activity CurrentActivity = null;
	
	public BleFacade( Activity caller )	// Constructor
	{
		CurrentActivity = caller;
		setCallbacks();
	}
	
	public boolean checkHardwareAvailable()
	{
		if ( bleWrapper.checkBleHardwareAvailable() == false )
		{
			Toast.makeText( CurrentActivity, "Hardware not BLE compatible!", Toast.LENGTH_SHORT ).show();
			//CurrentActivity.finish();
			return false;
		}
		return true;
	}
	
	public void setCallbacks() // Set the callback for the bleWrapper:
	{
		bleWrapper = new BleWrapper( CurrentActivity, new BleWrapperUiCallbacks.Null()
		{
			@Override
			public void uiDeviceFound( final BluetoothDevice device, final int rssi, final byte[] record )
			{
				CurrentActivity.runOnUiThread( new Runnable() 
	            {
	                @Override
	                public void run() 
	                {
	        			if( device != null && device.getName().equalsIgnoreCase( "RFduino" ) ) // select the closest RFduino.
	        			{
	        				bleDevice = device;

	        				//( (BleTestTest) CurrentActivity ).bleDeviceFoundCallback( device, rssi );
	        				//( (HeartRateActivity) CurrentActivity ).bleDeviceFoundCallback( device, rssi );
	        			}
	                } 
	            } );
			}
			
			@Override
			public void uiNewValueForCharacteristic( BluetoothGatt gatt, BluetoothDevice device, BluetoothGattService service, 
					BluetoothGattCharacteristic ch, String strValue, int intValue, byte[] rawValue, String timestamp )
			{
				super.uiNewValueForCharacteristic( gatt, device, service, ch, strValue, intValue, rawValue, timestamp);
				Log.d("DEBUG", "uiNewValueForCharacteristic");
				//( (BleTestTest) CurrentActivity ).bleDataReadCallback( rawValue[0] );
				//( (HeartRateActivity) CurrentActivity ).bleDataReadCallback( rawValue[0] );
			}
		} );
	}
	
	public boolean enable()
	{
		if ( bleWrapper.isBtEnabled() == false ) // check if Bluetooth is enabled
		{
			// Bluetooth is not enabled. Request to user to turn it on
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			CurrentActivity.startActivity( enableBtIntent );
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
	
	public void startScan()
	{
		bleWrapper.startScanning();
	}
	
	public void stopScan()
	{
		bleWrapper.stopScanning();
	}
	
	public void scanForDuration(int ms)
	{
		
	}
	
	public boolean connect( String name )
	{
		if( bleDevice.getName().equalsIgnoreCase( "RFduino" ) )
		{
			if( bleWrapper.connect( bleDevice.getAddress().toString() ) )
			{
				Log.d("DEBUG", "Connected with closest RFduino");
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
			Log.d("DEBUG", "No RFduino device found");
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

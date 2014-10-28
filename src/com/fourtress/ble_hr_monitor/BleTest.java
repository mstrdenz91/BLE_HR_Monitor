package com.fourtress.ble_hr_monitor;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.fourtress.ble_accelerator_lib.BleDefinedUUIDs;
import com.fourtress.ble_accelerator_lib.BleWrapper;
import com.fourtress.ble_accelerator_lib.BleWrapperUiCallbacks;

public class BleTest extends Activity implements OnClickListener
{
    private static final int MINIMAL_RSSI_VALUE = -999;
	
	boolean scanning = false;
	byte[] sendVal = new byte[]{0};
	byte receiveVal = 0;
	
	private BleWrapper bleWrapper = null;
	BluetoothDevice bleDevice = null;
	
    private Button scanDevicesButton, connectButton, toggleButton, clearButton;
    private TextView deviceNameText, deviceAddressText, deviceRSSIText, readDataText;
    
	@Override
	protected void onCreate( Bundle savedInstanceState ) 
	{
		super.onCreate( savedInstanceState );
		scanning = false;
		
		initUI();
		setBleCallbacks();
		
		if ( bleWrapper.checkBleHardwareAvailable() == false )
		{
			Toast.makeText(this, "Hardware not BLE compatible!", Toast.LENGTH_SHORT).show();
			finish();
		}
	}

	@Override
	protected void onResume() 
	{
		super.onResume();
		
		if ( bleWrapper.isBtEnabled() == false ) // check if Bluetooth is enabled
		{
			// Bluetooth is not enabled. Request to user to turn it on
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivity(enableBtIntent);
		}
		bleWrapper.initialize();
	}

	@Override
	protected void onPause() {
		super.onPause();
		bleWrapper.diconnect();
		bleWrapper.close();
	}

	@Override
	public void onClick(View view) 
	{
		switch( view.getId() )
		{
		case R.id.bScanDevices:
			toggleScan();
			break;
		case R.id.bConnect:
			connectToDevice();
			break;
		case R.id.bToggle:
			toggleRemoteLED();
			break;
		case R.id.bClearData:
			readIncomingData();
			break;
		default:
			break;
		}
	}
	
	private void initUI()
	{
		setContentView(R.layout.ble_test);
		
        scanDevicesButton = (Button) findViewById(R.id.bScanDevices);
        connectButton = (Button) findViewById(R.id.bConnect);
        toggleButton = (Button) findViewById(R.id.bToggle);
        clearButton = (Button) findViewById(R.id.bClearData);
        deviceNameText = (TextView) findViewById(R.id.tvDeviceName);
        deviceAddressText = (TextView) findViewById(R.id.tvDeviceAddress);
        deviceRSSIText = (TextView) findViewById(R.id.tvDeviceRSSI);
        readDataText = (TextView) findViewById(R.id.tvReadData);
        
        scanDevicesButton.setOnClickListener(this);
        connectButton.setOnClickListener(this);
        toggleButton.setOnClickListener(this);
        clearButton.setOnClickListener(this);
	}
    
	private void setBleCallbacks()
	{
		// Set the callback for the bleWrapper:
		bleWrapper = new BleWrapper( this, new BleWrapperUiCallbacks.Null()
		{
			int strongestRSSI;
			@Override
			public void uiDeviceFound( final BluetoothDevice device, final int rssi, final byte[] record )
			{
				strongestRSSI = MINIMAL_RSSI_VALUE;
	            runOnUiThread( new Runnable() 
	            {
	                @Override
	                public void run() 
	                {
	        			if( device.getName().equalsIgnoreCase( "RFduino" ) /*&& ( rssi > strongestRSSI )*/ )	// select the closest RFduino.
	        			{
	        				strongestRSSI = rssi;
	        				deviceNameText.setText( device.getName() );
	        				deviceAddressText.setText( device.getAddress() );
	        				deviceRSSIText.setText( Integer.toString( rssi ) );
	        				bleDevice = device;
	        				connectButton.setEnabled(true);
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
				receiveVal = rawValue[0];	// put the first bit in receiveVal
				for (byte b:rawValue)
				{
					Log.d("DEBUG", "Value: " + b);
				}
			}
		} );			
	}
	
	private void toggleScan()
	{
		scanning = !scanning;
		if( scanning )
		{
			scanDevicesButton.setText("Stop Scan");
			bleWrapper.startScanning();
		}
		else
		{
			scanDevicesButton.setText("Start Scan");
			bleWrapper.stopScanning();
		}
	}
    
	private void connectToDevice()
	{
		if( bleDevice.getName().equalsIgnoreCase( "RFduino" ) )
		{
			if( bleWrapper.connect( bleDevice.getAddress().toString() ) )
			{
				Log.d("DEBUG", "Connected with closest RFduino");
				toggleButton.setEnabled(true);
			}
			else
			{
				Log.d("DEBUG", "uiDeviceFound: Connection problem");
			}
		}
		else
		{
			Log.d("DEBUG", "No RFduino device found");
		}
	}

	private void toggleRemoteLED()
	{
		BluetoothGatt bleGatt = bleWrapper.getGatt();
		BluetoothGattService bleGattService = bleGatt.getService( BleDefinedUUIDs.Service.RFDUINO_UUID_SERVICE );
		BluetoothGattCharacteristic bleGattCharacteristic = bleGattService.getCharacteristic( BleDefinedUUIDs.Characteristic.RFDUINO_UUID_SEND );
        
        sendVal[0] ^= 1;	// toggle the first bit
        bleGattCharacteristic.setValue( sendVal );
        bleGattCharacteristic.setWriteType( BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE );
        bleGatt.writeCharacteristic( bleGattCharacteristic );
	}
	
	private void readIncomingDataObsolete()
	{
		BluetoothGatt bleGatt = bleWrapper.getGatt();
		BluetoothGattService bleGattService = bleGatt.getService( BleDefinedUUIDs.Service.RFDUINO_UUID_SERVICE );
		//BluetoothGattDescriptor bleGattDescriptor = bleGatt.
		BluetoothGattCharacteristic bleGattCharacteristic = bleGattService.getCharacteristic( BleDefinedUUIDs.Characteristic.RFDUINO_UUID_RECEIVE );
		BluetoothGattDescriptor bleGattDescriptor = bleGattCharacteristic.getDescriptor( BleDefinedUUIDs.Descriptor.RFDUINO_UUID_CLIENT_CONFIG );
		
		//bleGattDescriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
		bleGattDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
		
		bleWrapper.requestCharacteristicValue( bleGattCharacteristic );
		//readDataText.setText(bleGattCharacteristic.getUuid().toString());
		
		bleGatt.readCharacteristic( bleGattCharacteristic );
		//readDataText.setText( bleGattCharacteristic.getValue().toString() );
		
		bleWrapper.writeDataToCharacteristic(bleGattCharacteristic, new byte[] {0x01});
		
		//bleGattCharacteristic.setValue( new byte[]{ 0x09 } );
		//bleGatt.writeCharacteristic( bleGattCharacteristic );
	}

	private void readIncomingData()
	{
		BluetoothGatt bleGatt = bleWrapper.getGatt();
		BluetoothGattService bleService = bleGatt.getService(BleDefinedUUIDs.Service.RFDUINO_UUID_SERVICE);
		BluetoothGattCharacteristic bleCharacteristic = bleService.getCharacteristic(BleDefinedUUIDs.Characteristic.RFDUINO_UUID_RECEIVE);
		bleWrapper.requestCharacteristicValue( bleCharacteristic );
		
		readDataText.setText( Byte.toString( receiveVal )  );
	}
	
}

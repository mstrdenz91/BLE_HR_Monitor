package com.fourtress.ble_hr_monitor;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.fourtress.ble_accelerator_lib.BleFacade;

public class BleTestTest extends Activity implements OnClickListener
{
	boolean scanning = false;
	byte[] sendVal = new byte[]{0};
	byte receiveVal = 0;
	
	BleFacade bleHandler = null;
	
    private Button scanDevicesButton, connectButton, toggleButton, clearButton;
    private TextView deviceNameText, deviceAddressText, deviceRSSIText, readDataText;
    
	@Override
	protected void onCreate( Bundle savedInstanceState ) 
	{
		super.onCreate( savedInstanceState );
		
		bleHandler = new BleFacade( this );	// give this reference for UI functionality.
		
		initUI();
		
		bleHandler.checkHardwareAvailable();
	}

	@Override
	protected void onResume() 
	{
		super.onResume();
		bleHandler.enable();
	}

	@Override
	protected void onPause() {
		super.onPause();
		bleHandler.disconnect();
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
    
	public void bleDeviceFoundCallback( BluetoothDevice device, final int rssi )
	{
		deviceNameText.setText( device.getName().toString() );
		deviceAddressText.setText( device.getAddress().toString() );
		deviceRSSIText.setText( Integer.toString( rssi ) );
		connectButton.setEnabled(true);
	}
	
	public void bleDataReadCallback( byte receivedByte )
	{
		receiveVal = receivedByte;
	}
	
	private void toggleScan()
	{
		scanning = !scanning;
		if( scanning )
		{
			scanDevicesButton.setText("Stop Scan");
			bleHandler.startScan();
		}
		else
		{
			scanDevicesButton.setText("Start Scan");
			bleHandler.stopScan();
		}
	}
    
	private void connectToDevice()
	{
		boolean connected = bleHandler.connect( "RFduino" );
		if( connected )
		{
			toggleButton.setEnabled( true );
		}
	}

	private void toggleRemoteLED()
	{
		sendVal[0] ^= 1;
		bleHandler.sendRFduinoData( sendVal );
	}
	
	private void readIncomingData()
	{
		bleHandler.receiveRFduinoData();
		readDataText.setText( Byte.toString( receiveVal )  );
	}
}

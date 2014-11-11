package com.fourtress.ble_hr_monitor;

import java.util.Scanner;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;
import com.jjoe64.graphview.LineGraphView;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

public class HrGraphActivity extends AbstractBleActivity
{
	private TextView RSSILabel, RSSI, BPM;
	private LinearLayout GraphLayout;
	
	Intent enableBleIntent = new Intent( BluetoothAdapter.ACTION_REQUEST_ENABLE );
	
	GraphViewSeries series;
	GraphView graphView;
	
	int cntr = 0;
	final private int verticalMin = 40;
	final private int verticalMax = 120;
	final private int verticalInterval = ( ( verticalMax - verticalMin )/10 )+1 ;
	int maxLabel = 60;
	
	boolean freeView = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView( R.layout.hr_graph );
		
		RSSILabel	= (TextView)	findViewById( R.id.tvRSSILabel_Graph );
		RSSI 		= (TextView) 	findViewById( R.id.tvRSSI_Graph );
		BPM			= (TextView)	findViewById(R.id.tvHR_BPM);
		GraphLayout = (LinearLayout) findViewById(R.id.llGraphID);
		
		RSSI.setText( "..." );
		
		initGraph();
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate( R.menu.graph_menu, menu );
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
		if( cmd.equalsIgnoreCase( "Request_Ble_Enable" ) )
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
			//Toast.makeText( this, "RFduino Connected!", Toast.LENGTH_LONG ).show();
		}
		else if( cmd.equalsIgnoreCase( "Device_Disconnected" ) )
		{
			//Toast.makeText( this, "RFduino Disconnected!", Toast.LENGTH_LONG ).show();
		}
		else if( cmd.startsWith( "Data_Read" ) )
		{
			Scanner parse = new Scanner( cmd ).useDelimiter("[^0-9]+"); // Regular Expressions
			readData = parse.nextInt();
			drawNewVal( cntr, readData );
			cntr++;
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
	
	private void drawNewVal( int x, int y )
	{	
		BPM.setText( Integer.toString( y ) );
		graphView.removeSeries( series );
		if( y < verticalMin )
		{
			y = verticalMin;
		}
		if( y > verticalMax )
		{
			y = verticalMax;
		}
		series.appendData( new GraphViewData(x, y), true , 3600);
		graphView.addSeries( series );
	}
	
	private void initGraph()
	{
		series = new GraphViewSeries("", new GraphViewSeriesStyle(Color.rgb(255, 00, 00), 5), new GraphViewData[]{});
		
		graphView = new LineGraphView( this, "HeartRate BPM:" );
		graphView.addSeries( series );
		
		graphView.setManualYAxisBounds(verticalMax, verticalMin);
		graphView.getGraphViewStyle().setNumVerticalLabels( verticalInterval );
		graphView.getGraphViewStyle().setNumHorizontalLabels( 7 );
		
		GraphLayout.addView( graphView );
	}
}

package com.example.blescanner;

import com.example.blescanner.R;

import android.Manifest;
import android.app.Activity;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

public class DeviceScan extends ListActivity {

	private LeDeviceListAdapter mLeDeviceListAdapter;
	private BluetoothAdapter mBluetoothAdapter;
	private boolean mScanning;
	private Handler mHandler;
    private Handler mHandler1;
    private String mdeviceName;
    private int mInterval=20000;
    private int rssrange;
    private int listCount;
    private ArrayList<BluetoothDevice> mNewDevices1;
    private ArrayList<BluetoothDevice> mNewDevices2;
    private ArrayList<BluetoothDevice> mFinalDevices;
    private int totalCount=0;
    private String startTime,endTime;
	
	private static final int REQUEST_ENABLE_BT 	= 1;
	private static final long SCAN_PERIOD		= 10000;
	
	
	
	@Override
	public void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		getActionBar().setTitle( R.string.title_devices );
		mHandler = new Handler();
        mHandler1 = new Handler();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		// check whether BLE is supported
		if( !getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) ) {
			Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
			finish();
		}
		
		// init bluetooth adapter
		final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = bluetoothManager.getAdapter();
        mdeviceName = mBluetoothAdapter.getName();

        //accessing shared preferences
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String syncConnPref = sharedPref.getString("exibit_list", "");
        rssrange=-sharedPref.getInt("rssi_val",50);

        System.out.println("RSSI :"+rssrange);

        //
        mNewDevices1= new ArrayList<BluetoothDevice>();
        mNewDevices2= new ArrayList<BluetoothDevice>();
        mFinalDevices= new ArrayList<BluetoothDevice>();
        listCount=1;

		
		// check if bluetooth is supported in device
		if( mBluetoothAdapter == null ) {
			Toast.makeText( this , R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu( Menu menu ) {
		getMenuInflater().inflate( R.menu.device_scan, menu);
		if( !mScanning ) {
			menu.findItem(R.id.menu_stop).setVisible(false);
			menu.findItem(R.id.menu_scan).setVisible(true);
			menu.findItem(R.id.menu_refresh).setActionView(null);
		} else {
			menu.findItem(R.id.menu_stop).setVisible(true);
			menu.findItem(R.id.menu_scan).setVisible(false);
			menu.findItem(R.id.menu_refresh).setActionView( R.layout.actionbar_indeterminate_progress );
		}
		getMenuInflater().inflate(R.menu.menu_m, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected( MenuItem item ) {
		switch( item.getItemId() ) {
			case R.id.menu_scan:
				mLeDeviceListAdapter.clear();
				//scanLeDevice( true );
                startRepeatingTask();
				break;
			case R.id.menu_stop:
				scanLeDevice( false );
                stopRepeatingTask();
				break;
		}
		int id = item.getItemId();
		if(id == R.id.settings){

			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
		}

		if(id == R.id.exit){
			android.os.Process.killProcess(android.os.Process.myPid());
			System.exit(2);


		}
		return true;
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		// ensure bluetooth is enabled
		if( !mBluetoothAdapter.isEnabled() ) {
			if( !mBluetoothAdapter.isEnabled() ) {
				Intent enableBtIntent = new Intent( BluetoothAdapter.ACTION_REQUEST_ENABLE );
				startActivityForResult( enableBtIntent, REQUEST_ENABLE_BT );
			}
		}

        // Check Location Access
        if (ActivityCompat.checkSelfPermission(DeviceScan.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(DeviceScan.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(DeviceScan.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            //return;
        }

		// initializes list view adapter
		mLeDeviceListAdapter = new LeDeviceListAdapter();
		setListAdapter( mLeDeviceListAdapter );
		//scanLeDevice( true );
		
	}
	
	@Override
	protected void onActivityResult( int requestCode, int resultCode, Intent data ) {
		// if user chose not to enable BT
		if( (requestCode ==REQUEST_ENABLE_BT) && (resultCode==Activity.RESULT_CANCELED) ) {
			finish();
			return;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		//scanLeDevice( false );
		//mLeDeviceListAdapter.clear();
	}

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopRepeatingTask();

    }
	
	@Override
	protected void onListItemClick( ListView l, View v, int position, long id ) {
		
		final BluetoothDevice device = mLeDeviceListAdapter.getDevice(position);
		
		if( device == null ) {
			return;
		}
		
		Toast.makeText( this, "You selected device " + device.getName() , Toast.LENGTH_SHORT ).show();
		
//		final Intent intent = new Intent( this, DeviceControl.class );
//		intent.putExtra( DeviceControl.EXTRAS_DEVICE_NAME, device.getName() );
//		intent.putExtra( DeviceControl.EXTRAS_DEVICE_ADDRESS, device.getAddress() );
//		if( mScanning ) {
//			mBluetoothAdapter.stopLeScan( mLeScanCallback );
//			mScanning = false;
//		}
//		startActivity( intent );
	}
	
	private void scanLeDevice( final boolean enable ) {
		if( enable ) {
			// stops scanning after pre defined delay
			mHandler.postDelayed( new Runnable() {
				@Override
				public void run() {
					mScanning = false;
					mBluetoothAdapter.stopLeScan( mLeScanCallback );
                    System.out.println("End: "+listCount);
                    listCount++;

                    if(listCount==3){
                        //reset List Count

                        listCount=1;

                        if(!mNewDevices1.isEmpty() && !mNewDevices2.isEmpty()) {
                          // System.out.println("getin"+listCount);
                            getFinalList();

                            System.out.println(totalCount);
                            //Reset all buffers
                            mNewDevices1.clear();
                            mNewDevices2.clear();
                            totalCount=0;
                        }

                    }
					invalidateOptionsMenu();

				}
			}, SCAN_PERIOD );


			mScanning = true;
			mBluetoothAdapter.startLeScan( mLeScanCallback );
		} else {
			mScanning = false;
			mBluetoothAdapter.stopLeScan( mLeScanCallback );


		}
		invalidateOptionsMenu();

	}
	
	
	// Adapter for holding devices found while scanning
	private class LeDeviceListAdapter extends BaseAdapter {
		private ArrayList<BluetoothDevice> mLeDevices;
		// private ArrayList<beaconInfo> mBeaconInfo;
		private HashMap<String,String> rssiValue 	= new HashMap<String,String>();
		//private HashMap<String,String> rawData		= new HashMap<String,String>();
		private LayoutInflater mInflater;
		
		public LeDeviceListAdapter() {
			super();
			mLeDevices	= new ArrayList<BluetoothDevice>();
			// mBeaconInfo	= new ArrayList<beaconInfo>();
			mInflater	= DeviceScan.this.getLayoutInflater();
		}
		
		public void addDevice( BluetoothDevice device, int rssi, byte[] scanRecord ) {
			StringBuilder strRawData = new StringBuilder();
			
			// int hdrLength 	= scanRecord[0];
			// int nameLength	= scanRecord[scanRecord[0]+1];
			// int dataLength	= scanRecord[scanRecord[0]+1+scanRecord[scanRecord[0]+1]+1];
			
			// Log.d( "BTLE_SCAN", "Hdr=" + String.format( "%02X", hdrLength ) + 
			// 					" Name=" + String.format( "%02X",nameLength ) + 
			//					" Data = " + String.format( "%02X", dataLength ) );
			
			/*int packetLength = scanRecord[0] + 1 +
							   scanRecord[scanRecord[0]+1] + 1 +
							   scanRecord[scanRecord[0]+1+scanRecord[scanRecord[0]+1]+1] + 1;
			
			for( int i = 0; i < packetLength; i++ ) {
				strRawData.append( String.format("%02X ",scanRecord[i]) );
			}*/
            if(device.getName()!= null) {
                if (listCount == 1) {
                    if (rssi > rssrange) {
                        if (!mNewDevices1.contains(device)) {
                            mNewDevices1.add(device);

                        }

                    }

                }
                if (listCount == 2) {
                    if (rssi > rssrange) {
                        if (!mNewDevices2.contains(device)) {
                            mNewDevices2.add(device);

                        }

                    }

                }
                if (!mLeDevices.contains(device)) {
                    mLeDevices.add(device);
                    // mBeaconInfo.add( thisInfo );
                    rssiValue.put(device.toString(), Integer.toString(rssi));
                    //rawData.put( device.toString(), strRawData.toString() );
                } else {
                    // just update
                    //rssiValue.put( device.toString(), Integer.toString(rssi) );
                    // System.out.println(rssi);
                    //rawData.put( device.toString(), strRawData.toString() );
                }
            }
			//Log.d( "BTLE_SCAN", "Data["+device+"] : " + strRawData.toString() );
			
		}
		
		public BluetoothDevice getDevice( int position ) {
			return mLeDevices.get( position );
		}
		
		public void clear() {
			mLeDevices.clear();
		}
		
		@Override
		public int getCount() {
			return mLeDevices.size();
		}

		@Override
		public Object getItem(int position) {
			return mLeDevices.get( position );
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder;
			
			if( convertView == null ) {
				convertView 	= mInflater.inflate( R.layout.listitem_device, null );
				viewHolder		= new ViewHolder();
				viewHolder.deviceAddress = (TextView) convertView.findViewById( R.id.device_address );
				viewHolder.deviceName	 = (TextView) convertView.findViewById( R.id.device_name );
				viewHolder.rssiValue	 = (TextView) convertView.findViewById( R.id.device_rssi );
				//viewHolder.rawData		 = (TextView) convertView.findViewById( R.id.device_rawData );
				convertView.setTag( viewHolder );
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			
			BluetoothDevice device = mLeDevices.get( position );
			final String deviceName = device.getName();
			if( (deviceName != null) && (deviceName.length()>0) ) {
				viewHolder.deviceName.setText( deviceName );
			} else {
				viewHolder.deviceName.setText( R.string.unknown_device );
			}
			viewHolder.deviceAddress.setText( "MAC : " + device.getAddress() );
			viewHolder.rssiValue.setText( "RSSI : -" + (String)rssiValue.get(device.toString()) + " dBm" );
			//viewHolder.rawData.setText( "Raw : " + (String)rawData.get(device.toString()) );

			//viewHolder.rssiValue.setText(text);
			
			return convertView;
		}
		
	}
	
	// device scan callback
	private BluetoothAdapter.LeScanCallback mLeScanCallback = 
			new BluetoothAdapter.LeScanCallback() {
				
				@Override
				public void onLeScan( final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
					runOnUiThread( new Runnable() {
						@Override
						public void run() {
							mLeDeviceListAdapter.addDevice( device, rssi, scanRecord );
							mLeDeviceListAdapter.notifyDataSetChanged();
						}
					});					
				}
			};
	
	static class ViewHolder {
		TextView deviceName;
		TextView deviceAddress;
		TextView rssiValue;
		TextView rawData;
	}


    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {

                if(mScanning){
                    scanLeDevice( false );

                }
                mLeDeviceListAdapter.clear();
                scanLeDevice( true );
                System.out.println("Started: "+listCount);


            } finally {
                // 100% guarantee that this always happens, even if your update method throws an exception
                mHandler1.postDelayed(mStatusChecker, mInterval);
            }
        }
    };

    private void getFinalList() {
        for(int i=0;i<mNewDevices2.size();i++){

                if(mNewDevices1.contains(mNewDevices2.get(i))){
                    totalCount++;

                }

            }


      //  System.out.println(totalCount);
    }

    void startRepeatingTask() {
        mStatusChecker.run();
    }

    void stopRepeatingTask() {
        mHandler1.removeCallbacks(mStatusChecker);

    }
}



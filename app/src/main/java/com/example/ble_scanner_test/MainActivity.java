/*
 *
 *          AKM Low Energy Sensor Network Demo App
 *
 *          Company:        Asahi Kasei Microdevices: Semiconductor Division
 *          Developer:      Samir C. Mohammed (Applications Engineer)
 *
 */
package com.example.ble_scanner_test;




/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *                                                                                                 *
 *                                      I M P O R T S                                              *
 *                                                                                                 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import static android.bluetooth.le.ScanSettings.SCAN_MODE_LOW_LATENCY;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.graphics.Color;

import android.os.Bundle;
import android.os.Handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import android.util.Log;

import android.view.View;

import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;




/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *                                                                                                 *
 *                                          M A I N                                                *
 *                                                                                                 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
public class MainActivity extends AppCompatActivity
{




/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *                                                                                                 *
 *                                      V A R I A B L E S                                          *
 *                                                                                                 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
    private CharSequence ble_not_supported_message = "ble not supported...";
    private CharSequence ble_supported_message = "ble supported...";

    private Intent enable_bluetooth_intent;

    private boolean is_scanning;

    private Button start_scan_button = null;

    private static int start_scan_button_click_count = 0;

    private static final String sensor_1_MAC_address = "D7:A9:FA:3A:39:C1";
    private static final String sensor_2_MAC_address = "D8:A9:FA:3A:39:C1";

    private BluetoothAdapter bluetooth_adapter;
    private BluetoothLeScanner bluetooth_le_scanner;
    private BluetoothDevice ak1595_device = null;
    private BluetoothGatt bluetoothGatt;

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int COMPANY_ID = 0x004C;
    private static final int sensor_1_advertising_packet__minor_index = 21;
    private static final int sensor_1_advertising_packet__major_index = 19;
    private static final int element_at_beginning_of_list = 0;

    private ScanSettings settings = null;

    private List<ScanFilter> ble_scan_filter = null;

    ArrayList<String> sensor_1_scan_result_list = null;
    ArrayList<String> sensor_2_scan_result_list = null;

/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *                                                                                                 *
 *                                      F U N C T I O N S                                          *
 *                                                                                                 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // initialize main activity
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initialize BLE
        check_for_ble_support();
        initialize_bluetooth_adapter();
        initialize_major_and_minor_value_to_zero();
        manage_scan_button();
        initialize_scan_settings();
        initialize_scan_filter_settings();
        initialize_list_of_devices_found_by_ble_scan();

    }

    public void onResume() {
        super.onResume();
    }

    public void onPause() {
        super.onPause();
    }

    // Use this check to determine whether BLE is supported on the device
    public boolean check_for_ble_support()
    {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE))
        {
            Toast.makeText(this, ble_not_supported_message, Toast.LENGTH_SHORT).show();

            return false;
        }
        else
        {
            Toast.makeText(this, "bluetooth supported...", Toast.LENGTH_SHORT).show();
            return true;
        }
    }

    // Initializes Bluetooth adapter.
    public boolean initialize_bluetooth_adapter()
    {
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

        bluetooth_adapter = bluetoothManager.getAdapter();
        bluetooth_le_scanner = bluetooth_adapter.getBluetoothLeScanner();

        return check_if_bluetooth_adapter_was_initialized(bluetooth_adapter);
    }

    // Checks to see if Bluetooth Adapter has been initialized
    public boolean check_if_bluetooth_adapter_was_initialized(BluetoothAdapter bluetooth_adapter)
    {
        if (bluetooth_adapter.isEnabled())
        {
            Toast.makeText(this, "bluetooth adapter enabled...", Toast.LENGTH_SHORT).show();
            enable_bluetooth_intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enable_bluetooth_intent, REQUEST_ENABLE_BT);
            return true;
        }

        return false;
    }

    public void initialize_scan_settings()
    {
        ScanSettings.Builder settingBuilder = new ScanSettings.Builder();
        settingBuilder.setScanMode(SCAN_MODE_LOW_LATENCY);
        settings = settingBuilder.build();
    }

    public void initialize_scan_filter_settings()
    {
        ble_scan_filter = new ArrayList<>();

        ScanFilter scanFilter = new ScanFilter.Builder()
                .setDeviceAddress(sensor_1_MAC_address)
                .build();

        ble_scan_filter.add(scanFilter);

        ScanFilter scanFilter_2 = new ScanFilter.Builder()
                .setDeviceAddress(sensor_2_MAC_address)
                .build();

        ble_scan_filter.add(scanFilter_2);
    }

    public void initialize_list_of_devices_found_by_ble_scan()
    {
        if (null == sensor_1_scan_result_list)
        {
            sensor_1_scan_result_list = new ArrayList<>();
        }

        if (null == sensor_2_scan_result_list)
        {
            sensor_2_scan_result_list = new ArrayList<>();
        }
    }

    // start and stop scanning for BLE devices
    private void scanLeDevice(final boolean enable)
    {
        if (enable)
        {
            is_scanning = true;
            bluetooth_le_scanner.startScan(ble_scan_filter, settings, leScanCallback);
        }
        else
        {
            is_scanning = false;
            bluetooth_le_scanner.stopScan(leScanCallback);
            initialize_major_and_minor_value_to_zero();
        }
    }

    // Device scan callback.
    private ScanCallback leScanCallback =
            new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, final ScanResult result)
                {
                    Log.i("BLE", "AK1595 Found...");

                    BluetoothDevice found_device = result.getDevice();

                    if (sensor_1_MAC_address.equals(found_device.getAddress()))
                    {
                        final String sensor_1_advertising_packet_data = get_advertising_packet(result);
                        final String[] sensor_1_advertising_packet_data_array = sensor_1_advertising_packet_data.split(",");

                        if (!(null == sensor_1_advertising_packet_data))
                        {
                            sensor_1_scan_result_list.add(sensor_1_advertising_packet_data);
                        }

                        Log.i("sensor 1 data packet ", get_advertising_packet(result));

                        set_minor_value(sensor_1_advertising_packet_data_array[sensor_1_advertising_packet__minor_index],
                                found_device);

                        set_major_value(sensor_1_advertising_packet_data_array[sensor_1_advertising_packet__major_index],
                                found_device);

                        sensor_1_scan_result_list.remove(element_at_beginning_of_list);
                    }

                    if (sensor_2_MAC_address.equals(found_device.getAddress()))
                    {
                        final String sensor_2_advertising_packet_data = get_advertising_packet(result);
                        final String[] sensor_2_advertising_packet_data_array = sensor_2_advertising_packet_data.split(",");

                        if (!(null == sensor_2_advertising_packet_data))
                        {
                            sensor_2_scan_result_list.add(sensor_2_advertising_packet_data);
                        }

                        set_minor_value(sensor_2_advertising_packet_data_array[sensor_1_advertising_packet__minor_index],
                                found_device);

                        set_major_value(sensor_2_advertising_packet_data_array[sensor_1_advertising_packet__major_index],
                                found_device);

                        Log.i("sensor 2 data packet ", get_advertising_packet(result));

                        sensor_2_scan_result_list.remove(element_at_beginning_of_list);
                    }
                }

                @Override
                public void onScanFailed(int errorCode)
                {
                    super.onScanFailed(errorCode);
                    Log.i("BLE", "error");
                }
            };

    /*
        if start_scan_button_click_count is EVEN, then enable BLE scanning
        if start_scan_button_click_count is ODD, then disable BLE scanning
     */
    private void manage_scan_button()
    {
        start_scan_button = findViewById(R.id.start_scan_button);

        start_scan_button.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                if (ok_to_start_scanning())
                {
                    start_scanning_for_ble_devices(v);
                }
                else
                {
                    stop_scanning_for_ble_devices(v);
                }

                scan_button_was_pressed();
            }
        });
    }

    private boolean ok_to_start_scanning()
    {
        return (0 == (start_scan_button_click_count % 2));
    }

    private void start_scanning_for_ble_devices(View v)
    {
        if(v instanceof Button)
        {
            ((Button)v).setTextColor(Color.parseColor("#a4c639"));
        }

        scanLeDevice(true);
    }

    private void scan_button_was_pressed()
    {
        ++start_scan_button_click_count;
    }

    private void stop_scanning_for_ble_devices(View v)
    {
        if(v instanceof Button)
        {
            ((Button)v).setTextColor(Color.parseColor("#ff0000"));
        }

        scanLeDevice(false);
    }

    private String get_advertising_packet(ScanResult result)
    {
        byte[] manufacturerData = (result.getScanRecord().getManufacturerSpecificData(COMPANY_ID));

        return Arrays.toString(convert_signed_byte_array_to_unsigned_int_array(manufacturerData));
    }





    private int[] convert_signed_byte_array_to_unsigned_int_array(byte[] signed_byte_array)
    {
        int[] unsigned = new int [signed_byte_array.length];

        for (int index = 0; index <signed_byte_array.length; ++index)
        {
            unsigned[index] = signed_byte_array[index] & 0xFF;
        }

        return unsigned;
    }





    private void initialize_minor_value_to_zero()
    {
        TextView textView_sensor_1_minor = findViewById(R.id.sensor_1_minor);
        textView_sensor_1_minor.setText("DATA:");

        TextView textView_sensor_2_minor = findViewById(R.id.sensor_2_minor);
        textView_sensor_2_minor.setText("DATA:");
    }





    private void initialize_major_value_to_zero()
    {
        TextView textView_sensor_1_major = findViewById(R.id.sensor_1_major);
        textView_sensor_1_major.setText("DEVICE:");

        TextView textView_sensor_2_major = findViewById(R.id.sensor_2_major);
        textView_sensor_2_major.setText("DEVICE:");
    }





    private void initialize_major_and_minor_value_to_zero()
    {
        initialize_minor_value_to_zero();
        initialize_major_value_to_zero();
    }

    private void set_minor_value(final String set_to_this_value, BluetoothDevice device)
    {
        if (sensor_1_MAC_address.equals(device.getAddress()))
        {
            TextView textView_sensor_1_minor = findViewById(R.id.sensor_1_minor);
            textView_sensor_1_minor.setText("DATA: " + set_to_this_value);
        }
        else if (sensor_2_MAC_address.equals(device.getAddress()))
        {
            TextView textView_sensor_2_minor = findViewById(R.id.sensor_2_minor);
            textView_sensor_2_minor.setText("DATA: " + set_to_this_value);
        }
    }

    private void set_major_value(final String set_to_this_value, BluetoothDevice device)
    {
        if (sensor_1_MAC_address.equals(device.getAddress()))
        {
            TextView textView_sensor_1_major = findViewById(R.id.sensor_1_major);
            textView_sensor_1_major.setText("DEVICE: " + set_to_this_value);
        }
        else if (sensor_2_MAC_address.equals(device.getAddress()))
        {
            TextView textView_sensor_2_major = findViewById(R.id.sensor_2_major);
            textView_sensor_2_major.setText("DEVICE: " + set_to_this_value);
        }
    }
}


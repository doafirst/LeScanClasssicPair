package packt.com.androidbleserviceexplorer;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.AdvertisingSetParameters;
import android.bluetooth.le.AdvertisingSetCallback;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ListView;
import android.widget.EditText;
import android.widget.ToggleButton;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "FastPairing";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 2;
    private static final String DEVICE_INFORMATION_SERVICE_UUID = "180a";
    private static final String BATTERY_SERVICE_UUID = "180f";
    private static final String BATTERY_LEVEL_UUID = "2a19";
    private static final String SYSTEM_ID_UUID = "2a23";
    private static final String MODEL_NUMBER_STRING_UUID = "2a24";
    private static final String SERIAL_NUMBER_STRING_UUID = "2a25";
    private static final String FIRMWARE_REVISION_STRING_UUID = "2a26";
    private static final String HARDWARE_REVISION_STRING_UUID = "2a27";
    private static final String SOFTWARE_REVISION_STRING_UUID = "2a28";
    private static final String MANUFACTURER_NAME_STRING_UUID = "2a29";
    private static final String PNP_ID_UUID = "2a50";

    Button startScanningButton;
    Button stopScanningButton;
    ToggleButton toggleButton;
    ListView deviceListView;
    EditText editText1;
    EditText editText2;
    EditText editText3;

    //    The ListViews in Android are backed by adapters, which hold the data being displayed in a ListView
    //    deviceList will hold the data to be displayed in ListView
    ArrayAdapter<String> listAdapter;
    ArrayList<BluetoothDevice> deviceList;

    BluetoothManager bluetoothManager;
    BluetoothAdapter bluetoothAdapter;
    BluetoothLeScanner bluetoothLeScanner;
    BluetoothDevice device;
    BluetoothSocket socket;
    int manufacture_id = 0xFFEE;//0x1C01;
    int raw_index = 5;
    int min_rssi = -45;
    int connect_once = 0;
    int connect_index = 0;
    boolean manu_id_adv_only = false;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        deviceListView = findViewById(R.id.deviceListView);
        startScanningButton = findViewById(R.id.startScanButton);
        stopScanningButton = findViewById(R.id.stopScanButton);
        stopScanningButton.setVisibility(View.INVISIBLE);

        toggleButton = findViewById(R.id.tg1);
        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    manu_id_adv_only = buttonView.isChecked();
                } else {
                    manu_id_adv_only = buttonView.isChecked();
                }
            }
        });
        editText1 = findViewById(R.id.editText1);
        editText2 = findViewById(R.id.editText2);
        editText3 = findViewById(R.id.editText3);

        listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        deviceListView.setAdapter(listAdapter);
        deviceList = new ArrayList<>();

        startScanningButton.setOnClickListener(view -> startScanning());
        stopScanningButton.setOnClickListener(view -> stopScanning());
        initializeBluetooth();
        customize_advertising();
        deviceListView.setOnItemClickListener((adapterView, view, position, id) -> {
            /*
            stopScanning();
            BluetoothDevice device = deviceList.get(position);
            device.connectGatt(MainActivity.this, false, gattCallback);
            */
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!bluetoothAdapter.isEnabled()) {
            promptEnableBluetooth();
        }
    }

    private void customize_advertising(){
        BluetoothLeAdvertiser bluetoothLeAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
        UUID adv_uuid = UUID.randomUUID();
        byte[] data = {0x11,-0x01,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,
                0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,
                0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,
                0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,
                0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,
                0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,
                0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,
                0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,
                0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,
                0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,
                0x22,0x22,0x22,0x22,0x22,0x22,0x22,0x22,0x22,0x22,
        };
        if (bluetoothLeAdvertiser == null) {
            Log.e(TAG, "Adv create fail\n");
            return;
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            Log.i(TAG, "Android API ok " + android.os.Build.VERSION.SDK_INT);
        } else {
            Log.e(TAG, "Android API too old " + android.os.Build.VERSION.SDK_INT);
            return;
        }

        Log.i(TAG,"Max advertising data len: " + bluetoothAdapter.getLeMaximumAdvertisingDataLength());
        Log.i(TAG,"UUID: " + adv_uuid.toString());

        AdvertisingSetParameters advPara = new AdvertisingSetParameters.Builder()
                .setLegacyMode(false)
                //.setPrimaryPhy(BluetoothDevice.PHY_LE_1M)
                .setConnectable(true)
                .build();

        AdvertiseSettings advSettings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
                .setConnectable(false)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
                .build();

        AdvertiseData advData = new AdvertiseData.Builder()
                .setIncludeDeviceName(true)
                .setIncludeTxPowerLevel(false)
                .addServiceData(new ParcelUuid(adv_uuid),data)
                .build();

        bluetoothLeAdvertiser.startAdvertisingSet(advPara,advData,null,null,null,advertisingSetCallback);
        //bluetoothLeAdvertiser.startAdvertising(advSettings,advData,advertiseCallback);
    }
    AdvertisingSetCallback  advertisingSetCallback = new AdvertisingSetCallback (){

    };
    AdvertiseCallback advertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartFailure(int errorCode) {
            super.onStartFailure(errorCode);
            Log.d(TAG, "Advertising failed");
        }
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
            Log.d(TAG, "Advertising successfully started");
        }
    };

    @SuppressLint("MissingPermission")
    private void promptEnableBluetooth() {
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activityResultLauncher.launch(enableIntent);
        }
    }

    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() != MainActivity.RESULT_OK) {
                    promptEnableBluetooth();
                }
            }
    );

    private boolean hasPermission(String permissionType) {
        return ContextCompat.checkSelfPermission(this, permissionType) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        if (hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            return;
        }
        runOnUiThread(() -> {
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle("Location Permission Required");
            alertDialog.setMessage("This app needs location access to detect peripherals.");
            alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", (dialog, which) -> ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE));
            alertDialog.show();
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                requestLocationPermission();
            } else {
                startScanning();
            }
        }
    }

    public void initialPageValueCheck(){
        String st1 = editText1.getText().toString();
        if(st1.length()>0 && st1.length()<5) {
            manufacture_id = Integer.parseInt(st1,16);
            if(manufacture_id > 0xFFFF || manufacture_id < 0x00)
                manufacture_id = 0xFFEE;
        }

        String st2 = editText2.getText().toString();
        if(st2.length()>0) {
            raw_index = Integer.parseInt(st2);
            if (raw_index > 100 || raw_index < 0)
                raw_index = 5;
        }

        String st3 = editText3.getText().toString();
        if(st3.length()>0) {
            min_rssi = Integer.parseInt(st3);
            if (min_rssi > 0)
                min_rssi = 0 - min_rssi;
            if (min_rssi > -10 || min_rssi < -100)
                raw_index = -45;
        }

        Toast init_value_toast = Toast.makeText(this, "Manufacture: "+String.format("%X",manufacture_id) +"\nAddress Index: "+raw_index+"\nMinimal Rssi: "+min_rssi+"\nmanufacture id adv only: "+manu_id_adv_only, Toast.LENGTH_LONG);
        //init_value_toast.setGravity(Gravity.TOP,0,0);
        init_value_toast.show();

        toggleButton.setVisibility(View.INVISIBLE);
        editText1.setVisibility(View.INVISIBLE);
        editText2.setVisibility(View.INVISIBLE);
        editText3.setVisibility(View.INVISIBLE);
    }

    public void initializeBluetooth() {
        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
    }

    @SuppressLint("MissingPermission")
    public void startScanning() {
        if (!bluetoothAdapter.isEnabled()) {
            promptEnableBluetooth();
        }

        initialPageValueCheck();

        //    We only need location permission when we start scanning
        if (!hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            requestLocationPermission();
        } else {
            listAdapter.clear();
            deviceList.clear();
            startScanningButton.setVisibility(View.INVISIBLE);
            stopScanningButton.setVisibility(View.VISIBLE);

            List<ScanFilter> filters = new ArrayList<>();
            ScanFilter.Builder scanFilterBuilder = new ScanFilter.Builder();
            filters.add(scanFilterBuilder.build());

            ScanSettings.Builder settingsBuilder = new ScanSettings.Builder();
            settingsBuilder.setLegacy(false);

            //AsyncTask.execute(() -> bluetoothLeScanner.startScan(leScanCallBack));
            AsyncTask.execute(() -> bluetoothLeScanner.startScan(filters,settingsBuilder.build(),leScanCallBack));
        }
    }

    @SuppressLint("MissingPermission")
    public void stopScanning() {
        startScanningButton.setVisibility(View.VISIBLE);
        stopScanningButton.setVisibility(View.INVISIBLE);
        connect_once = 0;

        AsyncTask.execute(() -> bluetoothLeScanner.stopScan(leScanCallBack));
    }

    public void rfcommConnect(String bd_address) {
        Log.i(TAG,"ClassicConnect Input address >_<");
        UUID uuid_a = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
        device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(bd_address/*"13:CF:4B:99:3E:CC"*/);
        Log.e(TAG, "device: "+device);
        try {
            // Get a BluetoothSocket to connect with the given BluetoothDevice.
            // MY_UUID is the app's UUID string, also used in the server code.
            socket = device.createRfcommSocketToServiceRecord(uuid_a);
        } catch (IOException e) {
            Log.e(TAG, "Socket's create() method failed", e);
        }
        try {
            // Connect to the remote device through the socket. This call blocks
            // until it succeeds or throws an exception.
            socket.connect();
        } catch (IOException connectException) {
            // Unable to connect; close the socket and return.
            try {
                socket.close();
            } catch (IOException closeException) {
                Log.e(TAG, "Could not close the client socket", closeException);
            }
            return;
        }
    }

    public void bondA2dpConnect(String bd_address) {
        Log.i(TAG,"ClassicConnect address:"+bd_address);
        device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(bd_address);
        Log.i(TAG,"creatBond");
        device.createBond();
        Log.i(TAG,"a2dpConnect");
        a2dpConnect(device);
        return;
    }

    public void a2dpConnect(BluetoothDevice device_tmp) {
        //BluetoothDevice device = device_tmp;
        Log.e(TAG, "OpenA2dp Enter");
        BluetoothProfile.ServiceListener bs = new BluetoothProfile.ServiceListener() {
            @Override
            public void onServiceConnected(int profile, BluetoothProfile proxy) {
                Log.e(TAG, "onServiceConnected");
                if (profile == BluetoothGatt.A2DP) {
                    BluetoothA2dp a2dp = (BluetoothA2dp) proxy;
                    try {
                        a2dp.getClass().getMethod("connect", BluetoothDevice.class).invoke(a2dp, device_tmp);
                    } catch (Exception e) {
                    }
                }
            }
            @Override
            public void onServiceDisconnected(int i) {
                Log.e(TAG, "Not Connect ");
            }
        };
        bluetoothAdapter.getProfileProxy(this,bs,BluetoothProfile.A2DP);
    }

    //    The BluetoothLEScanner requires a callback function, which would be called for every device found.
    private final ScanCallback leScanCallBack = new ScanCallback() {
        @SuppressLint("MissingPermission")
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            if (result.getDevice() != null) {
                if (true) {
                    synchronized (result.getDevice()) {
                        byte[] data = result.getScanRecord().getManufacturerSpecificData(manufacture_id);
                        String str_addr = null;
                        boolean found_dev = false;
                        boolean connect_dev = false;

                        if(manu_id_adv_only == true && data == null ){
                            return;
                        }

                        if (data != null && data.length >= (6+raw_index)) {
                            str_addr = String.format("%02X:%02X:%02X:%02X:%02X:%02X",data[5+raw_index],data[4+raw_index],data[3+raw_index],data[2+raw_index],data[1+raw_index],data[0+raw_index]);
                            Log.d(TAG, "Reading size: " + data.length + " with value: " +str_addr);
                        }

                        if(str_addr != null)
                        {
                            found_dev = true;
                            if(connect_once == 0 && result.getRssi() > min_rssi) {
                                connect_dev = true;
                                connect_once = 1;
                                toastShow(result.getDevice(), str_addr);
                                //rfcommConnect(str_addr);
                                bondA2dpConnect(str_addr);
                            }
                        }

                        listShow(result,found_dev,connect_dev);

                    }
                }
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.e(TAG, "onScanFailed: code:" + errorCode);
        }
    };

    @SuppressLint("MissingPermission")
    private void toastShow(BluetoothDevice tmp_dev, String address)
    {
        Toast toast = Toast.makeText(this,"        ◎ LE Dev:"+"\n"+tmp_dev.getAddress()+"\n\n       -> BT Dev:\n" + address+"\n", Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER,0,0);
        toast.show();
    }

    @SuppressLint("MissingPermission")
    private void toastShow(int i )
    {
        Toast toast = Toast.makeText(this,"連線設備"+i, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER,0,0);
        toast.show();
    }

    //    Called by ScanCallBack function to check if the device is already present in listAdapter or not.
    @SuppressLint("MissingPermission")
    private boolean listShow(ScanResult res,boolean found_dev,boolean connect_dev) {
        BluetoothDevice device = res.getDevice();
        String itemDetails;
        int i;

        for (i = 0; i < deviceList.size(); ++i) {
            String addedDeviceDetail = deviceList.get(i).getAddress();
            if (addedDeviceDetail.equals(device.getAddress())) {
                itemDetails = String.format("%2d) ",i);
                itemDetails += device.getAddress() +" "+rssiStrengthPic(res.getRssi())+"  "+res.getRssi();
                if(found_dev) {itemDetails += " ☆★";}
                itemDetails += res.getDevice().getName() == null ? "" : "\n       "+  res.getDevice().getName();
                if(connect_dev) {
                    connect_index=i;
                    toastShow(i);
                }
                if(i==connect_index&&connect_once==1)
                {
                    itemDetails += "\n                          [CONNECT]";
                }
                Log.d(TAG,"Index:"+i+"/"+deviceList.size()+" "+itemDetails);
                listAdapter.remove(listAdapter.getItem(i));
                listAdapter.insert(itemDetails,i);
                return true;
            }
        }
        itemDetails = String.format("%2d) ",i);
        itemDetails += device.getAddress() +" "+rssiStrengthPic(res.getRssi())+"  "+res.getRssi();
        if(found_dev) {itemDetails += " ☆★";}
        itemDetails += res.getDevice().getName() == null ? "" : "\n       "+  res.getDevice().getName();
        if(connect_dev) {
            connect_index=i;
            toastShow(i);
        }
        if(i==connect_index&&connect_once==1)
        {
            itemDetails += "\n                          [CONNECT]";
        }
        listAdapter.add(itemDetails);
        deviceList.add(device);

        Log.e(TAG,"NEW:"+i+" "+itemDetails);
        return false;
    }
    private String rssiStrengthPic(int rs)
    {
        if(rs > -45)
        {
            return "▁▃▅▇";
        }
        if(rs > -62)
        {
            return "▁▃▅";
        }
        if(rs > -80)
        {
            return "▁▃";
        }
        if(rs > -95)
        {
            return "▁";
        }
        else
            return "";
    }
    //    The connectGatt method requires a BluetoothGattCallback
    //    Here the results of connection state changes and services discovery would be delivered asynchronously.
    protected BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        private volatile boolean isOnCharacteristicReadRunning = false;

        @SuppressLint("MissingPermission")
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            String address = gatt.getDevice().getAddress();

            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothGatt.STATE_CONNECTED) {
                    Log.w(TAG, "onConnectionStateChange() - Successfully connected to " + address);
                    boolean discoverServicesOk = gatt.discoverServices();
                    Log.i(TAG, "onConnectionStateChange: discovered Services: " + discoverServicesOk);
                } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                    Log.w(TAG, "onConnectionStateChange() - Successfully disconnected from " + address);
                    gatt.close();
                }
            } else {
                Log.w(TAG, "onConnectionStateChange: Error " + status + " encountered for " + address);
            }
        }

        @SuppressLint("MissingPermission")
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            final List<BluetoothGattService> services = gatt.getServices();
            runOnUiThread(() -> {
                for (int i = 0; i < services.size(); i++) {
                    BluetoothGattService service = services.get(i);
                    List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();

                    if (service.getUuid().toString().contains(DEVICE_INFORMATION_SERVICE_UUID) || service.getUuid().toString().contains(BATTERY_SERVICE_UUID)) {
                        Log.d(TAG, "onServicesDiscovered: Device Information Service Discovered");
                        StringBuilder buffer = new StringBuilder("Service Id: " + service.getUuid().toString());
                        for (int j = 0; j < characteristics.size(); j++) {
                            BluetoothGattCharacteristic characteristic = characteristics.get(j);
                            String characteristicUuid = characteristic.getUuid().toString();
                            buffer.append("\n\nCharacteristic: ");
                            buffer.append("\nUUID: ").append(characteristicUuid);
                            isOnCharacteristicReadRunning = true;
                            gatt.readCharacteristic(characteristic);
                            while (isOnCharacteristicReadRunning) {
//                                Do nothing
//                                Wait while the characteristic is being read in onCharacteristicRead function
                            }

                            if (characteristicUuid.contains(SYSTEM_ID_UUID)) {
                                buffer.append("\nSystem Id: ").append(new BigInteger(characteristic.getValue()).longValue());
                            } else if (characteristicUuid.contains(MODEL_NUMBER_STRING_UUID)) {
                                buffer.append("\nModel Number: ").append(new String(characteristic.getValue(), StandardCharsets.UTF_8));
                            } else if (characteristicUuid.contains(SERIAL_NUMBER_STRING_UUID)) {
                                buffer.append("\nSerial Number: ").append(new String(characteristic.getValue(), StandardCharsets.UTF_8));
                            } else if (characteristicUuid.contains(FIRMWARE_REVISION_STRING_UUID)) {
                                buffer.append("\nFirmware Revision: ").append(new String(characteristic.getValue(), StandardCharsets.UTF_8));
                            } else if (characteristicUuid.contains(HARDWARE_REVISION_STRING_UUID)) {
                                buffer.append("\nHardware Revision: ").append(new String(characteristic.getValue(), StandardCharsets.UTF_8));
                            } else if (characteristicUuid.contains(SOFTWARE_REVISION_STRING_UUID)) {
                                buffer.append("\nSoftware Revision: ").append(new String(characteristic.getValue(), StandardCharsets.UTF_8));
                            } else if (characteristicUuid.contains(MANUFACTURER_NAME_STRING_UUID)) {
                                buffer.append("\nManufacturer Name: ").append(new String(characteristic.getValue(), StandardCharsets.UTF_8));
                            } else if (characteristicUuid.contains(PNP_ID_UUID)) {
                                buffer.append("\nPnP Id: ").append(new BigInteger(characteristic.getValue()).longValue());
                            } else if (characteristicUuid.contains(BATTERY_LEVEL_UUID)) {
                                buffer.append("\nBattery Level: ").append(new BigInteger(characteristic.getValue()).longValue());
                            }
                        }
                        Log.d(TAG, "onServicesDiscovered: New Service: " + buffer);
                        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).setMessage(buffer.toString())
                                .setTitle("Device Information")
                                .setCancelable(false)
                                .setPositiveButton("OK", (dialog, which) -> dialog.cancel())
                                .create();
                        alertDialog.show();
                    }
                }
            });
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(TAG, "onCharacteristicRead: Read characteristic: UUID: " + characteristic.getUuid().toString());
            } else if (status == BluetoothGatt.GATT_READ_NOT_PERMITTED) {
                Log.e(TAG, "onCharacteristicRead: Read not permitted for " + characteristic.getUuid().toString());
            } else {
                Log.e(TAG, "onCharacteristicRead: Characteristic read failed for " + characteristic.getUuid().toString());
            }

            isOnCharacteristicReadRunning = false;
        }
    };
}

[TOC] 



# 1. Environment Setup



Step1. Install Android Studio :
			https://developer.android.com/studio

Step2. Install Java

Step3. Download App Code : [fastpairing_source.7z](http://psweb.sunplus.com/mtdvd/file_download.php?file_id=211681&type=bug)

Step4. Open Project :   
			開啟Android Studio -> File -> Open -> 選擇該資料夾

Step5. Build:
			Build ->Make Project 或 按下槌子圖案 

Step6. Install App: [fastpairing_v2.apk](http://psweb.sunplus.com/mtdvd/file_download.php?file_id=211683&type=bug)
			Method1 : adb install ./app/build/outputs/apk/debug/app-debug.apk
			Method2 : 將 apk 存入手機並透過安裝程式安裝

Step7. Debug Command:
			adb logcat | grep -E "FastPairing|bluetooth|avdtp|a2dp|acl"



PS. 建議將 adb tool 與 cygwin 安裝完成並設定好環境變數
		cygwin : https://www.cygwin.com/
		adb tool: https://developer.android.com/studio/releases/platform-tools?hl=zh-tw



# 2. File Path 

### 2.1 Java Code

**Path:**
	./app/src/main/java/packt/com/androidbleserviceexplorer/MainActivity.java

**Describe:** 
	所有 Java Code 皆在該檔案中




### 2.2 Manifest
**Path:**
	./app/src/main/AndroidManifest.xml

**Describe:** 
	可宣告 App 之 property，增加 BT 的 promission 

```xml
    <uses-permission android:name="android.permission.BLUETOOTH" />
    <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
    <uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
    <uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
    <uses-permission android:name="android.permission.BLUETOOTH_SCAN" />
```



### 2.3 Layout

**Path:**
	./app/src/main/res/layout/activity_main.xml

**Describe:** 
	如 EditText 輸入文字，

```xml
    <EditText
        android:id="@+id/editText2"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:ems="10"
        android:inputType="number"
        android:hint="Address Index: 0"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintHorizontal_bias="0.502"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintVertical_bias="0.25" />
```

可透過 findViewById 連結到 Java Code

```java
editText2 = findViewById(R.id.editText2);
```
# 3. Android Bluetooth  

### 3.1 Import Bluetooth Module
需 import 之 bluetooth module

```java
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
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
//or
import android.bluetooth.*;
import android.bluetooth.le.*;
```



### 3.2 Bluetooth Modules

#### 3.2.1 BluetoothAdapter

**Path**
		 https://developer.android.com/reference/android/bluetooth/BluetoothAdapter

**Initial**

```java
BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
```

**Functionality**

```Java
//Check Bluetooth Enabled
bluetoothAdapter.isEnabled()

//Get Scanner
bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
//Get Device by fixed mac address
bluetoothDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice("13:CF:4B:99:3E:CC");
//Get profile proxy 
bluetoothAdapter.getProfileProxy(this,bs,BluetoothProfile.A2DP);
```

#### 3.2.2 BluetoothLeScanner

**Path**
		https://developer.android.com/reference/android/bluetooth/le/BluetoothLeScanner

**Initial** 

```java
BluetoothLeScanner bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
```

**Functionality**

```Java
//Start LE Scan
AsyncTask.execute(() -> bluetoothLeScanner.startScan(leScanCallBack));
AsyncTask.execute(() -> bluetoothLeScanner.startScan(filters,settingsBuilder.build(),leScanCallBack));
```



#### 3.2.3 ScanResult

**Path**
		https://developer.android.com/reference/android/bluetooth/le/ScanResult

**Functionality**

```java
ScanResult result;

result.getDevice(); // Returns the remote Bluetooth device identified 
result.getRssi(); // signal strength in dBm
result.getScanRecord(); // combination of advertisement and scan response
result.getPrimaryPhy(); // Returns the primary Physical Layer on which this advertisment was received.
result.ScanRecord().getManufacturerSpecificData(int manufacturerId); // return raw data
result.ScanRecord().getServiceData(ParcelUuid serviceDataUuid);// return raw data
result.ScanRecord().toString(); //Returns a string representation
result.isLegacy(); // legacy adv or not 
result.isConnectable(); // connectable or not 
```

















#### 3.2.4 BluetoothDevice

**Path**
		https://developer.android.com/reference/android/bluetooth/BluetoothDevice

**Initial** 

```Java
BluetoothDevice bluetoothDevice;

//Get Device by fixed mac address
bluetoothDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice("13:CF:4B:99:3E:CC");

//Get Device by scan result 
void onScanResult(int callbackType, ScanResult result){ bluetoothDevice = result.getDevice();}

//Get Device by Bonded device list 
bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
```

**Functionality**

```java
//Bond device
bluetoothDevice.createBond();
```











# 4. Java Function 

### 4.1 LE Scan
**Initial**
宣告bluetoothLEScanner 並透過 bluetoothAdapter 獲取 scanner 物件。

```Java 
BluetoothLeScanner bluetoothLeScanner;

public void initializeBluetooth() {
    bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
}
```

**Scan Start**
在使用 .startScan() 需帶入 filer 及 settings 參數，不然會無法 scan 到 extended advertisement，最後一個參數則是帶入ScanCallback  回調函式。

```Java
public void startScanning() {
	List<ScanFilter> filters = new ArrayList<>();
	ScanFilter.Builder scanFilterBuilder = new ScanFilter.Builder();
	filters.add(scanFilterBuilder.build());
	
	ScanSettings.Builder settingsBuilder = new ScanSettings.Builder();
	settingsBuilder.setLegacy(false); //Disable Legacy Only 
	
	AsyncTask.execute(() -> bluetoothLeScanner.startScan(filters,settingsBuilder.build(),leScanCallBack));
}
```


**Scan Callback**
ScanResult 為 Scan 到的裝置，包含非常多資訊 如: advertisement data 、name 、rssi 、phy、sid。
    API .getScanRecord().getManufacturerSpecificData() 來獲取該 manufacture id 的 raw data 。
    API .getRssi() 來獲取 rssi 的強度 。
在 raw data 有值且 rssi 強度達標情況觸發藍牙連線 bondA2dpConnect(str_addr);

```Java 
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
                    if (data != null && data.length >= (6+raw_index)) {
                        str_addr = String.format("%02X:%02X:%02X:%02X:%02X:%02X",data[5+raw_index],data[4+raw_index],data[3+raw_index],data[2+raw_index],data[1+raw_index],data[0+raw_index]);
                    }
                    if(str_addr != null && result.getRssi() > min_rssi)
                    {
                        found_dev = true;
                        bondA2dpConnect(str_addr);//Connect to Classic device
                    }
                    listShow(result,found_dev);// UI Show Scan Result 
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
```





### 4.2 Classic Connect

**Connect Function**
兩種 connect 
    device.createBond()
    a2dpConnect(device);

```java
public void bondA2dpConnect(String bd_address) {
    device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(bd_address);
    device.createBond();
    a2dpConnect(device);
    return;
}
```


**Bonding**
未配對過的裝置使用 .createBond() 可觸發配對 

```java
    device.createBond();
```


**A2DP Connect**
已配對過的裝置可透過調用 a2dp profile service 來獲取 proxy 並觸發 a2dp connection 

```java
public void a2dpConnect(BluetoothDevice device_tmp) {
    BluetoothProfile.ServiceListener bs = new BluetoothProfile.ServiceListener() {
        @Override
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
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
            Log.e("harry_dbg", "Not Connect ");
        }
    };
    bluetoothAdapter.getProfileProxy(this,bs,BluetoothProfile.A2DP);
}
```





### 4.3 UI List Showed

**Initial**
宣告兩個 Array 分別儲存 string (UI 顯示) 與 device (藍芽裝置資訊) ，雙方 index 為一致的
    listAdapter : 為 String Array ，連結  list_item 並將字串 show 在 UI 上
    deviceList: 為 BluetoothDevice Array ，將 scan 到的 device 資訊存在該 Array 

```Java
ArrayAdapter<String> listAdapter;
ArrayList<BluetoothDevice> deviceList;

protected void onCreate(Bundle savedInstanceState) {
    listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
    deviceListView.setAdapter(listAdapter);
    deviceList = new ArrayList<>();
}
```


**UI Show  **
For 迴圈內: 在 deviceList 中有找到該 device ，替換 listAdapter 的顯示字串，達成 Rssi UI 更新
For 迴圈外: 在 deviceList 中沒找到該 device ，增加 listAdapter 及 deviceList 該裝置資訊
itemDetails : 為該欄位顯示之字串

```Java
private boolean listShow(ScanResult res,boolean found) {
    BluetoothDevice device = res.getDevice();
    String itemDetails;
    int i;
    for (i = 0; i < deviceList.size(); ++i) {
        String addedDeviceDetail = deviceList.get(i).getAddress();
        if (addedDeviceDetail.equals(device.getAddress())) {
            itemDetails = String.format("%2d) ",i); //顯示 index顯示 index
            itemDetails += device.getAddress() +" "+rssiStrengthPic(res.getRssi())+"  "+res.getRssi(); //顯示 address + rssi 
            itemDetails += res.getDevice().getName() == null ? "" : "\n       "+  res.getDevice().getName(); //顯示 name
            
            listAdapter.remove(listAdapter.getItem(i));
            listAdapter.insert(itemDetails,i);
            return true;
        }
    }
    itemDetails = String.format("%2d) ",i);//顯示 index顯示 index
    itemDetails += device.getAddress() +" "+rssiStrengthPic(res.getRssi())+"  "+res.getRssi();//顯示 address + rssi
    itemDetails += res.getDevice().getName() == null ? "" : "\n       "+  res.getDevice().getName();//顯示 name
    
    listAdapter.add(itemDetails);
    deviceList.add(device);
    return false;
}
```
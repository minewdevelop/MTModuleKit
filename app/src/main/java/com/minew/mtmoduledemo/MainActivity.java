package com.minew.mtmoduledemo;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.minew.modulekit.MTModule;
import com.minew.modulekit.MTModuleManager;
import com.minew.modulekit.enums.BluetoothState;
import com.minew.modulekit.enums.ConnectionState;
import com.minew.modulekit.interfaces.ModuleChangeConnection;
import com.minew.modulekit.interfaces.ScanMTModuleCallback;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final int PERMISSION_COARSE_LOCATION = 122;
    private final int REQUEST_ENABLE_BT = 123;
    private RecyclerView mRecyclerView;
    private RecycleAdapter mAdapter;
    private TextView mStart_scan;
    private EditText mFilterEdit;
    private Button mFilterBtn;
    private ProgressDialog progressDialog;
    private MTModuleManager mtModuleManager;
    private boolean isScanning;
    private String mFilterText="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!ensureBleExists())
            finish();
        if (!isBLEEnabled()) {
            showBLEDialog();
        }

        initView();
        initManager();
        initPermission();

        initListener();
    }


    private void initView() {

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mStart_scan = findViewById(R.id.start_scan);


        mRecyclerView = findViewById(R.id.recycle);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter = new RecycleAdapter();
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(new RecycleViewDivider(this, LinearLayoutManager
                .HORIZONTAL));

        mFilterBtn = findViewById(R.id.filter_btn);
        mFilterEdit = findViewById(R.id.filter_edit);


        dialogshow();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initManager();
    }

    @Override
    protected void onStart() {
        super.onStart();
        initManager();
    }

    private void initManager() {
        mtModuleManager = MTModuleManager.getInstance(this);

        MTModuleManager.getInstance(this).setModuleChangeConnection(new ModuleChangeConnection() {
            @Override
            public void onDeviceChangeStatus(final MTModule device, ConnectionState status) {
                if (progressDialog!=null) {
                    progressDialog.dismiss();
                }
                switch (status) {
                    case DeviceLinkStatus_Connected:
                        MTOperate.getInstance().setMtModule(device);

                        Intent intent = new Intent(MainActivity.this, ModuleActivity.class);
                        startActivity(intent);

                        break;
                    case DeviceLinkStatus_ConnectFailed:
                    case DeviceLinkStatus_Disconnect:
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "Connect fail!", Toast.LENGTH_SHORT).show();
                            }
                        });
                        break;
                }
            }
        });

        BluetoothState bluetoothState = MTModuleManager.getInstance(this).checkBluetoothState();
        switch (bluetoothState) {
            case BluetoothStateNotSupported:
                break;
            case BluetoothStatePowerOff:
                break;
            case BluetoothStatePowerOn:
//                mtModuleManager.startScan(scanMTModuleCallback);
                break;
        }
        mFilterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isScanning){
                    isScanning = false;
                    mtModuleManager.stopScan();
                }
            }
        });
    }

    ScanMTModuleCallback scanMTModuleCallback = new ScanMTModuleCallback() {
        @Override
        public void onScannedMTModule(LinkedList<MTModule> linkedList) {
            List<MTModule> list = new LinkedList<>();

            for (MTModule mtModule : linkedList) {
                if (mtModule.getMacAddress().contains(mFilterText)) {
                    list.add(mtModule);
                }
            }
//            list.addAll(linkedList);
            mAdapter.setData(list);
        }
    };

    private void initListener() {
        mAdapter.setOnItemClickListener(new RecycleAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                progressDialog.setMessage(getString(R.string.connecting)
                        + mAdapter.getData(position).getName());
                progressDialog.show();
                MTModule mtModule = mAdapter.getData(position);

                mtModuleManager.connect(mtModule);

            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        });
        mStart_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                scan();
            }
        });


    }

    private void scan() {
        if (isScanning) {
            isScanning = false;
            mStart_scan.setText("Start");
            if (mtModuleManager != null) {
                mtModuleManager.stopScan();
            }
        } else {
            isScanning = true;
            mStart_scan.setText("Stop");
            try {
                mtModuleManager.startScan(scanMTModuleCallback);
//                if (Build.VERSION.SDK_INT>Build.VERSION_CODES.LOLLIPOP) {
//                    ScanSettings scanSettings = new ScanSettings.Builder()
//                            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
//                            .setReportDelay(0)
//                            .build();
//                    ArrayList<ScanFilter> list = new ArrayList<>();
//                    list.add(new ScanFilter.Builder()
//                            .setServiceUuid(ParcelUuid.fromString("0000ffe1-0000-1000-8000-00805f9b34fb"))
//                            .build());
//                    mtModuleManager.startScan(scanMTModuleCallback, list, scanSettings);
//                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private void initPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_COARSE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_COARSE_LOCATION:
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mtModuleManager.startScan(scanMTModuleCallback);
                    }
                }, 1000);

                break;
        }
    }

    protected void dialogshow() {
        progressDialog = new ProgressDialog(MainActivity.this);

        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage("Connecting...");
    }


    private boolean ensureBleExists() {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "Phone does not support BLE", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    protected boolean isBLEEnabled() {
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        final BluetoothAdapter adapter = bluetoothManager.getAdapter();
        return adapter != null && adapter.isEnabled();
    }

    private void showBLEDialog() {
        final Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
    }
}

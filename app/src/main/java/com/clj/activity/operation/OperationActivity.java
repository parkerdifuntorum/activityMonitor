package com.clj.activity.operation;


import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import android.view.KeyEvent;
import android.view.View;

import com.clj.activity.R;
import com.clj.activity.comm.Observer;
import com.clj.activity.comm.ObserverManager;
import com.clj.fastble.BleManager;
import com.clj.fastble.data.BleDevice;

import java.util.ArrayList;
import java.util.List;
class ItemViewModel extends ViewModel {
    private MutableLiveData<Object> selectedItem = new MutableLiveData<Object>();
    public void selectItem(Object item) {
        //selectedItem.setValue(item);
        selectedItem = (MutableLiveData<Object>) item;
    }
    public MutableLiveData<Object> getSelectedItem() {
        return selectedItem;
    }
}

public class OperationActivity extends AppCompatActivity implements Observer {
    public Context context;
    private ItemViewModel model;

    public static final String KEY_DATA = "key_data";

    public BleDevice bleDevice;
    private BluetoothGattService bluetoothGattService;
    private BluetoothGattCharacteristic characteristic;
    private int charaProp;

    private Toolbar toolbar;
    private List<Fragment> fragments = new ArrayList<>();
    private int currentPage = 0;
    private String[] titles = new String[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operation);

        //model = new ViewModelProvider(this).get(ItemViewModel.class);
//        model = ViewModelProviders.of(this).get(ItemViewModel.class);


        System.out.println("Initializing data in operation activity");
        initData();
        System.out.println(bleDevice);
        initView();
        initPage();

        ObserverManager.getInstance().addObserver(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BleManager.getInstance().clearCharacterCallback(bleDevice);
        ObserverManager.getInstance().deleteObserver(this);
    }

    @Override
    public void disConnected(BleDevice device) {
        if (device != null && bleDevice != null && device.getKey().equals(bleDevice.getKey())) {
            finish();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (currentPage != 0) {
                currentPage--;
                changePage(currentPage);
                return true;
            } else {
                finish();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void initView() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(titles[0]);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentPage != 0) {
                    currentPage--;
                    changePage(currentPage);
                } else {
                    finish();
                }
            }
        });
    }

    private void initData() {
        bleDevice = getIntent().getParcelableExtra(KEY_DATA);
        //model = new ViewModelProvider(context).get(ItemViewModel.class);
//        model.selectItem(bleDevice);
        if (bleDevice == null)
            finish();

        //we might need this in the main activity
        titles = new String[]{
                getString(R.string.service_list),
                getString(R.string.characteristic_list),
                //"Metabolic Activity Monitor"};
                getString(R.string.console)};
                //String.format("Graph Data");
        System.out.println("Printing titles");
        System.out.println(bleDevice);
        System.out.println(titles);
    }

    private void initPage() {
        prepareFragment();
        changePage(0);
    }

    public void changePage(int page) {
        currentPage = page;
        toolbar.setTitle("Metabolic Care System");
        updateFragment(page);
        if (currentPage == 1) {
            System.out.println("Changing pages for the first time");
            BleDevice bleDevice = getBleDevice();
            System.out.println(bleDevice);
            System.out.println(bluetoothGattService);
            System.out.println(characteristic);
            ((CharacteristicListFragment) fragments.get(1)).showData();

        } else if (currentPage == 2) {
            ((CharacteristicOperationFragment) fragments.get(2)).showData();
        }
    }

    private void prepareFragment() {
        fragments.add(new ServiceListFragment());
        fragments.add(new CharacteristicListFragment());
        fragments.add(new CharacteristicOperationFragment());
        for (Fragment fragment : fragments) {
            getSupportFragmentManager().beginTransaction().add(R.id.fragment, fragment).hide(fragment).commit();
        }
    }

    private void updateFragment(int position) {
        if (position > fragments.size() - 1) {
            System.out.println("The fragment is outside of the size");
            return;
        }
        for (int i = 0; i < fragments.size(); i++) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            Fragment fragment = fragments.get(i);
            if (i == position) {
                System.out.println("Showing transaction fragment");
                transaction.show(fragment);
            } else {
                System.out.println("Hiding transaction fragment");
                transaction.hide(fragment);
            }
            System.out.println("The transaction fragment position is: ");
            System.out.println(position);
            transaction.setReorderingAllowed(true);
            transaction.commit();
        }
    }

    public BleDevice getBleDevice() {
        System.out.println("printing ble device in operation activity");
        System.out.println(bleDevice);
        return bleDevice;
    }

    public BluetoothGattService getBluetoothGattService() {
        return bluetoothGattService;
    }

    public void setBluetoothGattService(BluetoothGattService bluetoothGattService) {
        this.bluetoothGattService = bluetoothGattService;
    }

    public BluetoothGattCharacteristic getCharacteristic() {
        return characteristic;
    }

    public void setCharacteristic(BluetoothGattCharacteristic characteristic) {
        this.characteristic = characteristic;
    }

    public int getCharaProp() {
        return charaProp;
    }

    public void setCharaProp(int charaProp) {
        this.charaProp = charaProp;
    }


}

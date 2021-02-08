package com.clj.activity.operation;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.clj.activity.MainActivity;
import com.clj.activity.R;
import com.clj.fastble.BleManager;
import com.clj.fastble.data.BleDevice;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)

public class ServiceListFragment extends Fragment {
    //public int position = 2;

    private TextView txt_name, txt_mac;
    private ResultAdapter mResultAdapter;
    private ItemViewModel model;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_service_list, null);
        model = new ViewModelProvider(requireActivity()).get(ItemViewModel.class);
        initView(v);
        showData();
        return v;

    }

    private void initView(View v) {
        txt_name = (TextView) v.findViewById(R.id.txt_name);
        txt_mac = (TextView) v.findViewById(R.id.txt_mac);

        mResultAdapter = new ResultAdapter(getActivity());

        /*        BleDevice bleDevice = ((OperationActivity) getActivity()).getBleDevice();
        String name = bleDevice.getName();
        String mac = bleDevice.getMac();
        BluetoothGatt gatt = BleManager.getInstance().getBluetoothGatt(bleDevice);
        System.out.println("the gatt namesky");
        System.out.println(name);
        System.out.println(gatt);
//        System.out.println(gatt.getService(UUID.fromString("4fafc201-1fb5-459e-8fcc-c5c9c331914b")));
        //BluetoothGattService service1 = gatt.getService(UUID.fromString("4fafc201-1fb5-459e-8fcc-c5c9c331914b"));
        //System.out.println(service1);



        txt_name.setText(String.valueOf(getActivity().getString(R.string.name) + name));
        txt_mac.setText(String.valueOf(getActivity().getString(R.string.mac) + mac));


        System.out.println("Device adapter stuffs");
        System.out.println(mResultAdapter);
        //mResultAdapter.clear();
        int i = 0;
        for (BluetoothGattService service : gatt.getServices()) {

            System.out.println("printing servs");
            System.out.println(service);
            mResultAdapter.addResult(service);

            i = i+1;
        }
        //ArrayList<> list_device = (ArrayList) v.findViewById(R.id.list_service);
        */

        ListView listView_device = (ListView) v.findViewById(R.id.list_service);


        listView_device.setAdapter(mResultAdapter);

        //listView_device.setAdapter(mResultAdapter);
        System.out.println("ListView adapter stuffs");
        System.out.println(listView_device);
        //listView_device.setSelection(2);
        //System.out.println(listView_device);

/*        System.out.println("the namesky");
        System.out.println(name);
        System.out.println(gatt);
        BluetoothGattService service1 = gatt.getService(UUID.fromString("4fafc201-1fb5-459e-8fcc-c5c9c331914b"));
        System.out.println(service1);

        int position = 2;
        System.out.println("here's the position of position");
        System.out.println(position);
        BluetoothGattService service = mResultAdapter.getItem(position);
        System.out.println(service);
        ((OperationActivity) getActivity()).setBluetoothGattService(service1);
        mResultAdapter.notifyDataSetChanged();
        listView_device.setSelection(2);
        listView_device.deferNotifyDataSetChanged();
        ((OperationActivity) getActivity()).changePage(1);

*/



//        System.out.println(gatt.getService(UUID.fromString("4fafc201-1fb5-459e-8fcc-c5c9c331914b")));
        //BluetoothGattService service1 = gatt.getService(UUID.fromString("4fafc201-1fb5-459e-8fcc-c5c9c331914b"));
        //System.out.println(service1);







        //listView_device.equals(service);
        //((OperationActivity) getActivity()).changePage(1);
        /*listView_device.setAdapter(mResultAdapter);
        BluetoothGattService service = mResultAdapter.getItem(2);
        listView_device.setSelection(2);
        System.out.println(service);
        ((OperationActivity) getActivity()).setBluetoothGattService(service);
*/


        listView_device.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //position = 2;
                System.out.println("here's the position of position");
                System.out.println(position);
                BluetoothGattService service = mResultAdapter.getItem(position);
                System.out.println(service);
                ((OperationActivity) getActivity()).setBluetoothGattService(service);
                ((OperationActivity) getActivity()).changePage(1);
            }

        });
    }

    private void showData() {
        //mResultAdapter = new ResultAdapter(getActivity());
        BleDevice bleDevice = ((OperationActivity) getActivity()).getBleDevice();
        MutableLiveData<Object> bleDevice_temp = model.getSelectedItem();
        //BleDevice bleDevice = bleDevice_temp;

        String name = bleDevice.getName();
        String mac = bleDevice.getMac();
        BluetoothGatt gatt = BleManager.getInstance().getBluetoothGatt(bleDevice);
        System.out.println("the namesky");
        System.out.println(bleDevice.getName());
        System.out.println(gatt);
//        System.out.println(gatt.getService(UUID.fromString("4fafc201-1fb5-459e-8fcc-c5c9c331914b")));
        //BluetoothGattService service1 = gatt.getService(UUID.fromString("4fafc201-1fb5-459e-8fcc-c5c9c331914b"));
        //System.out.println(service1);



        txt_name.setText(String.valueOf(getActivity().getString(R.string.name) + name));
        txt_mac.setText(String.valueOf(getActivity().getString(R.string.mac) + mac));



        //mResultAdapter.clear();
        int i = 0;
        for (BluetoothGattService service : gatt.getServices()) {

            System.out.println("printing servs");
            System.out.println(service);
            mResultAdapter.addResult(service);

            i = i+1;
        }

        System.out.println("counting services");
        /*System.out.println(mResultAdapter.getCount());
        BluetoothGattService service = mResultAdapter.getItem(2);
        mResultAdapter.equals(service);
        System.out.println(service);


        System.out.println("here's the position of position");
        //System.out.println(position);
        //BluetoothGattService service = mResultAdapter.getItem(position);
        System.out.println(service);*/
        //mResultAdapter.addResult(service);
        mResultAdapter.notifyDataSetChanged();
        //((OperationActivity) getActivity()).setBluetoothGattService(service);
        //((OperationActivity) getActivity()).changePage(1);
        //Intent intent = new Intent(MainActivity.this, OperationActivity.class);
        //intent.putExtra(OperationActivity.KEY_DATA, bleDevice);

//        ((OperationActivity) getActivity()).changePage(1);

    }

    private class ResultAdapter extends BaseAdapter {

        private Context context;
        private List<BluetoothGattService> bluetoothGattServices;
        private List<BluetoothGattCharacteristic> characteristicList;

        ResultAdapter(Context context) {
            this.context = context;
            bluetoothGattServices = new ArrayList<>();
            characteristicList = new ArrayList<>();
        }
        void addResultC(BluetoothGattCharacteristic characteristic) {

            characteristicList.add(characteristic);
        }
    void addResult(BluetoothGattService service) {
        System.out.println("THis is the servic");
        System.out.println(service.toString());
        bluetoothGattServices.add(service);
    }

    void clear() {
        bluetoothGattServices.clear();
    }

    @Override
    public int getCount() {
        return bluetoothGattServices.size();
    }

    @Override
    public BluetoothGattService getItem(int position) {
        if (position > bluetoothGattServices.size()){
            System.out.println("the size is too big dummy");
            return null;}
        return bluetoothGattServices.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView != null) {
            holder = (ViewHolder) convertView.getTag();
        } else {
            convertView = View.inflate(context, R.layout.adapter_service, null);
            holder = new ViewHolder();
            convertView.setTag(holder);
            holder.txt_title = (TextView) convertView.findViewById(R.id.txt_title);
            holder.txt_uuid = (TextView) convertView.findViewById(R.id.txt_uuid);
            holder.txt_type = (TextView) convertView.findViewById(R.id.txt_type);
        }

        BluetoothGattService service = bluetoothGattServices.get(0);
        String uuid = service.getUuid().toString();
        //String uuid = "4fafc2011fb549e8fccc5c9c331914b";

        holder.txt_title.setText(String.valueOf(getActivity().getString(R.string.service) + "(" + position + ")"));
        holder.txt_uuid.setText(uuid);
        holder.txt_type.setText(getActivity().getString(R.string.type));
        return convertView;
    }

    class ViewHolder {
        TextView txt_title;
        TextView txt_uuid;
        TextView txt_type;
    }
}

}

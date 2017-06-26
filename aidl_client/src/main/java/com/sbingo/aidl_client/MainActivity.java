package com.sbingo.aidl_client;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.sbingo.ipc_sample.AIDL.IRemoteService;

public class MainActivity extends AppCompatActivity {

    boolean mIsBound;

    TextView mCallbackText;
    EditText num1;
    EditText num2;

    IRemoteService mService;

    private View.OnClickListener mBindListener = new View.OnClickListener() {
        public void onClick(View v) {
            doBindService();
        }
    };
    private View.OnClickListener mUnbindListener = new View.OnClickListener() {
        public void onClick(View v) {
            doUnbindService();
        }
    };
    private View.OnClickListener mAddListener = new View.OnClickListener() {
        public void onClick(View v) {
            tryToAdd();
        }
    };
    private View.OnClickListener mPidListener = new View.OnClickListener() {
        public void onClick(View v) {
            getPid();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button button = (Button) findViewById(R.id.bind);
        button.setOnClickListener(mBindListener);
        button = (Button) findViewById(R.id.unbind);
        button.setOnClickListener(mUnbindListener);
        num1 = (EditText) findViewById(R.id.num1);
        num2 = (EditText) findViewById(R.id.num2);
        findViewById(R.id.add).setOnClickListener(mAddListener);
        findViewById(R.id.pid).setOnClickListener(mPidListener);
        mCallbackText = (TextView) findViewById(R.id.callback);
        mCallbackText.setText("Not attached.");
    }

    void doBindService() {
        if (!mIsBound) {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.sbingo.ipc_sample", "com.sbingo.ipc_sample.AIDL.RemoteService"));
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
            mIsBound = true;
            mCallbackText.setText("Binding……");
        }
    }

    void doUnbindService() {
        if (mIsBound) {
            unbindService(mConnection);
            mIsBound = false;
            mCallbackText.setText("Unbinding.");
        }
    }

    private void tryToAdd() {
        if (!mIsBound) {
            Toast.makeText(MainActivity.this, "请先绑定服务端", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(num1.getText().toString()) ||
                TextUtils.isEmpty(num2.getText().toString())) {
            Toast.makeText(MainActivity.this, "请输入以上两个数字", Toast.LENGTH_SHORT).show();
        } else {
            try {
                int r = mService.add(Integer.valueOf(num1.getText().toString()), Integer.valueOf(num2.getText().toString()));
                mCallbackText.setText("相加结果：" + r);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private void getPid() {
        if (!mIsBound) {
            Toast.makeText(MainActivity.this, "请先绑定服务端", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            int r = mService.getPid();
            mCallbackText.setText("服务端Pid：" + r);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = IRemoteService.Stub.asInterface(service);
            mCallbackText.setText("已连接服务端");
            mIsBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mCallbackText.setText("Not attached.");
            mIsBound = false;
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        doUnbindService();
    }
}

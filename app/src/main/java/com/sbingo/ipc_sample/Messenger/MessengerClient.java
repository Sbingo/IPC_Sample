package com.sbingo.ipc_sample.Messenger;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.sbingo.ipc_sample.R;

/**
 * Author: Sbingo
 * Date:   2017/6/23
 */

public class MessengerClient extends Activity {
    /**
     * Messenger for communicating with service.
     */
    Messenger mService = null;
    /**
     * Flag indicating whether we have called bind on the service.
     */
    boolean mIsBound;
    /**
     * Some text view we are using to show state information.
     */
    TextView mCallbackText;
    EditText num1;
    EditText num2;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.messenger_client);
        // Watch for button clicks.
        Button button = (Button) findViewById(R.id.bind);
        button.setOnClickListener(mBindListener);
        button = (Button) findViewById(R.id.unbind);
        button.setOnClickListener(mUnbindListener);
        num1 = (EditText) findViewById(R.id.num1);
        num2 = (EditText) findViewById(R.id.num2);
        findViewById(R.id.add).setOnClickListener(mAddListener);
        mCallbackText = (TextView) findViewById(R.id.callback);
        mCallbackText.setText("Not attached.");
    }

    /**
     * Handler of incoming messages from service.
     */
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MessengerService.MSG_SET_VALUE:
                    mCallbackText.setText("Received from service: " + msg.arg1);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    final Messenger mMessenger = new Messenger(new IncomingHandler());

    /**
     * Class for interacting with the main interface of the service.
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  We are communicating with our
            // service through an IDL interface, so get a client-side
            // representation of that from the raw service object.
            mService = new Messenger(service);
            mCallbackText.setText("Attached.");
            // We want to monitor the service for as long as we are
            // connected to it.
            try {
                Message msg = Message.obtain(null,
                        MessengerService.MSG_REGISTER_CLIENT);
                msg.replyTo = mMessenger;
                mService.send(msg);

                // Give it some value as an example.
                msg = Message.obtain(null,
                        MessengerService.MSG_SET_VALUE, this.hashCode(), 0);
                mService.send(msg);
            } catch (RemoteException e) {
                // In this case the service has crashed before we could even
                // do anything with it; we can count on soon being
                // disconnected (and then reconnected if it can be restarted)
                // so there is no need to do anything here.
            }

            // As part of the sample, tell the user what happened.
            Toast.makeText(MessengerClient.this, "已连接服务端", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mService = null;
            mCallbackText.setText("Disconnected.");
            // As part of the sample, tell the user what happened.
            Toast.makeText(MessengerClient.this, "连接断开", Toast.LENGTH_SHORT).show();
        }
    };

    void doBindService() {
        // Establish a connection with the service.  We use an explicit
        // class name because there is no reason to be able to let other
        // applications replace our component.
        if (!mIsBound) {
            bindService(new Intent(MessengerClient.this, MessengerService.class), mConnection, Context.BIND_AUTO_CREATE);
            mIsBound = true;
            mCallbackText.setText("Binding……");
        }
    }

    void doUnbindService() {
        if (mIsBound) {
            // If we have received the service, and hence registered with
            // it, then now is the time to unregister.
            if (mService != null) {
                try {
                    Message msg = Message.obtain(null,
                            MessengerService.MSG_UNREGISTER_CLIENT);
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                } catch (RemoteException e) {
                    // There is nothing special we need to do if the service
                    // has crashed.
                }
            }

            // Detach our existing connection.
            unbindService(mConnection);
            mIsBound = false;
            mCallbackText.setText("Unbinding.");
        }
    }

    private void tryToAdd() {
        if (!mIsBound) {
            Toast.makeText(MessengerClient.this, "请先绑定服务端", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(num1.getText().toString()) ||
                TextUtils.isEmpty(num2.getText().toString())) {
            Toast.makeText(MessengerClient.this, "请输入以上两个数字", Toast.LENGTH_SHORT).show();
        } else {
            Message msg = Message.obtain(null,
                    MessengerService.MSG_ADD,
                    Integer.valueOf(num1.getText().toString()),
                    Integer.valueOf(num2.getText().toString()));
            msg.replyTo = mMessenger;
            try {
                mService.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        doUnbindService();
    }
}
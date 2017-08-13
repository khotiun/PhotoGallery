package com.bignerdranch.android.photogallery;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by hotun on 10.08.2017.
 */
//класс для определения просматривает ли пользователь приложение в данный момент если да то нужно отменить уведомление
public abstract class VisibleFragment extends Fragment {
    private static final String TAG = "VisibleFragment";

    //запустил приемник
    @Override
    public void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter(PollService.ACTION_SHOW_NOTIFICATION);//идентично если бы обьявили в манифесте
        getActivity().registerReceiver(mOnShowNotification, filter, PollService.PERM_PRIVATE, null);//регестрация приемника
    }

    //деактивировал приемник
    @Override
    public void onStop() {
        super.onStop();
        getActivity().unregisterReceiver(mOnShowNotification);//деинициализации приемника
    }

    private BroadcastReceiver mOnShowNotification = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Получение означает, что пользователь видит приложение,
            // поэтому оповещение отменяется
            Log.i(TAG, "canceling notification");
            setResultCode(Activity.RESULT_CANCELED);
        }
    };
}

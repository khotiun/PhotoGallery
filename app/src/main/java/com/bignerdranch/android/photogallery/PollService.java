package com.bignerdranch.android.photogallery;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.SystemClock;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;

import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import java.util.List;

/**
 * Created by hotun on 01.08.2017.
 */

public class PollService extends IntentService {
    private static final String TAG = "PollService";

    private static final long POLL_INTERVAL =
            AlarmManager.INTERVAL_FIFTEEN_MINUTES; //интервал

    public static Intent newIntent(Context context) {
        return new Intent(context, PollService.class);
    }
    //метод включает и отключает сигнал за вас
    public static void setServiceAlarm(Context context, boolean isOn) {
        //PendingIntent запускает  PollService
        //Задача решается вызовом метода PendingIntent.getService(…), в котором упаковывается вызов Context.startService(Intent).
        // Метод получает четыре параметра: Context для отправки интента; код запроса, по которому этот объект PendingIntent отличается от других;
        // отправляемый объект Intent и, наконец, набор флагов, управляющий процессом создания PendingIntent
        Intent i = PollService.newIntent(context);
        PendingIntent pi = PendingIntent.getService(context, 0, i ,0);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (isOn) {
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,//AlarmManager.ELAPSED_REALTIME - задает начальное время запуска относительно прошедшего реального времени: SystemClock.elapsedRealtime()
                    //В результате сигнал срабатывает по истечении заданного промежутка времени
                    SystemClock.elapsedRealtime(), POLL_INTERVAL, pi);
        } else {
            alarmManager.cancel(pi);
            pi.cancel();
        }
    }
    //Так как PendingIntent также удаляется при отмене сигнала, вы можете проверить, существует ли PendingIntent, чтобы узнать, активен сигнал или нет
    public static boolean isServiceAlarmOn(Context context){
        Intent i = PollService.newIntent(context);
        PendingIntent pi = PendingIntent.getService(context, 0, i, PendingIntent.FLAG_NO_CREATE);
        return pi != null;
    }

    public PollService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if(!isNetworkAvailableAndConnected()){
            return;
        }
        String query = QueryPreferences.getStoredQuery(this);//слово поиска
        String lastResultId = QueryPreferences.getLastResultId(this);//последний результат запроса
        List<GalleryItem> items;
        //если поиска был
        if (query == null) {
            items = new FlickrFetchr().fetchRecentPhotos();
        } else {
            items = new FlickrFetchr().searchPhotos(query);
        }

        if (items.size() == 0) {
            return;
        }

        String resultId = items.get(0).getId();
        if(resultId.equals(lastResultId)) {
            Log.i(TAG, "Got an old result: " + resultId);
        } else {
            Log.i(TAG, "Got a new result: " + resultId);//оявился новый резулбтат(новая картинка)

            Resources resources = getResources();
            Intent i = PhotoGalleryActivity.newIntent(this);
            PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);
            //создается уведомление
            Notification notification = new NotificationCompat.Builder(this)
                    .setTicker(resources.getString(R.string.new_pictures_title))//текст бегущей строки
                    .setSmallIcon(android.R.drawable.ic_menu_report_image)//значек
                    .setContentTitle(resources.getString(R.string.new_pictures_title))//задается заголовок уведомления
                    .setContentText(resources.getString(R.string.new_pictures_text))//задается текст уведомления
                    .setContentIntent(pi)//что происходит при нажатии на оповещение
                    .setAutoCancel(true)//оповещение при нажатии также будет удаляться с выдвижной панели оповещений
                    .build();
            //
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.notify(0, notification);//Передаваемый целочисленный параметр содержит идентификатор оповещения, уникальный в границах приложения.
            // Если вы отправите второе оповещение с тем же идентификатором, оно заменит последнее оповещение, отправленное с этим идентификатором


        }

        QueryPreferences.setLastResultId(this, resultId);

    }
    //Логика проверки доступности сети
    private boolean isNetworkAvailableAndConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        //если включен запрет использования фоновых служб, тогда getActiveNetworkInfo() вернет null
        boolean isNetworkAvailable = cm.getActiveNetworkInfo() != null;
        //код проверяет наличие полноценного сетевого подключения
        boolean isNetworkConnected = isNetworkAvailable &&
                cm.getActiveNetworkInfo().isConnected();
        return isNetworkConnected;
    }
}

package com.bignerdranch.android.photogallery;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static android.os.Build.VERSION_CODES.M;

/**
 * Created by hotun on 28.07.2017.
 */

public class ThumbnailDownloader<T> extends HandlerThread {
    private static final String TAG = "ThumbnailDownloader";
    private static final int MESSAGE_DOWNLOAD = 0;

    private Handler mRequestHandler;
    //ConcurrentHashMap — разновидность HashMap, безопасную по отношению к потокам
    private ConcurrentMap<T, String> mRequestMap = new ConcurrentHashMap<>();
    private Handler mResponseHandler;
    private ThumbnailDownloadListener<T> mThumbnailDownloadListener;

    public interface ThumbnailDownloadListener<T> {
        //Метод onThumbnailDownloaded(…), определенный в новом интерфейсе ThumbnailDownloadListener,
        // будет вызван через некоторое время, когда изображение было полностью загружено и готово к добавлению
        // в пользовательский интерфейс.
        void onThumbnailDownloaded(T target, Bitmap thumbnail);
    }

    public void setThumbnailDownloadListener
            (ThumbnailDownloadListener<T> listener) {
        mThumbnailDownloadListener = listener;
    }

    public ThumbnailDownloader(Handler responseHandler) {
        super(TAG);
        mResponseHandler = responseHandler;
    }

    //метод для обновления mRequestMap и постановки нового сообщения в очередь сообщений фонового потока.
    public void queueThumbnail(T target, String url) {
        Log.i(TAG, "Got a URL: " + url);

        if (url == null) {
            mRequestMap.remove(target);
        } else {
            mRequestMap.put(target, url);
            //сообщение строится вызовом obtainMessage
            //Сообщение берется непосредственно из mRequestHandler, в результате чего поле target нового объекта Message немедленно заполняется mRequestHandler.
            //Поле what - MESSAGE_DOWNLOAD, поле obj заносится значение T target (PhotoHolder в данном случае)
            mRequestHandler.obtainMessage(MESSAGE_DOWNLOAD, target).sendToTarget();
            //метод sendToTarget() - для отправки сообщения его обработчику
            //Новое сообщение представляет запрос на загрузку заданного T target (PhotoHolder из RecyclerView.)
            // Вспомните, что реализация адаптера RecyclerView из PhotoGalleryFragment вызывает queueThumbnail(…)
            // из onBindViewHolder(…), передавая объект PhotoHolder, для которого загружается изображение, и URL-адрес загружаемого изображения.
        }
    }

    //Когда объект Looper добирается до конкретного сообщения в очереди, он передает сообщение приемнику сообщения для обработки.
    // Как правило, сообщение обрабатывается в реализации Handler.handleMessage(…) приемника.
    @Override
    protected void onLooperPrepared() {
        //В переменной mRequestHandler будет храниться ссылка на объект Handler, отвечающий за постановку
        // в очередь запросов на загрузку в фоновом потоке ThumbnailDownloader.
        mRequestHandler = new Handler() {
            //В нашем случае реализация handleMessage(…) будет использовать FlickrFetchr для загрузки
            // байтов по URL-адресу и их преобразования в растровое изображение.
            //Этот объект также будет отвечать за обработку сообщений запросов на загрузку при извлечении их из очереди.
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MESSAGE_DOWNLOAD) {
                    T target = (T) msg.obj;
                    Log.i(TAG, "Got a request for URL: " +
                            mRequestMap.get(target));
                    handleRequest(target);
                }
            }
        };
    }
    //метод для удаления всех запросов из очереди
    public void clearQueue() {
        mRequestHandler.removeMessages(MESSAGE_DOWNLOAD);
    }

    //метод для загрузки байтов по URL-адресу и их преобразования в растровое изображение
    private void handleRequest(final T target) {
        try {
            final String url = mRequestMap.get(target);
            if (url == null) {
                return;
            }
            byte[] bitmapBytes = new FlickrFetchr().getUrlBytes(url);
            final Bitmap bitmap = BitmapFactory
                    .decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
            Log.i(TAG, "Bitmap created");
            mResponseHandler.post(new Runnable() {
                @Override
                public void run() {
                    //Такая проверка необходима, потому что RecyclerView заново использует свои представления
                    if (mRequestMap.get(target) != url) {
                        return;
                    }

                    mRequestMap.remove(target);
                    mThumbnailDownloadListener.onThumbnailDownloaded(target, bitmap);
                }
            });
        } catch (IOException ioe) {
            Log.e(TAG, "Error downloading image", ioe);
        }

    }
}

package com.bignerdranch.android.photogallery;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static android.R.attr.format;

/**
 * Created by hotun on 24.07.2017.
 */
//класс для работы с сетью
public class FlickrFetcher {

    private static final String TAG = "FlickrFetcher";

    private static final String API_KEY = "310877515e55382e1ae26c0122697660";
    //Метод getUrlBytes(String) получает низкоуровневые данные по URL и возвращает их в виде массива байтов.
    public byte[] getUrlBytes(String urlSpec) throws IOException {
        //Этот код создает объект URL на базе строки — например, https://www.bignerdranch.com.
        URL url = new URL(urlSpec);
        //Затем вызов метода openConnection() создает объект подключения к заданному URL-адресу.
        // Вызов URL.openConnection() возвращает URLConnection, но поскольку подключение осуществляется по протоколу HTTP,
        // мы можем преобразовать его в HttpURLConnection.
        //Это открывает доступ к HTTP-интерфейсам для работы с методами запросов, кодами ответов, методами потоковой передачи и т. д.
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        //Объект HttpURLConnection представляет подключение, но связь с конечной точкой будет установлена только после вызова getInputStream()
        // (или getOutputStream() для POST-вызовов). До этого момента вы не сможете получить действительный код ответа.

        try {
            //После создания объекта URL и открытия подключения программа многократно вызывает read(), пока в подключении не кончатся данные.
            // Объект InputStream предоставляет байты по мере их доступности. Когда чтение будет завершено, программа закрывает его и
            // выдает массив байтов из ByteArrayOutputStream.
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();
            //если соединение не установлено тогда выбрасываем Exception
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException(connection.getResponseMessage() + ": with " + urlSpec);
            }

            int bytesRead = 0;
            byte[] buffer = new byte [1024];
            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            return out.toByteArray();
        } finally {
            connection.disconnect();
        }
    }
    //Метод getUrlString(String) преобразует результат из getUrlBytes(String) в String.
    public String getUrlString(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }

    public List<GalleryItem> fetchItems() {

        List<GalleryItem> items = new ArrayList<>();
        //Здесь мы используем класс Uri.Builder для построения полного URL-адреса для API-запроса к Flickr.
        // Uri.Builder — вспомогательный класс для создания параметризованных URL-адресов с правильным кодированием символов.
        // Метод Uri.Builder.appendQueryParameter(String,String) автоматически кодирует строки запросов.
        //Обратите внимание на добавленные значения параметров method, api_key, format и nojsoncallback.
        // Мы также задали дополнительный параметр extras со значением url_s. Значение url_s приказывает Flickr включить URL-адрес для уменьшенной версии изображения, если оно доступно.
        try {
            String url = Uri.parse("https://api.flickr.com/services/rest/")
                    .buildUpon()
                    .appendQueryParameter("method", "flickr.photos.getRecent")//пакет и название метода для работы
                    .appendQueryParameter("api_key", API_KEY)
                    .appendQueryParameter("format", "json")
                    .appendQueryParameter("nojsoncallback", "1")
                    .appendQueryParameter("extras", "url_s")
                    .build().toString();
            String jsonString = getUrlString(url);
            Log.i(TAG, "Received JSON: " + jsonString);
            //Мы получаем объект JSONObject верхнего уровня, соответствующий внешним фигурным скобкам в исходном тексте JSON.
            // Объект верхнего уровня содержит вложенный объект JSONObject с именем photos. Во вложенном объекте JSONObject
            // находится объект JSONArray с именем photo. Этот массив содержит набор объектов JSONObject,
            // каждый из которых представляет метаданные одной фотографии.
            JSONObject jsonBody = new JSONObject(jsonString);
            parseItems(items, jsonBody);
        } catch (JSONException je) {
            Log.e(TAG, "Failed to parse items", je);
        } catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch items", ioe);
        }

        return items;
    }

    private void parseItems(List<GalleryItem>items, JSONObject jsonBody) throws IOException, JSONException{

        JSONObject photosJsonObject = jsonBody.getJSONObject("photos");
        JSONArray photoJsonArray = photosJsonObject.getJSONArray("photo");

        for (int i = 0; i < photoJsonArray.length(); i++) {
            JSONObject photoJsonObject = photoJsonArray.getJSONObject(i);

            GalleryItem item = new GalleryItem();
            item.setId(photoJsonObject.getString("id"));
            item.setCaption(photoJsonObject.getString("title"));
            //Flickr не всегда возвращает компонент url_s для каждого изображения.
            // Добавьте проверку для игнорирования изображений, не имеющих URL-адреса изображения.
            if (!photoJsonObject.has("url_s")) {
                continue;
            }

            item.setUrl(photoJsonObject.getString("url_s"));
            items.add(item);
        }
    }
}

package com.bignerdranch.android.photogallery;

import android.net.Uri;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by hotun on 24.07.2017.
 */
//класс для работы с сетью
public class FlickrFetchr {
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

}

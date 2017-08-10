package com.bignerdranch.android.photogallery;

import android.content.Context;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

/**
 * Created by hotun on 30.07.2017.
 */
//класс нужен для сохранения поиска введенного пользователем
public class QueryPreferences {
    private static final String PREF_SEARCH_QUERY = "searchQuery";//используется в качестве ключа для хранения запроса
    //Наша служба будет опрашивать Flickr на появление новых результатов, поэтому ей нужно знать результат последней выборки.
    // Для этой работы идеально подойдет механизм SharedPreferences.
    private static final String PREF_LAST_RESULT_ID = "lastResultId";//используется в качестве ключа для сохранение последнего результата
    //константа для проверки сигнал находиться во включенном или в отключенном состоянии
    private static final String PREF_IS_ALARM_ON = "isAlarmOn";
    //Метод getStoredQuery(Context) возвращает значение запроса, хранящееся в общих настройках.
    public static String getStoredQuery(Context context) {
        //метод PreferenceManager.getDefaultSharedPreferences(Context), который возвращает экземпляр с именем по умолчанию
        // и закрытыми (private) разрешениями (чтобы настройки были доступны только в границах вашего приложения).
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PREF_SEARCH_QUERY, null);
    }
    //Так как QueryPreferences не имеет собственного контекста, вызывающий компонент должен передать свой контекст как входной параметр
    //Метод setStoredQuery(Context) записывает запрос в хранилище общих настроек для заданного контекста.
    public static void setStoredQuery(Context context, String query) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()//используется для получения экземпляра SharedPreferences.Editor. Этот класс используется для сохранения значений в SharedPreferences.
                .putString(PREF_SEARCH_QUERY, query)
                .apply();//чтобы эти изменения стали видимыми для всех пользователей файла SharedPreferences
                //Метод apply() вносит изменения в память немедленно, а непосредственная запись в файл осуществляется в фоновом потоке
    }
    //метод для получения последнего результата
    public static String getLastResultId(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PREF_LAST_RESULT_ID, null);
    }
    //метод для сохранения последней результата
    public static void setLastResultId(Context context, String lastResultId) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(PREF_LAST_RESULT_ID, lastResultId)
                .apply();
    }

    public static boolean isAlarmOn(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(PREF_IS_ALARM_ON, false);
    }

    public static void setAlarmOn(Context context, boolean isOn) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(PREF_IS_ALARM_ON, isOn)
                .apply();
    }
}

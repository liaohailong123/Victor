package org.liaohailong.library.victor.callback;

import android.text.TextUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.liaohailong.library.victor.Util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.util.List;

/**
 * Describe as: 文本数据请求回调，自动将数据实例化为bean类
 * Created by LiaoHaiLong on 2018/5/1.
 */

public abstract class TextCallback<T> implements Callback<T> {

    @Override
    public void getRawData(int code, String data) {
        if (code < HttpURLConnection.HTTP_BAD_REQUEST) {
            T t = parseData(data);
            onSuccess(t);
        } else {
            onFailure(code, data);
        }
    }

    protected T parseData(String data) {
        if (TextUtils.isEmpty(data)) {
            return null;
        }
        JsonObject rawData = Util.stringToJson(data);
        try {
            Type type = Util.getClassTypeParameter(getClass());
            if (type == JsonObject.class) {
                //noinspection unchecked
                return (T) rawData;
            }
            if (type == String.class) {
                return (T) rawData.toString();
            }
            if (rawData == null) {
                return null;
            }

            if (type instanceof ParameterizedType) {
                ParameterizedType pType = (ParameterizedType) type;
                Type rawType = pType.getRawType();
                Type[] arguments = pType.getActualTypeArguments();

                // 如果Result的类型为 List<? extends JsonInterface>，则解析结果中的list字段
                if ((rawType instanceof Class) && ((Class<?>) rawType).isAssignableFrom(List.class)
                        && arguments.length == 1 && (arguments[0] instanceof Class)) {
                    JsonElement jsonElement = rawData.get(getResultListKey());
                    JsonArray jsonArray = null;
                    if (jsonElement != null && !jsonElement.isJsonNull()) {
                        jsonArray = jsonElement.getAsJsonArray();
                    }
                    //noinspection unchecked
                    return (T) Util.jsonArrayToGenericList(jsonArray, arguments[0]);
                }
            }
            //noinspection unchecked
            return Util.jsonToGenericObject(rawData, type);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    protected String getResultListKey() {
        return "list";
    }


    @Override
    public void onPreLoading(String url) {

    }

    @Override
    public void onLoading(String url, String tempFilePath, int progress) {

    }

    @Override
    public void onPostLoaded(String url, String filePath) {

    }

    @Override
    public void onLoadingError(String url, String info) {

    }
}

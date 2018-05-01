package org.liaohailong.library.victor;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Describe as: Http POST方法的请求参数
 * Created by LiaoHaiLong on 2018/5/1.
 */

public class HttpField {
    private Map<String, String> mParams = new HashMap<>();

    public void addParam(String header, String value) {
        mParams.put(header, value);
    }

    HttpField addParams(Map<String, String> headers) {
        mParams.putAll(headers);
        return this;
    }

    public Set<Map.Entry<String, String>> entrySet() {
        return mParams.entrySet();
    }

    public Map<String, String> getParams() {
        return mParams;
    }

    @Override
    public String toString() {
        return mParams.toString();
    }
}

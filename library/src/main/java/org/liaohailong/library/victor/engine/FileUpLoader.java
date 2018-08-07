package org.liaohailong.library.victor.engine;


import com.google.gson.JsonObject;

import org.liaohailong.library.victor.Deliver;
import org.liaohailong.library.victor.HttpField;
import org.liaohailong.library.victor.Response;
import org.liaohailong.library.victor.Util;
import org.liaohailong.library.victor.callback.Callback;
import org.liaohailong.library.victor.request.FileRequest;
import org.liaohailong.library.victor.request.Request;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.UUID;

/**
 * Describe as : 文件上传
 * Created by LHL on 2018/5/3.
 */

class FileUpLoader<Type> extends FileLoader<Type> {

    FileUpLoader(Request<Type> request, Deliver deliver) {
        super(request, deliver);
    }

    @Override
    Response<Type> doWork() {
        onPreLoading();

        FileRequest<Type> fileRequest = (FileRequest<Type>) mRequest;
        DataOutputStream dos = null;
        InputStream is = null;

        try {
            String end = "\r\n";
            String twoHyphens = "--";
            String boundary = UUID.randomUUID().toString(); // 边界标识 随机生成
            String CONTENT_TYPE = "multipart/form-data"; // 内容类型

            HttpURLConnection connection = (HttpURLConnection) new URL(mRequest.getUrl()).openConnection();
            setConnection(connection);
            addHeaders(connection);
            // 允许输入输出流
            connection.setDoInput(true);// 允许输入流
            connection.setDoOutput(true);// 允许输出流
            connection.setUseCaches(false);// 不允许使用缓存
            //指定流的大小，当内容达到这个值的时候就把流输出
            connection.setChunkedStreamingMode(128 * 1024);// 128K
            //计算请求主体内容总长度
            StringBuilder sb = new StringBuilder();
            //普通的表单的数据
            HttpField postParams = fileRequest.getPostParams();
            for (Map.Entry<String, String> entry : postParams.getParams().entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                sb.append(twoHyphens).append(boundary).append(end);
                sb.append("Content-Disposition: form-data; name=\"").append(key).append("\"").append(end);
                sb.append(end);
                sb.append(value).append(end);
            }
            //上传文件的头部
            String key = fileRequest.getFileKey();
            File file = fileRequest.getFile();
            sb.append(twoHyphens).append(boundary).append(end);
            sb.append("Content-Disposition: form-data; name=\"").append(key).append("\"; filename=\"").append(file.getName()).append("\"").append(end);
            sb.append(end);

            byte[] headerInfo = sb.toString().getBytes("UTF-8");
            byte[] endInfo = (end + twoHyphens + boundary + twoHyphens + end).getBytes("UTF-8");

            long contentLength = headerInfo.length + file.length() + endInfo.length;
            connection.addRequestProperty("Content-Length", String.valueOf(contentLength));
            // 使用POST方法
            connection.setRequestMethod("POST");// 请求方式
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("Charset", "UTF-8");// 设置编码
            connection.setRequestProperty("Content-Type",
                    CONTENT_TYPE + ";boundary=" + boundary);
            //尝试连接
            connection.connect();
            //开始写入报文主体
            dos = new DataOutputStream(connection.getOutputStream());
            //step1:写入文本参数
            dos.write(headerInfo);
            //step2:写入文件
            FileInputStream fileInputStream = new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(fileInputStream);

            long length = file.length();
            byte[] buffer = new byte[8192];
            int outLength = 0;
            int count;
            while ((count = bis.read(buffer, 0, buffer.length)) != -1) {
                dos.write(buffer, 0, count);
                outLength += count;
                int percent = (int) (((outLength * 1f) / (length * 1f)) * 100);
                //正在上传
                onLoading("", percent);
                //手动打断线程执行
                boolean interrupted = Thread.interrupted();
                if (interrupted) {
                    return null;
                }
                if (fileRequest.isCanceled) {
                    return null;
                }
            }
            //step3:写入结尾标识
            dos.write(endInfo);
            bis.close();
            dos.flush();
            dos.close();

            int code = connection.getResponseCode();
            if (code < HttpURLConnection.HTTP_BAD_REQUEST) {
                is = connection.getInputStream();
                //利用Json转移字符
                JsonObject jsonObject = Util.stringToJson(Util.streamToString(is));
                final String result = jsonObject != null ? jsonObject.toString() : "";
                final String url = mRequest.getUrl();
                final Callback<Type> callback = mRequest.getCallback();
                mDeliver.postResponse(new Runnable() {
                    @Override
                    public void run() {
                        callback.onPostLoaded(url, result);
                    }
                });
            } else {
                is = connection.getErrorStream();
                String result = Util.streamToString(is);
                onLoadingError(result);
            }
        } catch (Exception e) {
            e.printStackTrace();
            onLoadingError(e.toString());
        } finally {
            Util.close(dos);
            Util.close(is);
            mRequest.setCallback(null);
            mRequest = null;
        }
        return null;
    }
}

package org.liaohailong.library.victor.engine;


import org.liaohailong.library.victor.Deliver;
import org.liaohailong.library.victor.HttpField;
import org.liaohailong.library.victor.Response;
import org.liaohailong.library.victor.Util;
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
 * Describe as :
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
            // 使用POST方法
            connection.setRequestMethod("POST");// 请求方式
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("Charset", "UTF-8");// 设置编码
            connection.setRequestProperty("Content-Type",
                    CONTENT_TYPE + ";boundary=" + boundary);
            //尝试连接
            connection.connect();
            //开始写入报文主体
            dos = new DataOutputStream(
                    connection.getOutputStream());
            //step1:写入文本参数
            dos.writeBytes(twoHyphens + boundary + end);
            HttpField postParams = fileRequest.getPostParams();
            for (Map.Entry<String, String> entry : postParams.getParams().entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                dos.writeBytes("Content-Disposition: form-data; name=\"" + key + "\"" + end);
                dos.writeBytes(end);
                dos.writeUTF(value);
                dos.writeBytes(end);
            }
            //step2:写入文件
            String key = fileRequest.getFileKey();
            File file = fileRequest.getFile();
             /*
             * 这里重点注意： name里面的值为服务器端需要key 只有这个key 才可以得到对应的文件
             * filename是文件的名字，包含后缀名
             */
            dos.writeBytes(twoHyphens + boundary + end);
            dos.writeBytes("Content-Disposition: form-data; name=\"" + key
                    + "\"; filename=\"" + file.getName() + "\"" + end);
            dos.writeBytes(end);

            // 读取文件
            FileInputStream fileInputStream = new FileInputStream(file);
            int available = fileInputStream.available();
            BufferedInputStream bis = new BufferedInputStream(fileInputStream, available);
            long length = file.length();
            int outLength = 0;

            byte[] buffer = new byte[available];
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
                if (fileRequest.isCanceled()) {
                    return null;
                }
            }
            bis.close();
            dos.writeBytes(end);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + end);
            dos.flush();

            int code = connection.getResponseCode();
            if (code < HttpURLConnection.HTTP_BAD_REQUEST) {
                is = connection.getInputStream();
                onPostLoaded("");
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

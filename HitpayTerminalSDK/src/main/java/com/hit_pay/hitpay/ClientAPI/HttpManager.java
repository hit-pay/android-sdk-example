package com.hit_pay.hitpay.ClientAPI;

import android.util.Log;

import com.android.internal.http.multipart.MultipartEntity;
import com.android.internal.http.multipart.Part;

import org.apache.http.HttpEntity;
import java.io.ByteArrayOutputStream;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by Nitin on 23/7/16.
 */
public class HttpManager {


    private static HttpManager instance;
    private static final String TAG = "HttpManager";

    private HttpManager() {
    }

    public static HttpManager getInstance() {
        if (instance == null) {
            instance = new HttpManager();
        }
        return instance;
    }

    public HttpResponse doPost(String url, HashMap<String, String> headers, HashMap<String, String> params) {
        try {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpParams httpParameters = httpClient.getParams();
            HttpConnectionParams.setConnectionTimeout(httpParameters, 20000);
            HttpConnectionParams.setSoTimeout(httpParameters, 20000);
            HttpPost post = new HttpPost(url);
            ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();


            if (params != null) {
                Iterator<String> paramsIterator = params.keySet().iterator();
                while (paramsIterator.hasNext()) {
                    String key = paramsIterator.next();
                    nameValuePairs.add(new BasicNameValuePair(key, params.get(key)));
                }
            }

            if (headers != null) {
                Iterator<String> headersIterator = headers.keySet().iterator();
                while (headersIterator.hasNext()) {
                    String key = headersIterator.next();
                    post.setHeader(key, headers.get(key));
                }
            }

            post.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            HttpResponse response = httpClient.execute(post);
            if (response != null) {

                return response;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public HttpResponse doMultipartPost(String url, HashMap<String, String> headers, HashMap<String, String> params,String filePath) {

        try {

            HttpClient client = new DefaultHttpClient();
            HttpPost uploadFile = new HttpPost(url);
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            System.out.println("1.");
            if (headers != null) {
                Iterator<String> headersIterator = headers.keySet().iterator();
                while (headersIterator.hasNext()) {
                    String key = headersIterator.next();
                    uploadFile.addHeader(key, headers.get(key));
                }
            }
            System.out.println("2.");

            Iterator<String> paramsIterator = params.keySet().iterator();
            System.out.println("3.");
            while (paramsIterator.hasNext()) {
                String key = paramsIterator.next();
//                builder.addBinaryBody(key,key.getBytes(), ContentType.APPLICATION_OCTET_STREAM,key);
                builder.addTextBody(key,params.get(key), ContentType.APPLICATION_OCTET_STREAM);
            }

            if (filePath!=null){
                System.out.println("added image "+filePath);
                File file = new File(filePath);
                byte[] data = readFile(file);
                System.out.println("image size" +data.length);
//                builder.addBinaryBody("image",data, ContentType.MULTIPART_FORM_DATA,"image.jpeg");
                builder.addBinaryBody("image",data,ContentType.APPLICATION_JSON,"productImage.jpeg");
            }

            System.out.println("4.");

//            Iterator<String> filesIterator = files.keySet().iterator();
//            while (filesIterator.hasNext()) {
//                String key = filesIterator.next();
//                builder.addbi(key,params.get(key), ContentType.APPLICATION_OCTET_STREAM);
//            }

// This attaches the file to the POST:
//            File f = new File("[/path/to/upload]");
//            builder.addBinaryBody(
//                    "file",
//                    new FileInputStream(f),
//                    ContentType.APPLICATION_OCTET_STREAM,
//                    f.getName()
//            );

            System.out.println("Sending req");
            HttpEntity multipart = builder.build();
            uploadFile.setEntity(multipart);
            HttpResponse response = client.execute(uploadFile);
            System.out.println("posting done...");
//            HttpEntity responseEntity = response.getEntity();

//            HttpClient client = new DefaultHttpClient();
//            HttpPost post = new HttpPost(url);
//            post.setHeader("Accept", "application/json");
//            post.setHeader("Content-type", "multipart/form-data");
//            MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
//            entityBuilder.setMode(HttpMultipartMode.STRICT);
//
//            Iterator<String> paramsIterator = params.keySet().iterator();
//            while (paramsIterator.hasNext()) {
//                String key = paramsIterator.next();
//                entityBuilder.addTextBody(key,params.get(key));
//            }
//
//            if (headers != null) {
//                Iterator<String> headersIterator = headers.keySet().iterator();
//                while (headersIterator.hasNext()) {
//                    String key = headersIterator.next();
//                    post.setHeader(key, headers.get(key));
//                }
//            }
//
//            HttpEntity entity = entityBuilder.build();
//            post.setEntity(entity);
//            HttpResponse response = client.execute(post);
            return response;
        } catch (Exception e) {
            System.out.println("faillll");
            System.out.println(e.toString());
            e.printStackTrace();
            return null;
        }

    }

    public HttpResponse doMultipartPut(String url, HashMap<String, String> headers, HashMap<String, String> params,String filePath) {

        try {

            HttpClient client = new DefaultHttpClient();
            HttpPut uploadFile = new HttpPut(url);
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            System.out.println("1.");
            if (headers != null) {
                Iterator<String> headersIterator = headers.keySet().iterator();
                while (headersIterator.hasNext()) {
                    String key = headersIterator.next();
                    uploadFile.addHeader(key, headers.get(key));
                }
            }
            System.out.println("2.");

            Iterator<String> paramsIterator = params.keySet().iterator();
            System.out.println("3.");
            while (paramsIterator.hasNext()) {
                String key = paramsIterator.next();
                builder.addTextBody(key,params.get(key), ContentType.APPLICATION_OCTET_STREAM);
            }

            if (filePath!=null){
                System.out.println("added image "+filePath);
                File file = new File(filePath);
                byte[] data = readFile(file);
                System.out.println("image size" +data.length);
                builder.addBinaryBody("image",data,ContentType.APPLICATION_JSON,"productImage.jpeg");
            }

            System.out.println("4.");

            System.out.println("Sending req");
            HttpEntity multipart = builder.build();
            uploadFile.setEntity(multipart);
            HttpResponse response = client.execute(uploadFile);
            System.out.println("posting done...");
            return response;
        } catch (Exception e) {
            System.out.println("faillll");
            System.out.println(e.toString());
            e.printStackTrace();
            return null;
        }

    }

    public HttpResponse doPostRequest(String uri, HashMap<String, String> headers, Map<String, String> params) throws IOException {
        System.out.println(uri);
        JSONObject jsonObject = new JSONObject(params);
        String json = jsonObject.toString();
        System.out.println(json);

        HttpPost httpPost =  new HttpPost(uri);
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");
//        StringEntity se = new StringEntity(json);
//        se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
        StringEntity tmp = null;
        tmp = new StringEntity(json, "UTF-8");
        httpPost.setEntity(tmp);

//        httpPost.setEntity(se);
        if (headers != null) {
            Iterator<String> headerIterator = headers.keySet().iterator();

            while (headerIterator.hasNext()) {
                String key = headerIterator.next();
                httpPost.addHeader(key, headers.get(key));
            }
        }
        return new DefaultHttpClient().execute(httpPost);
    }

    public HttpResponse doPostObjectRequest(String uri, HashMap<String, String> headers, Map<String, Object> params) throws IOException {
        System.out.println(uri);
        JSONObject jsonObject = new JSONObject(params);
        String json = jsonObject.toString();
        System.out.println(json);

        HttpPost httpPost =  new HttpPost(uri);
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");
//        StringEntity se = new StringEntity(json);
//        se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
        StringEntity tmp = null;
        tmp = new StringEntity(json, "UTF-8");
        httpPost.setEntity(tmp);

//        httpPost.setEntity(se);
        if (headers != null) {
            Iterator<String> headerIterator = headers.keySet().iterator();

            while (headerIterator.hasNext()) {
                String key = headerIterator.next();
                httpPost.addHeader(key, headers.get(key));
            }
        }
        return new DefaultHttpClient().execute(httpPost);
    }

    public HttpResponse doPostObjectRequest(String uri, HashMap<String, String> headers, JSONObject jsonObject) throws IOException {
        System.out.println(uri);

        HttpPost httpPost =  new HttpPost(uri);
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");
//        StringEntity se = new StringEntity(json);
//        se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
        StringEntity tmp = null;
        tmp = new StringEntity(jsonObject.toString(), "UTF-8");
        httpPost.setEntity(tmp);

//        httpPost.setEntity(se);
        if (headers != null) {
            Iterator<String> headerIterator = headers.keySet().iterator();

            while (headerIterator.hasNext()) {
                String key = headerIterator.next();
                httpPost.addHeader(key, headers.get(key));
            }
        }
        return new DefaultHttpClient().execute(httpPost);
    }

    public HttpResponse doPutObjectRequest(String uri, HashMap<String, String> headers, Map<String, Object> params) throws IOException {
        System.out.println(uri);
        JSONObject jsonObject = new JSONObject(params);
        String json = jsonObject.toString();
        System.out.println(json);

        HttpPut httpPost =  new HttpPut(uri);
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");
        StringEntity tmp = null;
        tmp = new StringEntity(json, "UTF-8");
        httpPost.setEntity(tmp);

        if (headers != null) {
            Iterator<String> headerIterator = headers.keySet().iterator();

            while (headerIterator.hasNext()) {
                String key = headerIterator.next();
                httpPost.addHeader(key, headers.get(key));
            }
        }
        return new DefaultHttpClient().execute(httpPost);
    }

    public HttpResponse doPostObjects(String uri, HashMap<String, String> headers, Map<String, Object> params) throws IOException {
        System.out.println(uri);
        JSONObject jsonObject = new JSONObject(params);
        String json = jsonObject.toString();
        System.out.println(json);

        HttpPost httpPost = new HttpPost(uri);
//        httpPost.setHeader("Accept", "application/json");
//        httpPost.setHeader("Content-type", "application/x-www-form-urlencoded");
//        StringEntity se = new StringEntity(json);
//        se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
        StringEntity tmp = null;
        tmp = new StringEntity(json, "UTF-8");
        httpPost.setEntity(tmp);

//        httpPost.setEntity(se);
        if (headers != null) {
            Iterator<String> headerIterator = headers.keySet().iterator();

            while (headerIterator.hasNext()) {
                String key = headerIterator.next();
                httpPost.addHeader(key, headers.get(key));
            }
        }
        return new DefaultHttpClient().execute(httpPost);
    }

    public  HttpResponse doPut(String url, HashMap<String, String> headers, HashMap<String, Object> params) throws IOException {
        System.out.println(url);
        JSONObject jsonObject = new JSONObject(params);
        String json = jsonObject.toString();
        System.out.println(json);

        HttpPut httpPut = new HttpPut(url);
//        httpPut.setHeader("Accept", "application/json");
//        httpPut.setHeader("Content-type", "application/json");

        StringEntity tmp = null;
        tmp = new StringEntity(json, "UTF-8");
        httpPut.setEntity(tmp);

        if (headers != null) {
            Iterator<String> headerIterator = headers.keySet().iterator();

            while (headerIterator.hasNext()) {
                String key = headerIterator.next();
                httpPut.addHeader(key, headers.get(key));
            }
        }
        return new DefaultHttpClient().execute(httpPut);
    }

    public  HttpResponse doDelete(String url, HashMap<String, String> headers, HashMap<String, Object> params) throws IOException {
        System.out.println(url);
        JSONObject jsonObject = new JSONObject(params);
        String json = jsonObject.toString();
        System.out.println(json);

        HttpDelete httpDelete = new HttpDelete(url);
//        httpPut.setHeader("Accept", "application/json");
//        httpPut.setHeader("Content-type", "application/json");

        if (headers != null) {
            Iterator<String> headerIterator = headers.keySet().iterator();

            while (headerIterator.hasNext()) {
                String key = headerIterator.next();
                httpDelete.addHeader(key, headers.get(key));
            }
        }
        return new DefaultHttpClient().execute(httpDelete);
    }

    public HttpResponse doGet(String url, HashMap<String, String> headers, HashMap<String, String> params) throws IOException {
        System.out.println(url);
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpParams httpParameters = httpClient.getParams();
        List<NameValuePair> httpParams = new LinkedList<NameValuePair>();
        if (params != null) {
            Iterator<String> paramsIterator = params.keySet().iterator();
            while (paramsIterator.hasNext()) {
                String key = paramsIterator.next();
                httpParams.add(new BasicNameValuePair(key, params.get(key)));
            }
//            httpParams.add(new BasicNameValuePair("_", "" + System.currentTimeMillis()));
        }
        HttpGet get = null;
        if (httpParams.isEmpty()) {
            get = new HttpGet(url);
        } else {
            get = new HttpGet(url + "?" + URLEncodedUtils.format(httpParams, "UTF-8"));
        }

        get.setHeader("Accept", "application/json");
        get.setHeader("Content-type", "application/json");

        if (headers != null) {
            Iterator<String> headerIterator = headers.keySet().iterator();
            while (headerIterator.hasNext()) {
                String key = headerIterator.next();
                get.addHeader(key, headers.get(key));
            }
        }


        HttpResponse response = httpClient.execute(get);
        return response;

    }

    public static byte[] readFile(File file) throws IOException {
        // Open file
        RandomAccessFile f = new RandomAccessFile(file, "r");
        try {
            // Get and check length
            long longlength = f.length();
            int length = (int) longlength;
            if (length != longlength)
                throw new IOException("File size >= 2 GB");
            // Read file and return data
            byte[] data = new byte[length];
            f.readFully(data);
            return data;
        } finally {
            f.close();
        }
    }

}


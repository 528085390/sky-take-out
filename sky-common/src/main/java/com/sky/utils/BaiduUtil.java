/**
 * 选择AK使用SN校验：
 */

package com.sky.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.web.util.UriUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BaiduUtil {

    public static String AK = "AuxNX3Iy8zwNlP74AMp96Yd3HS6O5dyv";

    public static String SK = "i8LrKwlhGhGRt7DT13UC2fGRQ2nWJ94u";

    public static String URL = "https://api.map.baidu.com/place/v2/search?";

    public static String GER_TIME_URL = "https://api.map.baidu.com/directionlite/v1/riding?";

    public static Integer ACCEPT_DISTANCE = 5000;

    public Map getDestination(String query, String region) throws Exception {

        BaiduUtil snCal = new BaiduUtil();

        Map params = new LinkedHashMap<String, String>();
        params.put("query", query);
        params.put("region", region);
        params.put("output", "json");
        params.put("ak", AK);


        params.put("sn", snCal.caculateSn(query, region));

        Map latAndLng = snCal.requestGetSN(URL, params);
        return latAndLng;
    }

    public Boolean ifAccept(Map latAndLng) throws Exception {
        BaiduUtil snCal = new BaiduUtil();
        Double lat = (Double) latAndLng.get("lat");
        Double lng = (Double) latAndLng.get("lng");
        String destination = lat + "," + lng;

        Map params = new LinkedHashMap<String, String>();
        params.put("origin", "40.01116,116.339303");
        params.put("destination", destination);
        params.put("ak", AK);
        params.put("riding_type", "1");
        String currentTimestamp = String.valueOf(System.currentTimeMillis());
        params.put("timestamp", currentTimestamp);
        params.put("sn", snCal.caculateSn(destination));

        Integer distance = snCal.requestGetTime(GER_TIME_URL, params);
        System.out.println("距离：" + distance);
        return distance <= ACCEPT_DISTANCE;
    }

    /**
     * 选择了ak，使用SN校验：
     * 根据您选择的AK已为您生成调用代码
     * 检测您当前的AK设置了sn检验，本示例中已为您生成sn计算代码
     *
     * @param strUrl
     * @param param
     * @throws Exception
     */
    public Map requestGetSN(String strUrl, Map<String, String> param) throws Exception {
        if (strUrl == null || strUrl.length() <= 0 || param == null || param.size() <= 0) {
            return null;
        }

        StringBuffer queryString = new StringBuffer();
        queryString.append(strUrl);
        for (Map.Entry<?, ?> pair : param.entrySet()) {
            queryString.append(pair.getKey() + "=");
            //    第一种方式使用的 jdk 自带的转码方式  第二种方式使用的 spring 的转码方法 两种均可
            //    queryString.append(URLEncoder.encode((String) pair.getValue(), "UTF-8").replace("+", "%20") + "&");
            queryString.append(UriUtils.encode((String) pair.getValue(), "UTF-8") + "&");
        }

        if (queryString.length() > 0) {
            queryString.deleteCharAt(queryString.length() - 1);
        }

        java.net.URL url = new URL(queryString.toString());
        System.out.println(queryString.toString());
        URLConnection httpConnection = (HttpURLConnection) url.openConnection();
        httpConnection.connect();

        InputStreamReader isr = new InputStreamReader(httpConnection.getInputStream());
        BufferedReader reader = new BufferedReader(isr);
        StringBuffer buffer = new StringBuffer();
        String line;
        while ((line = reader.readLine()) != null) {
            buffer.append(line);
        }
        reader.close();
        isr.close();

        //处理结果 返回目的地的经纬度

        JSONObject jsonObject = JSONObject.parseObject(buffer.toString());
        List<JSONObject> results = jsonObject.getJSONArray("results").toJavaList(JSONObject.class);


        if (results != null && !results.isEmpty()) {
            JSONObject firstResult = results.get(0);
            JSONObject location = firstResult.getJSONObject("location");

            double lat = location.getDoubleValue("lat");
            double lng = location.getDoubleValue("lng");

            Map latAndLng = new HashMap();
            latAndLng.put("lat", lat);
            latAndLng.put("lng", lng);
            return latAndLng;

        }
        return null;
    }

    public String caculateSn(String query, String region) throws UnsupportedEncodingException,
            NoSuchAlgorithmException {
        BaiduUtil snCal = new BaiduUtil();

        // 计算sn跟参数对出现顺序有关，get请求请使用LinkedHashMap保存<key,value>，该方法根据key的插入顺序排序；post请使用TreeMap保存<key,value>，该方法会自动将key按照字母a-z顺序排序。
        // 所以get请求可自定义参数顺序（sn参数必须在最后）发送请求，但是post请求必须按照字母a-z顺序填充body（sn参数必须在最后）。
        // 以get请求为例：http://api.map.baidu.com/geocoder/v2/?address=百度大厦&output=json&ak=yourak，paramsMap中先放入address，再放output，然后放ak，放入顺序必须跟get请求中对应参数的出现顺序保持一致。
        Map paramsMap = new LinkedHashMap<String, String>();
        paramsMap.put("query", query);
        paramsMap.put("region", region);
        paramsMap.put("output", "json");
        paramsMap.put("ak", AK);


        // 调用下面的toQueryString方法，对LinkedHashMap内所有value作utf8编码，拼接返回结果address=%E7%99%BE%E5%BA%A6%E5%A4%A7%E5%8E%A6&output=json&ak=yourak
        String paramsStr = snCal.toQueryString(paramsMap);

        // 对paramsStr前面拼接上/geocoder/v2/?，后面直接拼接yoursk得到/geocoder/v2/?address=%E7%99%BE%E5%BA%A6%E5%A4%A7%E5%8E%A6&output=json&ak=yourakyoursk
        String wholeStr = new String("/place/v2/search?" + paramsStr + SK);

        System.out.println(wholeStr);
        // 对上面wholeStr再作utf8编码
        String tempStr = URLEncoder.encode(wholeStr, "UTF-8");

        // 调用下面的MD5方法得到最后的sn签名
        String sn = snCal.MD5(tempStr);
        System.out.println(sn);
        return sn;
    }

    public String caculateSn(String destination) throws UnsupportedEncodingException,
            NoSuchAlgorithmException {
        BaiduUtil snCal = new BaiduUtil();

        // 计算sn跟参数对出现顺序有关，get请求请使用LinkedHashMap保存<key,value>，该方法根据key的插入顺序排序；post请使用TreeMap保存<key,value>，该方法会自动将key按照字母a-z顺序排序。
        // 所以get请求可自定义参数顺序（sn参数必须在最后）发送请求，但是post请求必须按照字母a-z顺序填充body（sn参数必须在最后）。
        // 以get请求为例：http://api.map.baidu.com/geocoder/v2/?address=百度大厦&output=json&ak=yourak，paramsMap中先放入address，再放output，然后放ak，放入顺序必须跟get请求中对应参数的出现顺序保持一致。
        Map paramsMap = new LinkedHashMap<String, String>();
        paramsMap.put("origin", "40.01116,116.339303");
        paramsMap.put("destination", destination);
        paramsMap.put("ak", AK);
        paramsMap.put("riding_type", "1");
        String currentTimestamp = String.valueOf(System.currentTimeMillis());
        paramsMap.put("timestamp", currentTimestamp);

        // 调用下面的toQueryString方法，对LinkedHashMap内所有value作utf8编码，拼接返回结果address=%E7%99%BE%E5%BA%A6%E5%A4%A7%E5%8E%A6&output=json&ak=yourak
        String paramsStr = snCal.toQueryString(paramsMap);

        // 对paramsStr前面拼接上/geocoder/v2/?，后面直接拼接yoursk得到/geocoder/v2/?address=%E7%99%BE%E5%BA%A6%E5%A4%A7%E5%8E%A6&output=json&ak=yourakyoursk
        String wholeStr = new String("/directionlite/v1/riding?" + paramsStr + SK);

        System.out.println(wholeStr);
        // 对上面wholeStr再作utf8编码
        String tempStr = URLEncoder.encode(wholeStr, "UTF-8");

        // 调用下面的MD5方法得到最后的sn签名
        String sn = snCal.MD5(tempStr);
        System.out.println(sn);
        return sn;
    }

    // 对Map内所有value作utf8编码，拼接返回结果
    public String toQueryString(Map<?, ?> data)
            throws UnsupportedEncodingException {
        StringBuffer queryString = new StringBuffer();
        for (Map.Entry<?, ?> pair : data.entrySet()) {
            queryString.append(pair.getKey() + "=");
            //    第一种方式使用的 jdk 自带的转码方式  第二种方式使用的 spring 的转码方法 两种均可
            //    queryString.append(URLEncoder.encode((String) pair.getValue(), "UTF-8").replace("+", "%20") + "&");
            queryString.append(UriUtils.encode((String) pair.getValue(), "UTF-8") + "&");
        }
        if (queryString.length() > 0) {
            queryString.deleteCharAt(queryString.length() - 1);
        }
        return queryString.toString();
    }

    // 来自stackoverflow的MD5计算方法，调用了MessageDigest库函数，并把byte数组结果转换成16进制
    public String MD5(String md5) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest
                    .getInstance("MD5");
            byte[] array = md.digest(md5.getBytes());
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < array.length; ++i) {
                sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100)
                        .substring(1, 3));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
        }
        return null;
    }


    public Integer requestGetTime(String strUrl, Map<String, String> param) throws Exception {
        if (strUrl == null || strUrl.length() <= 0 || param == null || param.size() <= 0) {
            return null;
        }

        StringBuffer queryString = new StringBuffer();
        queryString.append(strUrl);
        for (Map.Entry<?, ?> pair : param.entrySet()) {
            queryString.append(pair.getKey() + "=");
            //    第一种方式使用的 jdk 自带的转码方式  第二种方式使用的 spring 的转码方法 两种均可
            //    queryString.append(URLEncoder.encode((String) pair.getValue(), "UTF-8").replace("+", "%20") + "&");
            queryString.append(UriUtils.encode((String) pair.getValue(), "UTF-8") + "&");
        }

        if (queryString.length() > 0) {
            queryString.deleteCharAt(queryString.length() - 1);
        }

        java.net.URL url = new URL(queryString.toString());
        System.out.println(queryString.toString());
        URLConnection httpConnection = (HttpURLConnection) url.openConnection();
        httpConnection.connect();

        InputStreamReader isr = new InputStreamReader(httpConnection.getInputStream());
        BufferedReader reader = new BufferedReader(isr);
        StringBuffer buffer = new StringBuffer();
        String line;
        while ((line = reader.readLine()) != null) {
            buffer.append(line);
        }
        reader.close();
        isr.close();

        //提取结果中的距离
        JSONObject jsonObject = JSONObject.parseObject(buffer.toString());
        JSONObject result = jsonObject.getJSONObject("result");
        JSONArray routes = result.getJSONArray("routes");

        if (routes != null && !routes.isEmpty()) {
            JSONObject firstRoute = routes.getJSONObject(0);
            Integer distance = firstRoute.getIntValue("distance");

            return distance;
        }
        return null;
    }

}



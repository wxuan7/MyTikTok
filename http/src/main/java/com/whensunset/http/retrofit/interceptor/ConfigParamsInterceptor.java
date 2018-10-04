package com.whensunset.http.retrofit.interceptor;

import android.text.TextUtils;
import android.util.Pair;

import com.whensunset.http.retrofit.RetrofitConfig;
import com.whensunset.http.retrofit.multipart.StreamRequestBody;
import com.whensunset.http.retrofit.utils.ParamsUtils;
import com.whensunset.utils.IOUtil;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.Response;
import okio.Buffer;

/**
 * 将 config 里面的 param 的 body、url 整合到 request 里去
 */
public class ConfigParamsInterceptor implements Interceptor {

  private static final String NAME = "name=\"";

  private final RetrofitConfig.Params mConfig;

  public ConfigParamsInterceptor(RetrofitConfig.Params config) {
    mConfig = config;
  }

  @Override
  public Response intercept(Chain chain) throws IOException {
    Request original = chain.request();
    HttpUrl originalHttpUrl = original.url();

    Set<String> queryParameterNames = originalHttpUrl.queryParameterNames();
    // 如果是 get 那么其储存的是 url 上的 param，如果是 post 那么其储存的是 body 中的 param
    Map<String, String> params = new HashMap<>();
    Map<String, String> multiParams = null;
    boolean get = "GET".equalsIgnoreCase(original.method());
    if (get) {
      if (queryParameterNames != null && !queryParameterNames.isEmpty()) {
        for (String key : queryParameterNames) {
          params.put(key, originalHttpUrl.queryParameter(key));
        }
      }
    } else if (original.body() != null) {
      if (original.body() instanceof FormBody) {
        FormBody originBody = (FormBody) original.body();
        int len = originBody.size();
        for (int i = 0; i < len; i++) {
          if (!params.containsKey(originBody.name(i))) {
            params.put(originBody.name(i), originBody.value(i));
          }
        }
      } else if (original.body() instanceof MultipartBody) {
        multiParams = extractMultipartParams(original);
        params.putAll(multiParams);
      }
    }

    Pair<Map<String, String>, Map<String, String>> pair =
        ParamsUtils.obtainParams(mConfig, params, get);
    Request.Builder builder = new Request.Builder();
    // 设置 request 的 body
    if (!get) {
      if (original.body() instanceof MultipartBody) {
        MultipartBody body = (MultipartBody) original.body();
        MultipartBody.Builder multipartBuilder =
            new MultipartBody.Builder(((MultipartBody) original.body()).boundary());
        multipartBuilder.setType(body.type());
        List<MultipartBody.Part> partList = new ArrayList<>(body.parts());

        for (MultipartBody.Part part : partList) {
          multipartBuilder.addPart(part.headers(), part.body());
        }

        Map<String, String> postParams = pair.second;
        if (postParams != null && !postParams.isEmpty()) {
          for (Map.Entry<String, String> entry : postParams.entrySet()) {

            if (multiParams != null && multiParams.containsKey(entry.getKey())) {
              continue;
            }

            multipartBuilder.addFormDataPart(entry.getKey(), entry.getValue());
          }
        }

        builder.method(original.method(), multipartBuilder.build());
      } else if (original.body() instanceof FormBody
          || original.body() == null
          || original.body().contentLength() == 0) {
        // 存在一部分没有任何参数的请求，只需要通用参数，这部分请求的 body 的 Length 是 0
        // 需要处理成 FormBody
        FormBody.Builder formBuilder = new FormBody.Builder();
        Map<String, String> postParams = pair.second;

        if (original.body() instanceof FormBody) {
          FormBody originBody = (FormBody) original.body();
          for (int i = 0; i < originBody.size(); i++) {
            String name = originBody.name(i);
            String value = originBody.value(i);
            if (postParams != null
                && postParams.containsKey(name)
                && TextUtils.equals(value, postParams.get(name))) {
              postParams.remove(name);
            }
          }
        }

        if (postParams != null) {
          for (Map.Entry<String, String> entry : postParams.entrySet()) {
            formBuilder.add(entry.getKey(), entry.getValue());
          }
        }
        builder.method(original.method(), formBuilder.build());
      }
    } else {
      builder.method(original.method(), original.body());
    }
    
    // 设置 request 的 head 、url、tag
  
    Headers lastHeaders = original.headers();
    Map<String, String> headers = mConfig.getHeaders();
    for (Map.Entry<String, String> entry : headers.entrySet()) {
      lastHeaders.newBuilder().add(entry.getKey(), entry.getValue());
    }
    builder.headers(lastHeaders);
  
    builder.url(buildUrl(originalHttpUrl, pair.first));
    builder.tag(original.tag());
    
    return chain.proceed(builder.build());
  }

  private Map<String, String> extractMultipartParams(Request original) throws IOException {
    MultipartBody multipartBody = (MultipartBody) original.body();
    Map<String, String> params = new HashMap<>();
    int size = multipartBody.size();
    for (int i = 0; i < size; i++) {
      MultipartBody.Part part = multipartBody.part(i);
      if (!(part.body() instanceof StreamRequestBody) && part.headers() != null) {
        String headerName = part.headers().get(part.headers().name(0));
        // multipart 的数据在添加成 FormData 的时候的格式是 form-data; name="", 这里是把对应的 key 取出
        int index = headerName.indexOf(NAME);
        // 取出来的 key 的名称会有 双引号阔起来，所以这里将双引号去除掉
        String name = headerName.substring(index + NAME.length(), headerName.length() - 1);

        // body 是一个 RequestBody 的形式，需要通过 buffer 输出到 Byte[] 数组中转换
        Buffer buffer = new Buffer();
        byte[] content = new byte[(int) part.body().contentLength()];
        part.body().writeTo(buffer);
        buffer.readFully(content);
        params.put(name, new String(content, Charset.forName("UTF-8")));
        IOUtil.closeQuietly(buffer);
      }
    }
    return params;
  }

  private HttpUrl buildUrl(HttpUrl url, Map<String, String> params) {
    if (params == null || params.isEmpty()) {
      return url;
    }
    HttpUrl.Builder builder = url.newBuilder();

    for (Map.Entry<String, String> entry : params.entrySet()) {
      if (url.queryParameter(entry.getKey()) == null) {
        builder.addQueryParameter(entry.getKey(), entry.getValue());
      } else {
        builder.setQueryParameter(entry.getKey(), entry.getValue());
      }
    }
    return builder.build();
  }
}

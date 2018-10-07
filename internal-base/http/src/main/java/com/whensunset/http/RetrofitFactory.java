package com.whensunset.http;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class RetrofitFactory {
  
  public static Retrofit.Builder newBuilder(final RetrofitConfig config) {
    return new Retrofit.Builder()
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(GsonConverterFactory.create(config.buildGson()))
        .addCallAdapterFactory(new CustomAdapterFactory() {
          
          @Override
          public Call<Object> buildCall(Call<Object> call) {
            return config.buildCall(call);
          }
          
          @Override
          public Observable<?> buildObservable(Observable<?> o, Call<Object> call,
                                               Annotation[] annotations) {
            return config.buildObservable(o, call, annotations);
          }
        })
        .addCallAdapterFactory(
            RxJava2CallAdapterFactory.createWithScheduler(config.getExecuteScheduler()))
        .baseUrl(config.buildBaseUrl())
        .client(config.buildClient());
  }
  
  
  public static abstract class CustomAdapterFactory extends CallAdapter.Factory {
    
    public abstract Call<Object> buildCall(Call<Object> call);
    
    public abstract Observable<?> buildObservable(Observable<?> o, Call<Object> call,
                                                  Annotation[] annotations);
    
    @Override
    public CallAdapter<?, ?> get(Type returnType, Annotation[] annotations, Retrofit retrofit) {
      if (CallAdapter.Factory.getRawType(returnType) != Observable.class) {
        return null; // Ignore non-Observable types.
      }
      
      // Look up the next call adapter which would otherwise be used if this one was not present.
      // noinspection unchecked returnType checked above to be Observable.
      final CallAdapter<Object, Observable<?>> delegate =
          (CallAdapter<Object, Observable<?>>) retrofit.nextCallAdapter(this, returnType,
              annotations);
      
      return new CallAdapter<Object, Object>() {
        
        @Override
        public Object adapt(final Call<Object> call) {
          Call<Object> newCall = buildCall(call);
          return buildObservable(delegate.adapt(newCall), newCall, annotations);
        }
        
        @Override
        public Type responseType() {
          return delegate.responseType();
        }
      };
    }
  }
}

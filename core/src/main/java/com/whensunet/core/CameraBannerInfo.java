package com.whensunet.core;

import android.support.annotation.IntDef;

import com.google.gson.annotations.SerializedName;
import com.whensunset.annotation.preference.BuiltInObjectPreference;
import com.whensunset.annotation.preference.OtherObjectPreference;
import com.whensunset.annotation.preference.PreferenceAnnotation;

import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

@PreferenceAnnotation
public class CameraBannerInfo implements Serializable {
  
  public static final int VIEW_TYPE_TXT = 1;
  public static final int VIEW_TYPE_MUSIC = 2;
  public static final int VIEW_TYPE_MAGIC_FACE = 3;
  public static final int VIEW_TYPE_OUTER_URI = 4;
  public static final int VIEW_TYPE_INNER_URI = 5;
  private static final long serialVersionUID = 6032444937692781359L;
  @SerializedName("iconUrl")
  @OtherObjectPreference(key = "magicIconCdnUrl")
  public List<String> mMagicBannerIconUrl;
  @SerializedName("id")
  @BuiltInObjectPreference(key = "activityId")
  public String mActivityId;
  @SerializedName("maxCount")
  @BuiltInObjectPreference(key = "maxCount")
  public int mMaxCount;
  @SerializedName("jumpUrl")
  @BuiltInObjectPreference(key = "jumpUrl")
  public String mJumpUrl;
  @SerializedName("beginShowTime")
  @BuiltInObjectPreference(key = "beginShowTime")
  public long mBeginShowTime;
  @BuiltInObjectPreference(key = "endShowTime")
  @SerializedName("endShowTime")
  public long mndShowTime;
  @SerializedName("activityViewType")
  @BuiltInObjectPreference(key = "activityViewType")
  public @BannerViewType
  int mActivityViewType;
  @SerializedName("magicFaceId")
  @BuiltInObjectPreference(key = "magicFaceId")
  public int mMagicFaceId;
  
  @IntDef(value = {VIEW_TYPE_TXT, VIEW_TYPE_MUSIC, VIEW_TYPE_MAGIC_FACE,
      VIEW_TYPE_OUTER_URI, VIEW_TYPE_INNER_URI})
  @Retention(RetentionPolicy.SOURCE)
  public @interface BannerViewType {
  }
  
}

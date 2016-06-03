package com.data.manipulation.pojo;

import java.io.Serializable;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class SiteData implements Serializable {

  private static final long serialVersionUID = -6691063471783076184L;

  private String siteId;
  private String name;
  private String keywords;
  private boolean mobile;
  private double score;
  
  public String getSiteId() {
    return siteId;
  }
  public void setSiteId(String id) {
    this.siteId = id;
  }
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public String getKeywords() {
    return keywords;
  }
  public void setKeywords(String keywords) {
    this.keywords = keywords;
  }
  public boolean isMobile() {
    return mobile;
  }
  public void setMobile(boolean isMobile) {
    this.mobile = isMobile;
  }
  public double getScore() {
    return score;
  }
  public void setScore(double score) {
    this.score = score;
  }
  
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }
}

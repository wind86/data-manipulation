package com.data.manipulation.pojo;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class FileData implements Serializable {
  
  private static final long serialVersionUID = 6336253874967949872L;

  private String fileName;
  private List<SiteData> data;
  
  public String getFileName() {
    return fileName;
  }
  public void setFileName(String fileName) {
    this.fileName = fileName;
  }
  public List<SiteData> getData() {
    return data;
  }
  public void setData(List<SiteData> sites) {
    this.data = sites;
  }
  
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }
}

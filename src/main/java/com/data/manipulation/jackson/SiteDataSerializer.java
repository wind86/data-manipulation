package com.data.manipulation.jackson;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;

import org.apache.commons.lang3.BooleanUtils;
import static org.apache.commons.lang3.StringUtils.*;

import com.data.manipulation.pojo.SiteData;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class SiteDataSerializer extends JsonSerializer<SiteData> {

  private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("##.##");
  
  @Override
  public void serialize(SiteData siteData, JsonGenerator jgen, SerializerProvider serializers)
      throws IOException, JsonProcessingException {
    
    jgen.writeStartObject();
    
    jgen.writeStringField("id", siteData.getSiteId());
    jgen.writeStringField("name", getSiteHost(siteData.getName()));
    jgen.writeNumberField("mobile", BooleanUtils.toInteger(siteData.isMobile()));
    jgen.writeStringField("keywords", defaultString(siteData.getKeywords()));
    
    jgen.writeFieldName("score");    
    long number = (long) siteData.getScore();
    if (number == siteData.getScore()) {
      jgen.writeNumber(number);
    } else {
      jgen.writeNumber(Double.parseDouble(DECIMAL_FORMAT.format(siteData.getScore())));
    }

    jgen.writeEndObject();
  }

  private String getSiteHost(String siteUrl) {
    try {
      URL url = new URL(prependIfMissing(lowerCase(siteUrl), "http://"));
      return url.getHost();
    } catch (MalformedURLException e) {
      return siteUrl;
    }
  }  
}
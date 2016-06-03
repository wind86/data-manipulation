package com.data.manipulation.jackson;

import java.io.IOException;

import com.data.manipulation.pojo.FileData;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class FileDataSerializer extends JsonSerializer<FileData> {

  @Override
  public void serialize(FileData fileData, JsonGenerator jgen, SerializerProvider serializers)
      throws IOException, JsonProcessingException {    
    jgen.writeStartObject();
    
    jgen.writeStringField("collectionId", fileData.getFileName());
    jgen.writeFieldName("sites");
    jgen.writeObject(fileData.getData());
    
    jgen.writeEndObject();
  }
}

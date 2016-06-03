package com.data.manipulation;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import static org.apache.commons.io.FileUtils.*;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.EnumUtils;
import static org.apache.commons.lang3.StringUtils.*;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.data.manipulation.jackson.FileDataSerializer;
import com.data.manipulation.jackson.SiteDataSerializer;
import com.data.manipulation.pojo.FileData;
import com.data.manipulation.pojo.SiteData;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class MainCLI {

  private static final Logger LOGGER = LoggerFactory.getLogger(MainCLI.class);
  
  private static final String INPUT_DIR_PATH_PARAM = "pathToDirectory";
  private static final String OUTPUT_FILE_NAME_PARAM = "outputFile";
  
  @SuppressWarnings("serial")
  private static final Map<String,List<String>> SITE_KEYWORDS_CACHE = new HashMap<String,List<String>>() {{
    put("example.com", Arrays.asList("sports","tennis","recreation"));
    put("example.jp", Arrays.asList("japan","travel"));
  }};
  
  private Options options;
  
  public MainCLI() {
    options = new Options();
    
    options.addOption(INPUT_DIR_PATH_PARAM, true, "path to directory containing csv and json files");
    options.addOption(OUTPUT_FILE_NAME_PARAM, true, "result file name");
  }
  
  public static void main(String[] args) throws Exception {
    new MainCLI().process(args);
  }
  
  public void process(String[] args) throws Exception {
    CommandLineParser parser = new DefaultParser();
    CommandLine cmd = parser.parse(options, args);    

    String inputDirLocation = cmd.getOptionValue(INPUT_DIR_PATH_PARAM);
    String outputFileName = cmd.getOptionValue(OUTPUT_FILE_NAME_PARAM);

    if (inputDirLocation == null || outputFileName == null) {
      LOGGER.error("unable to find required parameter {} or {}", INPUT_DIR_PATH_PARAM, OUTPUT_FILE_NAME_PARAM);
      return;
    }
    
    File inputDir = new File(inputDirLocation);    
    if (!inputDir.exists() || !inputDir.isDirectory()) {
      LOGGER.error("unable to find input directory {}", inputDir.getAbsolutePath());
      return;
    }
    
    List<FileData> fileData = readFileData(inputDir);
    
    LOGGER.info("processing file data ...");
    for (FileData fd : fileData) {
      for (SiteData siteData : fd.getData()) {
        siteData.setKeywords(getSiteKeywords(siteData));
      }
    }
    
    writeFileData(outputFileName, fileData);
    
    LOGGER.info("DONE");
  }
  
  private void writeFileData(String fileName, List<FileData> data) throws IOException {
    File outputFile = new File(fileName);
    
    if (outputFile.exists()) {
      forceDelete(outputFile);      
    }
    
    outputFile.createNewFile();
    
    LOGGER.info("generating result '{}'", outputFile.getAbsolutePath());
    
    ObjectMapper mapper = new ObjectMapper()
// pretty-print
//        .configure(SerializationFeature.INDENT_OUTPUT, true)
        .setLocale(Locale.US);
    
    SimpleModule module = new SimpleModule();
    module.addSerializer(FileData.class, new FileDataSerializer());
    module.addSerializer(SiteData.class, new SiteDataSerializer());
    
    mapper.registerModule(module);
    
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < data.size(); i++) {
      sb.append(mapper.writeValueAsString(data.get(i)));
      
      if (i < data.size() - 1) {
        sb.append(",");
      }
      
      sb.append(System.lineSeparator());
    }
    
    writeStringToFile(outputFile, sb.toString());
  }
  
  private String getSiteKeywords(SiteData siteData) {
    String keywords = null;
    String siteLocation = prependIfMissing(lowerCase(siteData.getName()), "http://");
    
    try {
      URL url = new URL(siteLocation);
      if (SITE_KEYWORDS_CACHE.containsKey(url.getHost())) {
        StringBuilder sb = new StringBuilder();
        List<String> cachedKeywords = SITE_KEYWORDS_CACHE.get(url.getHost());
        
        for (int i = 0; i < cachedKeywords.size(); i++) {
          sb.append(cachedKeywords.get(i));
          
          if (i < cachedKeywords.size() - 1) {
            sb.append(",");
          }
        }
        
        keywords = sb.toString();
      }
    } catch (MalformedURLException e) {
      LOGGER.warn("site url is incorrect '{}'", siteLocation);
    }
    
    return defaultString(keywords);
  }

  private List<FileData> readFileData(File inputDir) {
    List<FileData> fileData = new ArrayList<>();
    
    for (File file : inputDir.listFiles()) {
      try {
        fileData.add(readFile(file));
      } catch (Exception e) {
        LOGGER.error("unable to read file '{}' because of: {}", file.getAbsolutePath(), ExceptionUtils.getMessage(e));
      }
    }
    
    return fileData;
  }
  
  private FileData readFile(File file) throws IOException {
    LOGGER.info("reading file {}", file.getAbsoluteFile());
    
    String extension = FilenameUtils.getExtension(file.getName());
    
    FileDataReader reader = EnumUtils.getEnum(FileDataReader.class, upperCase(extension));    
    if (reader == null) {
      throw new IllegalStateException("unsupported file type: " + extension);
    }

    return reader.readFileData(file);
  }
  
  private enum FileDataReader {
    CSV {
      @Override
      public FileData readFileData(File file) throws IOException {
        try (CSVParser csvParser = CSVParser.parse(file, Charset.forName("UTF-8"), CSVFormat.DEFAULT)) {
          List<SiteData> siteData = new ArrayList<>();
          
          List<CSVRecord> records = csvParser.getRecords(); 
          // skip csv file header
          for (int i = 1; i < records.size(); i++) {
            CSVRecord record = records.get(i);
            SiteData data = new SiteData();
            
            data.setSiteId(String.valueOf(record.get(0)));
            data.setName(String.valueOf(record.get(1)));
            data.setMobile(BooleanUtils.toBoolean(record.get(2)));
            data.setScore(NumberUtils.toFloat(String.valueOf(record.get(3))));

            siteData.add(data);
          }
          
          FileData fileData = new FileData();
          
          fileData.setFileName(file.getName());
          fileData.setData(siteData);
          
          return fileData;
        }
      }      
    },
    
    JSON {
      
      TypeReference<List<SiteData>> siteDataTypeReference = new TypeReference<List<SiteData>>(){};
      ObjectMapper mapper = new ObjectMapper().setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
      
      @Override
      public FileData readFileData(File file) throws IOException {
        FileData fileData = new FileData();
        
        fileData.setFileName(file.getName());
        fileData.setData(mapper.readValue(file, siteDataTypeReference));
        
        return fileData;
      }      
    };
    
    public abstract FileData readFileData(File file) throws IOException;
  }  
}

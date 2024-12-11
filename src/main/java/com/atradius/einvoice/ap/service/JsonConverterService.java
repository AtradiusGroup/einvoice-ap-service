package com.atradius.einvoice.ap.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class JsonConverterService {
    public <T> T jsonToObject(String json, Class<T> type){
        T result = null;
        try{
            result = new ObjectMapper().readValue(json, type);
        }catch (Exception e){
            log.error("Failed to convert json to type {}", type, e);
        }
        return result;
    }

    public String objectToJson(Object object){
        String result = null;
        try{
            ObjectWriter ow = new ObjectMapper().writer();
            result = ow.writeValueAsString(object);
        }catch (Exception e){
            log.error("Failed converting to json ", e);
        }
        return result;
    }

    public String getStringValue(String json, String field){
        String result = null;
        try{
            Map props = new ObjectMapper().readValue(json, Map.class);
            result = (String)props.get(field);
        }catch (Exception e){
            log.error("Failed to get field {} value from json {}", field, json, e);
        }
        return result;
    }

    public Map getMapValue(String json, String field){
        Map result = null;
        try{
            Map props = new ObjectMapper().readValue(json, Map.class);
            result = (Map)props.get(field);
        }catch (Exception e){
            log.error("Failed to get field {} value from json {}", field, json, e);
        }
        return result;
    }

    public List getListValue(String json, String field){
        List result = null;
        try{
            Map props = new ObjectMapper().readValue(json, Map.class);
            result = (List)props.get(field);
        }catch (Exception e){
            log.error("Failed to get field {} value from json {}", field, json, e);
        }
        return result;
    }

    public <T> T convertValue(Object value, Class<T> clazz){
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper.convertValue(value, clazz);
    }

    public <T> T readValue(String data, Class<T> type){
        T convertedValue = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            convertedValue = mapper.readValue(data, type);
        }catch (Exception e){
            log.error("failed to read value "+data+" to type {}", e);
        }
        return convertedValue;
    }
}
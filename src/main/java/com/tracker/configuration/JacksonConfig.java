package com.tracker.configuration;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.StdDateFormat;

@Component
public class JacksonConfig implements BeanPostProcessor
{
    private static final Logger logger = LoggerFactory.getLogger(JacksonConfig.class);
    
    private static class StdDateFormatExtension extends StdDateFormat
    {
        private static final long serialVersionUID = 1L;
        
//        FastDateFormat isoFormat = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss'Z'");
        DateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

        @Override
        public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition fieldPosition) {
            return isoFormat.format(date, toAppendTo, fieldPosition);
        }
        
        @Override
        public StdDateFormatExtension clone() {
            return this;
        }
    }
    
    public Object postProcessBeforeInitialization(Object bean, String beanName)
            throws BeansException
    {
        return bean;
    }

    public Object postProcessAfterInitialization(Object bean, String beanName)
            throws BeansException
    {
        if ( bean instanceof MappingJackson2HttpMessageConverter ) {
            MappingJackson2HttpMessageConverter jsonConverter =
                    (MappingJackson2HttpMessageConverter) bean;
            final ObjectMapper objectMapper = jsonConverter.getObjectMapper();
            objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
//            objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"));
            objectMapper.setDateFormat(new StdDateFormatExtension());
            jsonConverter.setObjectMapper(objectMapper);
        }
        return bean;
    }
}

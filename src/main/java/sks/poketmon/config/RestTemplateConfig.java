package sks.poketmon.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();

        // 기존 메시지 컨버터 리스트 가져오기
        List<HttpMessageConverter<?>> messageConverters = new ArrayList<>(restTemplate.getMessageConverters());

        // Jackson JSON 메시지 컨버터 생성 및 설정
        MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();

        // 지원할 미디어 타입 설정
        List<MediaType> supportedMediaTypes = new ArrayList<>();
        supportedMediaTypes.add(MediaType.APPLICATION_JSON);
        supportedMediaTypes.add(new MediaType("application", "json", StandardCharsets.UTF_8));
        supportedMediaTypes.add(new MediaType("text", "json", StandardCharsets.UTF_8));
        supportedMediaTypes.add(new MediaType("application", "*+json", StandardCharsets.UTF_8));

        jsonConverter.setSupportedMediaTypes(supportedMediaTypes);

        // JSON 컨버터를 리스트 맨 앞에 추가 (우선순위 높임)
        messageConverters.add(0, jsonConverter);

        // 설정된 메시지 컨버터 리스트를 RestTemplate에 적용
        restTemplate.setMessageConverters(messageConverters);

        return restTemplate;
    }
}
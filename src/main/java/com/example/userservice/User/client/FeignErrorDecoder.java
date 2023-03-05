package com.example.userservice.User.client;

import feign.FeignException;
import feign.Response;
import feign.codec.DecodeException;
import feign.codec.Decoder;
import feign.codec.ErrorDecoder;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.lang.reflect.Type;

public class FeignErrorDecoder implements ErrorDecoder {
    @Override
    public Exception decode(String methodKey, Response response){
        switch(response.status()){
            case 400:
                break;
            case 404:
                if(methodKey.contains("getOrders")){
                    return new ResponseStatusException(HttpStatus.valueOf(response.status()),"User's orders is empty");
                }
                break;
            default:
                return new Exception(response.reason());
        }
        return null;
    }
}

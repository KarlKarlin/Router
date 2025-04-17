package dev.Authentication.services;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("FILE-PROCESSING")
public interface FileInterface {
}

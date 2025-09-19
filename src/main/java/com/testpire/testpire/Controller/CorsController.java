package com.testpire.testpire.Controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
public class CorsController {

    @RequestMapping(value = "/**", method = RequestMethod.OPTIONS)
    public ResponseEntity<?> handleOptions() {
        return ResponseEntity.ok()
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD, PATCH")
                .header("Access-Control-Allow-Headers", "*")
                .header("Access-Control-Max-Age", "3600")
                .build();
    }
}

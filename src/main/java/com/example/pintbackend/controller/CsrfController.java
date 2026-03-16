/**
 * File: null.java
 * Path: com.example.pintbackend.controller
 * <p>
 * Outline:
 * csrf controller
 * <p>
 * Author: jskt
 */

package com.example.pintbackend.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CsrfController {

    @GetMapping("/csrf-token")
    public void csrfToken(HttpServletRequest request) {
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (csrfToken != null) {
            csrfToken.getToken();
        }
    }
}

/**
 * File: null.java
 * Path: com.example.pintbackend.repository
 * <p>
 * Outline:
 * JPA repo for Images
 * <p>
 * Author: jskt
 */

package com.example.pintbackend.repository;

import com.example.pintbackend.domain.Image;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageRepository extends JpaRepository<Image, Long> {

}

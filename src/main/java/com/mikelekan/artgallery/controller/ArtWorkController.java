package com.mikelekan.artgallery.controller;

import com.mikelekan.artgallery.model.ArtWork;
import com.mikelekan.artgallery.repository.ArtWorkRepository;
import com.mikelekan.artgallery.service.ArtworkService;
import com.mikelekan.artgallery.service.S3Service;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("api/artworks")
public class ArtWorkController
{
    private final ArtworkService artworkService;
    private final S3Service s3Service;
    private final ArtWorkRepository artWorkRepository;

    public ArtWorkController(ArtworkService artworkService, S3Service s3Service,
                             ArtWorkRepository artWorkRepository)
            throws IOException
    {
        this.artworkService = artworkService;
        this.s3Service = s3Service;
        this.artWorkRepository = artWorkRepository;
    }

    @GetMapping
    public List<ArtWork> getArtworks()
    {
        return artworkService.getAllArtworks();
    }

    @PostMapping("/upload")
    public ResponseEntity<ArtWork> uploadArtWork(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam("artist") String artist,
            @RequestParam("price") BigDecimal price,
            @RequestParam("description") String description)
    {
        try
        {
            String imageUrl = s3Service.uploadFile(file);

            ArtWork art = ArtWork.builder()
                    .title(title)
                    .artist(artist)
                    .price(price)
                    .description(description)
                    .imageUrl(imageUrl)
                    .createdAt(LocalDateTime.now())
                    .build();
            ArtWork savedArt = artWorkRepository.save(art);
            return ResponseEntity.ok(savedArt);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteArtWork(@PathVariable Long id)
    {
        return artWorkRepository.findById(id).map(art -> {
            // 1. Delete the physical file from S3
            if (art.getImageUrl() != null && art.getImageUrl().contains("amazonaws.com")) {
                s3Service.deleteFile(art.getImageUrl());
            }

            // 2. Delete the record from the database
            artWorkRepository.delete(art);

            return ResponseEntity.noContent().<Void>build(); // Return 204 No Content
        }).orElse(ResponseEntity.notFound().build()); // Return 404 if ID doesn't exist
    }

}

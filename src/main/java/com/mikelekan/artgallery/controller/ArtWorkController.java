package com.mikelekan.artgallery.controller;

import com.mikelekan.artgallery.dto.ArtworkResponse;
import com.mikelekan.artgallery.model.ArtWork;
import com.mikelekan.artgallery.repository.ArtWorkRepository;
import com.mikelekan.artgallery.service.ArtworkService;
import com.mikelekan.artgallery.service.vendors.S3Service;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("api/artworks")
public class ArtWorkController
{
	private final ArtworkService artworkService;
	private final S3Service s3Service;
	private final ArtWorkRepository artWorkRepository;

	public ArtWorkController(ArtworkService artworkService, S3Service s3Service, ArtWorkRepository artWorkRepository)
			throws IOException {
		this.artworkService = artworkService;
		this.s3Service = s3Service;
		this.artWorkRepository = artWorkRepository;
	}

	@GetMapping
	public List<ArtworkResponse> getArtworks() {
		return artworkService.getAllArtworks().stream()
				.map(art -> ArtworkResponse.builder()
						.id(art.getId())
						.title(art.getTitle())
						.description(art.getDescription())
						.artist(art.getArtist())
						.price(art.getPrice())
						.sold(art.isSold())
						.createdAt(art.getCreatedAt())
						// Generate the URL right here during the mapping!
						.imageUrl(s3Service.getPresignedUrl(art.getImageUrl()))
						.build())
				.collect(Collectors.toList());
	}

	@PostMapping("/upload")
	public ResponseEntity<ArtWork> uploadArtWork(@RequestParam("file") MultipartFile file,
			@RequestParam("title") String title, @RequestParam("artist") String artist,
			@RequestParam("price") BigDecimal price, @RequestParam("description") String description)
	{
		try
		{
			String imageUrl = s3Service.uploadFile(file);

			ArtWork art = ArtWork.builder().title(title).artist(artist).price(price).description(description)
					.imageUrl(imageUrl).createdAt(LocalDateTime.now()).build();
			ArtWork savedArt = artWorkRepository.save(art);
			return ResponseEntity.ok(savedArt);
		} catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteArtWork(@PathVariable Long id)
	{
		return artWorkRepository.findById(id).map(art -> {
			// 1. Delete the physical file from S3
			// Simplified Delete logic in Controller
			if (art.getImageUrl() != null)
			{
				// We just pass the key directly now
				s3Service.deleteFile(art.getImageUrl());
			}

			// 2. Delete the record from the database
			artWorkRepository.delete(art);

			return ResponseEntity.noContent().<Void>build(); // Return 204 No Content
		}).orElse(ResponseEntity.notFound().build()); // Return 404 if ID doesn't exist
	}
}

package com.mikelekan.artgallery.service;

import com.mikelekan.artgallery.model.ArtWork;
import com.mikelekan.artgallery.repository.ArtWorkRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ArtworkServiceImpl implements ArtworkService {

    private final ArtWorkRepository artWorkRepository;

    public ArtworkServiceImpl(ArtWorkRepository artWorkRepository) {
        this.artWorkRepository = artWorkRepository;
    }

    @Override
    public List<ArtWork> getAllArtworks() {
        return artWorkRepository.findAll();
    }

    @Override
    public ArtWork getArtworkById(Long id) {
        return artWorkRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Artwork not found"));
    }

    @Override
    public ArtWork saveArtwork(ArtWork artwork) {
        return artWorkRepository.save(artwork);
    }
}

package com.mikelekan.artgallery.service;

import com.mikelekan.artgallery.model.ArtWork;

import java.util.List;

public interface ArtworkService {
    List<ArtWork> getAllArtworks();
    ArtWork getArtworkById(Long id);
    ArtWork saveArtwork(ArtWork artwork);
}

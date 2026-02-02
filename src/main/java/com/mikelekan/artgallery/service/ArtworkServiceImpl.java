package com.mikelekan.artgallery.service;

import com.mikelekan.artgallery.model.ArtWork;
import com.mikelekan.artgallery.repository.ArtWorkRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ArtworkServiceImpl implements ArtworkService
{

	private final ArtWorkRepository artWorkRepository;

	public ArtworkServiceImpl(ArtWorkRepository artWorkRepository) {
		this.artWorkRepository = artWorkRepository;
	}

	@Override
	public List<ArtWork> getAllArtworks()
	{
		List<ArtWork> list = artWorkRepository.findAll();

		System.out.println("Database found " + list.size() + " artworks.");
		return list;
	}

	@Override
	public ArtWork getArtworkById(Long id)
	{
		return artWorkRepository.findById(id).orElseThrow(() -> new RuntimeException("Artwork not found"));
	}

	@Override
	public ArtWork saveArtwork(ArtWork artwork)
	{
		return artWorkRepository.save(artwork);
	}
}

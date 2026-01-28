// package com.mikelekan.artgallery.config;
//
// import com.mikelekan.artgallery.model.ArtWork;
// import com.mikelekan.artgallery.repository.ArtWorkRepository;
// import org.springframework.boot.CommandLineRunner;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import java.math.BigDecimal;
//
// @Configuration
// public class DataInitializer {
//
// @Bean
// CommandLineRunner initDatabase(ArtWorkRepository repository) {
// return args -> {
// if (repository.count() == 0) {
// // Using the builder (Ensure @Builder is on your Artwork entity)
// ArtWork sample = ArtWork.builder()
// .title("Starry Night")
// .artist("Vincent van Gogh")
// .price(new BigDecimal("1000.00")) // String constructor is safer for
// BigDecimal
// .description("A nice painting")
// .build();
//
// repository.save(sample);
// System.out.println("Data Initialized: Starry Night has been saved.");
// }
// };
// }
// }
//

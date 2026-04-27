package com.bookvault.library.config;

import net.datafaker.Faker;
import com.bookvault.library.model.*;
import com.bookvault.library.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;
    private final GenreRepository genreRepository;
    private final AddressRepository addressRepository;
    private final PublisherRepository publisherRepository;
    private final SeriesRepository seriesRepository;
    private final ReaderRepository readerRepository;
    private final ReviewRepository reviewRepository;

    private final Faker faker = new Faker(new Locale("pl"));

    @Override
    @Transactional
    public void run(String... args) {
        if (authorRepository.count() == 0) {
            System.out.println(">>> ROZPOCZYNAM GENEROWANIE DANYCH TESTOWYCH...");

            List<Genre> genres = seedGenres();
            List<Address> addresses = seedAddresses(15);
            List<Author> authors = seedAuthors(20);
            List<Publisher> publishers = seedPublishers(8, addresses);
            List<Series> seriesList = seedSeries(10, authors);

            // Przekazujemy serie i autorów do książek
            List<Book> books = seedBooks(50, authors, publishers, seriesList, genres);

            List<Reader> readers = seedReaders(15);
            seedReviews(100, books, readers);

            System.out.println(">>> GENEROWANIE DANYCH ZAKOŃCZONE. MIŁEGO TESTOWANIA!");
        }
    }

    private List<Genre> seedGenres() {
        String[] genreNames = {"Fantasy", "Sci-Fi", "Kryminał", "Thriller", "Horror", "Biografia", "Historyczna", "Romans"};
        List<Genre> genres = new ArrayList<>();
        for (String name : genreNames) {
            // Używamy Twojej nowej metody, żeby nie dublować
            Genre g = genreRepository.findByNameIgnoreCase(name)
                    .orElseGet(() -> {
                        Genre newGenre = new Genre();
                        newGenre.setName(name);
                        return genreRepository.save(newGenre);
                    });
            genres.add(g);
        }
        return genres;
    }

    private List<Address> seedAddresses(int count) {
        List<Address> addresses = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Address a = new Address();
            a.setStreet(faker.address().streetName());
            a.setHouseNumber(faker.address().buildingNumber());
            a.setZipCode(faker.address().zipCode());
            a.setCity(faker.address().city());
            a.setCountry("Polska");
            addresses.add(addressRepository.save(a));
        }
        return addresses;
    }

    private List<Author> seedAuthors(int count) {
        List<Author> authors = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Author a = new Author();
            a.setFirstName(faker.name().firstName());
            a.setLastName(faker.name().lastName());
            a.setNationality(faker.nation().nationality());
            a.setBiography(faker.lorem().paragraph(3));
            // Generujemy datę urodzenia (wiek 25-90 lat)
            a.setBirthDate(faker.timeAndDate().birthday(25, 90));
            authors.add(authorRepository.save(a));
        }
        return authors;
    }

    private List<Publisher> seedPublishers(int count, List<Address> addresses) {
        List<Publisher> publishers = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Publisher p = new Publisher();
            p.setName("Wydawnictwo " + faker.book().publisher());
            p.setOwner(faker.name().fullName());
            p.setFoundationYear(faker.number().numberBetween(1945, 2023));
            p.setAddress(addresses.get(faker.random().nextInt(addresses.size())));
            publishers.add(publisherRepository.save(p));
        }
        return publishers;
    }

    private List<Series> seedSeries(int count, List<Author> authors) {
        List<Series> seriesList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Series s = new Series();
            // Lepsze nazwy serii
            String seriesTitle = faker.random().nextBoolean() ? faker.book().genre() : faker.space().constellation();
            s.setName("Kroniki " + seriesTitle);
            s.setVolumeCount((short) faker.number().numberBetween(3, 12));
            s.setAuthor(authors.get(faker.random().nextInt(authors.size())));
            seriesList.add(seriesRepository.save(s));
        }
        return seriesList;
    }

    private List<Book> seedBooks(int count, List<Author> authors, List<Publisher> publishers, List<Series> series, List<Genre> genres) {
        List<Book> books = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Book b = new Book();
            b.setTitle(faker.book().title());
            b.setPublicationYear(faker.number().numberBetween(1990, 2024));
            b.setPageCount(faker.number().numberBetween(120, 1100));
            b.setMood(faker.mood().emotion());
            b.setPublisher(publishers.get(faker.random().nextInt(publishers.size())));

            // LOGIKA SPÓJNOŚCI: Jeśli jest seria, autor musi być ten sam
            if (faker.random().nextInt(100) < 40) { // 40% szans na serię
                Series randomSeries = series.get(faker.random().nextInt(series.size()));
                b.setSeries(randomSeries);
                b.setAuthor(randomSeries.getAuthor()); // Autor z serii
            } else {
                b.setAuthor(authors.get(faker.random().nextInt(authors.size())));
            }

            // Gatunki
            Set<Genre> bookGenres = new HashSet<>();
            int numGenres = faker.number().numberBetween(1, 4);
            while (bookGenres.size() < numGenres) {
                bookGenres.add(genres.get(faker.random().nextInt(genres.size())));
            }
            b.setGenres(bookGenres);

            books.add(bookRepository.save(b));
        }
        return books;
    }

    private List<Reader> seedReaders(int count) {
        List<Reader> readers = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Reader r = new Reader();
            r.setFirstName(faker.name().firstName());
            r.setLastName(faker.name().lastName());
            r.setNationality("Polska");
            r.setBirthDate(faker.timeAndDate().birthday(25, 90));
            readers.add(readerRepository.save(r));
        }
        return readers;
    }

    private void seedReviews(int count, List<Book> books, List<Reader> readers) {
        for (int i = 0; i < count; i++) {
            Review r = new Review();
            r.setRating(faker.number().numberBetween(1, 6)); // Skala 1-5
            r.setContent(faker.lorem().paragraph(2));
            r.setBook(books.get(faker.random().nextInt(books.size())));
            r.setReader(readers.get(faker.random().nextInt(readers.size())));
            reviewRepository.save(r);
        }
    }
}
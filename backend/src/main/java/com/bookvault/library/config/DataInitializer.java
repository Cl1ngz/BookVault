package com.bookvault.library.config;

import net.datafaker.Faker;
import com.bookvault.library.model.*;
import com.bookvault.library.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Component
@RequiredArgsConstructor // Automatycznie wygeneruje konstruktor dla wszystkich pól final
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
    @Transactional // Zapewnia, że wszystkie operacje wykonają się w jednej transakcji
    public void run(String... args) {
        // Sprawdzamy czy baza jest pusta (patrzymy na autorów jako punkt startowy)
        if (authorRepository.count() == 0) {
            System.out.println(">>> Rozpoczynam generowanie danych testowych (DataFaker)...");

            // 1. GATUNKI (Stała lista)
            List<Genre> genres = seedGenres();

            // 2. ADRESY
            List<Address> addresses = seedAddresses(10);

            // 3. AUTORZY
            List<Author> authors = seedAuthors(15);

            // 4. WYDAWNICTWA (Potrzebują adresów)
            List<Publisher> publishers = seedPublishers(5, addresses);

            // 5. SERIE (Potrzebują autorów)
            List<Series> seriesList = seedSeries(5, authors);

            // 6. KSIĄŻKI (Potrzebują autorów, wydawnictw, serii i gatunków)
            List<Book> books = seedBooks(30, authors, publishers, seriesList, genres);

            // 7. CZYTELNICY
            List<Reader> readers = seedReaders(10);

            // 8. RECENZJE (Potrzebują książek i czytelników)
            seedReviews(50, books, readers);

            System.out.println(">>> Proces generowania danych zakończony pomyślnie!");
        }
    }

    private List<Genre> seedGenres() {
        String[] genreNames = {"Fantasy", "Sci-Fi", "Kryminał", "Thriller", "Horror", "Biografia", "Historyczna"};
        List<Genre> genres = new ArrayList<>();
        for (String name : genreNames) {
            Genre g = new Genre();
            g.setName(name);
            genres.add(genreRepository.save(g));
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
            a.setBiography(faker.lorem().paragraph());
            authors.add(authorRepository.save(a));
        }
        return authors;
    }

    private List<Publisher> seedPublishers(int count, List<Address> addresses) {
        List<Publisher> publishers = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Publisher p = new Publisher();
            p.setName(faker.book().publisher()); // POPRAWIONE
            p.setOwner(faker.name().fullName());
            p.setFoundationYear(faker.number().numberBetween(1950, 2020));
            p.setAddress(addresses.get(faker.random().nextInt(addresses.size())));
            publishers.add(publisherRepository.save(p));
        }
        return publishers;
    }

    private List<Series> seedSeries(int count, List<Author> authors) {
        List<Series> seriesList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Series s = new Series();
            s.setName("Saga " + faker.funnyName().name());
            s.setVolumeCount((short) faker.number().numberBetween(3, 10)); // POPRAWIONE RZUTOWANIE
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
            b.setPageCount(faker.number().numberBetween(150, 900));
            b.setMood(faker.mood().emotion());

            b.setAuthor(authors.get(faker.random().nextInt(authors.size())));
            b.setPublisher(publishers.get(faker.random().nextInt(publishers.size())));

            // Opcjonalne przypisanie do serii (50% szans)
            if (faker.random().nextBoolean()) {
                b.setSeries(series.get(faker.random().nextInt(series.size())));
            }

            // Losowe gatunki (od 1 do 3)
            Set<Genre> bookGenres = new HashSet<>();
            int numGenres = faker.number().numberBetween(1, 3);
            for(int j=0; j<numGenres; j++) {
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
            readers.add(readerRepository.save(r));
        }
        return readers;
    }

    private void seedReviews(int count, List<Book> books, List<Reader> readers) {
        for (int i = 0; i < count; i++) {
            Review r = new Review();
            r.setRating(faker.number().numberBetween(1, 5));
            r.setContent(faker.lorem().sentence());
            r.setBook(books.get(faker.random().nextInt(books.size())));
            r.setReader(readers.get(faker.random().nextInt(readers.size())));
            reviewRepository.save(r);
        }
    }
}
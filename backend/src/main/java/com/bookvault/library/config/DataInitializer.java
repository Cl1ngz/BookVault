package com.bookvault.library.config;

import com.bookvault.library.model.*;
import com.bookvault.library.repository.*;
import lombok.RequiredArgsConstructor;
import net.datafaker.Faker;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;
    private final GenreRepository genreRepository;
    private final PublisherRepository publisherRepository;
    private final SeriesRepository seriesRepository;
    private final ReaderRepository readerRepository;
    private final ReviewRepository reviewRepository;
    private final PasswordEncoder passwordEncoder;

    private final Faker faker = new Faker(new Locale("en"));

    @Override
    @Transactional
    public void run(String... args) {
        System.out.println(">>> BEGINNING THE DATABASE AUDIT ...");

        List<Genre> genres = seedGenres();
        List<Author> authors = seedAuthors(24);
        List<Publisher> publishers = seedPublishers(11);
        List<Series> seriesList = seedSeries(15, authors);
        seedBooks(1234, authors, publishers, seriesList, genres);

        seedReaders(60);
        seedReviews(5000);


        System.out.println(">>> DATA SYNCHRONIZATION COMPLETE!");
    }

    private List<Genre> seedGenres() {
        // if (genreRepository.count() > 0) return genreRepository.findAll();
        System.out.println("-> Generating generes...");
        String[] genreNames = {
                // Fiction
                "Fantasy", "Science Fiction", "Romance", "Horror", "Thriller",
                "Mystery", "Crime", "Historical Fiction", "Adventure", "Literary Fiction",
                "Contemporary Fiction", "Magical Realism", "Dystopian", "Speculative Fiction",
                "Paranormal", "Urban Fantasy", "Epic Fantasy", "Dark Fantasy", "Space Opera",
                "Cyberpunk", "Steampunk", "Alternate History", "Satire", "Humor", "Drama",
                "Coming of Age", "Women's Fiction", "Chick Lit", "Fairy Tale", "Mythology",
                "Short Stories", "Anthology",
                // Non-fiction
                "Biography", "Autobiography", "Memoir", "Self-Help", "Psychology",
                "Philosophy", "History", "Politics", "True Crime", "Science",
                "Popular Science", "Technology", "Business", "Economics", "Travel",
                "Food & Cooking", "Art", "Music", "Sports", "Health & Wellness",
                "Parenting", "Religion & Spirituality", "Essays",
                // YA / Children
                "Young Adult", "Middle Grade", "Children's", "Picture Book",
                // Other
                "Graphic Novel", "Manga", "Poetry", "Play / Drama"
        };
        List<Genre> allGenres = genreRepository.findAll();
        Set<String> existingNames = allGenres.stream().map(Genre::getName).collect(Collectors.toSet());

        for (String name : genreNames) {
            if (!existingNames.contains(name)) {
                Genre g = new Genre();
                g.setName(name);
                allGenres.add(genreRepository.save(g));
            }
        }
        return allGenres;
    }

    private List<Author> seedAuthors(int count) {
        // if (authorRepository.count() > 0) return authorRepository.findAll();
        System.out.println("-> Generating authors...");
        List<Author> allAuthors = authorRepository.findAll();
        Set<String> existingEmails = allAuthors.stream().map(Author::getEmail).filter(Objects::nonNull).collect(Collectors.toSet());
        int authorsToCreate = count - (int) allAuthors.stream().filter(a -> a.getEmail() != null && a.getEmail().startsWith("author")).count();


        if (authorsToCreate > 0) {
            for (int i = 0; i < count; i++) {
                String email = "author" + i + "@bookvault.test";
                if (!existingEmails.contains(email)) {
                    Author a = new Author();
                    a.setFirstName(faker.name().firstName());
                    a.setLastName(faker.name().lastName());
                    a.setNationality(faker.nation().nationality());
                    a.setBiography(faker.lorem().paragraph(3));
                    a.setBirthDate(faker.timeAndDate().birthday(25, 90));
                    a.setEmail(email);
                    a.setPasswordHash(passwordEncoder.encode("author123"));
                    allAuthors.add(authorRepository.save(a));
                }
            }
        }
        return allAuthors;
    }

    private List<Publisher> seedPublishers(int count) {
        // if (publisherRepository.count() > 0) {
        //     return publisherRepository.findAll();
        // }
        System.out.println("-> Generowanie wydawnictw (uproszczone, bez adresów)...");
        List<Publisher> allPublishers = publisherRepository.findAll();
        int publishersToCreate = count - allPublishers.size();

        if (publishersToCreate > 0) {
            for (int i = 0; i < publishersToCreate; i++) {
                Publisher p = new Publisher();
                p.setName("Wydawnictwo " + faker.book().publisher());
                p.setOwner(faker.name().fullName());
                p.setFoundationYear(faker.number().numberBetween(1945, 2024));
                allPublishers.add(publisherRepository.save(p));
            }
        }
        return allPublishers;
    }

    private List<Series> seedSeries(int count, List<Author> authors) {
        // if (seriesRepository.count() > 0) return seriesRepository.findAll();
        System.out.println("-> Generating series...");
        List<Series> allSeries = seriesRepository.findAll();
        int seriesToCreate = count - allSeries.size();

        if (seriesToCreate > 0 && !authors.isEmpty()) {
            for (int i = 0; i < seriesToCreate; i++) {
                Series s = new Series();
                String seriesTitle = faker.random().nextBoolean() ? faker.book().genre() : faker.space().constellation();
                s.setName("Kroniki " + seriesTitle);
                s.setVolumeCount((short) faker.number().numberBetween(3, 12));
                s.setAuthor(authors.get(faker.random().nextInt(authors.size())));
                allSeries.add(seriesRepository.save(s));
            }
        }
        return allSeries;
    }

    private List<Book> seedBooks(int count, List<Author> authors, List<Publisher> publishers, List<Series> series, List<Genre> genres) {
        // if (bookRepository.count() > 0) return bookRepository.findAll();
        System.out.println("-> Generating books...");
        List<Book> allBooks = bookRepository.findAll();
        int booksToCreate = count - allBooks.size();

        if (booksToCreate > 0 && !authors.isEmpty() && !publishers.isEmpty() && !genres.isEmpty()) {
            for (int i = 0; i < booksToCreate; i++) {
                Book b = new Book();
                b.setTitle(faker.book().title());
                b.setPublicationYear(faker.number().numberBetween(1990, 2024));
                b.setPageCount(faker.number().numberBetween(120, 1100));
                b.setMood(faker.mood().emotion());
                b.setPublisher(publishers.get(faker.random().nextInt(publishers.size())));

                if (!series.isEmpty() && faker.random().nextInt(100) < 40) {
                    Series randomSeries = series.get(faker.random().nextInt(series.size()));
                    b.setSeries(randomSeries);
                    b.setAuthor(randomSeries.getAuthor());
                } else {
                    b.setAuthor(authors.get(faker.random().nextInt(authors.size())));
                }

                Set<Genre> bookGenres = new HashSet<>();
                int numGenres = faker.number().numberBetween(1, 4);
                while (bookGenres.size() < numGenres) {
                    bookGenres.add(genres.get(faker.random().nextInt(genres.size())));
                }
                b.setGenres(bookGenres);
                allBooks.add(bookRepository.save(b));
            }
        }
        return allBooks;
    }

    private List<Reader> seedReaders(int count) {
        System.out.println("-> Generating readers...");
        List<Reader> allReaders = readerRepository.findAll();
        Set<String> existingEmails = allReaders.stream().map(Reader::getEmail).filter(Objects::nonNull).collect(Collectors.toSet());

        for (int i = 0; i < count; i++) {
            String email = "reader" + i + "@bookvault.test";
            if (!existingEmails.contains(email)) {
                Reader r = new Reader();
                r.setUsername(faker.internet().username() + faker.number().numberBetween(10, 999));
                r.setNationality("Polska");
                r.setBirthDate(faker.timeAndDate().birthday(18, 75));
                r.setEmail(email);
                r.setPasswordHash(passwordEncoder.encode("user123"));
                r.setRole("USER");
                allReaders.add(readerRepository.save(r));
            }
        }

        System.out.println("Test Readers have password: user123");
        return allReaders;
    }

    private void seedReviews(int count) {
        // if (reviewRepository.count() > 0) return;

        List<Book> books = bookRepository.findAll();
        List<Reader> readers = readerRepository.findAll();

        if (books.isEmpty() || readers.isEmpty()) return;

        System.out.println("-> Generating reviews...");
        long reviewsToCreate = count - reviewRepository.count();

        if (reviewsToCreate > 0) {
            Double[] ratingOptions = new Double[20];
            for (int i = 0; i < 20; i++) {
                ratingOptions[i] = (i + 1) * 0.25;
            }

            for (int i = 0; i < reviewsToCreate; i++) {
                Review r = new Review();
                r.setRating(faker.options().option(ratingOptions));
                r.setContent(faker.lorem().paragraph(2));
                r.setBook(books.get(faker.random().nextInt(books.size())));
                r.setReader(readers.get(faker.random().nextInt(readers.size())));
                reviewRepository.save(r);
            }
        }
    }
}

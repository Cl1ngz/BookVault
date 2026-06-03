package com.bookvault.library.config;

import net.datafaker.Faker;
import com.bookvault.library.model.*;
import com.bookvault.library.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

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

        seedGenres();

        List<Author> authors = seedAuthors(20);
        List<Publisher> publishers = seedPublishers(8);
        List<Series> seriesList = seedSeries(10, authors);
        seedBooks(50, authors, publishers, seriesList, genreRepository.findAll());

        seedReaders(15);
        seedReviews(100);


        System.out.println(">>> DATA SYNCHRONIZATION COMPLETE!");
    }

    private List<Genre> seedGenres() {
        if (genreRepository.count() > 0) return genreRepository.findAll();
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
        List<Genre> list = new ArrayList<>();
        for (String name : genreNames) {
            Genre g = new Genre();
            g.setName(name);
            list.add(genreRepository.save(g));
        }
        return list;
    }

    private List<Author> seedAuthors(int count) {
        if (authorRepository.count() > 0) return authorRepository.findAll();
        System.out.println("-> Generating authors...");
        List<Author> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Author a = new Author();
            a.setFirstName(faker.name().firstName());
            a.setLastName(faker.name().lastName());
            a.setNationality(faker.nation().nationality());
            a.setBiography(faker.lorem().paragraph(3));
            a.setBirthDate(faker.timeAndDate().birthday(25, 90));
            a.setEmail("author" + i + "@bookvault.test");
            a.setPasswordHash(passwordEncoder.encode("author123"));
            list.add(authorRepository.save(a));
        }
        return list;
    }

    private List<Publisher> seedPublishers(int count) {
        if (publisherRepository.count() > 0) {
            return publisherRepository.findAll();
        }

        System.out.println("-> Generowanie wydawnictw (uproszczone, bez adresów)...");
        List<Publisher> list = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            Publisher p = new Publisher();

            p.setName("Wydawnictwo " + faker.book().publisher());
            p.setOwner(faker.name().fullName());
            p.setFoundationYear(faker.number().numberBetween(1945, 2024));
            list.add(publisherRepository.save(p));
        }

        return list;
    }

    private List<Series> seedSeries(int count, List<Author> authors) {
        if (seriesRepository.count() > 0) return seriesRepository.findAll();
        System.out.println("-> Generating series...");
        List<Series> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Series s = new Series();
            String seriesTitle = faker.random().nextBoolean() ? faker.book().genre() : faker.space().constellation();
            s.setName("Kroniki " + seriesTitle);
            s.setVolumeCount((short) faker.number().numberBetween(3, 12));
            s.setAuthor(authors.get(faker.random().nextInt(authors.size())));
            list.add(seriesRepository.save(s));
        }
        return list;
    }

    private List<Book> seedBooks(int count, List<Author> authors, List<Publisher> publishers, List<Series> series, List<Genre> genres) {
        if (bookRepository.count() > 0) return bookRepository.findAll();
        System.out.println("-> Generating books...");
        List<Book> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
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
            list.add(bookRepository.save(b));
        }
        return list;
    }

    private List<Reader> seedReaders(int count) {
        if (readerRepository.count() > 0) return readerRepository.findAll();

        System.out.println("-> Generating readers...");
        List<Reader> list = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            Reader r = new Reader();

            r.setUsername(faker.internet().username() + faker.number().numberBetween(10, 999));
            r.setNationality("Polska");
            r.setBirthDate(faker.timeAndDate().birthday(18, 75));
            r.setEmail("reader" + i + "@bookvault.test");

            // shared passwword for fakckergenerated users
            r.setPasswordHash(passwordEncoder.encode("user123"));

            r.setRole("USER");

            list.add(readerRepository.save(r));
        }

        System.out.println("Test Readers have password: user123");
        return list;
    }

    private void seedReviews(int count) {
        if (reviewRepository.count() > 0) return;

        List<Book> books = bookRepository.findAll();
        List<Reader> readers = readerRepository.findAll();

        if (books.isEmpty() || readers.isEmpty()) return;

        System.out.println("-> Generating reviews...");

        for (int i = 0; i < count; i++) {
            Review r = new Review();
            r.setRating(Math.round(faker.number().randomDouble(2, 25, 500)) / 100.0);
            r.setContent(faker.lorem().paragraph(2));
            r.setBook(books.get(faker.random().nextInt(books.size())));
            r.setReader(readers.get(faker.random().nextInt(readers.size())));
            reviewRepository.save(r);
        }
    }
}
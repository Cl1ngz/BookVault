# Input Validation Improvements - BookVault

## Overview

This document outlines the comprehensive input validation enhancements applied to the BookVault application for creating and managing books, series, authors, publishers, and genres.

## Backend Validation (DTOs)

### BookDto & BookUpdateDto

**Required Fields:**
- `title`: Must be 2-255 characters, @NotBlank

**Constraints:**
- `publicationYear`: 1000-2100 (realistic publication range)
- `pageCount`: 1-10000 pages
- `mood`: 2-50 characters (when provided)
- `genreIds`: Empty set allowed (optional genres)

**Rationale:**
- Minimum year of 1000 excludes unrealistic dates
- Page count range covers all realistic books
- Mood field requires meaningful input (not single characters)

### AuthorDto

**Required Fields:**
- `firstName`: 2-100 characters, @NotBlank
- `lastName`: 2-100 characters, @NotBlank

**Constraints:**
- `birthDate`: Must be in the past (@Past constraint)
- `nationality`: 2-50 characters (when provided)
- `biography`: 10-5000 characters (meaningful content)
- `email`: Valid email format, max 255 chars (optional)

**Rationale:**
- Names require minimum 2 characters to prevent single-letter entries
- Biography has minimum 10 chars to ensure substantive content
- Email validation prevents invalid formats

### SeriesDto & SeriesUpdateDto

**Required Fields:**
- `name`: 2-150 characters, @NotBlank
- `volumeCount`: @NotNull (required), 1-1000 volumes

**Rationale:**
- Volume count now required for data consistency
- Range covers single volumes to extensive series

### GenreDto

**Required Fields:**
- `name`: 2-100 characters, @NotBlank

**Rationale:**
- Consistent with other entity naming requirements

### PublisherDto

**Required Fields:**
- `name`: 2-150 characters, @NotBlank

**Constraints:**
- `foundationYear`: 1400-2100 (realistic publishing industry timeline)
- `owner`: 2-150 characters (when provided)

**Rationale:**
- Publishing industry didn't exist before ~1400
- Owner field requires meaningful input

### ReviewDto

**Required Fields:**
- `bookId`: @NotNull
- `rating`: @NotNull, 0.25-5.0 scale

**Constraints:**
- `content`: 10-5000 characters (meaningful reviews only)

**Rationale:**
- Minimum 10 characters ensures substantive feedback
- Prevents spam reviews with minimal text

## Frontend Validation (Vue.js - ModeratorView)

### Real-Time Validation Features

1. **Character Length Display**
   - Show current/maximum character count
   - Visual feedback as user types

2. **Required Field Indicators**
   - Asterisk (*) for mandatory fields
   - Clear labels

3. **Input Type Constraints**
   - Number inputs with min/max attributes
   - Email inputs with type validation
   - Text areas with character limits

4. **Error Messages**
   - Clear, user-friendly error feedback
   - Specific validation failure reasons
   - Live validation as user corrects input

### Form Sections Enhanced

#### Add Book Form
- Title: Required, 2-255 chars
- Publication Year: Optional, 1000-2100
- Page Count: Optional, 1-10000
- Mood: Optional, 2-50 chars
- Genres: Multi-select checkboxes

#### Add Author Form
- First Name: Required, 2-100 chars
- Last Name: Required, 2-100 chars
- Birth Date: Optional, must be past date
- Nationality: Optional, 2-50 chars
- Biography: Optional, 10-5000 chars
- Email: Optional, valid email format

#### Add Series Form
- Name: Required, 2-150 chars
- Volume Count: Required, 1-1000
- Author: Optional dropdown selection

#### Add Genre Form
- Name: Required, 2-100 chars

#### Add Publisher Form
- Name: Required, 2-150 chars
- Foundation Year: Optional, 1400-2100
- Owner: Optional, 2-150 chars

## Server-Side Validation (Spring Boot)

### Validation Framework

Using Jakarta Validation (formerly Bean Validation) with annotations:
- `@NotBlank`: String cannot be null or whitespace
- `@NotNull`: Field must not be null
- `@Size(min, max)`: String/Collection size constraints
- `@Min/@Max`: Numeric range constraints
- `@Email`: Valid email format
- `@Past`: Date must be in the past
- `@PastOrPresent`: Date must not be future
- `@DecimalMin/@DecimalMax`: Decimal number constraints

### Error Handling

Controllers use `@Valid` annotation on DTOs:
```java
@PostMapping
public ResponseEntity<?> createBook(@Valid @RequestBody BookDto bookDto) {
    return bookService.createBook(bookDto);
}
```

Spring automatically:
1. Validates all constraints
2. Returns 400 Bad Request if validation fails
3. Includes error messages in response body

### Exception Handling

Services throw `IllegalArgumentException` for:
- Referenced entities not found (author, publisher, series, genre IDs)
- Business logic violations

## Duplicate Checking

### Current Implementation

Database constraints should be added for:
- Genre names (unique)
- Publisher names (unique)
- Series names (unique per author, or globally unique)
- Author full names (unique)

### Recommended Additions

```java
// In repository interfaces
public interface GenreRepository extends JpaRepository<Genre, Integer> {
    Optional<Genre> findByNameIgnoreCase(String name);
}

public interface PublisherRepository extends JpaRepository<Publisher, Integer> {
    Optional<Publisher> findByNameIgnoreCase(String name);
}

public interface SeriesRepository extends JpaRepository<Series, Integer> {
    Optional<Series> findByNameIgnoreCaseAndAuthor(String name, Author author);
}

public interface AuthorRepository extends JpaRepository<Author, Integer> {
    Optional<Author> findByFirstNameIgnoreCaseAndLastNameIgnoreCase(String firstName, String lastName);
}
```

### Service-Level Validation

```java
// Example: Check for duplicate genre before creation
if (genreRepository.findByNameIgnoreCase(genreDto.getName()).isPresent()) {
    throw new IllegalArgumentException("Genre '" + genreDto.getName() + "' already exists");
}
```

## Frontend User Experience

### Immediate Feedback
1. Real-time character counters
2. Validation error messages appear as user corrects
3. Submit button enabled only when form is valid
4. Visual indicators for required fields

### Error Message Examples
- "Title must be 2-255 characters (currently 1)"
- "Year must be between 1000 and 2100"
- "Invalid email format"
- "This genre already exists"
- "Author not found"

## Testing Checklist

### Backend Tests
- [ ] Valid DTOs pass validation
- [ ] Invalid DTOs rejected with 400 status
- [ ] Error messages are meaningful
- [ ] Database constraints enforced
- [ ] Duplicate entities prevented

### Frontend Tests
- [ ] Character counters update correctly
- [ ] Required field indicators visible
- [ ] Error messages display on validation failure
- [ ] Forms cannot be submitted with invalid data
- [ ] Successful submissions clear form

### Integration Tests
- [ ] End-to-end form submission flow
- [ ] Error recovery workflow
- [ ] Editing entities with validation
- [ ] Relationship validation (author/publisher IDs)

## Best Practices

1. **Always validate on both frontend and backend**
   - Frontend: Fast user feedback
   - Backend: Security and data integrity

2. **Provide clear error messages**
   - Tell users exactly what's wrong
   - Suggest how to fix the issue

3. **Use standard validators**
   - Jakarta Validation framework is robust
   - Reduces custom validation code

4. **Implement duplicate checking**
   - Prevent duplicate names across entities
   - Use case-insensitive queries

5. **Keep validation rules consistent**
   - Frontend and backend must match
   - Update documentation when rules change

## Future Enhancements

1. **Async validation** - Check duplicates as user types
2. **Autocomplete** - Suggest existing authors/publishers
3. **Bulk upload** - CSV validation and import
4. **Field dependencies** - Conditional validation rules
5. **Internationalization** - Localized error messages

package ru.hh.school.unittesting.homework;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LibraryManagerTest {

  @Mock
  private NotificationService notificationService;

  @Mock
  private UserService userService;

  @InjectMocks
  private LibraryManager libraryManager;

  // Arrange
  @BeforeEach
  void setUp() {
    libraryManager.addBook("1984", 5);
    libraryManager.addBook("To Kill a Mockingbird", 3);
    libraryManager.addBook("The Great Gatsby", 4);
    libraryManager.addBook("Pride and Prejudice", 6);
    libraryManager.addBook("Moby Dick", 2);
    libraryManager.addBook("War and Peace", 4);
    libraryManager.addBook("The Catcher in the Rye", 1);
    libraryManager.addBook("The Hobbit", 1);
    libraryManager.addBook("Fahrenheit 451", 1);
    libraryManager.addBook("The Lord of the Rings", 1);

    when(userService.isUserActive("Daniel K.")).thenReturn(true);
    libraryManager.borrowBook("The Catcher in the Rye", "Daniel K.");

    when(userService.isUserActive("Sophia H.")).thenReturn(true);
    libraryManager.borrowBook("The Hobbit", "Sophia H.");

    when(userService.isUserActive("James T.")).thenReturn(true);
    libraryManager.borrowBook("Fahrenheit 451", "James T.");

    when(userService.isUserActive("Olivia R.")).thenReturn(true);
    libraryManager.borrowBook("The Lord of the Rings", "Olivia R.");
  }

  @Test
  void addAlreadyPresentedBookTest() {
    // Act
    libraryManager.addBook("1984", 2);

    // Assert
    assertEquals(7, libraryManager.getAvailableCopies("1984"));
  }

  @Test
  void addNotPresentedBookTest() {
    // Act
    libraryManager.addBook("Some book", 2);

    // Assert
    assertEquals(2, libraryManager.getAvailableCopies("Some book"));
  }

  @Test
  void borrowBookWithInactiveUserTest() {
    // Arrange
    when(userService.isUserActive("John D.")).thenReturn(false);

    // Act
    String bookId = "1984";
    boolean result = libraryManager.borrowBook(bookId, "John D.");

    // Assert
    assertFalse(result);
    assertEquals(5, libraryManager.getAvailableCopies(bookId));
    verify(notificationService, times(1))
      .notifyUser("John D.", "Your account is not active.");
  }

  @Test
  void borrowBookWithActiveUserAndZeroAvailableCopiesTest() {
    // Arrange
    when(userService.isUserActive("Sarah L.")).thenReturn(true);

    // Act
    String bookId = "The Hobbit";
    boolean result = libraryManager.borrowBook(bookId, "Sarah L.");

    // Assert
    assertEquals(0, libraryManager.getAvailableCopies(bookId));
    assertFalse(result);
  }

  @Test
  void borrowBookWithActiveUserAndNonZeroAvailableCopiesTest() {
    // Arrange
    when(userService.isUserActive("Sarah L.")).thenReturn(true);

    // Act
    String bookId = "War and Peace";
    boolean result = libraryManager.borrowBook(bookId, "Sarah L.");

    // Assert
    assertTrue(result);
    assertEquals(3, libraryManager.getAvailableCopies(bookId));
    verify(notificationService, times(1))
        .notifyUser("Sarah L.", "You have borrowed the book: " + bookId);
  }

  @Test
  void returnUnborrowedBookTest() {
    // Act
    String bookId = "Moby Dick";
    boolean result = libraryManager.returnBook(bookId, "Daniel K.");

    // Assert
    assertFalse(result);
    assertEquals(2, libraryManager.getAvailableCopies(bookId));
  }

  @Test
  void returnBorrowedBookButWithWrongUserTest() {
    // Act
    String bookId = "To Kill a Mockingbird";
    boolean result = libraryManager.returnBook(bookId, "Sophia H.");

    // Assert
    assertFalse(result);
    assertEquals(3, libraryManager.getAvailableCopies(bookId));
  }

  @Test
  void returnBorrowedBook() {
    // Act
    String bookId = "The Catcher in the Rye";
    boolean result = libraryManager.returnBook(bookId, "Daniel K.");

    // Assert
    assertTrue(result);
    assertEquals(1, libraryManager.getAvailableCopies(bookId));
    verify(notificationService, times(1))
        .notifyUser("Daniel K.", "You have returned the book: " + bookId);
  }

  @ParameterizedTest
  @CsvSource({
      "Moby Dick, 2",
      "War and Peace, 4",
      "The Catcher in the Rye, 0",
      "Some random book, 0"
  })
  void getAvailableCopiesTest(String bookId, int quantity) {
    // Act
    int actual = libraryManager.getAvailableCopies(bookId);

    // Assert
    assertEquals(quantity, actual);
  }

  @Test
  void calculateDynamicLateFeeThrowsIllegalArgumentExceptionTest() {
    assertThrows(IllegalArgumentException.class,
        () -> libraryManager.calculateDynamicLateFee(-1, true, false));
  }

  @ParameterizedTest
  @CsvSource({
      "0, true, false, 0",
      "2, true, true, 1.2",
      "3, false, true, 1.2",
      "1, false, false, 0.5"
  })
  void calculateDynamicLateFeeTest(int overdueDays, boolean isBestseller, boolean isPremiumMember, double expectedLateFee) {
    // Act
    double actualLateFee = libraryManager.calculateDynamicLateFee(overdueDays, isBestseller, isPremiumMember);
    // Assert
    assertEquals(actualLateFee, expectedLateFee);
  }
}
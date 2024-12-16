package ru.hh.school.unittesting.homework;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LibraryManagerTest {

  @Mock
  private NotificationService notificationService;

  @Mock
  private UserService userService;

  @InjectMocks
  private LibraryManager libraryManager;

  private Map<String, Integer> bookInventory;

  // Arrange
  @BeforeEach
  void setUp() {
    try {
      Field bookInventoryField = libraryManager.getClass().getDeclaredField("bookInventory");
      bookInventoryField.setAccessible(true);
      bookInventory = (Map<String, Integer>) bookInventoryField.get(libraryManager);

      // Данные честно сгенерировал через GPT, было лень придумывать)))

      bookInventory.put("1984", 5);
      bookInventory.put("To Kill a Mockingbird", 3);
      bookInventory.put("The Great Gatsby", 4);
      bookInventory.put("Pride and Prejudice", 6);
      bookInventory.put("Moby Dick", 2);
      bookInventory.put("War and Peace", 1);
      bookInventory.put("The Catcher in the Rye", 0);
      bookInventory.put("The Hobbit", 0);
      bookInventory.put("Fahrenheit 451", 0);
      bookInventory.put("The Lord of the Rings", 0);

      Field borrowedBooksField = libraryManager.getClass().getDeclaredField("borrowedBooks");
      borrowedBooksField.setAccessible(true);
      Map<String, String> borrowedBooks = (Map<String, String>) borrowedBooksField.get(libraryManager);

      borrowedBooks.put("The Catcher in the Rye", "Daniel K.");
      borrowedBooks.put("The Hobbit", "Sophia H.");
      borrowedBooks.put("Fahrenheit 451", "James T.");
      borrowedBooks.put("The Lord of the Rings", "Olivia R.");

    } catch (NoSuchFieldException | IllegalAccessException e) {
      e.printStackTrace();
    }
  }

  @Test
  void addAlreadyPresentedBookTest() {
    // Act
    libraryManager.addBook("1984", 2);

    // Assert
    assertEquals(7, bookInventory.get("1984"));
    assertEquals(10, bookInventory.size());
  }

  @Test
  void addNotPresentedBookTest() {
    // Act
    libraryManager.addBook("Some book", 2);

    // Assert
    assertEquals(2, bookInventory.get("Some book"));
    assertEquals(11, bookInventory.size());
  }

  @Test
  void borrowBookWithInactiveUserTest() {
    // Arrange
    when(userService.isUserActive("John D.")).thenReturn(false);

    // Act
    boolean result = libraryManager.borrowBook("1984", "John D.");

    // Assert
    assertFalse(result);
  }

  @Test
  void borrowBookWithActiveUserAndZeroAvailableCopiesTest() {
    // Arrange
    when(userService.isUserActive("Sarah L.")).thenReturn(true);

    // Act
    boolean result = libraryManager.borrowBook("The Hobbit", "Sarah L.");

    // Assert
    assertFalse(result);
  }

  @Test
  void borrowBookWithActiveUserAndNonZeroAvailableCopiesTest() {
    // Arrange
    when(userService.isUserActive("Sarah L.")).thenReturn(true);

    // Act
    boolean result = libraryManager.borrowBook("War and Peace", "Sarah L.");

    // Assert
    assertTrue(result);
  }

  @Test
  void returnUnborrowedBookTest() {
    // Act
    boolean result = libraryManager.returnBook("Moby Dick", "Daniel K.");

    // Assert
    assertFalse(result);
  }

  @Test
  void returnBorrowedBookButWithWrongUserTest() {
    // Act
    boolean result = libraryManager.returnBook("The Catcher in the Rye", "Sophia H.");

    // Assert
    assertFalse(result);
  }

  @Test
  void returnBorrowedBook() {
    // Act
    boolean result = libraryManager.returnBook("The Catcher in the Rye", "Daniel K.");

    // Assert
    assertTrue(result);
  }

  @ParameterizedTest
  @CsvSource({
      "Moby Dick, 2",
      "War and Peace, 1",
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
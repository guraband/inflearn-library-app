package com.group.libraryapp.service.book

import com.group.libraryapp.domain.book.Book
import com.group.libraryapp.domain.book.BookRepository
import com.group.libraryapp.domain.book.BookType
import com.group.libraryapp.domain.user.User
import com.group.libraryapp.domain.user.UserRepository
import com.group.libraryapp.domain.user.loanhistory.UserLoanHistory
import com.group.libraryapp.domain.user.loanhistory.UserLoanHistoryRepository
import com.group.libraryapp.domain.user.loanhistory.UserLoanStatus
import com.group.libraryapp.dto.book.request.BookLoanRequest
import com.group.libraryapp.dto.book.request.BookRequest
import com.group.libraryapp.dto.book.request.BookReturnRequest
import com.group.libraryapp.dto.book.response.BookStatResponse
import org.assertj.core.api.AssertionsForInterfaceTypes.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class BookServiceTest @Autowired constructor(
    private val bookService: BookService,
    private val bookRepository: BookRepository,
    private val userRepository: UserRepository,
    private val userLoanHistoryRepository: UserLoanHistoryRepository,
) {
    @AfterEach
    fun clean() {
        bookRepository.deleteAll()
        userRepository.deleteAll()
    }

    @Test
    @DisplayName("책 등록 테스트")
    fun saveBookTest() {
        // given
        val request = BookRequest("Head First Java", BookType.COMPUTER)

        // when
        bookService.saveBook(request)

        // then
        val books = bookRepository.findAll()
        assertThat(books).hasSize(1)
        assertThat(books[0].name).isEqualTo("Head First Java")
        assertThat(books[0].type).isEqualTo(BookType.COMPUTER)
    }

    @Test
    @DisplayName("책 대여 테스트")
    fun loanBookTest() {
        // given
        val user = userRepository.save(User(null, "바트", 20))
        val book = bookRepository.save(Book.fixture("Head First Java"))

        // when
        bookService.loanBook(BookLoanRequest(user.name, book.name))

        // then
        val history = userLoanHistoryRepository.findAll()
        assertThat(history).hasSize(1)
        assertThat(history[0].bookName).isEqualTo("Head First Java")
        assertThat(history[0].user.id).isEqualTo(user.id)
        assertThat(history[0].status).isEqualTo(UserLoanStatus.LOANED)
    }

    @Test
    @DisplayName("중복 대출시 에러 발생 테스트")
    fun loanBookFailTest() {
        // given
        val user = userRepository.save(User(null, "바트", 20))
        val book = bookRepository.save(Book.fixture("Head First Java"))
        userLoanHistoryRepository.save(
            UserLoanHistory.fixture(
                user,
                book.name,
            )
        )
        val request = BookLoanRequest("바트", "Head First Java")

        // when & then
        assertThrows<IllegalArgumentException> {
            bookService.loanBook(request)
        }
    }

    @Test
    @DisplayName("반납 테스트")
    fun returnBookTest() {
        // given
        val user = userRepository.save(User(null, "바트", 20))
        val book = bookRepository.save(Book.fixture("Head First Java"))
        userLoanHistoryRepository.save(
            UserLoanHistory.fixture(
                user,
                book.name,
            )
        )
        val request = BookReturnRequest(user.name, book.name)

        // when
        bookService.returnBook(request)

        // then
        val history = userLoanHistoryRepository.findAll()
        assertThat(history).hasSize(1)
        assertThat(history[0].status).isEqualTo(UserLoanStatus.RETURNED)
    }

    @Test
    @DisplayName("책 대여 권 수 확인")
    fun countLoanBookTest() {
        // given
        val savedUser = userRepository.save(User(name = "회원1", age = null))
        userLoanHistoryRepository.saveAll(
            listOf(
                UserLoanHistory.fixture(savedUser, "A"),
                UserLoanHistory.fixture(savedUser, "B", UserLoanStatus.RETURNED),
                UserLoanHistory.fixture(savedUser, "C")
            )
        )

        // when
        val result = bookService.countLoanBook()

        // then
        assertThat(result).isEqualTo(2)
    }

    @Test
    @DisplayName("분야별 책 수를 확인")
    fun getBookStatisticsTest() {
        // given
        bookRepository.saveAll(
            listOf(
                Book.fixture("A", BookType.COMPUTER),
                Book.fixture("B", BookType.COMPUTER),
                Book.fixture("C", BookType.ECONOMY),
                Book.fixture("D", BookType.LANGUAGE),
            )
        )

        // when
        val results = bookService.getBookStatistics()

        // then
        assertCount(results, BookType.COMPUTER, 2)
        assertCount(results, BookType.ECONOMY, 1)
        assertCount(results, BookType.LANGUAGE, 1)
    }

    private fun assertCount(results: List<BookStatResponse>, type: BookType, count: Int) {
        assertThat(results.first { dto -> dto.type == type }.count).isEqualTo(count)
    }
}
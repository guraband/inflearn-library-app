package com.group.libraryapp.service.book

import com.group.libraryapp.domain.book.Book
import com.group.libraryapp.domain.book.BookRepository
import com.group.libraryapp.domain.user.User
import com.group.libraryapp.domain.user.UserRepository
import com.group.libraryapp.domain.user.loanhistory.UserLoanHistory
import com.group.libraryapp.domain.user.loanhistory.UserLoanHistoryRepository
import com.group.libraryapp.dto.book.request.BookLoanRequest
import com.group.libraryapp.dto.book.request.BookRequest
import com.group.libraryapp.dto.book.request.BookReturnRequest
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
        val request = BookRequest("Head First Java", "COMPUTER")

        // when
        bookService.saveBook(request)

        // then
        val books = bookRepository.findAll()
        assertThat(books).hasSize(1)
        assertThat(books[0].name).isEqualTo("Head First Java")
        assertThat(books[0].type).isEqualTo("COMPUTER")
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
        assertThat(history[0].isReturn).isFalse
    }

    @Test
    @DisplayName("중복 대출시 에러 발생 테스트")
    fun loanBookFailTest() {
        // given
        val user = userRepository.save(User(null, "바트", 20))
        val book = bookRepository.save(Book.fixture("Head First Java"))
        userLoanHistoryRepository.save(
            UserLoanHistory(
                null,
                user,
                book.name,
                false
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
            UserLoanHistory(
                null,
                user,
                book.name,
                false
            )
        )
        val request = BookReturnRequest(user.name, book.name)

        // when
        bookService.returnBook(request)

        // then
        val history = userLoanHistoryRepository.findAll()
        assertThat(history).hasSize(1)
        assertThat(history[0].isReturn).isTrue


    }
}
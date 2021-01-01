package com.sammidev.jpa.jpa

import org.hibernate.annotations.GenericGenerator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.repository.CrudRepository
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import java.time.LocalDateTime
import javax.persistence.*
import javax.validation.Valid
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@SpringBootApplication
class JpaApplication

fun main(args: Array<String>) {
	runApplication<JpaApplication>(*args)
}


/** SOME TEST

-> CREATE TRANSACTION SUCCESS
localhost:8080/transaction/
	{
	 "account": {
	   "id": "r001"
	 },
	 "timeTransaction":"2021-01-01T17:00:00",
	 "description" : "Test transaksi sukses",
	 "nominal" : "10000"
	}

-> CREATE TRANSACTION FAILED
localhost:8080/transaction/
	{
	 "account": {
	   "id": "r001"
	 },
	 "timeTransaction":"2021-01-01T17:00:00",
	 "description" : "Test transaksi sukses",
	 "nominal" : "7500"
	}

-> CREATE TRANSACTION FAILED
localhost:8080/account/r001/
	{
	 "account": {
	   "id": "r001"
	 },
	 "timeTransaction":"2021-01-01T17:00:00",
	 "description" : "Test transaksi sukses",
	 "nominal" : "15000"
	}

-> GET ACCOUNT INFO
localhost:8080/account/r002/
localhost:8080/account/r001/

-> LIST TRANSACTION
localhost:8080/account/r001/transaction/
localhost:8080/account/r002/transaction/

**/


@Controller
class TransaksiController(
		@Autowired private val accountRepository: AccountRepository,
	    @Autowired private val transactionRepository: TransactionRepository) {

	@Transactional
	@PostMapping("/transaction/")
	@ResponseStatus(HttpStatus.CREATED)
	fun insertTrasaction(@RequestBody @Valid transaction: Transaction) {
		val account = transaction.account.id?.let { accountRepository.findById(it).get() }
		transactionRepository.save(transaction)
		if (transaction.nominal
						.remainder(BigDecimal(7500))
						.compareTo(BigDecimal.ZERO) == 0) {
			throw Exception("kelipatan 7500 tak bole ye")
		}
		account!!.balance = account.balance.add(transaction.nominal)
		accountRepository.save(account)
	}

	@ResponseBody
	@GetMapping("/account/{account}/transaction/")
	fun transactionRecap(@PathVariable account: String): Iterable<Transaction> {
		val account = accountRepository.findById(account).get()
		return transactionRepository.findByAccount(account)
	}


	@ResponseBody
	@GetMapping("/account/{account}/")
	fun accountInfo(@PathVariable account: String) : Account {
		return accountRepository.findById(account).get()
	}
}

@Repository
interface CustomerRepository : CrudRepository<Customer, String>
@Repository
interface AccountRepository : CrudRepository<Account, String>
@Repository
interface TransactionRepository : CrudRepository<Transaction, String> {
	fun findByAccount(account: Account) : Iterable<Transaction>
}

@Entity
@Table(uniqueConstraints = [
	UniqueConstraint(columnNames = ["number"]),
	UniqueConstraint(columnNames = ["id_customer"])
],
name = "t_account")
data class Account(
	@Id
	@GeneratedValue(generator = "uuid")
	@GenericGenerator(name = "uuid", strategy = "uuid2")
	val id: String? = null,

	@Size(min = 3, max = 50)
	@Column(name = "number")
	@field: NotEmpty
	val number: String? = null,

	@field: NotNull
	@ManyToOne
	@JoinColumn(name = "id_customer")
	private val customer: Customer? = null,

	@field: NotNull
	var balance: BigDecimal = BigDecimal.ZERO
)

@Entity
@Table(uniqueConstraints = [
	UniqueConstraint(columnNames = ["number"])
],
name = "t_customer")
data class Customer (
		@Id
		@GeneratedValue(generator = "uuid")
		@GenericGenerator(name = "uuid", strategy = "uuid2")
		val id: String? = null,

		@Size(min = 3, max = 50)
		@field: NotEmpty
		@field: NotNull
		@Column(name = "number")
		val number: String? = null,

		@Size(min = 3, max = 50)
		@field: NotEmpty
		@field: NotNull
		val name: String? = null,
)

@Entity
@Table(name = "t_transaction")
data class Transaction (
		@Id
		@GeneratedValue(generator = "uuid")
		@GenericGenerator(name = "uuid", strategy = "uuid2")
		val id: String? = null,

		@field:NotNull
		@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
		val timeTransaction: LocalDateTime,

		@field:NotNull
		@ManyToOne @JoinColumn(name = "id_account")
		val account: Account,

		@Size(max = 255)
		@field: NotNull
		@field: NotEmpty
		val description: String,

		@Size(min = 0)
		@field: NotNull
		val nominal: BigDecimal
)
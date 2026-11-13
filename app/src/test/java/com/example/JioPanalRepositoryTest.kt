package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AccountStatus
import com.example.data.local.JioPanalDatabase
import com.example.data.local.JioPanalRepository
import com.example.data.local.TransactionStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class JioPanalRepositoryTest {

    private lateinit var db: JioPanalDatabase
    private lateinit var repo: JioPanalRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, JioPanalDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repo = JioPanalRepository(db)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `activation deposit approval credits 15 percent commission and unlocks withdrawals`() = runBlocking {
        val config = repo.ensureInitialConfig()
        assertEquals(5000.0, config.activationAmount, 0.01)
        assertEquals(15.0, config.commissionRatePercent, 0.01)
        assertEquals("vishalkumar90@upi", config.officialUpiId)
        assertEquals("@Sainishaab9076", config.telegramHandle)
        assertEquals("SAINI@9076", config.adminPasskey)

        val user = repo.loginOrRegisterWithGmail("partner.test@gmail.com")
        assertEquals(AccountStatus.INACTIVE, user.status)

        repo.submitDepositOrActivation(
            userId = user.userId,
            amount = 5000.0,
            utrRef = "UTR99887766",
            isActivation = true
        )

        val pendingUser = repo.observeUser(user.userId).first()!!
        assertEquals(AccountStatus.PENDING_APPROVAL, pendingUser.status)

        val pendingTxs = repo.observePendingTransactions().first()
        assertEquals(1, pendingTxs.size)
        val activationTx = pendingTxs.first()
        assertEquals(750.0, activationTx.commissionAmount, 0.01)

        val approvalAlert = repo.approveTransaction(activationTx.id)
        assertTrue(approvalAlert != null)

        val activeUser = repo.observeUser(user.userId).first()!!
        assertEquals(AccountStatus.ACTIVE, activeUser.status)
        assertEquals(750.0, activeUser.commissionEarned, 0.01)
        assertEquals(5750.0, activeUser.walletBalance, 0.01)

        repo.addLinkedAccount(user.userId, accountType = "UPI", upiId = "mypayout@okaxis")
        val accounts = repo.observeAccounts(user.userId).first()
        assertEquals(1, accounts.size)
        assertTrue(accounts.first().isPrimary)

        repo.requestWithdrawal(user.userId, 1000.0, "UPI (mypayout@okaxis)")
        val afterWithdraw = repo.observeUser(user.userId).first()!!
        assertEquals(4750.0, afterWithdraw.walletBalance, 0.01)

        val txs = repo.observeUserTransactions(user.userId).first()
        assertTrue(txs.any { it.status == TransactionStatus.APPROVED })
        assertTrue(txs.any { it.status == TransactionStatus.PENDING && it.amount == 1000.0 })
    }
}

package com.ivy.wallet.ui.loandetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ivy.wallet.base.computationThread
import com.ivy.wallet.base.ioThread
import com.ivy.wallet.logic.LoanCreator
import com.ivy.wallet.model.entity.Loan
import com.ivy.wallet.model.entity.LoanRecord
import com.ivy.wallet.persistence.dao.LoanDao
import com.ivy.wallet.persistence.dao.LoanRecordDao
import com.ivy.wallet.persistence.dao.SettingsDao
import com.ivy.wallet.ui.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoanDetailsViewModel @Inject constructor(
    private val loanDao: LoanDao,
    private val loanRecordDao: LoanRecordDao,
    private val loanCreator: LoanCreator,
    private val settingsDao: SettingsDao
) : ViewModel() {

    private val _baseCurrency = MutableStateFlow("")
    val baseCurrency = _baseCurrency.asStateFlow()

    private val _loan = MutableStateFlow<Loan?>(null)
    val loan = _loan.asStateFlow()

    private val _loanRecords = MutableStateFlow(emptyList<LoanRecord>())
    val loanRecords = _loanRecords.asStateFlow()

    private val _amountPaid = MutableStateFlow(0.0)
    val amountPaid = _amountPaid.asStateFlow()


    fun start(screen: Screen.LoanDetails) {
        viewModelScope.launch {
            _baseCurrency.value = ioThread {
                settingsDao.findFirst().currency
            }

            _loan.value = ioThread {
                loanDao.findById(id = screen.loanId)
            }

            _loanRecords.value = ioThread {
                loanRecordDao.findAllByLoanId(loanId = screen.loanId)
            }

            _amountPaid.value = computationThread {
                loanRecords.value.sumOf {
                    it.amount
                }
            }
        }
    }
}
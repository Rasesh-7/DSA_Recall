package com.example.dsarecall

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.dsarecall.data.local.AppDatabase
import com.example.dsarecall.data.repository.OnboardingRepository
import com.example.dsarecall.data.repository.ProblemRepositoryImpl
import com.example.dsarecall.domain.usecase.GetAnalyticsUseCase
import com.example.dsarecall.domain.usecase.GetDailyQueueUseCase
import com.example.dsarecall.domain.usecase.GetProblemBankUseCase
import com.example.dsarecall.domain.usecase.LogRecallAttemptUseCase
import com.example.dsarecall.ui.analytics.AnalyticsViewModel
import com.example.dsarecall.ui.bank.ProblemBankViewModel
import com.example.dsarecall.ui.navigation.MainAppNavigation
import com.example.dsarecall.ui.onboarding.OnboardingViewModel
import com.example.dsarecall.ui.queue.DailyQueueViewModel
import com.example.dsarecall.ui.theme.DSARecallTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = AppDatabase.getDatabase(applicationContext)
        val repository = ProblemRepositoryImpl(db.problemDao())
        val onboardingRepository = OnboardingRepository(applicationContext)

        // UseCases instantiation following Clean Architecture
        val getDailyQueueUseCase = GetDailyQueueUseCase(repository)
        val logRecallAttemptUseCase = LogRecallAttemptUseCase(repository)
        val getProblemBankUseCase = GetProblemBankUseCase(repository)
        val getAnalyticsUseCase = GetAnalyticsUseCase(repository)

        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return when {
                    modelClass.isAssignableFrom(OnboardingViewModel::class.java) ->
                        OnboardingViewModel(repository, onboardingRepository) as T
                    modelClass.isAssignableFrom(DailyQueueViewModel::class.java) ->
                        DailyQueueViewModel(getDailyQueueUseCase, logRecallAttemptUseCase, repository) as T
                    modelClass.isAssignableFrom(ProblemBankViewModel::class.java) ->
                        ProblemBankViewModel(getProblemBankUseCase, logRecallAttemptUseCase, repository) as T
                    modelClass.isAssignableFrom(AnalyticsViewModel::class.java) ->
                        AnalyticsViewModel(getAnalyticsUseCase) as T
                    else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
                }
            }
        }

        val onboardingViewModel = ViewModelProvider(this, factory)[OnboardingViewModel::class.java]
        val queueViewModel = ViewModelProvider(this, factory)[DailyQueueViewModel::class.java]
        val bankViewModel = ViewModelProvider(this, factory)[ProblemBankViewModel::class.java]
        val analyticsViewModel = ViewModelProvider(this, factory)[AnalyticsViewModel::class.java]

        val hasCompletedOnboarding = onboardingRepository.hasCompletedOnboarding()

        setContent {
            DSARecallTheme {
                MainAppNavigation(
                    onboardingViewModel = onboardingViewModel,
                    queueViewModel = queueViewModel,
                    bankViewModel = bankViewModel,
                    analyticsViewModel = analyticsViewModel,
                    hasCompletedOnboarding = hasCompletedOnboarding
                )
            }
        }
    }
}
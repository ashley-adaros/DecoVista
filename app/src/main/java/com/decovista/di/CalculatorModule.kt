package com.decovista.di

import com.decovista.calculator.domain.usecase.CalculateFitUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CalculatorModule {

    @Provides
    @Singleton
    fun provideCalculateFitUseCase(): CalculateFitUseCase {
        return CalculateFitUseCase()
    }
}

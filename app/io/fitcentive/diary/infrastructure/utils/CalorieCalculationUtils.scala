package io.fitcentive.diary.infrastructure.utils

import io.fitcentive.diary.domain.user.FitnessUserProfile

trait CalorieCalculationUtils {

  private val AvgSpeed = 1.34
  private val METCofactor = 3.5

  /**
    * Formula taken from https://www.omnicalculator.com/sports/steps-to-calories
    */
  def caloriesBurnedForStepsTaken(stepsTaken: Int, fitnessUserProfile: FitnessUserProfile): Double = {
    val stride = (fitnessUserProfile.heightInCm / 100) * 0.414;
    val distance = stride * stepsTaken
    val time = distance / AvgSpeed
    time * METCofactor * 3.5 * ((fitnessUserProfile.weightInLbs / 2.205) / (200 * 60))
  }
}

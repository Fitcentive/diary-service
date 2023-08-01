package io.fitcentive.diary.api

import cats.data.EitherT
import io.fitcentive.diary.domain.diary.{AllDiaryEntriesForDay, AllDiaryEntriesForMonth}
import io.fitcentive.diary.domain.exercise.{CardioWorkout, StrengthWorkout, UserStepsData}
import io.fitcentive.diary.domain.food.FoodEntry
import io.fitcentive.diary.domain.payloads.DiaryEntryIdsPayload
import io.fitcentive.diary.domain.user.FitnessUserProfile
import io.fitcentive.diary.infrastructure.utils.CalorieCalculationUtils
import io.fitcentive.diary.repositories.{ExerciseDiaryRepository, FoodDiaryRepository, UserRepository}
import io.fitcentive.diary.services.{MeetupService, MessageBusService}
import io.fitcentive.sdk.error.{DomainError, EntityNotAccessible, EntityNotFoundError}

import java.time.{Instant, LocalDate, LocalDateTime, ZoneOffset}
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}
import scala.util.chaining.scalaUtilChainingOps

@Singleton
class DiaryApi @Inject() (
  exerciseDiaryRepository: ExerciseDiaryRepository,
  foodDiaryRepository: FoodDiaryRepository,
  userRepository: UserRepository,
  meetupService: MeetupService,
  messageBusService: MessageBusService,
)(implicit ec: ExecutionContext)
  extends CalorieCalculationUtils {

  // --------------------------------
  // Exercise Diary API methods
  // --------------------------------
  def insertCardioDiaryEntry(userId: UUID, create: CardioWorkout.Create): Future[CardioWorkout] =
    for {
      c <-
        exerciseDiaryRepository
          .insertCardioWorkoutForUser(id = UUID.randomUUID(), userId = userId, create = create)
      entryDate = LocalDateTime.ofInstant(c.cardioDate, ZoneOffset.UTC).format(DateTimeFormatter.ISO_LOCAL_DATE)
      _ <- messageBusService.publishUserDiaryEntryCreatedEvent(userId, entryDate, c.durationInMinutes)
    } yield c

  def updateStrengthDiaryEntry(
    userId: UUID,
    diaryEntryId: UUID,
    update: StrengthWorkout.Update
  ): Future[Either[DomainError, StrengthWorkout]] =
    (for {
      entryOpt <-
        EitherT.right[DomainError](exerciseDiaryRepository.getStrengthWorkoutByIdForUser(userId, diaryEntryId))
      entry <- EitherT[Future, DomainError, StrengthWorkout](
        entryOpt
          .map(x => Future.successful(Right(x)))
          .getOrElse(Future.successful(Left(EntityNotFoundError(s"No diary entry found for id: $diaryEntryId"))))
      )
      _ <- EitherT[Future, DomainError, StrengthWorkout] {
        if (entry.userId == userId) Future.successful(Right(entry))
        else Future.successful(Left(EntityNotAccessible("Cannot update a diary entry that user did not create!")))
      }
      updatedEntry <- EitherT.right[DomainError](
        exerciseDiaryRepository.updateStrengthWorkoutByIdForUser(userId, diaryEntryId, update)
      )
    } yield updatedEntry).value

  def getStrengthDiaryEntry(userId: UUID, diaryEntryId: UUID): Future[Either[DomainError, StrengthWorkout]] =
    (for {
      entryOpt <-
        EitherT.right[DomainError](exerciseDiaryRepository.getStrengthWorkoutByIdForUser(userId, diaryEntryId))
      entry <- EitherT[Future, DomainError, StrengthWorkout](
        entryOpt
          .map(x => Future.successful(Right(x)))
          .getOrElse(Future.successful(Left(EntityNotFoundError(s"No diary entry found for id: $diaryEntryId"))))
      )
      _ <- EitherT[Future, DomainError, StrengthWorkout] {
        if (entry.userId == userId) Future.successful(Right(entry))
        else Future.successful(Left(EntityNotAccessible("Cannot access a diary entry that user did not create!")))
      }
    } yield entry).value

  def updateCardioDiaryEntry(
    userId: UUID,
    diaryEntryId: UUID,
    update: CardioWorkout.Update
  ): Future[Either[DomainError, CardioWorkout]] =
    (for {
      entryOpt <- EitherT.right[DomainError](exerciseDiaryRepository.getCardioWorkoutByIdForUser(userId, diaryEntryId))
      entry <- EitherT[Future, DomainError, CardioWorkout](
        entryOpt
          .map(x => Future.successful(Right(x)))
          .getOrElse(Future.successful(Left(EntityNotFoundError(s"No diary entry found for id: $diaryEntryId"))))
      )
      _ <- EitherT[Future, DomainError, CardioWorkout] {
        if (entry.userId == userId) Future.successful(Right(entry))
        else Future.successful(Left(EntityNotAccessible("Cannot update a diary entry that user did not create!")))
      }
      updatedEntry <-
        EitherT.right[DomainError](exerciseDiaryRepository.updateCardioWorkoutByIdForUser(userId, diaryEntryId, update))
    } yield updatedEntry).value

  def getCardioDiaryEntry(userId: UUID, diaryEntryId: UUID): Future[Either[DomainError, CardioWorkout]] =
    (for {
      entryOpt <- EitherT.right[DomainError](exerciseDiaryRepository.getCardioWorkoutByIdForUser(userId, diaryEntryId))
      entry <- EitherT[Future, DomainError, CardioWorkout](
        entryOpt
          .map(x => Future.successful(Right(x)))
          .getOrElse(Future.successful(Left(EntityNotFoundError(s"No diary entry found for id: $diaryEntryId"))))
      )
      _ <- EitherT[Future, DomainError, CardioWorkout] {
        if (entry.userId == userId) Future.successful(Right(entry))
        else Future.successful(Left(EntityNotAccessible("Cannot access a diary entry that user did not create!")))
      }
    } yield entry).value

  def deleteCardioDiaryEntry(userId: UUID, cardioWorkoutEntryId: UUID): Future[Unit] =
    for {
      _ <- exerciseDiaryRepository.deleteCardioWorkoutForUser(userId, cardioWorkoutEntryId)
      _ <- meetupService.deleteCardioEntryAssociatedToMeetup(cardioWorkoutEntryId)
    } yield ()

  def insertStrengthDiaryEntry(userId: UUID, create: StrengthWorkout.Create): Future[StrengthWorkout] =
    for {
      s <-
        exerciseDiaryRepository
          .insertStrengthWorkoutForUser(id = UUID.randomUUID(), userId = userId, create = create)
      activityMinutes = calculateActivityMinutes(s.sets.getOrElse(0), s.reps.getOrElse(0))
      entryDate = LocalDateTime.ofInstant(s.exerciseDate, ZoneOffset.UTC).format(DateTimeFormatter.ISO_LOCAL_DATE)
      _ <- messageBusService.publishUserDiaryEntryCreatedEvent(userId, entryDate, Some(activityMinutes))
    } yield s

  def deleteStrengthDiaryEntry(userId: UUID, strengthWorkoutEntryId: UUID): Future[Unit] =
    for {
      _ <- exerciseDiaryRepository.deleteStrengthWorkoutForUser(userId, strengthWorkoutEntryId)
      _ <- meetupService.deleteStrengthEntryAssociatedToMeetup(strengthWorkoutEntryId)
    } yield ()

  def associateDiaryEntriesToMeetup(meetupId: UUID, payload: DiaryEntryIdsPayload): Future[Unit] = {
    for {
      _ <- Future.unit
      foodFut = Future.sequence(
        payload.foodEntryIds.map(id => foodDiaryRepository.associateMeetupWithFoodEntryById(id, meetupId))
      )
      cardioFut = Future.sequence(
        payload.cardioEntryIds.map(id => exerciseDiaryRepository.associateMeetupWithCardioEntryById(id, meetupId))
      )
      strengthFut = Future.sequence(
        payload.strengthEntryIds.map(id => exerciseDiaryRepository.associateMeetupWithStrengthEntryById(id, meetupId))
      )
      _ <- foodFut
      _ <- cardioFut
      _ <- strengthFut
    } yield ()
  }

  def getAllDiaryEntriesForUserByDay(
    userId: UUID,
    day: Instant,
    offsetInMinutes: Int
  ): Future[AllDiaryEntriesForDay] = {
    val windowStart = day.plus(-offsetInMinutes, ChronoUnit.MINUTES)
    val windowEnd = windowStart.plus(1, ChronoUnit.DAYS)
    for {
      cardioEntries <- exerciseDiaryRepository.getAllCardioWorkoutsForDayByUser(userId = userId, windowStart, windowEnd)
      strengthEntries <-
        exerciseDiaryRepository.getAllStrengthWorkoutsForDayByUser(userId = userId, windowStart, windowEnd)
      foodEntries <- foodDiaryRepository.getAllFoodEntriesForDayByUser(userId, windowStart, windowEnd)
    } yield AllDiaryEntriesForDay(
      cardioWorkouts = cardioEntries,
      strengthWorkouts = strengthEntries,
      foodEntries = foodEntries
    )
  }

  def getAllDiaryEntriesForUserByMonth(
    userId: UUID,
    dateString: String,
    offsetInMinutes: Int
  ): Future[AllDiaryEntriesForMonth] = {
    val startOfMonth = LocalDate.parse(dateString).atStartOfDay().toInstant(ZoneOffset.UTC)
    var windowStart = startOfMonth.plus(-offsetInMinutes, ChronoUnit.MINUTES)

    val date = LocalDate.parse(dateString)
    val windowMaxEnd = {
      if (List(4, 6, 9, 11).contains(date.getMonth.getValue)) windowStart.plus(30, ChronoUnit.DAYS)
      else if (List(1, 3, 5, 7, 8, 10, 12).contains(date.getMonth.getValue)) windowStart.plus(31, ChronoUnit.DAYS)
      else {
        if (date.getYear % 4 == 0) windowStart.plus(29, ChronoUnit.DAYS)
        else windowStart.plus(28, ChronoUnit.DAYS)
      }
    }

    val keys: mutable.ArrayBuffer[Instant] = mutable.ArrayBuffer.empty
    while (windowStart.isBefore(windowMaxEnd)) {
      keys.addOne(windowStart)
      windowStart = windowStart.plus(1, ChronoUnit.DAYS)
    }

    Future
      .sequence(keys.toSeq.map { dayOfMonth =>
        val windowEnd = dayOfMonth.plus(1, ChronoUnit.DAYS)
        for {
          cardioEntries <-
            exerciseDiaryRepository.getAllCardioWorkoutsForDayByUser(userId = userId, dayOfMonth, windowEnd)
          strengthEntries <-
            exerciseDiaryRepository.getAllStrengthWorkoutsForDayByUser(userId = userId, dayOfMonth, windowEnd)
          foodEntries <- foodDiaryRepository.getAllFoodEntriesForDayByUser(userId, dayOfMonth, windowEnd)
        } yield AllDiaryEntriesForDay(
          cardioWorkouts = cardioEntries,
          strengthWorkouts = strengthEntries,
          foodEntries = foodEntries
        ).pipe { allEntries =>
          DateTimeFormatter
            .ofPattern("yyyy-MM-dd")
            .format(LocalDateTime.ofInstant(dayOfMonth, ZoneOffset.UTC)) -> allEntries
        }
      })
      .map { entries =>
        AllDiaryEntriesForMonth(entries = Map.from(entries))
      }

  }

  def getDiaryEntriesByIds(diaryEntriesPayload: DiaryEntryIdsPayload): Future[AllDiaryEntriesForDay] = {
    for {
      _ <- Future.unit
      cardioEntriesFut =
        if (diaryEntriesPayload.cardioEntryIds.nonEmpty)
          exerciseDiaryRepository.getCardioWorkoutsById(diaryEntriesPayload.cardioEntryIds)
        else Future.successful(Seq.empty)

      strengthEntriesFut =
        if (diaryEntriesPayload.strengthEntryIds.nonEmpty)
          exerciseDiaryRepository.getStrengthWorkoutsByIds(diaryEntriesPayload.strengthEntryIds)
        else Future.successful(Seq.empty)

      foodEntriesFut =
        if (diaryEntriesPayload.foodEntryIds.nonEmpty)
          foodDiaryRepository.getFoodEntriesByIds(diaryEntriesPayload.foodEntryIds)
        else Future.successful(Seq.empty)

      cardioEntries <- cardioEntriesFut
      strengthEntries <- strengthEntriesFut
      foodEntries <- foodEntriesFut

    } yield AllDiaryEntriesForDay(
      cardioWorkouts = cardioEntries,
      strengthWorkouts = strengthEntries,
      foodEntries = foodEntries
    )
  }

  def getCardioEntriesForUserByDay(userId: UUID, day: Instant, offsetInMinutes: Int): Future[Seq[CardioWorkout]] = {
    val windowStart = day.plus(-offsetInMinutes, ChronoUnit.MINUTES);
    val windowEnd = windowStart.plus(1, ChronoUnit.DAYS);
    exerciseDiaryRepository
      .getAllCardioWorkoutsForDayByUser(userId = userId, windowStart, windowEnd)
  }

  def getStrengthEntriesForUserByDay(userId: UUID, day: Instant, offsetInMinutes: Int): Future[Seq[StrengthWorkout]] = {
    val windowStart = day.plus(-offsetInMinutes, ChronoUnit.MINUTES)
    val windowEnd = windowStart.plus(1, ChronoUnit.DAYS)
    exerciseDiaryRepository
      .getAllStrengthWorkoutsForDayByUser(userId = userId, windowStart, windowEnd)
  }

  def getUserMostRecentlyViewedWorkoutIds(userId: UUID): Future[Seq[UUID]] =
    exerciseDiaryRepository
      .getUserRecentlyViewedWorkoutIds(userId)

  def addUserMostRecentlyViewedWorkout(userId: UUID, workoutId: UUID): Future[Unit] =
    for {
      existingHistory <- exerciseDiaryRepository.getUserRecentlyViewedWorkoutIds(userId)
      _ <-
        if (existingHistory.length >= 10) {
          existingHistory.lastOption
            .map(
              staleWorkoutId => exerciseDiaryRepository.deleteMostRecentlyViewedWorkoutForUser(userId, staleWorkoutId)
            )
            .getOrElse(Future.unit)
        } else {
          Future.unit
        }
      _ <- exerciseDiaryRepository.upsertMostRecentlyViewedWorkoutForUser(userId, workoutId)
    } yield ()

  // --------------------------------
  // Food Diary API methods
  // --------------------------------
  def updateFoodDiaryEntry(
    userId: UUID,
    foodDiaryEntryId: UUID,
    update: FoodEntry.Update
  ): Future[Either[DomainError, FoodEntry]] =
    (for {
      entryOpt <- EitherT.right[DomainError](foodDiaryRepository.getFoodEntryForUserById(userId, foodDiaryEntryId))
      entry <- EitherT[Future, DomainError, FoodEntry](
        entryOpt
          .map(x => Future.successful(Right(x)))
          .getOrElse(
            Future.successful(Left(EntityNotFoundError(s"No food diary entry found for id: $foodDiaryEntryId")))
          )
      )
      _ <- EitherT[Future, DomainError, FoodEntry] {
        if (entry.userId == userId) Future.successful(Right(entry))
        else Future.successful(Left(EntityNotAccessible("Cannot update a diary entry that user did not create!")))
      }
      updatedEntry <-
        EitherT.right[DomainError](foodDiaryRepository.updateFoodEntryForUserById(userId, foodDiaryEntryId, update))
    } yield updatedEntry).value

  def getFoodDiaryEntry(userId: UUID, foodDiaryEntryId: UUID): Future[Either[DomainError, FoodEntry]] =
    (for {
      entryOpt <- EitherT.right[DomainError](foodDiaryRepository.getFoodEntryForUserById(userId, foodDiaryEntryId))
      entry <- EitherT[Future, DomainError, FoodEntry](
        entryOpt
          .map(x => Future.successful(Right(x)))
          .getOrElse(
            Future.successful(Left(EntityNotFoundError(s"No food diary entry found for id: $foodDiaryEntryId")))
          )
      )
      _ <- EitherT[Future, DomainError, FoodEntry] {
        if (entry.userId == userId) Future.successful(Right(entry))
        else Future.successful(Left(EntityNotAccessible("Cannot access a diary entry that user did not create!")))
      }
    } yield entry).value

  def insertFoodDiaryEntry(userId: UUID, create: FoodEntry.Create): Future[FoodEntry] =
    for {
      f <-
        foodDiaryRepository
          .insertFoodDiaryEntry(id = UUID.randomUUID(), userId = userId, create = create)
      entryDate = LocalDateTime.ofInstant(f.entryDate, ZoneOffset.UTC).format(DateTimeFormatter.ISO_LOCAL_DATE)
      _ <- messageBusService.publishUserDiaryEntryCreatedEvent(userId, entryDate, None)
    } yield f

  def deleteFoodDiaryEntry(userId: UUID, foodEntryId: UUID): Future[Unit] =
    for {
      _ <- foodDiaryRepository.deleteFoodDiaryEntry(userId, foodEntryId)
      _ <- meetupService.deleteFoodEntryAssociatedToMeetup(foodEntryId)
    } yield ()

  def getFoodEntriesForUserByDay(userId: UUID, day: Instant, offsetInMinutes: Int): Future[Seq[FoodEntry]] = {
    val windowStart = day.plus(-offsetInMinutes, ChronoUnit.MINUTES)
    val windowEnd = windowStart.plus(1, ChronoUnit.DAYS)
    foodDiaryRepository
      .getAllFoodEntriesForDayByUser(userId, windowStart, windowEnd)
  }

  def getUserMostRecentlyViewedFoodIds(userId: UUID): Future[Seq[Int]] =
    foodDiaryRepository
      .getUserRecentlyViewedFoodIds(userId)

  def addUserMostRecentlyViewedFood(userId: UUID, foodId: Int): Future[Unit] =
    for {
      existingHistory <- foodDiaryRepository.getUserRecentlyViewedFoodIds(userId)
      _ <-
        if (existingHistory.length >= 10) {
          existingHistory.lastOption
            .map(staleFoodId => foodDiaryRepository.deleteMostRecentlyViewedFoodForUser(userId, staleFoodId))
            .getOrElse(Future.unit)
        } else {
          Future.unit
        }
      _ <- foodDiaryRepository.upsertMostRecentlyViewedFoodForUser(userId, foodId)
    } yield ()

  def dissociateFoodDiaryEntryFromMeetup(foodEntryId: UUID): Future[Unit] =
    foodDiaryRepository.dissociateMeetupFromFoodEntryById(foodEntryId)

  def dissociateCardioDiaryEntryFromMeetup(cardioEntryId: UUID): Future[Unit] =
    exerciseDiaryRepository.dissociateMeetupFromCardioEntryById(cardioEntryId)

  def dissociateStrengthDiaryEntryFromMeetup(strengthEntryId: UUID): Future[Unit] =
    exerciseDiaryRepository.dissociateMeetupFromStrengthEntryById(strengthEntryId)

  def upsertUserStepsData(
    userId: UUID,
    stepsTaken: Int,
    dateString: String
  ): Future[Either[DomainError, UserStepsData]] =
    (for {
      userFitnessProfile <- EitherT[Future, DomainError, FitnessUserProfile](
        userRepository
          .getFitnessUserProfile(userId)
          .map(_.map(Right.apply).getOrElse(Left(EntityNotFoundError("No user fitness profile found!"))))
      )
      stepsData <- EitherT.right[DomainError](
        exerciseDiaryRepository
          .upsertUserStepsData(
            userId,
            stepsTaken,
            caloriesBurnedForStepsTaken(stepsTaken, userFitnessProfile),
            dateString
          )
      )
      _ <- EitherT.right[DomainError](messageBusService.publishUserStepDataUpdatedEvent(stepsData))
    } yield stepsData).value

  def getUserStepsData(userId: UUID, dateString: String): Future[Either[DomainError, UserStepsData]] =
    exerciseDiaryRepository
      .getUserStepsData(userId, dateString)
      .map(_.map(Right.apply).getOrElse(Left(EntityNotFoundError("No steps data found!"))))

  /**
    * Same formula to be used in client side app
    */
  private def calculateActivityMinutes(sets: Int, reps: Int): Int = {
    if (sets == 0 || reps == 0) 0
    else (((reps * 4) * sets) + (scala.math.max(sets - 1, 1) * 30)) / 60
  }
}

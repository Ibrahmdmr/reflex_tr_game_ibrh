package com.reflex.tr.game.ibrh.ui.game

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.reflex.tr.game.ibrh.ads.AdConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * End-to-end coverage of the gameplay loop through [GameViewModel].
 *
 * The view model is driven with an [UnconfinedTestDispatcher] whose scheduler is never advanced,
 * so the countdown and target-timeout coroutines stay parked and each test observes only the
 * effects of the input it sends. The leaderboard is backed by the offline implementation, so no
 * test touches the network.
 */
@RunWith(RobolectricTestRunner::class)
class GameViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        // GamePreferences is process-wide; clear it so tests cannot leak state into each other.
        context.getSharedPreferences("game_preferences", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private val context: Context
        get() = ApplicationProvider.getApplicationContext()

    private fun createViewModel() = GameViewModel(
        gamePreferences = GamePreferences(context),
        leaderboardRepository = LocalLeaderboardRepository(),
        targetEngine = GameTargetEngine(),
        adConfig = AdConfig.Default,
        defaultPlayerName = { "Player" }
    )

    /** Starts a run with the one-off mode tip already dismissed, so gameplay accepts input. */
    private fun GameViewModel.startPlayableGame(mode: GameMode) {
        markModeTipShown(mode)
        startGame(mode)
    }

    private fun GameViewModel.tapCorrectTarget() {
        val target = uiState.value.targets.single { it.role == GameTargetRole.Correct }
        onTargetTapped(target.id)
    }

    private fun GameViewModel.tapDecoyTarget() {
        val target = uiState.value.targets.first { it.role != GameTargetRole.Correct }
        onTargetTapped(target.id)
    }

    // --- starting a run ---

    @Test
    fun `starts a run with a full board`() {
        val viewModel = createViewModel()

        viewModel.startPlayableGame(GameMode.Classic)

        val state = viewModel.uiState.value
        assertTrue(state.hasGameStarted)
        assertFalse(state.isPaused)
        assertFalse(state.isGameOver)
        assertEquals(0, state.score)
        assertEquals(0, state.combo)
        assertEquals(3, state.lives)
        assertEquals(GameMode.Classic, state.selectedMode)
        assertTrue("A run must start with at least one target", state.targets.isNotEmpty())
    }

    @Test
    fun `shows the mode tip on the first run of a mode`() {
        val viewModel = createViewModel()

        viewModel.startGame(GameMode.Classic)

        assertTrue("First run of a mode should pause for its tip", viewModel.uiState.value.isPaused)
    }

    // --- scoring ---

    @Test
    fun `scores and builds combo on a correct tap`() {
        val viewModel = createViewModel()
        viewModel.startPlayableGame(GameMode.Classic)

        viewModel.tapCorrectTarget()

        val state = viewModel.uiState.value
        assertEquals(1, state.score)
        assertEquals(1, state.successfulHits)
        assertEquals(1, state.totalAttempts)
        assertEquals(3, state.lives)
        assertTrue("A correct tap must open a combo", state.combo >= 1)
    }

    @Test
    fun `awards a combo bonus for perfect timing`() {
        // The test clock does not advance, so every tap lands inside the perfect window and
        // earns the extra combo step on top of the opening combo of 1.
        val viewModel = createViewModel()
        viewModel.startPlayableGame(GameMode.Classic)

        viewModel.tapCorrectTarget()

        val state = viewModel.uiState.value
        assertEquals(TimingGrade.Perfect, state.lastTimingGrade)
        assertEquals(2, state.combo)
        assertEquals(1, state.perfectHits)
    }

    @Test
    fun `keeps scoring across consecutive correct taps`() {
        val viewModel = createViewModel()
        viewModel.startPlayableGame(GameMode.Classic)

        repeat(5) { viewModel.tapCorrectTarget() }

        val state = viewModel.uiState.value
        assertEquals(5, state.score)
        assertEquals(5, state.successfulHits)
        assertEquals(3, state.lives)
        assertTrue("An unbroken run must keep growing the combo", state.combo >= 5)
        assertEquals("The peak combo is the current one in an unbroken run", state.combo, state.maxCombo)
    }

    @Test
    fun `spawns a fresh target after every hit`() {
        val viewModel = createViewModel()
        viewModel.startPlayableGame(GameMode.Classic)

        val firstId = viewModel.uiState.value.targets.single().id
        viewModel.tapCorrectTarget()
        val secondId = viewModel.uiState.value.targets.single().id

        assertTrue("A hit must replace the target", firstId != secondId)
    }

    @Test
    fun `raises the difficulty level as the score grows`() {
        val viewModel = createViewModel()
        viewModel.startPlayableGame(GameMode.Classic)

        assertEquals(1, viewModel.uiState.value.difficultyLevel)
        repeat(11) { viewModel.tapCorrectTarget() }

        assertEquals(2, viewModel.uiState.value.difficultyLevel)
    }

    // --- losing lives ---

    @Test
    fun `loses a life when a decoy is tapped`() {
        val viewModel = createViewModel()
        viewModel.startPlayableGame(GameMode.FakeTarget)

        viewModel.tapDecoyTarget()

        val state = viewModel.uiState.value
        assertEquals(2, state.lives)
        assertEquals(0, state.score)
        assertFalse(state.isGameOver)
    }

    @Test
    fun `loses a life when the player taps empty space`() {
        val viewModel = createViewModel()
        viewModel.startPlayableGame(GameMode.Classic)

        viewModel.onMissTapped()

        assertEquals(2, viewModel.uiState.value.lives)
    }

    @Test
    fun `resets the combo after a miss`() {
        val viewModel = createViewModel()
        viewModel.startPlayableGame(GameMode.Classic)

        repeat(3) { viewModel.tapCorrectTarget() }
        val comboBeforeMiss = viewModel.uiState.value.combo
        assertTrue(comboBeforeMiss > 0)

        viewModel.onMissTapped()

        val state = viewModel.uiState.value
        assertEquals(0, state.combo)
        assertEquals(
            "The best combo of the run must survive a miss",
            comboBeforeMiss,
            state.maxCombo
        )
    }

    @Test
    fun `ends the run once every life is gone`() {
        val viewModel = createViewModel()
        viewModel.startPlayableGame(GameMode.Classic)

        repeat(3) { viewModel.onMissTapped() }

        val state = viewModel.uiState.value
        assertTrue(state.isGameOver)
        assertEquals(0, state.lives)
    }

    // --- persistence ---

    @Test
    fun `records the run score as the mode best score`() {
        val viewModel = createViewModel()
        viewModel.startPlayableGame(GameMode.Classic)

        repeat(4) { viewModel.tapCorrectTarget() }
        repeat(3) { viewModel.onMissTapped() }

        val state = viewModel.uiState.value
        assertTrue(state.isGameOver)
        assertEquals(4, state.bestScoresByMode[GameMode.Classic])
    }

    @Test
    fun `keeps best scores separate per mode`() {
        val viewModel = createViewModel()

        viewModel.startPlayableGame(GameMode.Classic)
        repeat(2) { viewModel.tapCorrectTarget() }
        repeat(3) { viewModel.onMissTapped() }

        viewModel.startPlayableGame(GameMode.MovingTarget)
        repeat(3) { viewModel.onMissTapped() }

        val bestScores = viewModel.uiState.value.bestScoresByMode
        assertEquals(2, bestScores[GameMode.Classic])
        assertEquals(0, bestScores[GameMode.MovingTarget])
    }

    @Test
    fun `awards coins for a finished run`() {
        val viewModel = createViewModel()
        val coinsBefore = viewModel.uiState.value.progressionState.coins

        viewModel.startPlayableGame(GameMode.Classic)
        repeat(3) { viewModel.tapCorrectTarget() }
        repeat(3) { viewModel.onMissTapped() }

        val state = viewModel.uiState.value
        assertTrue(state.isGameOver)
        assertTrue(
            "A scoring run must pay out coins",
            state.progressionState.coins > coinsBefore
        )
    }

    // --- boosts ---

    /** Seeds the wallet before the view model reads its initial state from storage. */
    private fun giveCoins(amount: Int) {
        val preferences = GamePreferences(context)
        preferences.saveProgressionState(preferences.getProgressionState().copy(coins = amount))
    }

    @Test
    fun `coin boost charges the wallet and grants its advantage`() {
        val coinsBefore = 500
        giveCoins(coinsBefore)
        val viewModel = createViewModel()
        viewModel.markModeTipShown(GameMode.Classic)

        val started = viewModel.startGameWithCoinBoost(GameBoost.ExtraTime)

        val state = viewModel.uiState.value
        assertTrue("A affordable boost must start the run", started)
        assertEquals(GameBoost.ExtraTime, state.activeBoost)
        assertEquals("ExtraTime adds five seconds to the base 30", 35, state.timeLeftSeconds)
        assertEquals(coinsBefore - GameBoost.ExtraTime.coinPrice, state.progressionState.coins)
    }

    @Test
    fun `coin boost is refused when the player cannot afford it`() {
        val viewModel = createViewModel()
        viewModel.markModeTipShown(GameMode.Classic)

        val started = viewModel.startGameWithCoinBoost(GameBoost.ComboStart)

        val state = viewModel.uiState.value
        assertFalse("A boost beyond the wallet must not start a run", started)
        assertFalse(state.hasGameStarted)
        assertNull(state.activeBoost)
    }

    @Test
    fun `the extra life boost starts the run with a spare life`() {
        giveCoins(999)
        val viewModel = createViewModel()
        viewModel.markModeTipShown(GameMode.Classic)

        viewModel.startGameWithCoinBoost(GameBoost.ExtraLife)

        assertEquals(4, viewModel.uiState.value.lives)
    }

    // --- pause ---

    @Test
    fun `ignores taps while the run is paused`() {
        val viewModel = createViewModel()
        viewModel.startPlayableGame(GameMode.Classic)
        viewModel.tapCorrectTarget()

        val targetId = viewModel.uiState.value.targets.single().id
        viewModel.pauseGame()
        viewModel.onTargetTapped(targetId)

        val state = viewModel.uiState.value
        assertTrue(state.isPaused)
        assertEquals("A tap during pause must not score", 1, state.score)
    }
}

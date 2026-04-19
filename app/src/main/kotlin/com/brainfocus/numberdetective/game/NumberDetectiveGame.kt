package com.brainfocus.numberdetective.game

import kotlin.random.Random

class NumberDetectiveGame {
    companion object {
        private const val MAX_ATTEMPTS = 3
        private const val INITIAL_SCORE = 1000
        private const val DIGITS_LEVEL_1_2 = 3
        private const val DIGITS_LEVEL_3 = 4
        
        // Level 1-2 Hint patterns
        private val LEVEL_1_2_FIRST_A_CHOICES = listOf("xax", "xxa")
        private val LEVEL_1_2_FIRST_B_CHOICES = listOf("bxx", "xxb")
        private val LEVEL_1_2_FIRST_C_CHOICES = listOf("cxx", "xcx")
        
        private val LEVEL_1_2_ONLY_A_CORRECT = listOf("axx")
        private val LEVEL_1_2_ONLY_B_CORRECT = listOf("xbx")
        private val LEVEL_1_2_ONLY_C_CORRECT = listOf("xxc")
        
        private val LEVEL_1_2_AB_FALSE = listOf("bax", "bxa", "xab")
        private val LEVEL_1_2_AC_FALSE = listOf("cax", "xca", "cxa")
        private val LEVEL_1_2_CB_FALSE = listOf("bcx", "cxb", "xcb")


        // Level 3 Hint patterns
        private val LEVEL_3_AB_FIRST_TRUE = listOf("axbx", "axxb")
        private val LEVEL_3_AC_FIRST_TRUE = listOf("acxx", "axxc")
        private val LEVEL_3_AD_FIRST_TRUE = listOf("adxx", "axdx")

        private val LEVEL_3_BA_FIRST_TRUE = listOf("xbax", "xbxa")
        private val LEVEL_3_BC_FIRST_TRUE = listOf("cbxx", "xbxc")
        private val LEVEL_3_BD_FIRST_TRUE = listOf("dbxx", "xbdx")

        private val LEVEL_3_CA_FIRST_TRUE = listOf("xacx", "xxca")
        private val LEVEL_3_CB_FIRST_TRUE = listOf("bxcx", "xxcb")
        private val LEVEL_3_CD_FIRST_TRUE = listOf("xdcx", "dxcx")

        private val LEVEL_3_DA_FIRST_TRUE = listOf("xaxd", "xxad")
        private val LEVEL_3_DB_FIRST_TRUE = listOf("bxxd", "xxbd")
        private val LEVEL_3_DC_FIRST_TRUE = listOf("xcxd", "cxxd")

        private val LEVEL_3_AD_TRUE = listOf("axxd")
        private val LEVEL_3_BD_TRUE = listOf("xbxd")
        private val LEVEL_3_CD_TRUE = listOf("xxcd")
        private val LEVEL_3_AC_TRUE = listOf("axcx")

        // Level 3 Single Correct Digit
        private val LEVEL_3_SINGLE_TRUE = listOf("axxx", "xbxx", "xxcx", "xxxd")

        // Level 3 Single False Digit
        private val LEVEL_3_SINGLE_FALSE_A = listOf("xaxx", "xxax", "xxxa")
        private val LEVEL_3_SINGLE_FALSE_B = listOf("bxxx", "xxbx", "xxxb")
        private val LEVEL_3_SINGLE_FALSE_C = listOf("cxxx", "xcxx", "xxxc")
        private val LEVEL_3_SINGLE_FALSE_D = listOf("dxxx", "xdxx", "xxdx")

    }

    private var secretNumber = ""
    private var remainingAttempts = MAX_ATTEMPTS
    private var currentScore = INITIAL_SCORE
    private var pathSelection = (1..3).random()
    private val numbers = (0..9).toMutableList()
    private var isGameWon = false
    private var currentLevel = 1
    private var dummyNumbers = mutableListOf<Int>() // Level 3 için dummy rakamlar
    
    var firstHint = ""
        private set
    var secondHint = ""
        private set
    var thirdHint = ""
        private set
    var fourthHint = ""
        private set
    var fifthHint = ""
        private set
    
    fun getSecretNumber(): String = secretNumber
    fun getRemainingAttempts(): Int = remainingAttempts
    fun getCurrentScore(): Int = currentScore
    fun getCorrectAnswer(): String = secretNumber
    fun isGameWon(guess: String? = null): Boolean = guess?.let { it == secretNumber } ?: isGameWon
    fun isGameOver(): Boolean = remainingAttempts <= 0 || isGameWon

    fun startNewGame(level: Int = 1) {
        currentLevel = level
        numbers.clear()
        numbers.addAll(0..9)
        generateSecretNumber()
        remainingAttempts = MAX_ATTEMPTS
        currentScore = INITIAL_SCORE
        isGameWon = false
        pathSelection = (1..3).random()
        generateHints()
    }
    
    private fun generateSecretNumber() {
        if (currentLevel == 3) {
            secretNumber = numbers.shuffled().take(DIGITS_LEVEL_3).joinToString("")
        } else {
            secretNumber = numbers.shuffled().take(DIGITS_LEVEL_1_2).joinToString("")
        }
    }
    
    private fun generateHints() {
        if (currentLevel == 3) {
            generateLevel3Hints()
        } else {
            generateLevel1And2Hints()
        }
    }
    
    private fun generateLevel3Hints() {
        val (a, b, c, d) = secretNumber.map { it }
        val secretDigits = setOf(a, b, c, d)
        
        // İlk iki hint için x havuzu
        val firstPoolX = (0..9)
            .map { it.toString()[0] }
            .filterNot { it in secretDigits }
            .shuffled()
            .toMutableList()
            
        // Son üç hint için x havuzu
        val secondPoolX = (0..9)
            .map { it.toString()[0] }
            .filterNot { it in secretDigits }
            .shuffled()
            .toMutableList()
        
        fun replaceX(pattern: String, isFirstPool: Boolean): String {
            val availableX = if (isFirstPool) firstPoolX else secondPoolX
            return buildString {
                pattern.forEach { char ->
                    append(
                        when (char) {
                            'x' -> availableX.removeAt(0)
                            'a' -> a
                            'b' -> b
                            'c' -> c
                            'd' -> d
                            else -> char
                        }
                    )
                }
            }
        }
        val level_3_path_selection = (1..4).random()

        val (first, second, third, fourth, fifth) = when (level_3_path_selection) {
            1 -> listOf(
                LEVEL_3_SINGLE_TRUE.get(0),       // Sadece A doğru yerde
                LEVEL_3_SINGLE_FALSE_A.random(), // Sadece A var ama yanlış yerde
                LEVEL_3_BC_FIRST_TRUE.random(),  // B ve C var, B doğru yerde
                LEVEL_3_CD_FIRST_TRUE.random(), // C ve D var, C doğru yerde
                LEVEL_3_BD_TRUE.random(),  // B ve D var, ikisi de doğru yerde
                
            )
            2 -> listOf(
                LEVEL_3_SINGLE_TRUE.get(1),      // Sadece B doğru yerde
                LEVEL_3_SINGLE_FALSE_B.random(), // Sadece B var ama yanlış yerde
                LEVEL_3_AC_FIRST_TRUE.random(),  // A ve C var, A doğru yerde
                LEVEL_3_DA_FIRST_TRUE.random(),  // A ve D var, D doğru yerde
                LEVEL_3_CD_TRUE.random(),  // C ve D var, ikisi de doğru yerde
            )
            3 -> listOf(
                LEVEL_3_SINGLE_TRUE.get(2),       // Sadece C doğru yerde
                LEVEL_3_SINGLE_FALSE_C.random(), // Sadece C var ama yanlış yerde
                LEVEL_3_CA_FIRST_TRUE.random(),  // C ve A var, C doğru yerde
                LEVEL_3_CB_FIRST_TRUE.random(),  // C ve B var, C doğru yerde
                LEVEL_3_AD_TRUE.random(),       // A ve D var, ikisi de doğru yerde
            )
            else -> listOf(
                LEVEL_3_SINGLE_TRUE.get(3),       // Sadece D doğru yerde
                LEVEL_3_SINGLE_FALSE_D.random(), // Sadece D var ama yanlış yerde
                LEVEL_3_AB_FIRST_TRUE.random(),  // A ve B var, A doğru yerde
                LEVEL_3_BC_FIRST_TRUE.random(),  // B ve C var, B doğru yerde
                LEVEL_3_AC_TRUE.random(),  // A ve C var, ikisi de doğru yerde
            )
        }
        
        firstHint = replaceX(first, true)
        secondHint = replaceX(second, true)
        thirdHint = replaceX(third, false)
        fourthHint = replaceX(fourth, false)
        fifthHint = replaceX(fifth, false)
    }
    
    
    private fun generateLevel1And2Hints() {
        val (a, b, c) = secretNumber.map { it }
        
        val availableX = (0..9)
            .map { it.toString()[0] }
            .filterNot { it in setOf(a, b, c) }
            .toMutableList()
            .shuffled()
            .toMutableList()
        
        fun replaceX(pattern: String): String = buildString {
            pattern.forEach { char ->
                append(
                    when (char) {
                        'x' -> availableX.removeAt(0)
                        'a' -> a
                        'b' -> b
                        'c' -> c
                        else -> char
                    }
                )
            }
        }

        val temp = Random.nextBoolean()
        
        val (first, second, third, fourth, fifth) = when (pathSelection) {
            1 -> listOf(
                LEVEL_1_2_ONLY_A_CORRECT.random(),
                LEVEL_1_2_FIRST_A_CHOICES.random(),
                if (temp) LEVEL_1_2_AB_FALSE.random() else LEVEL_1_2_AC_FALSE.random(),
                LEVEL_1_2_CB_FALSE.random(),
                if (temp) "cbx" else "bxc"
            )
            2 -> listOf(
                LEVEL_1_2_ONLY_B_CORRECT.random(),
                LEVEL_1_2_FIRST_B_CHOICES.random(),
                if (temp) LEVEL_1_2_AB_FALSE.random() else LEVEL_1_2_CB_FALSE.random(),
                LEVEL_1_2_AC_FALSE.random(),
                if (temp) "acx" else "xac"
            )
            else -> listOf(
                LEVEL_1_2_ONLY_C_CORRECT.random(),
                LEVEL_1_2_FIRST_C_CHOICES.random(),
                if (temp) LEVEL_1_2_AC_FALSE.random() else LEVEL_1_2_CB_FALSE.random(),
                LEVEL_1_2_AB_FALSE.random(),
                if (temp) "axb" else "xba"
            )
        }

        firstHint = replaceX(first)
        secondHint = replaceX(second)
        thirdHint = replaceX(third)
        fourthHint = replaceX(fourth)
        fifthHint = replaceX(fifth)
    }
    
    fun makeGuess(guess: String): GuessResult {
        val digits = if (currentLevel == 3) DIGITS_LEVEL_3 else DIGITS_LEVEL_1_2
        require(guess.length == digits) { "Tahmin $digits basamaklı olmalıdır" }

        remainingAttempts--
        
        val secretChars = secretNumber.toCharArray()
        val guessChars = guess.toCharArray()
        
        var correct = 0
        var misplaced = 0
        
        // Mark used positions to avoid double counting
        val usedSecret = BooleanArray(digits) { false }
        val usedGuess = BooleanArray(digits) { false }
        
        // First pass: count correct positions
        for (i in secretChars.indices) {
            if (secretChars[i] == guessChars[i]) {
                correct++
                usedSecret[i] = true
                usedGuess[i] = true
            }
        }
        
        // Second pass: count misplaced digits
        for (i in guessChars.indices) {
            if (!usedGuess[i]) {  // Skip if this position was already counted
                for (j in secretChars.indices) {
                    if (!usedSecret[j] && guessChars[i] == secretChars[j]) {
                        misplaced++
                        usedSecret[j] = true
                        usedGuess[i] = true
                        break
                    }
                }
            }
        }
        
        if (correct == digits) {
            isGameWon = true
        }
        
        return GuessResult(correct, misplaced)
    }
    
    data class GuessResult(val correct: Int, val misplaced: Int)
}

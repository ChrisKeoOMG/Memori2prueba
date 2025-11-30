package com.example.Memori2

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.memori2.MainActivity
import com.example.memori2.Music
import com.example.memori2.TutorialActivity
import kotlin.jvm.java

class BallView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    private val paint = Paint().apply {
        color = Color.BLUE
        isAntiAlias = true
    }
    private val textPaint = Paint().apply {
        color = Color.BLACK
        textSize = 40f
        isAntiAlias = true
    }

    private var xPos = 0f
    private var yPos = 0f
    private var radius = 40f
    private var xVel = 0f
    private var yVel = 0f
    private var isPositionInitialized = false

    private var currentLevel = 1
    private var score = 0
    private var startTime: Long = 0
    private var level2Score = 0
    private var level2TimeSeconds = 0.0

    private val maxScorePerLevel = 1000
    private val scoreBaseTimeMs = 30000L

    private val obstacles = mutableListOf<Obstacle>()

    // Meta
    private val goalPaint = Paint().apply {
        color = Color.GREEN
        style = Paint.Style.FILL
    }
    private var goalX = 0f
    private var goalY = 0f
    private val goalSize = 80f
    private var gamePaused = false

    private inner class Obstacle(
        val x: Float,
        val y: Float,
        val width: Float,
        val height: Float,
        val color: Int = Color.DKGRAY
    ) {
        fun collidesWith(ballX: Float, ballY: Float, ballRadius: Float): Boolean {
            val closestX = ballX.coerceIn(x, x + width)
            val closestY = ballY.coerceIn(y, y + height)
            val distX = ballX - closestX
            val distY = ballY - closestY
            val distanceSquared = (distX * distX) + (distY * distY)
            return distanceSquared < (ballRadius * ballRadius)
        }
    }

    init {
        paint.color = Color.BLUE
    }



    private fun createObstacles() {
        obstacles.clear()
        val w = width.toFloat()
        val h = height.toFloat()
        val obsW = 20f






        obstacles.add(Obstacle(0f, 0f, w, obsW))
        obstacles.add(Obstacle(0f, h - obsW, w, obsW))
        obstacles.add(Obstacle(0f, 0f, obsW, h))
        obstacles.add(Obstacle(w - obsW, 0f, obsW, h))

        if (currentLevel == 1) {
            obstacles.add(Obstacle(w * 0.2f, 0f, obsW, h * 0.7f))
            obstacles.add(Obstacle(w * 0.4f, h * 0.3f, obsW, h * 0.7f))
            obstacles.add(Obstacle(w * 0.6f, 0f, obsW, h * 0.6f))
            obstacles.add(Obstacle(w * 0.8f, h * 0.4f, obsW, h * 0.6f))

            obstacles.add(Obstacle(w * 0.1f, h * 0.1f, w * 0.7f, obsW))
            obstacles.add(Obstacle(w * 0.75f, h * 0.2f, obsW, h * 0.2f))
        } else if (currentLevel == 2) {
            obstacles.add(Obstacle(w * 0.15f, h * 0.2f, obsW, h * 0.6f))
            obstacles.add(Obstacle(w * 0.35f, 0f, obsW, h * 0.5f))
            obstacles.add(Obstacle(w * 0.35f, h * 0.7f, obsW, h * 0.5f))
            obstacles.add(Obstacle(w * 0.55f, h * 0.3f, obsW, h * 0.7f))


            obstacles.add(Obstacle(w * 0.15f, h * 0.15f, w * 0.4f, obsW))
            obstacles.add(Obstacle(w * 0.45f, h * 0.8f, w * 0.4f, obsW))


            obstacles.add(Obstacle(w * 0.7f, 0f, obsW, h * 0.4f))
            obstacles.add(Obstacle(w * 0.7f, h * 0.6f, obsW, h * 0.4f))

            obstacles.add(Obstacle(w * 0.85f, h * 0.2f, obsW, h * 0.5f))
        }

        goalX = w - goalSize - 30f
        goalY = h - goalSize - 30f
    }


    fun updatePosition(dx: Float, dy: Float) {
        if (!isPositionInitialized || gamePaused) return


        xVel += dx
        yVel += dy
        val prevX = xPos
        val prevY = yPos

        xPos -= xVel
        for (obs in obstacles) {
            if (obs.collidesWith(xPos, yPos, radius)) {
                xPos = prevX
                xVel = -xVel * 0.5f
                break
            }
        }
        yPos += yVel
        for (obs in obstacles) {
            if (obs.collidesWith(xPos, yPos, radius)) {
                yPos = prevY
                yVel = -yVel * 0.5f
                break
            }
        }

        xVel *= 0.7f
        yVel *= 0.7f
        if (xPos < radius) {
            xPos = radius
            xVel = -xVel * 0.5f
        } else if (xPos > width - radius) {
            xPos = width - radius
            xVel = -xVel * 0.5f
        }

        if (yPos < radius) {
            yPos = radius
            yVel = -yVel * 0.5f
        } else if (yPos > height - radius) {
            yPos = height - radius
            yVel = -yVel * 0.5f
        }

        // 6. Meta
        if (xPos > goalX && xPos < goalX + goalSize &&
            yPos > goalY && yPos < goalY + goalSize
        ) {
            nextLevel()
        }

        invalidate()
    }

    private fun nextLevel() {
        val endTime = System.currentTimeMillis()
        val timeTakenMs = endTime - startTime
        val timeTakenSeconds = timeTakenMs / 1000.0

        // CÁLCULO DE PUNTUACIÓN (se mantiene)
        val scoreReduction = if (timeTakenMs >= scoreBaseTimeMs) {
            maxScorePerLevel
        } else {
            ((timeTakenMs.toFloat() / scoreBaseTimeMs.toFloat()) * maxScorePerLevel).toInt()
        }

        val pointsEarned = maxOf(0, maxScorePerLevel - scoreReduction)
        score += pointsEarned

        val toastMessage = String.format(
            "¡Nivel completado en %.2f s! Ganaste %d puntos.",
            timeTakenSeconds,
            pointsEarned,
            score
        )
        Toast.makeText(context, toastMessage, Toast.LENGTH_LONG).show()
        if (currentLevel == 2) {
            level2Score = score
            level2TimeSeconds = timeTakenSeconds

            gamePaused = true
            showEndGameDialog()

        } else {
            currentLevel++
            createObstacles()
            resetBallPosition()
        }
    }

    private fun showEndGameDialog() {
        post {
            AlertDialog.Builder(context)
                .setTitle("¡Juego Completado!")
                .setMessage(
                    "¡Felicitaciones! Has completado ambos niveles.\n\n" +
                            "Puntaje Final: $level2Score puntos\n" +
                            "Tiempo Nivel 2: ${String.format("%.2f s", level2TimeSeconds)}"
                )
                .setPositiveButton("Jugar de Nuevo") { dialog, _ ->
                    resetGame()
                    dialog.dismiss()
                }
                .setNegativeButton("Volver al Menú") { dialog, _ ->
                    val intent = Intent(context, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                    dialog.dismiss()
                }
                .setCancelable(false)
                .show()
        }
    }
    fun resetGame() {
        currentLevel = 1
        score = 0
        level2Score = 0
        level2TimeSeconds = 0.0
        gamePaused = false
        createObstacles()
        resetBallPosition()
        invalidate()
    }


    private fun resetBallPosition() {
        xPos = radius + 30f
        yPos = radius + 30f
        xVel = 0f
        yVel = 0f
        startTime = System.currentTimeMillis()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        createObstacles()
        if (!isPositionInitialized) {
            resetBallPosition()
            isPositionInitialized = true
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for (obs in obstacles) {
            val p = Paint().apply { color = obs.color; style = Paint.Style.FILL }
            canvas.drawRect(obs.x, obs.y, obs.x + obs.width, obs.y + obs.height, p)
        }
        canvas.drawRect(goalX, goalY, goalX + goalSize, goalY + goalSize, goalPaint)
        canvas.drawCircle(xPos, yPos, radius, paint)
        if (isPositionInitialized) {
            val currentTime = System.currentTimeMillis()
            val timeElapsedMs = currentTime - startTime

            val timeElapsedSeconds = timeElapsedMs / 1000.0

            val timeText = String.format("Tiempo: %.1f s", timeElapsedSeconds)
            val scoreText = "Puntaje:  $score"
            val levelText = "Nivel:  $currentLevel"
            canvas.drawText(levelText, 30f, 60f, textPaint)
            canvas.drawText(scoreText, 30f, 110f, textPaint)
            canvas.drawText(timeText, 30f, 160f, textPaint)

            if (!gamePaused) {
                postInvalidateDelayed(100)
            }
        }
    }
}
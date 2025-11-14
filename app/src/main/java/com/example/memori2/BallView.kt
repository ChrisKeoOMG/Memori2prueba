package com.example.Memori2

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.widget.Toast
import com.example.memori2.R

class BallView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    private val paint = Paint().apply {
        color = Color.BLUE
        isAntiAlias = true
    }

    private var xPos = 0f
    private var yPos = 0f
    private var radius = 40f
    private var xVel = 0f
    private var yVel = 0f
    private var isPositionInitialized = false

    private val obstacles = mutableListOf<Obstacle>()

    // Meta
    private val goalPaint = Paint().apply {
        color = Color.GREEN
        style = Paint.Style.FILL
    }
    private var goalX = 0f
    private var goalY = 0f
    private val goalSize = 80f

    private var currentLevel = 1 // Nivel actual

    private inner class Obstacle(
        val x: Float,
        val y: Float,
        val width: Float,
        val height: Float,
        val color: Int = Color.DKGRAY
    ) {
        fun collidesWith(ballX: Float, ballY: Float, ballRadius: Float): Boolean {
            return ballX + ballRadius >= x &&
                    ballX - ballRadius <= x + width &&
                    ballY + ballRadius >= y &&
                    ballY - ballRadius <= y + height
        }
    }

    init {
        paint.color = Color.BLUE
    }

    private fun createObstacles() {
        obstacles.clear()
        val w = width.toFloat()
        val h = height.toFloat()

        // Bordes
        obstacles.add(Obstacle(0f, 0f, w, 20f))
        obstacles.add(Obstacle(0f, h - 20f, w, 20f))
        obstacles.add(Obstacle(0f, 0f, 20f, h))
        obstacles.add(Obstacle(w - 20f, 0f, 20f, h))

        if (currentLevel == 1) {
            // Nivel 1
            obstacles.add(Obstacle(w * 0.2f, 0f, 20f, h * 0.7f))
            obstacles.add(Obstacle(w * 0.4f, h * 0.3f, 20f, h * 0.7f))
            obstacles.add(Obstacle(w * 0.6f, 0f, 20f, h * 0.6f))
            obstacles.add(Obstacle(w * 0.8f, h * 0.4f, 20f, h * 0.6f))
        } else if (currentLevel == 2) {
            // Nivel 2
            obstacles.add(Obstacle(w * 0.15f, h * 0.2f, 20f, h * 0.6f))
            obstacles.add(Obstacle(w * 0.35f, 0f, 20f, h * 0.5f))
            obstacles.add(Obstacle(w * 0.35f, h * 0.8f, 20f, h * 0.4f))
            obstacles.add(Obstacle(w * 0.55f, h * 0.3f, 20f, h * 0.7f))
            obstacles.add(Obstacle(w * 0.7f, 0f, 20f, h * 0.4f))
            obstacles.add(Obstacle(w * 0.7f, h * 0.7f, 20f, h * 0.7f))
            obstacles.add(Obstacle(w * 0.85f, h * 0.5f, 20f, h * 0.5f))
        }

        // Meta
        goalX = w - goalSize - 30f
        goalY = h - goalSize - 30f
    }

    fun updatePosition(dx: Float, dy: Float) {
        if (!isPositionInitialized) return

        val prevX = xPos
        val prevY = yPos

        xVel += dx
        yVel += dy
        xPos -= xVel
        yPos += yVel
        xVel *= 0.7f
        yVel *= 0.7f

        // Colisiones
        for (obs in obstacles) {
            if (obs.collidesWith(xPos, yPos, radius)) {
                xPos = prevX
                yPos = prevY
                xVel = -xVel * 0.5f
                yVel = -yVel * 0.5f
                break
            }
        }

        // Bordes
        if (xPos < radius) xPos = radius
        if (xPos > width - radius) xPos = width - radius
        if (yPos < radius) yPos = radius
        if (yPos > height - radius) yPos = height - radius

        // Meta
        if (xPos > goalX && xPos < goalX + goalSize &&
            yPos > goalY && yPos < goalY + goalSize
        ) {
            nextLevel()
        }

        invalidate()
    }

    private fun nextLevel() {
        currentLevel++
        if (currentLevel > 2) currentLevel = 1

        // Mostrar mensaje
        Toast.makeText(context, "¡Felicidades! Pasaste al nivel $currentLevel", Toast.LENGTH_SHORT).show()

        createObstacles()
        resetBallPosition()
    }

    private fun resetBallPosition() {
        xPos = radius + 30f
        yPos = radius + 30f
        xVel = 0f
        yVel = 0f
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
    }
}

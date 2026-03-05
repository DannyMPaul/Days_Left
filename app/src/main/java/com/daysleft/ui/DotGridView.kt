package com.daysleft.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.daysleft.R
import com.daysleft.data.DayStatus
import com.daysleft.util.Constants
import com.daysleft.util.DateUtils
import kotlin.math.min

/**
 * Custom view to display the 52x7 grid of dots representing the year.
 * Dots are coloured by status: PAST (dim teal), CURRENT (bright cyan), FUTURE (near-black).
 * Tapping dots does nothing — the Good/Bad marking feature has been removed.
 */
class DotGridView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var dotSize = 0f
    private var dotSpacing = 0f
    private var gridWidth = 0f
    private var gridHeight = 0f
    private var offsetX = 0f
    private var offsetY = 0f

    // Colors
    private val colorFuture = ContextCompat.getColor(context, R.color.dot_future)
    private val colorCurrent = ContextCompat.getColor(context, R.color.dot_current)
    private val colorPast = ContextCompat.getColor(context, R.color.dot_past)

    // Day data
    private var totalDays = DateUtils.getTotalDaysInCurrentYear()
    private var currentDay = DateUtils.getCurrentDayOfYear()
    private val dayStatuses = mutableMapOf<Int, DayStatus>()

    init {
        // Initialize all days as future by default
        for (i in 1..totalDays) {
            dayStatuses[i] = DayStatus.FUTURE
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        calculateDimensions()
    }

    private fun calculateDimensions() {
        val availableWidth = width - paddingLeft - paddingRight
        val availableHeight = height - paddingTop - paddingBottom

        // Calculate dot size based on available space
        val maxDotWidth = availableWidth / (Constants.GRID_COLUMNS + (Constants.GRID_COLUMNS - 1) * 0.5f)
        val maxDotHeight = availableHeight / (Constants.GRID_ROWS + (Constants.GRID_ROWS - 1) * 0.5f)

        dotSize = min(maxDotWidth, maxDotHeight)
        dotSpacing = dotSize * 0.3f

        gridWidth = Constants.GRID_COLUMNS * dotSize + (Constants.GRID_COLUMNS - 1) * dotSpacing
        gridHeight = Constants.GRID_ROWS * dotSize + (Constants.GRID_ROWS - 1) * dotSpacing

        // Center the grid
        offsetX = paddingLeft + (availableWidth - gridWidth) / 2
        offsetY = paddingTop + (availableHeight - gridHeight) / 2
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        var dayOfYear = 1

        for (row in 0 until Constants.GRID_ROWS) {
            for (col in 0 until Constants.GRID_COLUMNS) {
                if (dayOfYear > totalDays) break

                val x = offsetX + col * (dotSize + dotSpacing) + dotSize / 2
                val y = offsetY + row * (dotSize + dotSpacing) + dotSize / 2

                paint.color = getColorForDay(dayOfYear)
                canvas.drawCircle(x, y, dotSize / 2, paint)

                dayOfYear++
            }
        }
    }

    private fun getColorForDay(dayOfYear: Int): Int {
        return when (dayStatuses[dayOfYear] ?: DayStatus.FUTURE) {
            DayStatus.FUTURE  -> colorFuture
            DayStatus.CURRENT -> colorCurrent
            DayStatus.PAST    -> colorPast
        }
    }

    /**
     * Set all day statuses at once
     */
    fun setDayStatuses(statuses: Map<Int, DayStatus>) {
        dayStatuses.clear()
        dayStatuses.putAll(statuses)
        invalidate()
    }

    /**
     * Update current day and total days (for year changes)
     */
    fun updateYearData(currentDayOfYear: Int, totalDaysInYear: Int) {
        currentDay = currentDayOfYear
        totalDays = totalDaysInYear
        invalidate()
    }
}

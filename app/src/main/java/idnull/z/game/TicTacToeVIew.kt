package idnull.z.game

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import kotlin.math.max
import kotlin.math.min
import kotlin.properties.Delegates


typealias OnCellActionListener = (row: Int, column: Int, field: TicTacToeField) -> Unit


class TicTacToeView(
    context: Context,
    attributesSet: AttributeSet?,
    defStyleAttr: Int,
    defStyleRes: Int
) : View(context, attributesSet, defStyleAttr, defStyleRes) {

    constructor(context: Context, attributesSet: AttributeSet?, defStyleAttr: Int) :
            this(context, attributesSet, defStyleAttr, R.style.DefaultTicTacToeFieldStyle)

    constructor(context: Context, attributesSet: AttributeSet?) :
            this(context, attributesSet, R.attr.ticTacToeFieldStyle)

    constructor(context: Context) : this(context, null)

    var field: TicTacToeField? = null
        set(value) {

            field?.listeners?.remove(listener)
            field = value
            value?.listeners?.add(listener)
            updateViewSizes() // safe zone rect, cell size, cell padding
            requestLayout() //view size changed
            invalidate() // redraw view
        }

    var actionListener: OnCellActionListener? = null

    private var player1Color by Delegates.notNull<Int>()
    private var player2Color by Delegates.notNull<Int>()
    private var gridColor by Delegates.notNull<Int>()

    private val listener: OnFieldChangedListener = {
        invalidate()
    }


    // safe zone rect where we can drawing
    private val fieldRect = RectF(0f, 0f, 0f, 0f)

    private var cellSize: Float = 0f

    private var cellPadding: Float = 0f

    private val cellRect = RectF()

    private lateinit var player1Paint: Paint
    private lateinit var player2Paint: Paint
    private lateinit var gridPaint: Paint


    init {
        if (attributesSet != null) {
            initAttributes(attributesSet, defStyleAttr, defStyleRes)
        } else {
            initDefaultColors()
        }
        initPaints()
    }

    private fun initAttributes(attributesSet: AttributeSet, defStyleAttr: Int, defStyleRes: Int) {
        val typedArray = context.obtainStyledAttributes(
            attributesSet,
            R.styleable.TicTacToeView,
            defStyleAttr,
            defStyleRes
        )
        player1Color =
            typedArray.getColor(R.styleable.TicTacToeView_player1Color, PLAYER1_DEFAULT_COLOR)
        player2Color =
            typedArray.getColor(R.styleable.TicTacToeView_player2Color, PLAYER2_DEFAULT_COLOR)
        gridColor = typedArray.getColor(R.styleable.TicTacToeView_gridColor, GRID_DEFAULT_COLOR)

        typedArray.recycle()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        field?.listeners?.add { listener }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // min size of our view
        val minWidth = suggestedMinimumWidth + paddingLeft + paddingRight
        val minHeight = suggestedMinimumHeight + paddingTop + paddingBottom

        // calculating desired size of view
        val desiredCellSizeInPixels = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, DESIRED_CELL_SIZE,
            resources.displayMetrics
        ).toInt()

        val rows = field?.rows ?: 0
        val columns = field?.columns ?: 0
        val desiredWith =
            max(minWidth, columns * desiredCellSizeInPixels + paddingLeft + paddingRight)
        val desiredHeight =
            max(minHeight, rows * desiredCellSizeInPixels + paddingTop + paddingBottom)

        // submit view size
        setMeasuredDimension(
            resolveSize(desiredWith, widthMeasureSpec),
            resolveSize(desiredHeight, heightMeasureSpec)
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateViewSizes()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (field == null) return
        if (cellSize == 0f) return
        if (fieldRect.width() <= 0) return
        if (fieldRect.height() <= 0) return

        drawGrid(canvas)
        drawCells(canvas)
    }


    override fun onTouchEvent(event: MotionEvent): Boolean {
        val field = this.field ?: return false
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val row = getRow(event)
                val column = getColumn(event)
                return if (row >= 0 && column >= 0 && row < field.rows && column < field.columns) {
                    actionListener?.invoke(row, column, field)
                    true
                } else {
                    false
                }

            }
            MotionEvent.ACTION_UP -> {
                return performClick()
            }
        }
        return false
    }


    private fun getRow(event: MotionEvent): Int {
        return ((event.y - fieldRect.top) / cellSize).toInt()
    }

    private fun getColumn(event: MotionEvent): Int {
        return ((event.x - fieldRect.left) / cellSize).toInt()
    }


    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        field?.listeners?.remove { listener }
    }


    /**********************  Start   draw Section  ********************/
    private fun drawGrid(canvas: Canvas) {
        val field = this.field ?: return
        val xStart = fieldRect.left
        val xEnd = fieldRect.right
        for (i in 0..field.rows) {
            val y = fieldRect.top + cellSize * i
            canvas.drawLine(xStart, y, xEnd, y, gridPaint)
        }

        val yStart = fieldRect.top
        val yEnd = fieldRect.bottom
        for (i in 0..field.columns) {
            val x = fieldRect.left + cellSize * i
            canvas.drawLine(x, yStart, x, yEnd, gridPaint)
        }
    }

    private fun drawCells(canvas: Canvas) {
        val field = this.field ?: return
        for (row in 0 until field.rows) {
            for (column in 0 until field.columns) {
                val cell = field.getCell(row, column)
                if (cell == Cell.PLAYER_1) {
                    drawPlayer1(canvas, row, column)
                } else if (cell == Cell.PLAYER_2) {
                    drawPlayer2(canvas, row, column)
                }
            }
        }
    }


    private fun drawPlayer1(canvas: Canvas, row: Int, column: Int) {
        val cellRect = getCellRect(row, column)
        canvas.drawLine(cellRect.left, cellRect.top, cellRect.right, cellRect.bottom, player1Paint)
        canvas.drawLine(cellRect.right, cellRect.top, cellRect.left, cellRect.bottom, player1Paint)
    }

    private fun drawPlayer2(canvas: Canvas, row: Int, column: Int) {
        val cellRect = getCellRect(row, column)
        canvas.drawCircle(
            cellRect.centerX(),
            cellRect.centerY(),
            cellRect.width() / 2,
            player2Paint
        )
    }

    private fun initPaints() {
        // paint for drawing X
        player1Paint = Paint(Paint.ANTI_ALIAS_FLAG)
        player1Paint.color = player1Color
        player1Paint.style = Paint.Style.STROKE
        player1Paint.strokeWidth = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            3f,
            resources.displayMetrics
        )

        // paint for drawing O
        player2Paint = Paint(Paint.ANTI_ALIAS_FLAG)
        player2Paint.color = player2Color
        player2Paint.style = Paint.Style.STROKE
        player2Paint.strokeWidth = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            3f,
            resources.displayMetrics
        )

        // paint for drawing grid
        gridPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        gridPaint.color = gridColor
        gridPaint.style = Paint.Style.STROKE
        gridPaint.strokeWidth = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            1f,
            resources.displayMetrics
        )
    }
    private fun getCellRect(row: Int, column: Int): RectF {
        cellRect.left = fieldRect.left + column * cellSize + cellPadding
        cellRect.top = fieldRect.top + row * cellSize + cellPadding
        cellRect.right = cellRect.left + cellSize - cellPadding * 2
        cellRect.bottom = cellRect.top + cellSize - cellPadding * 2
        return cellRect
    }
    /*********************** End draw Section  *******************/
    private fun updateViewSizes() {
        val field = this.field ?: return
        val safeWidth = width - paddingLeft - paddingRight
        val safeHeight = height - paddingTop - paddingBottom
        val cellWidth = safeWidth / field.columns.toFloat()
        val cellHeight = safeHeight / field.rows.toFloat()
        cellSize = min(cellWidth, cellHeight)
        cellPadding = cellSize * 0.2f

        val fieldWidth = cellSize * field.columns
        val fieldHeight = cellSize * field.rows
        fieldRect.left = paddingLeft + (safeWidth - fieldWidth) / 2
        fieldRect.top = paddingTop + (safeHeight - fieldHeight) / 2
        fieldRect.right = fieldRect.left + fieldWidth
        fieldRect.bottom = fieldRect.top + fieldHeight
    }

    private fun initDefaultColors() {
        player1Color = PLAYER1_DEFAULT_COLOR
        player2Color = PLAYER2_DEFAULT_COLOR
        gridColor = GRID_DEFAULT_COLOR
    }

    companion object {
        const val PLAYER1_DEFAULT_COLOR = Color.GREEN
        const val PLAYER2_DEFAULT_COLOR = Color.RED
        const val GRID_DEFAULT_COLOR = Color.GRAY
        const val DESIRED_CELL_SIZE = 50f
    }
}
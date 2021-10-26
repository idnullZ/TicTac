package idnull.z.game

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import idnull.z.game.databinding.ActivityMainBinding



class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private var isFirstPlayer = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.view.field = TicTacToeField(3, 3)


        // listening user actions
        binding.view.actionListener = { row, column, currentField ->
            val cell = currentField.getCell(row, column)

            if (cell == Cell.EMPTY) {
                // cell is empty, changing it to X or O
                if (isFirstPlayer) {
                    currentField.setCell(row, column, Cell.PLAYER_1)
                } else {
                    currentField.setCell(row, column, Cell.PLAYER_2)
                }
                isFirstPlayer = !isFirstPlayer
            }
        }
    }
}
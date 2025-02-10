package com.example.sudokugame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.sudokugame.ui.theme.SudokuGameTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SudokuGameTheme {
                SudokuScreen()
            }
        }
    }
}

@Composable
fun SudokuScreen() {
    var grid by remember { mutableStateOf(generateSudokuGrid()) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var selectedCell by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var inputValue by remember { mutableStateOf(TextFieldValue()) }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = { grid = generateSudokuGrid() }) {
            Text("Reset Sudoku")
        }

        LazyVerticalGrid(columns = GridCells.Fixed(9), modifier = Modifier.padding(16.dp)) {
            itemsIndexed(grid) { index, value ->
                val row = index / 9
                val col = index % 9

                Card(
                    modifier = Modifier
                        .padding(4.dp)
                        .size(40.dp)
                        .clickable {
                            if (grid[index] == 0) {
                                selectedCell = row to col
                            }
                        },
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text(text = if (value != 0) value.toString() else "")
                    }
                }
            }
        }

        selectedCell?.let { (row, col) ->
            AlertDialog(
                onDismissRequest = { selectedCell = null },
                title = { Text("Enter Number") },
                text = {
                    TextField(
                        value = inputValue,
                        onValueChange = { newValue -> inputValue = newValue },
                        singleLine = true
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        val number = inputValue.text.toIntOrNull()
                        if (number in 1..9) {
                            val index = row * 9 + col
                            grid = grid.toMutableList().apply {
                                if (number != null) {
                                    this[index] = number
                                }
                            }
                            selectedCell = null
                            inputValue = TextFieldValue()
                            if (isSudokuComplete(grid)) {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("You won!")
                                }
                            }
                        }
                    }) {
                        Text("OK")
                    }
                }
            )
        }
        SnackbarHost(hostState = snackbarHostState)
    }
}

fun generateSudokuGrid(): List<Int> {
    val firstRow = (1..9).shuffled()
    return firstRow + List(72) { 0 }
}

//fun isSudokuComplete(grid: List<Int>): Boolean {
//    return grid.none { it == 0 }
//}

 fun isSudokuComplete(grid: List<Int>): Boolean {
    val size = 9
    val validSet = (1..9).toSet()

    // Check rows
    for (i in 0 until size) {
        val rowSet = grid.subList(i * size, (i + 1) * size).toSet()
        if (rowSet != validSet) return false
    }

    // Check columns
    for (i in 0 until size) {
        val columnSet = (0 until size).map { grid[it * size + i] }.toSet()
        if (columnSet != validSet) return false
    }

    // Check 3x3 blocks
    for (row in 0 until size step 3) {
        for (col in 0 until size step 3) {
            val blockSet = mutableSetOf<Int>()
            for (r in 0 until 3) {
                for (c in 0 until 3) {
                    blockSet.add(grid[(row + r) * size + (col + c)])
                }
            }
            if (blockSet != validSet) return false
        }
    }

    return true
}

package com.example.myapplicationtodolist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.myapplicationtodolist.ui.theme.MyApplicationToDoListTheme
import com.google.apps.card.v1.Widget

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationToDoListTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TodoScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoScreen() {
    // Liste temporare în memorie pentru a simula comportamentul CRUD în UI
    var todoList by remember { mutableStateOf(listOf<TodoItem>()) }
    var showDialog by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<TodoItem?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Planurile Mele (CRUD)") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                itemToEdit = null // Pregătim dialogul pentru un item nou (Create)
                showDialog = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "Adaugă sarcină")
            }
        }
    ) { paddingValues ->

        // R - READ: Afișarea listei de sarcini
        if (todoList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Nicio sarcină momentan. Apasă pe + pentru a adăuga!")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            )
            {
                items(todoList) { item ->
                    TodoRow(
                        item = item,
                        onCheckedChange = { checked ->
                            // U - UPDATE: Schimbarea stării bifei (Complete/Incomplete)
                            todoList = todoList.map {
                                if (it.id == item.id) it.copy(isCompleted = checked) else it
                            }
                        },
                        onEditClick = {
                            // U - UPDATE: Deschiderea dialogului de editare
                            itemToEdit = item
                            showDialog = true
                        },
                        onDeleteClick = {
                            // D - DELETE: Ștergerea elementului din listă
                            todoList = todoList.filter { it.id != item.id }
                        }
                    )
                }
            }
        }

        // C & U - CREATE & UPDATE: Dialogul prin care introducem sau modificăm datele
        if (showDialog) {
            TodoDialog(
                item = itemToEdit,
                onDismiss = { showDialog = false },
                onSave = { title, desc ->
                    if (itemToEdit == null) {
                        // CREATE: Adăugare item nou
                        todoList = todoList + TodoItem(title = title, description = desc)
                    } else {
                        // UPDATE: Salvare modificări item existent
                        todoList = todoList.map {
                            if (it.id == itemToEdit!!.id) it.copy(title = title, description = desc) else it
                        }
                    }
                    showDialog = false
                }
            )
        }
    }
}

@Composable
fun TodoRow(
    item: TodoItem,
    onCheckedChange: (Boolean) -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isCompleted) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = item.isCompleted,
                onCheckedChange = onCheckedChange
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = if (item.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                )
                if (item.description.isNotEmpty()) {
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodySmall,
                        textDecoration = if (item.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    )
                }
            }

            IconButton(onClick = onEditClick) {
                Icon(Icons.Default.Edit, contentDescription = "Editează", tint = MaterialTheme.colorScheme.primary)
            }

            IconButton(onClick = onDeleteClick) {
                Icon(Icons.Default.Delete, contentDescription = "Șterge", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

private fun RowScope.Checkbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Widget
) {
}

@Composable
fun TodoDialog(
    item: TodoItem?,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var title by remember { mutableStateOf(item?.title ?: "") }
    var description by remember { mutableStateOf(item?.description ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (item == null) "Adaugă Sarcină" else "Editează Sarcina") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Titlu") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descriere (opțional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (title.isNotBlank()) onSave(title, description) },
                enabled = title.isNotBlank()
            ) {
                Text("Salvează")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Anulează")
            }
        }
    )
}

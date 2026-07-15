package com.kilotakip.app.ui.reminder

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

private val dayLabels = listOf("Pzt" to 1, "Sal" to 2, "Çar" to 3, "Per" to 4, "Cum" to 5, "Cmt" to 6, "Paz" to 7)
private val reminderTypes = listOf("su" to "Su İç", "ilac" to "İlaç", "hareket" to "Hareket Et", "ozel" to "Özel")

@Composable
fun AddReminderDialog(
    onDismiss: () -> Unit,
    onConfirm: (type: String, title: String, days: Set<Int>, startTime: String, endTime: String?, interval: Int?) -> Unit
) {
    var selectedType by remember { mutableStateOf("su") }
    var title by remember { mutableStateOf("Su içmeyi unutma!") }
    var selectedDays by remember { mutableStateOf(setOf(1, 2, 3, 4, 5, 6, 7)) }
    var startTime by remember { mutableStateOf("09:00") }
    var endTime by remember { mutableStateOf("21:00") }
    var interval by remember { mutableStateOf("60") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Yeni Hatırlatıcı") },
        text = {
            Column {
                LazyRow {
                    items(reminderTypes) { (value, label) ->
                        FilterChip(
                            selected = selectedType == value,
                            onClick = { selectedType = value },
                            label = { Text(label) },
                            modifier = Modifier.height(36.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = title, onValueChange = { title = it },
                    label = { Text("Başlık") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Günler")
                LazyRow {
                    items(dayLabels) { (label, value) ->
                        FilterChip(
                            selected = value in selectedDays,
                            onClick = {
                                selectedDays = if (value in selectedDays) selectedDays - value else selectedDays + value
                            },
                            label = { Text(label) },
                            modifier = Modifier.height(36.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    OutlinedTextField(
                        value = startTime, onValueChange = { startTime = it },
                        label = { Text("Başlangıç (SS:dd)") }, singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = endTime, onValueChange = { endTime = it },
                        label = { Text("Bitiş (SS:dd)") }, singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = interval, onValueChange = { interval = it },
                    label = { Text("Tekrar aralığı (dakika, opsiyonel)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(
                    selectedType, title, selectedDays, startTime,
                    endTime.ifBlank { null }, interval.toIntOrNull()
                )
            }) { Text("Kaydet") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Vazgeç") } }
    )
}

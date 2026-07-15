package com.kilotakip.app.ui.admin

import androidx.compose.material3.ExperimentalMaterial3Api

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(onBack: () -> Unit, viewModel: AdminViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    var tabIndex by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Yönetim Paneli") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = tabIndex) {
                Tab(selected = tabIndex == 0, onClick = { tabIndex = 0 }, text = { Text("Kullanıcılar") })
                Tab(selected = tabIndex == 1, onClick = { tabIndex = 1 }, text = { Text("IP Kara Liste") })
            }

            if (tabIndex == 0) {
                UsersTab(uiState.users, onToggleBan = viewModel::toggleBan, onEndSessions = viewModel::endSessions)
            } else {
                IpBlacklistTab(
                    ips = uiState.blacklistedIps,
                    onAdd = viewModel::blockIp,
                    onRemove = viewModel::unblockIp
                )
            }
        }
    }
}

@Composable
private fun UsersTab(
    users: List<com.kilotakip.app.data.remote.dto.AdminUserDto>,
    onToggleBan: (com.kilotakip.app.data.remote.dto.AdminUserDto) -> Unit,
    onEndSessions: (com.kilotakip.app.data.remote.dto.AdminUserDto) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        items(users) { user ->
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Text(user.name, style = MaterialTheme.typography.bodyLarge)
                            Text(user.phone ?: user.username ?: "-", style = MaterialTheme.typography.bodySmall)
                        }
                        Row {
                            IconButton(onClick = { onEndSessions(user) }) {
                                Icon(Icons.Filled.Logout, contentDescription = "Oturumu sonlandır")
                            }
                            IconButton(onClick = { onToggleBan(user) }) {
                                Icon(
                                    if (user.status == "banned") Icons.Filled.CheckCircle else Icons.Filled.Block,
                                    contentDescription = "Ban/Unban"
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Kayıt: ${user.created_at ?: "-"}", style = MaterialTheme.typography.bodySmall)
                    Text("Son giriş IP: ${user.last_login_ip ?: "-"}", style = MaterialTheme.typography.bodySmall)
                    Text("Durum: ${user.status}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun IpBlacklistTab(
    ips: List<com.kilotakip.app.data.remote.dto.BlacklistedIpDto>,
    onAdd: (String, String?) -> Unit,
    onRemove: (Long) -> Unit
) {
    var newIp by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row {
            OutlinedTextField(
                value = newIp, onValueChange = { newIp = it },
                label = { Text("IP Adresi") }, singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = reason, onValueChange = { reason = it },
            label = { Text("Sebep (opsiyonel)") }, singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = {
            if (newIp.isNotBlank()) {
                onAdd(newIp, reason.ifBlank { null })
                newIp = ""
                reason = ""
            }
        }) { Text("Kara Listeye Ekle") }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn {
            items(ips) { ip ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(ip.ip_address, style = MaterialTheme.typography.bodyLarge)
                            ip.reason?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                        }
                        TextButton(onClick = { ip.id?.let(onRemove) }) { Text("Kaldır") }
                    }
                }
            }
        }
    }
}

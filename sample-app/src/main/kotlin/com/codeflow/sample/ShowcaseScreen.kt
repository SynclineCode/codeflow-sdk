@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.codeflow.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codeflow.sdk.analytics.CodeFlowAnalytics

@Composable
fun ShowcaseScreen() {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Buttons", "Inputs", "Lists", "Events")

    var navIndex by remember { mutableStateOf(0) }
    val navItems = listOf(
        Triple("Home", Icons.Default.Home, 0),
        Triple("Search", Icons.Default.Search, 1),
        Triple("Favorites", Icons.Default.Favorite, 2),
        Triple("Settings", Icons.Default.Settings, 3),
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("CodeFlow Analytics Demo") },
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Home, contentDescription = "Home")
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                navItems.forEach { (label, icon, idx) ->
                    NavigationBarItem(
                        selected = navIndex == idx,
                        onClick = { navIndex = idx },
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label) }
                    )
                }
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {},
                icon = { Icon(Icons.Default.Add, contentDescription = "Create") },
                text = { Text("Create") }
            )
        }
    ) { inner ->
        Column(Modifier.padding(inner)) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { idx, t ->
                    Tab(
                        selected = selectedTab == idx,
                        onClick = { selectedTab = idx },
                        text = { Text(t) }
                    )
                }
            }
            when (selectedTab) {
                0 -> ButtonsPane()
                1 -> InputsPane()
                2 -> ListsPane()
                3 -> EventsPane()
            }
        }
    }
}

@Composable
private fun ButtonsPane() {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionHeader("Buttons")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {}) { Text("Filled") }
                OutlinedButton(onClick = {}) { Text("Outlined") }
                TextButton(onClick = {}) { Text("Text") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilledTonalButton(onClick = {}) { Text("Tonal") }
                ElevatedButton(onClick = {}) { Text("Elevated") }
            }
        }
        item {
            SectionHeader("Icon buttons + FAB")
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {}) {
                    Icon(Icons.Default.Star, contentDescription = "Star")
                }
                IconButton(onClick = {}) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
                var fav by remember { mutableStateOf(false) }
                IconToggleButton(checked = fav, onCheckedChange = { fav = it }) {
                    if (fav) Icon(Icons.Default.Favorite, contentDescription = "Unfavorite")
                    else Icon(Icons.Default.FavoriteBorder, contentDescription = "Favorite")
                }
                FloatingActionButton(onClick = {}) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                }
            }
        }
        item {
            SectionHeader("Toggles")
            ToggleRow()
        }
        item {
            SectionHeader("Radios")
            RadioRow()
        }
        item {
            SectionHeader("Chips")
            ChipsRow()
        }
        item {
            SectionHeader("Tappable cards & surfaces")
            Card(
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("This whole card is clickable", fontWeight = FontWeight.Medium)
                    Text("Tap anywhere here", fontSize = 12.sp, color = Color.Gray)
                }
            }
            Surface(
                onClick = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFEADDFF)
            ) {
                Text("Surface(onClick=…)", Modifier.padding(16.dp))
            }
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .height(56.dp)
                    .background(Color(0xFFFFD8E4), RoundedCornerShape(12.dp))
                    .clickable {},
                contentAlignment = Alignment.Center
            ) {
                Text("Box with Modifier.clickable")
            }
        }
    }
}

@Composable
private fun ToggleRow() {
    var sw by remember { mutableStateOf(true) }
    var cb by remember { mutableStateOf(false) }
    var tri by remember { mutableStateOf(ToggleableState.Indeterminate) }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Switch(checked = sw, onCheckedChange = { sw = it })
        Text("Switch", Modifier.padding(horizontal = 12.dp))
        Checkbox(checked = cb, onCheckedChange = { cb = it })
        Text("Checkbox", Modifier.padding(horizontal = 12.dp))
        TriStateCheckbox(state = tri, onClick = {
            tri = when (tri) {
                ToggleableState.On -> ToggleableState.Off
                ToggleableState.Off -> ToggleableState.Indeterminate
                ToggleableState.Indeterminate -> ToggleableState.On
            }
        })
        Text("Tri-state", Modifier.padding(start = 12.dp))
    }
}

@Composable
private fun RadioRow() {
    var picked by remember { mutableStateOf("Small") }
    Row {
        listOf("Small", "Medium", "Large").forEach { opt ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable { picked = opt }
                    .padding(8.dp)
            ) {
                RadioButton(selected = picked == opt, onClick = { picked = opt })
                Text(opt, Modifier.padding(start = 4.dp))
            }
        }
    }
}

@Composable
private fun ChipsRow() {
    var selected by remember { mutableStateOf(false) }
    var input by remember { mutableStateOf(true) }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        AssistChip(onClick = {}, label = { Text("Assist") })
        FilterChip(
            selected = selected,
            onClick = { selected = !selected },
            label = { Text("Filter") }
        )
        InputChip(
            selected = input,
            onClick = { input = !input },
            label = { Text("Input") }
        )
        SuggestionChip(onClick = {}, label = { Text("Suggest") })
    }
}

@Composable
private fun InputsPane() {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SectionHeader("Text fields")
            var name by remember { mutableStateOf("") }
            TextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth()
            )
            var email by remember { mutableStateOf("") }
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
        }
        item {
            SectionHeader("Sliders")
            var v by remember { mutableStateOf(0.4f) }
            Slider(value = v, onValueChange = { v = it })
            Text("Value: ${(v * 100).toInt()}%")

            var range by remember { mutableStateOf(0.2f..0.8f) }
            RangeSlider(value = range, onValueChange = { range = it })
            Text("Range: ${(range.start * 100).toInt()}–${(range.endInclusive * 100).toInt()}%")
        }
        item {
            SectionHeader("Progress")
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun ListsPane() {
    val items = remember { (1..30).map { "Item #$it" } }
    Column {
        SectionHeader("Horizontal list", Modifier.padding(start = 16.dp, top = 16.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(8) { i ->
                Surface(
                    onClick = {},
                    shape = CircleShape,
                    color = Color(0xFFE8DEF8),
                    modifier = Modifier.size(72.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) { Text("#${i + 1}") }
                }
            }
        }
        SectionHeader("Vertical list", Modifier.padding(start = 16.dp, top = 16.dp))
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(items) { item ->
                Surface(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF7F2FA)
                ) {
                    Text(item, Modifier.padding(16.dp))
                }
            }
        }
    }
}

@Composable
private fun EventsPane() {
    // Polls the in-memory ring buffer the SDK exposes for diagnostics.
    var tick by remember { mutableStateOf(0) }
    val events = remember(tick) { CodeFlowAnalytics.recentEvents() }
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Captured events: ${events.size}", fontWeight = FontWeight.SemiBold)
            Button(onClick = { tick++ }) { Text("Refresh") }
        }
        HorizontalDivider()
        LazyColumn(contentPadding = PaddingValues(16.dp)) {
            items(events) { e ->
                Column(Modifier.padding(vertical = 6.dp)) {
                    Text("${e.name} — ${e.properties["role"]}: ${e.properties["label"]}",
                        fontWeight = FontWeight.Medium)
                    e.properties.entries
                        .filter { it.key != "role" && it.key != "label" }
                        .forEach { (k, v) ->
                            Text("  $k = $v", fontSize = 12.sp, color = Color.Gray)
                        }
                }
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color.Gray
    )
}

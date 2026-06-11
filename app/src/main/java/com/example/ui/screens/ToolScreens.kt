package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.ToolViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolDetailHeader(
    title: String,
    description: String,
    toolId: String,
    viewModel: ToolViewModel,
    onBack: () -> Unit
) {
    val favoriteToolIds by viewModel.favoriteToolIds.collectAsState()
    val isFav = favoriteToolIds.contains(toolId)
    val billing by viewModel.userBilling.collectAsState()

    // Show a blocking dialog to prevent tool access when credits are 0 on Free plan
    var showDepletedDialog by remember(billing.credits, billing.subscriptionPlan) {
        mutableStateOf(billing.subscriptionPlan == "Free" && billing.credits <= 0)
    }

    if (showDepletedDialog) {
        AlertDialog(
            onDismissRequest = { /* Force action or back out */ },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Error,
                        contentDescription = "Wallet Empty",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Credits Depleted",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "You have exhausted your 15 starter trial developer tokens.",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "To keep using this suite utility, please upgrade to a Pro Subscription Plan or top up your tokens using our secure PCI-compliant Stripe or PayPal payment options.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.setActiveTool("billing")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Filled.Lock, contentDescription = "Lock checkout", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Upgrade Plans / Refill")
                }
            },
            dismissButton = {
                TextButton(onClick = onBack) {
                    Text("Exit Tool")
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back To Catalog"
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { viewModel.toggleFavorite(toolId, isFav) }) {
                Icon(
                    imageVector = if (isFav) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = if (isFav) "Remove Favorite" else "Add Favorite",
                    tint = if (isFav) Color(0xFFF59E0B) else MaterialTheme.colorScheme.onSurface
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 4.dp)
        )
        
        // Elegant inline billing widget showing remaining credits or unlimited Pro access badge
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    if (billing.subscriptionPlan == "Free") {
                        if (billing.credits > 2) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        } else {
                            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                        }
                    } else {
                        Color(0xFFF59E0B).copy(alpha = 0.15f)
                    }
                )
                .clickable { viewModel.setActiveTool("billing") }
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Icon(
                imageVector = if (billing.subscriptionPlan == "Free") {
                    Icons.Filled.Token
                } else {
                    Icons.Filled.WorkspacePremium
                },
                contentDescription = "Billing Status Icon",
                tint = if (billing.subscriptionPlan == "Free") {
                    if (billing.credits > 2) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                } else {
                    Color(0xFFD97706)
                },
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (billing.subscriptionPlan == "Free") {
                    "Free Account: ${billing.credits} tokens left • Click to refill / upgrade"
                } else {
                    "Unlimited ${billing.subscriptionPlan} Developer Account Active • Premium Status"
                },
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = if (billing.subscriptionPlan == "Free") {
                    if (billing.credits > 2) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                } else {
                    Color(0xFFD97706)
                },
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = "Chevron to payment",
                tint = if (billing.subscriptionPlan == "Free") {
                    if (billing.credits > 2) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                } else {
                    Color(0xFFD97706)
                },
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun ActionOutputCard(
    output: String,
    onClear: (() -> Unit)? = null,
    actionLabel: String = "Formatted Results"
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Row {
                    if (output.isNotEmpty()) {
                        IconButton(onClick = {
                            clipboardManager.setText(AnnotatedString(output))
                            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(imageVector = Icons.Filled.ContentCopy, contentDescription = "Copy")
                        }
                    }
                    if (onClear != null && output.isNotEmpty()) {
                        IconButton(onClick = onClear) {
                            Icon(imageVector = Icons.Filled.Delete, contentDescription = "Clear")
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (output.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                            RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Awaiting input...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp)
                ) {
                    Text(
                        text = output,
                        fontFamily = FontFamily.Monospace,
                        style = TextStyle(fontSize = 13.sp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

// 1. JSON Formatter Screen
@Composable
fun JsonFormatterScreen(viewModel: ToolViewModel, onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    var input by remember { mutableStateOf("") }
    var output by remember { mutableStateOf("") }
    var indentValue by remember { mutableStateOf(4) } // 2 spaces, 4 spaces, 0 (minify)

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        ToolDetailHeader(
            title = "JSON Formatter & Validator",
            description = "Validate, beautify, and minify complex JSON inputs instantly.",
            toolId = "json_formatter",
            viewModel = viewModel,
            onBack = onBack
        )

        Column(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                label = { Text("Raw JSON Input") },
                textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 13.sp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                placeholder = { Text("Paste raw JSON string here...") }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(text = "Indentation Style", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = indentValue == 2,
                    onClick = { indentValue = 2 },
                    label = { Text("2 Spaces") }
                )
                FilterChip(
                    selected = indentValue == 4,
                    onClick = { indentValue = 4 },
                    label = { Text("4 Spaces") }
                )
                FilterChip(
                    selected = indentValue == 0,
                    onClick = { indentValue = 0 },
                    label = { Text("Minify") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = {
                        val trimmed = input.trim()
                        if (trimmed.isEmpty()) {
                            output = "Error: Input is empty!"
                            return@Button
                        }
                        try {
                            if (indentValue == 0) {
                                if (trimmed.startsWith("{")) {
                                    output = org.json.JSONObject(trimmed).toString()
                                } else if (trimmed.startsWith("[")) {
                                    output = org.json.JSONArray(trimmed).toString()
                                } else {
                                    output = "Error: Invalid JSON representation. Must start with '{' or '['"
                                }
                            } else {
                                if (trimmed.startsWith("{")) {
                                    output = org.json.JSONObject(trimmed).toString(indentValue)
                                } else if (trimmed.startsWith("[")) {
                                    output = org.json.JSONArray(trimmed).toString(indentValue)
                                } else {
                                    output = "Error: Invalid JSON representation. Must start with '{' or '['"
                                }
                            }
                            viewModel.addHistory(
                                "json_formatter",
                                "JSON Formatter",
                                "Format",
                                "Formatted ${input.take(20)}... as spacing $indentValue"
                            )
                        } catch (e: Exception) {
                            output = "Invalid JSON Syntax:\n${e.localizedMessage}"
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Apply Code")
                }

                OutlinedButton(
                    onClick = {
                        input = ""
                        output = ""
                    }
                ) {
                    Text("Clear")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            ActionOutputCard(
                output = output,
                onClear = { output = "" },
                actionLabel = "Processed JSON output"
            )
        }
    }
}

// 2. HTML Beautifier Screen
@Composable
fun HtmlBeautifierScreen(viewModel: ToolViewModel, onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    var input by remember { mutableStateOf("") }
    var output by remember { mutableStateOf("") }
    var minifyMode by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    // Lightweight XML indenter
    fun formatXmlHtml(inputStr: String, indentStr: String = "    "): String {
        try {
            val clean = inputStr.replace(Regex(">\\s+<"), "><").trim()
            val sb = StringBuilder()
            var indent = 0
            var i = 0
            val len = clean.length
            while (i < len) {
                if (clean[i] == '<') {
                    val isClose = i + 1 < len && clean[i + 1] == '/'
                    val isSelfClose = clean.indexOf('>', i).let { end ->
                        end != -1 && clean[end - 1] == '/'
                    }
                    if (isClose) {
                        indent = (indent - 1).coerceAtLeast(0)
                    }
                    if (sb.isNotEmpty() && sb.last() != '\n') sb.append('\n')
                    repeat(indent) { sb.append(indentStr) }
                    val end = clean.indexOf('>', i)
                    if (end != -1) {
                        sb.append(clean.substring(i, end + 1))
                        i = end + 1
                        if (!isClose && !isSelfClose) {
                            val nextSlash = clean.indexOf("</", i)
                            val nextClose = clean.indexOf('>', i)
                            if (nextSlash != -1 && nextSlash < nextClose) {
                                // Content holds inline closing tag
                            } else {
                                indent++
                            }
                        }
                    } else {
                        sb.append(clean.substring(i))
                        break
                    }
                } else {
                    val nextBracket = clean.indexOf('<', i)
                    if (nextBracket != -1) {
                        val text = clean.substring(i, nextBracket).trim()
                        if (text.isNotEmpty()) {
                            sb.append(text)
                        }
                        i = nextBracket
                    } else {
                        val text = clean.substring(i).trim()
                        if (text.isNotEmpty()) {
                            sb.append(text)
                        }
                        break
                    }
                }
            }
            return sb.toString().trim()
        } catch (e: Exception) {
            return "Parsing error: ${e.message}"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        ToolDetailHeader(
            title = "HTML & XML Beautifier",
            description = "Format nested tags with elegant spacing elements, or compress markup markup.",
            toolId = "html_beautifier",
            viewModel = viewModel,
            onBack = onBack
        )

        Column(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                label = { Text("Markup Input (HTML / XML)") },
                textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 13.sp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                placeholder = { Text("Paste tags here...") }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = minifyMode, onCheckedChange = { minifyMode = it })
                Text("Minify mode (Strip spacing gaps)")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = {
                        if (input.trim().isEmpty()) {
                            output = "Error: Input is empty!"
                            return@Button
                        }
                        output = if (minifyMode) {
                            input.replace(Regex(">\\s+<"), "><").trim()
                        } else {
                            formatXmlHtml(input)
                        }
                        viewModel.addHistory(
                            "html_beautifier",
                            "HTML & XML Beautifier",
                            if (minifyMode) "Minify" else "Beautify",
                            "Processed markup text length ${input.length}"
                        )
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (minifyMode) "Minify Markup" else "Beautify Markup")
                }

                OutlinedButton(
                    onClick = {
                        input = ""
                        output = ""
                    }
                ) {
                    Text("Clear")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            ActionOutputCard(
                output = output,
                onClear = { output = "" },
                actionLabel = "Processed Output"
            )
        }
    }
}

// 3. Base64 Codec Screen
@Composable
fun Base64CodecScreen(viewModel: ToolViewModel, onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    var input by remember { mutableStateOf("") }
    var output by remember { mutableStateOf("") }
    var modeEncode by remember { mutableStateOf(true) } // true = encode, false = decode

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        ToolDetailHeader(
            title = "Base64 Encoder/Decoder",
            description = "Easily encode text representations to Base64 standards or parse them back securely.",
            toolId = "base64",
            viewModel = viewModel,
            onBack = onBack
        )

        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ElevatedButton(
                    onClick = { modeEncode = true },
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = if (modeEncode) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Filled.Lock, contentDescription = "Encode")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Encode Text")
                }

                ElevatedButton(
                    onClick = { modeEncode = false },
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = if (!modeEncode) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Filled.LockOpen, contentDescription = "Decode")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Decode Text")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                label = { Text(if (modeEncode) "Plain Text Input" else "Base64 Encoded Input") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = {
                        if (input.isEmpty()) {
                            output = "Error: Input text is empty!"
                            return@Button
                        }
                        try {
                            if (modeEncode) {
                                output = android.util.Base64.encodeToString(
                                    input.toByteArray(Charsets.UTF_8),
                                    android.util.Base64.NO_WRAP
                                )
                                viewModel.addHistory("base64", "Base64 Codec", "Encode", "Encoded chars ${input.length}")
                            } else {
                                val decodedBytes = android.util.Base64.decode(input, android.util.Base64.DEFAULT)
                                output = String(decodedBytes, Charsets.UTF_8)
                                viewModel.addHistory("base64", "Base64 Codec", "Decode", "Decoded Base64")
                            }
                        } catch (e: Exception) {
                            output = "Decoding Error: Invalid Base64 syntax. Details: ${e.message}"
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (modeEncode) "Encode to Base64" else "Decode from Base64")
                }

                OutlinedButton(onClick = {
                    input = ""
                    output = ""
                }) {
                    Text("Clear")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            ActionOutputCard(
                output = output,
                onClear = { output = "" },
                actionLabel = "Base64 Result"
            )
        }
    }
}

// 4. URL Codec Screen
@Composable
fun UrlCodecScreen(viewModel: ToolViewModel, onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    var input by remember { mutableStateOf("") }
    var output by remember { mutableStateOf("") }
    var isEncodeMode by remember { mutableStateOf(true) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        ToolDetailHeader(
            title = "URL Encoder/Decoder",
            description = "Sanitize special characters safely into percent encodings for URL params.",
            toolId = "url_codec",
            viewModel = viewModel,
            onBack = onBack
        )

        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = isEncodeMode,
                    onClick = { isEncodeMode = true },
                    label = { Text("Encode Mode") }
                )
                FilterChip(
                    selected = !isEncodeMode,
                    onClick = { isEncodeMode = false },
                    label = { Text("Decode Mode") }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                label = { Text(if (isEncodeMode) "Plain parameters/URL Input" else "Percent-encoded url string") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = {
                        if (input.isEmpty()) return@Button
                        try {
                            if (isEncodeMode) {
                                output = java.net.URLEncoder.encode(input, "UTF-8")
                                viewModel.addHistory("url_codec", "URL Codec", "Encode", "Encoded URL parameters")
                            } else {
                                output = java.net.URLDecoder.decode(input, "UTF-8")
                                viewModel.addHistory("url_codec", "URL Codec", "Decode", "Decoded URL parameters")
                            }
                        } catch (e: Exception) {
                            output = "Error parsing URL: ${e.localizedMessage}"
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (isEncodeMode) "URL Encode" else "URL Decode")
                }

                OutlinedButton(onClick = {
                    input = ""
                    output = ""
                }) {
                    Text("Clear")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            ActionOutputCard(
                output = output,
                onClear = { output = "" },
                actionLabel = "Processed URL results"
            )
        }
    }
}

// 5. HTML Entities Screen
@Composable
fun HtmlEntitiesScreen(viewModel: ToolViewModel, onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    var input by remember { mutableStateOf("") }
    var output by remember { mutableStateOf("") }
    var isEscapeMode by remember { mutableStateOf(true) }

    val scrollState = rememberScrollState()

    fun escapeHtml(text: String): String {
        return text.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
    }

    fun unescapeHtml(text: String): String {
        return text.replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&amp;", "&")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        ToolDetailHeader(
            title = "HTML Entities Codec",
            description = "Escape reserved markup tags (<, >, &, \") into entity names safely.",
            toolId = "html_entities",
            viewModel = viewModel,
            onBack = onBack
        )

        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = isEscapeMode,
                    onClick = { isEscapeMode = true },
                    label = { Text("Escape (Entities)") }
                )
                FilterChip(
                    selected = !isEscapeMode,
                    onClick = { isEscapeMode = false },
                    label = { Text("Unescape (Plain)") }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                label = { Text("Text Content") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = {
                        if (input.isEmpty()) return@Button
                        output = if (isEscapeMode) escapeHtml(input) else unescapeHtml(input)
                        viewModel.addHistory(
                            "html_entities",
                            "HTML Entities",
                            if (isEscapeMode) "Escape" else "Unescape",
                            "Processed HTML characters length ${input.length}"
                        )
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (isEscapeMode) "Run Escaper" else "Run Unescaper")
                }

                OutlinedButton(onClick = {
                    input = ""
                    output = ""
                }) {
                    Text("Clear")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            ActionOutputCard(
                output = output,
                onClear = { output = "" },
                actionLabel = "Output Result"
            )
        }
    }
}

// 6. UUID Generator Screen
@Composable
fun UuidGeneratorScreen(viewModel: ToolViewModel, onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    var count by remember { mutableStateOf(5) }
    var useV4 by remember { mutableStateOf(true) } // true=v4 (random), false=v1 (sequential timestamp)
    var uppercase by remember { mutableStateOf(false) }
    var includeHyphens by remember { mutableStateOf(true) }
    var output by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        ToolDetailHeader(
            title = "UUID / GUID Generator",
            description = "Generate unique RFC-compliant UUID identifiers values in batch.",
            toolId = "uuid_gen",
            viewModel = viewModel,
            onBack = onBack
        )

        Column(modifier = Modifier.padding(16.dp)) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Generator Settings",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(text = "Bulk Count: $count items", style = MaterialTheme.typography.bodyMedium)
                    Slider(
                        value = count.toFloat(),
                        onValueChange = { count = it.toInt() },
                        valueRange = 1f..50f,
                        steps = 49
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Standard UUID Format (v4)")
                        Switch(checked = useV4, onCheckedChange = { useV4 = it })
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Keep Hyphens (eg. xxxx-xxxx)")
                        Switch(checked = includeHyphens, onCheckedChange = { includeHyphens = it })
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Capitalized Letters")
                        Switch(checked = uppercase, onCheckedChange = { uppercase = it })
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val list = mutableListOf<String>()
                    val secureRandom = java.security.SecureRandom()
                    for (i in 0 until count) {
                        var u = if (useV4) {
                            UUID.randomUUID().toString()
                        } else {
                            // Simple pseudo v1 node sequential values
                            val mostSig = (System.currentTimeMillis() shl 12) or 0x1000 or (secureRandom.nextLong() and 0x0FFF)
                            val leastSig = (secureRandom.nextLong() and 0x3FFFFFFFFFFFFFFL) or Long.MIN_VALUE
                            UUID(mostSig, leastSig).toString()
                        }
                        if (!includeHyphens) {
                            u = u.replace("-", "")
                        }
                        if (uppercase) {
                            u = u.uppercase()
                        }
                        list.add(u)
                    }
                    output = list.joinToString("\n")
                    viewModel.addHistory(
                        "uuid_gen",
                        "UUID Generator",
                        "Generate",
                        "Generated $count UUIDs (v${if (useV4) "4" else "1 pseudo"})"
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Filled.Autorenew, contentDescription = "Gen")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generate UUID List")
            }

            Spacer(modifier = Modifier.height(12.dp))

            ActionOutputCard(
                output = output,
                onClear = { output = "" },
                actionLabel = "Generated IDs"
            )
        }
    }
}

// 7. Password Generator Screen
@Composable
fun PasswordGeneratorScreen(viewModel: ToolViewModel, onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    var length by remember { mutableStateOf(16) }
    var incUpper by remember { mutableStateOf(true) }
    var incLower by remember { mutableStateOf(true) }
    var incNumbers by remember { mutableStateOf(true) }
    var incSymbols by remember { mutableStateOf(true) }
    var output by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    // Strength assessment entropy
    val strengthTextAndColor = remember(length, incUpper, incLower, incNumbers, incSymbols, output) {
        if (output.isEmpty()) return@remember Pair("Awaiting Generation", Color.Gray)
        var countTypes = 0
        if (incUpper) countTypes++
        if (incLower) countTypes++
        if (incNumbers) countTypes++
        if (incSymbols) countTypes++

        if (length < 8 || countTypes <= 1) {
            Pair("Weak (Unsafe)", Color(0xFFEF4444))
        } else if (length in 8..11 && countTypes == 2) {
            Pair("Medium (Basic)", Color(0xFFF59E0B))
        } else if (length in 12..15 && countTypes >= 3) {
            Pair("Strong (Very Secure)", Color(0xFF10B981))
        } else if (length >= 16 && countTypes == 4) {
            Pair("Bulletproof (Excellent)", Color(0xFF14B8A6))
        } else {
            Pair("Strong", Color(0xFF10B981))
        }
    }

    fun generatePassword(l: Int, up: Boolean, low: Boolean, num: Boolean, sym: Boolean): String {
        val uppers = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val lowers = "abcdefghijklmnopqrstuvwxyz"
        val digits = "0123456789"
        val symbols = "!@#$%^&*()_+-=[]{}|;':\",./<>?"

        val pool = StringBuilder()
        if (up) pool.append(uppers)
        if (low) pool.append(lowers)
        if (num) pool.append(digits)
        if (sym) pool.append(symbols)

        if (pool.isEmpty()) return ""

        val sr = java.security.SecureRandom()
        val pswd = StringBuilder()

        // Guarantee at least one character of each selected type
        val guaranteed = mutableListOf<Char>()
        if (up) guaranteed.add(uppers[sr.nextInt(uppers.length)])
        if (low) guaranteed.add(lowers[sr.nextInt(lowers.length)])
        if (num) guaranteed.add(digits[sr.nextInt(digits.length)])
        if (sym) guaranteed.add(symbols[sr.nextInt(symbols.length)])

        val remaining = l - guaranteed.size
        for (i in 0 until remaining.coerceAtLeast(0)) {
            pswd.append(pool[sr.nextInt(pool.length)])
        }

        guaranteed.forEach { char ->
            val pos = if (pswd.isNotEmpty()) sr.nextInt(pswd.length + 1) else 0
            pswd.insert(pos, char)
        }

        return pswd.toString()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        ToolDetailHeader(
            title = "Secure Password Generator",
            description = "Generate mathematically strong, cryptographically secure random passwords.",
            toolId = "password_gen",
            viewModel = viewModel,
            onBack = onBack
        )

        Column(modifier = Modifier.padding(16.dp)) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Password Parameters",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(text = "Length: $length characters", style = MaterialTheme.typography.bodyMedium)
                    Slider(
                        value = length.toFloat(),
                        onValueChange = { length = it.toInt() },
                        valueRange = 6f..64f,
                        steps = 58
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Include Uppercase (A-Z)")
                        Checkbox(checked = incUpper, onCheckedChange = { incUpper = it })
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Include Lowercase (a-z)")
                        Checkbox(checked = incLower, onCheckedChange = { incLower = it })
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Include Numbers (0-9)")
                        Checkbox(checked = incNumbers, onCheckedChange = { incNumbers = it })
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Include Special Symbols")
                        Checkbox(checked = incSymbols, onCheckedChange = { incSymbols = it })
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (!incUpper && !incLower && !incNumbers && !incSymbols) {
                        output = "Error: Check at least one characters parameter criteria!"
                        return@Button
                    }
                    output = generatePassword(length, incUpper, incLower, incNumbers, incSymbols)
                    viewModel.addHistory(
                        "password_gen",
                        "Secure Password",
                        "Generate",
                        "Generated secure password ($length chars)"
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Filled.Security, contentDescription = "Security key")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generate Password")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Estimated Security: ", style = MaterialTheme.typography.bodyMedium)
                Card(
                    colors = CardDefaults.cardColors(containerColor = strengthTextAndColor.second.copy(alpha = 0.2f)),
                    modifier = Modifier.padding(start = 4.dp)
                ) {
                    Text(
                        text = strengthTextAndColor.first,
                        color = strengthTextAndColor.second,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            ActionOutputCard(
                output = output,
                onClear = { output = "" },
                actionLabel = "Raw Generated Code"
            )
        }
    }
}

// 8. Hash Generator Screen
@Composable
fun HashGeneratorScreen(viewModel: ToolViewModel, onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    var input by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    fun calculateHash(rawText: String, algorithm: String): String {
        if (rawText.isEmpty()) return ""
        return try {
            val digest = java.security.MessageDigest.getInstance(algorithm)
            val bytes = digest.digest(rawText.toByteArray(Charsets.UTF_8))
            bytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            "Hash Calculation Fail: ${e.message}"
        }
    }

    val md5 = remember(input) { calculateHash(input, "MD5") }
    val sha1 = remember(input) { calculateHash(input, "SHA-1") }
    val sha256 = remember(input) { calculateHash(input, "SHA-256") }
    val sha512 = remember(input) { calculateHash(input, "SHA-512") }

    // Custom helper history trigger
    var lastLoggedLength by remember { mutableStateOf(0) }
    LaunchedEffect(input) {
        if (input.isNotEmpty() && Math.abs(input.length - lastLoggedLength) > 10) {
            delay(1500) // Debounce history logging
            viewModel.addHistory("hash_gen", "Crypto Hash", "Hash", "Calculated sha256 digests in real-time.")
            lastLoggedLength = input.length
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        ToolDetailHeader(
            title = "Crypto Hash Generator",
            description = "Examines security checksum hashes (MD5, SHA1, SHA256, SHA512) in real-time.",
            toolId = "hash_gen",
            viewModel = viewModel,
            onBack = onBack
        )

        Column(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                label = { Text("Write raw plain text input") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                placeholder = { Text("As you type, hashes dynamically update...") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Dynamic Output Checksums",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            ActionOutputCard(output = md5, actionLabel = "MD5 Hash Digest")
            ActionOutputCard(output = sha1, actionLabel = "SHA-1 Hash Digest")
            ActionOutputCard(output = sha256, actionLabel = "SHA-256 Hash Digest")
            ActionOutputCard(output = sha512, actionLabel = "SHA-512 Hash Digest")
        }
    }
}

// 9. Case Converter Screen
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun CaseConverterScreen(viewModel: ToolViewModel, onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    var input by remember { mutableStateOf("") }
    var output by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    val stats = remember(input) {
        val trimInput = input.trim()
        val words = if (trimInput.isEmpty()) 0 else trimInput.split(Regex("\\s+")).size
        val charCount = input.length
        val charNoSpace = input.filter { !it.isWhitespace() }.length
        val lines = if (input.isEmpty()) 0 else input.split('\n').size
        val seconds = (words / 200.0 * 60).toInt()
        Triple(words, charCount, "$charNoSpace characters (no space), $lines lines, ~$seconds Sec reading time")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        ToolDetailHeader(
            title = "Case Converter & Stats",
            description = "Change naming conventions of words or evaluate formatting statistics instantly.",
            toolId = "case_converter",
            viewModel = viewModel,
            onBack = onBack
        )

        Column(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                label = { Text("Rich input document text") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Analytical chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SuggestionChip(onClick = {}, label = { Text("${stats.first} Words") })
                SuggestionChip(onClick = {}, label = { Text("${stats.second} Chars") })
            }
            Text(
                text = stats.third,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Naming Schemes", style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ElevatedButton(onClick = {
                    output = input.uppercase(Locale.getDefault())
                    viewModel.addHistory("case_converter", "Case Converter", "Transform", "Converted details to UPPERCASE")
                }) {
                    Text("UPPERCASE")
                }
                ElevatedButton(onClick = {
                    output = input.lowercase(Locale.getDefault())
                    viewModel.addHistory("case_converter", "Case Converter", "Transform", "Converted details to lowercase")
                }) {
                    Text("lowercase")
                }
                ElevatedButton(onClick = {
                    output = input.split(Regex("\\s+")).joinToString(" ") { word ->
                        word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                    }
                    viewModel.addHistory("case_converter", "Case Converter", "Transform", "Converted details to Title Case")
                }) {
                    Text("Title Case")
                }
                ElevatedButton(onClick = {
                    val clean = input.lowercase(Locale.getDefault()).split(Regex("\\s+")).joinToString("_")
                    output = clean
                    viewModel.addHistory("case_converter", "Case Converter", "Transform", "Converted details to snake_case")
                }) {
                    Text("snake_case")
                }
                ElevatedButton(onClick = {
                    val parts = input.lowercase(Locale.getDefault()).split(Regex("\\s+"))
                    output = if (parts.isEmpty()) "" else parts.first() + parts.drop(1).joinToString("") { word ->
                        word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                    }
                    viewModel.addHistory("case_converter", "Case Converter", "Transform", "Converted details to camelCase")
                }) {
                    Text("camelCase")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            ActionOutputCard(
                output = output,
                onClear = { output = "" },
                actionLabel = "Formatted Results Output"
            )
        }
    }
}

// 10. Epoch Timestamp Converter Screen
@Composable
fun EpochConverterScreen(viewModel: ToolViewModel, onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    var activeEpochMillis by remember { mutableStateOf(System.currentTimeMillis()) }

    var manualEpochInput by remember { mutableStateOf((System.currentTimeMillis() / 1000L).toString()) }
    var convertOutput by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    // Dynamic Live ticker
    LaunchedEffect(Unit) {
        while (true) {
            activeEpochMillis = System.currentTimeMillis()
            delay(1000)
        }
    }

    fun parseEpochDate(epochSecStr: String): String {
        return try {
            val seconds = epochSecStr.trim().toLongOrNull() ?: return "Error: Non-numeric timestamp!"
            val millis = seconds * 1000L
            val date = Date(millis)

            val sdfLocal = SimpleDateFormat("yyyy-MM-dd HH:mm:ss (z)", Locale.getDefault())
            val sdfGmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss 'UTC'", Locale.getDefault())
            sdfGmt.timeZone = TimeZone.getTimeZone("UTC")

            "Local calendar: ${sdfLocal.format(date)}\nGMT standard: ${sdfGmt.format(date)}"
        } catch (e: Exception) {
            "Parse Error: ${e.message}"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        ToolDetailHeader(
            title = "Epoch Timestamp Converter",
            description = "Convert elapsed second timestamps to calendar times or UTC formats safely.",
            toolId = "epoch_converter",
            viewModel = viewModel,
            onBack = onBack
        )

        Column(modifier = Modifier.padding(16.dp)) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("System Clock Live Unix Epoch", style = MaterialTheme.typography.titleSmall)
                    Text(
                        text = "${activeEpochMillis / 1000L}",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Current UTC Date-Time: " + SimpleDateFormat("yyyy-MM-dd HH:mm:ss 'UTC'", Locale.getDefault()).apply {
                            timeZone = TimeZone.getTimeZone("UTC")
                        }.format(Date(activeEpochMillis)),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Manual Epoch Parser",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = manualEpochInput,
                        onValueChange = { manualEpochInput = it },
                        label = { Text("Unix second epoch values") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                convertOutput = parseEpochDate(manualEpochInput)
                                viewModel.addHistory(
                                    "epoch_converter",
                                    "Epoch Tracker",
                                    "Convert",
                                    "Parsed epoch value $manualEpochInput"
                                )
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Convert To Date")
                        }

                        OutlinedButton(onClick = {
                            manualEpochInput = (System.currentTimeMillis() / 1000L).toString()
                        }) {
                            Text("Reset")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            ActionOutputCard(
                output = convertOutput,
                onClear = { convertOutput = "" },
                actionLabel = "Date parsing results"
            )
        }
    }
}

// 11. Number Base Converter Screen
@Composable
fun BaseConverterScreen(viewModel: ToolViewModel, onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    var decVal by remember { mutableStateOf("") }
    var binVal by remember { mutableStateOf("") }
    var octVal by remember { mutableStateOf("") }
    var hexVal by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    fun updateAllFromDecimal(decStr: String) {
        decVal = decStr
        val num = decStr.toLongOrNull()
        if (num == null) {
            binVal = ""
            octVal = ""
            hexVal = ""
            return
        }
        binVal = java.lang.Long.toBinaryString(num)
        octVal = java.lang.Long.toOctalString(num)
        hexVal = java.lang.Long.toHexString(num).uppercase(Locale.getDefault())
    }

    fun updateAllFromBinary(binStr: String) {
        binVal = binStr
        val num = binStr.toLongOrNull(2)
        if (num == null) {
            decVal = ""
            octVal = ""
            hexVal = ""
            return
        }
        decVal = num.toString()
        octVal = java.lang.Long.toOctalString(num)
        hexVal = java.lang.Long.toHexString(num).uppercase(Locale.getDefault())
    }

    fun updateAllFromOctal(octStr: String) {
        octVal = octStr
        val num = octStr.toLongOrNull(8)
        if (num == null) {
            decVal = ""
            binVal = ""
            hexVal = ""
            return
        }
        decVal = num.toString()
        binVal = java.lang.Long.toBinaryString(num)
        hexVal = java.lang.Long.toHexString(num).uppercase(Locale.getDefault())
    }

    fun updateAllFromHex(hexStr: String) {
        hexVal = hexStr
        val num = hexStr.toLongOrNull(16)
        if (num == null) {
            decVal = ""
            binVal = ""
            octVal = ""
            return
        }
        decVal = num.toString()
        binVal = java.lang.Long.toBinaryString(num)
        octVal = java.lang.Long.toOctalString(num)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        ToolDetailHeader(
            title = "Number Base Converter",
            description = "Input coordinates anywhere to translate decimal, hexadecimal, binary, and octal.",
            toolId = "base_converter",
            viewModel = viewModel,
            onBack = onBack
        )

        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Entering numbers into any block updates all others actively:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = decVal,
                        onValueChange = { clean ->
                            val filtered = clean.filter { it.isDigit() }
                            updateAllFromDecimal(filtered)
                        },
                        label = { Text("Decimal (Base 10)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = hexVal,
                        onValueChange = { clean ->
                            val filtered = clean.filter { it.isDigit() || (it.uppercaseChar() in 'A'..'F') }.uppercase()
                            updateAllFromHex(filtered)
                        },
                        label = { Text("Hexadecimal (Base 16)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = binVal,
                        onValueChange = { clean ->
                            val filtered = clean.filter { it == '0' || it == '1' }
                            updateAllFromBinary(filtered)
                        },
                        label = { Text("Binary (Base 2)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = octVal,
                        onValueChange = { clean ->
                            val filtered = clean.filter { it in '0'..'7' }
                            updateAllFromOctal(filtered)
                        },
                        label = { Text("Octal (Base 8)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = {
                    decVal = ""
                    binVal = ""
                    octVal = ""
                    hexVal = ""
                    viewModel.addHistory("base_converter", "Base Converter", "Clear", "Cleared translation layout")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Reset All Forms")
            }
        }
    }
}

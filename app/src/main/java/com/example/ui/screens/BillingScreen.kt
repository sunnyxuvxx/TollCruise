package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.ToolViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillingScreen(
    viewModel: ToolViewModel,
    onBack: () -> Unit
) {
    val billing by viewModel.userBilling.collectAsState()
    val scope = rememberCoroutineScope()

    // Screen State Management
    var selectedPlan by remember { mutableStateOf<String?>(null) } // "Pro", "Ultra" or null
    var selectedCreditPurchase by remember { mutableStateOf<Pair<Int, Double>?>(null) } // (Credits, Price) or null
    
    // Active billing flow step: "idle", "checkout", "processing", "success"
    var billingStep by remember { mutableStateOf("idle") } 
    var paymentMethod by remember { mutableStateOf("stripe") } // "stripe" or "paypal"
    var processingStepText by remember { mutableStateOf("") }
    var purchaseReceiptDetails by remember { mutableStateOf<String>("") }

    // Stripe checkout form values
    var cardNumber by remember { mutableStateOf("") }
    var cardExpiry by remember { mutableStateOf("") }
    var cardCvc by remember { mutableStateOf("") }
    var cardName by remember { mutableStateOf("") }

    // PayPal checkout form values
    var paypalEmail by remember { mutableStateOf("") }
    var paypalPassword by remember { mutableStateOf("") }

    // Focus manager for input fields
    val focusManager = LocalFocusManager.current

    // Validation Statuses
    val isCardValid = remember(cardNumber) {
        val raw = cardNumber.replace(" ", "")
        raw.length >= 15 && raw.length <= 16 && raw.all { it.isDigit() } && luhnCheck(raw)
    }
    val isExpiryValid = remember(cardExpiry) {
        val parts = cardExpiry.split("/")
        if (parts.size == 2) {
            val mm = parts[0].toIntOrNull() ?: 0
            val yy = parts[1].toIntOrNull() ?: 0
            mm in 1..12 && yy >= 26 // Expired checks
        } else false
    }
    val isCvcValid = remember(cardCvc) { cardCvc.length in 3..4 && cardCvc.all { it.isDigit() } }
    val isNameValid = remember(cardName) { cardName.trim().split(" ").size >= 2 }

    val isStripeFormComplete = isCardValid && isExpiryValid && isCvcValid && isNameValid
    val isPaypalFormComplete = remember(paypalEmail, paypalPassword) {
        paypalEmail.contains("@") && paypalEmail.contains(".") && paypalPassword.length >= 6
    }

    // Luhn validation, Card brand helpers
    val cardBrand = remember(cardNumber) {
        val raw = cardNumber.replace(" ", "")
        detectCardBrand(raw)
    }

    // Execution: Simulation of secure payment clearing async steps
    fun startPaymentProcessing(type: String, planName: String?, creditsAmount: Int?, price: Double) {
        scope.launch {
            billingStep = "processing"
            val steps = listOf(
                "Establishing secure TLS 1.3 socket to gateway servers...",
                "Initiating PCI-compliant tokenization...",
                "Verifying card brand and authorization network...",
                "Running anti-fraud risk engines...",
                "Sending capture proposal to payment processor bank branch...",
                "Updating ToolCruise database records...",
                "Finalizing digital wallet synchronization..."
            )
            for (step in steps) {
                processingStepText = step
                delay(800)
            }
            
            // Execute Database upgrades and generate invoice texts
            if (planName != null) {
                viewModel.purchaseSubscription(
                    plan = planName,
                    price = price,
                    paymentMethod = if (type == "stripe") "Stripe Elements CC" else "PayPal Account Link"
                )
                purchaseReceiptDetails = "Upgraded to $planName Monthly Subscription | Charge Amount: $$price"
            } else if (creditsAmount != null) {
                viewModel.purchaseCredits(
                    addedCredits = creditsAmount,
                    price = price,
                    paymentMethod = if (type == "stripe") "Stripe Elements CC" else "PayPal Wallet"
                )
                purchaseReceiptDetails = "Refilled Wallet: +$creditsAmount Processing Tokens | Charge Amount: $$price"
            }

            // Move to success step
            billingStep = "success"
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.CreditCard,
                            contentDescription = "Billing Portal",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Secure Pay Portal",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (billingStep == "checkout") {
                            billingStep = "idle"
                            selectedPlan = null
                            selectedCreditPurchase = null
                        } else {
                            onBack()
                        }
                    }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Return")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp)
                )
            )
        }
    ) { innerPadding ->
        AnimatedContent(
            targetState = billingStep,
            transitionSpec = {
                (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                    slideOutHorizontally { width -> -width } + fadeOut())
            },
            label = "PaymentStepsTransition",
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) { step ->
            when (step) {
                "idle" -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // 1. Current Plan Overview Header
                        item {
                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                ),
                                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(20.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Active Subscription",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                        )
                                        Surface(
                                            color = if (billing.subscriptionPlan == "Free") MaterialTheme.colorScheme.surfaceVariant else Color(0xFFF59E0B),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text(
                                                text = billing.subscriptionPlan.uppercase(),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = if (billing.subscriptionPlan == "Free") MaterialTheme.colorScheme.onSurfaceVariant else Color.White,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    
                                    if (billing.subscriptionPlan == "Free") {
                                        Text(
                                            text = "Free Tier Sandbox Developer",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "You are currently running on a metered plan, which deducts 1 token per execution. Refill tokens below or upgrade to unlock unlimited compile, format, and generate sequences offline.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                        )
                                    } else {
                                        Text(
                                            text = "Unlimited ${billing.subscriptionPlan} Developer Account",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Enrolled via secure client app. Renews securely on automatic billing intervals. Enjoy zero credit restrictions on formatting, encoders, and generators.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Button(
                                            onClick = { viewModel.cancelSubscription() },
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(Icons.Filled.Cancel, contentDescription = "Cancel Sub")
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Cancel Premium Subscription Plan")
                                        }
                                    }

                                    HorizontalDivider(
                                        modifier = Modifier.padding(vertical = 12.dp),
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Filled.Savings,
                                                contentDescription = "Wallet Balance",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Execution Credits Balance:",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Text(
                                            text = "${billing.credits} Tokens",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (billing.credits > 2) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }

                        // 2. Premium Subscription Offers Matrix
                        item {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                ) {
                                    Icon(
                                        Icons.Outlined.WorkspacePremium,
                                        contentDescription = "Upgrade Plans",
                                        tint = MaterialTheme.colorScheme.secondary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Unlock Unlimited Access",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    // Subscription: Pro
                                    SubscriptionOfferCard(
                                        modifier = Modifier.weight(1f),
                                        title = "Pro Dev",
                                        price = "$9.99/mo",
                                        features = listOf("Unlimited Executions", "Full Speed Processing", "No Refresh Restrictions", "Advanced Formatting Depth"),
                                        badge = "Best Value",
                                        isCurrent = billing.subscriptionPlan == "Pro",
                                        onClick = {
                                            selectedPlan = "Pro"
                                            selectedCreditPurchase = null
                                            billingStep = "checkout"
                                        }
                                    )

                                    // Subscription: Ultra
                                    SubscriptionOfferCard(
                                        modifier = Modifier.weight(1f),
                                        title = "Ultra Architect",
                                        price = "$24.99/mo",
                                        features = listOf("All Pro Features", "Live Concurrency Compilation", "Shared Workspace", "Priority Beta Access Thread"),
                                        badge = "Elite Pack",
                                        isCurrent = billing.subscriptionPlan == "Ultra",
                                        onClick = {
                                            selectedPlan = "Ultra"
                                            selectedCreditPurchase = null
                                            billingStep = "checkout"
                                        }
                                    )
                                }
                            }
                        }

                        // 3. Metered Top-Off Refill Plans
                        item {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                ) {
                                    Icon(
                                        Icons.Outlined.OfflineBolt,
                                        contentDescription = "Metered Plans",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Pay-As-You-Go Credit Boosters",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Column(
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val creditOffers = listOf(
                                        Triple(50, 2.99, "Starter Kit (approx. 5.9¢/token)"),
                                        Triple(200, 7.99, "Power Architect Kit (approx. 3.9¢/token - Best Refill)"),
                                        Triple(500, 14.99, "Elite Developer Kit (approx. 2.9¢/token)")
                                    )

                                    creditOffers.forEach { (credits, price, label) ->
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    selectedPlan = null
                                                    selectedCreditPurchase = Pair(credits, price)
                                                    billingStep = "checkout"
                                                },
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(16.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(
                                                            Icons.Filled.Token,
                                                            contentDescription = "Tokens",
                                                            tint = MaterialTheme.colorScheme.primary,
                                                            modifier = Modifier.size(20.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text(
                                                            text = "+$credits Execution Credits",
                                                            style = MaterialTheme.typography.titleSmall,
                                                            fontWeight = FontWeight.ExtraBold
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        text = label,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                                Button(
                                                    onClick = {
                                                        selectedPlan = null
                                                        selectedCreditPurchase = Pair(credits, price)
                                                        billingStep = "checkout"
                                                    },
                                                    shape = RoundedCornerShape(8.dp),
                                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                                                ) {
                                                    Text("$$price")
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // 4. Secure badge
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Security,
                                    contentDescription = "Secure Badge",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "PCI-DSS Compliant • 256-Bit SSL Encrypted Processing Gateway",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                "checkout" -> {
                    // Check local order descriptions
                    val orderTitle = if (selectedPlan != null) "Upgrade to $selectedPlan Plan" else "+${selectedCreditPurchase?.first} Token Wallet Top-up"
                    val orderPriceText = if (selectedPlan != null) {
                        if (selectedPlan == "Pro") "$9.99/mo" else "$24.99/mo"
                    } else "$${selectedCreditPurchase?.second}"

                    val doublePriceValue = if (selectedPlan != null) {
                        if (selectedPlan == "Pro") 9.99 else 24.99
                    } else selectedCreditPurchase?.second ?: 0.0

                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Order Summary Header
                        item {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primaryContainer),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(20.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Selected Order",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = orderTitle,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.ExtraBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Text(
                                        text = orderPriceText,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(start = 12.dp)
                                    )
                                }
                            }
                        }

                        // Gateway tabs selection: Stripe vs PayPal
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp))
                                    .padding(4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (paymentMethod == "stripe") MaterialTheme.colorScheme.primary else Color.Transparent)
                                        .clickable { paymentMethod = "stripe" }
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Filled.Payment,
                                            contentDescription = "Stripe",
                                            tint = if (paymentMethod == "stripe") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Stripe Card",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (paymentMethod == "stripe") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (paymentMethod == "paypal") Color(0xFF003087) else Color.Transparent)
                                        .clickable { paymentMethod = "paypal" }
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Outlined.AccountBalanceWallet,
                                            contentDescription = "PayPal",
                                            tint = if (paymentMethod == "paypal") Color.White else MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "PayPal Checkout",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (paymentMethod == "paypal") Color.White else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }

                        // Form input layers based on Gateway selection
                        item {
                            AnimatedContent(
                                targetState = paymentMethod,
                                label = "FormTransition"
                            ) { method ->
                                when (method) {
                                    "stripe" -> {
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(12.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = "Stripe Card Elements Verification",
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )

                                            // Card Number Form
                                            OutlinedTextField(
                                                value = cardNumber,
                                                onValueChange = { input ->
                                                    val clean = input.filter { it.isDigit() || it == ' ' }.take(19)
                                                    cardNumber = formatCardNumberSpacing(clean)
                                                },
                                                label = { Text("Credit Card Number") },
                                                placeholder = { Text("4000 1234 5678 9010") },
                                                keyboardOptions = KeyboardOptions(
                                                    keyboardType = KeyboardType.Number,
                                                    imeAction = ImeAction.Next
                                                ),
                                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                                                leadingIcon = {
                                                    Icon(
                                                        imageVector = getCardBrandIcon(cardBrand),
                                                        contentDescription = "Card brand icon",
                                                        tint = if (isCardValid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                },
                                                trailingIcon = {
                                                    if (cardNumber.isNotEmpty()) {
                                                        if (isCardValid) {
                                                            Icon(Icons.Filled.CheckCircle, contentDescription = "Valid card", tint = Color(0xFF10B981))
                                                        } else {
                                                            Icon(Icons.Filled.Warning, contentDescription = "Card number invalid", tint = MaterialTheme.colorScheme.error)
                                                        }
                                                    }
                                                },
                                                isError = cardNumber.isNotEmpty() && !isCardValid,
                                                supportingText = {
                                                    if (cardNumber.isNotEmpty() && !isCardValid) {
                                                        Text("Card number fails checksum (Luhn check invalid) or length range.")
                                                    } else if (cardNumber.isNotEmpty()) {
                                                        Text("Network Detected: " + cardBrand)
                                                    }
                                                },
                                                shape = RoundedCornerShape(12.dp),
                                                modifier = Modifier.fillMaxWidth().testTag("stripe_cc_field")
                                            )

                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                // Expiration Date Form
                                                OutlinedTextField(
                                                    value = cardExpiry,
                                                    onValueChange = { input ->
                                                        val filtered = input.filter { it.isDigit() || it == '/' }.take(5)
                                                        cardExpiry = formatExpiryDate(filtered)
                                                    },
                                                    label = { Text("Expiry (MM/YY)") },
                                                    placeholder = { Text("12/28") },
                                                    keyboardOptions = KeyboardOptions(
                                                        keyboardType = KeyboardType.Number,
                                                        imeAction = ImeAction.Next
                                                    ),
                                                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Right) }),
                                                    isError = cardExpiry.isNotEmpty() && !isExpiryValid,
                                                    supportingText = {
                                                        if (cardExpiry.isNotEmpty() && !isExpiryValid) {
                                                            Text("Invalid (Expiry must be MM/YY, e.g. 12/28)")
                                                        }
                                                    },
                                                    shape = RoundedCornerShape(12.dp),
                                                    modifier = Modifier.weight(1.2f).testTag("stripe_expiry_field")
                                                )

                                                // CVC Code
                                                OutlinedTextField(
                                                    value = cardCvc,
                                                    onValueChange = { input ->
                                                        cardCvc = input.filter { it.isDigit() }.take(4)
                                                    },
                                                    label = { Text("CVC") },
                                                    placeholder = { Text("CVC") },
                                                    keyboardOptions = KeyboardOptions(
                                                        keyboardType = KeyboardType.Number,
                                                        imeAction = ImeAction.Next
                                                    ),
                                                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                                                    isError = cardCvc.isNotEmpty() && !isCvcValid,
                                                    supportingText = {
                                                        if (cardCvc.isNotEmpty() && !isCvcValid) {
                                                            Text("Invalid digits (3-4 digits expected)")
                                                        }
                                                    },
                                                    shape = RoundedCornerShape(12.dp),
                                                    modifier = Modifier.weight(1.0f).testTag("stripe_cvc_field")
                                                )
                                            }

                                            // Cardholder Full Name
                                            OutlinedTextField(
                                                value = cardName,
                                                onValueChange = { cardName = it },
                                                label = { Text("Cardholder Name") },
                                                placeholder = { Text("e.g. Satoshi Nakamoto") },
                                                leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = "Name") },
                                                keyboardOptions = KeyboardOptions(
                                                    keyboardType = KeyboardType.Text,
                                                    imeAction = ImeAction.Done
                                                ),
                                                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                                                isError = cardName.isNotEmpty() && !isNameValid,
                                                supportingText = {
                                                    if (cardName.isNotEmpty() && !isNameValid) {
                                                        Text("Please enter your full first and last name.")
                                                    }
                                                },
                                                shape = RoundedCornerShape(12.dp),
                                                modifier = Modifier.fillMaxWidth().testTag("stripe_name_field")
                                            )
                                        }
                                    }

                                    "paypal" -> {
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(12.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Icon(
                                                    Icons.Outlined.PhonelinkSetup,
                                                    contentDescription = "PayPal Link",
                                                    tint = Color(0xFF0079C1),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "PayPal Express Integration Sandbox",
                                                    style = MaterialTheme.typography.labelLarge,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF003087)
                                                )
                                            }

                                            Text(
                                                text = "Sign in to authorize your PayPal secure express payment options. Funds will be captured automatically upon authorization approval.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )

                                            // Email Field
                                            OutlinedTextField(
                                                value = paypalEmail,
                                                onValueChange = { paypalEmail = it },
                                                label = { Text("PayPal Account Email") },
                                                placeholder = { Text("user@domain.com") },
                                                leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = "Email") },
                                                keyboardOptions = KeyboardOptions(
                                                    keyboardType = KeyboardType.Email,
                                                    imeAction = ImeAction.Next
                                                ),
                                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                                                shape = RoundedCornerShape(12.dp),
                                                modifier = Modifier.fillMaxWidth().testTag("paypal_email_field")
                                            )

                                            // Password Field (Masked)
                                            OutlinedTextField(
                                                value = paypalPassword,
                                                onValueChange = { paypalPassword = it },
                                                label = { Text("PayPal Secure Password") },
                                                placeholder = { Text("••••••••") },
                                                leadingIcon = { Icon(Icons.Outlined.VpnKey, contentDescription = "Key") },
                                                visualTransformation = PasswordVisualTransformation(),
                                                keyboardOptions = KeyboardOptions(
                                                    keyboardType = KeyboardType.Password,
                                                    imeAction = ImeAction.Done
                                                ),
                                                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                                                shape = RoundedCornerShape(12.dp),
                                                modifier = Modifier.fillMaxWidth().testTag("paypal_password_field")
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Complete Secure Payment Execute Button
                        item {
                            val isActionEnabled = if (paymentMethod == "stripe") isStripeFormComplete else isPaypalFormComplete
                            val btnText = if (paymentMethod == "stripe") {
                                "Pay securely $orderPriceText with Stripe"
                            } else {
                                "Pay safely $orderPriceText with PayPal"
                            }

                            Button(
                                onClick = {
                                    startPaymentProcessing(
                                        type = paymentMethod,
                                        planName = selectedPlan,
                                        creditsAmount = selectedCreditPurchase?.first,
                                        price = doublePriceValue
                                    )
                                },
                                enabled = isActionEnabled,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (paymentMethod == "stripe") MaterialTheme.colorScheme.primary else Color(0xFFFFC439),
                                    contentColor = if (paymentMethod == "stripe") MaterialTheme.colorScheme.onPrimary else Color(0xFF003087)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("execute_payment_button")
                            ) {
                                Icon(Icons.Filled.Lock, contentDescription = "Secure submit lock")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = btnText,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            
                            if (!isActionEnabled) {
                                Text(
                                    text = "💡 Please fill out all payment credentials correctly to authorization billing.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.error,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                                )
                            }
                        }

                        // Sandbox simulation details disclaimer
                        item {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "ℹ️ Payment Simulator Guide",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "You are currently running in secure developer isolation test mode. Enter any valid credit card sequence that passes the standard mathematical Luhn checksum validation algorithm (such as 4111 1111 1111 1111 or 4000 1234 5678 9010), any MM/YY expiring in the future, and a 3-digit CVV block to simulate real financial approval.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                "processing" -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(64.dp),
                                strokeWidth = 5.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                "Processing Secure Transaction",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Encrypting transaction parameters and communication with payment rails securely...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            // Sequential loading text tracker
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = processingStepText,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                "success" -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "Success check",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(96.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Payment Captured Successfully!",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Thank you for supporting ToolCruise. Your authorization clear completes successfully and your developer account has been instantly upgraded.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(30.dp))

                        // High fidelity billing receipt
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "DIGITAL RECEIPT & ORDER SUMMARY",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Transaction Status:", style = MaterialTheme.typography.bodySmall)
                                    Text("PAID / CAPTURED", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Billing Method:", style = MaterialTheme.typography.bodySmall)
                                    Text(if (paymentMethod == "stripe") "Stripe Elements Vault" else "PayPal Account Link", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = purchaseReceiptDetails,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Your persistent SQLite wallet balance table has synced. Balance: ${billing.credits} tokens. Plan: ${billing.subscriptionPlan.uppercase()}.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(40.dp))

                        Button(
                            onClick = {
                                billingStep = "idle"
                                selectedPlan = null
                                selectedCreditPurchase = null
                                onBack() // Return to dashboard
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("continue_to_suite_button")
                        ) {
                            Text(
                                "Continue to Developer Suite",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
        }
    }
}

// Subscription Card Composable helpers
@Composable
fun SubscriptionOfferCard(
    modifier: Modifier = Modifier,
    title: String,
    price: String,
    features: List<String>,
    badge: String,
    isCurrent: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxHeight()
            .clickable(enabled = !isCurrent) { onClick() },
        border = BorderStroke(
            1.5.dp, 
            if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                // Badge info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = badge,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    if (isCurrent) {
                        Text(
                            "ACTIVE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = price,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                // List items features in checklist format
                features.forEach { feat ->
                    Row(
                        modifier = Modifier.padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = "Included",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = feat,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            lineHeight = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onClick,
                enabled = !isCurrent,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isCurrent) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = if (isCurrent) "Current Plan" else "Select",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// Card Luhn validation checksum checks
private fun luhnCheck(number: String): Boolean {
    var sum = 0
    var alternate = false
    for (i in number.length - 1 downTo 0) {
        var n = number[i].toString().toInt()
        if (alternate) {
            n *= 2
            if (n > 9) {
                n = (n % 10) + 1
            }
        }
        sum += n
        alternate = !alternate
    }
    return sum % 10 == 0
}

// Detect popular card issues/brands
private fun detectCardBrand(raw: String): String {
    if (raw.isEmpty()) return "Unknown"
    return when {
        raw.startsWith("4") -> "Visa"
        raw.startsWith("5") -> "MasterCard"
        raw.startsWith("34") || raw.startsWith("37") -> "Amex"
        raw.startsWith("6") -> "Discover"
        else -> "Secure CC"
    }
}

private fun getCardBrandIcon(brand: String): ImageVector {
    return when (brand) {
        "Visa" -> Icons.Filled.CreditCard
        "MasterCard" -> Icons.Filled.CreditCard
        "Amex" -> Icons.Filled.CreditCard
        else -> Icons.Outlined.CreditCard
    }
}

// Card spacing layout text helper formatting
private fun formatCardNumberSpacing(clean: String): String {
    val digitsOnly = clean.replace(" ", "")
    val sb = StringBuilder()
    for (i in digitsOnly.indices) {
        sb.append(digitsOnly[i])
        if ((i + 1) % 4 == 0 && (i + 1) != digitsOnly.length) {
            sb.append(" ")
        }
    }
    return sb.toString()
}

// Expiry MM/YY formatting helper
private fun formatExpiryDate(input: String): String {
    val digits = input.replace("/", "")
    if (digits.isEmpty()) return ""
    return if (digits.length >= 2) {
        val mm = digits.take(2)
        val yy = digits.substring(2)
        if (yy.isNotEmpty()) "$mm/$yy" else mm
    } else {
        digits
    }
}

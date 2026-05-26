package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.InfoCalm
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.layout.Spacer

import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextButton

// Spacing scale
object Spacing {
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
}

// Radius scale
object Radius {
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
    val full = 9999.dp
}

@Composable
fun CardBase(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.shadow(4.dp, RoundedCornerShape(Radius.lg)),
        shape = RoundedCornerShape(Radius.lg),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        content = {
            Box(Modifier.padding(Spacing.lg)) {
                content()
            }
        }
    )
}

@Composable
fun CardIslamic(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.shadow(6.dp, RoundedCornerShape(Radius.xl)),
        shape = RoundedCornerShape(Radius.xl),
        border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.5f)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        content = {
            Box(Modifier.padding(Spacing.xl)) {
                content()
            }
        }
    )
}

@Composable
fun CardHealth(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.shadow(4.dp, RoundedCornerShape(Radius.lg)),
        shape = RoundedCornerShape(Radius.lg),
        border = BorderStroke(1.dp, InfoCalm.copy(alpha = 0.3f)),
        colors = CardDefaults.cardColors(
            containerColor = InfoCalm.copy(alpha = 0.05f)
        ),
        content = {
            Box(Modifier.padding(Spacing.lg)) {
                content()
            }
        }
    )
}

@Composable
fun BanglaText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontWeight: FontWeight? = null,
    fontSize: TextUnit = TextUnit.Unspecified,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    overflow: TextOverflow = TextOverflow.Clip,
    maxLines: Int = Int.MAX_VALUE
) {
    val fontScale = com.example.ui.theme.LocalFontScale.current
    val finalFontSize = if (fontSize != TextUnit.Unspecified && fontSize.isSp) {
        (fontSize.value * fontScale).sp
    } else if (fontSize == TextUnit.Unspecified) {
        (16 * fontScale).sp
    } else {
        fontSize
    }
    val finalLineHeight = if (lineHeight != TextUnit.Unspecified && lineHeight.isSp) {
        (lineHeight.value * fontScale).sp
    } else {
        lineHeight
    }

    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.bodyLarge,
        color = color,
        fontWeight = fontWeight,
        fontSize = finalFontSize,
        textAlign = textAlign,
        lineHeight = finalLineHeight,
        textDecoration = textDecoration,
        overflow = overflow,
        maxLines = maxLines
    )
}

@Composable
fun BanglaHeading(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontWeight: FontWeight? = null,
    fontSize: TextUnit = TextUnit.Unspecified,
    textAlign: TextAlign? = null,
    overflow: TextOverflow = TextOverflow.Clip,
    maxLines: Int = Int.MAX_VALUE
) {
    val fontScale = com.example.ui.theme.LocalFontScale.current
    val finalFontSize = if (fontSize != TextUnit.Unspecified && fontSize.isSp) {
        (fontSize.value * fontScale).sp
    } else if (fontSize == TextUnit.Unspecified) {
        (22 * fontScale).sp
    } else {
        fontSize
    }

    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.headlineMedium,
        color = color,
        fontWeight = fontWeight,
        fontSize = finalFontSize,
        textAlign = textAlign,
        overflow = overflow,
        maxLines = maxLines
    )
}

@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    icon: @Composable (() -> Unit)? = null
) {
    val largeButton = com.example.ui.theme.LocalLargeButtonMode.current
    val fontScale = com.example.ui.theme.LocalFontScale.current
    val minHeight = if (largeButton) 72.dp else 56.dp

    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = minHeight),
        enabled = enabled,
        shape = RoundedCornerShape(Radius.md),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(alpha = 0.5f),
            disabledContentColor = contentColor.copy(alpha = 0.5f)
        )
    ) {
        if (icon != null) {
            icon()
            Spacer(modifier = Modifier.width(Spacing.sm))
        }
        BanglaText(
            text = text,
            color = contentColor,
            fontWeight = FontWeight.Bold,
            fontSize = (16 * fontScale).sp
        )
    }
}

@Composable
fun AppTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.defaultMinSize(minHeight = 48.dp)
    ) {
        BanglaText(text = text, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { BanglaText(text = label) },
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 56.dp),
        singleLine = singleLine,
        shape = RoundedCornerShape(Radius.md),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
        )
    )
}

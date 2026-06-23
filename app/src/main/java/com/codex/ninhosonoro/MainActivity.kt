package com.codex.ninhosonoro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NinhoSonoroTheme {
                NinhoSonoroApp()
            }
        }
    }
}

private data class SleepSoundUi(
    val kind: SleepSoundKind,
    val title: String,
    val subtitle: String,
    val accent: Color,
    val icon: ImageVector,
    val gradient: List<Color>
)

private val sleepSounds = listOf(
    SleepSoundUi(
        kind = SleepSoundKind.WOMB,
        title = "Utero calmo",
        subtitle = "pulso grave e macio",
        accent = Color(0xFF8D79E6),
        icon = Icons.Rounded.Favorite,
        gradient = listOf(Color(0xFFFFE6ED), Color(0xFFE8E4FF))
    ),
    SleepSoundUi(
        kind = SleepSoundKind.RAIN,
        title = "Chuva fina",
        subtitle = "gotas suaves no quarto",
        accent = Color(0xFF4E88D7),
        icon = Icons.Rounded.WaterDrop,
        gradient = listOf(Color(0xFFE7F6FF), Color(0xFFDDE9FF))
    ),
    SleepSoundUi(
        kind = SleepSoundKind.WHITE_NOISE,
        title = "Ruido branco",
        subtitle = "cobertor sonoro estavel",
        accent = Color(0xFFB08C63),
        icon = Icons.Rounded.GraphicEq,
        gradient = listOf(Color(0xFFFFF4DA), Color(0xFFFFE7BD))
    ),
    SleepSoundUi(
        kind = SleepSoundKind.WIND,
        title = "Vento leve",
        subtitle = "respiro lento da noite",
        accent = Color(0xFF4C9C91),
        icon = Icons.Rounded.Air,
        gradient = listOf(Color(0xFFE4FFF3), Color(0xFFDAF1FF))
    ),
    SleepSoundUi(
        kind = SleepSoundKind.LULLABY,
        title = "Caixinha",
        subtitle = "melodia simples e doce",
        accent = Color(0xFFE08A53),
        icon = Icons.Rounded.MusicNote,
        gradient = listOf(Color(0xFFFFEDD7), Color(0xFFFFD9CF))
    ),
    SleepSoundUi(
        kind = SleepSoundKind.HEARTBEAT,
        title = "Coracao",
        subtitle = "ritmo quente e constante",
        accent = Color(0xFFD96F8B),
        icon = Icons.Rounded.Favorite,
        gradient = listOf(Color(0xFFFFE1E7), Color(0xFFFFF0F2))
    )
)

@Composable
private fun NinhoSonoroApp() {
    val engine = remember { SleepAudioEngine() }
    DisposableEffect(Unit) {
        onDispose { engine.release() }
    }

    var selected by rememberSaveable { mutableStateOf(SleepSoundKind.WOMB) }
    var volume by rememberSaveable { mutableFloatStateOf(0.62f) }
    var timerMinutes by rememberSaveable { mutableIntStateOf(30) }
    var remainingSeconds by rememberSaveable { mutableIntStateOf(timerMinutes * 60) }
    var isPlaying by rememberSaveable { mutableStateOf(false) }

    val selectedSound = sleepSounds.first { it.kind == selected }

    LaunchedEffect(volume) {
        engine.setVolume(volume)
    }

    LaunchedEffect(selected, isPlaying) {
        if (isPlaying) {
            engine.play(selected, volume)
        }
    }

    LaunchedEffect(timerMinutes, isPlaying) {
        if (!isPlaying) {
            remainingSeconds = timerMinutes * 60
        }
    }

    LaunchedEffect(isPlaying, timerMinutes) {
        while (isPlaying && timerMinutes > 0 && remainingSeconds > 0) {
            delay(1_000)
            remainingSeconds -= 1
            if (remainingSeconds == 0) {
                isPlaying = false
                engine.stop()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFFFF7EC),
                        Color(0xFFF6EEFF),
                        Color(0xFFEAF7F4)
                    )
                )
            )
    ) {
        SleepBackdrop()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(WindowInsets.safeDrawing.asPaddingValues())
                .padding(horizontal = 20.dp)
                .padding(top = 12.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Header()
            SleepHero(
                sound = selectedSound,
                isPlaying = isPlaying,
                volume = volume,
                timerMinutes = timerMinutes,
                remainingSeconds = remainingSeconds,
                onPlayClick = {
                    if (isPlaying) {
                        isPlaying = false
                        engine.stop()
                    } else {
                        if (timerMinutes > 0 && remainingSeconds <= 0) {
                            remainingSeconds = timerMinutes * 60
                        }
                        isPlaying = true
                        engine.play(selected, volume)
                    }
                }
            )

            SectionTitle(title = "Escolha o ambiente", subtitle = "toque suave, troca instantanea")
            SoundGrid(
                selected = selected,
                onSelected = { kind ->
                    selected = kind
                    if (isPlaying) {
                        engine.play(kind, volume)
                    }
                }
            )

            TimerSelector(
                selectedMinutes = timerMinutes,
                onSelected = { minutes ->
                    timerMinutes = minutes
                    remainingSeconds = minutes * 60
                }
            )

            VolumePanel(
                volume = volume,
                onVolumeChange = { volume = it }
            )

            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun Header() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "Ninho Sonoro",
                color = Color(0xFF3F365A),
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.sp
            )
            Text(
                text = "sono do bebe, simples e encantador",
                color = Color(0xFF7A718F),
                fontSize = 14.sp
            )
        }

        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.72f))
                .border(1.dp, Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.NightsStay,
                contentDescription = null,
                tint = Color(0xFF7069D7),
                modifier = Modifier.size(27.dp)
            )
        }
    }
}

@Composable
private fun SleepHero(
    sound: SleepSoundUi,
    isPlaying: Boolean,
    volume: Float,
    timerMinutes: Int,
    remainingSeconds: Int,
    onPlayClick: () -> Unit
) {
    val pulseTransition = rememberInfiniteTransition(label = "sleep pulse")
    val pulse by pulseTransition.animateFloat(
        initialValue = 0.82f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2_000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse size"
    )
    val activeColor by animateColorAsState(
        targetValue = if (isPlaying) sound.accent else Color(0xFFBBB1C9),
        label = "active accent"
    )
    val progress = if (timerMinutes == 0) 1f else {
        remainingSeconds.toFloat() / (timerMinutes * 60).toFloat()
    }.coerceIn(0f, 1f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(36.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.88f),
                        sound.gradient.first().copy(alpha = 0.78f),
                        sound.gradient.last().copy(alpha = 0.9f)
                    )
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.9f), RoundedCornerShape(36.dp))
            .padding(22.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isPlaying) "Tocando agora" else "Pronto para ninar",
                        color = Color(0xFF70677E),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = sound.title,
                        color = Color(0xFF2F2944),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.White.copy(alpha = 0.72f))
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Timer,
                            contentDescription = null,
                            tint = activeColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = if (timerMinutes == 0) "Livre" else formatTime(remainingSeconds),
                            color = Color(0xFF3F365A),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            PlaybackOrb(
                accent = activeColor,
                progress = progress,
                pulse = if (isPlaying) pulse else 0.86f,
                isPlaying = isPlaying,
                onClick = onPlayClick
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HeroMetric(
                    label = "Volume",
                    value = "${(volume * 100).toInt()}%",
                    modifier = Modifier.weight(1f)
                )
                HeroMetric(
                    label = "Timer",
                    value = if (timerMinutes == 0) "livre" else "${timerMinutes} min",
                    modifier = Modifier.weight(1f)
                )
                HeroMetric(
                    label = "Clima",
                    value = "suave",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun PlaybackOrb(
    accent: Color,
    progress: Float,
    pulse: Float,
    isPlaying: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier.size(178.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 13.dp.toPx()
            drawCircle(
                color = Color.White.copy(alpha = 0.72f),
                radius = size.minDimension * 0.42f,
                center = center
            )
            drawCircle(
                color = accent.copy(alpha = 0.12f),
                radius = size.minDimension * 0.48f * pulse,
                center = center
            )
            drawArc(
                color = Color(0xFFE8DFF3),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            drawArc(
                color = accent,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(
                            accent.copy(alpha = 0.98f),
                            Color(0xFFFFB59C).copy(alpha = if (isPlaying) 0.95f else 0.55f)
                        )
                    )
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                contentDescription = if (isPlaying) "Pausar" else "Tocar",
                tint = Color.White,
                modifier = Modifier.size(42.dp)
            )
        }
    }
}

@Composable
private fun HeroMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.58f))
            .padding(horizontal = 12.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            color = Color(0xFF81778F),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = value,
            color = Color(0xFF332A45),
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1
        )
    }
}

@Composable
private fun SectionTitle(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = title,
            color = Color(0xFF332A45),
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = subtitle,
            color = Color(0xFF81778F),
            fontSize = 13.sp
        )
    }
}

@Composable
private fun SoundGrid(selected: SleepSoundKind, onSelected: (SleepSoundKind) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        sleepSounds.chunked(2).forEach { rowSounds ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                rowSounds.forEach { sound ->
                    SoundCard(
                        sound = sound,
                        selected = selected == sound.kind,
                        onClick = { onSelected(sound.kind) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowSounds.size == 1) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun SoundCard(
    sound: SleepSoundUi,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor by animateColorAsState(
        targetValue = if (selected) sound.accent else Color.White.copy(alpha = 0.72f),
        label = "sound border"
    )

    Column(
        modifier = modifier
            .height(136.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(Brush.linearGradient(sound.gradient))
            .border(1.5.dp, borderColor, RoundedCornerShape(26.dp))
            .clickable(onClick = onClick)
            .padding(15.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.74f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = sound.icon,
                    contentDescription = null,
                    tint = sound.accent,
                    modifier = Modifier.size(23.dp)
                )
            }
            if (selected) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(sound.accent)
                        .border(3.dp, Color.White, CircleShape)
                )
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = sound.title,
                color = Color(0xFF332A45),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp,
                maxLines = 1
            )
            Text(
                text = sound.subtitle,
                color = Color(0xFF71687E),
                fontSize = 12.sp,
                lineHeight = 15.sp
            )
        }
    }
}

@Composable
private fun TimerSelector(selectedMinutes: Int, onSelected: (Int) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitle(title = "Temporizador", subtitle = "o som desliga sozinho")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            listOf(15, 30, 45, 60, 0).forEach { minutes ->
                TimerChip(
                    label = if (minutes == 0) "Livre" else "${minutes}m",
                    selected = selectedMinutes == minutes,
                    onClick = { onSelected(minutes) }
                )
            }
        }
    }
}

@Composable
private fun TimerChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(if (selected) Color(0xFF5D5BD7) else Color.White.copy(alpha = 0.72f))
            .border(
                width = 1.dp,
                color = if (selected) Color(0xFF5D5BD7) else Color.White,
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else Color(0xFF4C435E),
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun VolumePanel(volume: Float, onVolumeChange: (Float) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Color.White.copy(alpha = 0.72f))
            .border(1.dp, Color.White, RoundedCornerShape(28.dp))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.VolumeUp,
                    contentDescription = null,
                    tint = Color(0xFF7069D7),
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Volume do quarto",
                    color = Color(0xFF332A45),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp
                )
            }
            Text(
                text = "${(volume * 100).toInt()}%",
                color = Color(0xFF7069D7),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp
            )
        }
        Slider(
            value = volume,
            onValueChange = onVolumeChange,
            valueRange = 0f..1f,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF7069D7),
                activeTrackColor = Color(0xFF7069D7),
                inactiveTrackColor = Color(0xFFE6DFEE)
            )
        )
    }
}

@Composable
private fun SleepBackdrop() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        drawCircle(
            color = Color.White.copy(alpha = 0.38f),
            radius = w * 0.22f,
            center = Offset(w * 0.88f, h * 0.11f)
        )
        drawCircle(
            color = Color(0xFFFFCF9E).copy(alpha = 0.18f),
            radius = w * 0.18f,
            center = Offset(w * 0.08f, h * 0.31f)
        )
        drawLine(
            color = Color.White.copy(alpha = 0.5f),
            start = Offset(w * 0.04f, h * 0.17f),
            end = Offset(w * 0.42f, h * 0.12f),
            strokeWidth = 2.5f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = Color.White.copy(alpha = 0.45f),
            start = Offset(w * 0.58f, h * 0.42f),
            end = Offset(w * 0.96f, h * 0.38f),
            strokeWidth = 2.2f,
            cap = StrokeCap.Round
        )
        repeat(10) { index ->
            val x = w * ((index * 37 % 91) / 100f)
            val y = h * (0.08f + (index * 19 % 78) / 100f)
            drawCircle(
                color = Color.White.copy(alpha = 0.55f),
                radius = 2.2f + (index % 3),
                center = Offset(x, y)
            )
        }
    }
}

private fun formatTime(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}

@Composable
private fun NinhoSonoroTheme(content: @Composable () -> Unit) {
    val scheme = lightColorScheme(
        primary = Color(0xFF7069D7),
        secondary = Color(0xFFE08A53),
        tertiary = Color(0xFF4C9C91),
        background = Color(0xFFFFF7EC),
        surface = Color.White,
        onPrimary = Color.White,
        onSecondary = Color.White,
        onTertiary = Color.White,
        onBackground = Color(0xFF332A45),
        onSurface = Color(0xFF332A45)
    )
    MaterialTheme(colorScheme = scheme, content = content)
}

@Preview(showBackground = true, widthDp = 390, heightDp = 840)
@Composable
private fun NinhoSonoroPreview() {
    NinhoSonoroTheme {
        Surface {
            NinhoSonoroApp()
        }
    }
}

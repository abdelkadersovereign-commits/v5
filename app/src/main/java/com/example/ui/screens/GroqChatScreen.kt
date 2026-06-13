package com.example.ui.screens

  import androidx.compose.animation.core.*
  import androidx.compose.foundation.BorderStroke
  import androidx.compose.foundation.background
  import androidx.compose.foundation.border
  import androidx.compose.foundation.layout.*
  import androidx.compose.foundation.lazy.LazyColumn
  import androidx.compose.foundation.lazy.items
  import androidx.compose.foundation.lazy.rememberLazyListState
  import androidx.compose.foundation.shape.CircleShape
  import androidx.compose.foundation.shape.RoundedCornerShape
  import androidx.compose.foundation.text.KeyboardActions
  import androidx.compose.foundation.text.KeyboardOptions
  import androidx.compose.material.icons.Icons
  import androidx.compose.material.icons.rounded.*
  import androidx.compose.material3.*
  import androidx.compose.runtime.*
  import androidx.compose.ui.Alignment
  import androidx.compose.ui.Modifier
  import androidx.compose.ui.graphics.Brush
  import androidx.compose.ui.graphics.Color
  import androidx.compose.ui.graphics.graphicsLayer
  import androidx.compose.ui.platform.LocalFocusManager
  import androidx.compose.ui.text.font.FontFamily
  import androidx.compose.ui.text.font.FontWeight
  import androidx.compose.ui.text.input.ImeAction
  import androidx.compose.ui.text.style.TextAlign
  import androidx.compose.ui.unit.dp
  import androidx.compose.ui.unit.sp
  import com.example.adaptive.UIConfig
  import com.example.data.chat.ChatMessage
  import com.example.ui.theme.AmberGold
  import com.example.ui.theme.CyberCyan
  import com.example.ui.theme.DeepNavy
  import com.example.ui.theme.SurfaceCard
  import com.example.ui.theme.SurfaceDark
  import com.example.ui.theme.TextMuted
  import com.example.ui.theme.TextSecondary
  import com.example.ui.viewmodel.GroqChatViewModel
  import kotlinx.coroutines.launch

  @Composable
  fun GroqChatScreen(
      viewModel: GroqChatViewModel,
      uiConfig: UIConfig,
      isArabic: Boolean
  ) {
      val messages  by viewModel.messages.collectAsState()
      val isTyping  by viewModel.isTyping.collectAsState()
      val listState = rememberLazyListState()
      val scope     = rememberCoroutineScope()
      var inputText by remember { mutableStateOf("") }

      LaunchedEffect(Unit) {
          viewModel.runBackgroundAnalysis(uiConfig)
      }

      LaunchedEffect(messages.size, isTyping) {
          val target = if (isTyping) messages.size else maxOf(0, messages.size - 1)
          if (messages.isNotEmpty() || isTyping) {
              scope.launch { listState.animateScrollToItem(target) }
          }
      }

      Column(
          modifier = Modifier
              .fillMaxSize()
              .background(DeepNavy)
      ) {
          ChatTopBar(onClear = { viewModel.clearHistory() })

          if (messages.isEmpty() && !isTyping) {
              ChatEmptyState(
                  isArabic  = isArabic,
                  onPrompt  = { viewModel.sendMessage(it, uiConfig) }
              )
          } else {
              LazyColumn(
                  state             = listState,
                  modifier          = Modifier.weight(1f),
                  contentPadding    = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                  verticalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                  items(messages, key = { it.id }) { msg ->
                      ChatMessageBubble(message = msg, isArabic = isArabic)
                  }
                  if (isTyping) {
                      item(key = "typing_indicator") {
                          TypingIndicatorRow()
                      }
                  }
              }
          }

          ChatInputBar(
              value     = inputText,
              onChange  = { inputText = it },
              onSend    = {
                  val text = inputText.trim()
                  if (text.isNotBlank()) {
                      viewModel.sendMessage(text, uiConfig)
                      inputText = ""
                  }
              },
              isEnabled = !isTyping,
              isArabic  = isArabic
          )
      }
  }

  // ── Top bar ──────────────────────────────────────────────────────────────────

  @Composable
  private fun ChatTopBar(onClear: () -> Unit) {
      Box(
          modifier = Modifier
              .fillMaxWidth()
              .background(
                  Brush.verticalGradient(
                      colors = listOf(Color(0xFF060D18), DeepNavy)
                  )
              )
              .padding(horizontal = 16.dp, vertical = 14.dp)
      ) {
          Column(
              modifier = Modifier.align(Alignment.Center),
              horizontalAlignment = Alignment.CenterHorizontally
          ) {
              Text(
                  text       = "MASTER BRAIN",
                  color      = CyberCyan,
                  fontSize   = 13.sp,
                  fontWeight = FontWeight.Black,
                  fontFamily = FontFamily.Monospace,
                  letterSpacing = 2.5.sp
              )
              Text(
                  text     = "LLaMA 3.3 · Groq",
                  color    = CyberCyan.copy(alpha = 0.45f),
                  fontSize = 10.sp,
                  fontFamily = FontFamily.Monospace
              )
          }

          Box(
              modifier = Modifier
                  .align(Alignment.CenterEnd)
                  .size(34.dp)
                  .background(Color.White.copy(alpha = 0.04f), CircleShape)
                  .border(1.dp, Color.White.copy(alpha = 0.06f), CircleShape),
              contentAlignment = Alignment.Center
          ) {
              IconButton(onClick = onClear, modifier = Modifier.size(34.dp)) {
                  Icon(
                      imageVector        = Icons.Rounded.DeleteSweep,
                      contentDescription = "Clear history",
                      tint               = Color.White.copy(alpha = 0.35f),
                      modifier           = Modifier.size(16.dp)
                  )
              }
          }
      }
  }

  // ── Empty state ───────────────────────────────────────────────────────────────

  @Composable
  private fun ChatEmptyState(isArabic: Boolean, onPrompt: (String) -> Unit) {
      val suggestions = if (isArabic) listOf(
          "غيّر لون التطبيق إلى العنبر",
          "اجعل الواجهة أكثر إحكاماً",
          "ماذا تستطيع أن تفعل؟"
      ) else listOf(
          "Change the accent color to amber",
          "Switch the layout to compact mode",
          "What can you do for me?"
      )

      Column(
          modifier = Modifier
              .weight(1f)
              .fillMaxWidth()
              .padding(horizontal = 32.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center
      ) {
          Box(
              modifier = Modifier
                  .size(80.dp)
                  .background(CyberCyan.copy(alpha = 0.06f), CircleShape)
                  .border(1.dp, CyberCyan.copy(alpha = 0.15f), CircleShape),
              contentAlignment = Alignment.Center
          ) {
              Icon(
                  imageVector        = Icons.Rounded.SmartToy,
                  contentDescription = null,
                  tint               = CyberCyan.copy(alpha = 0.5f),
                  modifier           = Modifier.size(38.dp)
              )
          }

          Spacer(modifier = Modifier.height(20.dp))

          Text(
              text       = if (isArabic) "المخ الرئيسي جاهز" else "Master Brain is Online",
              color      = CyberCyan.copy(alpha = 0.8f),
              fontSize   = 17.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
          )

          Spacer(modifier = Modifier.height(8.dp))

          Text(
              text      = if (isArabic)
                  "اسألني عن أي شيء أو اطلب تخصيص الواجهة"
              else
                  "Ask me anything or request a UI change.",
              color     = TextMuted,
              fontSize  = 12.sp,
              textAlign = TextAlign.Center,
              lineHeight = 18.sp
          )

          Spacer(modifier = Modifier.height(28.dp))

          suggestions.forEach { prompt ->
              OutlinedButton(
                  onClick  = { onPrompt(prompt) },
                  modifier = Modifier
                      .fillMaxWidth()
                      .padding(vertical = 3.dp),
                  colors   = ButtonDefaults.outlinedButtonColors(contentColor = CyberCyan),
                  border   = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.22f)),
                  shape    = RoundedCornerShape(20.dp)
              ) {
                  Text(
                      text       = prompt,
                      fontSize   = 12.sp,
                      fontFamily = FontFamily.Monospace,
                      modifier   = Modifier.padding(vertical = 2.dp)
                  )
              }
          }
      }
  }

  // ── Message bubbles ───────────────────────────────────────────────────────────

  @Composable
  private fun ChatMessageBubble(message: ChatMessage, isArabic: Boolean) {
      when (message.role) {
          "user"       -> UserBubble(message)
          "suggestion" -> SuggestionBubble(message)
          else         -> AssistantBubble(message)
      }
  }

  @Composable
  private fun UserBubble(message: ChatMessage) {
      Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End
      ) {
          Box(
              modifier = Modifier
                  .widthIn(max = 270.dp)
                  .background(
                      CyberCyan.copy(alpha = 0.12f),
                      RoundedCornerShape(topStart = 18.dp, topEnd = 4.dp, bottomStart = 18.dp, bottomEnd = 18.dp)
                  )
                  .border(
                      1.dp,
                      CyberCyan.copy(alpha = 0.28f),
                      RoundedCornerShape(topStart = 18.dp, topEnd = 4.dp, bottomStart = 18.dp, bottomEnd = 18.dp)
                  )
                  .padding(horizontal = 14.dp, vertical = 10.dp)
          ) {
              Text(
                  text       = message.content,
                  color      = Color.White.copy(alpha = 0.95f),
                  fontSize   = 14.sp,
                  lineHeight = 21.sp
              )
          }
      }
  }

  @Composable
  private fun AssistantBubble(message: ChatMessage) {
      Row(
          modifier          = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.Start,
          verticalAlignment = Alignment.Top
      ) {
          Box(
              modifier = Modifier
                  .size(30.dp)
                  .background(CyberCyan.copy(alpha = 0.08f), CircleShape)
                  .border(1.dp, CyberCyan.copy(alpha = 0.2f), CircleShape),
              contentAlignment = Alignment.Center
          ) {
              Icon(
                  imageVector        = Icons.Rounded.SmartToy,
                  contentDescription = null,
                  tint               = CyberCyan.copy(alpha = 0.8f),
                  modifier           = Modifier.size(14.dp)
              )
          }

          Spacer(modifier = Modifier.width(8.dp))

          Box(
              modifier = Modifier
                  .widthIn(max = 270.dp)
                  .background(
                      SurfaceDark,
                      RoundedCornerShape(topStart = 4.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 18.dp)
                  )
                  .border(
                      1.dp,
                      Color.White.copy(alpha = 0.05f),
                      RoundedCornerShape(topStart = 4.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 18.dp)
                  )
                  .padding(horizontal = 14.dp, vertical = 10.dp)
          ) {
              Text(
                  text       = message.content,
                  color      = Color.White.copy(alpha = 0.88f),
                  fontSize   = 14.sp,
                  lineHeight = 21.sp
              )
          }
      }
  }

  @Composable
  private fun SuggestionBubble(message: ChatMessage) {
      Box(
          modifier = Modifier
              .fillMaxWidth()
              .background(AmberGold.copy(alpha = 0.05f), RoundedCornerShape(14.dp))
              .border(1.dp, AmberGold.copy(alpha = 0.22f), RoundedCornerShape(14.dp))
              .padding(12.dp)
      ) {
          Row(verticalAlignment = Alignment.Top) {
              Icon(
                  imageVector        = Icons.Rounded.Lightbulb,
                  contentDescription = null,
                  tint               = AmberGold,
                  modifier           = Modifier.size(15.dp).padding(top = 1.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                  Text(
                      text          = "PROACTIVE INSIGHT",
                      color         = AmberGold,
                      fontSize      = 9.sp,
                      fontWeight    = FontWeight.Black,
                      fontFamily    = FontFamily.Monospace,
                      letterSpacing = 1.2.sp
                  )
                  Spacer(modifier = Modifier.height(5.dp))
                  Text(
                      text       = message.content,
                      color      = Color.White.copy(alpha = 0.82f),
                      fontSize   = 13.sp,
                      lineHeight = 19.sp
                  )
              }
          }
      }
  }

  // ── Typing indicator ──────────────────────────────────────────────────────────

  @Composable
  private fun TypingIndicatorRow() {
      val transition = rememberInfiniteTransition(label = "typing")

      Row(
          modifier          = Modifier.padding(start = 14.dp, top = 2.dp, bottom = 4.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(5.dp)
      ) {
          Box(
              modifier = Modifier
                  .size(26.dp)
                  .background(CyberCyan.copy(alpha = 0.08f), CircleShape)
                  .border(1.dp, CyberCyan.copy(alpha = 0.15f), CircleShape),
              contentAlignment = Alignment.Center
          ) {
              Icon(Icons.Rounded.SmartToy, contentDescription = null, tint = CyberCyan.copy(alpha = 0.6f), modifier = Modifier.size(12.dp))
          }

          Spacer(modifier = Modifier.width(4.dp))

          repeat(3) { i ->
              val offset by transition.animateFloat(
                  initialValue = 0f,
                  targetValue  = -5f,
                  animationSpec = infiniteRepeatable(
                      animation = tween(380, easing = FastOutSlowInEasing),
                      repeatMode = RepeatMode.Reverse,
                      initialStartOffset = StartOffset(i * 110)
                  ),
                  label = "dot$i"
              )
              Box(
                  modifier = Modifier
                      .size(7.dp)
                      .graphicsLayer { translationY = offset }
                      .background(CyberCyan.copy(alpha = 0.65f), CircleShape)
              )
          }
      }
  }

  // ── Input bar ─────────────────────────────────────────────────────────────────

  @Composable
  private fun ChatInputBar(
      value: String,
      onChange: (String) -> Unit,
      onSend: () -> Unit,
      isEnabled: Boolean,
      isArabic: Boolean
  ) {
      val focusManager = LocalFocusManager.current

      Surface(
          modifier     = Modifier.fillMaxWidth(),
          color        = Color(0xFF060D18),
          tonalElevation = 0.dp
      ) {
          Row(
              modifier = Modifier
                  .fillMaxWidth()
                  .navigationBarsPadding()
                  .padding(horizontal = 12.dp, vertical = 10.dp),
              verticalAlignment = Alignment.Bottom,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
              OutlinedTextField(
                  value         = value,
                  onValueChange = onChange,
                  enabled       = isEnabled,
                  modifier      = Modifier.weight(1f),
                  placeholder   = {
                      Text(
                          text     = if (isArabic) "اكتب رسالة..." else "Message Master Brain...",
                          color    = Color.White.copy(alpha = 0.25f),
                          fontSize = 13.sp
                      )
                  },
                  colors = OutlinedTextFieldDefaults.colors(
                      focusedTextColor      = Color.White,
                      unfocusedTextColor    = Color.White,
                      focusedBorderColor    = CyberCyan.copy(alpha = 0.4f),
                      unfocusedBorderColor  = Color.White.copy(alpha = 0.08f),
                      cursorColor           = CyberCyan,
                      focusedContainerColor   = SurfaceCard,
                      unfocusedContainerColor = SurfaceCard,
                      disabledContainerColor  = SurfaceCard,
                      disabledTextColor       = Color.White.copy(alpha = 0.4f),
                      disabledBorderColor     = Color.White.copy(alpha = 0.04f)
                  ),
                  shape           = RoundedCornerShape(24.dp),
                  maxLines        = 5,
                  keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                  keyboardActions = KeyboardActions(onSend = { onSend(); focusManager.clearFocus() }),
                  textStyle       = LocalTextStyle.current.copy(fontSize = 14.sp)
              )

              val active = value.isNotBlank() && isEnabled

              Box(
                  modifier = Modifier
                      .size(46.dp)
                      .background(
                          if (active) CyberCyan else Color.White.copy(alpha = 0.04f),
                          CircleShape
                      )
                      .border(
                          1.dp,
                          if (active) CyberCyan else Color.White.copy(alpha = 0.06f),
                          CircleShape
                      ),
                  contentAlignment = Alignment.Center
              ) {
                  IconButton(
                      onClick  = { onSend(); focusManager.clearFocus() },
                      enabled  = active,
                      modifier = Modifier.size(46.dp)
                  ) {
                      Icon(
                          imageVector        = Icons.Rounded.Send,
                          contentDescription = "Send",
                          tint               = if (active) Color(0xFF040810) else Color.White.copy(alpha = 0.18f),
                          modifier           = Modifier.size(18.dp)
                      )
                  }
              }
          }
      }
  }
  
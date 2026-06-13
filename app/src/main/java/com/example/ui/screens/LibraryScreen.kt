package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberZen
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.VoidBlack
import com.example.ui.viewmodel.DashboardViewModel
import java.util.Calendar

// ─── Data Models ────────────────────────────────────────────────────────────

data class DuaItem(
    val titleAr: String,
    val textAr: String,
    val category: String,
    val tag: String
)

data class HadithItem(
    val textAr: String,
    val source: String,
    val grade: String
)

data class SecurityTip(
    val titleAr: String,
    val bodyAr: String,
    val tag: String
)

// ─── Content Data ────────────────────────────────────────────────────────────

private val allDuas: List<DuaItem> = listOf(
    // الصباح
    DuaItem("دعاء الاستيقاظ", "اَلْحَمْدُ لِلَّهِ الَّذِي أَحْيَانَا بَعْدَ مَا أَمَاتَنَا وَإِلَيْهِ النُّشُورُ", "الصباح", "صباح"),
    DuaItem("دعاء الصباح المأثور", "اَللَّهُمَّ بِكَ أَصْبَحْنَا، وَبِكَ أَمْسَيْنَا، وَبِكَ نَحْيَا، وَبِكَ نَمُوتُ، وَإِلَيْكَ النُّشُورُ", "الصباح", "صباح"),
    DuaItem("دعاء العافية", "اَللَّهُمَّ إِنِّي أَسْأَلُكَ الْعَافِيَةَ فِي الدُّنْيَا وَالآخِرَةِ", "الصباح", "صباح"),
    DuaItem("دعاء الحفظ من الشر", "اَللَّهُمَّ احْفَظْنِي مِنْ بَيْنِ يَدَيَّ وَمِنْ خَلْفِي وَعَنْ يَمِينِي وَعَنْ شِمَالِي وَمِنْ فَوْقِي، وَأَعُوذُ بِعَظَمَتِكَ أَنْ أُغْتَالَ مِنْ تَحْتِي", "الصباح", "صباح"),
    DuaItem("سيد الاستغفار", "اَللَّهُمَّ أَنْتَ رَبِّي، لَا إِلَهَ إِلَّا أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ، وَأَنَا عَلَى عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ، أَعُوذُ بِكَ مِنْ شَرِّ مَا صَنَعْتُ", "الصباح", "صباح"),
    // المساء
    DuaItem("دعاء المساء", "اَللَّهُمَّ بِكَ أَمْسَيْنَا، وَبِكَ أَصْبَحْنَا، وَبِكَ نَحْيَا، وَبِكَ نَمُوتُ، وَإِلَيْكَ الْمَصِيرُ", "المساء", "مساء"),
    DuaItem("دعاء الحفظ مساءً", "أَعُوذُ بِكَلِمَاتِ اللَّهِ التَّامَّاتِ مِنْ شَرِّ مَا خَلَقَ", "المساء", "مساء"),
    DuaItem("دعاء اللهم إني أمسيت", "اَللَّهُمَّ إِنِّي أَمْسَيْتُ أُشْهِدُكَ وَأُشْهِدُ حَمَلَةَ عَرْشِكَ وَمَلَائِكَتَكَ وَجَمِيعَ خَلْقِكَ، أَنَّكَ أَنْتَ اللَّهُ لَا إِلَهَ إِلَّا أَنْتَ، وَأَنَّ مُحَمَّدًا عَبْدُكَ وَرَسُولُكَ", "المساء", "مساء"),
    // قبل النوم
    DuaItem("دعاء النوم", "اَللَّهُمَّ بِاسْمِكَ أَمُوتُ وَأَحْيَا", "قبل النوم", "نوم"),
    DuaItem("آية الكرسي", "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ ۚ لَا تَأْخُذُهُ سِنَةٌ وَلَا نَوْمٌ", "قبل النوم", "نوم"),
    DuaItem("دعاء الاستعاذة قبل النوم", "اَللَّهُمَّ قِنِي عَذَابَكَ يَوْمَ تَبْعَثُ عِبَادَكَ", "قبل النوم", "نوم"),
    // الطعام
    DuaItem("دعاء الأكل", "بِسْمِ اللَّهِ، اَللَّهُمَّ بَارِكْ لَنَا فِيمَا رَزَقْتَنَا وَقِنَا عَذَابَ النَّارِ", "الطعام", "طعام"),
    DuaItem("دعاء بعد الطعام", "اَلْحَمْدُ لِلَّهِ الَّذِي أَطْعَمَنَا وَسَقَانَا وَجَعَلَنَا مُسْلِمِينَ", "الطعام", "طعام"),
    // السفر
    DuaItem("دعاء السفر", "اَللَّهُمَّ إِنَّا نَسْأَلُكَ فِي سَفَرِنَا هَذَا الْبِرَّ وَالتَّقْوَى، وَمِنَ الْعَمَلِ مَا تَرْضَى", "السفر", "سفر"),
    DuaItem("دعاء ركوب السيارة", "سُبْحَانَ الَّذِي سَخَّرَ لَنَا هَذَا وَمَا كُنَّا لَهُ مُقْرِنِينَ، وَإِنَّا إِلَى رَبِّنَا لَمُنْقَلِبُونَ", "السفر", "سفر"),
    // الدخول والخروج
    DuaItem("دعاء دخول المنزل", "اَللَّهُمَّ إِنِّي أَسْأَلُكَ خَيْرَ الْمَوْلِجِ وَخَيْرَ الْمَخْرَجِ، بِسْمِ اللَّهِ وَلَجْنَا وَبِسْمِ اللَّهِ خَرَجْنَا وَعَلَى اللَّهِ رَبِّنَا تَوَكَّلْنَا", "الدخول والخروج", "منزل"),
    DuaItem("دعاء الخروج من المنزل", "بِسْمِ اللَّهِ تَوَكَّلْتُ عَلَى اللَّهِ وَلَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ", "الدخول والخروج", "منزل"),
    DuaItem("دعاء دخول المسجد", "اَللَّهُمَّ افْتَحْ لِي أَبْوَابَ رَحْمَتِكَ", "الدخول والخروج", "مسجد"),
    // الكرب والضيق
    DuaItem("دعاء الكرب", "لَا إِلَهَ إِلَّا اللَّهُ الْعَظِيمُ الْحَلِيمُ، لَا إِلَهَ إِلَّا اللَّهُ رَبُّ الْعَرْشِ الْعَظِيمِ، لَا إِلَهَ إِلَّا اللَّهُ رَبُّ السَّمَاوَاتِ وَرَبُّ الْأَرْضِ وَرَبُّ الْعَرْشِ الْكَرِيمِ", "الكرب والضيق", "كرب"),
    DuaItem("دعاء يونس عليه السلام", "لَا إِلَٰهَ إِلَّا أَنتَ سُبْحَانَكَ إِنِّي كُنتُ مِنَ الظَّالِمِينَ", "الكرب والضيق", "كرب"),
    DuaItem("دعاء الهم والحزن", "اَللَّهُمَّ إِنِّي عَبْدُكَ، ابْنُ عَبْدِكَ، ابْنُ أَمَتِكَ، نَاصِيَتِي بِيَدِكَ، مَاضٍ فِيَّ حُكْمُكَ، عَدْلٌ فِيَّ قَضَاؤُكَ", "الكرب والضيق", "كرب"),
    // الاستخارة والتوكل
    DuaItem("دعاء الاستخارة", "اَللَّهُمَّ إِنِّي أَسْتَخِيرُكَ بِعِلْمِكَ، وَأَسْتَقْدِرُكَ بِقُدْرَتِكَ، وَأَسْأَلُكَ مِنْ فَضْلِكَ الْعَظِيمِ", "الاستخارة", "استخارة"),
    DuaItem("دعاء التوكل", "حَسْبُنَا اللَّهُ وَنِعْمَ الْوَكِيلُ", "الاستخارة", "توكل"),
    // الرزق
    DuaItem("دعاء الرزق", "اَللَّهُمَّ اكْفِنِي بِحَلَالِكَ عَنْ حَرَامِكَ وَأَغْنِنِي بِفَضْلِكَ عَمَّنْ سِوَاكَ", "الرزق", "رزق"),
    DuaItem("دعاء البركة في الرزق", "اَللَّهُمَّ بَارِكْ لَنَا فِيمَا رَزَقْتَنَا وَزِدْنَا مِنْهُ", "الرزق", "رزق"),
    // الشكر
    DuaItem("دعاء الشكر", "رَبِّ أَوْزِعْنِي أَنْ أَشْكُرَ نِعْمَتَكَ الَّتِي أَنْعَمْتَ عَلَيَّ وَعَلَى وَالِدَيَّ وَأَنْ أَعْمَلَ صَالِحًا تَرْضَاهُ", "الشكر", "شكر"),
    // الوالدين
    DuaItem("دعاء الوالدين", "رَبِّ ارْحَمْهُمَا كَمَا رَبَّيَانِي صَغِيرًا", "الوالدين", "والدين"),
    DuaItem("دعاء البر بالوالدين", "رَبِّ اغْفِرْ لِي وَلِوَالِدَيَّ وَلِلْمُؤْمِنِينَ يَوْمَ يَقُومُ الْحِسَابُ", "الوالدين", "والدين"),
    // عام
    DuaItem("دعاء الهداية", "اهْدِنَا الصِّرَاطَ الْمُسْتَقِيمَ، صِرَاطَ الَّذِينَ أَنْعَمْتَ عَلَيْهِمْ", "عام", "هداية"),
    DuaItem("دعاء الثبات", "يَا مُقَلِّبَ الْقُلُوبِ ثَبِّتْ قَلْبِي عَلَى دِينِكَ", "عام", "ثبات")
)

private val allHadiths: List<HadithItem> = listOf(
    HadithItem("إِنَّمَا الأَعْمَالُ بِالنِّيَّاتِ، وَإِنَّمَا لِكُلِّ امْرِئٍ مَا نَوَى", "متفق عليه", "صحيح"),
    HadithItem("الدِّينُ النَّصِيحَةُ. قُلنَا: لِمَنْ؟ قَالَ: للهِ، وَلِكِتَابِهِ، وَلِرَسُولِهِ، وَلأَئِمَّةِ المُسْلِمِينَ، وَعَامَّتِهِم", "رواه مسلم", "صحيح"),
    HadithItem("اتَّقِ اللهَ حَيثُمَا كُنتَ، وَأَتبِعِ السَّيِّئَةَ الحَسَنَةَ تَمحُهَا، وَخَالِقِ النَّاسَ بِخُلُقٍ حَسَنٍ", "رواه الترمذي", "حسن"),
    HadithItem("مَنْ كَانَ يُؤمِنُ بِاللهِ وَاليَومِ الآخِرِ فَليَقُل خَيرًا أَو لِيَصمُت", "متفق عليه", "صحيح"),
    HadithItem("لا يُؤمِنُ أَحَدُكُم حَتَّى يُحِبَّ لأَخِيهِ مَا يُحِبُّ لِنَفسِهِ", "متفق عليه", "صحيح"),
    HadithItem("المُسلِمُ مَن سَلِمَ المُسلِمُونَ مِن لِسَانِهِ وَيَدِهِ، وَالمُهَاجِرُ مَن هَجَرَ مَا نَهَى اللهُ عَنهُ", "رواه البخاري", "صحيح"),
    HadithItem("مَن لا يَرحَمُ النَّاسَ لا يَرحَمهُ اللهُ", "متفق عليه", "صحيح"),
    HadithItem("إِنَّ اللهَ جَمِيلٌ يُحِبُّ الجَمَالَ", "رواه مسلم", "صحيح"),
    HadithItem("أَحَبُّ الأَعمَالِ إِلَى اللهِ أَدوَمُهَا وَإِن قَلَّ", "متفق عليه", "صحيح"),
    HadithItem("الطَّهُورُ شَطرُ الإِيمَانِ، وَالحَمدُ للهِ تَملأُ المِيزَانَ، وَسُبحَانَ اللهِ وَالحَمدُ للهِ تَملآنِ مَا بَينَ السَّمَاءِ وَالأَرضِ", "رواه مسلم", "صحيح"),
    HadithItem("البِرُّ حُسنُ الخُلُقِ، وَالإِثمُ مَا حَاكَ فِي نَفسِكَ وَكَرِهتَ أَن يَطَّلِعَ عَلَيهِ النَّاسُ", "رواه مسلم", "صحيح"),
    HadithItem("مَن سَلَكَ طَرِيقًا يَلتَمِسُ فِيهِ عِلمًا سَهَّلَ اللهُ لَهُ طَرِيقًا إِلَى الجَنَّةِ", "رواه مسلم", "صحيح"),
    HadithItem("خَيرُكُم مَن تَعَلَّمَ القُرآنَ وَعَلَّمَهُ", "رواه البخاري", "صحيح"),
    HadithItem("كُلُّ ابنِ آدَمَ خَطَّاءٌ، وَخَيرُ الخَطَّائِينَ التَّوَّابُونَ", "رواه الترمذي", "حسن"),
    HadithItem("إِنَّ أَثقَلَ شَيءٍ فِي مِيزَانِ المُؤمِنِ يَومَ القِيَامَةِ خُلُقٌ حَسَنٌ، وَإِنَّ اللهَ لَيُبغِضُ الفَاحِشَ البَذِيءَ", "رواه الترمذي", "صحيح"),
    HadithItem("المُؤمِنُ القَوِيُّ خَيرٌ وَأَحَبُّ إِلَى اللهِ مِنَ المُؤمِنِ الضَّعِيفِ، وَفِي كُلٍّ خَيرٌ", "رواه مسلم", "صحيح"),
    HadithItem("صِلَةُ الرَّحِمِ تَزِيدُ فِي العُمُرِ وَالرِّزقِ", "رواه أحمد", "صحيح"),
    HadithItem("اللَّهُمَّ لا سَهلَ إِلَّا مَا جَعَلتَهُ سَهلًا، وَأَنتَ تَجعَلُ الحَزنَ سَهلًا إِذَا شِئتَ", "رواه ابن حبان", "صحيح"),
    HadithItem("ازهَد فِي الدُّنيَا يُحِبَّكَ اللهُ، وَازهَد فِيمَا عِندَ النَّاسِ يُحِبَّكَ النَّاسُ", "رواه ابن ماجه", "صحيح"),
    HadithItem("لا تَحقِرَنَّ مِنَ المَعرُوفِ شَيئًا وَلَو أَن تَلقَى أَخَاكَ بِوَجهٍ طَلقٍ", "رواه مسلم", "صحيح"),
    HadithItem("الكَلِمَةُ الطَّيِّبَةُ صَدَقَةٌ", "متفق عليه", "صحيح"),
    HadithItem("مَن أَحَبَّ لِقَاءَ اللهِ أَحَبَّ اللهُ لِقَاءَهُ", "متفق عليه", "صحيح"),
    HadithItem("إِنَّ اللهَ يُحِبُّ إِذَا عَمِلَ أَحَدُكُم عَمَلًا أَن يُتقِنَهُ", "رواه الطبراني", "صحيح"),
    HadithItem("مَن صَلَّى الفَجرَ فَهُوَ فِي ذِمَّةِ اللهِ", "رواه مسلم", "صحيح"),
    HadithItem("فَضلُ صَلاةِ الجَمَاعَةِ عَلَى صَلاةِ الفَذِّ بِسَبعٍ وَعِشرِينَ دَرَجَةً", "متفق عليه", "صحيح"),
    HadithItem("إِنَّ أَوَّلَ مَا يُحَاسَبُ بِهِ العَبدُ يَومَ القِيَامَةِ مِن عَمَلِهِ صَلاتُهُ", "رواه الترمذي", "صحيح"),
    HadithItem("حُجِبَت النَّارُ بِالشَّهَوَاتِ، وَحُجِبَتِ الجَنَّةُ بِالمَكَارِهِ", "متفق عليه", "صحيح"),
    HadithItem("مَن قَالَ سُبحَانَ اللهِ وَبِحَمدِهِ فِي يَومٍ مِائَةَ مَرَّةٍ، حُطَّت خَطَايَاهُ وَإِن كَانَت مِثلَ زَبَدِ البَحرِ", "متفق عليه", "صحيح"),
    HadithItem("مَن حَافَظَ عَلَى أَربَعِ رَكَعَاتٍ قَبلَ الظُّهرِ وَأَربَعٍ بَعدَهَا حَرَّمَهُ اللهُ عَلَى النَّارِ", "رواه الترمذي", "صحيح"),
    HadithItem("الصِّيَامُ جُنَّةٌ، وَالصَّدَقَةُ تُطفِئُ الخَطِيئَةَ كَمَا يُطفِئُ المَاءُ النَّارَ", "رواه الترمذي", "صحيح")
)

// 60 unique security tips — rotated daily, 6 shown per day
private val allSecurityTips: List<SecurityTip> = listOf(
    SecurityTip("كلمة المرور القوية", "استخدم 16 حرفاً على الأقل تجمع بين الأرقام والرموز والحروف الكبيرة والصغيرة. الطول هو الدرع الحقيقي، لا التعقيد وحده.", "حماية الحسابات"),
    SecurityTip("المصادقة الثنائية", "فعِّل التحقق بخطوتين على كل حساب حساس. حتى لو سُرقت كلمة مرورك، يبقى الحساب محمياً بالعامل الثاني.", "حماية الحسابات"),
    SecurityTip("مدير كلمات المرور", "Bitwarden وKeePass أدوات مفتوحة المصدر موثوقة لتخزين كلماتك. لا تعيد استخدام كلمة مرور في أكثر من موقع أبداً.", "حماية الحسابات"),
    SecurityTip("التحديث الفوري", "ثغرات الأمان تُكتشف يومياً وتُصلح بالتحديثات. تأخير تحديث نظامك لأسبوع واحد يعرّضك لمئات الثغرات.", "الجهاز والنظام"),
    SecurityTip("شبكات Wi-Fi العامة", "في المقهى أو المطار؟ لا تدخل حساباتك المصرفية. الشبكات العامة سهلة الاختراق، استخدم VPN أو بيانات الجوال.", "الشبكات"),
    SecurityTip("الروابط المشبوهة", "قبل أي نقرة، مرّر الماوس فوق الرابط. عنوان URL الحقيقي يظهر أسفل الشاشة. خطأ مطبعي واحد في الدومين يعني موقعاً مزيفاً.", "الاحتيال الإلكتروني"),
    SecurityTip("التصيد الاحتيالي", "الرسائل التي تُلحّ وتضغط ('حسابك سيُحذف خلال ساعة') هي علامة احتيال كلاسيكية. الشركات الحقيقية لا تهدد بهذه الطريقة.", "الاحتيال الإلكتروني"),
    SecurityTip("أذونات التطبيقات", "هل يطلب تطبيق المصباح الإذن للوصول إلى جهات اتصالك؟ هذا خطر. راجع أذونات تطبيقاتك كل 3 أشهر وأزل غير الضروري.", "التطبيقات"),
    SecurityTip("النسخ الاحتياطي", "قاعدة 3-2-1: ثلاث نسخ، على وسيطين مختلفين، واحدة خارج الموقع. هجوم الفدية لن يؤثر عليك إذا كانت بياناتك مؤمّنة.", "حماية البيانات"),
    SecurityTip("تشفير الجهاز", "فعِّل التشفير الكامل للقرص (BitLocker على ويندوز، FileVault على ماك). لو سُرق جهازك، بياناتك تبقى غير قابلة للقراءة.", "الجهاز والنظام"),
    SecurityTip("بريد إلكتروني مؤقت", "عند التسجيل في مواقع غير موثوقة، استخدم بريداً مؤقتاً من Guerrilla Mail أو Temp Mail. يحمي بريدك الحقيقي من السبام.", "الخصوصية"),
    SecurityTip("بيانات الاستعادة", "سؤال الأمان 'اسم حيوانك الأليف' هو أضعف نقطة في حسابك. استخدم إجابات عشوائية وخزِّنها في مدير كلمات مرورك.", "حماية الحسابات"),
    SecurityTip("شاشة القفل", "اضبط قفل الشاشة التلقائي على 30 ثانية. لحظة واحدة تتركها مكشوفة كافية لمن يريد الوصول إلى بياناتك.", "الجهاز والنظام"),
    SecurityTip("التحديق الرقمي", "Shoulder Surfing: شخص يقف خلفك في الطابور يمكنه رؤية كلمة مرورك. غطِّ يدك عند كتابة الأرقام السرية.", "الخصوصية"),
    SecurityTip("هاتف مسروق", "فعِّل خاصية 'امسح البيانات عن بُعد'. على iPhone: Find My، على Android: Find My Device. لحظة الضياع ستشكرك.", "الجهاز والنظام"),
    SecurityTip("شبكة VPN الموثوقة", "ليست كل VPN آمنة. تجنب VPN المجانية التي تبيع بياناتك. Proton VPN وMullvad خيارات موثوقة بسياسات صارمة.", "الشبكات"),
    SecurityTip("تجزئة الشبكة المنزلية", "خصِّص شبكة Wi-Fi منفصلة للأجهزة الذكية (تلفاز، ثلاجة). اختراق أي منها لن يصل إلى حاسوبك الشخصي.", "الشبكات"),
    SecurityTip("Metadata الصور", "صورك تحتوي بيانات مخفية: الموقع الجغرافي، موديل هاتفك، التاريخ. استخدم أدوات إزالة الـ EXIF قبل نشر الصور.", "الخصوصية"),
    SecurityTip("إعدادات الخصوصية", "راجع إعدادات خصوصية حساباتك الاجتماعية شهرياً. منصات التواصل تتغير سياساتها باستمرار وغالباً بشكل خفي.", "الخصوصية"),
    SecurityTip("الذكاء الاصطناعي والاحتيال", "أصوات وأوجه مزيفة بالذكاء الاصطناعي تُستخدم للابتزاز. إذا تلقيت مكالمة غريبة من 'شخص تعرفه'، تحقق بطريقة ثانية.", "الاحتيال الإلكتروني"),
    SecurityTip("طابعة الوثائق المهمة", "الطابعات تحتفظ بسجل لكل صفحة طُبعت. إذا بعت طابعتك، امسح ذاكرتها الداخلية أولاً.", "الجهاز والنظام"),
    SecurityTip("رمز QR", "رموز QR يمكن أن تعيد توجيهك لمواقع خبيثة. استخدم تطبيق QR يعرض الرابط قبل فتحه ويتيح لك قراءته.", "الاحتيال الإلكتروني"),
    SecurityTip("البلوتوث في الأماكن العامة", "بلوتوثك المفتوح يبثّ اسم جهازك باستمرار. أغلقه عندما لا تستخدمه. يقلل استهلاك البطارية أيضاً.", "الشبكات"),
    SecurityTip("حسابات الأطفال", "أنشئ حسابات محدودة الصلاحيات للأطفال. فضولهم الطبيعي قد يقودهم لتحميل برامج خطيرة بدون قصد.", "التطبيقات"),
    SecurityTip("تجميد الائتمان", "إذا تسرّبت بياناتك المصرفية، اطلب تجميد ائتمانك. يمنع المحتالين من فتح حسابات باسمك.", "حماية البيانات"),
    SecurityTip("DNS المشفر", "استخدم Cloudflare (1.1.1.1) أو NextDNS. يمنع مزود الإنترنت من رؤية المواقع التي تزورها ويحجب الروابط الخبيثة.", "الشبكات"),
    SecurityTip("متصفح آمن", "Firefox مع إضافة uBlock Origin يحجب 95% من الإعلانات المتتبعة. Brave متصفح بديل جيد بحجب تلقائي.", "التصفح"),
    SecurityTip("وضع التصفح المجهول", "التصفح المجهول لا يخفيك عن موقع الإنترنت ولا عن مزود الإنترنت. يخفي فقط سجلك المحلي على جهازك.", "التصفح"),
    SecurityTip("ملحقات المتصفح الخطرة", "إضافات المتصفح تملك وصولاً لكل ما تفعله. تحقق من الأذونات المطلوبة قبل تثبيت أي إضافة.", "التصفح"),
    SecurityTip("بريد بروتون", "ProtonMail وTutanota يوفران تشفيراً من طرف لطرف. اجعل أحدهما بريدك الاحتياطي للأمور الحساسة.", "الخصوصية"),
    SecurityTip("بروتوكول HTTPS", "تحقق من وجود القفل الأخضر HTTPS قبل إدخال أي بيانات شخصية. HTTP بدون S يعني أن بياناتك تُرسل مكشوفة.", "التصفح"),
    SecurityTip("التطبيقات من مصادر غير رسمية", "تثبيت APK من خارج المتجر الرسمي يعرّضك لبرامج تجسس خبيثة. حتى التطبيقات الشهيرة يمكن تعديلها.", "التطبيقات"),
    SecurityTip("بيانات الاتصال المهنية", "لا تشارك رقم هاتفك الشخصي في سياقات العمل العامة. استخدم رقماً ثانياً أو خدمة Google Voice.", "الخصوصية"),
    SecurityTip("هجوم القاموس", "Dictionary Attack: برامج تجرب ملايين الكلمات في ثوانٍ. لا تستخدم أي كلمة موجودة في القاموس ككلمة مرور.", "حماية الحسابات"),
    SecurityTip("مراقبة نشاط الحساب", "فعِّل إشعارات تسجيل الدخول على كل حساتك. أي دخول من جهاز جديد يجب أن تعرفه فوراً.", "حماية الحسابات"),
    SecurityTip("الشاشات العامة", "لا تترك شاشتك بدون قفل في الأماكن العامة. حتى الاطلاع لثوانٍ كافٍ لالتقاط معلومات حساسة.", "الخصوصية"),
    SecurityTip("اختراق الصوت", "مساعدات صوتية مثل Alexa وSiri تستمع لكلمات التنشيط. ضعها بعيداً عن المحادثات الحساسة.", "الخصوصية"),
    SecurityTip("بيانات التطبيق", "عند حذف تطبيق، ابحث في إعداداته أولاً عن خيار 'حذف البيانات' أو 'حذف الحساب'. الحذف وحده لا يمسح بياناتك من خوادمهم.", "التطبيقات"),
    SecurityTip("الشبكة الخاصة الافتراضية", "VPN في العمل يحمي اتصالك. لكن مزود الـ VPN نفسه يرى كل حركة مرورك. اختر مزوداً لا يحتفظ بسجلات.", "الشبكات"),
    SecurityTip("الخداع الهاتفي", "Vishing: مكالمات مزيفة تتظاهر بأنها من البنك أو الحكومة. البنك الحقيقي لن يطلب منك رمز OTP أبداً.", "الاحتيال الإلكتروني"),
    SecurityTip("أمان السحابة", "استخدم تشفيراً من طرفك قبل رفع ملفات حساسة للسحابة. Cryptomator أداة مجانية للتشفير المحلي.", "حماية البيانات"),
    SecurityTip("الكاميرا ولصقة التغطية", "غطِّ كاميرا اللابتوب عند عدم الاستخدام. برامج التجسس المتقدمة تستطيع تشغيلها دون إضاءة المؤشر.", "الجهاز والنظام"),
    SecurityTip("جهاز USB المشبوه", "USB Drop Attack: لا تحشر USB وجدته في جهازك. يمكنه تنفيذ كود خبيث في ثوانٍ حتى لو بدا فارغاً.", "الجهاز والنظام"),
    SecurityTip("بيانات WHOIS", "إذا تملك دومين، فعِّل خصوصية WHOIS. بدونها، اسمك وعنوانك وهاتفك متاحان للجميع.", "الخصوصية"),
    SecurityTip("مراجعة الجلسات النشطة", "كل شهر، راجع الأجهزة المتصلة بحساباتك (Google، Facebook، Instagram) واحذف الأجهزة القديمة أو الغريبة.", "حماية الحسابات"),
    SecurityTip("التشفير من طرف لطرف", "Signal أفضل تطبيق مراسلة من حيث الخصوصية. حتى Signal نفسها لا تستطيع قراءة رسائلك.", "الخصوصية"),
    SecurityTip("أمان الراوتر المنزلي", "غيِّر اسم وكلمة مرور الراوتر الافتراضيين فوراً. كلمات المرور الافتراضية منشورة على الإنترنت للجميع.", "الشبكات"),
    SecurityTip("برنامج مكافحة الفيروسات", "Windows Defender الافتراضي كافٍ إذا حافظت على تحديث نظامك. لا تثبت برامج مكافحة فيروسات من مصادر مجهولة.", "الجهاز والنظام"),
    SecurityTip("التقمص الرقمي", "Doxxing: جمع معلوماتك من صور ومنشورات وسائل التواصل. قلِّل ما تنشره عن عنوانك ومكان عملك.", "الخصوصية"),
    SecurityTip("التوقيع الرقمي", "قبل تنفيذ برنامج مهم، تحقق من توقيعه الرقمي. في ويندوز: انقر بزر الماوس الأيمن > Properties > Digital Signatures.", "الجهاز والنظام"),
    SecurityTip("صفحات تسجيل الدخول", "تحقق دائماً من عنوان URL كاملاً عند تسجيل الدخول. الفرق بين google.com وgoog1e.com قد يكلفك حسابك.", "التصفح"),
    SecurityTip("بيانات الشبكة الخلوية", "حتى بيانات الجوال ليست محصّنة. مع جهاز IMSI Catcher، يمكن لجهات التجسس اعتراضها. VPN يضيف طبقة حماية.", "الشبكات"),
    SecurityTip("كلمات المرور المُعاد استخدامها", "في هجوم Credential Stuffing، يجرب المخترقون كلمات مرور مسرّبة على آلاف المواقع. كلمة مرور فريدة لكل موقع.", "حماية الحسابات"),
    SecurityTip("سجلات التطبيقات", "بعض التطبيقات تخزّن مفاتيح API وبيانات حساسة في سجلات النظام. احذف Cache التطبيقات الحساسة بانتظام.", "التطبيقات"),
    SecurityTip("الصور على الإنترنت", "Google Reverse Image Search يستطيع تتبع كل مكان نُشرت فيه صورتك. كن واعياً بما تنشر من صور شخصية.", "الخصوصية"),
    SecurityTip("هجمات Man in the Middle", "في الشبكات المشتركة، يمكن لشخص وضع نفسه بينك وبين الموقع. HTTPS يحمي، لكن VPN يضيف طبقة إضافية.", "الشبكات"),
    SecurityTip("التطبيقات المصرفية", "استخدم التطبيق الرسمي فقط وتحقق من صدوره من الجهة الرسمية. هناك تطبيقات مصرفية مزيفة في المتاجر.", "حماية الحسابات"),
    SecurityTip("الطابعة الشبكية", "الطابعات الشبكية لها IP خاص بها وقد تكون ثغرة. عزِّلها في شبكة منفصلة إذا لم تكن تستخدمها مع الحاسوب.", "الشبكات"),
    SecurityTip("حسابات وسائل التواصل", "لا تسجِّل الدخول لمواقع عبر 'تسجيل دخول بـ Facebook/Google' إلا للمواقع الموثوقة. كل ربط يعطي الموقع صلاحيات.", "حماية الحسابات"),
    SecurityTip("الوعي الأمني اليومي", "الأمن الرقمي عادة وليس حدثاً. كما تقفل بيتك يومياً، افحص أمانك الرقمي يومياً. الوعي هو درعك الأول.", "عام")
)

private fun getTodaysTips(): List<SecurityTip> {
    val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
    val startIndex = (dayOfYear * 6) % allSecurityTips.size
    return (0 until 6).map { offset ->
        allSecurityTips[(startIndex + offset) % allSecurityTips.size]
    }
}

private val duaCategories = listOf("الكل", "الصباح", "المساء", "قبل النوم", "الطعام", "السفر", "الدخول والخروج", "الكرب والضيق", "الاستخارة", "الرزق", "الشكر", "الوالدين", "عام")

// ─── Main Screen ─────────────────────────────────────────────────────────────

@Composable
fun LibraryScreen(
    viewModel: DashboardViewModel,
    modifier: Modifier = Modifier
) {
    val isAr by viewModel.isArabic.collectAsState()
    val haptic = LocalHapticFeedback.current

    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color(0xFF030812), VoidBlack)))
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // ── Header ──────────────────────────────────────────────────────
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
                Text(
                    text = "المكتبة الرقمية",
                    color = AmberZen,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "أدعية  •  أحاديث  •  أمن رقمي",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 2.dp)
                )
            }

            // ── Tab Row ──────────────────────────────────────────────────────
            val tabs = listOf("الأدعية", "الأحاديث", "نصائح أمنية")
            val tabColors = listOf(AmberZen, CyberCyan, Color(0xFF9C27B0))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tabs.forEachIndexed { idx, label ->
                    val isSelected = idx == selectedTab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .border(
                                1.dp,
                                if (isSelected) tabColors[idx] else Color.White.copy(alpha = 0.1f),
                                RoundedCornerShape(12.dp)
                            )
                            .background(
                                if (isSelected) tabColors[idx].copy(alpha = 0.15f) else Color.White.copy(alpha = 0.03f),
                                RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                                selectedTab = idx
                                searchQuery = ""
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) tabColors[idx] else Color.White.copy(alpha = 0.5f),
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            // ── Search (Duas & Hadiths only) ─────────────────────────────────
            if (selectedTab != 2) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = {
                        Text(
                            "بحث...",
                            color = Color.White.copy(alpha = 0.3f),
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = CyberCyan.copy(alpha = 0.6f))
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = tabColors[selectedTab].copy(alpha = 0.6f),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = tabColors[selectedTab]
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            // ── Content ──────────────────────────────────────────────────────
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(150)) },
                label = "libTab"
            ) { tab ->
                when (tab) {
                    0 -> DuasTab(searchQuery = searchQuery, accentColor = tabColors[0])
                    1 -> HadithsTab(searchQuery = searchQuery, accentColor = tabColors[1])
                    2 -> SecurityTipsTab(accentColor = tabColors[2])
                }
            }
        }
    }
}

// ─── Duas Tab ────────────────────────────────────────────────────────────────

@Composable
private fun DuasTab(searchQuery: String, accentColor: Color) {
    val haptic = LocalHapticFeedback.current
    var selectedCategory by remember { mutableStateOf("الكل") }
    val expandedIndex = remember { mutableStateOf<Int?>( null) }

    val filtered = remember(searchQuery, selectedCategory) {
        allDuas.filter { dua ->
            val matchesSearch = searchQuery.isBlank() ||
                dua.titleAr.contains(searchQuery) ||
                dua.textAr.contains(searchQuery) ||
                dua.category.contains(searchQuery)
            val matchesCat = selectedCategory == "الكل" || dua.category == selectedCategory
            matchesSearch && matchesCat
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        // Category filter
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(duaCategories.size) { idx ->
                    val cat = duaCategories[idx]
                    val isSelected = cat == selectedCategory
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .border(1.dp, if (isSelected) accentColor else Color.White.copy(alpha = 0.15f), RoundedCornerShape(50))
                            .background(if (isSelected) accentColor.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.04f))
                            .clickable { selectedCategory = cat }
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(cat, color = if (isSelected) accentColor else Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                    }
                }
            }
        }

        if (filtered.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("لا توجد نتائج", color = Color.White.copy(alpha = 0.3f), fontSize = 14.sp)
                }
            }
        }

        itemsIndexed(filtered) { idx, dua ->
            val isExpanded = expandedIndex.value == idx
            DuaCard(
                dua = dua,
                isExpanded = isExpanded,
                accentColor = accentColor,
                onToggle = {
                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                    expandedIndex.value = if (isExpanded) null else idx
                }
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun DuaCard(dua: DuaItem, isExpanded: Boolean, accentColor: Color, onToggle: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(0.8.dp, if (isExpanded) accentColor.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
            .background(
                if (isExpanded) accentColor.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.02f),
                RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onToggle)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(accentColor.copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🤲", fontSize = 18.sp)
                }
                Column {
                    Text(dua.titleAr, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Text(dua.category, color = accentColor.copy(alpha = 0.7f), fontSize = 11.sp)
                }
            }
            Text(if (isExpanded) "▲" else "▼", color = accentColor.copy(alpha = 0.6f), fontSize = 12.sp)
        }

        AnimatedVisibility(visible = isExpanded, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
            Column(modifier = Modifier.padding(start = 14.dp, end = 14.dp, bottom = 14.dp)) {
                HorizontalDivider(color = accentColor.copy(alpha = 0.15f))
                Spacer(Modifier.height(12.dp))
                Text(
                    text = dua.textAr,
                    color = Color.White,
                    fontSize = 16.sp,
                    lineHeight = 28.sp,
                    textAlign = TextAlign.Right,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .border(0.7.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(dua.tag, color = accentColor, fontSize = 11.sp)
                }
            }
        }
    }
}

// ─── Hadiths Tab ─────────────────────────────────────────────────────────────

@Composable
private fun HadithsTab(searchQuery: String, accentColor: Color) {
    val haptic = LocalHapticFeedback.current
    val expandedIndex = remember { mutableStateOf<Int?>(null) }

    val filtered = remember(searchQuery) {
        if (searchQuery.isBlank()) allHadiths
        else allHadiths.filter { it.textAr.contains(searchQuery) || it.source.contains(searchQuery) }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp), horizontalArrangement = Arrangement.End) {
                Text("${allHadiths.size} حديثاً شريفاً", color = accentColor.copy(alpha = 0.6f), fontSize = 11.sp)
            }
        }

        itemsIndexed(filtered) { idx, hadith ->
            val isExpanded = expandedIndex.value == idx
            HadithCard(
                hadith = hadith,
                index = allHadiths.indexOf(hadith) + 1,
                isExpanded = isExpanded,
                accentColor = accentColor,
                onToggle = {
                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                    expandedIndex.value = if (isExpanded) null else idx
                }
            )
        }
    }
}

@Composable
private fun HadithCard(hadith: HadithItem, index: Int, isExpanded: Boolean, accentColor: Color, onToggle: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(0.8.dp, if (isExpanded) accentColor.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
            .background(if (isExpanded) accentColor.copy(alpha = 0.05f) else Color.White.copy(alpha = 0.02f), RoundedCornerShape(14.dp))
            .clickable(onClick = onToggle)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier.size(38.dp).background(accentColor.copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("📖", fontSize = 18.sp)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("حديث شريف", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Text(hadith.source, color = accentColor.copy(alpha = 0.7f), fontSize = 11.sp)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .border(0.7.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(hadith.grade, color = accentColor, fontSize = 10.sp)
                }
                Text(if (isExpanded) "▲" else "▼", color = accentColor.copy(alpha = 0.6f), fontSize = 12.sp)
            }
        }

        if (!isExpanded) {
            Text(
                text = hadith.textAr,
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 13.sp,
                lineHeight = 20.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth().padding(start = 14.dp, end = 14.dp, bottom = 12.dp)
            )
        }

        AnimatedVisibility(visible = isExpanded, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
            Column(modifier = Modifier.padding(start = 14.dp, end = 14.dp, bottom = 14.dp)) {
                HorizontalDivider(color = accentColor.copy(alpha = 0.15f))
                Spacer(Modifier.height(12.dp))
                Text(
                    text = hadith.textAr,
                    color = Color.White,
                    fontSize = 16.sp,
                    lineHeight = 28.sp,
                    textAlign = TextAlign.Right,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                Text(hadith.source, color = accentColor.copy(alpha = 0.7f), fontSize = 12.sp, textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

// ─── Security Tips Tab ───────────────────────────────────────────────────────

@Composable
private fun SecurityTipsTab(accentColor: Color) {
    val haptic = LocalHapticFeedback.current
    val expandedIndex = remember { mutableStateOf<Int?>(null) }
    val todaysTips = remember { getTodaysTips() }

    val dayOfYear = remember { Calendar.getInstance().get(Calendar.DAY_OF_YEAR) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            // Daily freshness badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .border(0.7.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(50))
                        .background(accentColor.copy(alpha = 0.08f), RoundedCornerShape(50))
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    Text("🛡️ نصائح اليوم — تتجدد يومياً", color = accentColor, fontSize = 11.sp)
                }
                Text("${todaysTips.size} نصائح", color = Color.White.copy(alpha = 0.3f), fontSize = 11.sp)
            }
        }

        itemsIndexed(todaysTips) { idx, tip ->
            val isExpanded = expandedIndex.value == idx
            SecurityTipCard(
                tip = tip,
                index = idx + 1,
                isExpanded = isExpanded,
                accentColor = accentColor,
                onToggle = {
                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                    expandedIndex.value = if (isExpanded) null else idx
                }
            )
        }

        item {
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "تعود غداً لنصائح جديدة مختلفة كلياً ✨",
                    color = Color.White.copy(alpha = 0.3f),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun SecurityTipCard(tip: SecurityTip, index: Int, isExpanded: Boolean, accentColor: Color, onToggle: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(0.8.dp, if (isExpanded) accentColor.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
            .background(if (isExpanded) accentColor.copy(alpha = 0.07f) else Color.White.copy(alpha = 0.02f), RoundedCornerShape(14.dp))
            .clickable(onClick = onToggle)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier.size(38.dp).background(accentColor.copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🔐", fontSize = 18.sp)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(tip.titleAr, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(2.dp))
                    if (!isExpanded) {
                        Text(
                            tip.bodyAr,
                            color = Color.White.copy(alpha = 0.55f),
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .border(0.7.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(tip.tag, color = accentColor.copy(alpha = 0.8f), fontSize = 10.sp)
                    }
                }
            }
            Text(if (isExpanded) "▲" else "▼", color = accentColor.copy(alpha = 0.6f), fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
        }

        AnimatedVisibility(visible = isExpanded, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
            Column(modifier = Modifier.padding(start = 14.dp, end = 14.dp, bottom = 14.dp)) {
                HorizontalDivider(color = accentColor.copy(alpha = 0.15f))
                Spacer(Modifier.height(10.dp))
                Text(
                    tip.bodyAr,
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 14.sp,
                    lineHeight = 24.sp,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

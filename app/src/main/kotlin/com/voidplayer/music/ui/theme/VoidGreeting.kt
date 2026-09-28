package com.voidplayer.music.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.voidplayer.music.data.FAMOUS_ARTISTS
import java.time.LocalTime
import kotlin.random.Random

data class VoidGreeting(
    val rawText: String,
    val firstPart: String,
    val secondPart: String?,
    val isRainbow: Boolean = false,
    val isRare: Boolean = false
)

private object SessionJoke {
    val pool = listOf(
        "Spotify? Never heard of it",
        "Why do yall keep saying this small company name Spotify?",
        "Premium? bro you are getting premium pro max for FREE here",
        "Music is enjoyment and it should be free of interruptions",
        "Ads? wait here. ✨poof✨",
        "AFK dont kill",
        "Puffing up the snacks wait",
        "Adresh wuz here",
        "btw you are at earth rn",
        "CHAT IMMA ABOUT TO HIT 60 MILLION",
        "Never gonna give you up-",
        "yo",
        "uwu",
        "hold on my mouse battery died",
        "hold on developer is pondering",
        "void in chinese",
        "yo (username/acc name)",
        "w (username)",
        "hmm dev is sleeping",
        "we got void before GTA VI",
        "this msg is ( ) % rare",
        "boom boom paw",
        "btw lions are just a big fluffy cat",
        "this app was made with love and 8gb of pure terror",
        "this app was made in a pc worse than your phone",
        "another one",
        "HEYY MAKARENA",
        "yo broski wanna hear smth?",
        ". _ _ . / . _ . . / . . .",
        ". . . / _ _ _ / . . .",
        "10010100",
        "you are seeing this because i made you see it",
        "did you know that you did know you know did?",
        "ya y ay ay ayayayayayayayay",
        "BOOOOOM",
        "shit *user* is here lemme hide my gf rq",
        "67",
        "void chan is going on a vacation because she is pregnant",
        "hey *user* meet void chan",
        "yooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo",
        "i had to learn Kotlin just to keep making VOID JUST BECAUSE CLAUDE DIED",
        "THE DUDE BEHIND MY SUCCESS IS CHAT GI BI DI",
        "the chat has turned blue",
        "go watch Stranger Things",
        "btw idgaf about Spotify",
        "hmm id looking to the girl i just likes",
        "yoink",
        "imma smash my keyboard until it looks like code",
        "dua lipa cooking",
        "glitter bomb",
        "btw Mark Rober is a YouTuber",
        "did you know? Bruno Mars didnt come from Mars?",
        "yoyioyk",
        "mawn pawn tawn gawn vawn",
        "SPOTIFY IS COOKED",
        "JOYCE DRIVVEEEEEEEEEEEEE!",
        "spacebar",
        "vineboom",
        "lawn mower is a machine",
        "NEEEEEEEEEEEOWWW",
        "imma swoop some stuff",
        "HEADSHOTS 223 FLAWLESS VICTORY",
        "you know what else is pink? ofc its a strawberry shake YA PERVS",
        "DAI DAI IKUO",
        "sooooooooooooooooooooooooo this was supposed to be smth absurd but i forgot",
        "OI II A II OI I A I",
        "BTW BTW IS BY THE WAY IN BTW",
        "woof",
        "lets see lets see",
        "only ogs know this",
        "soooooooooooooooooo i am VOID",
        "uhh so what is 2=2?",
        "im a panCAKE",
        "AAOUUUUUUUUUUUUUUU",
        "did you know that bluetooth isnt called bluetooth because it is bluetooth?",
        "this is a shower thought",
        "are sweet potatoes sweet because they are named sweet or they are sweet because they have small potato stuff in it?",
        "XD",
        ":D",
        ":)",
        ":]",
        ": \\",
        ";P",
        ". ω .",
        "wowie zowie",
        "So tell me abt 2020- bro",
        "911 can be a car, a incident, or a po po number choose what you like",
        "AND HIS NAME IS JOHN CENAAA",
        "google is mad",
        "yoi",
        "hehe boii",
        "hiiiiiiiiiiiiiiiiiiiiiiiiiiiiii",
        "iPhone 20 pro max",
        "one sec",
        "did somebody say banana?",
        "WW3 INCOMING",
        "my balls are fragile sir",
        "doomscroolllllllllll",
        "SONNY AND CHER ARE BEST FRIENDS",
        "SURPRISE HORSEY!",
        "TONY STARK WAS ABLE TO BUILD THIS IN A CAVE WITH A BOX OF SCRAPS",
        "THE GOLDEN DANDELION",
        "DRIVING IN MY CAR RIGHT AFTER A BEER-"
    )

    fun getRandomIndex(): Int = (0 until pool.size).random()
}

@Composable
fun rememberVoidGreeting(
    currentTitle: String?,
    currentArtist: String?,
    accountName: String?,
    currentRoute: String? = null
): VoidGreeting {
    val name = accountName ?: "Guest"

    var activeJokeIndex by remember { mutableStateOf(SessionJoke.getRandomIndex()) }

    LaunchedEffect(currentRoute) {
        if (currentRoute != null && Random.nextBoolean()) {
            activeJokeIndex = SessionJoke.getRandomIndex()
        }
    }

    var reactionText by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(currentTitle, currentArtist) {
        if (currentTitle == null) {
            reactionText = null
            return@LaunchedEffect
        }
        val hay = "${currentArtist.orEmpty()} ${currentTitle}".lowercase()

        if (hay.contains("baby shark")) {
            reactionText = "BRO WHO LET YOU IN HERE LEMME TURN ON BABY MODE FOR YA"
            return@LaunchedEffect
        }

        val artist = FAMOUS_ARTISTS.find { a -> a.keywords.any { hay.contains(it) } }
        if (artist == null) {
            reactionText = null
            return@LaunchedEffect
        }
        val songMatch = artist.songs.find { s -> s.keywords.any { hay.contains(it) } }
        reactionText = songMatch?.reactions?.random() ?: artist.reactions.random()
    }

    val rawText = reactionText ?: SessionJoke.pool[activeJokeIndex]
        .replace("(username/acc name)", name)
        .replace("(username)", name)
        .replace("*user*", name)
        .replace("( ) % rare", "1.5% rare")

    val parts = rawText.split(" || ")
    val firstPart = parts[0]
    val secondPart = if (parts.size > 1) parts[1] else null

    return VoidGreeting(
        rawText = rawText,
        firstPart = firstPart,
        secondPart = secondPart,
        isRainbow = activeJokeIndex == 9,
        isRare = activeJokeIndex % 7 == 0
    )
}

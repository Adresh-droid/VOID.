package com.voidplayer.music.data

/**
 * Curated list of widely-known artists — when a track by one of these plays,
 * Home shows a little celebratory toast in the greeting text.
 * Matching is substring-based against the track's artist field (and falls
 * back to title, for singles tagged without a separate artist).
 *
 * Each artist carries general reactions (shown when any track by that artist
 * plays) plus their most famous songs, each with song-specific reactions.
 * Song reactions fire when the track title matches one of the song keywords
 * and take priority over the general artist reaction.
 *
 * Ported verbatim from the VOID web app (FAMOUS_ARTISTS in the main HTML).
 */
data class FamousSong(
    val title: String,
    val keywords: List<String>,
    val reactions: List<String>
)

data class FamousArtist(
    val name: String,
    val keywords: List<String>,
    val reactions: List<String>,
    val songs: List<FamousSong>
)

val FAMOUS_ARTISTS: List<FamousArtist> = listOf(
    FamousArtist(
        name = "Taylor Swift",
        keywords = listOf("taylor swift"),
        reactions = listOf(
            "Swiftie mode: on", "that's Taylor.", "vault track energy", "which era is this", "the eras never end"
        ),
        songs = listOf(
            FamousSong("Shake It Off", listOf("shake it off"), listOf("players gonna play", "haters gonna hate", "shake shake shake", "this is pure serotonin", "the bridge goes off")),
            FamousSong("Blank Space", listOf("blank space"), listOf("got a long list of ex-lovers", "nightmare dressed like a daydream", "that bridge, every time", "iconic villain era Taylor", "this is peak 1989")),
            FamousSong("Anti-Hero", listOf("anti-hero"), listOf("it's me, hi", "I'm the problem, it's me", "Midnights just different", "that chorus never gets old", "self-aware pop perfection")),
            FamousSong("Love Story", listOf("love story"), listOf("Romeo save me", "Fearless era forever", "that bridge hits different grown up", "this song defined a generation", "you'll say you'll never leave")),
            FamousSong("All Too Well", listOf("all too well"), listOf("ten minute version, obviously", "scarf on the door", "all too well is cinema", "vault track that became the anthem", "this one requires a moment")),
            FamousSong("Cruel Summer", listOf("cruel summer"), listOf("screaming the bridge in the car", "Lover era sleeper hit", "that key change is everything", "this should've been the lead single", "synthpop Taylor is undefeated")),
            FamousSong("Wildest Dreams", listOf("wildest dreams"), listOf("1989 deep cut hitting hard", "say you'll remember me", "that production is timeless", "cinematic Taylor at her peak", "this one feels like a movie")),
            FamousSong("cardigan", listOf("cardigan"), listOf("folklore era in full effect", "that's cottage-core Taylor", "vintage tee and cardigan energy", "Aaron Dessner production is a vibe", "this one's quiet and devastating")),
            FamousSong("Lavender Haze", listOf("lavender haze"), listOf("Midnights opener, still goes hard", "meet me at midnight", "that synth intro is perfect", "she said what she said", "lavender haze living")),
            FamousSong("Style", listOf("style"), listOf("James Dean daydream look in my eye", "1989 is a perfect album", "this song is effortlessly cool", "that production never ages", "midnight, you come and pick me up"))
        )
    ),
    FamousArtist(
        name = "Ariana Grande",
        keywords = listOf("ariana grande"),
        reactions = listOf(
            "that whistle note though", "that's Ari", "high notes incoming", "ponytail-and-all energy", "thank u, next track"
        ),
        songs = listOf(
            FamousSong("thank u, next", listOf("thank u, next", "thank you next"), listOf("she said what she said", "that bridge is iconic", "thank u, next is an anthem", "grown up energy", "this era changed everything")),
            FamousSong("7 rings", listOf("7 rings", "seven rings"), listOf("I see it I like it", "bought it", "trap Ariana is elite", "this was a moment in time", "the flex is real")),
            FamousSong("God is a woman", listOf("god is a woman"), listOf("vocals from another planet", "that bridge is spiritual", "feminist pop anthem", "she delivered on every note", "production is immaculate")),
            FamousSong("positions", listOf("positions"), listOf("heaven sent positions", "silky smooth production", "that title track hits", "R&B Ari is unmatched", "looping this all night")),
            FamousSong("Into You", listOf("into you"), listOf("Dangerous Woman era deep cut", "that drop is massive", "this one still goes off", "I'm so into you", "danceable and cinematic")),
            FamousSong("No Tears Left to Cry", listOf("no tears left to cry"), listOf("her comeback single was perfect", "production is cinematic", "that key change is everything", "Sweetener era begins here", "no tears, just vibes")),
            FamousSong("Problem", listOf("problem"), listOf("one less problem without ya", "that brass hit is iconic", "Big Sean verse incoming", "this was peak 2014 Ari", "the hook never gets old")),
            FamousSong("Break Free", listOf("break free"), listOf("Zedd produced this perfectly", "that drop still hits", "EDM Ariana era", "electropop banger incoming", "this is a full body experience")),
            FamousSong("Side to Side", listOf("side to side"), listOf("Nicki and Ari together is unstoppable", "that gym energy", "Dangerous Woman collab delivered", "side to side forever", "the hook is stupidly catchy")),
            FamousSong("Rain on Me", listOf("rain on me"), listOf("Gaga and Ari is iconic", "I didn't ask for the rain", "this collab was unexpected and perfect", "dance pop royalty together", "hands up to the sky"))
        )
    ),
    FamousArtist(
        name = "Billie Eilish",
        keywords = listOf("billie eilish"),
        reactions = listOf(
            "whisper vocals detected", "that's Billie.", "bedroom-pop hour", "moody and it's working", "lost in this one"
        ),
        songs = listOf(
            FamousSong("bad guy", listOf("bad guy"), listOf("duh", "that bass is iconic", "minimalist and massive at once", "this made her a global star", "the duh heard round the world")),
            FamousSong("Happier Than Ever", listOf("happier than ever"), listOf("that second half hits like a freight train", "she built up to scream and earned it", "the quiet-to-loud dynamic is genius", "Happier Than Ever the album is a masterpiece", "that guitar solo is underrated")),
            FamousSong("Ocean Eyes", listOf("ocean eyes"), listOf("where it all started", "she was a teenager when she wrote this", "still gorgeous years later", "that chorus floats", "ethereal debut energy")),
            FamousSong("when the party's over", listOf("when the party's over", "when the partys over"), listOf("don't you know I'm no good for you", "this one hurts quietly", "the restraint in her voice is everything", "crying in a good way", "emotional devastation, softly delivered")),
            FamousSong("lovely", listOf("lovely"), listOf("Khalid and Billie is a perfect pair", "that harmony is haunting", "13 Reasons Why changed the soundtrack game", "this one lingers after it ends", "beautiful and aching")),
            FamousSong("Therefore I Am", listOf("therefore i am"), listOf("I'm not your friend or anything", "cocky and it works", "that stripped-back production is bold", "this slaps with minimal effort", "get my bag and get my name")),
            FamousSong("Bury a Friend", listOf("bury a friend"), listOf("WHEN WE ALL FALL ASLEEP era", "that industrial production is wild", "creepy and addictive", "the horror movie of pop songs", "step on the glass")),
            FamousSong("Bellyache", listOf("bellyache"), listOf("debut era Billie is underrated", "I'm the bad guy", "that carefree dark energy", "this song has a wink in it", "early Billie absolutely delivered")),
            FamousSong("idontwannabeyouanymore", listOf("idontwannabeyouanymore"), listOf("this title is a whole mood", "heartbreaking piano ballad", "so young and already this deep", "the vulnerability here is real", "this hits at 2am specifically")),
            FamousSong("What Was I Made For", listOf("what was i made for"), listOf("Barbie movie gave us this gem", "I used to float", "this won the Oscar for a reason", "crying in the cinema again", "quiet devastation, peak Billie"))
        )
    ),
    FamousArtist(
        name = "Drake",
        keywords = listOf("drake"),
        reactions = listOf(
            "six god hours", "that's Drake.", "Toronto in the room", "certified mood", "started from the playlist"
        ),
        songs = listOf(
            FamousSong("God's Plan", listOf("god's plan", "gods plan"), listOf("they wishin' and wishin'", "music video gave it all away", "this was inescapable in 2018", "the hook is massive", "OVO sound on full display")),
            FamousSong("Hotline Bling", listOf("hotline bling"), listOf("you used to call me on my cell phone", "those dance moves live rent free", "this defined a cultural moment", "sample flip genius", "ever since you left the city")),
            FamousSong("One Dance", listOf("one dance"), listOf("Afrobeats Drake is underrated", "that groove is effortless", "Baby I need one dance", "this was everywhere in 2016", "WizKid collab was perfect")),
            FamousSong("Started From the Bottom", listOf("started from the bottom"), listOf("now we here", "Toronto anthem", "this was the comeback song", "the pride in this track is real", "now the whole team here")),
            FamousSong("Passionfruit", listOf("passionfruit"), listOf("More Life Drake is his best", "dancehall Drake is a vibe", "tropical and smooth", "this is the late-night version of Drake", "that chorus floats")),
            FamousSong("Forever", listOf("forever"), listOf("Eminem, Kanye, Wayne, and Drake on one track", "this was a moment", "the summit of 2009 rap", "whoever thought of this collab is a genius", "Forever in the game")),
            FamousSong("Take Care", listOf("take care"), listOf("Rihanna x Drake delivered", "I'll take care of you", "this is peak vulnerable Drake", "that sample is beautiful", "this album changed R&B")),
            FamousSong("HYFR", listOf("hyfr", "hell yeah fucking right"), listOf("Lil Wayne x Drake era", "hell yeah f***ing right", "the energy is through the roof", "Take Care deep cut banger", "this one goes hard immediately")),
            FamousSong("Nonstop", listOf("nonstop"), listOf("this is a Scorpion standout", "the flow is relentless", "nonstop until I hit a million", "that beat hits immediately", "Drake in beast mode")),
            FamousSong("Rich Flex", listOf("rich flex"), listOf("21 Savage and Drake together", "can you do a rich flex", "Her Loss was unexpected", "this was everywhere", "Atlanta meets Toronto energy"))
        )
    ),
    FamousArtist(
        name = "The Weeknd",
        keywords = listOf("weeknd"),
        reactions = listOf(
            "XO hours", "that's The Weeknd.", "falsetto and streetlights", "dark and it's working", "after-hours energy"
        ),
        songs = listOf(
            FamousSong("Blinding Lights", listOf("blinding lights"), listOf("80s synth and modern production perfection", "most streamed song of all time for a reason", "that chorus never gets old", "I've been on my own for long enough", "synthwave Abel is peak Abel")),
            FamousSong("Starboy", listOf("starboy"), listOf("Daft Punk produced this perfectly", "look what you've done", "that bass line hits immediately", "I'm a Starboy", "Daft Punk x Weeknd was everything")),
            FamousSong("Can't Feel My Face", listOf("can't feel my face", "cant feel my face"), listOf("Michael Jackson energy in a pop banger", "And I know she'll be the death of me", "crossover Weeknd moment", "that chorus is unstoppable", "XO x pop perfection")),
            FamousSong("The Hills", listOf("the hills"), listOf("I only call you when it's half past five", "dark atmospheric production", "this announced a new era", "Beauty Behind the Madness peak", "the instrumental is menacing")),
            FamousSong("Save Your Tears", listOf("save your tears"), listOf("After Hours standout", "Ariana remix elevated it", "I don't need you but I want you", "this melody is stuck in my head forever", "sad disco at its finest")),
            FamousSong("Earned It", listOf("earned it"), listOf("Fifty Shades of Grey gave us this", "piano-driven Weeknd is divine", "Earned It Fifty Shades of Grey", "that orchestral production is gorgeous", "his most elegant song")),
            FamousSong("Die For You", listOf("die for you"), listOf("Starboy deep cut that became an anthem", "I'd die for you", "this resurfaced and everyone remembered it", "delayed classic energy", "the emotional peak of Starboy")),
            FamousSong("Often", listOf("often"), listOf("Beauty Behind the Madness intro", "you think about me often baby", "dark R&B Weeknd early days", "the production is hypnotic", "this is where it all clicked")),
            FamousSong("Out of Time", listOf("out of time"), listOf("Dawn FM era is underrated", "this samples Takahashi", "I apologize for what I did", "80s Japanese pop sample is genius", "nostalgic and melancholy")),
            FamousSong("Call Out My Name", listOf("call out my name"), listOf("My Dear Melancholy is devastating", "I almost cut a piece of myself for your life", "this about Bella Hadid or not", "short album, massive impact", "raw heartbreak perfectly produced"))
        )
    ),
    FamousArtist(
        name = "Beyoncé",
        keywords = listOf("beyonce", "beyoncé"),
        reactions = listOf(
            "the queen has entered", "that's Beyoncé.", "Houston in the room", "flawless, as expected", "the Beyhive knows"
        ),
        songs = listOf(
            FamousSong("Crazy in Love", listOf("crazy in love"), listOf("the intro horn is iconic forever", "Uh oh uh oh uh oh", "Jay-Z verse incoming", "this launched the Beyoncé solo era", "still one of the greatest pop songs ever made")),
            FamousSong("Single Ladies", listOf("single ladies"), listOf("put a ring on it", "the choreography is permanently in culture", "all the single ladies", "this won the VMA Beyoncé interrupted", "flawless from start to finish")),
            FamousSong("Lemonade", listOf("lemonade"), listOf("the album is a visual masterpiece", "Formation era changed everything", "lemonade was a film and an album and an event", "the Beyhive was not ready", "art at the highest level")),
            FamousSong("Formation", listOf("formation"), listOf("okay ladies now let's get in formation", "Super Bowl 50 performance was legendary", "Southern Black pride anthem", "this is a statement song", "I slay")),
            FamousSong("Halo", listOf("halo"), listOf("remember those walls I built", "this voice is from another dimension", "Beyoncé ballad is always devastating", "I can see your halo", "one of the greatest vocal performances in pop")),
            FamousSong("Irreplaceable", listOf("irreplaceable"), listOf("to the left to the left", "breakup anthem hall of fame", "this is a classic song", "everything you own in a box to the left", "the attitude on this track is chef's kiss")),
            FamousSong("Texas Hold 'Em", listOf("texas hold 'em", "texas hold em"), listOf("Beyoncé goes country and delivers", "this is RENAISSANCE ACT II energy", "two step to this immediately", "Western Bey is an era", "unexpected and perfect")),
            FamousSong("Love On Top", listOf("love on top"), listOf("the key changes in this song are legendary", "how many times does it modulate", "honey honey", "this is technically insane", "best key changes in pop music history")),
            FamousSong("Drunk in Love", listOf("drunk in love"), listOf("I've been drinking watermelon", "JAY-Z showed up for this", "the production is hypnotic", "drunk in love is a moment", "raw and intense Beyoncé")),
            FamousSong("CUFF IT", listOf("cuff it"), listOf("RENAISSANCE banger", "this is the most fun she's had on record", "the groove is immaculate", "turn this up immediately", "disco Beyoncé is everything"))
        )
    ),
    FamousArtist(
        name = "Bad Bunny",
        keywords = listOf("bad bunny"),
        reactions = listOf(
            "Benito o nada", "that's Bad Bunny.", "Puerto Rico in the room", "reggaetón hour", "conejo malo on repeat"
        ),
        songs = listOf(
            FamousSong("Tití Me Preguntó", listOf("tití me preguntó", "titi me pregunto", "titi me preguntó"), listOf("Un Verano Sin Ti was perfect", "this hook is stuck in my head", "beach vibes and banger energy", "Latin summer anthem", "the production is so bright and fun")),
            FamousSong("MO CARTA", listOf("mo carta"), listOf("nadie sabe lo que va a pasar mañana", "Bad Bunny storytelling hits different", "this is a cultural statement", "emotionally charged and raw", "Benito at his most reflective")),
            FamousSong("Yo Perreo Sola", listOf("yo perreo sola"), listOf("YHLQMDLG era classic", "she dances alone and that's the point", "feminist reggaetón anthem", "the music video said everything", "this was ahead of its time")),
            FamousSong("DAKITI", listOf("dakiti"), listOf("Jhay Cortez x Bad Bunny is perfect", "the production is silky", "esta noche baby quédate", "this was the song of the year", "trap and R&B reggaetón blend")),
            FamousSong("Callaíta", listOf("callaíta"), listOf("Tainy produced this perfectly", "El Conejo Malo at his smoothest", "this song has a whole mood", "callaíta callaíta", "romantic Bad Bunny hits different")),
            FamousSong("Me Porto Bonito", listOf("me porto bonito"), listOf("Chencho Corleone collab was fire", "Un Verano Sin Ti beach anthem", "this is the song of summer", "me porto bonito pa' ti", "laid back and infectious")),
            FamousSong("Si Veo a Tu Mamá", listOf("si veo a tu mamá", "si veo a tu mama"), listOf("YHLQMDLG deep cut", "this one hurts quietly", "if I see your mom", "Bad Bunny heartbreak hits different", "the vulnerability is real")),
            FamousSong("LA CANCIÓN", listOf("la canción"), listOf("J Balvin x Bad Bunny was iconic", "OASIS album delivered", "this is a tropical vibe", "la canción is a summer staple", "the collab everyone needed")),
            FamousSong("Efecto", listOf("efecto"), listOf("Un Verano Sin Ti standout", "that beat is so warm", "efecto efecto efecto", "this makes you want to dance", "Benito in full summer mode")),
            FamousSong("Un Verano Sin Ti", listOf("un verano sin ti"), listOf("the album not just the song", "this album made history on streaming", "a summer without you", "tropical and emotional at once", "the closing track is devastating"))
        )
    ),
    FamousArtist(
        name = "Dua Lipa",
        keywords = listOf("dua lipa"),
        reactions = listOf(
            "disco ball activated", "that's Dua.", "dance floor hours", "levitating, basically", "future nostalgia hits again"
        ),
        songs = listOf(
            FamousSong("Levitating", listOf("levitating"), listOf("I got you moonlight", "the DaBaby remix went crazy", "this was on repeat for a year", "Future Nostalgia peak pop", "turn up the disco ball")),
            FamousSong("Don't Start Now", listOf("don't start now", "dont start now"), listOf("the bass line is iconic", "did you forget our connection", "this is Future Nostalgia's moment", "the production is perfect", "no I don't need your love")),
            FamousSong("Physical", listOf("physical"), listOf("Olivia Newton-John energy updated", "the synth bassline is insane", "Future Nostalgia opener delivered", "work me like a doctor", "this is aerobics disco perfection")),
            FamousSong("New Rules", listOf("new rules"), listOf("one don't pick up the phone", "the rulebook every heartbreak needs", "this made Dua a global star", "the choreography is iconic", "three if he pulls you back")),
            FamousSong("One Kiss", listOf("one kiss"), listOf("Calvin Harris x Dua was massive", "one kiss is all it takes", "summer anthem 2018", "falling in love again", "this just makes you move")),
            FamousSong("Hotter than Hell", listOf("hotter than hell"), listOf("debut Dua was already delivering", "this is an underrated banger", "hotter than hell it's true", "raw energy from the start", "she was always going to be a star")),
            FamousSong("Break My Heart", listOf("break my heart"), listOf("INXS sample elevated this", "Future Nostalgia deep cut", "I knew from the start", "the production choice was inspired", "this is dance floor ready")),
            FamousSong("Illusion", listOf("illusion"), listOf("Radical Optimism era", "she came back with confidence", "that disco synth is addictive", "Dua in 2024 is still unmatched", "this is pure feel-good energy")),
            FamousSong("Electricity", listOf("electricity"), listOf("Silk City collab was perfect", "Mark Ronson brought the groove", "this is understated and great", "electricity running through me", "a sleeper hit from a great album")),
            FamousSong("IDGAF", listOf("idgaf"), listOf("debut era anthem", "you called me again drunk in the bathroom", "the self-respect anthem", "this is a breakup energy masterclass", "told you I'm fine but it wasn't the truth"))
        )
    ),
    FamousArtist(
        name = "Kendrick Lamar",
        keywords = listOf("kendrick lamar", "kendrick"),
        reactions = listOf(
            "Compton in the room", "that's Kendrick.", "bar for bar, no filler", "K.Dot hours", "the pen game is unfair"
        ),
        songs = listOf(
            FamousSong("HUMBLE.", listOf("humble"), listOf("sit down be humble", "Mike WiLL Made-It produced this perfectly", "the visuals matched the energy", "this was inescapable and rightfully so", "DAMN. era is a classic era")),
            FamousSong("Alright", listOf("alright"), listOf("we gon' be alright", "protest anthem of a generation", "To Pimp a Butterfly is a masterpiece", "this song took on a life beyond music", "Kendrick at his most important")),
            FamousSong("Money Trees", listOf("money trees"), listOf("good kid m.A.A.d city is a classic", "it's just me and you against the world", "Jay Rock verse goes hard", "that sample is beautiful", "the story in this song is vivid")),
            FamousSong("Swimming Pools", listOf("swimming pools"), listOf("the outro verse is elite", "good kid m.A.A.d city opening statement", "pour up drank head shot", "the flip in this song is brilliant", "addiction and peer pressure never sounded so good")),
            FamousSong("Not Like Us", listOf("not like us"), listOf("Drake diss that became a summer anthem", "Compton block party actually happened", "you're not a colleague you're a colonizer", "Kendrick won and we witnessed it", "this diss track crossed into pop culture")),
            FamousSong("DNA.", listOf("dna.", "dna"), listOf("DAMN. opener is violent and perfect", "I got loyalty, got royalty inside my DNA", "Mike WiLL beat switch mid-track", "the aggression here is controlled and precise", "Fox News used it and proved his point")),
            FamousSong("King Kunta", listOf("king kunta"), listOf("To Pimp a Butterfly funk era", "I got a bone to pick", "West Coast funk revival", "everybody wanna cut the legs off him", "that Kunta Kinte reference is loaded")),
            FamousSong("Backseat Freestyle", listOf("backseat freestyle"), listOf("good kid mAAd city raw energy", "park it in the front and the back", "the teenage recklessness is captured perfectly", "Martin had a dream Martin had a dream", "the energy is unmatched")),
            FamousSong("Poetic Justice", listOf("poetic justice"), listOf("Drake on a Kendrick track", "Janet Jackson sample is perfect", "if I told you a flower bloomed in a dark room", "good kid mAAd city standout", "this one is smooth and deep")),
            FamousSong("N95", listOf("n95"), listOf("Mr. Morale & The Big Steppers era", "take off that mask", "the production from Pharrell is insane", "Kendrick confronts his own issues here", "this album required bravery"))
        )
    ),
    FamousArtist(
        name = "Rihanna",
        keywords = listOf("rihanna"),
        reactions = listOf(
            "Barbados in the room", "that's Rihanna.", "Navy hours", "RiRi never misses", "we need an album, but this works too"
        ),
        songs = listOf(
            FamousSong("Umbrella", listOf("umbrella"), listOf("ella ella eh eh", "the song that changed everything", "this debut of an era was massive", "Jay-Z intro and then Rihanna delivered", "umbrella era Rihanna is iconic")),
            FamousSong("We Found Love", listOf("we found love"), listOf("Calvin Harris produced a perfect song", "found love in a hopeless place", "this was everywhere for months", "the emotion in the drop", "Rihanna x Calvin is magic")),
            FamousSong("Diamonds", listOf("diamonds"), listOf("shine bright like a diamond", "Sia wrote this and it shows", "the chorus is massive and simple", "this is an anthem for ages", "unapologetically beautiful song")),
            FamousSong("Work", listOf("work"), listOf("work work work work work", "Drake and Rihanna together again", "dancehall Rihanna is undefeated", "this was in every club in 2016", "ANTI album delivered")),
            FamousSong("Needed Me", listOf("needed me"), listOf("ANTI era dark Rihanna", "didn't they tell you that I was a savage", "the cold delivery is perfect", "this is her most unbothered track", "ANTI is her best album, easily")),
            FamousSong("Stay", listOf("stay"), listOf("Mikky Ekko on the hook", "not even sure what I need from you", "the vulnerability here is rare for Rihanna", "quiet devastation", "this proved her vocal range")),
            FamousSong("Disturbia", listOf("disturbia"), listOf("bum bum be-dum bum bum be-dum", "dark pop before dark pop was everywhere", "this bangs and always has", "disturbia was ahead of its time", "the horror pop aesthetic delivered")),
            FamousSong("Only Girl (In the World)", listOf("only girl"), listOf("I want you to make me feel", "the euphoria in this song is real", "Loud era Rihanna going massive", "this is pure pop perfection", "make me feel like I'm the only girl")),
            FamousSong("Love the Way You Lie", listOf("love the way you lie"), listOf("Eminem and Rihanna together", "just gonna stand there and watch me burn", "this hit a cultural nerve", "one of the biggest collaborations ever", "the chorus is chilling")),
            FamousSong("S&M", listOf("s&m"), listOf("Loud era banger", "sticks and stones may break my bones", "the provocative energy served a purpose", "this is just fun pop with attitude", "chains and whips excite me apparently"))
        )
    ),
    FamousArtist(
        name = "Ed Sheeran",
        keywords = listOf("ed sheeran"),
        reactions = listOf(
            "loop pedal starter pack", "that's Ed.", "acoustic hour", "this one's just guitar and heart", "math symbol album, probably"
        ),
        songs = listOf(
            FamousSong("Shape of You", listOf("shape of you"), listOf("I'm in love with the shape of you", "most streamed Spotify song for years", "maracas and a guitar loop and a hit", "this is undeniably catchy", "push and pull like a magnet do")),
            FamousSong("Perfect", listOf("perfect"), listOf("dancing in the dark with you", "wedding song of a generation", "this is romantically devastating", "I found a love for me", "the string section is perfect")),
            FamousSong("Thinking Out Loud", listOf("thinking out loud"), listOf("take me into your loving arms", "Sam Smith and Ed Sheeran were everywhere", "this became a wedding staple", "the Van Morrison influence is clear", "people fall in love in mysterious ways")),
            FamousSong("Castle on the Hill", listOf("castle on the hill"), listOf("driving at 90 down those country lanes", "+ era nostalgia perfected", "the production is cinematic", "this made everyone feel something about home", "one of his most underrated songs")),
            FamousSong("Bad Habits", listOf("bad habits"), listOf("every time you mention me and her", "Ed went pop-electronic and it worked", "the hook is massive", "vampire visual was a moment", "bad habits lead to late nights")),
            FamousSong("Photograph", listOf("photograph"), listOf("x era Ed is beautiful", "loving can hurt loving can hurt sometimes", "the vulnerability in this is real", "a love song that doesn't oversell", "we keep this love in a photograph")),
            FamousSong("A-Team", listOf("a-team", "a team"), listOf("the debut that made everyone notice", "white lips pale face", "this is heartbreaking storytelling", "social realism in a folk song", "Ed Sheeran at his rawest")),
            FamousSong("Galway Girl", listOf("galway girl"), listOf("she played the fiddle in an Irish band", "÷ era Irish folk pop", "this is irresistibly fun", "the Celtic energy is infectious", "she beat me at darts")),
            FamousSong("Bloodstream", listOf("bloodstream"), listOf("x era deep cut that hits hard", "I've been spinning out of time", "dark Ed Sheeran is underrated", "the production builds perfectly", "this is his most underrated era")),
            FamousSong("Shivers", listOf("shivers"), listOf("I got shivers down my spine", "= album opener is a banger", "Ed goes fully pop here and wins", "the disco energy is fun", "this works better than expected"))
        )
    ),
    FamousArtist(
        name = "Post Malone",
        keywords = listOf("post malone"),
        reactions = listOf(
            "tattoo-and-twang energy", "that's Posty.", "genre lines, ignored", "this one's laid-back chaos", "sad boy anthem incoming"
        ),
        songs = listOf(
            FamousSong("Rockstar", listOf("rockstar"), listOf("21 Savage on the hook delivered", "I've been f***ing hoes and popping pillies", "this was the number one song for weeks", "beerbongs and bentleys era", "the lo-fi intro is instantly recognizable")),
            FamousSong("Congratulations", listOf("congratulations"), listOf("they used to not believe in me", "stoney era Posty was raw", "the emotion in this is real", "Quavo collab was unexpected", "congratulations is still a tearjerker")),
            FamousSong("Sunflower", listOf("sunflower"), listOf("Spider-Man soundtrack gave us this gem", "there's a sunflower in my house", "need a friend", "this is deceptively simple and beautiful", "Swae Lee elevates everything he touches")),
            FamousSong("Better Now", listOf("better now"), listOf("Beerbongs era emotional hit", "I'm better now", "the guitar energy is country-adjacent", "this is Post's most melodic side", "you probably think that you are better now")),
            FamousSong("Circles", listOf("circles"), listOf("Hollywood's Bleeding standout", "we couldn't turn around", "pop-rock Post is his best era", "the production is polished and emotional", "this song is stuck in your head forever")),
            FamousSong("White Iverson", listOf("white iverson"), listOf("the song that started everything", "white Iverson I'm posting", "Allen Iverson tribute and debut banger", "this came out of nowhere and hit", "the cornrows were in full effect")),
            FamousSong("Saint-Tropez", listOf("saint-tropez", "saint tropez"), listOf("Hollywood's Bleeding luxury energy", "the lifestyle in this song is cinematic", "trap on a yacht basically", "Saint-Tropez sounds expensive", "the hook is effortlessly cool")),
            FamousSong("I Like You (A Happier Song)", listOf("i like you", "happier song"), listOf("Doja Cat x Post is perfect", "this is unironically joyful", "they both said let's just make a feel-good track", "happiness suits them both", "happy Post Malone is a thing and I like it")),
            FamousSong("Go Flex", listOf("go flex"), listOf("stoney era deep cut", "this was underground before it blew", "Post in his rawest form", "go flex for the culture", "the lo-fi rap energy is addictive")),
            FamousSong("Psycho", listOf("psycho"), listOf("Ty Dolla \$ign on this is perfect", "now she want a photo", "the calm delivery is everything", "beerbongs and bentleys delivered", "this just doesn't get old"))
        )
    ),
    FamousArtist(
        name = "Olivia Rodrigo",
        keywords = listOf("olivia rodrigo"),
        reactions = listOf(
            "teen angst, perfectly captured", "that's Olivia.", "breakup anthem hour", "sour into guts energy", "scream-singing recommended"
        ),
        songs = listOf(
            FamousSong("drivers license", listOf("drivers license", "driver's license"), listOf("I drove down your street", "this broke streaming records for a reason", "the bridge is a cry session", "debut single of the decade", "Red lights stop signs everything")),
            FamousSong("good 4 u", listOf("good 4 u"), listOf("well good for you I guess", "the pop-punk energy is immaculate", "this was in everyone's head", "Paramore vibes updated for Gen Z", "the sarcasm drips from every line")),
            FamousSong("brutal", listOf("brutal"), listOf("I'm so sick of seventeen", "SOUR opener delivered immediately", "the rock energy came out of nowhere", "angst perfectly articulated", "this is a generational anthem of confusion")),
            FamousSong("traitor", listOf("traitor"), listOf("brown guilty eyes", "the detail in these lyrics", "this is devastating in the best way", "the restraint here is impressive", "SOUR deep cut that rewards listening")),
            FamousSong("deja vu", listOf("deja vu"), listOf("do you get déjà vu", "this is subtle and cutting", "the sonic layering is impressive for a debut", "car rides to Malibu", "she used your tricks against you lyrically")),
            FamousSong("vampire", listOf("vampire"), listOf("GUTS opener smashed records", "what's a camera on this song", "the Elton John piano energy", "I loved you truly", "this was everywhere and deserved to be")),
            FamousSong("lacy", listOf("lacy"), listOf("oh lacy GUTS standout", "golden and glowing", "the worship and jealousy mixed is wild", "this is her most emotionally complex song", "it's subtle but hits later")),
            FamousSong("favorite crime", listOf("favorite crime"), listOf("SOUR emotional gut punch", "complicit in my own demise", "this is a quiet devastation track", "the harmony on this is beautiful", "I was your favorite crime")),
            FamousSong("enough for you", listOf("enough for you"), listOf("SOUR most underrated track", "I wore makeup on my days off", "the specificity of heartbreak here", "this song is quietly one of her best", "you made me feel like I wasn't enough")),
            FamousSong("all-american bitch", listOf("all-american bitch"), listOf("GUTS opener before vampire", "I'm grateful all the time", "the satirical performance is genius", "the soft verse into loud chorus", "America the beautiful subverted"))
        )
    ),
    FamousArtist(
        name = "Doja Cat",
        keywords = listOf("doja cat"),
        reactions = listOf(
            "internet's favorite chaos agent", "that's Doja.", "genre-hopping in real time", "this one's unbothered and iconic", "no rules, just vibes"
        ),
        songs = listOf(
            FamousSong("Say So", listOf("say so"), listOf("day to night to morning", "the Nicki remix elevated it", "this brought disco back for a minute", "say so into a relationship", "the production is warm and smooth")),
            FamousSong("Kiss Me More", listOf("kiss me more"), listOf("SZA and Doja together is iconic", "do you think about me", "the chemistry is effortless", "this was a summer anthem", "Planet Her delivered immediately")),
            FamousSong("Need to Know", listOf("need to know"), listOf("Planet Her standout", "do you got a big enough whatever", "the confidence is palpable", "trap R&B Doja is elite", "this song is unbothered cool")),
            FamousSong("Woman", listOf("woman"), listOf("I'm a woman na na na", "the African pop influence is beautiful", "Planet Her cultural reach", "this is a feminist banger", "the visuals matched the sound")),
            FamousSong("Mooo!", listOf("mooo", "moo!", "mooo!"), listOf("b*tch I'm a cow", "the internet made this a star", "SoundCloud to global was real", "Doja Cat was always chaotic", "this is what she meant about her range")),
            FamousSong("Planet Her", listOf("planet her"), listOf("the title track is underrated", "cosmic Doja is a vibe", "I want you on my planet", "the album title delivered", "dreamy and confident")),
            FamousSong("Streets", listOf("streets"), listOf("I want you devour you too", "this became a TikTok staple", "Hot Pink sleeper hit became massive", "you are my street love", "the longing in this song is real")),
            FamousSong("Get Into It (Yuh)", listOf("get into it", "yuh"), listOf("Planet Her energy in a bottle", "yuh yuh yuh get into it", "this is pure charisma", "Doja doing whatever she wants and winning", "the spoken word break is iconic")),
            FamousSong("Agora Hills", listOf("agora hills"), listOf("Scarlet era deeper emotion", "I want you at the Oscars", "luxury love song from Doja", "this era showed a new side", "the sentimentality is real and earned")),
            FamousSong("Paint The Town Red", listOf("paint the town red"), listOf("Scarlet opener was massive", "you say I'm crazy I say you're lazy", "the devil in the details", "this is Doja in full villain era", "the meme into anthem pipeline delivered"))
        )
    ),
    FamousArtist(
        name = "Travis Scott",
        keywords = listOf("travis scott"),
        reactions = listOf(
            "Astroworld in the room", "that's Travis.", "Cactus Jack hours", "this one's all rage and reverb", "Houston sound on deck"
        ),
        songs = listOf(
            FamousSong("SICKO MODE", listOf("sicko mode"), listOf("the beat switches are legendary", "Drake and Travis together", "is that Bruno Mars on the hook", "three beat switches and they all go hard", "Astroworld production is wild")),
            FamousSong("Goosebumps", listOf("goosebumps"), listOf("you give me goosebumps every time", "Birds in the Trap era", "Kendrick verse was a bonus", "the falsetto carries this", "psychedelic trap at its best")),
            FamousSong("Antidote", listOf("antidote"), listOf("Rodeo era breakthrough", "she's an antidote", "the hook is infectiously simple", "this made people pay attention", "Houston in the trap era")),
            FamousSong("HIGHEST IN THE ROOM", listOf("highest in the room"), listOf("highest in the room baby", "the production is spacey and perfect", "Travis at his most melodic", "this is a vibe track", "love is what I want but do I need it")),
            FamousSong("STARGAZING", listOf("stargazing"), listOf("Astroworld opener is perfect", "these pills got me stargazing", "the build in this song is theatrical", "Travis in full concept mode", "Astroworld is a great album")),
            FamousSong("Butterfly Effect", listOf("butterfly effect"), listOf("Huncho Jack era", "I got a lot to be thankful for", "the Quavo chemistry worked", "a butterfly on my cock I swear", "melodic Travis hits different")),
            FamousSong("WAKE UP", listOf("wake up"), listOf("The Weeknd on an Astroworld track", "the feature was perfect", "this is the emotional core of Astroworld", "wake me up", "two artists at their peak on one song")),
            FamousSong("way back", listOf("way back"), listOf("Birds in the Trap deep cut", "Kendrick verse again shows up", "the sample is psychedelic", "Travis storytelling mode activated", "Houston to the world")),
            FamousSong("Mamacita", listOf("mamacita"), listOf("Rodeo era Rich Homie Quon collab", "RICO is building to something", "the Spanish title fits the vibe", "this is early Travis and it already slaps", "Houston trap with Latin flair")),
            FamousSong("K-POP", listOf("k-pop", "kpop"), listOf("UTOPIA era massive track", "Bad Bunny and The Weeknd together", "this was unexpected and great", "K-POP but not really K-POP", "the feature list is wild"))
        )
    ),
    FamousArtist(
        name = "SZA",
        keywords = listOf("sza"),
        reactions = listOf(
            "that's SZA.", "CTRL-era feelings", "SZA on the speakers", "SOS hours", "good days, kind of"
        ),
        songs = listOf(
            FamousSong("Kill Bill", listOf("kill bill"), listOf("I might kill my ex", "SOS was a statement album", "the Uma Thurman reference lands", "this is darkly funny and real", "one of her most direct lyrics ever")),
            FamousSong("Good Days", listOf("good days"), listOf("this is such a hopeful-sad song", "stacking pennies in my head", "the falsetto is otherworldly", "Good Days was a pandemic gift", "she dropped it on her birthday and delivered")),
            FamousSong("Supermodel", listOf("supermodel"), listOf("CTRL opener is perfect", "I could be your supermodel", "her debut album was a classic instantly", "the guitar energy is raw", "this introduced SZA to the world")),
            FamousSong("The Weekend", listOf("the weekend"), listOf("on Mondays I talk to Louis", "CTRL standout that took time to blow", "the infidelity narrative is complicated", "the production is understated and great", "this song slowly became huge")),
            FamousSong("Snooze", listOf("snooze"), listOf("SOS centerpiece", "any man of mine", "the emotional depth here is staggering", "this is her best vocal performance", "would you snooze on a life with me")),
            FamousSong("Love Galore", listOf("love galore"), listOf("why you bother me when you know you don't want me", "Travis Scott verse elevates it", "CTRL era summer anthem", "the chemistry is undeniable", "leave me alone love galore")),
            FamousSong("Normal Girl", listOf("normal girl"), listOf("CTRL deep cut with big feelings", "I wish I was a normal girl", "the insecurity is painfully relatable", "this one hits quietly and hard", "simple production, massive emotion")),
            FamousSong("All the Stars", listOf("all the stars"), listOf("Black Panther soundtrack delivered", "Kendrick x SZA is a perfect collab", "I wanna honor you", "the cinematic quality matches the film", "all the stars above me")),
            FamousSong("Nobody Gets Me", listOf("nobody gets me"), listOf("SOS emotional center", "nobody gets me like you", "the heartbreak here is specific and real", "this is one of her most vulnerable", "SZA at her most open")),
            FamousSong("Blind", listOf("blind"), listOf("SOS fan favorite", "why do I always let you back in", "the guitar loop is addictive", "this is effortlessly SZA", "blind to the things you do"))
        )
    ),
    FamousArtist(
        name = "Bruno Mars",
        keywords = listOf("bruno mars"),
        reactions = listOf(
            "that's Bruno.", "funk and showmanship, incoming", "Bruno Mars on the speakers", "24K magic hours", "uptown funk vibes, basically"
        ),
        songs = listOf(
            FamousSong("Uptown Funk", listOf("uptown funk"), listOf("Mark Ronson produced a timeless song", "don't believe me just watch", "this was the song of 2015", "the funk revival was real", "too hot hot damn")),
            FamousSong("24K Magic", listOf("24k magic", "24 k magic"), listOf("24 karat magic in the air", "the album was underrated", "this is pure showmanship", "nobody does live performance like Bruno", "catch me in the Dominican Republic")),
            FamousSong("Locked Out of Heaven", listOf("locked out of heaven"), listOf("the Police influence is clear", "never had much faith but I needed belief", "Bruno's rock era was unexpected", "swim in the ocean while holding your hand", "this is a perfect pop song")),
            FamousSong("Just the Way You Are", listOf("just the way you are"), listOf("her eyes her eyes like a million stars", "debut single that made him a star", "this is a genuine love song no irony", "the simplicity is the genius", "you're amazing just the way you are")),
            FamousSong("Grenade", listOf("grenade"), listOf("I'd catch a grenade for ya", "Doo-Wops & Hooligans era emotion", "this is dramatic and beautiful", "the gospel choir energy", "easy come easy go")),
            FamousSong("That's What I Like", listOf("that's what i like", "thats what i like"), listOf("24K Magic standout", "I got a condo in Manhattan", "the luxury flex is fun", "the falsetto is impeccable", "this is pure smooth groove")),
            FamousSong("Leave the Door Open", listOf("leave the door open"), listOf("Silk Sonic is a perfect duo", "Anderson Paak and Bruno together", "the soul of classic R&B in 2021", "leave the door open baby", "this is retro done perfectly")),
            FamousSong("Treasure", listOf("treasure"), listOf("you are my treasure", "Unorthodox Jukebox delivered", "disco Bruno is irresistible", "this makes everyone move", "treasure that is what you are")),
            FamousSong("Versace on the Floor", listOf("versace on the floor"), listOf("24K Magic slow jam", "let's take our time tonight", "the build in this song is theatrical", "this is sophisticated R&B", "the chorus is stunning")),
            FamousSong("APT.", listOf("apt.", "apt"), listOf("ROSÉ and Bruno collab delivered", "the Korean drinking game energy", "this was a massive global hit", "so charming and fun", "k-pop meets pop perfection"))
        )
    ),
    FamousArtist(
        name = "Justin Bieber",
        keywords = listOf("justin bieber"),
        reactions = listOf(
            "that's Biebs.", "Justin Bieber on the speakers", "this one's smoother than expected", "the Biebs never really left", "pop royalty, still"
        ),
        songs = listOf(
            FamousSong("Baby", listOf("baby"), listOf("baby baby baby oh", "Ludacris verse is burned into history", "the song that launched a YouTube era", "like baby baby baby no", "this is objectively formative pop history")),
            FamousSong("Love Yourself", listOf("love yourself"), listOf("my mama don't like you", "Ed Sheeran wrote this and it shows", "the stripped back production works", "Purpose era was a comeback", "cause you should love yourself")),
            FamousSong("Sorry", listOf("sorry"), listOf("is it too late now to say sorry", "Purpose was a comeback album", "the dancehall production was fresh", "Skrillex and Blood co-produced this", "Justin said sorry and meant it")),
            FamousSong("Peaches", listOf("peaches"), listOf("I got my peaches out in Georgia", "Justice era smooth Justin", "Daniel Caesar and Giveon elevated this", "the vibes are immaculate", "this sounds like a warm evening")),
            FamousSong("Yummy", listOf("yummy"), listOf("you're so yummy yummy", "Changes era Justin", "the whisper vocals", "this is oddly addictive", "yummy yum for my tummy tummy")),
            FamousSong("What Do You Mean?", listOf("what do you mean"), listOf("Purpose era lead single delivered", "first time that you say sorry", "the Jack Ü production is clean", "what do you mean oh oh", "comeback Justin was at his smoothest")),
            FamousSong("Intentions", listOf("intentions"), listOf("picture perfect you don't need no filter", "Quavo x Bieber works somehow", "Changes era feel-good track", "intentions so clear", "the wholesome energy of this song")),
            FamousSong("Ghost", listOf("ghost"), listOf("Justice emotional standout", "young and in love it just gets harder", "this is genuinely moving", "the concept is simple and devastating", "if I can't be close to you")),
            FamousSong("Holy", listOf("holy"), listOf("Chance the Rapper on a Bieber track", "this is as close to gospel pop as it gets", "holy holy holy", "Justice era first single", "the faith element is sincere")),
            FamousSong("Boyfriend", listOf("boyfriend"), listOf("if I was your boyfriend I'd never let you go", "Believe era Justin was cool", "the swagger here is early", "swag swag swag on you", "Mike Posner co-wrote a banger"))
        )
    ),
    FamousArtist(
        name = "Adele",
        keywords = listOf("adele"),
        reactions = listOf(
            "that's Adele.", "someone grab the tissues", "Adele on the speakers", "that voice, still unmatched", "powerhouse ballad hours"
        ),
        songs = listOf(
            FamousSong("Hello", listOf("hello"), listOf("hello it's me", "the piano intro is iconic", "30 era comeback was everything", "I must have called a thousand times", "the music video in black and white is perfect")),
            FamousSong("Rolling in the Deep", listOf("rolling in the deep"), listOf("there's a fire starting in my heart", "21 is a classic album", "the drums hit like a wall", "we could have had it all", "the power in her voice is unreal")),
            FamousSong("Someone Like You", listOf("someone like you"), listOf("never mind I'll find someone like you", "the piano simplicity is genius", "this made everyone cry at the VMAs", "old friend why are you so shy", "the live version is extraordinary")),
            FamousSong("Set Fire to the Rain", listOf("set fire to the rain"), listOf("I set fire to the rain", "21 deep cut that is huge", "the contradiction in the title is the point", "this is vocally devastating", "watched it pour as I touched your face")),
            FamousSong("Easy On Me", listOf("easy on me"), listOf("go easy on me baby", "30 lead single was perfect", "the piano is lonely and beautiful", "there ain't no room for things to change", "Adele returned and delivered immediately")),
            FamousSong("Skyfall", listOf("skyfall"), listOf("this is the day I die", "Bond theme of the decade", "the cinematic scope of this song", "let the sky fall when it crumbles", "Oscar winner for a reason")),
            FamousSong("Chasing Pavements", listOf("chasing pavements"), listOf("19 era Adele is a debut classic", "should I give up or keep chasing pavements", "this song launched everything", "the heartbreak at nineteen is palpable", "a debut single that announced greatness")),
            FamousSong("Make You Feel My Love", listOf("make you feel my love"), listOf("Bob Dylan song done definitively", "when the rain is blowing in your face", "this is Adele at her most tender", "the restraint in her voice here", "she made this her own completely")),
            FamousSong("Water Under the Bridge", listOf("water under the bridge"), listOf("25 underrated banger", "say that it's not enough", "Adele doing uptempo is a treat", "this deserved more attention", "the chorus is massive")),
            FamousSong("Oh My God", listOf("oh my god"), listOf("30 uptempo surprise", "I can't keep on losing you", "Adele in a different mode", "the drums hit hard", "this is the most fun she's been on record"))
        )
    ),
    FamousArtist(
        name = "Frank Ocean",
        keywords = listOf("frank ocean"),
        reactions = listOf(
            "that's Frank.", "blonde-era hush", "Frank Ocean on the speakers", "reclusive genius hours", "quiet but it hits hard"
        ),
        songs = listOf(
            FamousSong("Pyramids", listOf("pyramids"), listOf("channel ORANGE ten-minute masterpiece", "the synth mid-section is John Mayer", "Cleopatra going by the name of Isis", "this song is a journey", "the musical ambition is staggering")),
            FamousSong("Thinking Bout You", listOf("thinking bout you"), listOf("a tornado flew around my room", "channel ORANGE breakthrough single", "the falsetto is devastating", "do you think about me still", "this became immediately iconic")),
            FamousSong("Nights", listOf("nights"), listOf("blonde midpoint and the beat switches", "everytime I see you in my dreams", "every night f*** every night", "the production flip is legendary", "nocturnal Frank is the best Frank")),
            FamousSong("Self Control", listOf("self control"), listOf("blonde emotional peak", "I, I, I know you won't", "the harmonics at the end are stunning", "summer's not as long as it used to be", "this ending wrecks you every time")),
            FamousSong("Ivy", listOf("ivy"), listOf("I thought that I was dreaming", "blonde album guitar work is peak", "the nostalgia is physically painful", "remember we were kids", "Frank and guitar is perfection")),
            FamousSong("Bad Religion", listOf("bad religion"), listOf("channel ORANGE most vulnerable moment", "a taxi driver confessional", "this is unrequited love perfectly captured", "it's a bad religion to be in love with someone", "Frank at his most emotionally raw")),
            FamousSong("Pink + White", listOf("pink + white", "pink and white"), listOf("blonde opener and it's perfect", "that's the way every day goes", "Beyoncé on the hook is subtle genius", "the production is celestial", "light of the morning wrapped in song")),
            FamousSong("Super Rich Kids", listOf("super rich kids"), listOf("Earl Sweatshirt verse is memorable", "too many bottles of this wine we can't pronounce", "channel ORANGE social commentary", "too many empty souls", "the satire is elegant")),
            FamousSong("Lost", listOf("lost"), listOf("channel ORANGE deep cut", "lost in it", "the acoustic foundation is beautiful", "one of his most melodically pure songs", "lost and I like how it feels")),
            FamousSong("Nikes", listOf("nikes"), listOf("blonde opener with pitch-shifted vocals", "rest in peace to Trayvon", "those wings on his back aren't for decoration", "the statement in the first two minutes", "Frank came back with something to say"))
        )
    ),
    FamousArtist(
        name = "Karol G",
        keywords = listOf("karol g"),
        reactions = listOf(
            "bichota hours", "that's Karol G.", "Latin pop royalty", "reggaetón with attitude", "mañana será bonito energy"
        ),
        songs = listOf(
            FamousSong("BICHOTA", listOf("bichota"), listOf("bichota bichota", "the confidence is a whole personality", "this redefined her career", "colombiana at the top", "la bichota arrived and stayed")),
            FamousSong("TQG", listOf("tqg"), listOf("Shakira x Karol G was everything", "te quedé grande", "this song is for the exes of exes", "te quedé grande papa", "two Colombian queens on one track")),
            FamousSong("PROVENZA", listOf("provenza"), listOf("mañana será bonito emotional core", "la carita tuya me la sé", "this is Karol at her most tender", "the folk-pop aesthetic works perfectly", "Colombian countryside in a song")),
            FamousSong("Mientras Me Curo del Cuervo", listOf("mientras me curo", "del cuervo"), listOf("heartbreak Karol hits different", "this is deeply personal and it shows", "mañana será bonito goes deep here", "healing and hurting simultaneously", "the vulnerability is earned")),
            FamousSong("Mi Ex Tenía Razón", listOf("mi ex tenía razón", "mi ex tenia razon"), listOf("my ex was right", "the confession is iconic", "Karol admitting the ex had a point", "this took honesty and courage", "charming and self-aware")),
            FamousSong("Cairo", listOf("cairo"), listOf("Ovy On The Drums production", "the beat is hypnotic", "this is a certified banger", "Cairo Cairo", "the production just keeps building")),
            FamousSong("Mamiii", listOf("mamiii"), listOf("Becky G x Karol G collab", "the girl anthem for the broken-hearted", "mamiii you're the one", "this song is for the WhatsApp screenshots", "the diss energy is classy")),
            FamousSong("QLONA", listOf("qlona"), listOf("mañana será bonito hard turn", "Peso Pluma collab was inspired", "qlona qlona", "the corrido trap blend works", "Karol going north Mexico and thriving")),
            FamousSong("Gatúbela", listOf("gatúbela", "gatubela"), listOf("Maluma x Karol G energy", "catwoman energy", "this is flirtatious and fun", "the chemistry is obvious", "gatúbela in her element")),
            FamousSong("La Bichota", listOf("la bichota"), listOf("the anthem that named an era", "la bichota is a lifestyle", "this established her dominance", "the street credibility is real", "bichota season every season"))
        )
    ),
    FamousArtist(
        name = "Lady Gaga",
        keywords = listOf("lady gaga"),
        reactions = listOf(
            "little monsters, assemble", "that's Gaga.", "Lady Gaga on the speakers", "born this way hours", "chromatica on deck"
        ),
        songs = listOf(
            FamousSong("Bad Romance", listOf("bad romance"), listOf("ra ra ah ah ah", "the music video changed pop visuals", "I want your love and I want your revenge", "this is an art piece disguised as a pop song", "want your bad romance")),
            FamousSong("Poker Face", listOf("poker face"), listOf("can't read my poker face", "The Fame era global domination", "this was everywhere and deserved to be", "the production is immaculate", "p-p-p-poker face")),
            FamousSong("Born This Way", listOf("born this way"), listOf("I was born this way", "the LGBTQ anthem of a generation", "Madonna comparisons came and she ignored them", "this is a cultural declaration", "you're beautiful in your way")),
            FamousSong("Shallow", listOf("shallow"), listOf("A Star Is Born was a triumph", "tell me something boy", "Bradley Cooper singing is unexpected", "the key change is everything", "I'm falling in all the good ways")),
            FamousSong("Just Dance", listOf("just dance"), listOf("debut single and already unstoppable", "just dance it'll be okay", "The Fame era is seminal", "this launched a career and an era", "RedOne produced a classic")),
            FamousSong("Applause", listOf("applause"), listOf("ARTPOP era underrated", "I live for the applause", "the theatrical entrance into that album", "ARTPOP as an album deserves re-evaluation", "pop music I live for it")),
            FamousSong("Edge of Glory", listOf("edge of glory"), listOf("Born This Way closing statement", "I'm on the edge of glory", "the saxophone solo is Bruce Springsteen", "this is euphoric pop at its best", "there ain't no reason you and me should be alone")),
            FamousSong("Telephone", listOf("telephone"), listOf("Beyoncé x Gaga is iconic", "stop callin' stop callin'", "The Fame Monster delivered", "the music video is a film", "I'm busy")),
            FamousSong("G.U.Y.", listOf("g.u.y.", "guy"), listOf("ARTPOP deep cut", "I wanna be the one beneath your skin", "this is underrated in her catalog", "the production is ambitious", "GUY energy is unapologetic")),
            FamousSong("Rain On Me", listOf("rain on me"), listOf("Ariana x Gaga was perfect", "I didn't ask for the rain", "this collab came at the right moment", "survivors making pop music", "Chromatica gift"))
        )
    ),
    FamousArtist(
        name = "Harry Styles",
        keywords = listOf("harry styles"),
        reactions = listOf(
            "that's Harry.", "fine line hours", "Harry Styles on the speakers", "watermelon sugar vibes", "post-1D glow up, still going"
        ),
        songs = listOf(
            FamousSong("Watermelon Sugar", listOf("watermelon sugar"), listOf("watermelon sugar high", "Fine Line summer anthem", "this is peak sunshine pop", "tastes like strawberries on a summer evening", "Harry went full 70s pop and won")),
            FamousSong("As It Was", listOf("as it was"), listOf("Harry come on it's as it was", "Harry's House lead single smashed records", "the melancholy in an upbeat song", "go home Harry it's as it was", "one of the biggest songs of 2022")),
            FamousSong("Adore You", listOf("adore you"), listOf("I'd walk through fire for you", "Fine Line emotional standout", "the music video with the fish", "just let me adore you", "the tender delivery is perfect")),
            FamousSong("Falling", listOf("falling"), listOf("Fine Line piano ballad masterpiece", "what am I now am I someone you just walk by", "this is vulnerable Harry at his best", "the bridge is a moment", "I'm falling again")),
            FamousSong("Sign of the Times", listOf("sign of the times"), listOf("debut Harry solo single was a statement", "just stop your crying", "the Bowie influence is real", "this told everyone he had something to say", "we never learn we've been here before")),
            FamousSong("Golden", listOf("golden"), listOf("Fine Line opener is gorgeous", "I'm hoping one day you'll make your way back to me", "golden golden golden", "the instrumentation is lush", "running away from you feels like falling")),
            FamousSong("Late Night Talking", listOf("late night talking"), listOf("Harry's House summer deep cut", "we've been doing all this late night talking", "the hook is irresistibly catchy", "this is feel-good Harry", "late night chatting into hits")),
            FamousSong("Music for a Sushi Restaurant", listOf("music for a sushi restaurant"), listOf("Harry's House opening statement", "green eyes fried rice I could cook it", "the goofiness is charming", "unexpected Harry banger", "this is pure fun and it works")),
            FamousSong("Treat People with Kindness", listOf("treat people with kindness"), listOf("Fine Line closing anthem", "maybe we can find a place", "the gospel choir energy is real", "TPWK is a lifestyle", "spreading joy through big pop music")),
            FamousSong("Cherry", listOf("cherry"), listOf("Fine Line heartbreak corner", "I forgot that you existed", "the French outro is from his then-girlfriend", "this is quietly devastating", "don't you call him what you used to call me"))
        )
    ),
    FamousArtist(
        name = "Kanye West",
        keywords = listOf("kanye west", "ye"),
        reactions = listOf(
            "that's Ye.", "Chicago production, unmistakable", "Kanye on the speakers", "college dropout hours", "production genius, love him or not"
        ),
        songs = listOf(
            FamousSong("GOLD DIGGER", listOf("gold digger"), listOf("Ray Charles sample is a masterpiece", "she take my money", "Late Registration era classic", "18 years 18 years", "this was the definitive 2005 pop song")),
            FamousSong("Stronger", listOf("stronger"), listOf("Daft Punk sample elevates everything", "Graduation era pop crossover", "what doesn't kill me makes me stronger", "this merged rap and electronic music perfectly", "still can't hold me down")),
            FamousSong("All Falls Down", listOf("all falls down"), listOf("College Dropout emotional depth", "we buy a lot of clothes but we don't really need 'em", "the Syleena Johnson hook is perfect", "this is social commentary in a love song", "College Dropout era Kanye was special")),
            FamousSong("Runaway", listOf("runaway"), listOf("My Beautiful Dark Twisted Fantasy", "let's have a toast for the douchebags", "the piano intro is legendary", "the 9-minute version is an experience", "this album is genuinely great")),
            FamousSong("Black Skinhead", listOf("black skinhead"), listOf("Yeezus industrial rap era", "for my families", "the production is confrontational and brilliant", "thirty minutes had an idea", "Daft Punk and Rick Rubin produced this")),
            FamousSong("Through the Wire", listOf("through the wire"), listOf("College Dropout debut single recorded with wired jaw", "chaka khan let me rock you", "the origin story is a song", "this was the introduction and it was perfect", "wired jaw rapping is commitment")),
            FamousSong("Good Life", listOf("good life"), listOf("Graduation era T-Pain collab", "this is the easy Kanye moment", "have you ever had a good life", "Graduation summer is real", "the optimism of this era")),
            FamousSong("Flashing Lights", listOf("flashing lights"), listOf("Graduation deep cut favorite", "flashing lights lights lights", "the sample flip is stunning", "this is one of his best album tracks", "lit up by her beauty")),
            FamousSong("Can't Tell Me Nothing", listOf("can't tell me nothing"), listOf("Graduation opener delivers immediately", "la la la la wait till I get my money right", "the defiance in this track", "this is Kanye at his most confident", "the Lil' Wayne remix exists too")),
            FamousSong("New Slaves", listOf("new slaves"), listOf("Yeezus political statement", "I will not let my son have an ego", "this is the thesis of Yeezus", "the Frank Ocean outro is stunning", "confrontational and necessary"))
        )
    ),
    FamousArtist(
        name = "Eminem",
        keywords = listOf("eminem"),
        reactions = listOf(
            "that's Slim Shady.", "Detroit in the room", "Eminem on the speakers", "rewind that line, seriously", "Marshall Mathers hours"
        ),
        songs = listOf(
            FamousSong("Lose Yourself", listOf("lose yourself"), listOf("8 Mile gave us an Oscar-winning song", "you only get one shot do not miss your chance", "this is the greatest rap motivational track", "the movie was good but this was better", "mom's spaghetti is now cultural shorthand")),
            FamousSong("Stan", listOf("stan"), listOf("my tea's gone cold I'm wondering why", "Dido on the hook is perfect", "this song defined obsessive fan culture", "the word stan comes from this song", "a horror story about parasocial relationships")),
            FamousSong("Without Me", listOf("without me"), listOf("guess who's back back back", "The Eminem Show era return", "two trailer park girls go round the outside", "this was a triumphant comeback single", "the self-awareness is refreshing")),
            FamousSong("The Real Slim Shady", listOf("the real slim shady"), listOf("will the real Slim Shady please stand up", "The Marshall Mathers LP era classic", "this was a cultural moment", "please stand up please stand up", "the cultural satire is sharp")),
            FamousSong("Not Afraid", listOf("not afraid"), listOf("Recovery era comeback", "I'm not afraid to take a stand", "sobriety anthem wrapped in a rap banger", "this was personal and universal", "Eminem found his way back")),
            FamousSong("Rap God", listOf("rap god"), listOf("the speed rap section is absurd", "I'm beginning to feel like a rap god", "the technical skill is on another level", "this is a flex and a half", "rewind and count those syllables")),
            FamousSong("Love the Way You Lie", listOf("love the way you lie"), listOf("Rihanna on the hook elevated this", "just gonna stand there and watch me burn", "Recovery era biggest hit", "this hit a cultural nerve", "the toxicity is depicted not endorsed")),
            FamousSong("Mockingbird", listOf("mockingbird"), listOf("Encore emotional peak", "I love you Hailie", "the tenderness here is unexpected", "hush little baby don't you cry", "the raw paternal love is moving")),
            FamousSong("Godzilla", listOf("godzilla"), listOf("Juice WRLD x Eminem is unexpected", "the speed at the end is inhuman", "Music to be Murdered By era", "Godzilla is a flex record", "the rap speed record attempt is real")),
            FamousSong("When I'm Gone", listOf("when i'm gone"), listOf("Curtain Call farewell track", "have you ever loved someone so much", "the daughter narrative returns", "this is one of his most emotional songs", "Hailie come back I just want to tell you"))
        )
    )
)

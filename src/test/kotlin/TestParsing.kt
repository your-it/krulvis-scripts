import org.junit.jupiter.api.Test

class TestParsing {

    @Test
    fun test_parsing(){
        val text = "at quality: 156<br>Best completed in: 6m 11s at quality: 164<br>You're awarded: 17040 Smithing XP and 34,080 coins.<br>"
        val quality = text.substring(text.indexOf("quality: ") + 9, text.indexOf("<br>")).toInt()
        val smithingXp = text.substring(text.indexOf("You're awarded: ") + 16, text.indexOf(" Smithing XP")).toInt()
        val coins = text.substring(text.indexOf("XP and ") + 7, text.indexOf(" coins")).replace(",", "").toInt()
        assert(quality == 156)
        assert(smithingXp == 17040)
        assert(coins == 34080)
    }
}
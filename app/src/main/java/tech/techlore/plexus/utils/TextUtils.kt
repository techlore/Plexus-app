/*
 *     Copyright (C) 2022-present Techlore
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package tech.techlore.plexus.utils

import android.content.Context
import android.util.Patterns
import tech.techlore.plexus.R

class TextUtils {
    
    companion object {
        
        fun hasRepeatedChars(charSequence: CharSequence): Boolean {
            val repeatedCharsRegex = """^(?s)(?!.*(.+)\1{2,}).*$""".toRegex() // *insert regex meme here*
            // This regex prevents words like AAAAA, BBBBB, ABBBB, ABABABAB etc.
            // while still allowing real words like coffee, committee etc.
            return repeatedCharsRegex.find(charSequence) == null
        }
        
        fun hasBlockedWord(context: Context, charSequence: CharSequence): Boolean {
            val blockedWords = context.resources.getStringArray(R.array.blocked_words)
            val blockedWordsPattern = blockedWords.joinToString("|") { Regex.escape(it) }
            val blockedWordsRegex =
                "(?i)\\b($blockedWordsPattern)\\b".toRegex(setOf(RegexOption.IGNORE_CASE)) // *next regex meme goes here*
            return blockedWordsRegex.find(charSequence) != null
        }
        
        fun hasEmojis(charSequence: CharSequence): Boolean {
            val emojiRegex = Regex("[\\p{So}\\p{Sk}]") // This will block emojis and other unnecessary symbols
            return emojiRegex.find(charSequence) != null
        }
        
        fun hasURL(charSequence: CharSequence): Boolean {
            return Patterns.WEB_URL.matcher(charSequence).find()
        }
    
        fun hasEmail(charSequence: CharSequence): Boolean {
            return Patterns.EMAIL_ADDRESS.matcher(charSequence).find()
        }
        
    }
    
}
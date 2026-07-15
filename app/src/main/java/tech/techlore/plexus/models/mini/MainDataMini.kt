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

package tech.techlore.plexus.models.mini

import androidx.annotation.StringRes
import tech.techlore.plexus.R

data class MainDataMini(
    val name: String,
    val packageName: String,
    val iconUrl: String? = null,
    val dgScore: Float,
    val mgScore: Float,
    val installedFrom: String,
    val isInstalled: Boolean,
    var isFav: Boolean
) {
    @get:StringRes
    val dgStatusStringResId: Int get() = getStatusStringResId(dgScore)
    
    @get:StringRes
    val mgStatusStringResId: Int get() = getStatusStringResId(mgScore)
    
    @StringRes
    private fun getStatusStringResId(score: Float): Int {
        return when (score) {
            0.0f -> R.string.na
            in 1.0f..1.9f -> R.string.broken_title
            in 2.0f..2.9f -> R.string.bronze_title
            in 3.0f..3.4f -> R.string.silver_title
            else -> R.string.gold_title
        }
    }
}
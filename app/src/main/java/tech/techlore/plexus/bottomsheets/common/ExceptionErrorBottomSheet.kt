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

package tech.techlore.plexus.bottomsheets.common

import tech.techlore.plexus.R

class ExceptionErrorBottomSheet(
    exception: Exception,
    negativeBtnText: String,
    onPositiveBtnClick: () -> Unit,
    onNegativeBtnClick: () -> Unit
) : BaseErrorBottomSheet() {
    
    override val titleTextResId = R.string.error_occurred_title
    override val descriptionText = TextOrRes.Text(exception.toString())
    override val negativeButtonText = negativeBtnText
    override val positiveBtnClickAction = onPositiveBtnClick
    override val negativeBtnClickAction = onNegativeBtnClick
}
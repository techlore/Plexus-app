/*
 * Copyright (c) 2022-present Techlore
 *
 *  This file is part of Plexus.
 *
 *  Plexus is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plexus is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Plexus.  If not, see <https://www.gnu.org/licenses/>.
 */

package tech.techlore.plexus.utils

class DbUtils {
    
    companion object {
        
        fun truncatedDgScore(dgScore: Float): Float {
            val truncatedDgScore = ((dgScore * 10.0f).toInt().toFloat()) / 10.0f
            val finalScore =
                if (dgScore != truncatedDgScore) {
                    truncatedDgScore
                    // If score is 2 decimal places,
                    // convert to 1 decimal place without rounding off
                }
                else {
                    dgScore
                }
    
            return finalScore
        }
    
        fun truncatedMgScore(mgScore: Float): Float {
            val truncatedMgScore = ((mgScore * 10.0f).toInt().toFloat()) / 10.0f
            val finalScore =
                if (mgScore != truncatedMgScore) {
                    truncatedMgScore
                    // If score is 2 decimal places,
                    // convert to 1 decimal place without rounding off
                }
                else {
                    mgScore
                }
        
            return finalScore
        }
        
    }
    
}
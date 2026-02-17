/*
 * OpenSchoolCloud Calendar
 * Copyright (C) 2025 OpenSchoolCloud / Aldewereld Consultancy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package nl.openschoolcloud.calendar.presentation.theme

import androidx.compose.ui.graphics.Color

/**
 * OpenSchoolCloud Brand Colors
 * Conform Huisstijlgids v1.0
 */
object OSCColors {
    // Primaire kleuren
    val OscBlauw = Color(0xFF3B9FD9)        // #3B9FD9 - Primair / Logo
    val DonkerBlauw = Color(0xFF2B7FB9)     // #2B7FB9 - Koppen / Accenten
    val LichtBlauw = Color(0xFFE8F4FB)      // #E8F4FB - Achtergronden
    val Wit = Color(0xFFFFFFFF)             // #FFFFFF - Achtergrond

    // Tekst kleuren
    val DonkerGrijs = Color(0xFF333333)     // #333333 - Hoofdtekst
    val Grijs = Color(0xFF666666)           // #666666 - Secundaire tekst

    // Feedback kleuren
    val SuccesGroen = Color(0xFF4CAF50)     // #4CAF50 - Checkmarks / Voordelen
    val FoutRood = Color(0xFFE53935)        // Error states
    val WaarschuwingOranje = Color(0xFFFFA726) // Warning states
}

// Light theme colors
val md_theme_light_primary = OSCColors.OscBlauw
val md_theme_light_onPrimary = OSCColors.Wit
val md_theme_light_primaryContainer = OSCColors.LichtBlauw
val md_theme_light_onPrimaryContainer = OSCColors.DonkerBlauw

val md_theme_light_secondary = OSCColors.DonkerBlauw
val md_theme_light_onSecondary = OSCColors.Wit
val md_theme_light_secondaryContainer = OSCColors.LichtBlauw
val md_theme_light_onSecondaryContainer = OSCColors.DonkerBlauw

val md_theme_light_tertiary = OSCColors.SuccesGroen
val md_theme_light_onTertiary = OSCColors.Wit

val md_theme_light_error = OSCColors.FoutRood
val md_theme_light_onError = OSCColors.Wit
val md_theme_light_errorContainer = Color(0xFFFFDAD6)
val md_theme_light_onErrorContainer = Color(0xFF410002)

val md_theme_light_background = OSCColors.Wit
val md_theme_light_onBackground = OSCColors.DonkerGrijs
val md_theme_light_surface = OSCColors.Wit
val md_theme_light_onSurface = OSCColors.DonkerGrijs
val md_theme_light_surfaceVariant = OSCColors.LichtBlauw
val md_theme_light_onSurfaceVariant = OSCColors.Grijs

val md_theme_light_outline = Color(0xFFBDBDBD)
val md_theme_light_outlineVariant = Color(0xFFE0E0E0)

// Dark theme colors — blue-tinged dark for OSC brand identity
val md_theme_dark_primary = Color(0xFF5BB8F0)           // Lighter blue for dark bg readability
val md_theme_dark_onPrimary = Color(0xFF003258)
val md_theme_dark_primaryContainer = Color(0xFF1E4D7B)
val md_theme_dark_onPrimaryContainer = Color(0xFFD0E8FF)

val md_theme_dark_secondary = Color(0xFF90CAF9)
val md_theme_dark_onSecondary = Color(0xFF003258)
val md_theme_dark_secondaryContainer = Color(0xFF1E3A5F)
val md_theme_dark_onSecondaryContainer = Color(0xFFD0E8FF)

val md_theme_dark_tertiary = Color(0xFF81C784)
val md_theme_dark_onTertiary = Color(0xFF1B5E20)

val md_theme_dark_error = Color(0xFFFFB4AB)
val md_theme_dark_onError = Color(0xFF690005)
val md_theme_dark_errorContainer = Color(0xFF93000A)
val md_theme_dark_onErrorContainer = Color(0xFFFFDAD6)

val md_theme_dark_background = Color(0xFF1A1A2E)        // Blue-tinged dark background
val md_theme_dark_onBackground = Color(0xFFE2E2E8)
val md_theme_dark_surface = Color(0xFF252540)            // Blue-tinged surface
val md_theme_dark_onSurface = Color(0xFFE2E2E8)
val md_theme_dark_surfaceVariant = Color(0xFF2E2E4A)     // Blue-tinged surface variant
val md_theme_dark_onSurfaceVariant = Color(0xFFC0C0CC)

val md_theme_dark_outline = Color(0xFF6A6A80)
val md_theme_dark_outlineVariant = Color(0xFF3A3A54)

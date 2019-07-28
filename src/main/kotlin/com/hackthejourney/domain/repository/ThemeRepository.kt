package com.hackthejourney.domain.repository

import com.hackthejourney.domain.model.Theme

interface ThemeRepository {

    fun all(): List<Theme>
    fun byName(themeName: String): Theme?

}

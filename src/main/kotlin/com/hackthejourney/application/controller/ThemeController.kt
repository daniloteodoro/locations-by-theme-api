package com.hackthejourney.application.controller

import com.hackthejourney.application.dto.ThemeSimple
import com.hackthejourney.application.dto.TravelApiResponse
import com.hackthejourney.domain.model.Theme
import com.hackthejourney.domain.repository.ThemeRepository
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@CrossOrigin
@RestController
@RequestMapping("/themes", produces = [MediaType.APPLICATION_JSON_VALUE])
@Api(value = "themes", tags = ["Themes"], description = "Contain information about travel themes, such as Skiing or going to the beach.")
class ThemeController(private val themeRepository: ThemeRepository) {

    @ApiOperation("List all travel themes.")
    @GetMapping
    fun listThemes() =
        ResponseEntity.ok(TravelApiResponse(themeRepository.all().map{ThemeSimple.of(it)}))

    @ApiOperation("Search themes by name and return detailed information.")
    @GetMapping("/{name}")
    fun getByName(@PathVariable name: String): ResponseEntity<TravelApiResponse<Theme>> {
        val found = themeRepository.byName(name)
        return if (found != null)
            return ResponseEntity.ok(TravelApiResponse(found))
        else
            ResponseEntity.notFound().build()
    }

}
